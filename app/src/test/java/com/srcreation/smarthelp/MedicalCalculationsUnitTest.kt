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
}
