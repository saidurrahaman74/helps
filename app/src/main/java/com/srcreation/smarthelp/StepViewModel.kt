package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.StepEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class StepViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val stepDao = db.stepDao()
    
    // Get profileId from intent
    private val profileId = savedStateHandle.get<Int>("PROFILE_ID") ?: 1

    val allRecords: Flow<List<StepEntity>> = stepDao.getAllRecords(profileId)

    fun addStepRecord(steps: Int, date: String) {
        viewModelScope.launch {
            stepDao.insertRecord(
                StepEntity(
                    profileId = profileId,
                    steps = steps,
                    date = date
                )
            )

            // 🌐 Sync to Caregiver / Family Cloud
            CaregiverSyncRepository.syncVitals(
                context = getApplication(),
                type = "STEP",
                title = "দৈনিক হাঁটার গতিবিধি",
                valueText = "$steps কদম",
                statusTag = "Active"
            )

            val syncRepo = SyncRepository(getApplication())
            syncRepo.syncAllUnsynced()
            syncRepo.triggerSync()
        }
    }

    fun clearAllStepRecords(onCleared: () -> Unit) {
        viewModelScope.launch {
            stepDao.deleteByProfile(profileId)
            onCleared()
        }
    }
}
