package com.vocallock.feature.detail

import com.vocallock.data.database.entity.GroupEntity

data class DetailUiState(
    val isLoading: Boolean = true,
    val title: String = "",
    val isGroup: Boolean = false,

    // Security Settings
    val authType: String = "TEXT_PASSWORD",
    val hasSecretSaved: Boolean = false,
    val showPasswordDialog: Boolean = false,

    // Organization
    val availableGroups: List<GroupEntity> = emptyList(),
    val currentGroupId: String? = null,

    // Voice Settings
    val voiceUnlockPhrase: String = "",
    val voiceLockPhrase: String = "",
    val isStrict: Boolean = false,
    val showVoiceRecordDialog: Boolean = false,

    val showDeleteConfirmationDialog: Boolean = false
)

sealed class DetailUiEvent {
    data class OnAuthTypeSelected(val authType: String) : DetailUiEvent()
    data class OnVoiceUnlockTyped(val phrase: String) : DetailUiEvent()
    data class OnVoiceLockTyped(val phrase: String) : DetailUiEvent()
    data class OnStrictModeToggled(val isStrict: Boolean) : DetailUiEvent()
    data class OnPasswordSaved(val password: String) : DetailUiEvent()
    data class OnMoveToGroup(val groupId: String?) : DetailUiEvent()

    object OnSetPasswordClicked : DetailUiEvent()
    object OnDismissPasswordDialog : DetailUiEvent()
    object OnRecordVoiceFingerprintClicked : DetailUiEvent()
    object OnDismissVoiceDialog : DetailUiEvent()
    object OnVoicePrintSaved : DetailUiEvent()
    object OnSaveChanges : DetailUiEvent()
    object OnBackClicked : DetailUiEvent()
    object OnDeleteClicked : DetailUiEvent()
    object OnDismissDeleteDialog : DetailUiEvent()
    object OnConfirmDelete : DetailUiEvent()
}

sealed class DetailUiEffect {
    data class ShowToast(val message: String) : DetailUiEffect()
    object NavigateBack : DetailUiEffect()
    object NavigateToHome : DetailUiEffect()
}