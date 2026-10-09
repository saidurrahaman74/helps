package com.srcreation.smarthelp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*

data class PatientInfo(val patientId: String, val name: String)

data class VitalItem(
    val type: String,
    val valueText: String,
    val unit: String,
    val timestamp: Long
)

data class VitalsSnapshot(
    val bp: VitalItem? = null,
    val pulse: VitalItem? = null,
    val spo2: VitalItem? = null,
    val insulin: VitalItem? = null,
    val temp: VitalItem? = null
)

data class DoseSlotSnapshot(
    val slot: String = "",
    val taken: Boolean = false,
    val takenAt: Long = 0L,
    val status: String = "Due"
)

data class MedicineSnapshot(
    val id: String = "",
    val name: String = "",
    val dose: String = "",
    val time: String = "",
    val slots: List<DoseSlotSnapshot> = emptyList(),
    val taken: Boolean = false,
    val takenAt: Long = 0L
)

class CaregiverDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _linkedPatients = MutableStateFlow<List<PatientInfo>>(emptyList())
    val linkedPatients: StateFlow<List<PatientInfo>> = _linkedPatients

    private val _selectedPatient = MutableStateFlow<PatientInfo?>(null)
    val selectedPatient: StateFlow<PatientInfo?> = _selectedPatient

    private val _vitalsState = MutableStateFlow(VitalsSnapshot())
    val vitalsState: StateFlow<VitalsSnapshot> = _vitalsState

    private val _medicinesState = MutableStateFlow<List<MedicineSnapshot>>(emptyList())
    val medicinesState: StateFlow<List<MedicineSnapshot>> = _medicinesState

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> = _isEmpty

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var linksListener: ListenerRegistration? = null
    private var vitalsListener: ListenerRegistration? = null
    private var medicinesListener: ListenerRegistration? = null

    init {
        listenToLinkedPatients()
    }

    fun listenToLinkedPatients() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            Log.e("Caregiver", "auth.currentUser is null in CaregiverDashboardViewModel")
            _isLoading.value = false
            _isEmpty.value = true
            _errorMessage.value = "User session expired. Please log in again."
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        linksListener?.remove()
        try {
            linksListener = firestore.collection("links")
                .whereEqualTo("caregiverId", uid)
                .whereEqualTo("status", "active")
                .addSnapshotListener { snapshot, error ->
                    _isLoading.value = false
                    if (error != null) {
                        Log.e("Caregiver", "Error listening to caregiver links", error)
                        val msg = if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            "Permission denied: You do not have access to view these caregiver links."
                        } else {
                            error.localizedMessage ?: "Failed to sync caregiver links."
                        }
                        _errorMessage.value = msg
                        _linkedPatients.value = emptyList()
                        _selectedPatient.value = null
                        _isEmpty.value = true
                        clearPatientListeners()
                        return@addSnapshotListener
                    }

                    if (snapshot == null || snapshot.isEmpty) {
                        _linkedPatients.value = emptyList()
                        _selectedPatient.value = null
                        _isEmpty.value = true
                        clearPatientListeners()
                        return@addSnapshotListener
                    }

                    try {
                        val docs = snapshot.documents
                        val validDocs = docs.filter { !it.getString("patientId").isNullOrBlank() }
                        if (validDocs.isEmpty()) {
                            _linkedPatients.value = emptyList()
                            _selectedPatient.value = null
                            _isEmpty.value = true
                            clearPatientListeners()
                            return@addSnapshotListener
                        }

                        val list = mutableListOf<PatientInfo>()
                        var loadedCount = 0

                        for (doc in validDocs) {
                            val pid = doc.getString("patientId") ?: continue
                            firestore.collection("users").document(pid).get()
                                .addOnSuccessListener { userDoc ->
                                    val name = userDoc?.getString("name") ?: "Patient (${pid.take(5)})"
                                    list.add(PatientInfo(pid, name))
                                    loadedCount++
                                    if (loadedCount == validDocs.size) {
                                        _linkedPatients.value = list.toList()
                                        _isEmpty.value = list.isEmpty()
                                        if (_selectedPatient.value == null && list.isNotEmpty()) {
                                            selectPatient(list.first())
                                        }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Caregiver", "Failed to fetch user doc for patient $pid", e)
                                    list.add(PatientInfo(pid, "Patient (${pid.take(5)})"))
                                    loadedCount++
                                    if (loadedCount == validDocs.size) {
                                        _linkedPatients.value = list.toList()
                                        _isEmpty.value = list.isEmpty()
                                        if (_selectedPatient.value == null && list.isNotEmpty()) {
                                            selectPatient(list.first())
                                        }
                                    }
                                }
                        }
                    } catch (e: Exception) {
                        Log.e("Caregiver", "Error parsing caregiver links snapshot", e)
                        _errorMessage.value = "Error parsing caregiver data."
                    }
                }
        } catch (e: Exception) {
            Log.e("Caregiver", "Failed to register links listener", e)
            _isLoading.value = false
            _isEmpty.value = true
            _errorMessage.value = e.localizedMessage ?: "Failed to initialize caregiver data."
        }
    }

    fun selectPatient(patient: PatientInfo) {
        _selectedPatient.value = patient
        attachPatientListeners(patient.patientId)
    }

    private fun attachPatientListeners(patientId: String) {
        clearPatientListeners()

        try {
            // 1. Real-time Vitals Listener
            vitalsListener = firestore.collection("patients").document(patientId)
                .collection("vitals")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("Caregiver", "Error listening to vitals for $patientId", error)
                        if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            _errorMessage.value = "Permission denied: Cannot access patient vitals."
                        }
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

                    try {
                        var latestBp: VitalItem? = null
                        var latestPulse: VitalItem? = null
                        var latestSpO2: VitalItem? = null
                        var latestInsulin: VitalItem? = null
                        var latestTemp: VitalItem? = null

                        for (doc in snapshot.documents) {
                            val type = doc.getString("type") ?: ""
                            val ts = when (val t = doc.get("timestamp")) {
                                is Number -> t.toLong()
                                is String -> t.toLongOrNull() ?: 0L
                                else -> 0L
                            }
                            val val1 = doc.get("value")
                            val val2 = doc.get("value2")
                            val unit = doc.getString("unit") ?: ""

                            when (type) {
                                "BP" -> {
                                    if (ts >= (latestBp?.timestamp ?: 0L)) {
                                        val text = if (val2 != null && val2.toString() != "0") "$val1/$val2 $unit" else "$val1 $unit"
                                        latestBp = VitalItem("BP", text, unit, ts)
                                    }
                                }
                                "PULSE" -> {
                                    if (ts >= (latestPulse?.timestamp ?: 0L)) {
                                        latestPulse = VitalItem("PULSE", "$val1 $unit", unit, ts)
                                    }
                                }
                                "SPO2" -> {
                                    if (ts >= (latestSpO2?.timestamp ?: 0L)) {
                                        latestSpO2 = VitalItem("SPO2", "$val1 $unit", unit, ts)
                                    }
                                }
                                "GLUCOSE" -> {
                                    if (ts >= (latestInsulin?.timestamp ?: 0L)) {
                                        latestInsulin = VitalItem("GLUCOSE", "$val1 $unit", unit, ts)
                                    }
                                }
                                "TEMPERATURE" -> {
                                    if (ts >= (latestTemp?.timestamp ?: 0L)) {
                                        latestTemp = VitalItem("TEMPERATURE", "$val1 $unit", unit, ts)
                                    }
                                }
                            }
                        }

                        _vitalsState.value = VitalsSnapshot(
                            bp = latestBp,
                            pulse = latestPulse,
                            spo2 = latestSpO2,
                            insulin = latestInsulin,
                            temp = latestTemp
                        )
                    } catch (e: Exception) {
                        Log.e("Caregiver", "Error parsing vitals snapshot for $patientId", e)
                    }
                }

            // 2. Fetch patient timezone first, then attach medicines listener
            firestore.collection("users").document(patientId).get()
                .addOnSuccessListener { userDoc ->
                    val timeZoneId = userDoc?.getString("timeZone")?.takeIf { it.isNotBlank() } ?: TimeZone.getDefault().id
                    val tz = TimeZone.getTimeZone(timeZoneId)

                    val calendar = Calendar.getInstance(tz)
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    if (hour in 0..4) {
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                    }
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { this.timeZone = tz }
                    val todayDate = dateFormat.format(calendar.time)

                    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = calendar.get(Calendar.MINUTE)
                    val currentTimeInMinutes = currentHour * 60 + currentMinute

                    medicinesListener = firestore.collection("patients").document(patientId)
                        .collection("medicines")
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e("Caregiver", "Error listening to medicines for $patientId", error)
                                if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                    _errorMessage.value = "Permission denied: Cannot access patient medicines."
                                }
                                return@addSnapshotListener
                            }
                            if (snapshot == null) return@addSnapshotListener

                            try {
                                val medList = mutableListOf<MedicineSnapshot>()
                                val docs = snapshot.documents
                                if (docs.isEmpty()) {
                                    _medicinesState.value = emptyList()
                                    return@addSnapshotListener
                                }

                                var loadedMedsCount = 0
                                for (doc in docs) {
                                    val medId = doc.id
                                    val name = doc.getString("name") ?: ""
                                    val dose = doc.getString("dose") ?: ""
                                    val timeStr = doc.getString("time") ?: ""
                                    val slotNames = timeStr.split(",").map { it.trim() }.filter { it.isNotBlank() }

                                    firestore.collection("patients").document(patientId)
                                        .collection("medicines").document(medId)
                                        .collection("doses")
                                        .whereEqualTo("date", todayDate)
                                        .get()
                                        .addOnSuccessListener { doseSnap ->
                                            val doseMap = mutableMapOf<String, Pair<Boolean, Long>>()
                                            for (dDoc in doseSnap.documents) {
                                                val slot = dDoc.getString("slot") ?: ""
                                                val taken = dDoc.getBoolean("taken") ?: false
                                                val takenAt = dDoc.getLong("takenAt") ?: 0L
                                                if (slot.isNotBlank()) {
                                                    doseMap[slot] = Pair(taken, takenAt)
                                                }
                                            }

                                            val slotsSnapshots = slotNames.map { slotName ->
                                                val pair = doseMap[slotName]
                                                val isTaken = pair?.first ?: false
                                                val takenAt = pair?.second ?: 0L

                                                val scheduledHour = when (slotName.lowercase()) {
                                                    "morning" -> 8
                                                    "noon" -> 14
                                                    "evening" -> 18
                                                    "night" -> 22
                                                    else -> 12
                                                }
                                                val scheduledMinutes = scheduledHour * 60
                                                val status = when {
                                                    isTaken -> "Taken"
                                                    currentTimeInMinutes > scheduledMinutes + 120 -> "Missed"
                                                    else -> "Due"
                                                }

                                                DoseSlotSnapshot(
                                                    slot = slotName,
                                                    taken = isTaken,
                                                    takenAt = takenAt,
                                                    status = status
                                                )
                                            }

                                            val overallTaken = slotsSnapshots.isNotEmpty() && slotsSnapshots.all { it.taken }
                                            val maxTakenAt = slotsSnapshots.maxOfOrNull { it.takenAt } ?: 0L

                                            medList.add(
                                                MedicineSnapshot(
                                                    id = medId,
                                                    name = name,
                                                    dose = dose,
                                                    time = timeStr,
                                                    slots = slotsSnapshots,
                                                    taken = overallTaken,
                                                    takenAt = maxTakenAt
                                                )
                                            )
                                            loadedMedsCount++
                                            if (loadedMedsCount == docs.size) {
                                                _medicinesState.value = medList.sortedBy { it.name }
                                            }
                                        }
                                        .addOnFailureListener {
                                            val slotsSnapshots = slotNames.map { slotName ->
                                                val scheduledHour = when (slotName.lowercase()) {
                                                    "morning" -> 8
                                                    "noon" -> 14
                                                    "evening" -> 18
                                                    "night" -> 22
                                                    else -> 12
                                                }
                                                val scheduledMinutes = scheduledHour * 60
                                                val status = if (currentTimeInMinutes > scheduledMinutes + 120) "Missed" else "Due"

                                                DoseSlotSnapshot(slot = slotName, taken = false, takenAt = 0L, status = status)
                                            }

                                            medList.add(
                                                MedicineSnapshot(
                                                    id = medId,
                                                    name = name,
                                                    dose = dose,
                                                    time = timeStr,
                                                    slots = slotsSnapshots,
                                                    taken = false,
                                                    takenAt = 0L
                                                )
                                            )
                                            loadedMedsCount++
                                            if (loadedMedsCount == docs.size) {
                                                _medicinesState.value = medList.sortedBy { it.name }
                                            }
                                        }
                                }
                            } catch (e: Exception) {
                                Log.e("Caregiver", "Error parsing medicines snapshot for $patientId", e)
                            }
                        }
                }
                .addOnFailureListener {
                    // Fallback to default timezone if user doc fetch fails
                    // ...
                }
        } catch (e: Exception) {
            Log.e("Caregiver", "Failed to attach patient listeners for $patientId", e)
        }
    }

    private fun clearPatientListeners() {
        vitalsListener?.remove()
        vitalsListener = null
        medicinesListener?.remove()
        medicinesListener = null
    }

    override fun onCleared() {
        super.onCleared()
        linksListener?.remove()
        clearPatientListeners()
    }

        fun formatTime(timestamp: Long): String {
        if (timestamp <= 0L) return "N/A"
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
