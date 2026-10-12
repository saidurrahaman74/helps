package com.srcreation.smarthelp

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.PendingDeletionEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

class SyncRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val firestore = FirebaseFirestore.getInstance()
    private val authRepo = AuthRepository()

    companion object {
        private val syncMutex = Mutex()
    }

    suspend fun syncAllUnsynced(): Boolean {
        return syncMutex.withLock {
            syncAllUnsyncedInternal()
        }
    }

    private suspend fun syncAllUnsyncedInternal(): Boolean {
        if (!authRepo.isUserLoggedIn()) return false
        val uid = authRepo.uid() ?: return false
        val ownerUid = LocalDataOwnership.getOwnerUid(context)
        if (ownerUid.isNullOrBlank()) {
            LocalDataOwnership.claimIfFresh(context, uid)
            val updatedOwner = LocalDataOwnership.getOwnerUid(context)
            if (updatedOwner != uid && LocalDataOwnership.hasUserHealthData(context)) {
                return false
            }
        } else if (ownerUid != uid) {
            // Local data owner mismatch guard: do NOT upload another user's local data
            return false
        }

        try {
            // 1. Sync BP (Primary profile owner only: profileId == 1)
            val unsyncedBp = db.bpDao().getUnsyncedRecords().filter { it.profileId == 1 }
            for (bp in unsyncedBp) {
                if (db.bpDao().getFullBpList().none { it.id == bp.id }) {
                    firestore.collection("patients").document(uid).collection("vitals").document(bp.id).delete().await()
                    continue
                }
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
                if (db.bpDao().getFullBpList().none { it.id == bp.id }) {
                    firestore.collection("patients").document(uid).collection("vitals").document(bp.id).delete().await()
                }
            }

            // 2. Sync Pulse (Primary profile owner only: profileId == 1)
            val unsyncedPulse = db.pulseDao().getUnsyncedRecords().filter { it.profileId == 1 }
            for (pulse in unsyncedPulse) {
                if (db.pulseDao().getFullPulseList().none { it.id == pulse.id }) {
                    firestore.collection("patients").document(uid).collection("vitals").document(pulse.id).delete().await()
                    continue
                }
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
                if (db.pulseDao().getFullPulseList().none { it.id == pulse.id }) {
                    firestore.collection("patients").document(uid).collection("vitals").document(pulse.id).delete().await()
                }
            }

            // 3. Sync SpO2 (Primary profile owner only: profileId == 1)
            val unsyncedSpO2 = db.spo2Dao().getUnsyncedRecords().filter { it.profileId == 1 }
            for (spo2 in unsyncedSpO2) {
                if (db.spo2Dao().getFullSpO2List().none { it.id == spo2.id }) {
                    firestore.collection("patients").document(uid).collection("vitals").document(spo2.id).delete().await()
                    continue
                }
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
                if (db.spo2Dao().getFullSpO2List().none { it.id == spo2.id }) {
                    firestore.collection("patients").document(uid).collection("vitals").document(spo2.id).delete().await()
                }
            }

            // 4. Sync Insulin / Glucose (Primary profile owner only: profileId == 1)
            val unsyncedInsulin = db.insulinDao().getUnsyncedRecords().filter { it.profileId == 1 }
            for (insulin in unsyncedInsulin) {
                if (db.insulinDao().getFullInsulinList().none { it.id == insulin.id }) {
                    firestore.collection("patients").document(uid).collection("vitals").document(insulin.id).delete().await()
                    continue
                }
                val glucoseVal = insulin.glucose.toDoubleOrNull()
                val hasValidGlucose = glucoseVal != null && glucoseVal > 0.0
                val data = hashMapOf<String, Any?>(
                    "type" to "GLUCOSE",
                    "kind" to if (hasValidGlucose) "GLUCOSE" else "INSULIN_DOSE",
                    "value" to if (hasValidGlucose) glucoseVal else null,
                    "value2" to (insulin.dose.toDoubleOrNull() ?: 0.0),
                    "unit" to insulin.unit,
                    "iUnit" to insulin.iUnit,
                    "timestamp" to insulin.timestamp
                )
                firestore.collection("patients").document(uid)
                    .collection("vitals").document(insulin.id).set(data).await()
                db.insulinDao().markSynced(insulin.id)
                if (db.insulinDao().getFullInsulinList().none { it.id == insulin.id }) {
                    firestore.collection("patients").document(uid).collection("vitals").document(insulin.id).delete().await()
                }
            }

            // 5. Sync Step (Primary profile owner only: profileId == 1)
            val unsyncedSteps = db.stepDao().getUnsyncedRecords().filter { it.profileId == 1 }
            for (step in unsyncedSteps) {
                if (db.stepDao().getFullStepList().none { it.id == step.id }) {
                    firestore.collection("patients").document(uid).collection("vitals").document(step.id).delete().await()
                    continue
                }
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
                if (db.stepDao().getFullStepList().none { it.id == step.id }) {
                    firestore.collection("patients").document(uid).collection("vitals").document(step.id).delete().await()
                }
            }

            // 6. Sync Temp (Primary profile owner only: profileId == 1)
            val unsyncedTemp = db.tempDao().getUnsyncedRecords().filter { it.profileId == 1 }
            for (temp in unsyncedTemp) {
                if (db.tempDao().getFullTempList().none { it.id == temp.id }) {
                    firestore.collection("patients").document(uid).collection("vitals").document(temp.id).delete().await()
                    continue
                }
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
                if (db.tempDao().getFullTempList().none { it.id == temp.id }) {
                    firestore.collection("patients").document(uid).collection("vitals").document(temp.id).delete().await()
                }
            }

            // 7. Sync Medicines (Primary profile owner only: profileId == 1)
            val unsyncedMeds = db.medicineDao().getUnsyncedMedicines().filter { it.profileId == 1 }
            for (med in unsyncedMeds) {
                if (db.medicineDao().getFullMedicineList().none { it.id == med.id }) {
                    firestore.collection("patients").document(uid).collection("medicines").document(med.id).delete().await()
                    continue
                }
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
                if (db.medicineDao().getFullMedicineList().none { it.id == med.id }) {
                    firestore.collection("patients").document(uid).collection("medicines").document(med.id).delete().await()
                }
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

            // 9. Process Pending Deletions in batched writes of at most 400
            val pendingList = db.pendingDeletionDao().getAllPendingDeletions()
            if (pendingList.isNotEmpty()) {
                val ownerUid = LocalDataOwnership.getOwnerUid(context)
                val isOwnershipValid = (ownerUid.isNullOrBlank() || ownerUid == uid)
                if (isOwnershipValid) {
                    val chunks = pendingList.filter { it.ownerUid == uid }.chunked(400)
                    for (chunk in chunks) {
                        try {
                            val batch = firestore.batch()
                            val docsToDelete = mutableListOf<PendingDeletionEntity>()
                            for (pending in chunk) {
                                if (pending.kind == "medicine") {
                                    val dosesRef = firestore.collection("patients").document(uid)
                                        .collection("medicines").document(pending.docId)
                                        .collection("doses")
                                    val doseSnap = dosesRef.get().await()
                                    for (dDoc in doseSnap.documents) {
                                        batch.delete(dDoc.reference)
                                    }
                                    batch.delete(firestore.collection("patients").document(uid).collection("medicines").document(pending.docId))
                                } else {
                                    batch.delete(firestore.collection("patients").document(uid).collection("vitals").document(pending.docId))
                                }
                                docsToDelete.add(pending)
                            }
                            batch.commit().await()
                            for (pending in docsToDelete) {
                                db.pendingDeletionDao().delete(pending)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Keep row and retry later on network/permission error
                        }
                    }
                }
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
