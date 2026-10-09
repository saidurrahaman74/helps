package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.MedicalRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MedicalVaultViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.medicalRecordDao()
    
    // Isolation: Get profileId from intent or default to 1 (Patient)
    private val profileId = savedStateHandle.get<Int>("PROFILE_ID") ?: 1

    val allRecords: Flow<List<MedicalRecordEntity>> = dao.getAllRecords(profileId)

    fun addRecord(title: String, type: String, imagePaths: List<String>, note: String) {
        viewModelScope.launch {
            val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            // Save as comma-separated string for multi-image support
            val pathsString = imagePaths.joinToString(",")
            dao.insertRecord(
                MedicalRecordEntity(
                    profileId = profileId,
                    title = title,
                    type = type,
                    date = date,
                    imageUris = pathsString,
                    note = note
                )
            )
        }
    }

    fun deleteRecord(record: MedicalRecordEntity) {
        viewModelScope.launch {
            dao.deleteRecord(record)
        }
    }
}
