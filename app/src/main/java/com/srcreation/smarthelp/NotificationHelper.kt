package com.srcreation.smarthelp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val CHANNEL_HEALTH_ALERTS = "health_alerts"
    const val CHANNEL_INSTRUCTIONS_REQUESTS = "instructions_requests"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val healthAlertsChannel = NotificationChannel(
                CHANNEL_HEALTH_ALERTS,
                "Health alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Important health alerts for linked patients and missed medicine doses"
            }

            val instructionsChannel = NotificationChannel(
                CHANNEL_INSTRUCTIONS_REQUESTS,
                "Instructions and requests",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "New instructions from caregivers and pending access requests"
            }

            notificationManager.createNotificationChannel(healthAlertsChannel)
            notificationManager.createNotificationChannel(instructionsChannel)
        }
    }

    fun showNotification(context: Context, title: String, message: String) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_HEALTH_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showHealthAlertNotification(
        context: Context,
        notificationId: Int,
        patientName: String,
        title: String,
        message: String
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, CaregiverDashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val publicNotification = NotificationCompat.Builder(context, CHANNEL_HEALTH_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Health alert")
            .setContentText("Health alert for $patientName. Open the app.")
            .setAutoCancel(true)
            .build()

        val notification = NotificationCompat.Builder(context, CHANNEL_HEALTH_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showInstructionNotification(
        context: Context,
        notificationId: Int,
        patientName: String,
        message: String
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, CaregiverInstructionsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("PATIENT_NAME", patientName)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val publicNotification = NotificationCompat.Builder(context, CHANNEL_INSTRUCTIONS_REQUESTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("New instruction")
            .setContentText("New instruction received. Open the app.")
            .setAutoCancel(true)
            .build()

        val notification = NotificationCompat.Builder(context, CHANNEL_INSTRUCTIONS_REQUESTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("New Instruction")
            .setContentText(message)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun showAccessRequestNotification(
        context: Context,
        notificationId: Int,
        caregiverName: String
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, WhoCanSeeMyDataActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val publicNotification = NotificationCompat.Builder(context, CHANNEL_INSTRUCTIONS_REQUESTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Access request")
            .setContentText("New access request. Open the app.")
            .setAutoCancel(true)
            .build()

        val notification = NotificationCompat.Builder(context, CHANNEL_INSTRUCTIONS_REQUESTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Pending Access Request")
            .setContentText("$caregiverName has requested access to your health data.")
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
