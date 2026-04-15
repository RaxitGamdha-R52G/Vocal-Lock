package com.vocallock.feature.group

import com.vocallock.data.database.entity.AppEntity
import com.vocallock.data.database.entity.GroupEntity

data class GroupContentsUiState(
    val isLoading: Boolean = true,
    val group: GroupEntity? = null,
    val apps: List<AppEntity> = emptyList()
)