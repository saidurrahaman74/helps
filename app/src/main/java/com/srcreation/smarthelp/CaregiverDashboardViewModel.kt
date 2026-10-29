package com.srcreation.smarthelp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class PatientInfo(
    val patientId: String,
    val name: String,
    val status: String,
    val linkId: String
)

data class VitalItem(
    val type: String,
    val val1Text: String,
    val val2Text: String = "",
    val unit: String,
    val timestamp: Long,
    val alertLevel: VitalAlertLevel = VitalAlertLevel.NORMAL,
    val alertText: String = "",
    val valueText: String = "",
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
    val oldDataDangerNotice: String? = null,
    val recentAlerts24h: List<String> = emptyList(),
    val lastUpdatedText: String = "",
    val isFresh: Boolean = false,
    val isDimmed: Boolean = false,
    val rawLastUpdatedTimeFormatted: String = ""
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
    val takenAt: Long = 0L,
    val loadFailed: Boolean = false
)

class CaregiverDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = AuthRepository()

    private val _linkedPatients = MutableStateFlow<List<PatientInfo>>(emptyList())
    val linkedPatients: StateFlow<List<PatientInfo>> = _linkedPatients

    private val _selectedPatient = MutableStateFlow<PatientInfo?>(null)
    val selectedPatient: StateFlow<PatientInfo?> = _selectedPatient

    private val _vitalsState = MutableStateFlow(VitalsSnapshot())
    val vitalsState: StateFlow<VitalsSnapshot> = _vitalsState

    private val _medicinesState = MutableStateFlow<List<MedicineSnapshot>>(emptyList())
    val medicinesState: StateFlow<List<MedicineSnapshot>> = _medicinesState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> = _isEmpty

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isPatientTimeZoneUnknown = MutableStateFlow(false)
    val isPatientTimeZoneUnknown: StateFlow<Boolean> = _isPatientTimeZoneUnknown

    private val _sessionAlertHistory = MutableStateFlow<List<String>>(emptyList())
    val sessionAlertHistory: StateFlow<List<String>> = _sessionAlertHistory

    private var linksListener: ListenerRegistration? = null
    private var vitalsAlertsListener: ListenerRegistration? = null
    private val typeVitalsListeners = mutableListOf<ListenerRegistration>()
    private var medicinesListener: ListenerRegistration? = null
    private val doseListeners = mutableListOf<ListenerRegistration>()
    private var activeDoseMedIds = emptySet<String>()
    private val recordedAlertDocIds = mutableSetOf<String>()

    private var timeoutJob: kotlinx.coroutines.Job? = null
    private var freshnessTimeoutJob: kotlinx.coroutines.Job? = null

    // Two separate generation counters:
    // linksGeneration is incremented only in listenToLinkedPatients
    private var linksGeneration = 0L
    // patientGeneration is incremented in selectPatient and clearPatientListeners
    private var patientGeneration = 0L

    init {
        listenToLinkedPatients()
    }

    fun listenToLinkedPatients() {
        val uid = auth.uid()
        if (uid.isNullOrBlank()) {
            _isEmpty.value = true
            _isLoading.value = false
            return
        }

        linksGeneration++
        val myLinksGen = linksGeneration

        timeoutJob?.cancel()
        if (_linkedPatients.value.isEmpty()) {
            _isLoading.value = true
        }
        _errorMessage.value = null

        timeoutJob = viewModelScope.launch {
            delay(10_000L)
            if (myLinksGen == linksGeneration && _isLoading.value) {
                _isLoading.value = false
                if (_linkedPatients.value.isEmpty()) {
                    _errorMessage.value = "Could not load, check your connection"
                }
            }
        }

        linksListener?.remove()
        linksListener = firestore.collection("links")
            .whereEqualTo("caregiverId", uid)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (myLinksGen != linksGeneration) {
                    Log.d("Caregiver", "Dropping stale links callback for gen $myLinksGen (current: $linksGeneration)")
                    return@addSnapshotListener
                }

                if (error != null) {
                    Log.e("Caregiver", "Links snapshot error: ${error.localizedMessage}", error)
                    _errorMessage.value = "Failed to load linked patients. ${error.localizedMessage}"
                    _isLoading.value = false
                    timeoutJob?.cancel()
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    _isLoading.value = false
                    timeoutJob?.cancel()
                    return@addSnapshotListener
                }

                val docs = snapshot.documents
                if (docs.isEmpty()) {
                    _linkedPatients.value = emptyList()
                    _selectedPatient.value = null
                    _isEmpty.value = true
                    _isLoading.value = false
                    timeoutJob?.cancel()
                    clearPatientListeners()
                    return@addSnapshotListener
                }

                _isEmpty.value = false
                val validDocs = docs.filter { !it.getString("patientId").isNullOrBlank() }
                if (validDocs.isEmpty()) {
                    _linkedPatients.value = emptyList()
                    _selectedPatient.value = null
                    _isEmpty.value = true
                    _isLoading.value = false
                    timeoutJob?.cancel()
                    clearPatientListeners()
                    return@addSnapshotListener
                }

                if (BuildConfig.DEBUG) {
                    val statusSummary = validDocs.map { "${it.getString("patientId")}:${it.getString("status")}" }
                    Log.d("Caregiver", "Links snapshot received. Count=${validDocs.size}, Statuses=$statusSummary")
                }

                val list = mutableListOf<PatientInfo>()
                var completedCount = 0

                fun checkAndFinish() {
                    if (myLinksGen != linksGeneration) return
                    completedCount++
                    if (completedCount == validDocs.size) {
                        _linkedPatients.value = list.toList()
                        _isLoading.value = false
                        timeoutJob?.cancel()

                        val decision = reconcileSelection(_selectedPatient.value, list)
                        if (BuildConfig.DEBUG) {
                            Log.d("Caregiver", "Selection reconcile result: $decision for current=${_selectedPatient.value}")
                        }
                        when (decision) {
                            is ReconcileResult.KeepAsIs -> {
                                // No change needed
                            }
                            is ReconcileResult.ReselectWith -> {
                                selectPatient(decision.patient, forceReattach = true)
                            }
                            is ReconcileResult.Clear -> {
                                _selectedPatient.value = null
                                clearPatientListeners()
                            }
                        }
                    }
                }

                for (doc in validDocs) {
                    val pid = doc.getString("patientId") ?: continue
                    val status = doc.getString("status") ?: "active"
                    val linkId = doc.id
                    val linkInput = LinkDocInput(linkId, pid, status)

                    if (status == "active") {
                        firestore.collection("users").document(pid).get()
                            .addOnSuccessListener { userDoc ->
                                if (myLinksGen != linksGeneration) return@addOnSuccessListener
                                val rawName = userDoc?.getString("name")
                                list.add(buildPatientInfo(linkInput, Result.success(rawName)))
                                checkAndFinish()
                            }
                            .addOnFailureListener {
                                if (myLinksGen != linksGeneration) return@addOnFailureListener
                                list.add(buildPatientInfo(linkInput, Result.failure(it)))
                                checkAndFinish()
                            }
                    } else {
                        list.add(buildPatientInfo(linkInput, null))
                        checkAndFinish()
                    }
                }
            }
    }

    fun selectPatient(patient: PatientInfo, forceReattach: Boolean = false) {
        val current = _selectedPatient.value
        val isSame = current?.patientId == patient.patientId &&
                current?.status == patient.status &&
                current?.linkId == patient.linkId

        val isAnyVitalsActive = vitalsAlertsListener != null || typeVitalsListeners.isNotEmpty()
        if (isSame && !forceReattach && isAnyVitalsActive && medicinesListener != null) {
            if (BuildConfig.DEBUG) {
                Log.d("Caregiver", "Patient ${patient.patientId} is already selected with status ${patient.status}. Skipping re-attach.")
            }
            return
        }

        patientGeneration++
        _selectedPatient.value = patient
        if (patient.status == "active") {
            if (BuildConfig.DEBUG) {
                Log.d("Caregiver", "Attaching patient listeners for active patient ${patient.patientId} (${patient.name})")
            }
            attachPatientListeners(patient.patientId)
        } else {
            if (BuildConfig.DEBUG) {
                Log.d("Caregiver", "Clearing patient listeners for non-active patient ${patient.patientId} (status=${patient.status})")
            }
            clearPatientListeners()
        }
    }

    companion object {
        sealed class ReconcileResult {
            object KeepAsIs : ReconcileResult()
            data class ReselectWith(val patient: PatientInfo) : ReconcileResult()
            object Clear : ReconcileResult()
        }

        data class LinkDocInput(
            val linkId: String,
            val patientId: String,
            val status: String
        )

        fun pickBestPatient(list: List<PatientInfo>): PatientInfo? {
            return list.firstOrNull { it.status == "active" } ?: list.firstOrNull()
        }

        fun reconcileSelection(
            current: PatientInfo?,
            newList: List<PatientInfo>
        ): ReconcileResult {
            if (newList.isEmpty()) {
                return ReconcileResult.Clear
            }
            if (current == null) {
                val best = pickBestPatient(newList)
                return if (best != null) ReconcileResult.ReselectWith(best) else ReconcileResult.Clear
            }

            val exactMatch = newList.firstOrNull {
                it.patientId == current.patientId &&
                        it.status == current.status &&
                        it.linkId == current.linkId
            }

            if (exactMatch != null) {
                return ReconcileResult.KeepAsIs
            }

            val updatedEntry = newList.firstOrNull { it.patientId == current.patientId }
            if (updatedEntry != null) {
                return ReconcileResult.ReselectWith(updatedEntry)
            }

            val bestRemaining = pickBestPatient(newList)
            return if (bestRemaining != null) ReconcileResult.ReselectWith(bestRemaining) else ReconcileResult.Clear
        }

        fun calculateTimerFreshness(
            elapsedSeconds: Long,
            hasServerSnapshotArrived: Boolean
        ): DataFreshnessState {
            return when {
                hasServerSnapshotArrived -> DataFreshnessState.FRESH
                elapsedSeconds >= 10L -> DataFreshnessState.OFFLINE_OR_OLD
                else -> DataFreshnessState.UPDATING
            }
        }

        fun mapDoseStatus(
            rawStatus: String,
            isFresh: Boolean
        ): String {
            if (!isFresh) {
                return "Unknown"
            }
            return rawStatus
        }

        fun buildOldDataNotice(
            hasDangerAlerts: Boolean,
            lastUpdatedText: String
        ): String? {
            if (!hasDangerAlerts) return null
            return "Old data: the last reading showed a danger ($lastUpdatedText)"
        }

        enum class DataFreshnessState {
            FRESH,
            UPDATING,
            OFFLINE_OR_OLD
        }

        data class FreshnessDecision(
            val state: DataFreshnessState,
            val statusText: String,
            val isDimmed: Boolean,
            val showRedAlerts: Boolean
        )

        fun calculateDataFreshness(
            isFromCache: Boolean,
            secondsSinceAttach: Long,
            hasPreviousData: Boolean,
            lastUpdatedTimeFormatted: String
        ): FreshnessDecision {
            return when {
                !isFromCache -> {
                    FreshnessDecision(
                        state = DataFreshnessState.FRESH,
                        statusText = if (lastUpdatedTimeFormatted.isNotBlank()) "Updated: $lastUpdatedTimeFormatted" else "Updated",
                        isDimmed = false,
                        showRedAlerts = true
                    )
                }
                secondsSinceAttach >= 10 -> {
                    FreshnessDecision(
                        state = DataFreshnessState.OFFLINE_OR_OLD,
                        statusText = if (lastUpdatedTimeFormatted.isNotBlank()) "Offline or old data, last updated $lastUpdatedTimeFormatted" else "Offline or old data",
                        isDimmed = true,
                        showRedAlerts = false
                    )
                }
                else -> {
                    FreshnessDecision(
                        state = DataFreshnessState.UPDATING,
                        statusText = if (hasPreviousData && lastUpdatedTimeFormatted.isNotBlank()) "Updating... (last updated $lastUpdatedTimeFormatted)" else "Updating...",
                        isDimmed = true,
                        showRedAlerts = false
                    )
                }
            }
        }

        fun shouldAttachListeners(
            isForeground: Boolean,
            selectedPatient: PatientInfo?,
            isVitalsListenerActive: Boolean,
            isMedsListenerActive: Boolean
        ): Boolean {
            val isActivePatientSelected = selectedPatient != null && selectedPatient.status == "active"
            val isAnyListenerMissing = !isVitalsListenerActive || !isMedsListenerActive
            return isForeground && isActivePatientSelected && isAnyListenerMissing
        }

        fun buildPatientInfo(
            doc: LinkDocInput,
            userNameResult: Result<String?>?
        ): PatientInfo {
            val pid = doc.patientId
            val status = doc.status
            val linkId = doc.linkId

            if (status == "active") {
                val rawName = userNameResult?.getOrNull()
                val isFailure = userNameResult != null && userNameResult.isFailure
                val name = if (isFailure || rawName.isNullOrBlank()) {
                    "Patient (${pid.take(5)}) (Name unavailable)"
                } else {
                    rawName
                }
                return PatientInfo(pid, name, status, linkId)
            } else {
                val name = "Patient (${pid.take(5)}) (Waiting for approval)"
                return PatientInfo(pid, name, status, linkId)
            }
        }
    }

    fun onForegroundResume() {
        val uid = auth.uid()
        if (uid.isNullOrBlank()) return

        if (linksListener == null) {
            listenToLinkedPatients()
        }

        val currentSelected = _selectedPatient.value
        val isAnyVitalsActive = vitalsAlertsListener != null || typeVitalsListeners.isNotEmpty()
        if (shouldAttachListeners(
                isForeground = true,
                selectedPatient = currentSelected,
                isVitalsListenerActive = isAnyVitalsActive,
                isMedsListenerActive = medicinesListener != null
            )
        ) {
            selectPatient(currentSelected!!, forceReattach = true)
        }
    }

    fun onBackgroundStop() {
        linksListener?.remove()
        linksListener = null
        clearPatientListeners()
    }

    fun getActiveListenersSummary(): String {
        val linksActive = if (linksListener != null) 1 else 0
        val vitalsAlertsActive = if (vitalsAlertsListener != null) 1 else 0
        val typeVitalsActive = typeVitalsListeners.size
        val medsActive = if (medicinesListener != null) 1 else 0
        val doseActive = doseListeners.size
        val total = linksActive + vitalsAlertsActive + typeVitalsActive + medsActive + doseActive
        return "Links=$linksActive, VitalsAlerts=$vitalsAlertsActive, TypeVitals=$typeVitalsActive, Meds=$medsActive, Doses=$doseActive, Total=$total"
    }

    private fun attachPatientListeners(patientId: String) {
        clearPatientListeners()
        val myPatientGen = patientGeneration

        freshnessTimeoutJob?.cancel()
        val attachTimestamp = System.currentTimeMillis()

        freshnessTimeoutJob = viewModelScope.launch {
            delay(10_000L)
            if (myPatientGen == patientGeneration && !_vitalsState.value.isFresh) {
                val currentVitals = _vitalsState.value
                val decision = calculateDataFreshness(
                    isFromCache = true,
                    secondsSinceAttach = 10L,
                    hasPreviousData = currentVitals.bp != null || currentVitals.pulse != null || currentVitals.spo2 != null || currentVitals.insulin != null || currentVitals.temp != null,
                    lastUpdatedTimeFormatted = currentVitals.rawLastUpdatedTimeFormatted
                )
                val unknownMeds = _medicinesState.value.map { med ->
                    med.copy(slots = med.slots.map { slot -> slot.copy(status = "Unknown") })
                }
                _medicinesState.value = unknownMeds
                _vitalsState.value = currentVitals.copy(
                    lastUpdatedText = decision.statusText,
                    isFresh = false,
                    isDimmed = true,
                    activeDangerAlerts = emptyList()
                )
            }
        }

        val currentVitals = _vitalsState.value
        val updatedLastUpdatedText = if (currentVitals.lastUpdatedText.isNotBlank()) {
            if (currentVitals.lastUpdatedText.contains("Updating...")) currentVitals.lastUpdatedText else "${currentVitals.lastUpdatedText} (Updating...)"
        } else {
            "Updating..."
        }
        _vitalsState.value = currentVitals.copy(
            lastUpdatedText = updatedLastUpdatedText,
            isFresh = false,
            isDimmed = true
        )

        val currentMeds = _medicinesState.value
        if (currentMeds.isNotEmpty()) {
            _medicinesState.value = currentMeds.map { med ->
                val loadingSlots = med.slots.map { slot ->
                    slot.copy(status = "Loading")
                }
                med.copy(slots = loadingSlots)
            }
        }

        _isPatientTimeZoneUnknown.value = false

        var isMedsFresh = false
        var isDosesFresh = false

        // 1. Attach 24h vitals alerts listener
        val since24h = System.currentTimeMillis() - 24 * 3600 * 1000L
        var isAlertsServerSnapshotDelivered = false
        val typeServerSnapshotsDelivered = mutableSetOf<String>()

        vitalsAlertsListener = firestore.collection("patients").document(patientId)
            .collection("vitals")
            .whereGreaterThanOrEqualTo("timestamp", since24h)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (myPatientGen != patientGeneration) return@addSnapshotListener
                if (error != null || snapshot == null) return@addSnapshotListener

                val isCache = snapshot.metadata.isFromCache
                val isMetadataOnly = snapshot.metadata.hasPendingWrites() || (snapshot.documentChanges.isEmpty() && snapshot.documents.isNotEmpty())

                if (!isCache) {
                    isAlertsServerSnapshotDelivered = true
                }

                try {
                    val activeDangers = mutableListOf<String>()
                    val recentAlerts = mutableListOf<String>()

                    for (doc in snapshot.documents) {
                        val type = doc.getString("type") ?: ""
                        val val1 = doc.get("value")
                        val val2 = doc.get("value2")
                        val unit = doc.getString("unit") ?: ""
                        val kind = doc.getString("kind")
                        val ts = when (val t = doc.get("timestamp")) {
                            is Number -> t.toLong()
                            is String -> t.toLongOrNull() ?: 0L
                            else -> 0L
                        }

                        val item = createVitalItem(type, val1, val2, unit, ts, kind) ?: continue
                        if (!item.isOlderThan24Hours && item.alertLevel == VitalAlertLevel.DANGER) {
                            val msg = "${item.type}: ${item.valueText} - ${item.alertText}"
                            activeDangers.add(msg)
                            recentAlerts.add(msg)
                            if (!isMetadataOnly && !recordedAlertDocIds.contains(doc.id)) {
                                recordedAlertDocIds.add(doc.id)
                                recordSessionAlert(msg)
                            }
                        } else if (!item.isOlderThan24Hours && item.alertLevel == VitalAlertLevel.WARNING) {
                            recentAlerts.add("${item.type}: ${item.valueText} - ${item.alertText}")
                        }
                    }

                    _sessionAlertHistory.value.forEach { historical ->
                        if (!recentAlerts.contains(historical)) {
                            recentAlerts.add(historical)
                        }
                    }

                    val allVitalsFresh = isAlertsServerSnapshotDelivered && typeServerSnapshotsDelivered.size == 5
                    val currentVitals = _vitalsState.value
                    val oldNotice = buildOldDataNotice(activeDangers.isNotEmpty(), currentVitals.rawLastUpdatedTimeFormatted)

                    _vitalsState.value = currentVitals.copy(
                        activeDangerAlerts = if (allVitalsFresh) activeDangers else emptyList(),
                        oldDataDangerNotice = if (!allVitalsFresh) oldNotice else null,
                        recentAlerts24h = recentAlerts,
                        isFresh = allVitalsFresh,
                        isDimmed = !allVitalsFresh
                    )
                } catch (e: Exception) {
                    Log.e("Caregiver", "Error parsing 24h vitals alerts snapshot for $patientId", e)
                }
            }

        // 2. Attach 5 per-type latest reading listeners
        val vitalTypes = listOf("BP", "PULSE", "SPO2", "GLUCOSE", "TEMPERATURE")
        val latestItemsMap = mutableMapOf<String, VitalItem>()

        for (vType in vitalTypes) {
            val listener = firestore.collection("patients").document(patientId)
                .collection("vitals")
                .whereEqualTo("type", vType)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                    if (myPatientGen != patientGeneration) return@addSnapshotListener
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val isCache = snapshot.metadata.isFromCache
                    if (!isCache) {
                        typeServerSnapshotsDelivered.add(vType)
                    }

                    try {
                        val doc = snapshot.documents.firstOrNull()
                        if (doc != null) {
                            val val1 = doc.get("value")
                            val val2 = doc.get("value2")
                            val unit = doc.getString("unit") ?: ""
                            val kind = doc.getString("kind")
                            val ts = when (val t = doc.get("timestamp")) {
                                is Number -> t.toLong()
                                is String -> t.toLongOrNull() ?: 0L
                                else -> 0L
                            }
                            val item = createVitalItem(vType, val1, val2, unit, ts, kind)
                            if (item != null) {
                                latestItemsMap[vType] = item
                            }
                        }

                        val maxTs = latestItemsMap.values.maxOfOrNull { it.timestamp } ?: 0L
                        val rawTimeStr = if (maxTs > 0L) formatTime(maxTs) else ""
                        val allVitalsFresh = isAlertsServerSnapshotDelivered && typeServerSnapshotsDelivered.size == 5

                        val currentVitals = _vitalsState.value
                        val elapsedSec = (System.currentTimeMillis() - attachTimestamp) / 1000L
                        val freshnessDecision = calculateDataFreshness(
                            isFromCache = !allVitalsFresh,
                            secondsSinceAttach = elapsedSec,
                            hasPreviousData = latestItemsMap.isNotEmpty(),
                            lastUpdatedTimeFormatted = rawTimeStr
                        )

                        if (allVitalsFresh) {
                            freshnessTimeoutJob?.cancel()
                        }

                        val hasDanger = _vitalsState.value.activeDangerAlerts.isNotEmpty()
                        val oldNotice = buildOldDataNotice(hasDanger, rawTimeStr)

                        _vitalsState.value = currentVitals.copy(
                            bp = latestItemsMap["BP"] ?: currentVitals.bp,
                            pulse = latestItemsMap["PULSE"] ?: currentVitals.pulse,
                            spo2 = latestItemsMap["SPO2"] ?: currentVitals.spo2,
                            insulin = latestItemsMap["GLUCOSE"] ?: currentVitals.insulin,
                            temp = latestItemsMap["TEMPERATURE"] ?: currentVitals.temp,
                            oldDataDangerNotice = if (!allVitalsFresh) oldNotice else null,
                            lastUpdatedText = freshnessDecision.statusText,
                            isFresh = allVitalsFresh,
                            isDimmed = freshnessDecision.isDimmed,
                            rawLastUpdatedTimeFormatted = rawTimeStr
                        )
                    } catch (e: Exception) {
                        Log.e("Caregiver", "Error parsing per-type vital snapshot for $vType on $patientId", e)
                    }
                }
            typeVitalsListeners.add(listener)
        }

        // 3. Fetch patient timezone first, then attach medicines & today doses listeners
        firestore.collection("users").document(patientId).get()
            .addOnSuccessListener { userDoc ->
                if (myPatientGen != patientGeneration) return@addOnSuccessListener

                val rawTz = userDoc?.getString("timeZone")
                val isTzMissing = rawTz.isNullOrBlank()
                _isPatientTimeZoneUnknown.value = isTzMissing

                val timeZoneId = if (!isTzMissing) rawTz!! else TimeZone.getDefault().id
                val tz = TimeZone.getTimeZone(timeZoneId)

                val calendar = Calendar.getInstance(tz)
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                if (hour in 0..4) {
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                }
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { this.timeZone = tz }
                val todayDate = dateFormat.format(calendar.time)

                val rawHour = calendar.get(Calendar.HOUR_OF_DAY)
                val rawMinute = calendar.get(Calendar.MINUTE)
                val currentTimeInMinutes = if (rawHour in 0..4) (1440 + rawHour * 60 + rawMinute) else (rawHour * 60 + rawMinute)

                medicinesListener = firestore.collection("patients").document(patientId)
                    .collection("medicines")
                    .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                        if (myPatientGen != patientGeneration) return@addSnapshotListener
                        if (error != null || snapshot == null) return@addSnapshotListener

                        val isMedsCache = snapshot.metadata.isFromCache
                        if (!isMedsCache) {
                            isMedsFresh = true
                        }

                        try {
                            val docs = snapshot.documents
                            if (docs.isEmpty()) {
                                _medicinesState.value = emptyList()
                                return@addSnapshotListener
                            }

                            val newMedIds = docs.map { it.id }.toSet()
                            val medMap = mutableMapOf<String, MedicineSnapshot>()

                            for (doc in docs) {
                                val medId = doc.id
                                val name = doc.getString("name") ?: ""
                                val dose = doc.getString("dose") ?: ""
                                val timeStr = doc.getString("time") ?: ""
                                val slotNames = timeStr.split(",").map { it.trim() }.filter { it.isNotBlank() }

                                val isDoseDataFresh = isMedsFresh && isDosesFresh
                                val loadingSlots = slotNames.map { slotName ->
                                    val mappedStatus = mapDoseStatus("Loading", isDoseDataFresh)
                                    DoseSlotSnapshot(slot = slotName, taken = false, takenAt = 0L, status = mappedStatus)
                                }
                                medMap[medId] = MedicineSnapshot(
                                    id = medId, name = name, dose = dose, time = timeStr,
                                    slots = loadingSlots, taken = false, takenAt = 0L, loadFailed = false
                                )
                            }
                            _medicinesState.value = medMap.values.sortedBy { it.name }

                            // Prevent dose listener churn: only recreate doseListeners if medicine ID set changed
                            if (newMedIds == activeDoseMedIds && doseListeners.isNotEmpty()) {
                                return@addSnapshotListener
                            }

                            activeDoseMedIds = newMedIds
                            doseListeners.forEach { it.remove() }
                            doseListeners.clear()

                            var dosesServerCount = 0

                            for (doc in docs) {
                                val medId = doc.id
                                val name = doc.getString("name") ?: ""
                                val dose = doc.getString("dose") ?: ""
                                val timeStr = doc.getString("time") ?: ""
                                val slotNames = timeStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                val scheduleTimesMap = doc.get("scheduleTimes") as? Map<String, String> ?: emptyMap()

                                val dListener = firestore.collection("patients").document(patientId)
                                    .collection("medicines").document(medId)
                                    .collection("doses")
                                    .whereEqualTo("date", todayDate)
                                    .addSnapshotListener(MetadataChanges.INCLUDE) { doseSnap, doseErr ->
                                        if (myPatientGen != patientGeneration) return@addSnapshotListener
                                        if (doseErr != null) return@addSnapshotListener

                                        val isDoseCache = doseSnap?.metadata?.isFromCache ?: true
                                        if (!isDoseCache) {
                                            dosesServerCount++
                                            if (dosesServerCount == docs.size) {
                                                isDosesFresh = true
                                            }
                                        }

                                        val isDoseDataFresh = isMedsFresh && isDosesFresh
                                        val doseMap = mutableMapOf<String, Pair<Boolean, Long>>()
                                        if (doseSnap != null) {
                                            for (dDoc in doseSnap.documents) {
                                                val slot = dDoc.getString("slot") ?: ""
                                                val taken = dDoc.getBoolean("taken") ?: false
                                                val takenAt = dDoc.getLong("takenAt") ?: 0L
                                                if (slot.isNotBlank()) {
                                                    doseMap[slot] = Pair(taken, takenAt)
                                                }
                                            }
                                        }

                                        val slotsSnapshots = slotNames.map { slotName ->
                                            val pair = doseMap[slotName]
                                            val isTaken = pair?.first ?: false
                                            val takenAt = pair?.second ?: 0L
                                            val rawStatus = MedicineScheduleHelper.calculateDoseStatus(slotName, isTaken, currentTimeInMinutes, scheduleTimesMap = scheduleTimesMap)
                                            val finalStatus = mapDoseStatus(rawStatus, isDoseDataFresh)

                                            DoseSlotSnapshot(slot = slotName, taken = isTaken, takenAt = takenAt, status = finalStatus)
                                        }

                                        val overallTaken = slotsSnapshots.isNotEmpty() && slotsSnapshots.all { it.taken }
                                        val maxTakenAt = slotsSnapshots.maxOfOrNull { it.takenAt } ?: 0L

                                        medMap[medId] = MedicineSnapshot(
                                            id = medId, name = name, dose = dose, time = timeStr,
                                            slots = slotsSnapshots, taken = overallTaken, takenAt = maxTakenAt, loadFailed = false
                                        )
                                        _medicinesState.value = medMap.values.sortedBy { it.name }
                                    }
                                doseListeners.add(dListener)
                            }
                        } catch (e: Exception) {
                            Log.e("Caregiver", "Error parsing medicines snapshot for $patientId", e)
                        }
                    }
            }
            .addOnFailureListener {
                if (myPatientGen != patientGeneration) return@addOnFailureListener
                _isPatientTimeZoneUnknown.value = true
            }
    }

    private fun createVitalItem(type: String, val1: Any?, val2: Any?, unit: String, ts: Long, kind: String? = null): VitalItem? {
        if (type == "GLUCOSE" && kind == "INSULIN_DOSE") return null

        val val1Double = val1?.toString()?.toDoubleOrNull()
        if (val1Double == null || val1Double <= 0.0) return null

        val (timeAgoText, isOlderThan24) = formatTimeAgo(ts)

        val alertResult = when (type) {
            "BP" -> {
                val sys = val1Double
                val dia = val2?.toString()?.toDoubleOrNull() ?: 0.0
                if (sys < 50 || sys > 250 || dia < 30 || dia > 150) {
                    Pair(VitalAlertLevel.DANGER, "Impossible value")
                } else if (sys >= 160 || dia >= 100 || sys < 85) {
                    Pair(VitalAlertLevel.DANGER, "Critical BP")
                } else if (sys >= 130 || dia >= 85) {
                    Pair(VitalAlertLevel.WARNING, "Elevated BP")
                } else {
                    Pair(VitalAlertLevel.NORMAL, "")
                }
            }
            "PULSE" -> {
                if (val1Double < 30 || val1Double > 220) {
                    Pair(VitalAlertLevel.DANGER, "Impossible value")
                } else if (val1Double < 50 || val1Double > 120) {
                    Pair(VitalAlertLevel.DANGER, "Abnormal Pulse")
                } else if (val1Double < 60 || val1Double > 100) {
                    Pair(VitalAlertLevel.WARNING, "Borderline Pulse")
                } else {
                    Pair(VitalAlertLevel.NORMAL, "")
                }
            }
            "SPO2" -> {
                if (val1Double < 50 || val1Double > 100) {
                    Pair(VitalAlertLevel.DANGER, "Impossible value")
                } else if (val1Double < 90) {
                    Pair(VitalAlertLevel.DANGER, "Low SpO2")
                } else if (val1Double < 94) {
                    Pair(VitalAlertLevel.WARNING, "Borderline SpO2")
                } else {
                    Pair(VitalAlertLevel.NORMAL, "")
                }
            }
            "GLUCOSE" -> {
                if (val1Double < 20 || val1Double > 600) {
                    Pair(VitalAlertLevel.DANGER, "Impossible value")
                } else if (val1Double < 70 || val1Double > 250) {
                    Pair(VitalAlertLevel.DANGER, "Critical Glucose")
                } else if (val1Double > 180) {
                    Pair(VitalAlertLevel.WARNING, "High Glucose")
                } else {
                    Pair(VitalAlertLevel.NORMAL, "")
                }
            }
            "TEMPERATURE" -> {
                if (val1Double < 90 || val1Double > 110) {
                    Pair(VitalAlertLevel.DANGER, "Impossible value")
                } else if (val1Double >= 102.0 || val1Double < 95.0) {
                    Pair(VitalAlertLevel.DANGER, "High Fever / Hypothermia")
                } else if (val1Double >= 99.5) {
                    Pair(VitalAlertLevel.WARNING, "Mild Fever")
                } else {
                    Pair(VitalAlertLevel.NORMAL, "")
                }
            }
            else -> Pair(VitalAlertLevel.NORMAL, "")
        }

        val valueDisplay = if (type == "BP") {
            val dia = val2?.toString()?.toDoubleOrNull()?.toInt() ?: 0
            "${val1Double.toInt()}/$dia $unit"
        } else {
            "${val1Double.toInt()} $unit"
        }

        val isImpossible = alertResult.second == "Impossible value"

        return VitalItem(
            type = type,
            val1Text = val1Double.toString(),
            val2Text = val2?.toString() ?: "",
            unit = unit,
            timestamp = ts,
            alertLevel = alertResult.first,
            alertText = alertResult.second,
            valueText = valueDisplay,
            timeAgoText = timeAgoText,
            isOlderThan24Hours = isOlderThan24,
            isImpossible = isImpossible
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

    fun clearPatientListeners() {
        patientGeneration++
        freshnessTimeoutJob?.cancel()
        freshnessTimeoutJob = null
        vitalsAlertsListener?.remove()
        vitalsAlertsListener = null
        typeVitalsListeners.forEach { it.remove() }
        typeVitalsListeners.clear()
        medicinesListener?.remove()
        medicinesListener = null
        doseListeners.forEach { it.remove() }
        doseListeners.clear()
        activeDoseMedIds = emptySet()
        recordedAlertDocIds.clear()
        Log.d("Caregiver", "Active listeners after clear: Total=0")
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
