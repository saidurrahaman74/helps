package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.BpEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BpViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val bpDao = db.bpDao()
    
    // Get profileId from intent (1 for Patient, 2 for Nurse)
    private val profileId = savedStateHandle.get<Int>("PROFILE_ID") ?: 1

    val allRecords: Flow<List<BpEntity>> = bpDao.getAllRecords(profileId)

    fun addRecord(systolic: Int, diastolic: Int, timeCategory: String, date: String) {
        viewModelScope.launch {
            bpDao.insertRecord(
                BpEntity(
                    profileId = profileId,
                    systolic = systolic,
                    diastolic = diastolic,
                    timeCategory = timeCategory,
                    date = date
                )
            )

            // 🌐 Caregiver Sync
            val tag = when {
                systolic > 170 || diastolic > 110 -> "Danger High (বেশি বিপজ্জনক)"
                systolic >= 140 || diastolic >= 90 -> "উচ্চ রক্তচাপ"
                systolic < 90 || diastolic < 60 -> "নিম্ন রক্তচাপ"
                else -> "স্বাভাবিক"
            }
            CaregiverSyncRepository.syncVitals(
                context = getApplication(),
                type = "BP",
                title = "ব্লাড প্রেসার",
                valueText = "$systolic/$diastolic mmHg ($timeCategory)",
                statusTag = tag
            )

            // 🌐 Sync Unsynced Room Records to Firestore if role is Patient
            val syncRepo = SyncRepository(getApplication())
            syncRepo.syncAllUnsynced()
            syncRepo.triggerSync()
        }
    }

    fun deleteRecord(record: BpEntity) {
        viewModelScope.launch {
            DeletionSync.deleteBp(getApplication(), record)
        }
    }
}
