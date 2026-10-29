package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StepGuideState { STEP1, STEP2, STEP3, DONE }

class PatientLinkHubViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val pairingRepo = PairingRepository(application)

    // Tab Index: 0 = LINK, 1 = PERMISSIONS
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Pairing State
    private val _pairingCode = MutableStateFlow("")
    val pairingCode: StateFlow<String> = _pairingCode.asStateFlow()

    private val _codeGenerated = MutableStateFlow(false)
    val codeGenerated: StateFlow<Boolean> = _codeGenerated.asStateFlow()

    private val _isGeneratingCode = MutableStateFlow(false)
    val isGeneratingCode: StateFlow<Boolean> = _isGeneratingCode.asStateFlow()

    private val _stepState = MutableStateFlow(StepGuideState.STEP1)
    val stepState: StateFlow<StepGuideState> = _stepState.asStateFlow()

    // Permissions State (Tab 1)
    private val _pendingRequests = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val pendingRequests: StateFlow<List<Map<String, Any>>> = _pendingRequests.asStateFlow()

    private val _activeCaregivers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val activeCaregivers: StateFlow<List<Map<String, Any>>> = _activeCaregivers.asStateFlow()

    // Badge Counts
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    private val _unreadInstructionsCount = MutableStateFlow(0)
    val unreadInstructionsCount: StateFlow<Int> = _unreadInstructionsCount.asStateFlow()

    // Listener Registrations
    private var pendingListener: ListenerRegistration? = null
    private var activeListener: ListenerRegistration? = null

    private var isTabInitialized = false

    fun selectTab(index: Int) {
        _selectedTab.value = parseTabValue(index)
        updateActiveTabListeners()
    }

    fun parseTabExtra(extra: Any?): Int {
        if (extra != null) {
            val parsed = parseTabValue(extra)
            _selectedTab.value = parsed
            isTabInitialized = true
        } else if (!isTabInitialized) {
            _selectedTab.value = 0
            isTabInitialized = true
        }
        return _selectedTab.value
    }

    fun checkStatusForCurrentCode(code: String) {
        val uid = auth.currentUser?.uid ?: return
        if (code.isBlank() || !_codeGenerated.value) {
            _stepState.value = StepGuideState.STEP1
            return
        }

        firestore.collection("links")
            .whereEqualTo("patientId", uid)
            .whereEqualTo("code", code)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot == null || snapshot.isEmpty) {
                    _stepState.value = StepGuideState.STEP2
                } else {
                    val doc = snapshot.documents.first()
                    val status = doc.getString("status") ?: "pending"
                    if (status == "active") {
                        _stepState.value = StepGuideState.DONE
                    } else {
                        _stepState.value = StepGuideState.STEP3
                    }
                }
            }
            .addOnFailureListener {
                _stepState.value = StepGuideState.STEP2
            }
    }

    fun generatePairingCode(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        if (_isGeneratingCode.value) return
        _isGeneratingCode.value = true
        viewModelScope.launch {
            try {
                val code = pairingRepo.createPairingCode()
                _pairingCode.value = code
                _codeGenerated.value = true
                _stepState.value = StepGuideState.STEP2
                onSuccess(code)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to generate pairing code")
            } finally {
                _isGeneratingCode.value = false
            }
        }
    }

    fun resetPairingCodeState() {
        _pairingCode.value = ""
        _codeGenerated.value = false
        _stepState.value = StepGuideState.STEP1
    }

    fun refreshBadgeCounts() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("links")
            .whereEqualTo("patientId", uid)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                _pendingCount.value = snapshot.size()
            }
            .addOnFailureListener {}

        firestore.collection("patients")
            .document(uid)
            .collection("instructions")
            .whereEqualTo("seen", false)
            .get()
            .addOnSuccessListener { snapshot ->
                _unreadInstructionsCount.value = snapshot.size()
            }
            .addOnFailureListener {}
    }

    private fun updateActiveTabListeners() {
        val uid = auth.currentUser?.uid ?: return
        detachAllTabListeners()

        if (_selectedTab.value == 1) {
            pendingListener = firestore.collection("links")
                .whereEqualTo("patientId", uid)
                .whereEqualTo("status", "pending")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val list = snapshot.documents.map { doc ->
                            mapOf(
                                "linkId" to doc.id,
                                "name" to (doc.getString("caregiverName") ?: "Caregiver"),
                                "createdAt" to (doc.getLong("createdAt") ?: System.currentTimeMillis())
                            )
                        }
                        _pendingRequests.value = list
                        _pendingCount.value = list.size
                    }
                }

            activeListener = firestore.collection("links")
                .whereEqualTo("patientId", uid)
                .whereEqualTo("status", "active")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        _activeCaregivers.value = snapshot.documents.map { doc ->
                            mapOf(
                                "linkId" to doc.id,
                                "name" to (doc.getString("caregiverName") ?: "Caregiver")
                            )
                        }
                    }
                }
        }
    }

    fun detachAllTabListeners() {
        pendingListener?.remove()
        pendingListener = null
        activeListener?.remove()
        activeListener = null
    }

    override fun onCleared() {
        super.onCleared()
        detachAllTabListeners()
    }

    companion object {
        fun parseTabValue(tabExtra: Any?): Int {
            return when (tabExtra) {
                is Int -> if (tabExtra in 0..1) tabExtra else 0
                is String -> when (tabExtra.uppercase()) {
                    "LINK", "PAIRING", "0" -> 0
                    "PERMISSIONS", "WHO_CAN_SEE", "1" -> 1
                    else -> 0
                }
                else -> 0
            }
        }
    }
}
