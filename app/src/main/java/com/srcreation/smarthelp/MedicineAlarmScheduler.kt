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

            if (is4Times) {
                scheduleSpecificAlarm(context, med, 6, 0, "4t_1", "Morning")
                scheduleSpecificAlarm(context, med, 12, 0, "4t_2", "Noon")
                scheduleSpecificAlarm(context, med, 18, 0, "4t_3", "Evening")
                scheduleSpecificAlarm(context, med, 0, 0, "4t_4", "Night")
                return@forEach
            }

            val totalMins = MedicineScheduleHelper.getScheduledMinutesForSlot(timeKey, med.customTime) ?: (8 * 60)
            val normalizedMins = if (totalMins >= 1440) totalMins - 1440 else totalMins
            val h = normalizedMins / 60
            val m = normalizedMins % 60
            calendar.set(Calendar.HOUR_OF_DAY, h)
            calendar.set(Calendar.MINUTE, m)
            if (totalMins >= 1440) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis() + 1000L) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val medName = med.name
            val medId = med.id
            val category = med.category
            val dosage = if (med.dosageValue.isNotBlank()) "${med.dosageValue} ${med.dosageUnit}" else ""
            val profileId = med.profileId

            val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
                putExtra("MED_NAME", medName)
                putExtra("MED_ID", medId)
                putExtra("MED_CAT", category)
                putExtra("MED_DOSAGE", dosage)
                putExtra("PROFILE_ID", profileId)
                putExtra("EXTRA_SLOT", timeKey)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context, 
                (med.id.toString() + timeKey).hashCode(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            setAlarmSafely(context, alarmManager, calendar.timeInMillis, pendingIntent, medName, medId, category, dosage, profileId, timeKey)
        }
    }

    private fun scheduleSpecificAlarm(context: Context, med: MedicineEntity, hour: Int, minute: Int, suffix: String, slotName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis() + 1000L) add(Calendar.DAY_OF_YEAR, 1)
        }

        val medName = med.name
        val medId = med.id
        val category = med.category
        val dosage = if (med.dosageValue.isNotBlank()) "${med.dosageValue} ${med.dosageUnit}" else ""
        val profileId = med.profileId

        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            putExtra("MED_NAME", medName)
            putExtra("MED_ID", medId)
            putExtra("MED_CAT", category)
            putExtra("MED_DOSAGE", dosage)
            putExtra("PROFILE_ID", profileId)
            putExtra("EXTRA_SLOT", slotName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, (med.id.toString() + suffix).hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setAlarmSafely(context, alarmManager, calendar.timeInMillis, pendingIntent, medName, medId, category, dosage, profileId, slotName)
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

        setAlarmSafely(context, alarmManager, triggerAtMs, pendingIntent, medName, medId, category, dosage, profileId, slot)
    }

    private fun setAlarmSafely(
        context: Context,
        alarmManager: AlarmManager,
        triggerAtMs: Long,
        pendingIntent: PendingIntent,
        medName: String = "Medicine",
        medId: String = "",
        category: String = "None",
        dosage: String = "",
        profileId: Int = 1,
        slot: String? = null
    ) {
        try {
            val showIntent = Intent(context, MedicineAlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NO_USER_ACTION
                putExtra("MED_NAME", medName)
                putExtra("MED_ID", medId)
                putExtra("MED_CAT", category)
                putExtra("MED_DOSAGE", dosage)
                putExtra("PROFILE_ID", profileId)
                if (slot != null) putExtra("EXTRA_SLOT", slot)
            }
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val showPendingIntent = PendingIntent.getActivity(context, triggerAtMs.hashCode(), showIntent, flags)
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMs, showPendingIntent)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            } else {
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            }
        } catch (e: Exception) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            } catch (_: Exception) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            }
        }
    }
}
