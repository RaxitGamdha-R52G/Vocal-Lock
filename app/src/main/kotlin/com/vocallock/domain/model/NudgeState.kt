package com.vocallock.domain.model

data class NudgeState(
    val packageName: String,
    val usageCount: Int,
    val snoozed: Boolean,
    val sessionStartMillis: Long,
)
