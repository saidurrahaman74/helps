package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.InsulinEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class InsulinViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.insulinDao()
    
    // Get profileId from intent (1 for Patient, 2 for Nurse)
    private val profileId = savedStateHandle.get<Int>("PROFILE_ID") ?: 1

    val allRecords: Flow<List<InsulinEntity>> = dao.getAllRecords(profileId)

    fun addRecord(glucose: String, unit: String, dose: String, iUnit: String, meal: String, daytime: String, date: String, time: String) {
        viewModelScope.launch {
            dao.insertRecord(
                InsulinEntity(
                    profileId = profileId,
                    glucose = glucose,
                    unit = unit,
                    dose = dose,
                    iUnit = iUnit,
                    mealTime = meal,
                    dayTime = daytime,
                    date = date,
                    time = time
                )
            )

            // 🌐 Caregiver Sync
            CaregiverSyncRepository.syncVitals(
                context = getApplication(),
                type = "SUGAR",
                title = "রক্তের গ্লুকোজ / ডায়াবেটিস",
                valueText = "$glucose $unit (ইনসুলিন: $dose $iUnit)",
                statusTag = "$daytime ($meal)"
            )

            val syncRepo = SyncRepository(getApplication())
            syncRepo.syncAllUnsynced()
            syncRepo.triggerSync()
        }
    }

    fun deleteRecord(record: InsulinEntity) {
        viewModelScope.launch {
            dao.deleteRecord(record)
        }
    }
}
