package com.vocallock.data.repository

import android.content.Context
import com.vocallock.data.datastore.nudgeStateDataStore
import com.vocallock.data.datastore.proto.AppNudgeEntry
import com.vocallock.data.datastore.proto.NudgeStatePrefs
import com.vocallock.domain.model.NudgeState
import com.vocallock.domain.repository.NudgeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class NudgeRepositoryImpl(private val context: Context) : NudgeRepository {
    private val store get() = context.nudgeStateDataStore
    private val sessionMs = 6L * 60 * 60 * 1000

    override fun observeState(packageName: String): Flow<NudgeState?> =
        store.data.map { prefs ->
            prefs.appStatesMap[packageName]?.let {
                NudgeState(packageName, it.usageCount, it.snoozed, it.sessionStartMillis)
            }
        }

    override suspend fun getState(packageName: String): NudgeState? {
        val entry = store.data.first().appStatesMap[packageName] ?: return null
        return NudgeState(packageName, entry.usageCount, entry.snoozed, entry.sessionStartMillis)
    }

    override suspend fun increment(packageName: String) {
        val now = System.currentTimeMillis()
        store.updateData { prefs ->
            val cur = prefs.appStatesMap[packageName]
            val newSess = cur == null || (now - cur.sessionStartMillis) > sessionMs
            val entry = AppNudgeEntry.newBuilder()
                .setUsageCount(if (newSess) 1 else (cur.usageCount ?: 0) + 1)
                .setSnoozed(cur?.snoozed ?: false)
                .setSessionStartMillis(if (newSess) now else cur.sessionStartMillis ?: now)
                .build()
            NudgeStatePrefs.newBuilder(prefs).putAppStates(packageName, entry).build()
        }
    }

    override suspend fun snooze(packageName: String) {
        store.updateData { prefs ->
            val cur = prefs.appStatesMap[packageName] ?: AppNudgeEntry.getDefaultInstance()
            NudgeStatePrefs.newBuilder(prefs)
                .putAppStates(packageName, cur.toBuilder().setSnoozed(true).build())
                .build()
        }
    }

    override suspend fun reset(packageName: String) {
        store.updateData { prefs ->
            NudgeStatePrefs.newBuilder(prefs).removeAppStates(packageName).build()
        }
    }
}
