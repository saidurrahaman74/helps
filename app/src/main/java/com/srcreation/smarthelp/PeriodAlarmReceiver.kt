package com.srcreation.smarthelp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class PeriodAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("REMINDER_TYPE") ?: return
        showNotification(context, type)

        if (type == "TOMORROW") {
            val alarmIntent = Intent(context, PeriodAlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            try {
                context.startActivity(alarmIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showNotification(context: Context, type: String) {
        val channelId = "period_reminder_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Period Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Period tracking alarms and alerts"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val currentLocale = context.resources.configuration.locales[0].language
        val isBangla = currentLocale == "bn"

        val title = if (type == "TOMORROW") {
            context.getString(R.string.notif_period_tomorrow)
        } else {
            if (isBangla) "পিরিয়ড শুরু হয় নাই" else "Period Not Started"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(if (isBangla) "আপনার পরবর্তী পিরিয়ড সাইকেল আগামীকাল শুরু হতে যাচ্ছে।" else "Your next period cycle begins tomorrow.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(if (type == "TOMORROW") 2001 else 2002, notification)
    }
}
