package com.srcreation.smarthelp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_deletions")
data class PendingDeletionEntity(
    @PrimaryKey
    val id: String,
    val ownerUid: String,
    val kind: String,
    val docId: String,
    val createdAt: Long
)
