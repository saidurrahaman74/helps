package com.srcreation.smarthelp

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.srcreation.smarthelp.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class BackupData(
    val profiles: List<ProfileEntity> = emptyList(),
    val medicines: List<MedicineEntity> = emptyList(),
    val bpRecords: List<BpEntity> = emptyList(),
    val insulinRecords: List<InsulinEntity> = emptyList(),
    val stepRecords: List<StepEntity> = emptyList(),
    val periodSettings: List<PeriodEntity> = emptyList(),
    val periodLogs: List<PeriodLogEntity> = emptyList(),
    val vaccines: List<VaccineEntity> = emptyList(),
    val bmiRecords: List<BmiEntity> = emptyList(),
    val healthGoals: List<HealthGoalEntity> = emptyList(),
    val medicalRecords: List<MedicalRecordEntity> = emptyList(),
    val medicalId: List<MedicalIdEntity> = emptyList(),
    val emergencyContacts: List<EmergencyContactEntity> = emptyList(),
    val notifications: List<NotificationEntity> = emptyList(),
    val waterLogs: List<WaterLogEntity> = emptyList(),
    val appointments: List<AppointmentEntity> = emptyList()
)

class BackupManager(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val gson = Gson()

    suspend fun createBackup(): String = withContext(Dispatchers.IO) {
        val data = BackupData(
            profiles = db.profileDao().getProfileList(),
            medicines = db.medicineDao().getFullMedicineList(),
            bpRecords = db.bpDao().getFullBpList(),
            insulinRecords = db.insulinDao().getFullInsulinList(),
            stepRecords = db.stepDao().getFullStepList(),
            periodSettings = db.periodDao().getFullSettingsList(),
            periodLogs = db.periodDao().getFullLogList(),
            vaccines = db.vaccineDao().getFullVaccineList(),
            bmiRecords = db.bmiDao().getFullBmiList(),
            healthGoals = db.bmiDao().getFullGoalList(),
            medicalRecords = db.medicalRecordDao().getFullRecordList(),
            medicalId = db.medicalIdDao().getFullMedicalIdList(),
            emergencyContacts = db.medicalIdDao().getFullContactList(),
            notifications = db.notificationDao().getFullNotificationList(),
            waterLogs = db.waterDao().getAllWaterLogs(),
            appointments = db.appointmentDao().getFullAppointmentList()
        )
        gson.toJson(data)
    }

    suspend fun restoreBackup(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val json = reader.readText()
                val data = gson.fromJson(json, BackupData::class.java)
                
                // Restore each table (using REPLACE strategy from DAOs)
                data.profiles.let { db.profileDao().insertAllProfiles(it) }
                data.medicines.let { db.medicineDao().insertAll(it) }
                data.bpRecords.let { db.bpDao().insertAll(it) }
                data.insulinRecords.let { db.insulinDao().insertAll(it) }
                data.stepRecords.let { db.stepDao().insertAll(it) }
                data.periodSettings.let { db.periodDao().insertAllSettings(it) }
                data.periodLogs.let { db.periodDao().insertAllLogs(it) }
                data.vaccines.let { db.vaccineDao().insertAll(it) }
                data.bmiRecords.let { db.bmiDao().insertAllBmi(it) }
                data.healthGoals.let { db.bmiDao().insertAllGoals(it) }
                data.medicalRecords.let { db.medicalRecordDao().insertAll(it) }
                data.medicalId.let { db.medicalIdDao().insertAllMedicalIds(it) }
                data.emergencyContacts.let { db.medicalIdDao().insertAllContacts(it) }
                data.notifications.let { db.notificationDao().insertAll(it) }
                data.waterLogs.let { db.waterDao().insertAllWaterLogs(it) }
                data.appointments.let { db.appointmentDao().insertAllAppointments(it) }
                
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
