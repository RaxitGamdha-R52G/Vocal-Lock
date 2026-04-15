package com.vocallock.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vocallock.data.database.dao.AppDao
import com.vocallock.data.database.dao.GroupDao
import com.vocallock.data.database.entity.AppEntity
import com.vocallock.data.database.entity.GroupEntity

@Database(
    entities = [AppEntity::class, GroupEntity::class],
    version = 3,
    exportSchema = false
)
abstract class VocalLockDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun groupDao(): GroupDao
}