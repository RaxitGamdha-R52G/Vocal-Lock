package com.vocallock.service

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.datastore.core.DataStore
import com.vocallock.data.database.dao.AppDao
import com.vocallock.data.database.dao.GroupDao
import com.vocallock.data.datastore.proto.GlobalSettingsPrefs
import com.vocallock.domain.usecase.ShouldShowNudgeUseCase
import com.vocallock.domain.usecase.TrackInheritedUsageUseCase
import com.vocallock.feature.overlay.LockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class VocalLockAccessibilityService : AccessibilityService() {

    companion object {
        val sessionUnlockedApps = mutableSetOf<String>()
        var activeLockScreenPackage: String? = null
    }

    private val appDao: AppDao by inject()
    private val groupDao: GroupDao by inject()
    private val trackUsage: TrackInheritedUsageUseCase by inject()
    private val shouldShowNudge: ShouldShowNudgeUseCase by inject()
    private val nudgeManager: NudgeManager by inject()
    private val globalSettings: DataStore<GlobalSettingsPrefs> by inject()

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var usageStatsManager: UsageStatsManager

    private val systemReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.vocallock.UNLOCK_APP" -> {
                    val unlockedPackage = intent.getStringExtra("UNLOCKED_PACKAGE")
                    val usedMaster = intent.getBooleanExtra("USED_MASTER_PASSWORD", false)

                    if (unlockedPackage != null && usedMaster) {
                        serviceScope.launch {
                            trackUsage(unlockedPackage)
                            val threshold = globalSettings.data.first().nudgeThreshold
                            if (shouldShowNudge(
                                    unlockedPackage,
                                    threshold
                                )
                            ) nudgeManager.showSetupNudge("App")
                        }
                    }
                }

                Intent.ACTION_SCREEN_OFF -> {
                    Log.d("VocalLock_Watchdog", "Screen off. Wiping session memory.")
                    sessionUnlockedApps.clear()
                    activeLockScreenPackage = null
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val filter = IntentFilter().apply {
            addAction("com.vocallock.UNLOCK_APP")
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(systemReceiver, filter, RECEIVER_NOT_EXPORTED)

        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        startUsageStatsMonitor()
    }

    private var lastEventTime = System.currentTimeMillis()

    private fun startUsageStatsMonitor() {
        serviceScope.launch {
            while (isActive) {
                val time = System.currentTimeMillis()
                val events = usageStatsManager.queryEvents(time - 2000, time)
                val event = UsageEvents.Event()

                var maxTimeProcessed = lastEventTime

                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (event.timeStamp > lastEventTime) {
                        maxTimeProcessed = maxOf(maxTimeProcessed, event.timeStamp)

                        when (event.eventType) {
                            UsageEvents.Event.ACTIVITY_RESUMED -> {
                                evaluateForegroundApp(event.packageName)
                            }

                            24 -> {
                                if (sessionUnlockedApps.contains(event.packageName)) {
                                    Log.d(
                                        "VocalLock_Watchdog",
                                        "${event.packageName} was KILLED from recents. Wiping memory."
                                    )
                                    sessionUnlockedApps.remove(event.packageName)
                                    if (activeLockScreenPackage == event.packageName) {
                                        activeLockScreenPackage = null
                                    }
                                }
                            }
                        }
                    }
                }
                lastEventTime = maxTimeProcessed
                delay(300)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        evaluateForegroundApp(packageName)
    }

    private fun evaluateForegroundApp(packageName: String) {
        val ignoredPackages = listOf(
            applicationContext.packageName,
            "com.google.android.inputmethod.latin",
            "com.samsung.android.honeyboard",
            "com.android.systemui",
            "com.google.android.googlequicksearchbox",
            "com.google.android.tts"
        )
        if (ignoredPackages.any { packageName.contains(it, ignoreCase = true) }) return

        if (packageName.contains("launcher", ignoreCase = true)) {
            activeLockScreenPackage = null
            return
        }

        if (sessionUnlockedApps.contains(packageName)) {
            if (activeLockScreenPackage == packageName) activeLockScreenPackage = null
            return
        }

        if (activeLockScreenPackage != null && packageName != activeLockScreenPackage) {
            activeLockScreenPackage = null
        }

        serviceScope.launch {
            val appEntity = appDao.getAppByPackage(packageName) ?: return@launch
            val groupEntity =
                if (appEntity.groupId != null) groupDao.getGroupById(appEntity.groupId) else null

            val requiredSecretBytes =
                appEntity.encryptedSecretOverride ?: groupEntity?.encryptedSecret
            val targetPhrase = appEntity.voiceUnlockPhraseOverride ?: groupEntity?.voiceUnlockPhrase
            val isStrict = appEntity.isStrictOverride ?: groupEntity?.isStrict ?: false
            val authType = appEntity.authTypeOverride ?: groupEntity?.authType ?: "TEXT_PASSWORD"
            val isUsingMasterPassword =
                appEntity.encryptedSecretOverride == null && groupEntity?.encryptedSecret != null

            if (requiredSecretBytes != null && activeLockScreenPackage != packageName) {
                activeLockScreenPackage = packageName

                val lockIntent =
                    Intent(this@VocalLockAccessibilityService, LockActivity::class.java).apply {
                        putExtra("PACKAGE_NAME", packageName)
                        putExtra("APP_NAME", appEntity.displayName)
                        putExtra("AUTH_TYPE", authType)
                        putExtra("TARGET_PHRASE", targetPhrase)
                        putExtra("IS_STRICT", isStrict)
                        putExtra("PASSWORD_BYTES", requiredSecretBytes)
                        putExtra("USED_MASTER_PASSWORD", isUsingMasterPassword)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                startActivity(lockIntent)
            }
        }
    }

    override fun onInterrupt() {}
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try {
            unregisterReceiver(systemReceiver)
        } catch (e: Exception) {
        }
    }
}