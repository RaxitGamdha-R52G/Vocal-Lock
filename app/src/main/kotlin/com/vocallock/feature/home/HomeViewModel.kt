package com.vocallock.feature.home

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocallock.data.database.dao.AppDao
import com.vocallock.data.database.dao.GroupDao
import com.vocallock.data.database.entity.GroupEntity
import com.vocallock.data.datastore.proto.GlobalSettingsPrefs
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class HomeViewModel(
    private val appDao: AppDao,
    private val groupDao: GroupDao,
    private val globalSettings: DataStore<GlobalSettingsPrefs>
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<HomeUiEffect>()
    val uiEffect: SharedFlow<HomeUiEffect> = _uiEffect.asSharedFlow()

    init {
        observeGridData()
        observeGlobalSettings()
    }

    private fun observeGlobalSettings() {
        viewModelScope.launch {
            globalSettings.data.collect { prefs ->
                _state.update { it.copy(maxGridTiles = prefs.maxGridTiles) }
            }
        }
    }

    private fun observeGridData() {
        viewModelScope.launch {
            combine(
                appDao.observeAllApps(),
                groupDao.observeAllGroups()
            ) { allApps, allGroups ->
                val standalone = allApps.filter { it.groupId == null }
                val grouped = allGroups.associateWith { group ->
                    allApps.filter { app -> app.groupId == group.id }
                }
                _state.value.copy(
                    isLoading = false,
                    standaloneApps = standalone,
                    groupsWithApps = grouped
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnAppClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(
                        HomeUiEffect.NavigateToDetail(
                            event.packageName,
                            false
                        )
                    )
                }
            }

            is HomeUiEvent.OnGroupClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(
                        HomeUiEffect.NavigateToDetail(
                            event.groupId,
                            true
                        )
                    )
                }
            }

            is HomeUiEvent.OnCreateGroupClicked -> _state.update { it.copy(showCreateGroupDialog = true) }
            is HomeUiEvent.OnDismissGroupDialog -> _state.update { it.copy(showCreateGroupDialog = false) }
            is HomeUiEvent.OnConfirmCreateGroup -> createGroup(event.name, event.password)
            is HomeUiEvent.OnLockIndividualAppClicked -> {}
            is HomeUiEvent.OnDeleteGroup -> viewModelScope.launch { groupDao.deleteGroup(event.groupId) }
            is HomeUiEvent.OnDeleteAppLock -> viewModelScope.launch { appDao.deleteApp(event.packageName) }
            is HomeUiEvent.OnMoveAppToGroup -> {
                viewModelScope.launch {
                    val app = appDao.getAppByPackage(event.packageName)
                    if (app != null) {
                        // Inherits the group's settings automatically by changing its parent ID!
                        appDao.upsertApp(app.copy(groupId = event.targetGroupId))
                    }
                }
            }
        }
    }

    private fun createGroup(name: String, password: String?) {
        viewModelScope.launch {
            val newGroup = GroupEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                authType = "TEXT_PASSWORD",
                encryptedSecret = password?.toByteArray(),
                secretIv = null,
                recoveryHash = null,
                voiceUnlockPhrase = null,
                voiceLockPhrase = null,
                isStrict = false
            )
            groupDao.upsertGroup(newGroup)
            _state.update { it.copy(showCreateGroupDialog = false) }
        }
    }
}