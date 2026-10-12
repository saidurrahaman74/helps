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

class PatientLinkHubViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val pairingRepo = PairingRepository(application)
    private val instructionsRepo = InstructionsRepository()

    // Tab Index: 0 = LINK, 1 = PERMISSIONS, 2 = INSTRUCTIONS
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Pairing State
    private val _pairingCode = MutableStateFlow("")
    val pairingCode: StateFlow<String> = _pairingCode.asStateFlow()

    private val _codeGenerated = MutableStateFlow(false)
    val codeGenerated: StateFlow<Boolean> = _codeGenerated.asStateFlow()

    private val _isGeneratingCode = MutableStateFlow(false)
    val isGeneratingCode: StateFlow<Boolean> = _isGeneratingCode.asStateFlow()

    // Permissions State (Tab 2)
    private val _pendingRequests = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val pendingRequests: StateFlow<List<Map<String, Any>>> = _pendingRequests.asStateFlow()

    private val _activeCaregivers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val activeCaregivers: StateFlow<List<Map<String, Any>>> = _activeCaregivers.asStateFlow()

    // Instructions State (Tab 3)
    private val _instructions = MutableStateFlow<List<InstructionItem>>(emptyList())
    val instructions: StateFlow<List<InstructionItem>> = _instructions.asStateFlow()

    // Badge Counts
    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    private val _unreadInstructionsCount = MutableStateFlow(0)
    val unreadInstructionsCount: StateFlow<Int> = _unreadInstructionsCount.asStateFlow()

    // Listener Registrations
    private var pendingListener: ListenerRegistration? = null
    private var activeListener: ListenerRegistration? = null
    private var instructionsListener: ListenerRegistration? = null

    fun selectTab(index: Int) {
        _selectedTab.value = parseTabValue(index)
        updateActiveTabListeners()
    }

    fun parseTabExtra(extra: Any?): Int {
        val parsed = parseTabValue(extra)
        _selectedTab.value = parsed
        return parsed
    }

    fun generatePairingCode(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        if (_isGeneratingCode.value) return
        _isGeneratingCode.value = true
        viewModelScope.launch {
            try {
                val code = pairingRepo.createPairingCode()
                _pairingCode.value = code
                _codeGenerated.value = true
                onSuccess(code)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to generate pairing code")
            } finally {
                _isGeneratingCode.value = false
            }
        }
    }

    fun refreshBadgeCounts() {
        val uid = auth.currentUser?.uid ?: return

        // Lightweight pending requests count
        firestore.collection("links")
            .whereEqualTo("patientId", uid)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snapshot ->
                _pendingCount.value = snapshot.size()
            }
            .addOnFailureListener {
                // Graceful fallback for permission/offline issues
            }

        // Lightweight unread instructions count
        firestore.collection("patients")
            .document(uid)
            .collection("instructions")
            .whereEqualTo("seen", false)
            .get()
            .addOnSuccessListener { snapshot ->
                _unreadInstructionsCount.value = snapshot.size()
            }
            .addOnFailureListener {
                // Graceful fallback for permission/offline issues
            }
    }

    private fun updateActiveTabListeners() {
        val uid = auth.currentUser?.uid ?: return
        detachAllTabListeners()

        when (_selectedTab.value) {
            1 -> { // Tab 2: Who Can See My Data
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
            2 -> { // Tab 3: Received Instructions
                instructionsListener = firestore.collection("patients")
                    .document(uid)
                    .collection("instructions")
                    .orderBy("sentAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null) {
                            val items = snapshot.documents.mapNotNull { doc ->
                                val text = doc.getString("text") ?: return@mapNotNull null
                                val sentAt = doc.getLong("sentAt") ?: 0L
                                val seen = doc.getBoolean("seen") ?: false
                                InstructionItem(id = doc.id, text = text, sentAt = sentAt, seen = seen)
                            }
                            _instructions.value = items
                            _unreadInstructionsCount.value = items.count { !it.seen }
                        }
                    }
            }
        }
    }

    fun markInstructionAsSeen(instructionId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                instructionsRepo.markAsSeen(uid, instructionId)
                refreshBadgeCounts()
            } catch (_: Exception) {}
        }
    }

    fun detachAllTabListeners() {
        pendingListener?.remove()
        pendingListener = null
        activeListener?.remove()
        activeListener = null
        instructionsListener?.remove()
        instructionsListener = null
    }

    override fun onCleared() {
        super.onCleared()
        detachAllTabListeners()
    }

    companion object {
        fun parseTabValue(tabExtra: Any?): Int {
            return when (tabExtra) {
                is Int -> if (tabExtra in 0..2) tabExtra else 0
                is String -> when (tabExtra.uppercase()) {
                    "LINK", "PAIRING", "0" -> 0
                    "PERMISSIONS", "WHO_CAN_SEE", "1" -> 1
                    "INSTRUCTIONS", "2" -> 2
                    else -> 0
                }
                else -> 0
            }
        }
    }
}
