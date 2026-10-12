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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class DailyTask(val title: String, val isCompleted: Boolean, val icon: ImageVector)

class WellnessViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WellnessRepository(application)
    private val db = AppDatabase.getDatabase(application)
    private val pref = PreferenceManager(application)
    private val profileMutex = Mutex()

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

    suspend fun ensureProfile(): ProfileEntity {
        return profileMutex.withLock {
            val profileId = pref.getActiveProfile()
            var profile = db.profileDao().getProfileById(profileId)
            if (profile == null) {
                db.profileDao().insertProfileIgnore(ProfileEntity(id = profileId, name = "Me", relation = "Self"))
                profile = db.profileDao().getProfileById(profileId)

                val uid = AuthRepository().uid()
                if (!uid.isNullOrBlank()) {
                    LocalDataOwnership.claimIfFresh(getApplication(), uid)
                }
            }
            profile ?: ProfileEntity(id = profileId, name = "Me", relation = "Self")
        }
    }

    fun fetchCurrentProfile() {
        viewModelScope.launch {
            if (LocalDataOwnership.isOwnershipBlocked(getApplication())) {
                return@launch
            }
            val profile = ensureProfile()
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
