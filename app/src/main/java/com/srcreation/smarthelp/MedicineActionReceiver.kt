package com.srcreation.smarthelp

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.srcreation.smarthelp.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicineActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val medId = intent.getStringExtra("MED_ID") ?: ""
        val slot = intent.getStringExtra("EXTRA_SLOT")
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 1001)

        if (action == "ACTION_TAKEN" && medId.isNotBlank()) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    MedicineRepository(context).markDoseTaken(medId, slot)
                    val db = AppDatabase.getDatabase(context)
                    val med = db.medicineDao().getFullMedicineList().find { it.id == medId }
                    med?.let {
                        // 🌐 Sync to Caregiver Cloud
                        CaregiverSyncRepository.syncMedicineTaken(
                            context = context,
                            medicineName = it.name,
                            dosage = "${it.dosageValue} ${it.dosageUnit}".trim(),
                            timeCategory = slot ?: it.selectedTimes,
                            status = "TAKEN"
                        )

                        // ⚠️ Refill Alert Check
                        if (it.currentStock <= 3) {
                            val title = context.getString(R.string.med_refill_alert_title)
                            val body = context.getString(R.string.med_refill_alert_body, it.currentStock, it.dosageUnit, it.name)
                            NotificationHelper.showNotification(context, title, body)

                            CaregiverSyncRepository.syncVitals(
                                context = context,
                                type = "REFILL",
                                title = "স্টক রিফিল নোটিশ (${it.name})",
                                valueText = "মাত্র ${it.currentStock}টি বাকি আছে",
                                statusTag = "Refill Soon"
                            )
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        } else if (action == "ACTION_SNOOZE" && medId.isNotBlank()) {
            val medName = intent.getStringExtra("MED_NAME") ?: "Medicine"
            val category = intent.getStringExtra("MED_CAT") ?: "None"
            val dosage = intent.getStringExtra("MED_DOSAGE") ?: ""
            val profileId = intent.getIntExtra("PROFILE_ID", 1)

            MedicineAlarmScheduler.scheduleSnoozeAlarm(
                context = context,
                medName = medName,
                medId = medId,
                category = category,
                dosage = dosage,
                profileId = profileId,
                delayMinutes = 10,
                slot = slot
            )

            // 🌐 Sync Snooze to Caregiver Cloud
            CaregiverSyncRepository.syncMedicineTaken(
                context = context,
                medicineName = medName,
                dosage = dosage,
                timeCategory = category,
                status = "SNOOZED"
            )
        }

        // Dismiss notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }
}
