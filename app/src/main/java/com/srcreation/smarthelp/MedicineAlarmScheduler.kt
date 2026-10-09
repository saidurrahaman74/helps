package com.srcreation.smarthelp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.srcreation.smarthelp.db.MedicineEntity
import java.util.*

object MedicineAlarmScheduler {
    fun scheduleAlarms(context: Context, med: MedicineEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val timeList = med.selectedTimes.split(",")
        
        timeList.forEach { rawKey ->
            val timeKey = rawKey.trim()
            if (timeKey.isBlank()) return@forEach

            val calendar = Calendar.getInstance().apply {
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val is4Times = timeKey.contains("4", ignoreCase = true) || timeKey.contains("6", ignoreCase = true)
            val isCustom = timeKey.contains("Set", ignoreCase = true) || timeKey.contains("Custom", ignoreCase = true)

            if (is4Times) {
                scheduleSpecificAlarm(context, med, 6, 0, "4t_1", "Morning")
                scheduleSpecificAlarm(context, med, 12, 0, "4t_2", "Noon")
                scheduleSpecificAlarm(context, med, 18, 0, "4t_3", "Evening")
                scheduleSpecificAlarm(context, med, 0, 0, "4t_4", "Night")
                return@forEach
            }

            if (isCustom) {
                med.customTime?.let { custom ->
                    val parts = custom.split(":")
                    if (parts.size == 2) {
                        val h = parts[0].trim().toIntOrNull() ?: 8
                        val m = parts[1].trim().toIntOrNull() ?: 0
                        calendar.set(Calendar.HOUR_OF_DAY, h)
                        calendar.set(Calendar.MINUTE, m)
                    }
                }
            } else {
                when {
                    timeKey.equals("Morning", ignoreCase = true) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, 8)
                        calendar.set(Calendar.MINUTE, 0)
                    }
                    timeKey.equals("Noon", ignoreCase = true) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, 14)
                        calendar.set(Calendar.MINUTE, 0)
                    }
                    timeKey.equals("Evening", ignoreCase = true) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, 18)
                        calendar.set(Calendar.MINUTE, 0)
                    }
                    timeKey.equals("Night", ignoreCase = true) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, 22)
                        calendar.set(Calendar.MINUTE, 0)
                    }
                    else -> {
                        calendar.set(Calendar.HOUR_OF_DAY, 8)
                        calendar.set(Calendar.MINUTE, 0)
                    }
                }
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
                putExtra("MED_NAME", med.name)
                putExtra("MED_ID", med.id)
                putExtra("MED_CAT", med.category)
                putExtra("MED_DOSAGE", if (med.dosageValue.isNotBlank()) "${med.dosageValue} ${med.dosageUnit}" else "")
                putExtra("PROFILE_ID", med.profileId)
                putExtra("EXTRA_SLOT", timeKey)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context, 
                (med.id.toString() + timeKey).hashCode(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            setAlarmSafely(alarmManager, calendar.timeInMillis, pendingIntent)
        }
    }

    private fun scheduleSpecificAlarm(context: Context, med: MedicineEntity, hour: Int, minute: Int, suffix: String, slotName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            putExtra("MED_NAME", med.name)
            putExtra("MED_ID", med.id)
            putExtra("MED_CAT", med.category)
            putExtra("MED_DOSAGE", if (med.dosageValue.isNotBlank()) "${med.dosageValue} ${med.dosageUnit}" else "")
            putExtra("PROFILE_ID", med.profileId)
            putExtra("EXTRA_SLOT", slotName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, (med.id.toString() + suffix).hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setAlarmSafely(alarmManager, calendar.timeInMillis, pendingIntent)
    }

    fun cancelAlarms(context: Context, med: MedicineEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val timeList = med.selectedTimes.split(",")
        timeList.forEach { t ->
            val intent = Intent(context, MedicineAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, (med.id.toString() + t.trim()).hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
        listOf("4t_1", "4t_2", "4t_3", "4t_4", "_snooze").forEach { suffix ->
            val intent = Intent(context, MedicineAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, (med.id.toString() + suffix).hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    fun scheduleSnoozeAlarm(
        context: Context,
        medName: String,
        medId: String,
        category: String,
        dosage: String,
        profileId: Int = 1,
        delayMinutes: Int = 10,
        slot: String? = null
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtMs = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)

        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            putExtra("MED_NAME", medName)
            putExtra("MED_ID", medId)
            putExtra("MED_CAT", category)
            putExtra("MED_DOSAGE", dosage)
            putExtra("PROFILE_ID", profileId)
            if (slot != null) putExtra("EXTRA_SLOT", slot)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (medId.toString() + "_snooze").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarmSafely(alarmManager, triggerAtMs, pendingIntent)
    }

    private fun setAlarmSafely(alarmManager: AlarmManager, triggerAtMs: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
        }
    }
}
