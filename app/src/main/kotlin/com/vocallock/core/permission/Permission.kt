package com.vocallock.core.permission

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.PowerManager
import android.os.Process
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.compose.koinInject

// ── PermissionStatus ──────────────────────────────────────────

enum class PermissionStatus { GRANTED, DENIED, PERMANENTLY_DENIED, UNKNOWN }

sealed class UiPermissionEvent {
    data class RequirePermission(val permission: String) : UiPermissionEvent()
}

// ── PermissionManager ─────────────────────────────────────────

class PermissionManager(private val context: Context) {

    private val _states = MutableStateFlow(
        mapOf(
            Manifest.permission.RECORD_AUDIO to PermissionStatus.UNKNOWN,
            Manifest.permission.POST_NOTIFICATIONS to PermissionStatus.UNKNOWN,
        )
    )
    val states: StateFlow<Map<String, PermissionStatus>> = _states.asStateFlow()

    val hasAudio get() = _states.value[Manifest.permission.RECORD_AUDIO] == PermissionStatus.GRANTED
    val hasNotifications get() = _states.value[Manifest.permission.POST_NOTIFICATIONS] == PermissionStatus.GRANTED

    fun refresh() {
        _states.value = _states.value.keys.associateWith { perm ->
            if (ContextCompat.checkSelfPermission(
                    context,
                    perm
                ) == PackageManager.PERMISSION_GRANTED
            )
                PermissionStatus.GRANTED else PermissionStatus.DENIED
        }
    }

    fun update(permission: String, granted: Boolean, showRationale: Boolean) {
        val status = when {
            granted -> PermissionStatus.GRANTED
            showRationale -> PermissionStatus.DENIED
            else -> PermissionStatus.PERMANENTLY_DENIED
        }
        _states.value += (permission to status)
    }

    fun isAccessibilityEnabled(): Boolean {
        val mgr = context.getSystemService(Context.ACCESSIBILITY_SERVICE)
                as android.view.accessibility.AccessibilityManager
        return mgr.isEnabled
    }

    // Check for Usage Access
    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // Check for Battery Optimization bypass
    fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
}

// ── PermissionHandlerComposable ───────────────────────────────

@Composable
fun PermissionHandlerComposable(
    events: MutableSharedFlow<UiPermissionEvent>,
    content: @Composable () -> Unit,
) {
    val permissionManager: PermissionManager = koinInject()

    val audioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionManager.update(Manifest.permission.RECORD_AUDIO, granted, !granted)
    }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionManager.update(Manifest.permission.POST_NOTIFICATIONS, granted, !granted)
    }

    LaunchedEffect(Unit) {
        permissionManager.refresh()
        events.collect { event ->
            when (event) {
                is UiPermissionEvent.RequirePermission -> when (event.permission) {
                    Manifest.permission.RECORD_AUDIO -> audioLauncher.launch(event.permission)
                    Manifest.permission.POST_NOTIFICATIONS -> notifLauncher.launch(event.permission)
                }
            }
        }
    }

    content()
}