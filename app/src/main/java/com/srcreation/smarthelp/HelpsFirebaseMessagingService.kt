package com.srcreation.smarthelp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class HelpsFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        AuthRepository().updateFcmToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Smart Help Instruction"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["text"] ?: "New instruction received from caregiver."

        showNotification(applicationContext, title, body)
    }

    private fun showNotification(context: Context, title: String, message: String) {
        val channelId = "fcm_instructions_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Caregiver Instructions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new instructions from caregiver"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val prefManager = PreferenceManager(context)
        val role = prefManager.getFamilyRole()

        val targetIntent = if (role.equals("CAREGIVER", ignoreCase = true)) {
            Intent(context, CaregiverDashboardActivity::class.java)
        } else {
            Intent(context, PatientInstructionsActivity::class.java)
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), targetIntent, flags)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_splash_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
