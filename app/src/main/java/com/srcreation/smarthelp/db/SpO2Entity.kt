package com.srcreation.smarthelp.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "spo2_records")
data class SpO2Entity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val profileId: Int = 1,
    val spo2Percentage: Int = 98,
    val pulseRate: Int = 75,
    val breathingStatus: String = "Normal Breathing", // Normal Breathing, Shortness of Breath, Oxygen Support
    val date: String = "",
    val time: String = "",
    val synced: Boolean = false
)
