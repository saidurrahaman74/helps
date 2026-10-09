package com.srcreation.smarthelp

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.srcreation.smarthelp.db.AppDatabase
import kotlinx.coroutines.tasks.await

class SyncRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val firestore = FirebaseFirestore.getInstance()
    private val authRepo = AuthRepository()

    suspend fun syncAllUnsynced(): Boolean {
        if (!authRepo.isUserLoggedIn()) return false
        val role = authRepo.getRole()
        if (!role.equals("patient", ignoreCase = true)) return false

        val uid = authRepo.uid() ?: return false

        try {
            // 1. Sync BP
            val unsyncedBp = db.bpDao().getUnsyncedRecords()
            for (bp in unsyncedBp) {
                val data = hashMapOf(
                    "type" to "BP",
                    "value" to bp.systolic,
                    "value2" to bp.diastolic,
                    "unit" to "mmHg",
                    "timestamp" to bp.timestamp
                )
                firestore.collection("patients").document(uid)
                    .collection("vitals").document(bp.id).set(data).await()
                db.bpDao().markSynced(bp.id)
            }

            // 2. Sync Pulse
            val unsyncedPulse = db.pulseDao().getUnsyncedRecords()
            for (pulse in unsyncedPulse) {
                val data = hashMapOf(
                    "type" to "PULSE",
                    "value" to pulse.pulseBpm,
                    "value2" to 0,
                    "unit" to "BPM",
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("patients").document(uid)
                    .collection("vitals").document(pulse.id).set(data).await()
                db.pulseDao().markSynced(pulse.id)
            }

            // 3. Sync SpO2
            val unsyncedSpO2 = db.spo2Dao().getUnsyncedRecords()
            for (spo2 in unsyncedSpO2) {
                val data = hashMapOf(
                    "type" to "SPO2",
                    "value" to spo2.spo2Percentage,
                    "value2" to spo2.pulseRate,
                    "unit" to "%",
                    "timestamp" to System.currentTimeMillis()
                )
                firestore.collection("patients").document(uid)
                    .collection("vitals").document(spo2.id).set(data).await()
                db.spo2Dao().markSynced(spo2.id)
            }

            // 4. Sync Insulin
            val unsyncedInsulin = db.insulinDao().getUnsyncedRecords()
            for (insulin in unsyncedInsulin) {
                val data = hashMapOf(
                    "type" to "GLUCOSE",
                    "value" to (insulin.glucose.toDoubleOrNull() ?: 0.0),
                    "value2" to (insulin.dose.toDoubleOrNull() ?: 0.0),
                    "unit" to insulin.unit,
                    "timestamp" to insulin.timestamp
                )
                firestore.collection("patients").document(uid)
                    .collection("vitals").document(insulin.id).set(data).await()
                db.insulinDao().markSynced(insulin.id)
            }

            // 5. Sync Step
            val unsyncedSteps = db.stepDao().getUnsyncedRecords()
            for (step in unsyncedSteps) {
                val data = hashMapOf(
                    "type" to "STEPS",
                    "value" to step.steps,
                    "value2" to 0,
                    "unit" to "steps",
                    "timestamp" to step.timestamp
                )
                firestore.collection("patients").document(uid)
                    .collection("vitals").document(step.id).set(data).await()
                db.stepDao().markSynced(step.id)
            }

            // 6. Sync Temp
            val unsyncedTemp = db.tempDao().getUnsyncedRecords()
            for (temp in unsyncedTemp) {
                val data = hashMapOf(
                    "type" to "TEMPERATURE",
                    "value" to temp.tempValue,
                    "value2" to 0.0,
                    "unit" to temp.unit,
                    "timestamp" to temp.timestamp
                )
                firestore.collection("patients").document(uid)
                    .collection("vitals").document(temp.id).set(data).await()
                db.tempDao().markSynced(temp.id)
            }

            // 7. Sync Medicines
            val unsyncedMeds = db.medicineDao().getUnsyncedMedicines()
            for (med in unsyncedMeds) {
                val data = hashMapOf(
                    "name" to med.name,
                    "dose" to "${med.dosageValue} ${med.dosageUnit}".trim(),
                    "time" to med.selectedTimes,
                    "taken" to (med.lastTakenTimestamp > 0),
                    "takenAt" to med.lastTakenTimestamp
                )
                firestore.collection("patients").document(uid)
                    .collection("medicines").document(med.id).set(data).await()
                db.medicineDao().markSynced(med.id)
            }

            // 8. Sync Medicine Doses
            val unsyncedDoses = db.medicineDoseDao().getUnsyncedDoses()
            for (dose in unsyncedDoses) {
                val data = hashMapOf(
                    "slot" to dose.slot,
                    "date" to dose.date,
                    "taken" to dose.taken,
                    "takenAt" to dose.takenAt
                )
                val doseDocId = "${dose.date}_${dose.slot}"
                firestore.collection("patients").document(uid)
                    .collection("medicines").document(dose.medicineId)
                    .collection("doses").document(doseDocId).set(data).await()
                db.medicineDoseDao().markSynced(dose.id)
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun triggerSync() {
        SyncWorker.enqueue(context)
    }
}
