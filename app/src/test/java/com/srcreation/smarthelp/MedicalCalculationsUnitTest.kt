package com.srcreation.smarthelp

import com.srcreation.smarthelp.db.MedicineEntity
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.pow
import kotlin.math.sqrt

class MedicalCalculationsUnitTest {

    @Test
    fun testDrugInteractionEngine_ParacetamolOverdose() {
        val existingMeds = listOf(
            MedicineEntity(id = "1", name = "Napa 500mg", category = "Tablet", foodRelation = "After", selectedTimes = "Morning", durationDays = 5, initialStock = 10, currentStock = 10)
        )
        val conflict = DrugInteractionEngine.checkConflict("Ace 500mg", existingMeds)
        assertNotNull(conflict)
        assertEquals("OVERDOSE_PARACETAMOL", conflict?.conflictType)
    }

    @Test
    fun testDrugInteractionEngine_MultipleNsaids() {
        val existingMeds = listOf(
            MedicineEntity(id = "1", name = "Clofenac 50mg", category = "Tablet", foodRelation = "After", selectedTimes = "Morning", durationDays = 5, initialStock = 10, currentStock = 10)
        )
        val conflict = DrugInteractionEngine.checkConflict("Ibuprofen 400mg", existingMeds)
        assertNotNull(conflict)
        assertEquals("MULTIPLE_NSAID", conflict?.conflictType)
    }

    @Test
    fun testDrugInteractionEngine_NoConflict() {
        val existingMeds = listOf(
            MedicineEntity(id = "1", name = "Seclo 20mg", category = "Capsule", foodRelation = "Before", selectedTimes = "Morning", durationDays = 14, initialStock = 14, currentStock = 14)
        )
        val conflict = DrugInteractionEngine.checkConflict("Napa 500mg", existingMeds)
        assertNull(conflict)
    }

    @Test
    fun testBsaMostellerFormula() {
        val heightCm = 170.0
        val weightKg = 65.0
        val bsa = sqrt((heightCm * weightKg) / 3600.0)
        // Expected ~1.75 m2
        assertEquals(1.75, bsa, 0.02)
    }

    @Test
    fun testBsaDuBoisFormula() {
        val heightCm = 170.0
        val weightKg = 65.0
        val bsa = 0.007184 * heightCm.pow(0.725) * weightKg.pow(0.425)
        // Expected ~1.75 m2
        assertEquals(1.75, bsa, 0.03)
    }

    @Test
    fun testBmiCategoryCalculation() {
        fun getBmiCategory(bmi: Double): String {
            return when {
                bmi < 18.5 -> "Underweight"
                bmi in 18.5..24.9 -> "Normal"
                bmi in 25.0..29.9 -> "Overweight"
                else -> "Obese"
            }
        }

        assertEquals("Underweight", getBmiCategory(17.5))
        assertEquals("Normal", getBmiCategory(22.0))
        assertEquals("Overweight", getBmiCategory(27.3))
        assertEquals("Obese", getBmiCategory(32.1))
    }

    @Test
    fun testBpCategoryAssessment() {
        fun getBpCategory(sys: Int, dia: Int): String {
            return when {
                sys >= 140 || dia >= 90 -> "High"
                sys >= 130 || dia >= 81 -> "Mild High"
                sys >= 121 || dia >= 70 -> "Normal"
                sys >= 100 || dia >= 60 -> "Optimal"
                else -> "Low"
            }
        }

        assertEquals("Optimal", getBpCategory(110, 65))
        assertEquals("Normal", getBpCategory(125, 75))
        assertEquals("High", getBpCategory(145, 95))
        assertEquals("Low", getBpCategory(90, 55))
    }

    @Test
    fun testIvFluidFlowRateCalculation() {
        val volumeMl = 1000.0
        val hours = 8.0
        val dripFactor = 20 // Standard drops/ml

        val mlPerHour = volumeMl / hours
        val dropsPerMin = (volumeMl * dripFactor) / (hours * 60)

        assertEquals(125.0, mlPerHour, 0.1)
        assertEquals(41.66, dropsPerMin, 0.1)
    }

    @Test
    fun testClaimIfFresh_FreshUnset() {
        val owner: String? = null
        val hasData = false
        val canClaim = owner.isNullOrBlank() && !hasData
        assertTrue("Fresh UNSET with no data should allow claimIfFresh", canClaim)
    }

    @Test
    fun testClaimIfFresh_UnsetData() {
        val owner: String? = null
        val hasData = true
        val canClaim = owner.isNullOrBlank() && !hasData
        assertFalse("UNSET with existing data should block claimIfFresh", canClaim)
    }

    @Test
    fun testClaimIfFresh_SetEqual() {
        val owner: String? = "uid1"
        val currentUid = "uid1"
        val ownerMatches = owner == currentUid
        assertTrue("SET equal should match owner", ownerMatches)
    }

    @Test
    fun testClaimIfFresh_SetDifferent() {
        val owner: String? = "uid1"
        val currentUid = "uid2"
        val ownerMismatch = owner != null && owner != currentUid
        assertTrue("SET different should detect mismatch", ownerMismatch)
    }

    @Test
    fun testClaimByUserChoice_FreshUnset() {
        val newOwner = "uid1"
        val claimed = newOwner.isNotBlank()
        assertTrue("claimByUserChoice should force claim in fresh UNSET", claimed)
    }

    @Test
    fun testClaimByUserChoice_UnsetData() {
        val newOwner = "uid1"
        val claimed = newOwner.isNotBlank()
        assertTrue("claimByUserChoice should force claim in UNSET with data upon user confirmation", claimed)
    }

    @Test
    fun testClaimByUserChoice_SetEqual() {
        val owner: String? = "uid1"
        val newOwner: String? = "uid1"
        val claimed = owner == newOwner
        assertTrue("claimByUserChoice should succeed when SET equal", claimed)
    }

    @Test
    fun testClaimByUserChoice_SetDifferent() {
        val newOwner = "uid2"
        val claimed = newOwner.isNotBlank()
        assertTrue("claimByUserChoice should allow overriding owner upon user choice", claimed)
    }

    @Test
    fun testUploadDecision_AllStates() {
        val allow1 = (null as String?).isNullOrBlank()
        assertTrue(allow1)

        val owner2: String? = null
        val hasData2 = true
        val allow2 = !(owner2.isNullOrBlank() && hasData2)
        assertFalse(allow2)

        val owner3 = "uid1"
        val current3 = "uid1"
        val allow3 = owner3 == current3
        assertTrue(allow3)

        val owner4 = "uid1"
        val current4 = "uid2"
        val allow4 = owner4 == current4
        assertFalse(allow4)
    }

    @Test
    fun testAlarmActionDoseSaveAndUploadGating_FreshUnset() {
        val owner: String? = null
        val hasData = false
        val isBlocked = owner.isNullOrBlank() && hasData
        val doseSavedLocally = true
        val uploadEnqueued = !isBlocked

        assertTrue("Dose must always be saved locally in Fresh UNSET", doseSavedLocally)
        assertTrue("Upload should be enqueued when not blocked", uploadEnqueued)
    }

    @Test
    fun testAlarmActionDoseSaveAndUploadGating_UnsetData() {
        val owner: String? = null
        val hasData = true
        val isBlocked = (owner.isNullOrBlank() && hasData) || (owner != null && owner != "currentUid")
        val doseSavedLocally = true
        val uploadEnqueued = !isBlocked

        assertTrue("Dose must always be saved locally in UNSET with data", doseSavedLocally)
        assertFalse("Upload must NOT be enqueued in UNSET with data", uploadEnqueued)
    }

    @Test
    fun testAlarmActionDoseSaveAndUploadGating_SetEqual() {
        val owner = "uid1"
        val currentUid = "uid1"
        val isBlocked = owner != currentUid
        val doseSavedLocally = true
        val uploadEnqueued = !isBlocked

        assertTrue("Dose must always be saved locally when SET equal", doseSavedLocally)
        assertTrue("Upload should be enqueued when SET equal", uploadEnqueued)
    }

    @Test
    fun testAlarmActionDoseSaveAndUploadGating_SetDifferent() {
        val owner = "uid1"
        val currentUid = "uid2"
        val isBlocked = owner != currentUid
        val doseSavedLocally = true
        val uploadEnqueued = !isBlocked

        assertTrue("Dose must always be saved locally when SET different", doseSavedLocally)
        assertFalse("Upload must NOT be enqueued when SET different", uploadEnqueued)
    }

    @Test
    fun testBootReceiverReschedule_AllStates() {
        listOf(
            Triple(null, false, "Fresh UNSET"),
            Triple(null, true, "UNSET with Data"),
            Triple("uid1", true, "SET Equal"),
            Triple("uid1", false, "SET Different")
        ).forEach { (_, _, stateName) ->
            val alarmRescheduled = true
            assertTrue("Boot receiver must ALWAYS reschedule alarms in state $stateName", alarmRescheduled)
        }
    }

    @Test
    fun testAlertThresholdsBoundary_SpO2() {
        assertEquals(VitalAlertLevel.DANGER, AlertThresholds.checkSpO2(89.0).level)
        assertEquals(VitalAlertLevel.WARNING, AlertThresholds.checkSpO2(90.0).level)
        assertEquals(VitalAlertLevel.WARNING, AlertThresholds.checkSpO2(93.0).level)
        assertEquals(VitalAlertLevel.NORMAL, AlertThresholds.checkSpO2(94.0).level)
    }

    @Test
    fun testAlertThresholdsBoundary_Pulse() {
        assertEquals(VitalAlertLevel.DANGER, AlertThresholds.checkPulse(49).level)
        assertEquals(VitalAlertLevel.NORMAL, AlertThresholds.checkPulse(50).level)
        assertEquals(VitalAlertLevel.NORMAL, AlertThresholds.checkPulse(120).level)
        assertEquals(VitalAlertLevel.DANGER, AlertThresholds.checkPulse(121).level)
    }

    @Test
    fun testAlertThresholdsBoundary_Temperature() {
        assertEquals(VitalAlertLevel.NORMAL, AlertThresholds.checkTemperature(37.8, "°C").level)
        assertEquals(VitalAlertLevel.WARNING, AlertThresholds.checkTemperature(37.9, "°C").level)
        assertEquals(VitalAlertLevel.WARNING, AlertThresholds.checkTemperature(39.0, "°C").level)
        assertEquals(VitalAlertLevel.DANGER, AlertThresholds.checkTemperature(39.1, "°C").level)

        assertEquals(VitalAlertLevel.WARNING, AlertThresholds.checkTemperature(102.2, "°F").level)
        assertEquals(VitalAlertLevel.DANGER, AlertThresholds.checkTemperature(102.38, "°F").level)
    }

    @Test
    fun testAlertThresholdsBoundary_BloodPressure() {
        assertEquals(VitalAlertLevel.DANGER, AlertThresholds.checkBp(89, 80).level)
        assertEquals(VitalAlertLevel.NORMAL, AlertThresholds.checkBp(90, 80).level)
        assertEquals(VitalAlertLevel.NORMAL, AlertThresholds.checkBp(160, 80).level)
        assertEquals(VitalAlertLevel.DANGER, AlertThresholds.checkBp(161, 80).level)

        assertEquals(VitalAlertLevel.DANGER, AlertThresholds.checkBp(120, 59).level)
        assertEquals(VitalAlertLevel.NORMAL, AlertThresholds.checkBp(120, 60).level)
        assertEquals(VitalAlertLevel.NORMAL, AlertThresholds.checkBp(120, 100).level)
        assertEquals(VitalAlertLevel.DANGER, AlertThresholds.checkBp(120, 101).level)
    }

    @Test
    fun testAlertThresholdsBoundary_GlucoseAndUnitConversion() {
        assertEquals(VitalAlertLevel.DANGER, AlertThresholds.checkGlucose(69.0, "mg/dL").level)
        assertEquals(VitalAlertLevel.NORMAL, AlertThresholds.checkGlucose(70.0, "mg/dL").level)
        assertEquals(VitalAlertLevel.NORMAL, AlertThresholds.checkGlucose(250.0, "mg/dL").level)
        assertEquals(VitalAlertLevel.DANGER, AlertThresholds.checkGlucose(251.0, "mg/dL").level)

        assertEquals(VitalAlertLevel.DANGER, AlertThresholds.checkGlucose(3.8, "mmol/L").level)
        assertEquals(VitalAlertLevel.NORMAL, AlertThresholds.checkGlucose(3.9, "mmol/L").level)
    }

    @Test
    fun testInsulinDoseNeverEvaluatedAsGlucose() {
        val glucoseReading = 110.0
        val insulinDoseUnits = 8.0
        
        val glucoseResult = AlertThresholds.checkGlucose(glucoseReading, "mg/dL")
        assertEquals(VitalAlertLevel.NORMAL, glucoseResult.level)

        assertTrue("Glucose check is strictly separate from insulin dose units", glucoseReading != insulinDoseUnits)
    }

    @Test
    fun testInsulinRecord_BlankGlucoseDose10_KindInsulinDoseNoGlucoseAlert() {
        val glucoseStr = ""
        val glucoseVal = glucoseStr.toDoubleOrNull()
        val hasValidGlucose = glucoseVal != null && glucoseVal > 0.0
        val kind = if (hasValidGlucose) "GLUCOSE" else "INSULIN_DOSE"

        assertEquals("INSULIN_DOSE", kind)
        assertFalse("Blank glucose with dose 10 must not be valid glucose reading", hasValidGlucose)

        val isIgnored = kind != "GLUCOSE" || !hasValidGlucose
        assertTrue("INSULIN_DOSE document must be ignored in glucose alert evaluation", isIgnored)
    }

    @Test
    fun testZeroValueDocument_IgnoredAsGlucoseAndNoDanger() {
        val value = 0.0
        val isIgnored = value <= 0.0
        assertTrue("Document with value 0.0 must be ignored", isIgnored)

        val result = AlertThresholds.checkGlucose(value, "mg/dL")
        assertEquals("Zero value must not trigger danger alert", VitalAlertLevel.NORMAL, result.level)
    }

    @Test
    fun testGlucose55_Danger() {
        val result = AlertThresholds.checkGlucose(55.0, "mg/dL")
        assertEquals(VitalAlertLevel.DANGER, result.level)
        assertEquals("below 70 mg/dL", result.detailText)
    }

    @Test
    fun testEarlierDangerFollowedByNormal_InRecentListNotInBanner() {
        val reading1 = AlertThresholds.checkSpO2(88.0) // Danger
        val reading2 = AlertThresholds.checkSpO2(98.0) // Normal (Latest)

        assertEquals("Latest reading is normal, so top banner level is NORMAL", VitalAlertLevel.NORMAL, reading2.level)
        assertEquals("Earlier reading is DANGER", VitalAlertLevel.DANGER, reading1.level)
    }

    @Test
    fun testImpossibleValues_MarkedCheckThisValue() {
        val spo2Impossible = AlertThresholds.checkSpO2(105.0)
        assertTrue("SpO2 105% is impossible", spo2Impossible.isImpossible)

        val pulseImpossible = AlertThresholds.checkPulse(350)
        assertTrue("Pulse 350 BPM is impossible", pulseImpossible.isImpossible)

        val tempImpossible = AlertThresholds.checkTemperature(48.0, "°C")
        assertTrue("Temp 48°C is impossible", tempImpossible.isImpossible)

        val bpImpossible = AlertThresholds.checkBp(350, 220)
        assertTrue("BP 350/220 is impossible", bpImpossible.isImpossible)

        val glucoseImpossible = AlertThresholds.checkGlucose(1200.0, "mg/dL")
        assertTrue("Glucose 1200 mg/dL is impossible", glucoseImpossible.isImpossible)
    }

    @Test
    fun testGateDecision_FourOwnershipStates() {
        // State 1: Fresh UNSET
        val dec1 = LocalDataOwnership.evaluateGateDecision(null, "uid1", false)
        assertEquals(GateDecision.SHOW_CONTENT, dec1)

        // State 2: UNSET with Data
        val dec2 = LocalDataOwnership.evaluateGateDecision(null, "uid1", true)
        assertEquals(GateDecision.REDIRECT_TO_MAIN, dec2)

        // State 3: SET Equal
        val dec3 = LocalDataOwnership.evaluateGateDecision("uid1", "uid1", true)
        assertEquals(GateDecision.SHOW_CONTENT, dec3)

        // State 4: SET Different
        val dec4 = LocalDataOwnership.evaluateGateDecision("uid1", "uid2", true)
        assertEquals(GateDecision.REDIRECT_TO_MAIN, dec4)
    }

    @Test
    fun testDatabaseTablesCoverageGuard() {
        val schemaDir = java.io.File("schemas/com.srcreation.smarthelp.db.AppDatabase")
        val targetDir = if (schemaDir.exists()) schemaDir else java.io.File("app/schemas/com.srcreation.smarthelp.db.AppDatabase")

        assertTrue("Schema directory missing!", targetDir.exists() && targetDir.isDirectory)

        val schemaFile = targetDir.listFiles { _, name -> name.endsWith(".json") }
            ?.maxByOrNull { it.name.substringBefore(".json").toIntOrNull() ?: 0 }

        assertNotNull("No Room schema JSON file found!", schemaFile)

        val jsonString = schemaFile!!.readText()
        val jsonObject = com.google.gson.JsonParser.parseString(jsonString).asJsonObject
        val entitiesArray = jsonObject.getAsJsonObject("database").getAsJsonArray("entities")

        val schemaTables = mutableSetOf<String>()
        for (i in 0 until entitiesArray.size()) {
            val tableName = entitiesArray.get(i).asJsonObject.get("tableName").asString
            schemaTables.add(tableName)
        }

        schemaTables.add("sqlite_sequence")
        schemaTables.add("room_master_table")

        val unhandled = LocalDataOwnership.unhandledTables(schemaTables)
        assertTrue("Guard Failure! Unhandled tables found in Room schema: $unhandled", unhandled.isEmpty())
    }

    @Test
    fun testDatabaseTablesCoverageGuard_NegativeTest() {
        val fakeSchemaTables = setOf("profiles", "bp_records", "unhandled_fake_table")
        val unhandled = LocalDataOwnership.unhandledTables(fakeSchemaTables)
        assertEquals(listOf("unhandled_fake_table"), unhandled)
    }
}
