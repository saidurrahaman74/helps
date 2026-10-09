package com.srcreation.smarthelp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.PeriodEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MedicineBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON" ||
            action == "android.intent.action.REBOOT"
        ) {
            val db = AppDatabase.getDatabase(context)
            CoroutineScope(Dispatchers.IO).launch {
                // 1. Reschedule Medicine Alarms
                try {
                    val allMedicines = db.medicineDao().getFullMedicineList()
                    allMedicines.forEach { med ->
                        MedicineAlarmScheduler.scheduleAlarms(context, med)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 2. Reschedule Period Reminders
                try {
                    val periodSettingsList = db.periodDao().getFullSettingsList()
                    periodSettingsList.forEach { periodSetting ->
                        periodSetting.lastPeriodDate?.let { lastDate ->
                            reschedulePeriodReminders(context, lastDate, periodSetting.cycleLength)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // 3. Reschedule Duty Roster Shift Alarms
                try {
                    val rosterMap = DutyRosterStorage.loadRosterMap(context)
                    rosterMap.values.filter { it.isAlarmSet }.forEach { shiftData ->
                        rescheduleShiftAlarm(context, shiftData)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun reschedulePeriodReminders(context: Context, lastPeriodDate: Long, cycleLength: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val nextPeriodStart = Calendar.getInstance().apply {
            timeInMillis = lastPeriodDate
            add(Calendar.DAY_OF_YEAR, cycleLength)
        }

        val tomorrowReminder = Calendar.getInstance().apply {
            timeInMillis = nextPeriodStart.timeInMillis
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val overdueReminder = Calendar.getInstance().apply {
            timeInMillis = nextPeriodStart.timeInMillis
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        scheduleAlarm(context, alarmManager, tomorrowReminder.timeInMillis, "TOMORROW", 2001)
        scheduleAlarm(context, alarmManager, overdueReminder.timeInMillis, "OVERDUE", 2002)
    }

    private fun scheduleAlarm(context: Context, alarmManager: AlarmManager, time: Long, type: String, requestCode: Int) {
        if (time <= System.currentTimeMillis()) return

        val intent = Intent(context, PeriodAlarmReceiver::class.java).apply {
            putExtra("REMINDER_TYPE", type)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        }
    }

    private fun rescheduleShiftAlarm(context: Context, shiftData: ShiftData) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val dateCal = try {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val d = sdf.parse(shiftData.date) ?: return
            Calendar.getInstance().apply { time = d }
        } catch (_: Exception) {
            return
        }

        val targetCal = Calendar.getInstance().apply {
            timeInMillis = dateCal.timeInMillis
            when (shiftData.shiftType) {
                "Morning" -> { set(Calendar.HOUR_OF_DAY, 7); set(Calendar.MINUTE, 0) }
                "Evening" -> { set(Calendar.HOUR_OF_DAY, 13); set(Calendar.MINUTE, 0) }
                "Night" -> { set(Calendar.HOUR_OF_DAY, 19); set(Calendar.MINUTE, 30) }
                else -> { set(Calendar.HOUR_OF_DAY, 7); set(Calendar.MINUTE, 0) }
            }
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (targetCal.timeInMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
            putExtra("MED_NAME", "Nurse Duty Shift: ${shiftData.shiftType}")
            putExtra("MED_CAT", "Shift Alarm")
            putExtra("MED_DOSAGE", "Time to prepare for ${shiftData.shiftType} shift!")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ("duty_shift_${targetCal.timeInMillis}").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetCal.timeInMillis, pendingIntent)
        }
    }
}
