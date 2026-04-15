package com.vocallock.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vocallock.data.database.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM app_groups ORDER BY name ASC")
    fun observeAllGroups(): Flow<List<GroupEntity>>

    // One-time fetch for the Detail Screen selector
    @Query("SELECT * FROM app_groups ORDER BY name ASC")
    suspend fun getAllGroups(): List<GroupEntity>

    @Query("SELECT * FROM app_groups WHERE id = :id")
    suspend fun getGroupById(id: String): GroupEntity?

    @Upsert
    suspend fun upsertGroup(group: GroupEntity)

    @Query("DELETE FROM app_groups WHERE id = :id")
    suspend fun deleteGroup(id: String)
}