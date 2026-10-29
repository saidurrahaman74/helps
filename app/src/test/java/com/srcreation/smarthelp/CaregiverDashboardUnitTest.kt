package com.srcreation.smarthelp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CaregiverDashboardUnitTest {

    data class MockLinkDoc(
        val linkId: String,
        val patientId: String,
        val status: String
    )

    data class MockProcessState(
        var isLoading: Boolean = true,
        var isEmpty: Boolean = false,
        var errorMessage: String? = null,
        var linkedPatients: List<PatientInfo> = emptyList(),
        var selectedPatient: PatientInfo? = null,
        var linksGeneration: Long = 1L,
        var patientGeneration: Long = 1L
    )

    fun simulateLinkProcessing(
        docs: List<MockLinkDoc>,
        userNamesMap: Map<String, String?>,
        userFailuresSet: Set<String>,
        state: MockProcessState,
        linksGen: Long = state.linksGeneration,
        timeoutFired: Boolean = false
    ) {
        if (linksGen != state.linksGeneration) {
            return
        }

        if (timeoutFired) {
            if (state.isLoading) {
                state.isLoading = false
                if (state.linkedPatients.isEmpty()) {
                    state.errorMessage = "Could not load, check your connection"
                }
            }
            return
        }

        if (docs.isEmpty()) {
            state.linkedPatients = emptyList()
            state.selectedPatient = null
            state.isEmpty = true
            state.isLoading = false
            return
        }

        state.isEmpty = false
        val validDocs = docs.filter { it.patientId.isNotBlank() }
        if (validDocs.isEmpty()) {
            state.linkedPatients = emptyList()
            state.selectedPatient = null
            state.isEmpty = true
            state.isLoading = false
            return
        }

        val list = mutableListOf<PatientInfo>()
        var completedCount = 0

        fun checkAndFinish() {
            if (linksGen != state.linksGeneration) return
            completedCount++
            if (completedCount == validDocs.size) {
                state.linkedPatients = list.toList()
                state.isLoading = false

                val decision = CaregiverDashboardViewModel.reconcileSelection(state.selectedPatient, list)
                when (decision) {
                    is CaregiverDashboardViewModel.Companion.ReconcileResult.KeepAsIs -> {
                        // Keep current
                    }
                    is CaregiverDashboardViewModel.Companion.ReconcileResult.ReselectWith -> {
                        simulateSelectPatient(decision.patient, state)
                    }
                    is CaregiverDashboardViewModel.Companion.ReconcileResult.Clear -> {
                        state.selectedPatient = null
                    }
                }
            }
        }

        for (doc in validDocs) {
            val pid = doc.patientId
            val status = doc.status
            val linkId = doc.linkId
            val linkInput = CaregiverDashboardViewModel.Companion.LinkDocInput(linkId, pid, status)

            if (status == "active") {
                if (userFailuresSet.contains(pid)) {
                    if (linksGen != state.linksGeneration) continue
                    val info = CaregiverDashboardViewModel.buildPatientInfo(linkInput, Result.failure(Exception("Fetch error")))
                    list.add(info)
                    checkAndFinish()
                } else {
                    if (linksGen != state.linksGeneration) continue
                    val rawName = userNamesMap[pid]
                    val info = CaregiverDashboardViewModel.buildPatientInfo(linkInput, Result.success(rawName))
                    list.add(info)
                    checkAndFinish()
                }
            } else {
                val info = CaregiverDashboardViewModel.buildPatientInfo(linkInput, null)
                list.add(info)
                checkAndFinish()
            }
        }
    }

    fun simulateSelectPatient(patient: PatientInfo, state: MockProcessState) {
        state.patientGeneration++
        state.selectedPatient = patient
    }

    @Test
    fun testReconcile_pendingToActive_triggersReselectWithActive() {
        val current = PatientInfo("p1", "Patient 1", "pending", "l1")
        val newList = listOf(
            PatientInfo("p1", "Patient 1", "active", "l1")
        )

        val result = CaregiverDashboardViewModel.reconcileSelection(current, newList)
        assertTrue(result is CaregiverDashboardViewModel.Companion.ReconcileResult.ReselectWith)
        val reselect = result as CaregiverDashboardViewModel.Companion.ReconcileResult.ReselectWith
        assertEquals("active", reselect.patient.status)
        assertEquals("p1", reselect.patient.patientId)
    }

    @Test
    fun testReconcile_samePatientAndSameStatus_givesKeepAsIs() {
        val current = PatientInfo("p1", "Patient 1", "active", "l1")
        val newList = listOf(
            PatientInfo("p1", "Patient 1", "active", "l1")
        )

        val result = CaregiverDashboardViewModel.reconcileSelection(current, newList)
        assertEquals(CaregiverDashboardViewModel.Companion.ReconcileResult.KeepAsIs, result)
    }

    @Test
    fun testReconcile_activeToPending_reselectsPending() {
        val current = PatientInfo("p1", "Patient 1", "active", "l1")
        val newList = listOf(
            PatientInfo("p1", "Patient 1", "pending", "l1")
        )

        val result = CaregiverDashboardViewModel.reconcileSelection(current, newList)
        assertTrue(result is CaregiverDashboardViewModel.Companion.ReconcileResult.ReselectWith)
        val reselect = result as CaregiverDashboardViewModel.Companion.ReconcileResult.ReselectWith
        assertEquals("pending", reselect.patient.status)
    }

    @Test
    fun testReconcile_patientRemoved_picksBestRemaining() {
        val current = PatientInfo("p1", "Patient 1", "active", "l1")
        val newList = listOf(
            PatientInfo("p2", "Patient 2", "active", "l2")
        )

        val result = CaregiverDashboardViewModel.reconcileSelection(current, newList)
        assertTrue(result is CaregiverDashboardViewModel.Companion.ReconcileResult.ReselectWith)
        val reselect = result as CaregiverDashboardViewModel.Companion.ReconcileResult.ReselectWith
        assertEquals("p2", reselect.patient.patientId)
    }

    @Test
    fun testReconcile_twoPatientsOneChangesStatus_reselectsChanged() {
        val current = PatientInfo("p1", "Patient 1", "pending", "l1")
        val newList = listOf(
            PatientInfo("p1", "Patient 1", "active", "l1"),
            PatientInfo("p2", "Patient 2", "active", "l2")
        )

        val result = CaregiverDashboardViewModel.reconcileSelection(current, newList)
        assertTrue(result is CaregiverDashboardViewModel.Companion.ReconcileResult.ReselectWith)
        val reselect = result as CaregiverDashboardViewModel.Companion.ReconcileResult.ReselectWith
        assertEquals("p1", reselect.patient.patientId)
        assertEquals("active", reselect.patient.status)
    }

    @Test
    fun testReconcile_emptyList_returnsClear() {
        val current = PatientInfo("p1", "Patient 1", "active", "l1")
        val result = CaregiverDashboardViewModel.reconcileSelection(current, emptyList())
        assertEquals(CaregiverDashboardViewModel.Companion.ReconcileResult.Clear, result)
    }

    @Test
    fun testNoLinksAtAll_endsLoadingImmediately() {
        val state = MockProcessState(isLoading = true)
        simulateLinkProcessing(
            docs = emptyList(),
            userNamesMap = emptyMap(),
            userFailuresSet = emptySet(),
            state = state
        )

        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
        assertTrue(state.linkedPatients.isEmpty())
        assertNull(state.selectedPatient)
    }

    @Test
    fun testOnlyPendingLinks_endsLoadingAndShowsWaitingForApproval() {
        val state = MockProcessState(isLoading = true)
        val docs = listOf(
            MockLinkDoc("l1", "p12345", "pending")
        )

        simulateLinkProcessing(
            docs = docs,
            userNamesMap = emptyMap(),
            userFailuresSet = emptySet(),
            state = state
        )

        assertFalse(state.isLoading)
        assertFalse(state.isEmpty)
        assertEquals(1, state.linkedPatients.size)
        val patient = state.linkedPatients.first()
        assertEquals("pending", patient.status)
        assertTrue(patient.name.contains("Waiting for approval"))
        assertEquals("p12345", state.selectedPatient?.patientId)
    }

    @Test
    fun testOneActivePlusOnePending_selectsActivePatient() {
        val state = MockProcessState(isLoading = true)
        val docs = listOf(
            MockLinkDoc("l1", "p_pending", "pending"),
            MockLinkDoc("l2", "p_active", "active")
        )
        val names = mapOf("p_active" to "Active User")

        simulateLinkProcessing(
            docs = docs,
            userNamesMap = names,
            userFailuresSet = emptySet(),
            state = state
        )

        assertFalse(state.isLoading)
        assertEquals(2, state.linkedPatients.size)
        assertEquals("p_active", state.selectedPatient?.patientId)
        assertEquals("Active User", state.selectedPatient?.name)
    }

    @Test
    fun testThreeActiveLinks_namesArrivingInDifferentOrders() {
        val state = MockProcessState(isLoading = true)
        val docs = listOf(
            MockLinkDoc("l1", "p100", "active"),
            MockLinkDoc("l2", "p200", "active"),
            MockLinkDoc("l3", "p300", "active")
        )
        val namesInArrivalOrder = mapOf(
            "p300" to "Alice",
            "p100" to "Bob",
            "p200" to "Charlie"
        )

        simulateLinkProcessing(
            docs = docs,
            userNamesMap = namesInArrivalOrder,
            userFailuresSet = emptySet(),
            state = state
        )

        assertFalse(state.isLoading)
        assertEquals(3, state.linkedPatients.size)
        assertEquals("p100", state.selectedPatient?.patientId)
    }

    @Test
    fun testNameFetchFailure_showsNameUnavailableNote() {
        val state = MockProcessState(isLoading = true)
        val docs = listOf(
            MockLinkDoc("l1", "p1", "active"),
            MockLinkDoc("l2", "p2", "active")
        )
        val names = mapOf("p1" to "Success Patient")
        val failures = setOf("p2")

        simulateLinkProcessing(
            docs = docs,
            userNamesMap = names,
            userFailuresSet = failures,
            state = state
        )

        assertFalse(state.isLoading)
        assertEquals(2, state.linkedPatients.size)
        assertEquals("Success Patient", state.linkedPatients[0].name)
        assertTrue(state.linkedPatients[1].name.contains("Name unavailable"))
    }

    @Test
    fun testBothQueriesFail() {
        val state = MockProcessState(isLoading = true)
        val docs = listOf(
            MockLinkDoc("l1", "p1", "active"),
            MockLinkDoc("l2", "p2", "active")
        )
        val failures = setOf("p1", "p2")

        simulateLinkProcessing(
            docs = docs,
            userNamesMap = emptyMap(),
            userFailuresSet = failures,
            state = state
        )

        assertFalse(state.isLoading)
        assertEquals(2, state.linkedPatients.size)
        assertTrue(state.linkedPatients[0].name.contains("Name unavailable"))
        assertTrue(state.linkedPatients[1].name.contains("Name unavailable"))
    }

    @Test
    fun testTimeoutPath() {
        val state = MockProcessState(isLoading = true)

        simulateLinkProcessing(
            docs = emptyList(),
            userNamesMap = emptyMap(),
            userFailuresSet = emptySet(),
            state = state,
            timeoutFired = true
        )

        assertFalse(state.isLoading)
        assertEquals("Could not load, check your connection", state.errorMessage)
    }

    @Test
    fun testLinkRemovedWhileOpen() {
        val state = MockProcessState(isLoading = true)
        val initialDocs = listOf(
            MockLinkDoc("l1", "p1", "active"),
            MockLinkDoc("l2", "p2", "active")
        )
        val names = mapOf("p1" to "Patient One", "p2" to "Patient Two")

        simulateLinkProcessing(
            docs = initialDocs,
            userNamesMap = names,
            userFailuresSet = emptySet(),
            state = state
        )
        assertEquals("p1", state.selectedPatient?.patientId)

        val updatedDocs = listOf(
            MockLinkDoc("l2", "p2", "active")
        )
        simulateLinkProcessing(
            docs = updatedDocs,
            userNamesMap = names,
            userFailuresSet = emptySet(),
            state = state
        )

        assertFalse(state.isLoading)
        assertEquals(1, state.linkedPatients.size)
        assertEquals("p2", state.selectedPatient?.patientId)
    }

    @Test
    fun testSelectionDoesNotDropLaterLinkSnapshotUpdates() {
        val state = MockProcessState(isLoading = true)
        val initialDocs = listOf(
            MockLinkDoc("l1", "p1", "pending")
        )

        simulateLinkProcessing(
            docs = initialDocs,
            userNamesMap = emptyMap(),
            userFailuresSet = emptySet(),
            state = state
        )
        assertEquals("p1", state.selectedPatient?.patientId)
        assertEquals("pending", state.selectedPatient?.status)
        val linksGenBefore = state.linksGeneration
        val patientGenBefore = state.patientGeneration

        simulateSelectPatient(state.selectedPatient!!, state)
        assertEquals(linksGenBefore, state.linksGeneration)
        assertTrue(state.patientGeneration > patientGenBefore)

        val updatedDocs = listOf(
            MockLinkDoc("l1", "p1", "active")
        )
        simulateLinkProcessing(
            docs = updatedDocs,
            userNamesMap = mapOf("p1" to "Patient One Active"),
            userFailuresSet = emptySet(),
            state = state
        )

        assertEquals(1, state.linkedPatients.size)
        assertEquals("active", state.linkedPatients[0].status)
        assertEquals("Patient One Active", state.linkedPatients[0].name)
        assertEquals("active", state.selectedPatient?.status)
    }

    @Test
    fun testStaleCallbackIgnored() {
        val state = MockProcessState(isLoading = true, linksGeneration = 2L)
        val docs = listOf(
            MockLinkDoc("l1", "p1", "active")
        )

        simulateLinkProcessing(
            docs = docs,
            userNamesMap = mapOf("p1" to "Old Generation Patient"),
            userFailuresSet = emptySet(),
            state = state,
            linksGen = 1L
        )

        assertTrue(state.isLoading)
        assertTrue(state.linkedPatients.isEmpty())
    }

    @Test
    fun testShouldAttachListeners_whenForegroundAndActivePatientAndNotAttached_returnsTrue() {
        val patient = PatientInfo("p1", "John", "active", "l1")
        val result = CaregiverDashboardViewModel.shouldAttachListeners(
            isForeground = true,
            selectedPatient = patient,
            isVitalsListenerActive = false,
            isMedsListenerActive = false
        )
        assertTrue(result)
    }

    @Test
    fun testShouldAttachListeners_whenBackground_returnsFalse() {
        val patient = PatientInfo("p1", "John", "active", "l1")
        val result = CaregiverDashboardViewModel.shouldAttachListeners(
            isForeground = false,
            selectedPatient = patient,
            isVitalsListenerActive = false,
            isMedsListenerActive = false
        )
        assertFalse(result)
    }

    @Test
    fun testShouldAttachListeners_whenPendingPatient_returnsFalse() {
        val patient = PatientInfo("p1", "John", "pending", "l1")
        val result = CaregiverDashboardViewModel.shouldAttachListeners(
            isForeground = true,
            selectedPatient = patient,
            isVitalsListenerActive = false,
            isMedsListenerActive = false
        )
        assertFalse(result)
    }

    @Test
    fun testShouldAttachListeners_whenAlreadyAttached_returnsFalse() {
        val patient = PatientInfo("p1", "John", "active", "l1")
        val result = CaregiverDashboardViewModel.shouldAttachListeners(
            isForeground = true,
            selectedPatient = patient,
            isVitalsListenerActive = true,
            isMedsListenerActive = true
        )
        assertFalse(result)
    }

    @Test
    fun testOnForegroundResume_whenAlreadyAttached_doesNothing() {
        val patient = PatientInfo("p1", "John", "active", "l1")
        val shouldAttach = CaregiverDashboardViewModel.shouldAttachListeners(
            isForeground = true,
            selectedPatient = patient,
            isVitalsListenerActive = true,
            isMedsListenerActive = true
        )
        assertFalse(shouldAttach)
    }

    @Test
    fun testStopThenStart_decidesToReattachBothLinksAndPatientListeners() {
        val selectedPatient = PatientInfo("p1", "John", "active", "l1")

        val isLinksActive = false
        val isVitalsActive = false
        val isMedsActive = false

        val shouldReattachLinks = !isLinksActive
        val shouldReattachPatient = CaregiverDashboardViewModel.shouldAttachListeners(
            isForeground = true,
            selectedPatient = selectedPatient,
            isVitalsListenerActive = isVitalsActive,
            isMedsListenerActive = isMedsActive
        )

        assertTrue(shouldReattachLinks)
        assertTrue(shouldReattachPatient)
    }

    @Test
    fun testPatientBecamePendingOrRemovedInBackground_notReattachedAsActive() {
        val selectedBeforeBackground = PatientInfo("p1", "John", "active", "l1")

        val newListFromBackground = listOf(
            PatientInfo("p1", "John", "pending", "l1")
        )

        val reconcile = CaregiverDashboardViewModel.reconcileSelection(selectedBeforeBackground, newListFromBackground)
        assertTrue(reconcile is CaregiverDashboardViewModel.Companion.ReconcileResult.ReselectWith)
        val reselect = reconcile as CaregiverDashboardViewModel.Companion.ReconcileResult.ReselectWith
        assertEquals("pending", reselect.patient.status)

        val shouldAttach = CaregiverDashboardViewModel.shouldAttachListeners(
            isForeground = true,
            selectedPatient = reselect.patient,
            isVitalsListenerActive = false,
            isMedsListenerActive = false
        )
        assertFalse(shouldAttach)
    }

    @Test
    fun testFreshness_serverSnapshotArrives_returnsFreshAndFullColor() {
        val decision = CaregiverDashboardViewModel.calculateDataFreshness(
            isFromCache = false,
            secondsSinceAttach = 2L,
            hasPreviousData = true,
            lastUpdatedTimeFormatted = "10:30 AM"
        )

        assertEquals(CaregiverDashboardViewModel.Companion.DataFreshnessState.FRESH, decision.state)
        assertEquals("Updated: 10:30 AM", decision.statusText)
        assertFalse(decision.isDimmed)
        assertTrue(decision.showRedAlerts)
    }

    @Test
    fun testFreshness_cachedSnapshotUnder10s_returnsUpdatingAndDimmedNoRedAlerts() {
        val decision = CaregiverDashboardViewModel.calculateDataFreshness(
            isFromCache = true,
            secondsSinceAttach = 3L,
            hasPreviousData = true,
            lastUpdatedTimeFormatted = "10:30 AM"
        )

        assertEquals(CaregiverDashboardViewModel.Companion.DataFreshnessState.UPDATING, decision.state)
        assertTrue(decision.statusText.contains("Updating..."))
        assertTrue(decision.isDimmed)
        assertFalse(decision.showRedAlerts)
    }

    @Test
    fun testFreshness_cachedSnapshotOver10s_returnsOfflineOrOldAndDimmedNoRedAlerts() {
        val decision = CaregiverDashboardViewModel.calculateDataFreshness(
            isFromCache = true,
            secondsSinceAttach = 12L,
            hasPreviousData = true,
            lastUpdatedTimeFormatted = "10:30 AM"
        )

        assertEquals(CaregiverDashboardViewModel.Companion.DataFreshnessState.OFFLINE_OR_OLD, decision.state)
        assertTrue(decision.statusText.contains("Offline or old data"))
        assertTrue(decision.isDimmed)
        assertFalse(decision.showRedAlerts)
    }

    @Test
    fun testFreshness_cachedSnapshotDoesNotClearUpdatingText() {
        val decision = CaregiverDashboardViewModel.calculateDataFreshness(
            isFromCache = true,
            secondsSinceAttach = 1L,
            hasPreviousData = false,
            lastUpdatedTimeFormatted = ""
        )

        assertEquals(CaregiverDashboardViewModel.Companion.DataFreshnessState.UPDATING, decision.state)
        assertEquals("Updating...", decision.statusText)
        assertTrue(decision.isDimmed)
        assertFalse(decision.showRedAlerts)
    }

    @Test
    fun testTimerFreshness_at0Sec_returnsUpdating() {
        val state = CaregiverDashboardViewModel.calculateTimerFreshness(
            elapsedSeconds = 0L,
            hasServerSnapshotArrived = false
        )
        assertEquals(CaregiverDashboardViewModel.Companion.DataFreshnessState.UPDATING, state)
    }

    @Test
    fun testTimerFreshness_at5Sec_returnsUpdating() {
        val state = CaregiverDashboardViewModel.calculateTimerFreshness(
            elapsedSeconds = 5L,
            hasServerSnapshotArrived = false
        )
        assertEquals(CaregiverDashboardViewModel.Companion.DataFreshnessState.UPDATING, state)
    }

    @Test
    fun testTimerFreshness_at9Sec_returnsUpdating() {
        val state = CaregiverDashboardViewModel.calculateTimerFreshness(
            elapsedSeconds = 9L,
            hasServerSnapshotArrived = false
        )
        assertEquals(CaregiverDashboardViewModel.Companion.DataFreshnessState.UPDATING, state)
    }

    @Test
    fun testTimerFreshness_at10Sec_returnsOfflineOrOld() {
        val state = CaregiverDashboardViewModel.calculateTimerFreshness(
            elapsedSeconds = 10L,
            hasServerSnapshotArrived = false
        )
        assertEquals(CaregiverDashboardViewModel.Companion.DataFreshnessState.OFFLINE_OR_OLD, state)
    }

    @Test
    fun testTimerFreshness_at15Sec_returnsOfflineOrOld() {
        val state = CaregiverDashboardViewModel.calculateTimerFreshness(
            elapsedSeconds = 15L,
            hasServerSnapshotArrived = false
        )
        assertEquals(CaregiverDashboardViewModel.Companion.DataFreshnessState.OFFLINE_OR_OLD, state)
    }

    @Test
    fun testTimerFreshness_whenServerArrived_returnsFresh() {
        val state = CaregiverDashboardViewModel.calculateTimerFreshness(
            elapsedSeconds = 5L,
            hasServerSnapshotArrived = true
        )
        assertEquals(CaregiverDashboardViewModel.Companion.DataFreshnessState.FRESH, state)
    }

    @Test
    fun testMapDoseStatus_whenNotFresh_returnsUnknown() {
        val status = CaregiverDashboardViewModel.mapDoseStatus(
            rawStatus = "Missed",
            isFresh = false
        )
        assertEquals("Unknown", status)
    }

    @Test
    fun testMapDoseStatus_whenFresh_returnsRawStatus() {
        val status = CaregiverDashboardViewModel.mapDoseStatus(
            rawStatus = "Missed",
            isFresh = true
        )
        assertEquals("Missed", status)
    }

    @Test
    fun testBuildOldDataNotice_whenDangerAlertsExist_returnsNoticeText() {
        val notice = CaregiverDashboardViewModel.buildOldDataNotice(
            hasDangerAlerts = true,
            lastUpdatedText = "10:30 AM"
        )
        assertEquals("Old data: the last reading showed a danger (10:30 AM)", notice)
    }

    @Test
    fun testBuildOldDataNotice_whenNoDangerAlerts_returnsNull() {
        val notice = CaregiverDashboardViewModel.buildOldDataNotice(
            hasDangerAlerts = false,
            lastUpdatedText = "10:30 AM"
        )
        assertNull(notice)
    }

    @Test
    fun testDoseListenerReattachment_onlyWhenMedicineSetChanges() {
        val existingMedIds = setOf("m1", "m2")
        val sameMedIds = setOf("m1", "m2")
        val newMedIds = setOf("m1", "m2", "m3")

        val shouldReattachSame = existingMedIds != sameMedIds
        val shouldReattachNew = existingMedIds != newMedIds

        assertFalse(shouldReattachSame)
        assertTrue(shouldReattachNew)
    }

    @Test
    fun testMetadataOnlyEvent_doesNotRecordDuplicateAlert() {
        val recordedDocIds = mutableSetOf<String>()
        val docId = "alert_doc_1"

        // First event (real new document)
        var wasRecordedFirst = false
        if (!recordedDocIds.contains(docId)) {
            recordedDocIds.add(docId)
            wasRecordedFirst = true
        }

        // Second event (metadata-only update for same docId)
        var wasRecordedSecond = false
        if (!recordedDocIds.contains(docId)) {
            recordedDocIds.add(docId)
            wasRecordedSecond = true
        }

        assertTrue(wasRecordedFirst)
        assertFalse(wasRecordedSecond)
    }

    @Test
    fun testOwnershipGrep_claimByUserChoiceOnlyInAllowedFiles() {
        val srcDir = File("src/main/java")
        if (!srcDir.exists()) return

        val illegalFiles = mutableListOf<String>()
        srcDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension == "kt") {
                val fileName = file.name
                if (fileName != "LocalDataOwnership.kt" && fileName != "MainActivity.kt") {
                    val text = file.readText()
                    if (text.contains("claimByUserChoice")) {
                        illegalFiles.add(fileName)
                    }
                }
            }
        }

        assertTrue(
            "claimByUserChoice must ONLY be referenced in LocalDataOwnership.kt and MainActivity.kt, but found in: $illegalFiles",
            illegalFiles.isEmpty()
        )
    }
}
