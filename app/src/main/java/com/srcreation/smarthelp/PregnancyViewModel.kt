package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.GyneProfileEntity
import com.srcreation.smarthelp.db.KickLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PregnancyViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val gyneDao = db.gyneDao()
    private val pregnancyDao = db.pregnancyDao()
    private val periodDao = db.periodDao()
    private val pref = PreferenceManager(application)

    val gyneProfile: Flow<GyneProfileEntity?> = gyneDao.getGyneProfile(pref.getActiveProfile())
    val kickLogs: Flow<List<KickLogEntity>> = pregnancyDao.getAllKickLogs(pref.getActiveProfile())
    
    // Automated pregnancy data using Period Tracker's LMP if available
    val automatedPregnancyData: Flow<PregnancyData?> = combine(
        gyneProfile,
        periodDao.getSettings(pref.getActiveProfile())
    ) { gyne, period ->
        val lmpString = gyne?.lmpDate?.ifBlank { null } 
            ?: period?.lastPeriodDate?.let { SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(Date(it)) }
            ?: return@combine null

        calculatePregnancy(lmpString)
    }

    fun updatePregnancyStatus(isPregnant: Boolean) {
        viewModelScope.launch {
            gyneDao.updatePregnancyStatus(pref.getActiveProfile(), isPregnant)
        }
    }

    fun addKickLog(count: Int, duration: Int) {
        viewModelScope.launch {
            val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            pregnancyDao.insertKickLog(
                KickLogEntity(
                    profileId = pref.getActiveProfile(),
                    kickCount = count,
                    durationMinutes = duration,
                    date = date
                )
            )
        }
    }

    private fun calculatePregnancy(lmpDate: String): PregnancyData? {
        return try {
            val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            val lmp = sdf.parse(lmpDate) ?: return null
            val today = Calendar.getInstance().apply { 
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) 
            }.time

            val diffInMillis = today.time - lmp.time
            val days = TimeUnit.MILLISECONDS.toDays(diffInMillis).toInt()
            
            if (days < 0 || days > 280) return null // Standard pregnancy is 280 days (40 weeks)

            val weeks = days / 7
            val remainingDays = days % 7
            
            // EDD calculation (Naegele's Rule: LMP + 7 days - 3 months + 1 year)
            val eddCal = Calendar.getInstance().apply {
                time = lmp
                add(Calendar.DAY_OF_YEAR, 280)
            }

            val trimester = when {
                weeks < 13 -> 1
                weeks < 27 -> 2
                else -> 3
            }

            PregnancyData(
                lmpDate = lmpDate,
                eddDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(eddCal.time),
                gestationalWeeks = weeks,
                gestationalDays = remainingDays,
                trimester = trimester,
                daysRemaining = (280 - days)
            )
        } catch (e: Exception) {
            null
        }
    }
}

data class PregnancyData(
    val lmpDate: String,
    val eddDate: String,
    val gestationalWeeks: Int,
    val gestationalDays: Int,
    val trimester: Int,
    val daysRemaining: Int
)
