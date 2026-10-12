package com.srcreation.smarthelp

object MedicineScheduleHelper {
    fun calculateDoseStatus(slotName: String, isTaken: Boolean, currentTimeInMinutes: Int): String {
        if (isTaken) return "Taken"
        val scheduledHour = when (slotName.lowercase()) {
            "morning" -> 8
            "noon" -> 14
            "evening" -> 20
            "night" -> 22
            else -> 12
        }
        val scheduledMinutes = scheduledHour * 60
        return if (currentTimeInMinutes > scheduledMinutes + 120) "Missed" else "Due"
    }
}
