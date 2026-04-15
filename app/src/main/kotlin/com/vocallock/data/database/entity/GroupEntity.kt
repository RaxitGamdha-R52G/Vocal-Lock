package com.vocallock.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,

    // Security Settings
    val authType: String,            // Stored as String (e.g., "TEXT_PASSWORD", "PIN")
    val encryptedSecret: ByteArray?, // The actual password/PIN data
    val secretIv: ByteArray?,        // IV for decryption
    val recoveryHash: String?,       // 12-word recovery hash specific to this group

    // Voice Settings
    val voiceUnlockPhrase: String?,
    val voiceLockPhrase: String?,
    val isStrict: Boolean = false,   // Toggles MFCC biometric matching
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GroupEntity
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}