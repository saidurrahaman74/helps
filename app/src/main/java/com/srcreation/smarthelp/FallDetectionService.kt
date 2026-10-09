package com.srcreation.smarthelp

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlin.math.sqrt

class FallDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    
    private val FALL_THRESHOLD = 30.0 // High impact G-force
    private val STILL_THRESHOLD = 2.0 // Lack of movement after fall
    
    private var lastImpactTime: Long = 0
    private var isImpactDetected = false

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1002, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(1002, createNotification())
        }
    }

    private fun createNotification(): Notification {
        val channelId = "fall_detection_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, 
                "Fall Detection Service", 
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the fall detection sensor active to protect you."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, 
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Fall Detection is Active")
            .setContentText("Smart Help is monitoring for sudden impacts.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            val acceleration = sqrt(x * x + y * y + z * z)
            
            // Detect Impact
            if (acceleration > FALL_THRESHOLD) {
                lastImpactTime = System.currentTimeMillis()
                isImpactDetected = true
            }
            
            // If impact was detected, check for lack of movement
            if (isImpactDetected && (System.currentTimeMillis() - lastImpactTime > 2000)) {
                if (acceleration < STILL_THRESHOLD) {
                    triggerFallAlert()
                    isImpactDetected = false // Reset
                }
            }
            
            // Reset if too much time passed without stillness
            if (isImpactDetected && (System.currentTimeMillis() - lastImpactTime > 5000)) {
                isImpactDetected = false
            }
        }
    }

    private fun triggerFallAlert() {
        // 🌐 Sync Fall Detection to Caregiver Cloud
        CaregiverSyncRepository.syncEmergencyAlert(
            context = this,
            alertType = "FALL_DETECTED",
            message = "⚠️ পড়ে যাওয়ার (Fall Detected) ঘটনা শনাক্ত করা হয়েছে!",
            location = ""
        )

        val intent = Intent(this, FallTriggerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}
