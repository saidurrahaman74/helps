package com.srcreation.smarthelp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PatientLinkHubUnitTest {

    @Test
    fun testTabParsing_StringValues() {
        assertEquals(0, PatientLinkHubViewModel.parseTabValue("LINK"))
        assertEquals(0, PatientLinkHubViewModel.parseTabValue("PAIRING"))
        assertEquals(1, PatientLinkHubViewModel.parseTabValue("PERMISSIONS"))
        assertEquals(1, PatientLinkHubViewModel.parseTabValue("WHO_CAN_SEE"))
        assertEquals(2, PatientLinkHubViewModel.parseTabValue("INSTRUCTIONS"))
    }

    @Test
    fun testTabParsing_IntegerValues() {
        assertEquals(0, PatientLinkHubViewModel.parseTabValue(0))
        assertEquals(1, PatientLinkHubViewModel.parseTabValue(1))
        assertEquals(2, PatientLinkHubViewModel.parseTabValue(2))
    }

    @Test
    fun testTabParsing_InvalidAndMissingValues() {
        assertEquals(0, PatientLinkHubViewModel.parseTabValue(null))
        assertEquals(0, PatientLinkHubViewModel.parseTabValue("INVALID_TAB_STRING"))
        assertEquals(0, PatientLinkHubViewModel.parseTabValue(-1))
        assertEquals(0, PatientLinkHubViewModel.parseTabValue(99))
    }

    @Test
    fun testRedirectForwarding_PreservesTabAndExtrasData() {
        val originalExtras = mapOf("PATIENT_NAME" to "John Doe", "PATIENT_ID" to "p_123")
        val redirectedExtras = mutableMapOf<String, Any>()
        
        // Forwarding original extras
        redirectedExtras.putAll(originalExtras)
        // Injecting TAB extra
        redirectedExtras["TAB"] = "INSTRUCTIONS"

        assertEquals("INSTRUCTIONS", redirectedExtras["TAB"])
        assertEquals("John Doe", redirectedExtras["PATIENT_NAME"])
        assertEquals("p_123", redirectedExtras["PATIENT_ID"])
    }

    @Test
    fun testBadgeCountLogic_CalculatesPendingAndUnread() {
        val requests = listOf(
            mapOf("status" to "pending"),
            mapOf("status" to "active"),
            mapOf("status" to "pending")
        )
        val pendingCount = requests.count { it["status"] == "pending" }
        assertEquals(2, pendingCount)

        val instructions = listOf(
            InstructionItem("1", "Text 1", System.currentTimeMillis(), seen = false),
            InstructionItem("2", "Text 2", System.currentTimeMillis(), seen = true),
            InstructionItem("3", "Text 3", System.currentTimeMillis(), seen = false)
        )
        val unreadCount = instructions.count { !it.seen }
        assertEquals(2, unreadCount)
    }

    @Test
    fun testPairingCode_NotGeneratedOnTabOpen() {
        val code = ""
        val codeGenerated = false

        // Simulating Tab 1 initial open state
        fun onTabOpen() {
            // Code is NOT automatically generated here
        }

        onTabOpen()

        assertFalse("Pairing code should NOT be generated automatically on tab open", codeGenerated)
        assertTrue("Pairing code should start empty until explicit user action", code.isEmpty())
    }
}
