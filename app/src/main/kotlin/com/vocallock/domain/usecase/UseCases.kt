package com.vocallock.domain.usecase

import com.vocallock.domain.repository.NudgeRepository

// ── Nudge ─────────────────────────────────────────────────────

class TrackInheritedUsageUseCase(private val repo: NudgeRepository) {
    suspend operator fun invoke(pkg: String) = repo.increment(pkg)
}

class ShouldShowNudgeUseCase(private val repo: NudgeRepository) {
    suspend operator fun invoke(pkg: String, threshold: Int = 3): Boolean {
        val s = repo.getState(pkg) ?: return false
        return !s.snoozed && s.usageCount >= threshold
    }
}

class SnoozeNudgeUseCase(private val repo: NudgeRepository) {
    suspend operator fun invoke(pkg: String) = repo.snooze(pkg)
}
