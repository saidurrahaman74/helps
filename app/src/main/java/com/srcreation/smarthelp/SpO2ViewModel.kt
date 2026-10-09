package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.SpO2Entity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SpO2ViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val spo2Dao = db.spo2Dao()
    
    private val profileId = savedStateHandle.get<Int>("PROFILE_ID") ?: 1

    val allRecords: Flow<List<SpO2Entity>> = spo2Dao.getAllRecords(profileId)

    fun addSpO2Record(spo2: Int, pulse: Int, breathingStatus: String, date: String, time: String) {
        viewModelScope.launch {
            spo2Dao.insertRecord(
                SpO2Entity(
                    profileId = profileId,
                    spo2Percentage = spo2,
                    pulseRate = pulse,
                    breathingStatus = breathingStatus,
                    date = date,
                    time = time
                )
            )

            // 🌐 Sync to Caregiver / Family Cloud
            val statusTag = when {
                spo2 < 81 -> "Danger Hypoxia (<81%)"
                spo2 < 90 -> "Hypoxia Alert (<90%)"
                spo2 in 90..94 -> "Borderline SpO2 (90-94%)"
                else -> "Normal SpO2 (95-100%)"
            }
            CaregiverSyncRepository.syncVitals(
                context = getApplication(),
                type = "SPO2",
                title = "অক্সিজেন স্যাচুরেশন (SpO2 Tracker)",
                valueText = "$spo2% SpO2 (Pulse: $pulse bpm)",
                statusTag = statusTag
            )

            val syncRepo = SyncRepository(getApplication())
            syncRepo.syncAllUnsynced()
            syncRepo.triggerSync()
        }
    }

    fun deleteRecord(record: SpO2Entity) {
        viewModelScope.launch {
            spo2Dao.deleteRecord(record)
        }
    }

    fun clearAllRecords(onCleared: () -> Unit) {
        viewModelScope.launch {
            spo2Dao.deleteByProfile(profileId)
            onCleared()
        }
    }
}
