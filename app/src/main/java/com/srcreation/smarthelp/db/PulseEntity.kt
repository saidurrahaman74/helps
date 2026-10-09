package com.srcreation.smarthelp.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pulse_records")
data class PulseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val profileId: Int = 1,
    val pulseBpm: Int = 75,
    val activityState: String = "At Rest", // At Rest, After Exercise, Stressed
    val date: String = "",
    val time: String = "",
    val synced: Boolean = false
)
