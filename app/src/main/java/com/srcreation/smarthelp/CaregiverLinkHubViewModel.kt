package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class HubSelectedPatient(
    val id: String,
    val name: String,
    val status: String = "active",
    val linkId: String = ""
)

class CaregiverLinkHubViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // 0 = Link Patient, 1 = Dashboard, 2 = Send Instruction, 3 = History & Charts
    private val _selectedTab = MutableStateFlow(1)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isLoadingInitial = MutableStateFlow(true)
    val isLoadingInitial: StateFlow<Boolean> = _isLoadingInitial.asStateFlow()

    private val _linkedPatients = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val linkedPatients: StateFlow<List<Map<String, Any>>> = _linkedPatients.asStateFlow()

    private val _hasPendingLinks = MutableStateFlow(false)
    val hasPendingLinks: StateFlow<Boolean> = _hasPendingLinks.asStateFlow()

    private val _selectedPatient = MutableStateFlow<HubSelectedPatient?>(null)
    val selectedPatient: StateFlow<HubSelectedPatient?> = _selectedPatient.asStateFlow()

    private var isTabInitialized = false

    fun selectTab(index: Int) {
        _selectedTab.value = parseTabValue(index)
    }

    fun parseTabExtra(extra: Any?, initialPatientId: String?) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val query = firestore.collection("links")
                    .whereEqualTo("caregiverId", uid)
                val linksSnap = query.get().await()

                val docs = linksSnap?.documents ?: emptyList()
                val activeList = mutableListOf<Map<String, Any>>()
                var pendingFound = false

                for (doc in docs) {
                    val status = doc.getString("status") ?: "active"
                    val patientId = doc.getString("patientId") ?: continue
                    val patientName = doc.getString("patientName") ?: "Patient"

                    if (status == "active") {
                        activeList.add(mapOf("patientId" to patientId, "patientName" to patientName, "linkId" to doc.id, "status" to status))
                    } else if (status == "pending") {
                        pendingFound = true
                    }
                }

                _linkedPatients.value = activeList
                _hasPendingLinks.value = pendingFound

                // Set selected patient (validate initialPatientId against active links)
                if (activeList.isNotEmpty()) {
                    val validTarget = activeList.firstOrNull { it["patientId"] == initialPatientId }
                        ?: _selectedPatient.value?.let { current ->
                            activeList.firstOrNull { it["patientId"] == current.id }
                        }
                        ?: activeList.first()
                    _selectedPatient.value = HubSelectedPatient(
                        id = validTarget["patientId"] as String,
                        name = validTarget["patientName"] as String,
                        status = (validTarget["status"] as? String) ?: "active",
                        linkId = (validTarget["linkId"] as? String) ?: ""
                    )
                } else {
                    _selectedPatient.value = null
                }

                // Determine default tab if not explicitly requested or if initial load
                val defaultTab = when {
                    activeList.isNotEmpty() -> 1 // Dashboard
                    pendingFound -> 1 // Dashboard
                    else -> 0 // Link Patient
                }

                val explicitTab = parseTabValue(extra)
                if (extra != null) {
                    _selectedTab.value = explicitTab
                    isTabInitialized = true
                } else if (!isTabInitialized) {
                    _selectedTab.value = defaultTab
                    isTabInitialized = true
                }
            } catch (_: Exception) {
                if (extra != null) {
                    _selectedTab.value = parseTabValue(extra)
                    isTabInitialized = true
                }
            } finally {
                _isLoadingInitial.value = false
            }
        }
    }

    fun setSelectedPatient(patient: HubSelectedPatient?) {
        _selectedPatient.value = patient
    }

    fun detachAllTabListeners() {
        // Tab listeners cleanup
    }

    companion object {
        fun parseTabValue(tabExtra: Any?): Int {
            return when (tabExtra) {
                is Int -> if (tabExtra in 0..3) tabExtra else 1
                is String -> when (tabExtra.uppercase()) {
                    "LINK_PATIENT", "LINK", "0" -> 0
                    "DASHBOARD", "1" -> 1
                    "SEND_INSTRUCTION", "INSTRUCTION", "2" -> 2
                    "HISTORY_CHARTS", "HISTORY", "3" -> 3
                    else -> 1
                }
                else -> 1
            }
        }
    }
}
