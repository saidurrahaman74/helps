package com.srcreation.smarthelp

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun insulinCalculator_isCorrect() {
        assertEquals("--", calculateInsulinResult("", "mg/dL", "Medium"))
        assertEquals("Invalid", calculateInsulinResult("abc", "mg/dL", "Medium"))
        assertEquals("0 Units", calculateInsulinResult("120", "mg/dL", "Medium"))
        assertEquals("4 Units", calculateInsulinResult("180", "mg/dL", "Medium"))
        assertEquals("2 Units", calculateInsulinResult("180", "mg/dL", "Low"))
        assertEquals("8 Units", calculateInsulinResult("180", "mg/dL", "High"))
        assertEquals("CALL DOCTOR", calculateInsulinResult("450", "mg/dL", "Medium"))
        
        // Comma test
        assertEquals("4 Units", calculateInsulinResult("180,0", "mg/dL", "Medium"))
        
        // mmol/L test
        // 10 mmol/L * 18 = 180 mg/dL -> Medium -> 4 Units
        assertEquals("4 Units", calculateInsulinResult("10", "mmol/L", "Medium"))
    }
}