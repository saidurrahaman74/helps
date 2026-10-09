package com.srcreation.smarthelp.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val relation: String, // e.g., Self, Father, Mother, Child
    val gender: String = "Male",
    val avatarRes: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "medicine_reminders")
data class MedicineEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val profileId: Int = 1,
    val name: String,
    val dosageValue: String = "",
    val dosageUnit: String = "mg",
    val category: String, // Tablet, Capsule, Cream, etc.
    val foodRelation: String, // Before Meal, After Meal
    val selectedTimes: String, // Comma separated: Morning, Noon, Evening, Night, 4times, Custom
    val customTime: String? = null, // Store HH:mm if custom
    val durationDays: Int, // 0 for Continuous
    val isContinuous: Boolean = false,
    val initialStock: Int,
    val currentStock: Int,
    val lastTakenTimestamp: Long = 0,
    val isActive: Boolean = true,
    val synced: Boolean = false
)

@Entity(tableName = "bp_records")
data class BpEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val profileId: Int = 1,
    val systolic: Int,
    val diastolic: Int,
    val timeCategory: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "insulin_records")
data class InsulinEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val profileId: Int = 1,
    val glucose: String,
    val unit: String,
    val dose: String,
    val iUnit: String,
    val mealTime: String,
    val dayTime: String = "Morning",
    val date: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "step_records")
data class StepEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val profileId: Int = 1,
    val steps: Int,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "period_settings")
data class PeriodEntity(
    @PrimaryKey val profileId: Int = 1,
    val lastPeriodDate: Long?,
    val cycleLength: Int,
    val periodDuration: Int
)

@Entity(tableName = "period_logs")
data class PeriodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int = 1,
    val date: String,
    val mood: String,
    val flow: String,
    val pain: String,
    val symptoms: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "medical_records")
data class MedicalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int = 1,
    val title: String,
    val type: String,
    val date: String,
    val imageUris: String, // Comma separated local paths
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_medical_id")
data class MedicalIdEntity(
    @PrimaryKey val profileId: Int = 1,
    val fullName: String,
    val bloodGroup: String,
    val age: String,
    val allergies: String,
    val chronicConditions: String,
    val currentMedications: String,
    val weight: String,
    val height: String,
    val imageUri: String? = null
)

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int = 1,
    val name: String,
    val phone: String,
    val relation: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int = 1,
    val remoteId: String? = null,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "vaccine_records")
data class VaccineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int = 1,
    val name: String,
    val doseInfo: String,
    val date: String,
    val isTaken: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bmi_records")
data class BmiEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int = 1,
    val bmi: Double,
    val weight: Double,
    val height: Double,
    val category: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "health_goals")
data class HealthGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int = 1,
    val type: String,
    val value: Double,
    val unit: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int = 1,
    val amountMl: Int,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "doctor_appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int = 1,
    val doctorName: String,
    val specialty: String,
    val chamberName: String,
    val appointmentDate: String,
    val appointmentTime: String,
    val followUpDate: String? = null,
    val note: String? = null,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "io_records")
data class IoRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int,
    val type: String, // Intake, Output
    val category: String, // Oral, IV, Urine, Vomitus, Stool, Drain, etc.
    val amountMl: Int,
    val time: String,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "gyne_profiles")
data class GyneProfileEntity(
    @PrimaryKey val profileId: Int = 1,
    val maritalStatus: String = "",
    val gravity: String = "0", // Total pregnancies
    val parity: String = "0", // Births
    val abortions: String = "0",
    val livingChildren: String = "0",
    val lmpDate: String = "",
    val contraceptiveHistory: String = "",
    val gynecologicalHistory: String = "",
    val menstrualCycleLength: String = "28",
    val lastCheckupDate: String = "",
    val isPregnant: Boolean = false,
    val imageUri: String? = null
)

@Entity(tableName = "pregnancy_kick_logs")
data class KickLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int = 1,
    val kickCount: Int,
    val durationMinutes: Int,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "temp_records")
data class TempEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val profileId: Int = 1,
    val tempValue: Double,
    val unit: String, // C or F
    val bodySite: String, // Oral, Axillary, etc.
    val status: String,
    val date: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "lab_records")
data class LabRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int = 1,
    val testName: String,
    val resultValue: Double,
    val unit: String,
    val normalMin: Double,
    val normalMax: Double,
    val statusTag: String, // HIGH, NORMAL, LOW
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "medicine_doses")
data class MedicineDoseEntity(
    @PrimaryKey val id: String,
    val profileId: Int = 1,
    val medicineId: String,
    val slot: String,
    val date: String,
    val taken: Boolean = true,
    val takenAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
