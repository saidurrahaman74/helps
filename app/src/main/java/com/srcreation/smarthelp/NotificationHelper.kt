package com.srcreation.smarthelp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "smart_help_updates"
    private const val CHANNEL_NAME = "Smart Help Updates"
    private const val ALARM_CHANNEL_ID = "medicine_alarm_channel"

    fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_splash_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    fun showMedicineAlarm(context: Context, title: String, message: String, medId: String = "") {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = if (medId.isNotBlank()) medId.hashCode() else 1001

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(ALARM_CHANNEL_ID, "Medicine Alarms", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Urgent notifications for taking medicine"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MedicineReminderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        // Action Intent for "Taken" button
        val takenIntent = Intent(context, MedicineActionReceiver::class.java).apply {
            action = "ACTION_TAKEN"
            putExtra("MED_ID", medId)
            putExtra("NOTIFICATION_ID", notificationId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(context, 2, takenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Action Intent for "Snooze" button
        val snoozeIntent = Intent(context, MedicineActionReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra("MED_ID", medId)
            putExtra("MED_NAME", message)
            putExtra("NOTIFICATION_ID", notificationId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(context, 3, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_splash_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_splash_foreground, "✓ TAKEN", takenPendingIntent)
            .addAction(R.drawable.ic_splash_foreground, "⏱️ SNOOZE 10 MINS", snoozePendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
