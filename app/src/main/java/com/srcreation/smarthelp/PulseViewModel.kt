package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.PulseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PulseViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val pulseDao = db.pulseDao()
    
    private val profileId = savedStateHandle.get<Int>("PROFILE_ID") ?: 1

    val allRecords: Flow<List<PulseEntity>> = pulseDao.getAllRecords(profileId)

    fun addPulseRecord(bpm: Int, activityState: String, date: String, time: String) {
        viewModelScope.launch {
            pulseDao.insertRecord(
                PulseEntity(
                    profileId = profileId,
                    pulseBpm = bpm,
                    activityState = activityState,
                    date = date,
                    time = time
                )
            )

            // 🌐 Sync to Caregiver / Family Cloud
            val statusTag = when {
                bpm > 145 -> "Danger High (>145)"
                bpm > 100 -> "High Pulse (Tachycardia)"
                bpm < 60 -> "Low Pulse (Bradycardia)"
                else -> "Normal Pulse"
            }
            CaregiverSyncRepository.syncVitals(
                context = getApplication(),
                type = "PULSE",
                title = "হার্ট রেট ও পাল্স (Pulse Tracker)",
                valueText = "$bpm bpm ($activityState)",
                statusTag = statusTag
            )

            val syncRepo = SyncRepository(getApplication())
            syncRepo.syncAllUnsynced()
            syncRepo.triggerSync()
        }
    }

    fun deleteRecord(record: PulseEntity) {
        viewModelScope.launch {
            DeletionSync.deletePulse(getApplication(), record)
        }
    }

    fun clearAllRecords(onCleared: () -> Unit) {
        viewModelScope.launch {
            val list = pulseDao.getFullPulseList().filter { it.profileId == profileId }
            for (record in list) {
                DeletionSync.deletePulse(getApplication(), record)
            }
            onCleared()
        }
    }
}
