package com.vocallock.feature.selector

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocallock.data.database.dao.AppDao
import com.vocallock.data.database.entity.AppEntity
import com.vocallock.data.provider.AppIconProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSelectorViewModel(
    savedStateHandle: SavedStateHandle,
    private val appDao: AppDao,
    private val iconProvider: AppIconProvider
) : ViewModel() {

    private val targetGroupId: String? = savedStateHandle["groupId"]

    private val _state = MutableStateFlow(AppSelectorUiState())
    val state: StateFlow<AppSelectorUiState> = _state.asStateFlow()

    private var allInstalledApps = listOf<SelectableApp>()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val lockedApps = appDao.observeAllApps().first()
            val lockedPackageNames = lockedApps.map { it.packageName }.toSet()

            val apps = withContext(Dispatchers.IO) {
                iconProvider.getInstalledApps()
                    .filter { it.packageName !in lockedPackageNames }
                    .map { info ->
                        SelectableApp(
                            packageName = info.packageName,
                            label = info.loadLabel(iconProvider.packageManager).toString(),
                            icon = info.loadIcon(iconProvider.packageManager)
                        )
                    }.sortedBy { it.label }
            }
            allInstalledApps = apps
            _state.update { it.copy(isLoading = false, apps = apps) }
        }
    }

    fun onEvent(event: AppSelectorUiEvent) {
        when (event) {
            is AppSelectorUiEvent.OnSearchChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
                filterApps(event.query)
            }

            is AppSelectorUiEvent.OnAppToggled -> {
                _state.update { currentState ->
                    val updatedList = currentState.apps.map {
                        if (it.packageName == event.packageName) it.copy(isSelected = !it.isSelected) else it
                    }
                    currentState.copy(apps = updatedList)
                }
            }

            is AppSelectorUiEvent.OnConfirmSelection -> saveSelectedApps()
            is AppSelectorUiEvent.OnBackClicked -> {}
        }
    }

    private fun filterApps(query: String) {
        val filtered = if (query.isBlank()) allInstalledApps else allInstalledApps.filter {
            it.label.contains(
                query,
                ignoreCase = true
            )
        }
        _state.update { it.copy(apps = filtered) }
    }

    private fun saveSelectedApps() {
        viewModelScope.launch {
            val selected = _state.value.apps.filter { it.isSelected }
            selected.forEach { app ->
                appDao.upsertApp(
                    AppEntity(
                        packageName = app.packageName,
                        displayName = app.label,
                        groupId = targetGroupId,
                        authTypeOverride = null,
                        encryptedSecretOverride = null,
                        secretIvOverride = null,
                        recoveryHashOverride = null,
                        voiceUnlockPhraseOverride = null,
                        voiceLockPhraseOverride = null,
                        isStrictOverride = null,
                        orderIndex = System.currentTimeMillis().toInt()
                    )
                )
            }
        }
    }
}