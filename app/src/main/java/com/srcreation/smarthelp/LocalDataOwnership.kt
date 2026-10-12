package com.srcreation.smarthelp

import android.content.Context
import com.srcreation.smarthelp.db.AppDatabase
import java.text.SimpleDateFormat
import java.util.*

enum class GateDecision { SHOW_CONTENT, REDIRECT_TO_MAIN }

object LocalDataOwnership {

    val COUNTED_TABLES = setOf(
        "profiles",
        "medicine_reminders",
        "medicine_doses",
        "bp_records",
        "insulin_records",
        "step_records",
        "period_settings",
        "period_logs",
        "medical_records",
        "user_medical_id",
        "emergency_contacts",
        "vaccine_records",
        "bmi_records",
        "health_goals",
        "water_logs",
        "doctor_appointments",
        "io_records",
        "gyne_profiles",
        "pregnancy_kick_logs",
        "temp_records",
        "lab_records",
        "pulse_records",
        "spo2_records",
        "diet_foods",
        "diet_meals",
        "diet_meal_items",
        "user_diet_profiles"
    )

    val EXCLUDED_WITH_REASON = mapOf(
        "notifications" to "Ephemeral remote push announcements and system reminders, not user-entered clinical/health tracker data.",
        "pending_deletions" to "Internal sync queue for tracking remote Firestore document deletions, not user health data.",
        "sqlite_sequence" to "SQLite internal schema sequence management table.",
        "room_master_table" to "Room database internal schema verification table."
    )

    fun unhandledTables(schemaTables: Set<String>): List<String> {
        val counted = COUNTED_TABLES
        val excluded = EXCLUDED_WITH_REASON.keys
        return schemaTables.filter { table ->
            !counted.contains(table) && !excluded.contains(table)
        }
    }

    fun evaluateGateDecision(ownerUid: String?, currentUid: String?, hasHealthData: Boolean): GateDecision {
        if (currentUid.isNullOrBlank()) {
            return GateDecision.SHOW_CONTENT
        }
        if (!ownerUid.isNullOrBlank() && ownerUid != currentUid) {
            return GateDecision.REDIRECT_TO_MAIN
        }
        if (ownerUid.isNullOrBlank() && hasHealthData) {
            return GateDecision.REDIRECT_TO_MAIN
        }
        return GateDecision.SHOW_CONTENT
    }

    fun getOwnerUid(context: Context): String? {
        val pref = PreferenceManager(context)
        return pref.getLocalDataOwnerUidInternal()
    }

    internal fun setOwnerUidInternal(context: Context, uid: String) {
        val pref = PreferenceManager(context)
        pref.setLocalDataOwnerUidInternal(uid)
    }

    suspend fun isOwnershipBlocked(context: Context): Boolean {
        val uid = AuthRepository().uid() ?: return false
        val ownerUid = getOwnerUid(context)
        val hasData = hasUserHealthData(context)
        return evaluateGateDecision(ownerUid, uid, hasData) == GateDecision.REDIRECT_TO_MAIN
    }

    suspend fun hasUserHealthData(context: Context): Boolean {
        val db = AppDatabase.getDatabase(context)
        return try {
            val bp = db.bpDao().getFullBpList().isNotEmpty()
            val med = db.medicineDao().getFullMedicineList().isNotEmpty()
            val doses = db.medicineDoseDao().getFullDoseList().isNotEmpty()
            val insulin = db.insulinDao().getFullInsulinList().isNotEmpty()
            val step = db.stepDao().getFullStepList().isNotEmpty()
            val water = db.waterDao().getAllWaterLogs().isNotEmpty()
            val appt = db.appointmentDao().getFullAppointmentList().isNotEmpty()
            val record = db.medicalRecordDao().getFullRecordList().isNotEmpty()
            val vaccine = db.vaccineDao().getFullVaccineList().isNotEmpty()
            val bmi = db.bmiDao().getFullBmiList().isNotEmpty()
            val goal = db.bmiDao().getFullGoalList().isNotEmpty()
            
            val medicalId = db.medicalIdDao().getFullMedicalIdList().isNotEmpty()
            val contacts = db.medicalIdDao().getFullContactList().isNotEmpty()
            val periodSettings = db.periodDao().getFullSettingsList().isNotEmpty()
            val periodLogs = db.periodDao().getFullLogList().isNotEmpty()
            val gyne = db.gyneDao().getFullGyneList().isNotEmpty()
            val kick = db.gyneDao().getFullKickList().isNotEmpty()
            val pulse = db.pulseDao().getFullPulseList().isNotEmpty()
            val spo2 = db.spo2Dao().getFullSpO2List().isNotEmpty()
            val temp = db.tempDao().getFullTempList().isNotEmpty()
            val lab = db.labDao().getFullLabList().isNotEmpty()
            val io = db.ioDao().getFullIoList().isNotEmpty()
            val diet = db.dietDao().getFullMealList().isNotEmpty()
            
            val profiles = db.profileDao().getProfileList()
            val hasFamilyProfiles = profiles.any { it.id != 1 }
            val defaultProfileEdited = profiles.any { it.id == 1 && (it.name != "Me" || it.relation != "Self") }

            bp || med || doses || insulin || step || water || appt || record || vaccine || bmi || goal ||
                    medicalId || contacts || periodSettings || periodLogs || gyne || kick || pulse || spo2 ||
                    temp || lab || io || diet || hasFamilyProfiles || defaultProfileEdited
        } catch (_: Exception) {
            false
        }
    }

    suspend fun countUserHealthRecords(context: Context): Int {
        val db = AppDatabase.getDatabase(context)
        return try {
            db.bpDao().getFullBpList().size +
                    db.medicineDao().getFullMedicineList().size +
                    db.insulinDao().getFullInsulinList().size +
                    db.stepDao().getFullStepList().size +
                    db.waterDao().getAllWaterLogs().size +
                    db.appointmentDao().getFullAppointmentList().size +
                    db.medicalRecordDao().getFullRecordList().size +
                    db.vaccineDao().getFullVaccineList().size +
                    db.bmiDao().getFullBmiList().size +
                    db.bmiDao().getFullGoalList().size +
                    db.medicalIdDao().getFullMedicalIdList().size +
                    db.medicalIdDao().getFullContactList().size +
                    db.periodDao().getFullSettingsList().size +
                    db.periodDao().getFullLogList().size +
                    db.pulseDao().getFullPulseList().size +
                    db.spo2Dao().getFullSpO2List().size +
                    db.tempDao().getFullTempList().size +
                    db.labDao().getFullLabList().size +
                    db.ioDao().getFullIoList().size +
                    db.gyneDao().getFullGyneList().size +
                    db.gyneDao().getFullKickList().size +
                    db.dietDao().getFullMealList().size +
                    db.profileDao().getProfileList().count { it.id != 1 || it.name != "Me" || it.relation != "Self" }
        } catch (_: Exception) {
            0
        }
    }

    suspend fun getNewestRecordDate(context: Context): String {
        val db = AppDatabase.getDatabase(context)
        return try {
            val timestamps = mutableListOf<Long>()
            db.bpDao().getFullBpList().forEach { timestamps.add(it.timestamp) }
            db.medicineDao().getFullMedicineList().forEach { timestamps.add(it.lastTakenTimestamp) }
            db.insulinDao().getFullInsulinList().forEach { timestamps.add(it.timestamp) }
            db.stepDao().getFullStepList().forEach { timestamps.add(it.timestamp) }
            db.waterDao().getAllWaterLogs().forEach { timestamps.add(it.timestamp) }
            db.appointmentDao().getFullAppointmentList().forEach { timestamps.add(it.timestamp) }
            db.medicalRecordDao().getFullRecordList().forEach { timestamps.add(it.timestamp) }
            db.vaccineDao().getFullVaccineList().forEach { timestamps.add(it.timestamp) }
            db.tempDao().getFullTempList().forEach { timestamps.add(it.timestamp) }
            
            val maxTs = timestamps.maxOrNull() ?: 0L
            if (maxTs > 0L) {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(maxTs))
            } else {
                "Unknown"
            }
        } catch (_: Exception) {
            "Unknown"
        }
    }

    suspend fun claimIfFresh(context: Context, uid: String): Boolean {
        val currentOwner = getOwnerUid(context)
        if (currentOwner.isNullOrBlank()) {
            val hasData = hasUserHealthData(context)
            if (!hasData) {
                setOwnerUidInternal(context, uid)
                return true
            }
        }
        return false
    }

    fun claimByUserChoice(context: Context, uid: String) {
        setOwnerUidInternal(context, uid)
    }

    fun resetOwnerUidToUnset(context: Context) {
        setOwnerUidInternal(context, "")
    }
}
