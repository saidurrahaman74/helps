package com.srcreation.smarthelp

import java.util.Locale

object MedicineScheduleHelper {

    fun getScheduledMinutesForSlot(slotName: String, customTime: String? = null, scheduleTimesMap: Map<String, String>? = null): Int? {
        val cleanSlot = slotName.trim()

        // 1. Check scheduleTimesMap if provided
        if (scheduleTimesMap != null && scheduleTimesMap.containsKey(cleanSlot)) {
            val timeStr = scheduleTimesMap[cleanSlot] ?: ""
            val parsed = parseTimeStringToMinutes(timeStr)
            if (parsed != null) return adjustMinutesToDayTimeline(parsed)
        }

        // 2. Check customTime if slot is custom or matches
        if (!customTime.isNullOrBlank() && (cleanSlot.contains("Custom", true) || cleanSlot.contains("Set", true))) {
            val parsed = parseTimeStringToMinutes(customTime)
            if (parsed != null) return adjustMinutesToDayTimeline(parsed)
        }

        val baseMinutes = when (cleanSlot.lowercase(Locale.ROOT)) {
            "morning" -> 8 * 60       // 08:00 (480 min)
            "noon" -> 14 * 60         // 14:00 (840 min)
            "evening" -> 20 * 60      // 20:00 (1200 min)
            "night" -> 22 * 60        // 22:00 (1320 min)
            "4t_1", "06:00", "6:00" -> 6 * 60 // 06:00 (360 min)
            "4t_2", "12:00" -> 12 * 60 // 12:00 (720 min)
            "4t_3", "18:00" -> 18 * 60 // 18:00 (1080 min)
            "4t_4", "0:00", "00:00" -> 0 // 00:00 Midnight (0 min)
            else -> parseTimeStringToMinutes(cleanSlot)
        } ?: return null

        return adjustMinutesToDayTimeline(baseMinutes)
    }

    private fun adjustMinutesToDayTimeline(minutes: Int): Int {
        // Our day runs from 05:00 (300 min) to 04:59 next morning (1739 min).
        // Any scheduled time below 300 minutes (00:00 to 04:59) belongs to the end of the day timeline (+1440 min).
        return if (minutes < 300) minutes + 1440 else minutes
    }

    private fun parseTimeStringToMinutes(timeStr: String): Int? {
        try {
            val parts = timeStr.trim().split(":")
            if (parts.size >= 2) {
                var h = parts[0].trim().toIntOrNull() ?: return null
                val mAndAmPm = parts[1].trim()
                val spaceParts = mAndAmPm.split(" ")
                val m = spaceParts[0].trim().toIntOrNull() ?: 0
                if (spaceParts.size > 1) {
                    val ampm = spaceParts[1].trim().uppercase(Locale.ROOT)
                    if (ampm == "PM" && h < 12) h += 12
                    if (ampm == "AM" && h == 12) h = 0
                }
                return h * 60 + m
            }
        } catch (_: Exception) {}
        return null
    }

    fun calculateDoseStatus(slotName: String, isTaken: Boolean, currentTimeInMinutes: Int, customTime: String? = null, scheduleTimesMap: Map<String, String>? = null): String {
        if (isTaken) return "Taken"
        val scheduledMinutes = getScheduledMinutesForSlot(slotName, customTime, scheduleTimesMap) ?: return "Unknown"
        return if (currentTimeInMinutes > scheduledMinutes + 120) "Missed" else "Due"
    }
}
