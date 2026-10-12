package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.MedicineEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class MedicineViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val medicineDao = db.medicineDao()
    private val pref = PreferenceManager(application)
    
    private val initialProfileId = savedStateHandle.get<Int>("PROFILE_ID") ?: pref.getActiveProfile()
    private val _currentProfileId = MutableStateFlow(initialProfileId)
    val currentProfileId: StateFlow<Int> = _currentProfileId

    @OptIn(ExperimentalCoroutinesApi::class)
    val allMedicines: Flow<List<MedicineEntity>> = _currentProfileId.flatMapLatest { pid ->
        medicineDao.getAllMedicines(pid)
    }

    fun setProfileId(pid: Int) {
        _currentProfileId.value = pid
    }

    val activeProfileId: Int
        get() = _currentProfileId.value

    fun addMedicine(
        name: String,
        dosageValue: String,
        dosageUnit: String,
        category: String,
        foodRelation: String,
        selectedTimes: List<String>,
        customTime: String?,
        durationDays: Int,
        isContinuous: Boolean,
        stock: Int
    ) {
        viewModelScope.launch {
            val pid = activeProfileId
            val med = MedicineEntity(
                profileId = pid,
                name = name,
                dosageValue = dosageValue,
                dosageUnit = dosageUnit,
                category = category,
                foodRelation = foodRelation,
                selectedTimes = selectedTimes.joinToString(","),
                customTime = customTime,
                durationDays = durationDays,
                isContinuous = isContinuous,
                initialStock = stock,
                currentStock = stock
            )
            medicineDao.insertMedicine(med)
            MedicineAlarmScheduler.scheduleAlarms(getApplication(), med)
            MedicineWidgetProvider.updateAllWidgets(getApplication())

            val syncRepo = SyncRepository(getApplication())
            syncRepo.syncAllUnsynced()
            syncRepo.triggerSync()
        }
    }

    fun updateMedicine(medicine: MedicineEntity) {
        viewModelScope.launch {
            val updated = medicine.copy(synced = false)
            medicineDao.updateMedicine(updated)
            MedicineWidgetProvider.updateAllWidgets(getApplication())
            val syncRepo = SyncRepository(getApplication())
            syncRepo.syncAllUnsynced()
            syncRepo.triggerSync()
        }
    }

    fun insertDose(dose: com.srcreation.smarthelp.db.MedicineDoseEntity) {
        viewModelScope.launch {
            db.medicineDoseDao().insertDose(dose)
            val syncRepo = SyncRepository(getApplication())
            syncRepo.syncAllUnsynced()
            syncRepo.triggerSync()
        }
    }
    
    fun deleteMedicine(medicine: MedicineEntity) {
        viewModelScope.launch {
            DeletionSync.deleteMedicine(getApplication(), medicine)
            MedicineAlarmScheduler.cancelAlarms(getApplication(), medicine)
            MedicineWidgetProvider.updateAllWidgets(getApplication())
        }
    }
}
