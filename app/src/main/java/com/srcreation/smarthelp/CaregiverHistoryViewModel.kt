package com.srcreation.smarthelp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class VitalHistoryRecord(
    val id: String = "",
    val type: String = "",
    val value: Double = 0.0,
    val value2: Double = 0.0,
    val unit: String = "",
    val timestamp: Long = 0L,
    val kind: String = ""
)

data class DoseHistoryRecord(
    val medicineId: String = "",
    val slot: String = "",
    val date: String = "",
    val taken: Boolean = false,
    val takenAt: Long = 0L
)

class CaregiverHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()

    private val _patientId = MutableStateFlow("")
    val patientId: StateFlow<String> = _patientId

    private val _patientName = MutableStateFlow("Patient")
    val patientName: StateFlow<String> = _patientName

    private val _selectedVital = MutableStateFlow("BP")
    val selectedVital: StateFlow<String> = _selectedVital

    // Default: Last 30 days (support for 7, 30, 90)
    private var selectedDays = 30
    private val defaultStart = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -selectedDays) }.timeInMillis
    private val defaultEnd = System.currentTimeMillis()

    private val _startDate = MutableStateFlow(defaultStart)
    val startDate: StateFlow<Long> = _startDate

    private val _endDate = MutableStateFlow(defaultEnd)
    val endDate: StateFlow<Long> = _endDate

    private val _vitalsHistory = MutableStateFlow<List<VitalHistoryRecord>>(emptyList())
    val vitalsHistory: StateFlow<List<VitalHistoryRecord>> = _vitalsHistory

    private val _dosesHistory = MutableStateFlow<List<DoseHistoryRecord>>(emptyList())
    val dosesHistory: StateFlow<List<DoseHistoryRecord>> = _dosesHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Session cache to prevent refetching on tab switch
    private var cachedPatientId: String = ""
    private var cachedVitalType: String = ""
    private var cachedDays: Int = -1
    private var isHubCacheValid = false

    fun initPatient(pid: String, name: String, forceRefresh: Boolean = false) {
        if (_patientId.value == pid && name.isNotBlank()) {
            _patientName.value = name
        } else {
            _patientId.value = pid
            if (name.isNotBlank()) _patientName.value = name
            isHubCacheValid = false
        }

        if (forceRefresh || !isHubCacheValid || cachedPatientId != pid || cachedVitalType != _selectedVital.value || cachedDays != selectedDays) {
            loadHistory()
        }
    }

    fun setVitalType(type: String) {
        if (_selectedVital.value == type && isHubCacheValid) return
        _selectedVital.value = type
        loadHistory()
    }

    fun setDateRange(start: Long, end: Long) {
        _startDate.value = start
        _endDate.value = end
        loadHistory()
    }

    fun setPresetRange(days: Int) {
        val clampedDays = days.coerceIn(7, 90)
        if (selectedDays == clampedDays && isHubCacheValid) return
        selectedDays = clampedDays
        val end = System.currentTimeMillis()
        val start = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -selectedDays) }.timeInMillis
        setDateRange(start, end)
    }

    fun refreshHistory() {
        isHubCacheValid = false
        loadHistory()
    }

    fun loadHistory() {
        val pid = _patientId.value
        if (pid.isBlank()) return

        val type = _selectedVital.value
        val sinceTimestamp = System.currentTimeMillis() - selectedDays * 24L * 3600 * 1000
        
        // Format sinceDate as yyyy-MM-dd for doses query
        val cal = Calendar.getInstance().apply { timeInMillis = sinceTimestamp }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sinceDateStr = dateFormat.format(cal.time)

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // 1. Bounded vitals query per vital type
                val snapshot = firestore.collection("patients").document(pid).collection("vitals")
                    .whereEqualTo("type", type)
                    .whereGreaterThanOrEqualTo("timestamp", sinceTimestamp)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(200)
                    .get()
                    .await()

                val records = mutableListOf<VitalHistoryRecord>()
                for (doc in snapshot.documents) {
                    val docType = doc.getString("type") ?: type
                    val kind = doc.getString("kind") ?: ""
                    val ts = when (val t = doc.get("timestamp")) {
                        is Number -> t.toLong()
                        is String -> t.toLongOrNull() ?: 0L
                        else -> 0L
                    }

                    // Insulin chart rule: ignore INSULIN_DOSE for glucose, ignore zero/negative/impossible values
                    if (type.equals("GLUCOSE", ignoreCase = true) && kind.equals("INSULIN_DOSE", ignoreCase = true)) {
                        continue
                    }

                    val val1 = when (val v = doc.get("value")) {
                        is Number -> v.toDouble()
                        is String -> v.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                    val val2 = when (val v = doc.get("value2")) {
                        is Number -> v.toDouble()
                        is String -> v.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                    val unit = doc.getString("unit") ?: ""

                    // Ignore zero, negative and impossible values
                    if (val1 <= 0.0 && val2 <= 0.0) continue
                    if (type.equals("BP", ignoreCase = true) && (val1 < 50 || val1 > 250 || val2 < 30 || val2 > 150)) continue
                    if (type.equals("PULSE", ignoreCase = true) && (val1 < 30 || val1 > 220)) continue
                    if (type.equals("SPO2", ignoreCase = true) && (val1 < 50 || val1 > 100)) continue
                    if (type.equals("GLUCOSE", ignoreCase = true) && (val1 < 20 || val1 > 600)) continue
                    if (type.equals("TEMPERATURE", ignoreCase = true) && (val1 < 90 || val1 > 110)) continue

                    records.add(
                        VitalHistoryRecord(
                            id = doc.id,
                            type = docType,
                            value = val1,
                            value2 = val2,
                            unit = unit,
                            timestamp = ts,
                            kind = kind
                        )
                    )
                }

                // 2. Bounded doses query using "date" string field (patients/{pid}/medicines/{id}/doses)
                val allDoses = mutableListOf<DoseHistoryRecord>()
                try {
                    val medsSnap = firestore.collection("patients").document(pid).collection("medicines").get().await()
                    for (medDoc in medsSnap.documents) {
                        val medId = medDoc.id
                        val dosesSnap = firestore.collection("patients").document(pid)
                            .collection("medicines").document(medId)
                            .collection("doses")
                            .whereGreaterThanOrEqualTo("date", sinceDateStr)
                            .orderBy("date", Query.Direction.DESCENDING)
                            .limit(200)
                            .get()
                            .await()

                        for (dDoc in dosesSnap.documents) {
                            val slot = dDoc.getString("slot") ?: ""
                            val dateStr = dDoc.getString("date") ?: ""
                            val taken = dDoc.getBoolean("taken") ?: false
                            val takenAt = dDoc.getLong("takenAt") ?: 0L
                            allDoses.add(DoseHistoryRecord(medId, slot, dateStr, taken, takenAt))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("History", "Error fetching doses history: ${e.localizedMessage}", e)
                }

                _vitalsHistory.value = records.sortedBy { it.timestamp }
                _dosesHistory.value = allDoses.sortedByDescending { it.date }

                cachedPatientId = pid
                cachedVitalType = type
                cachedDays = selectedDays
                isHubCacheValid = true
            } catch (e: Exception) {
                Log.e("History", "Query failed: ${e.localizedMessage}", e)
                _errorMessage.value = e.localizedMessage ?: "Query failed"
                _vitalsHistory.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0L) return "N/A"
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        if (timestamp <= 0L) return "N/A"
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
