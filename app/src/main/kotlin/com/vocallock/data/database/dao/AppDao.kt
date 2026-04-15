package com.vocallock.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.vocallock.data.database.entity.AppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps ORDER BY orderIndex ASC, displayName ASC")
    fun observeAllApps(): Flow<List<AppEntity>>

    // Fetches only the apps inside a specific group
    @Query("SELECT * FROM apps WHERE groupId = :groupId ORDER BY orderIndex ASC, displayName ASC")
    fun observeAppsForGroup(groupId: String): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE packageName = :pkg")
    suspend fun getAppByPackage(pkg: String): AppEntity?

    @Upsert
    suspend fun upsertApp(app: AppEntity)

    @Query("DELETE FROM apps WHERE packageName = :pkg")
    suspend fun deleteApp(pkg: String)
}