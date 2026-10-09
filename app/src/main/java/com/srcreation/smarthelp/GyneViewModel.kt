package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.GyneProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class GyneViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.gyneDao()
    private val pref = PreferenceManager(application)

    val gyneProfile: Flow<GyneProfileEntity?> = dao.getGyneProfile(pref.getActiveProfile())

    fun saveGyneProfile(
        maritalStatus: String,
        gravity: String,
        parity: String,
        abortions: String,
        livingChildren: String,
        lmpDate: String,
        contraceptive: String,
        history: String,
        cycleLength: String,
        lastCheckup: String,
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            dao.saveGyneProfile(
                GyneProfileEntity(
                    profileId = pref.getActiveProfile(),
                    maritalStatus = maritalStatus,
                    gravity = gravity,
                    parity = parity,
                    abortions = abortions,
                    livingChildren = livingChildren,
                    lmpDate = lmpDate,
                    contraceptiveHistory = contraceptive,
                    gynecologicalHistory = history,
                    menstrualCycleLength = cycleLength,
                    lastCheckupDate = lastCheckup,
                    imageUri = imageUri
                )
            )
        }
    }

    fun deleteGyneProfile() {
        viewModelScope.launch {
            dao.deleteByProfile(pref.getActiveProfile())
        }
    }
}
