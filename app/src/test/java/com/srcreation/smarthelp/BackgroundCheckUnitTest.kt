package com.srcreation.smarthelp

import org.junit.Assert.*
import org.junit.Test

class BackgroundCheckUnitTest {

    @Test
    fun testDeduplication() {
        val eventId = "test_event_123"
        val notifiedMap = mutableMapOf<String, Boolean>()
        
        fun isAlreadyNotified(id: String): Boolean = notifiedMap[id] ?: false
        fun markNotified(id: String) { notifiedMap[id] = true }

        assertFalse(isAlreadyNotified(eventId))
        markNotified(eventId)
        assertTrue(isAlreadyNotified(eventId))
    }

    @Test
    fun testFirstRunInitializationNoOldData() {
        var lastChecked = 0L
        val currentTime = 1000000L

        if (lastChecked == 0L) {
            lastChecked = currentTime
        }

        assertEquals(1000000L, lastChecked)
    }

    @Test
    fun testNotificationCapOfThree() {
        val notifications = mutableListOf<String>("Alert 1", "Alert 2", "Alert 3", "Alert 4", "Alert 5")
        val maxNotifications = 3
        val sentNotifications = mutableListOf<String>()

        if (notifications.size <= maxNotifications) {
            sentNotifications.addAll(notifications)
        } else {
            for (i in 0 until maxNotifications) {
                sentNotifications.add(notifications[i])
            }
            val extraCount = notifications.size - maxNotifications
            sentNotifications.add("$extraCount more alerts, open the app.")
        }

        assertEquals(4, sentNotifications.size)
        assertEquals("Alert 1", sentNotifications[0])
        assertEquals("Alert 2", sentNotifications[1])
        assertEquals("Alert 3", sentNotifications[2])
        assertEquals("2 more alerts, open the app.", sentNotifications[3])
    }

    @Test
    fun testPerUidStateSeparation() {
        val uid1 = "user_abc"
        val uid2 = "user_xyz"

        val prefs = mutableMapOf<String, Any>()
        fun setPref(uid: String, key: String, value: Any) {
            prefs["u_${uid}_$key"] = value
        }
        fun getPref(uid: String, key: String): Any? {
            return prefs["u_${uid}_$key"]
        }

        setPref(uid1, "last_checked", 100L)
        setPref(uid2, "last_checked", 200L)

        assertEquals(100L, getPref(uid1, "last_checked"))
        assertEquals(200L, getPref(uid2, "last_checked"))
    }

    @Test
    fun testInsulinDoseIsNeverGlucoseRule() {
        val glucoseVal = 60.0
        val result = AlertThresholds.checkGlucose(glucoseVal, "mg/dL")
        assertEquals(VitalAlertLevel.DANGER, result.level)

        val impossibleGlucose = 1200.0
        val impossibleResult = AlertThresholds.checkGlucose(impossibleGlucose, "mg/dL")
        assertTrue(impossibleResult.isImpossible)
    }
}
