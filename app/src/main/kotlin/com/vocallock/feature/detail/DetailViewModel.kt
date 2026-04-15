package com.vocallock.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocallock.data.database.dao.AppDao
import com.vocallock.data.database.dao.GroupDao
import com.vocallock.data.database.entity.AppEntity
import com.vocallock.data.database.entity.GroupEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val appDao: AppDao,
    private val groupDao: GroupDao
) : ViewModel() {

    private val targetId: String = checkNotNull(savedStateHandle["targetId"])
    private val isGroup: Boolean = checkNotNull(savedStateHandle["isGroup"])

    private val _state = MutableStateFlow(DetailUiState(isLoading = true, isGroup = isGroup))
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    private val _uiEffect = MutableSharedFlow<DetailUiEffect>()
    val uiEffect: SharedFlow<DetailUiEffect> = _uiEffect.asSharedFlow()

    private var currentApp: AppEntity? = null
    private var currentGroup: GroupEntity? = null
    private var pendingPasswordToSave: String? = null

    init {
        loadDetails()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            val allGroups = groupDao.getAllGroups()

            if (isGroup) {
                currentGroup = groupDao.getGroupById(targetId)
                currentGroup?.let { group ->
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            title = group.name,
                            authType = group.authType,
                            hasSecretSaved = group.encryptedSecret != null,
                            voiceUnlockPhrase = group.voiceUnlockPhrase ?: "",
                            voiceLockPhrase = group.voiceLockPhrase ?: "",
                            isStrict = group.isStrict,
                            availableGroups = allGroups
                        )
                    }
                }
            } else {
                currentApp = appDao.getAppByPackage(targetId)
                currentApp?.let { app ->
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            title = app.displayName,
                            authType = app.authTypeOverride ?: "TEXT_PASSWORD",
                            hasSecretSaved = app.encryptedSecretOverride != null,
                            currentGroupId = app.groupId,
                            voiceUnlockPhrase = app.voiceUnlockPhraseOverride ?: "",
                            voiceLockPhrase = app.voiceLockPhraseOverride ?: "",
                            isStrict = app.isStrictOverride ?: false,
                            availableGroups = allGroups
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: DetailUiEvent) {
        when (event) {
            is DetailUiEvent.OnAuthTypeSelected -> _state.update { it.copy(authType = event.authType) }
            is DetailUiEvent.OnVoiceUnlockTyped -> _state.update { it.copy(voiceUnlockPhrase = event.phrase) }
            is DetailUiEvent.OnVoiceLockTyped -> _state.update { it.copy(voiceLockPhrase = event.phrase) }
            is DetailUiEvent.OnStrictModeToggled -> _state.update { it.copy(isStrict = event.isStrict) }
            is DetailUiEvent.OnMoveToGroup -> _state.update { it.copy(currentGroupId = event.groupId) }
            is DetailUiEvent.OnSetPasswordClicked -> _state.update { it.copy(showPasswordDialog = true) }
            is DetailUiEvent.OnDismissPasswordDialog -> _state.update { it.copy(showPasswordDialog = false) }
            is DetailUiEvent.OnPasswordSaved -> {
                pendingPasswordToSave = event.password
                _state.update { it.copy(showPasswordDialog = false, hasSecretSaved = true) }
            }

            is DetailUiEvent.OnRecordVoiceFingerprintClicked -> _state.update {
                it.copy(
                    showVoiceRecordDialog = true
                )
            }

            is DetailUiEvent.OnDismissVoiceDialog -> _state.update { it.copy(showVoiceRecordDialog = false) }
            is DetailUiEvent.OnVoicePrintSaved -> _state.update { it.copy(showVoiceRecordDialog = false) }
            is DetailUiEvent.OnSaveChanges -> saveChangesToDatabase()
            is DetailUiEvent.OnBackClicked -> {
                viewModelScope.launch { _uiEffect.emit(DetailUiEffect.NavigateBack) }
            }

            is DetailUiEvent.OnDeleteClicked -> _state.update { it.copy(showDeleteConfirmationDialog = true) }
            is DetailUiEvent.OnDismissDeleteDialog -> _state.update {
                it.copy(
                    showDeleteConfirmationDialog = false
                )
            }

            is DetailUiEvent.OnConfirmDelete -> deleteItemFromVault()
        }
    }

    private fun deleteItemFromVault() {
        viewModelScope.launch {
            if (isGroup) {
                groupDao.deleteGroup(targetId)
                _uiEffect.emit(DetailUiEffect.ShowToast("Group deleted"))
            } else {
                appDao.deleteApp(targetId)
                _uiEffect.emit(DetailUiEffect.ShowToast("App removed from vault"))
            }
            _uiEffect.emit(DetailUiEffect.NavigateToHome)
        }
    }

    private fun saveChangesToDatabase() {
        viewModelScope.launch {
            val newSecret = pendingPasswordToSave?.toByteArray()

            if (isGroup) {
                currentGroup?.let { group ->
                    groupDao.upsertGroup(
                        group.copy(
                            authType = _state.value.authType,
                            voiceUnlockPhrase = _state.value.voiceUnlockPhrase.takeIf { it.isNotBlank() },
                            voiceLockPhrase = _state.value.voiceLockPhrase.takeIf { it.isNotBlank() },
                            isStrict = _state.value.isStrict,
                            encryptedSecret = newSecret ?: group.encryptedSecret
                        )
                    )
                }
                _uiEffect.emit(DetailUiEffect.ShowToast("Group settings saved"))
            } else {
                currentApp?.let { app ->
                    appDao.upsertApp(
                        app.copy(
                            authTypeOverride = _state.value.authType,
                            groupId = _state.value.currentGroupId,
                            voiceUnlockPhraseOverride = _state.value.voiceUnlockPhrase.takeIf { it.isNotBlank() },
                            voiceLockPhraseOverride = _state.value.voiceLockPhrase.takeIf { it.isNotBlank() },
                            isStrictOverride = _state.value.isStrict,
                            encryptedSecretOverride = newSecret ?: app.encryptedSecretOverride
                        )
                    )
                }
                _uiEffect.emit(DetailUiEffect.ShowToast("App settings saved"))
            }
            _uiEffect.emit(DetailUiEffect.NavigateBack)
        }
    }
}