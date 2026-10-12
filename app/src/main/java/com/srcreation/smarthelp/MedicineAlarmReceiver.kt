package com.srcreation.smarthelp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.srcreation.smarthelp.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicineAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medName = intent.getStringExtra("MED_NAME") ?: "Medicine"
        val medId = intent.getStringExtra("MED_ID") ?: ""
        val category = intent.getStringExtra("MED_CAT") ?: "None"
        val dosage = intent.getStringExtra("MED_DOSAGE") ?: ""
        val profileId = intent.getIntExtra("PROFILE_ID", 1)
        val slot = intent.getStringExtra("EXTRA_SLOT") ?: ""

        // 1. Acquire WakeLock to keep CPU awake during alarm trigger
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "SmartHelp:MedicineAlarmWakeLock"
        )
        wakeLock?.acquire(10 * 1000L) // 10 seconds

        // 2. Reschedule alarm for tomorrow so it repeats daily
        if (medId.isNotBlank()) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val medList = db.medicineDao().getFullMedicineList()
                    val med = medList.find { it.id == medId }
                    med?.let {
                        MedicineAlarmScheduler.scheduleAlarms(context, it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }

        // 3. Launch Full Screen Activity DIRECTLY to guarantee instant full screen alarm on time
        val fullScreenIntent = Intent(context, MedicineAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra("MED_NAME", medName)
            putExtra("MED_ID", medId)
            putExtra("MED_CAT", category)
            putExtra("MED_DOSAGE", dosage)
            putExtra("PROFILE_ID", profileId)
            putExtra("EXTRA_SLOT", slot)
        }
        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 4. Show Notification with Full Screen Intent and Actions
        showMedicineNotification(context, medName, medId, category, dosage, profileId, slot)

        // 5. Sync "DUE" status to Caregiver Cloud
        CaregiverSyncRepository.syncMedicineTaken(
            context = context,
            medicineName = medName,
            dosage = dosage,
            timeCategory = if (slot.isNotBlank()) slot else category,
            status = "DUE"
        )
    }

    private fun showMedicineNotification(context: Context, name: String, id: String, category: String, dosage: String, profileId: Int, slot: String) {
        val channelId = "medicine_reminder_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Medicine Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alarm for medicine reminders"
                enableVibration(true)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(context, MedicineAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("MED_NAME", name)
            putExtra("MED_ID", id)
            putExtra("MED_CAT", category)
            putExtra("MED_DOSAGE", dosage)
            putExtra("PROFILE_ID", profileId)
            putExtra("EXTRA_SLOT", slot)
        }
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val notificationId = if (id.isNotBlank()) id.hashCode() else 1001
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context, notificationId, fullScreenIntent, flags
        )

        // Action: Taken
        val takeIntent = Intent(context, MedicineActionReceiver::class.java).apply {
            action = "ACTION_TAKEN"
            putExtra("MED_ID", id)
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("EXTRA_SLOT", slot)
        }
        val takePendingIntent = PendingIntent.getBroadcast(context, notificationId + 1, takeIntent, flags)

        // Action: Snooze
        val snoozeIntent = Intent(context, MedicineActionReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra("MED_ID", id)
            putExtra("MED_NAME", name)
            putExtra("MED_CAT", category)
            putExtra("MED_DOSAGE", dosage)
            putExtra("PROFILE_ID", profileId)
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("EXTRA_SLOT", slot)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(context, notificationId + 2, snoozeIntent, flags)

        val isPill = category == "Tablet" || category == "Capsule"
        val title = if (isPill) "ওষুধ খেয়ে নিন ($name)" else "ঔষধ ব্যবহার করুন ($name)"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(name)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(R.drawable.ic_launcher_foreground, "✓ Taken", takePendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "⏰ Snooze", snoozePendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
