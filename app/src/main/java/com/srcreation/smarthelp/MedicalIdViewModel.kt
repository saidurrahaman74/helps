package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.EmergencyContactEntity
import com.srcreation.smarthelp.db.MedicalIdEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MedicalIdViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.medicalIdDao()
    
    // Get profileId from intent
    private val profileId = savedStateHandle.get<Int>("PROFILE_ID") ?: 1

    val medicalId: Flow<MedicalIdEntity?> = dao.getMedicalId(profileId)
    val allContacts: Flow<List<EmergencyContactEntity>> = dao.getAllContacts(profileId)

    fun saveMedicalId(
        name: String, blood: String, age: String, 
        allergies: String, chronic: String, meds: String,
        weight: String, height: String, imageUri: String? = null
    ) {
        viewModelScope.launch {
            dao.saveMedicalId(
                MedicalIdEntity(
                    profileId = profileId,
                    fullName = name,
                    bloodGroup = blood,
                    age = age,
                    allergies = allergies,
                    chronicConditions = chronic,
                    currentMedications = meds,
                    weight = weight,
                    height = height,
                    imageUri = imageUri
                )
            )
        }
    }

    fun addContact(name: String, phone: String, relation: String) {
        viewModelScope.launch {
            dao.insertContact(EmergencyContactEntity(profileId = profileId, name = name, phone = phone, relation = relation))
        }
    }

    fun deleteContact(contact: EmergencyContactEntity) {
        viewModelScope.launch {
            dao.deleteContact(contact)
        }
    }

    fun deleteMedicalId() {
        viewModelScope.launch {
            dao.deleteMedicalIdByProfile(profileId)
            dao.deleteContactsByProfile(profileId)
        }
    }
}
