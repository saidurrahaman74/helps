package com.srcreation.smarthelp

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.MedicineDoseEntity
import com.srcreation.smarthelp.db.MedicineEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MedicineRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val medicineDao = db.medicineDao()
    private val doseDao = db.medicineDoseDao()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun markDoseTaken(medicineId: String, preferredSlot: String? = null) {
        val meds = medicineDao.getFullMedicineList()
        val med = meds.find { it.id == medicineId } ?: return

        // 1. Get patient time zone from Firestore or default
        var timeZoneId = TimeZone.getDefault().id
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val userDoc = firestore.collection("users").document(uid).get().await()
                val tz = userDoc.getString("timeZone")
                if (!tz.isNullOrBlank()) timeZoneId = tz
            }
        } catch (_: Exception) {}

        val tz = TimeZone.getTimeZone(timeZoneId)
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance(tz).apply { timeInMillis = now }

        // 2. Night doses after midnight (00:00 to 04:59) recorded under previous day
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        if (hour in 0..4) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { timeZone = tz }
        val targetDate = dateFormat.format(calendar.time)

        val availableSlots = med.selectedTimes.split(",").map { it.trim() }.filter { it.isNotBlank() }

        // 3. Slot determination: pass exact slot or find nearest untaken slot
        val slot = if (!preferredSlot.isNullOrBlank() && availableSlots.any { it.equals(preferredSlot, ignoreCase = true) }) {
            availableSlots.first { it.equals(preferredSlot, ignoreCase = true) }
        } else {
            val todayDoses = doseDao.getFullDoseList().filter { it.medicineId == med.id && it.date == targetDate && it.taken }
            val untakenSlots = availableSlots.filter { s -> todayDoses.none { it.slot.equals(s, ignoreCase = true) } }
            untakenSlots.firstOrNull() ?: availableSlots.firstOrNull() ?: "Morning"
        }

        val newStock = (med.currentStock - 1).coerceAtLeast(0)
        val updatedMed = med.copy(
            currentStock = newStock,
            lastTakenTimestamp = now,
            synced = false
        )
        medicineDao.updateMedicine(updatedMed)

        val doseId = "${med.id}_${targetDate}_${slot}"
        val doseEntity = MedicineDoseEntity(
            id = doseId,
            profileId = med.profileId,
            medicineId = med.id,
            slot = slot,
            date = targetDate,
            taken = true,
            takenAt = now,
            synced = false
        )
        doseDao.insertDose(doseEntity)

        val isBlocked = LocalDataOwnership.isOwnershipBlocked(context)
        if (!isBlocked) {
            val syncRepo = SyncRepository(context)
            syncRepo.syncAllUnsynced()
            SyncWorker.enqueue(context)
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Saved on this phone. Open the app to confirm which account this phone belongs to.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
