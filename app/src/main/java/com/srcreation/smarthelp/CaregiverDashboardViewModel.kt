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

data class PatientInfo(
    val patientId: String,
    val name: String,
    val status: String = "active",
    val linkId: String = ""
)

data class VitalItem(
    val type: String,
    val valueText: String,
    val unit: String,
    val timestamp: Long,
    val alertLevel: VitalAlertLevel = VitalAlertLevel.NORMAL,
    val alertText: String = "",
    val detailText: String = "",
    val timeAgoText: String = "",
    val isOlderThan24Hours: Boolean = false,
    val isImpossible: Boolean = false
)

data class VitalsSnapshot(
    val bp: VitalItem? = null,
    val pulse: VitalItem? = null,
    val spo2: VitalItem? = null,
    val insulin: VitalItem? = null,
    val temp: VitalItem? = null,
    val activeDangerAlerts: List<String> = emptyList(),
    val recentAlerts24h: List<String> = emptyList(),
    val lastUpdatedText: String = ""
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

    private val _sessionAlertHistory = MutableStateFlow<List<String>>(emptyList())
    val sessionAlertHistory: StateFlow<List<String>> = _sessionAlertHistory

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
                            val status = doc.getString("status") ?: "active"
                            val linkId = doc.id

                            firestore.collection("users").document(pid).get()
                                .addOnSuccessListener { userDoc ->
                                    val rawName = userDoc?.getString("name") ?: "Patient (${pid.take(5)})"
                                    val name = if (status == "pending") "$rawName (Waiting for approval)" else rawName
                                    list.add(PatientInfo(pid, name, status, linkId))
                                    loadedCount++
                                    if (loadedCount == validDocs.size) {
                                        _linkedPatients.value = list.toList()
                                        _isEmpty.value = list.isEmpty()
                                        if (_selectedPatient.value == null && list.isNotEmpty()) {
                                            selectPatient(list.first())
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    val name = if (status == "pending") "Patient (${pid.take(5)}) (Waiting for approval)" else "Patient (${pid.take(5)})"
                                    list.add(PatientInfo(pid, name, status, linkId))
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
        if (patient.status == "active") {
            attachPatientListeners(patient.patientId)
        } else {
            clearPatientListeners()
            _vitalsState.value = VitalsSnapshot()
            _medicinesState.value = emptyList()
        }
    }

    private fun attachPatientListeners(patientId: String) {
        clearPatientListeners()

        try {
            // 1. Real-time Vitals Listener
            vitalsListener = firestore.collection("patients").document(patientId)
                .collection("vitals")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            Log.w("Caregiver", "Permission denied for vitals on patient $patientId")
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

                        val maxAllowedTs = System.currentTimeMillis() + 5 * 60 * 1000L
                        val twentyFourHoursAgo = System.currentTimeMillis() - 24 * 3600 * 1000L
                        val recent24hDangers = mutableListOf<Pair<Long, String>>()

                        for (doc in snapshot.documents) {
                            val type = doc.getString("type") ?: ""
                            val kind = doc.getString("kind")
                            val ts = when (val t = doc.get("timestamp")) {
                                is Number -> t.toLong()
                                is String -> t.toLongOrNull() ?: 0L
                                else -> 0L
                            }
                            if (ts > maxAllowedTs) continue

                            val val1 = doc.get("value")
                            val val2 = doc.get("value2")
                            val unit = doc.getString("unit") ?: ""

                            val item = createVitalItem(type, val1, val2, unit, ts, kind) ?: continue

                            when (type) {
                                "BP" -> if (ts >= (latestBp?.timestamp ?: 0L)) latestBp = item
                                "PULSE" -> if (ts >= (latestPulse?.timestamp ?: 0L)) latestPulse = item
                                "SPO2" -> if (ts >= (latestSpO2?.timestamp ?: 0L)) latestSpO2 = item
                                "GLUCOSE" -> if (ts >= (latestInsulin?.timestamp ?: 0L)) latestInsulin = item
                                "TEMPERATURE" -> if (ts >= (latestTemp?.timestamp ?: 0L)) latestTemp = item
                            }

                            if (item.alertLevel == VitalAlertLevel.DANGER && ts >= twentyFourHoursAgo) {
                                val dateTimeStr = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(ts))
                                val dangerMsg = "${item.type} ${item.valueText} on $dateTimeStr, ${item.detailText}"
                                recent24hDangers.add(ts to dangerMsg)
                                recordSessionAlert(dangerMsg)
                            }
                        }

                        val activeDangers = mutableListOf<String>()
                        listOfNotNull(latestBp, latestPulse, latestSpO2, latestInsulin, latestTemp).forEach { item ->
                            if (item.alertLevel == VitalAlertLevel.DANGER && item.timestamp >= twentyFourHoursAgo) {
                                val dateTimeStr = SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(item.timestamp))
                                val bannerMsg = "${item.type} ${item.valueText} on $dateTimeStr, ${item.detailText}"
                                activeDangers.add(bannerMsg)
                            }
                        }

                        val sortedRecent24h = recent24hDangers.sortedByDescending { it.first }.map { it.second }.distinct()

                        val maxTs = listOfNotNull(latestBp?.timestamp, latestPulse?.timestamp, latestSpO2?.timestamp, latestInsulin?.timestamp, latestTemp?.timestamp).maxOrNull() ?: 0L
                        val lastUpdatedText = if (maxTs > 0L && maxTs >= twentyFourHoursAgo) {
                            "Last updated: " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(maxTs))
                        } else {
                            "No recent data"
                        }

                        _vitalsState.value = VitalsSnapshot(
                            bp = latestBp,
                            pulse = latestPulse,
                            spo2 = latestSpO2,
                            insulin = latestInsulin,
                            temp = latestTemp,
                            activeDangerAlerts = activeDangers,
                            recentAlerts24h = sortedRecent24h,
                            lastUpdatedText = lastUpdatedText
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
                                if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                    Log.w("Caregiver", "Permission denied for medicines on patient $patientId")
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

                                                val status = MedicineScheduleHelper.calculateDoseStatus(slotName, isTaken, currentTimeInMinutes)

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
                                                    "evening" -> 20
                                                    "night" -> 22
                                                    else -> 12
                                                }
                                                val scheduledMinutes = scheduledHour * 60
                                                val status = MedicineScheduleHelper.calculateDoseStatus(slotName, false, currentTimeInMinutes)

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
                .addOnFailureListener {}
        } catch (e: Exception) {
            Log.e("Caregiver", "Failed to attach patient listeners for $patientId", e)
        }
    }

    private fun createVitalItem(type: String, val1: Any?, val2: Any?, unit: String, ts: Long, kind: String? = null): VitalItem? {
        if (type == "GLUCOSE" && kind == "INSULIN_DOSE") return null

        val val1Double = val1?.toString()?.toDoubleOrNull()
        if (val1Double == null || val1Double <= 0.0) return null

        val (timeAgoText, isOlderThan24) = formatTimeAgo(ts)

        val alertResult = when (type) {
            "BP" -> {
                val sys = val1.toString().toIntOrNull() ?: return null
                val dia = val2.toString().toIntOrNull() ?: return null
                AlertThresholds.checkBp(sys, dia)
            }
            "PULSE" -> {
                val bpm = val1Double.toInt()
                AlertThresholds.checkPulse(bpm)
            }
            "SPO2" -> {
                val spo2 = val1Double
                AlertThresholds.checkSpO2(spo2)
            }
            "GLUCOSE" -> {
                val glucoseVal = val1Double
                AlertThresholds.checkGlucose(glucoseVal, unit)
            }
            "TEMPERATURE" -> {
                val tempVal = val1Double
                AlertThresholds.checkTemperature(tempVal, unit)
            }
            else -> VitalAlertResult(VitalAlertLevel.NORMAL)
        }

        val valueText = when (type) {
            "BP" -> if (val2 != null && val2.toString() != "0") "$val1/$val2 $unit" else "$val1 $unit"
            else -> "$val1 $unit"
        }

        val alertMsgWithDetail = if (alertResult.isImpossible) {
            "Check this value"
        } else if (alertResult.level != VitalAlertLevel.NORMAL && alertResult.detailText.isNotBlank()) {
            "${alertResult.message}, ${alertResult.detailText}"
        } else {
            alertResult.message
        }

        return VitalItem(
            type = type,
            valueText = valueText,
            unit = unit,
            timestamp = ts,
            alertLevel = if (alertResult.isImpossible) VitalAlertLevel.NORMAL else alertResult.level,
            alertText = alertMsgWithDetail,
            detailText = alertResult.detailText,
            timeAgoText = timeAgoText,
            isOlderThan24Hours = isOlderThan24,
            isImpossible = alertResult.isImpossible
        )
    }

    private fun formatTimeAgo(timestamp: Long): Pair<String, Boolean> {
        if (timestamp <= 0L) return Pair("No recent data", true)
        val diffMs = System.currentTimeMillis() - timestamp
        val isOlder24 = diffMs >= 24 * 3600 * 1000L
        if (diffMs < 0) return Pair("taken just now", isOlder24)
        val diffMin = diffMs / (1000 * 60)
        val diffHours = diffMin / 60
        val diffDays = diffHours / 24

        val text = when {
            diffMin < 1 -> "taken just now"
            diffMin < 60 -> "taken $diffMin min ago"
            diffHours < 24 -> "taken $diffHours hrs ago"
            else -> "taken $diffDays days ago"
        }
        return Pair(text, isOlder24)
    }

    private fun recordSessionAlert(message: String) {
        if (message.isBlank()) return
        val list = _sessionAlertHistory.value.toMutableList()
        if (!list.contains(message)) {
            list.add(0, message)
            _sessionAlertHistory.value = list
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

    fun cancelPendingRequest(linkId: String) {
        if (linkId.isBlank()) return
        firestore.collection("links").document(linkId).delete()
    }
}
