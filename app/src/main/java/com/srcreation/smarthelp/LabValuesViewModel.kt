package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.LabRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class LabValuesViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.labDao()
    
    private val profileId = savedStateHandle.get<Int>("PROFILE_ID") ?: 1

    val allRecords: Flow<List<LabRecordEntity>> = dao.getAllRecords(profileId)

    fun addRecord(
        testName: String,
        resultValue: Double,
        unit: String,
        normalMin: Double,
        normalMax: Double,
        date: String
    ) {
        val statusTag = if (resultValue > normalMax) {
            "HIGH"
        } else if (resultValue < normalMin) {
            "LOW"
        } else {
            "NORMAL"
        }

        viewModelScope.launch {
            dao.insertRecord(
                LabRecordEntity(
                    profileId = profileId,
                    testName = testName,
                    resultValue = resultValue,
                    unit = unit,
                    normalMin = normalMin,
                    normalMax = normalMax,
                    statusTag = statusTag,
                    date = date
                )
            )

            // 🌐 Caregiver Sync
            CaregiverSyncRepository.syncVitals(
                context = getApplication(),
                type = "LAB",
                title = "ল্যাব রিপোর্ট ($testName)",
                valueText = "$resultValue $unit",
                statusTag = statusTag
            )
        }
    }

    fun deleteRecord(record: LabRecordEntity) {
        viewModelScope.launch {
            dao.deleteRecord(record)
        }
    }
}
