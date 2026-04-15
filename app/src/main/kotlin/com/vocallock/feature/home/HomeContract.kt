package com.vocallock.feature.home

import com.vocallock.data.database.entity.AppEntity
import com.vocallock.data.database.entity.GroupEntity

sealed class HomeUiEffect {
    // This tells the UI exactly which ID to open and whether it's an app or a group
    data class NavigateToDetail(val targetId: String, val isGroup: Boolean) : HomeUiEffect()
}

/**
 * The single source of truth for the Home Screen's UI.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val standaloneApps: List<AppEntity> = emptyList(),
    val groupsWithApps: Map<GroupEntity, List<AppEntity>> = emptyMap(),
    val showCreateGroupDialog: Boolean = false,
    val maxGridTiles: Int = 4
)

/**
 * Every action the user can take on the Home Screen.
 */
sealed class HomeUiEvent {
    data class OnAppClicked(val packageName: String) : HomeUiEvent()
    data class OnGroupClicked(val groupId: String) : HomeUiEvent()
    data class OnConfirmCreateGroup(val name: String, val password: String?) : HomeUiEvent()
    data class OnDeleteGroup(val groupId: String) : HomeUiEvent()
    data class OnDeleteAppLock(val packageName: String) : HomeUiEvent()
    data class OnMoveAppToGroup(val packageName: String, val targetGroupId: String?) : HomeUiEvent()

    object OnCreateGroupClicked : HomeUiEvent()
    object OnDismissGroupDialog : HomeUiEvent()
    object OnLockIndividualAppClicked : HomeUiEvent()

}