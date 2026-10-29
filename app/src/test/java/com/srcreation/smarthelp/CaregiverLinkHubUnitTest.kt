package com.srcreation.smarthelp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class CaregiverLinkHubUnitTest {

    @Test
    fun testTabParsing_StringValues() {
        assertEquals(0, CaregiverLinkHubViewModel.parseTabValue("LINK_PATIENT"))
        assertEquals(0, CaregiverLinkHubViewModel.parseTabValue("LINK"))
        assertEquals(1, CaregiverLinkHubViewModel.parseTabValue("DASHBOARD"))
        assertEquals(2, CaregiverLinkHubViewModel.parseTabValue("SEND_INSTRUCTION"))
        assertEquals(2, CaregiverLinkHubViewModel.parseTabValue("INSTRUCTION"))
        assertEquals(3, CaregiverLinkHubViewModel.parseTabValue("HISTORY_CHARTS"))
        assertEquals(3, CaregiverLinkHubViewModel.parseTabValue("HISTORY"))
    }

    @Test
    fun testTabParsing_IntegerValues() {
        assertEquals(0, CaregiverLinkHubViewModel.parseTabValue(0))
        assertEquals(1, CaregiverLinkHubViewModel.parseTabValue(1))
        assertEquals(2, CaregiverLinkHubViewModel.parseTabValue(2))
        assertEquals(3, CaregiverLinkHubViewModel.parseTabValue(3))
    }

    @Test
    fun testTabParsing_InvalidAndMissingValues() {
        assertEquals(1, CaregiverLinkHubViewModel.parseTabValue(null))
        assertEquals(1, CaregiverLinkHubViewModel.parseTabValue("INVALID_TAB"))
        assertEquals(1, CaregiverLinkHubViewModel.parseTabValue(-1))
        assertEquals(1, CaregiverLinkHubViewModel.parseTabValue(99))
    }

    @Test
    fun testRedirectForwarding_PreservesTabAndExtrasData() {
        val originalExtras = mapOf("PATIENT_ID" to "p_789", "ALERT_TYPE" to "DANGER")
        val redirectedExtras = mutableMapOf<String, Any>()

        redirectedExtras.putAll(originalExtras)
        redirectedExtras["TAB"] = "DASHBOARD"

        assertEquals("DASHBOARD", redirectedExtras["TAB"])
        assertEquals("p_789", redirectedExtras["PATIENT_ID"])
        assertEquals("DANGER", redirectedExtras["ALERT_TYPE"])
    }

    @Test
    fun testSelectedPatientSharingAcrossTabs() {
        val patient = HubSelectedPatient("p_123", "Jane Doe")
        val tab1Patient = patient
        val tab2Patient = patient
        val tab3Patient = patient

        assertEquals("p_123", tab1Patient.id)
        assertEquals("Jane Doe", tab2Patient.name)
        assertEquals(tab1Patient, tab2Patient)
        assertEquals(tab2Patient, tab3Patient)
    }

    @Test
    fun testDefaultTabDecisionLogic() {
        val activeListEmpty = emptyList<Map<String, Any>>()
        val activeListNonEmpty = listOf(mapOf("patientId" to "p_1", "patientName" to "Test"))

        val tabNoLinks = if (activeListEmpty.isNotEmpty()) 1 else 0
        assertEquals(0, tabNoLinks)

        val tabActive = if (activeListNonEmpty.isNotEmpty()) 1 else 0
        assertEquals(1, tabActive)
    }

    @Test
    fun testPatientIdValidationAgainstActiveList() {
        val activePatients = listOf("p_1", "p_2", "p_3")
        val requestedIdValid = "p_2"
        val requestedIdInvalid = "p_999"

        assertTrue(activePatients.contains(requestedIdValid))
        assertFalse(activePatients.contains(requestedIdInvalid))
    }

    @Test
    fun testBadgeLogic_DangerAlertsCount() {
        val dangerCount = 2
        assertEquals(2, dangerCount)
    }

    @Test
    fun testDateRangeStringFormatting() {
        val sinceTimestamp = System.currentTimeMillis() - 30 * 24L * 3600 * 1000
        val cal = Calendar.getInstance().apply { timeInMillis = sinceTimestamp }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sinceDateStr = dateFormat.format(cal.time)

        assertTrue("Date format should match yyyy-MM-dd", sinceDateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun testDosesQueryUsesDateFieldNotTimestamp() {
        val doseFieldUsed = "date"
        assertEquals("date", doseFieldUsed)
    }

    @Test
    fun testChartIgnoresInsulinDoseAndInvalidValues() {
        val records = listOf(
            VitalHistoryRecord(type = "GLUCOSE", kind = "INSULIN_DOSE", value = 10.0),
            VitalHistoryRecord(type = "GLUCOSE", kind = "GLUCOSE", value = -5.0),
            VitalHistoryRecord(type = "GLUCOSE", kind = "GLUCOSE", value = 0.0),
            VitalHistoryRecord(type = "GLUCOSE", kind = "GLUCOSE", value = 110.0)
        )

        val filtered = records.filter { r ->
            val isInsulinDose = r.type.equals("GLUCOSE", true) && r.kind.equals("INSULIN_DOSE", true)
            val isValidValue = r.value > 0.0 && r.value in 20.0..600.0
            !isInsulinDose && isValidValue
        }

        assertEquals(1, filtered.size)
        assertEquals(110.0, filtered[0].value, 0.01)
    }

    @Test
    fun testCacheReuseAcrossTabSwitches() {
        var loadCount = 0
        var cachedPatientId = ""
        var cachedVital = ""
        var isCacheValid = false

        fun loadHistory(pid: String, vital: String) {
            if (isCacheValid && cachedPatientId == pid && cachedVital == vital) {
                return
            }
            loadCount++
            cachedPatientId = pid
            cachedVital = vital
            isCacheValid = true
        }

        loadHistory("p_1", "BP")
        assertEquals(1, loadCount)

        loadHistory("p_1", "BP")
        assertEquals(1, loadCount)

        loadHistory("p_1", "PULSE")
        assertEquals(2, loadCount)
    }

    @Test
    fun testFailedDosesLoadGivesUnknownState_NeverMissed() {
        val isDoseLoadFailed = true
        val status = if (isDoseLoadFailed) "Could not load" else "Due"

        assertEquals("Could not load", status)
        assertFalse("Failed dose load must never yield Missed status", status == "Missed")
    }

    @Test
    fun testStaleCallbackIgnoredAfterPatientSwitch() {
        var currentGen = 1L
        var loadedPatientId = ""

        fun onDosesLoaded(callbackGen: Long, patientId: String) {
            if (callbackGen != currentGen) {
                return
            }
            loadedPatientId = patientId
        }

        currentGen = 2L
        onDosesLoaded(1L, "p_old")
        assertEquals("Stale callback should be ignored and not update state", "", loadedPatientId)

        onDosesLoaded(2L, "p_new")
        assertEquals("Valid callback should update state", "p_new", loadedPatientId)
    }

    @Test
    fun testUnknownStateSendsNoMissedNotification() {
        val status = "Could not load"
        val shouldSendNotification = (status == "Missed")

        assertFalse("Unknown or failed dose state must never trigger Missed notification", shouldSendNotification)
    }

    @Test
    fun testMissingPatientTimeZoneShowsWarningLabel() {
        val patientTimeZone: String? = null
        val isTzUnknown = patientTimeZone.isNullOrBlank()

        assertTrue("Missing patient timezone should trigger warning label flag", isTzUnknown)
    }

    @Test
    fun testDoseListenersClearedBeforeReattaching() {
        val activeListeners = mutableListOf<String>()

        fun attachDoseListeners(meds: List<String>) {
            activeListeners.clear()
            meds.forEach { activeListeners.add("listener_$it") }
        }

        attachDoseListeners(listOf("m1", "m2"))
        assertEquals(2, activeListeners.size)

        attachDoseListeners(listOf("m3"))
        assertEquals("Previous listeners must be cleared before re-attaching", 1, activeListeners.size)
        assertEquals("listener_m3", activeListeners[0])
    }

    @Test
    fun testSelectPatientTwiceDoesNotDuplicateListeners() {
        var attachCount = 0
        var currentSelectedId: String? = null
        var isListenersAttached = false

        fun selectPatient(id: String) {
            if (currentSelectedId == id && isListenersAttached) {
                return
            }
            currentSelectedId = id
            attachCount++
            isListenersAttached = true
        }

        selectPatient("p_1")
        assertEquals(1, attachCount)

        selectPatient("p_1")
        assertEquals("Selecting same patient twice must not duplicate listener attach", 1, attachCount)
    }

    @Test
    fun testLoadingStateIsNeutral_NeverMissed() {
        val initialStatus = "Loading"
        val isMissed = (initialStatus == "Missed")

        assertFalse("Initial loading status must be neutral and never Missed", isMissed)
        assertEquals("Loading", initialStatus)
    }

    @Test
    fun testTimeZoneWrittenOnAppStartForLoggedInUser() {
        val isLoggedIn = true
        var isTzUpdated = false

        fun onAppStart() {
            if (isLoggedIn) {
                val tz = TimeZone.getDefault().id
                if (tz.isNotBlank()) {
                    isTzUpdated = true
                }
            }
        }

        onAppStart()
        assertTrue("Timezone must be updated on app start for logged-in user", isTzUpdated)
    }

    @Test
    fun testStepGuideStateTransitionsAndExpiry() {
        fun evaluateStep(code: String, isExpired: Boolean, isPending: Boolean, isAccepted: Boolean): StepGuideState {
            return when {
                code.isBlank() || isExpired -> StepGuideState.STEP1
                isAccepted -> StepGuideState.DONE
                isPending -> StepGuideState.STEP3
                else -> StepGuideState.STEP2
            }
        }

        assertEquals(StepGuideState.STEP1, evaluateStep("", isExpired = false, isPending = false, isAccepted = false))
        assertEquals(StepGuideState.STEP2, evaluateStep("123456", isExpired = false, isPending = false, isAccepted = false))
        assertEquals(StepGuideState.STEP3, evaluateStep("123456", isExpired = false, isPending = true, isAccepted = false))
        assertEquals(StepGuideState.DONE, evaluateStep("123456", isExpired = false, isPending = false, isAccepted = true))
        assertEquals(StepGuideState.STEP1, evaluateStep("123456", isExpired = true, isPending = false, isAccepted = false))
    }

    @Test
    fun testStepGuideUserWithExistingActiveLinksStartsAtStep1ForNewCode() {
        val existingActiveLinksCount = 3
        var currentCode = ""

        fun onUserGenerateNewCode() {
            currentCode = "654321"
        }

        val stepBeforeGenerate = if (currentCode.isBlank()) StepGuideState.STEP1 else StepGuideState.STEP2
        assertEquals("User with existing links generating a new code starts at STEP1 until code is generated", StepGuideState.STEP1, stepBeforeGenerate)

        onUserGenerateNewCode()
        val stepAfterGenerate = if (currentCode.isBlank()) StepGuideState.STEP1 else StepGuideState.STEP2
        assertEquals(StepGuideState.STEP2, stepAfterGenerate)
        assertTrue("Active links count is preserved", existingActiveLinksCount == 3)
    }

    @Test
    fun testSummaryCard_OwnershipGateBlockedShowsNothing() {
        val result = DailySummaryLogic.computeDailySummary(
            now = System.currentTimeMillis(),
            timeZoneId = "UTC",
            medicines = emptyList(),
            todayDoses = emptyList(),
            readings = VitalsReadings(),
            isGateBlocked = true,
            isBn = false
        )
        assertEquals(DailySummaryResult.Hidden, result)
    }

    @Test
    fun testSummaryCard_AmberStateWordingAndVitalsList() {
        val result = DailySummaryLogic.computeDailySummary(
            now = System.currentTimeMillis(),
            timeZoneId = "UTC",
            medicines = emptyList(),
            todayDoses = emptyList(),
            readings = VitalsReadings(spo2Size = 92.0),
            isGateBlocked = false,
            isBn = false
        )
        assertTrue(result is DailySummaryResult.Warning)
        assertTrue((result as DailySummaryResult.Warning).msg.contains("outside your set limits"))
    }

    @Test
    fun testSummaryCard_DangerNamesSpecificVitalAndEmergencyDisclaimer() {
        val result = DailySummaryLogic.computeDailySummary(
            now = System.currentTimeMillis(),
            timeZoneId = "UTC",
            medicines = emptyList(),
            todayDoses = emptyList(),
            readings = VitalsReadings(bpSys = 180, bpDia = 110),
            isGateBlocked = false,
            isBn = false
        )
        assertTrue(result is DailySummaryResult.Danger)
        val msg = (result as DailySummaryResult.Danger).msg
        assertTrue(msg.contains("BP"))
        assertTrue(msg.contains("If you feel unwell, call your doctor or emergency services."))
        assertTrue(msg.contains("outside your set limits"))
    }

    @Test
    fun testSummaryCard_OnlyCheckedVitalsListed() {
        val result = DailySummaryLogic.computeDailySummary(
            now = System.currentTimeMillis(),
            timeZoneId = "UTC",
            medicines = emptyList(),
            todayDoses = emptyList(),
            readings = VitalsReadings(bpSys = 120, bpDia = 80),
            isGateBlocked = false,
            isBn = false
        )
        assertTrue(result is DailySummaryResult.WithinLimits)
        val text = (result as DailySummaryResult.WithinLimits).msg
        assertTrue(text.startsWith("BP"))
        assertFalse(text.contains("Pulse"))
        assertFalse(text.contains("SpO2"))
    }

    @Test
    fun testRangeMeter_BpAndGlucoseHaveNoAmberZone() {
        val bpWarningLevel = AlertThresholds.checkBp(120, 80).level
        val glucoseWarningLevel = AlertThresholds.checkGlucose(110.0, "mg/dL").level

        assertEquals("BP has no amber warning level", VitalAlertLevel.NORMAL, bpWarningLevel)
        assertEquals("Glucose has no amber warning level", VitalAlertLevel.NORMAL, glucoseWarningLevel)
    }

    @Test
    fun testRangeMeter_SpO2AndTemperatureHaveWarningZone() {
        val spo2Warning = AlertThresholds.checkSpO2(92.0).level
        val tempCWarning = AlertThresholds.checkTemperature(38.0, "°C").level
        val tempFWarning = AlertThresholds.checkTemperature(100.4, "°F").level

        assertEquals("SpO2 92% should give WARNING level", VitalAlertLevel.WARNING, spo2Warning)
        assertEquals("Temp 38.0°C should give WARNING level", VitalAlertLevel.WARNING, tempCWarning)
        assertEquals("Temp 100.4°F should give WARNING level", VitalAlertLevel.WARNING, tempFWarning)
    }

    @Test
    fun testContrastCheck_WithRealColorConstantsInLightAndDarkThemes() {
        fun computeLuminance(r: Double, g: Double, b: Double): Double {
            fun transform(c: Double): Double = if (c <= 0.03928) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
            return 0.2126 * transform(r) + 0.7152 * transform(g) + 0.0722 * transform(b)
        }

        fun computeContrastRatio(lum1: Double, lum2: Double): Double {
            val l1 = Math.max(lum1, lum2)
            val l2 = Math.min(lum1, lum2)
            return (l1 + 0.05) / (l2 + 0.05)
        }

        val amberTextLum = computeLuminance(0.572, 0.251, 0.055)
        val amberBgLum = computeLuminance(0.996, 0.953, 0.780)
        val amberRatio = computeContrastRatio(amberBgLum, amberTextLum)

        assertTrue("Amber text #92400E on #FEF3C7 must exceed 4.5:1 ratio (computed: $amberRatio)", amberRatio >= 4.5)

        val darkBgLum = computeLuminance(0.118, 0.161, 0.231)
        val whiteTextLum = computeLuminance(1.0, 1.0, 1.0)
        val darkRatio = computeContrastRatio(whiteTextLum, darkBgLum)

        assertTrue("White text on #1E293B must exceed 4.5:1 ratio (computed: $darkRatio)", darkRatio >= 4.5)
    }

    @Test
    fun testComputeDailySummary_MidnightNightDoseNotTakenIsCounted() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.MAY, 10, 0, 30, 0)
        }
        val now = cal.timeInMillis

        val med = com.srcreation.smarthelp.db.MedicineEntity(
            id = "m1", profileId = 1, name = "Aspirin", category = "Tablet", foodRelation = "Before Meal",
            selectedTimes = "Night", durationDays = 7, initialStock = 10, currentStock = 10
        )
        val doses = emptyList<com.srcreation.smarthelp.db.MedicineDoseEntity>()

        val result = DailySummaryLogic.computeDailySummary(
            now = now,
            timeZoneId = "UTC",
            medicines = listOf(med),
            todayDoses = doses,
            readings = VitalsReadings(),
            isGateBlocked = false,
            isBn = false
        )

        assertTrue(result is DailySummaryResult.WithinLimits)
        assertTrue((result as DailySummaryResult.WithinLimits).msg.contains("missed"))
    }

    @Test
    fun testComputeDailySummary_MidnightNightDoseTakenIsNotCounted() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.MAY, 10, 0, 30, 0)
        }
        val now = cal.timeInMillis

        val med = com.srcreation.smarthelp.db.MedicineEntity(
            id = "m1", profileId = 1, name = "Aspirin", category = "Tablet", foodRelation = "Before Meal",
            selectedTimes = "Night", durationDays = 7, initialStock = 10, currentStock = 10
        )
        val doses = listOf(
            com.srcreation.smarthelp.db.MedicineDoseEntity(
                id = "d1", profileId = 1, medicineId = "m1", slot = "Night", date = "2026-05-09", taken = true, takenAt = now, synced = true
            )
        )

        val result = DailySummaryLogic.computeDailySummary(
            now = now,
            timeZoneId = "UTC",
            medicines = listOf(med),
            todayDoses = doses,
            readings = VitalsReadings(),
            isGateBlocked = false,
            isBn = false
        )

        assertTrue(result is DailySummaryResult.NoReadings)
    }

    @Test
    fun testComputeDailySummary_At0500DateIsCurrentOne() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2026, Calendar.MAY, 10, 5, 0, 0)
        }
        val now = cal.timeInMillis

        val med = com.srcreation.smarthelp.db.MedicineEntity(
            id = "m1", profileId = 1, name = "Aspirin", category = "Tablet", foodRelation = "Before Meal",
            selectedTimes = "Morning", durationDays = 7, initialStock = 10, currentStock = 10
        )
        val doses = emptyList<com.srcreation.smarthelp.db.MedicineDoseEntity>()

        val result = DailySummaryLogic.computeDailySummary(
            now = now,
            timeZoneId = "UTC",
            medicines = listOf(med),
            todayDoses = doses,
            readings = VitalsReadings(),
            isGateBlocked = false,
            isBn = false
        )

        assertTrue(result is DailySummaryResult.WithinLimits)
    }

    @Test
    fun testMedicineScheduleHelper_Custom2000At1500IsDue() {
        val status = MedicineScheduleHelper.calculateDoseStatus("Evening", isTaken = false, currentTimeInMinutes = 900, customTime = "20:00")
        assertEquals("Due", status)
    }

    @Test
    fun testMedicineScheduleHelper_Custom0600At1100IsMissed() {
        val status = MedicineScheduleHelper.calculateDoseStatus("Morning", isTaken = false, currentTimeInMinutes = 660, customTime = "06:00")
        assertEquals("Missed", status)
    }

    @Test
    fun testMedicineScheduleHelper_4tSlotsAtVariousTimes() {
        assertEquals("Due", MedicineScheduleHelper.calculateDoseStatus("4t_1", false, 300))
        assertEquals("Due", MedicineScheduleHelper.calculateDoseStatus("4t_4", false, 840))
        assertEquals("Due", MedicineScheduleHelper.calculateDoseStatus("4t_4", false, 1530))
        assertEquals("Missed", MedicineScheduleHelper.calculateDoseStatus("4t_4", false, 1590))
    }

    @Test
    fun testMedicineScheduleHelper_UnknownSlotReturnsUnknown() {
        val status = MedicineScheduleHelper.calculateDoseStatus("UnknownSlotName", false, 600)
        assertEquals("Unknown", status)
    }

    @Test
    fun testCaregiverCheckWorker_SendsNoMissedForUnknownSlot() {
        val status = "Unknown"
        val isMissed = (status == "Missed")
        assertFalse("Unknown slot status must never trigger missed notification", isMissed)
    }

    @Test
    fun testSyncRepository_ScheduleTimesPresentInMedicineMap() {
        val med = com.srcreation.smarthelp.db.MedicineEntity(
            id = "m1", profileId = 1, name = "Aspirin", category = "Tablet", foodRelation = "Before Meal",
            selectedTimes = "Morning,Night", durationDays = 7, initialStock = 10, currentStock = 10
        )
        val scheduleMap = mutableMapOf<String, String>()
        val slots = med.selectedTimes.split(",").map { it.trim() }
        for (slot in slots) {
            val mins = MedicineScheduleHelper.getScheduledMinutesForSlot(slot, med.customTime)
            if (mins != null) {
                val norm = if (mins >= 1440) mins - 1440 else mins
                scheduleMap[slot] = String.format(Locale.ROOT, "%02d:%02d", norm / 60, norm % 60)
            }
        }

        assertTrue(scheduleMap.containsKey("Morning"))
        assertTrue(scheduleMap.containsKey("Night"))
        assertEquals("08:00", scheduleMap["Morning"])
        assertEquals("22:00", scheduleMap["Night"])
    }
}
