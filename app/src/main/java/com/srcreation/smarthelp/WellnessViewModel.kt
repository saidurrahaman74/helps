package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.srcreation.smarthelp.db.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.ui.graphics.vector.ImageVector
import com.srcreation.smarthelp.db.ProfileEntity
import com.srcreation.smarthelp.db.MedicineEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

data class DailyTask(val title: String, val isCompleted: Boolean, val icon: ImageVector)

class WellnessViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WellnessRepository(application)
    private val db = AppDatabase.getDatabase(application)
    private val pref = PreferenceManager(application)

    val wellnessScore: Flow<Int> = repository.wellnessScore
    val weeklyWellnessTrend: Flow<List<Float>> = repository.weeklyWellnessTrend
    val dailyTasks: Flow<List<DailyTask>> = repository.dailyTasks
    val smartInsights: Flow<List<SmartInsight>> = repository.smartInsights
    val nextMedicine: Flow<MedicineEntity?> = repository.nextMedicine

    private val _currentProfile = MutableStateFlow<ProfileEntity?>(null)
    val currentProfile: StateFlow<ProfileEntity?> = _currentProfile

    init {
        fetchCurrentProfile()
    }

    fun fetchCurrentProfile() {
        viewModelScope.launch {
            val profileId = pref.getActiveProfile()
            var profile = db.profileDao().getProfileById(profileId)
            
            if (profile == null) {
                db.profileDao().insertProfile(ProfileEntity(id = 1, name = "Me", relation = "Self"))
                profile = db.profileDao().getProfileById(1)
            }
            _currentProfile.value = profile
        }
    }

    fun markMedicineAsTaken(medicine: MedicineEntity) {
        viewModelScope.launch {
            MedicineRepository(getApplication()).markDoseTaken(medicine.id)
            MedicineWidgetProvider.updateAllWidgets(getApplication())
        }
    }
}
