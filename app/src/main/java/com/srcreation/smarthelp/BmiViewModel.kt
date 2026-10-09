package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.BmiEntity
import com.srcreation.smarthelp.db.HealthGoalEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BmiViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val bmiDao = db.bmiDao()
    private val pref = PreferenceManager(application)

    // Get profileId from intent (1 for Patient, 2 for Nurse) or fallback to active profile
    val profileId: Int = savedStateHandle.get<Int>("PROFILE_ID") ?: pref.getActiveProfile()

    val allBmiRecords: Flow<List<BmiEntity>> = bmiDao.getAllBmiRecords(profileId)
    val allHealthGoals: Flow<List<HealthGoalEntity>> = bmiDao.getAllHealthGoals(profileId)

    fun addBmiRecord(bmi: Double, weight: Double, height: Double, date: String, category: String = "BMI") {
        viewModelScope.launch {
            bmiDao.insertBmi(
                BmiEntity(
                    profileId = profileId,
                    bmi = bmi,
                    weight = weight,
                    height = height,
                    category = category,
                    date = date
                )
            )

            // 🌐 Sync to Caregiver / Family Cloud
            CaregiverSyncRepository.syncVitals(
                context = getApplication(),
                type = "BMI",
                title = "ওজন ও বিএমআই",
                valueText = "ওজন: %.1f kg (BMI: %.1f)".format(weight, bmi),
                statusTag = category
            )
        }
    }

    fun getHealthGoals(type: String): Flow<List<HealthGoalEntity>> = bmiDao.getHealthGoals(profileId, type)

    fun addHealthGoal(type: String, value: Double, unit: String, date: String) {
        viewModelScope.launch {
            bmiDao.insertHealthGoal(
                HealthGoalEntity(
                    profileId = profileId,
                    type = type,
                    value = value,
                    unit = unit,
                    date = date
                )
            )
        }
    }

    fun deleteBmiRecord(id: Int) {
        viewModelScope.launch {
            bmiDao.deleteBmiById(id)
        }
    }

    fun deleteHealthGoal(id: Int) {
        viewModelScope.launch {
            bmiDao.deleteGoalById(id)
        }
    }

    fun addSampleRecords() {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

            val now = dateFormat.format(cal.time)
            
            cal.add(Calendar.DAY_OF_YEAR, -5)
            val date5DaysAgo = dateFormat.format(cal.time)

            cal.add(Calendar.DAY_OF_YEAR, -5)
            val date10DaysAgo = dateFormat.format(cal.time)

            cal.add(Calendar.DAY_OF_YEAR, -5)
            val date15DaysAgo = dateFormat.format(cal.time)

            val sampleList = listOf(
                BmiEntity(profileId = profileId, bmi = 25.4, weight = 73.5, height = 170.0, category = "BMI", date = date15DaysAgo, timestamp = System.currentTimeMillis() - 15 * 86400000L),
                BmiEntity(profileId = profileId, bmi = 24.8, weight = 71.8, height = 170.0, category = "BMI", date = date10DaysAgo, timestamp = System.currentTimeMillis() - 10 * 86400000L),
                BmiEntity(profileId = profileId, bmi = 24.2, weight = 70.0, height = 170.0, category = "BMI", date = date5DaysAgo, timestamp = System.currentTimeMillis() - 5 * 86400000L),
                BmiEntity(profileId = profileId, bmi = 23.8, weight = 68.8, height = 170.0, category = "BMI", date = now, timestamp = System.currentTimeMillis())
            )

            bmiDao.insertAllBmi(sampleList)
        }
    }
}
