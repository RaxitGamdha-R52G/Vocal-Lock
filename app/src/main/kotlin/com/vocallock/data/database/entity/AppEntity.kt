package com.vocallock.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "apps",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL // If a group is deleted, the app stays but becomes standalone
        )
    ],
    indices = [Index("groupId")]
)
data class AppEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val groupId: String?, // Null means it's a standalone app

    // Individual Overrides (If null, the app inherits from its Group)
    val authTypeOverride: String?,
    val encryptedSecretOverride: ByteArray?,
    val secretIvOverride: ByteArray?,
    val recoveryHashOverride: String?,

    val voiceUnlockPhraseOverride: String?,
    val voiceLockPhraseOverride: String?,
    val isStrictOverride: Boolean?,

    val orderIndex: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppEntity
        return packageName == other.packageName
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }
}