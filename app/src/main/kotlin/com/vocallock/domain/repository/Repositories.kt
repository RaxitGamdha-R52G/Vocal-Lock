package com.vocallock.domain.repository

import com.vocallock.domain.model.NudgeState
import kotlinx.coroutines.flow.Flow

interface NudgeRepository {
    fun observeState(packageName: String): Flow<NudgeState?>
    suspend fun getState(packageName: String): NudgeState?
    suspend fun increment(packageName: String)
    suspend fun snooze(packageName: String)
    suspend fun reset(packageName: String)
}
