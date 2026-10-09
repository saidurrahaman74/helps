package com.srcreation.smarthelp

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.PeriodEntity
import com.srcreation.smarthelp.db.PeriodLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PeriodViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val periodDao = db.periodDao()
    private val pref = PreferenceManager(application)

    val settings: Flow<PeriodEntity?> = periodDao.getSettings(pref.getActiveProfile())
    val allLogs: Flow<List<PeriodLogEntity>> = periodDao.getAllLogs(pref.getActiveProfile())

    fun saveSettings(lastPeriodDate: Long?, cycleLength: Int, periodDuration: Int) {
        viewModelScope.launch {
            periodDao.saveSettings(
                PeriodEntity(
                    profileId = pref.getActiveProfile(),
                    lastPeriodDate = lastPeriodDate,
                    cycleLength = cycleLength,
                    periodDuration = periodDuration
                )
            )
            scheduleReminders(lastPeriodDate, cycleLength)
        }
    }

    private fun scheduleReminders(lastPeriodDate: Long?, cycleLength: Int) {
        if (lastPeriodDate == null) return
        
        val context = getApplication<Application>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val nextPeriodStart = Calendar.getInstance().apply { 
            timeInMillis = lastPeriodDate
            add(Calendar.DAY_OF_YEAR, cycleLength)
        }

        // 1. One day before
        val tomorrowReminder = Calendar.getInstance().apply {
            timeInMillis = nextPeriodStart.timeInMillis
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 9) // 9:00 AM
            set(Calendar.MINUTE, 0)
        }

        // 2. One day after (Overdue)
        val overdueReminder = Calendar.getInstance().apply {
            timeInMillis = nextPeriodStart.timeInMillis
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 10) // 10:00 AM
            set(Calendar.MINUTE, 0)
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

    fun addLog(mood: String, flow: String, pain: String, symptoms: String) {
        viewModelScope.launch {
            val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            periodDao.insertLog(
                PeriodLogEntity(
                    profileId = pref.getActiveProfile(),
                    date = date,
                    mood = mood,
                    flow = flow,
                    pain = pain,
                    symptoms = symptoms
                )
            )
        }
    }
}
