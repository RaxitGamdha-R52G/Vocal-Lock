package com.vocallock.feature.group

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocallock.data.database.dao.AppDao
import com.vocallock.data.database.dao.GroupDao
import com.vocallock.data.database.entity.AppEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupContentsViewModel(
    savedStateHandle: SavedStateHandle,
    private val groupDao: GroupDao,
    private val appDao: AppDao
) : ViewModel() {
    val groupId: String = checkNotNull(savedStateHandle["groupId"])

    private val _state = MutableStateFlow(GroupContentsUiState())
    val state: StateFlow<GroupContentsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val group = groupDao.getGroupById(groupId)
            appDao.observeAppsForGroup(groupId).collect { appsList ->
                _state.update { it.copy(isLoading = false, group = group, apps = appsList) }
            }
        }
    }

    fun renameGroup(newName: String) {
        viewModelScope.launch {
            _state.value.group?.let { group ->
                groupDao.upsertGroup(group.copy(name = newName))
            }
        }
    }

    fun removeAppFromGroup(app: AppEntity) {
        viewModelScope.launch {
            appDao.upsertApp(app.copy(groupId = null))
        }
    }

    fun reorderAppsLocally(fromIndex: Int, toIndex: Int) {
        val currentList = _state.value.apps.toMutableList()
        val movedItem = currentList.removeAt(fromIndex)
        currentList.add(toIndex, movedItem)
        _state.update { it.copy(apps = currentList) }
    }

    fun saveAppOrderToDatabase() {
        viewModelScope.launch {
            val updatedApps = _state.value.apps.mapIndexed { index, app ->
                app.copy(orderIndex = index)
            }
            updatedApps.forEach { appDao.upsertApp(it) }
        }
    }
}