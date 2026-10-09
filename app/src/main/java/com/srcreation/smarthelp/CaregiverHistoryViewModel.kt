package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
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
    val timestamp: Long = 0L
)

class CaregiverHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()

    private val _patientId = MutableStateFlow("")
    val patientId: StateFlow<String> = _patientId

    private val _patientName = MutableStateFlow("Patient")
    val patientName: StateFlow<String> = _patientName

    private val _selectedVital = MutableStateFlow("BP")
    val selectedVital: StateFlow<String> = _selectedVital

    // Default: Last 7 days
    private val defaultStart = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.timeInMillis
    private val defaultEnd = System.currentTimeMillis()

    private val _startDate = MutableStateFlow(defaultStart)
    val startDate: StateFlow<Long> = _startDate

    private val _endDate = MutableStateFlow(defaultEnd)
    val endDate: StateFlow<Long> = _endDate

    private val _vitalsHistory = MutableStateFlow<List<VitalHistoryRecord>>(emptyList())
    val vitalsHistory: StateFlow<List<VitalHistoryRecord>> = _vitalsHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun initPatient(pid: String, name: String) {
        _patientId.value = pid
        if (name.isNotBlank()) _patientName.value = name
        loadHistory()
    }

    fun setVitalType(type: String) {
        _selectedVital.value = type
        loadHistory()
    }

    fun setDateRange(start: Long, end: Long) {
        _startDate.value = start
        _endDate.value = end
        loadHistory()
    }

    fun setPresetRange(days: Int) {
        val end = System.currentTimeMillis()
        val start = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -days) }.timeInMillis
        setDateRange(start, end)
    }

    fun loadHistory() {
        val pid = _patientId.value
        if (pid.isBlank()) return

        val type = _selectedVital.value
        val start = _startDate.value
        val end = _endDate.value

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("patients").document(pid)
                    .collection("vitals")
                    .get()
                    .await()

                val records = mutableListOf<VitalHistoryRecord>()
                for (doc in snapshot.documents) {
                    val docType = doc.getString("type") ?: ""
                    val ts = when (val t = doc.get("timestamp")) {
                        is Number -> t.toLong()
                        is String -> t.toLongOrNull() ?: 0L
                        else -> 0L
                    }

                    if (docType.equals(type, ignoreCase = true) && ts in start..end) {
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

                        records.add(
                            VitalHistoryRecord(
                                id = doc.id,
                                type = docType,
                                value = val1,
                                value2 = val2,
                                unit = unit,
                                timestamp = ts
                            )
                        )
                    }
                }

                // Sort by timestamp ascending for chart
                _vitalsHistory.value = records.sortedBy { it.timestamp }
            } catch (e: Exception) {
                e.printStackTrace()
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
