package com.vocallock.feature.settings

import android.Manifest
import android.content.Context
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocallock.core.permission.PermissionManager
import com.vocallock.data.datastore.proto.GlobalSettingsPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val permMgr: PermissionManager,
    private val globalSettings: DataStore<GlobalSettingsPrefs>
) : ViewModel() {

    private val _s = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _s.asStateFlow()

    init {
        viewModelScope.launch {
            permMgr.states.collect { m ->
                _s.update {
                    it.copy(
                        hasAudio = m[Manifest.permission.RECORD_AUDIO]?.name == "GRANTED",
                        hasNotifications = m[Manifest.permission.POST_NOTIFICATIONS]?.name == "GRANTED",
                    )
                }
            }
        }
        viewModelScope.launch {
            globalSettings.data.collect { prefs ->
                _s.update {
                    it.copy(
                        maxGridTiles = prefs.maxGridTiles,
                        nudgeThreshold = prefs.nudgeThreshold
                    )
                }
            }
        }
    }

    fun refreshStates(context: Context) {
        _s.update {
            it.copy(
                accessibilityEnabled = permMgr.isAccessibilityEnabled(),
                isOverlayEnabled = Settings.canDrawOverlays(context),
                hasUsageAccess = permMgr.hasUsageAccess(),
                isIgnoringBatteryOptimizations = permMgr.isIgnoringBatteryOptimizations()
            )
        }
    }

    fun setThreshold(v: Int) {
        viewModelScope.launch {
            globalSettings.updateData { prefs -> prefs.toBuilder().setNudgeThreshold(v).build() }
        }
    }

    fun setMaxGridTiles(v: Int) {
        viewModelScope.launch {
            globalSettings.updateData { prefs -> prefs.toBuilder().setMaxGridTiles(v).build() }
        }
    }
}