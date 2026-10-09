package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.VaccineEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class VaccineViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val vaccineDao = db.vaccineDao()
    private val pref = PreferenceManager(application)

    val allVaccines: Flow<List<VaccineEntity>> = vaccineDao.getAllVaccines(pref.getActiveProfile())

    fun addVaccine(name: String, doseInfo: String, date: String, isTaken: Boolean) {
        viewModelScope.launch {
            vaccineDao.insertVaccine(
                VaccineEntity(
                    profileId = pref.getActiveProfile(),
                    name = name,
                    doseInfo = doseInfo,
                    date = date,
                    isTaken = isTaken
                )
            )
        }
    }

    fun updateVaccine(vaccine: VaccineEntity) {
        viewModelScope.launch {
            vaccineDao.updateVaccine(vaccine)
        }
    }
}
