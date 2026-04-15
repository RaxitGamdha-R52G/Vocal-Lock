package com.vocallock.feature.settings

data class SettingsUiState(
    val hasAudio: Boolean = false,
    val hasNotifications: Boolean = false,
    val accessibilityEnabled: Boolean = false,
    val isDeviceAdminEnabled: Boolean = false,
    val isOverlayEnabled: Boolean = false,
    val hasUsageAccess: Boolean = false,
    val isIgnoringBatteryOptimizations: Boolean = false,
    val nudgeThreshold: Int = 3,
    val maxGridTiles: Int = 4
)