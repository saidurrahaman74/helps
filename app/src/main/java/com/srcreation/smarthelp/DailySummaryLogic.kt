package com.srcreation.smarthelp

import com.srcreation.smarthelp.db.MedicineDoseEntity
import com.srcreation.smarthelp.db.MedicineEntity
import java.util.*

data class VitalsReadings(
    val bpSys: Int? = null,
    val bpDia: Int? = null,
    val pulseBpm: Int? = null,
    val spo2Size: Double? = null,
    val spo2Percentage: Int? = null
)

sealed class DailySummaryResult {
    data class NoReadings(val msg: String) : DailySummaryResult()
    data class WithinLimits(val msg: String) : DailySummaryResult()
    data class Warning(val msg: String) : DailySummaryResult()
    data class Danger(val msg: String) : DailySummaryResult()
    object Hidden : DailySummaryResult()
}

object DailySummaryLogic {
    fun computeDailySummary(
        now: Long,
        timeZoneId: String,
        medicines: List<MedicineEntity>,
        todayDoses: List<MedicineDoseEntity>,
        readings: VitalsReadings,
        isGateBlocked: Boolean,
        isBn: Boolean
    ): DailySummaryResult {
        if (isGateBlocked) return DailySummaryResult.Hidden

        val tz = TimeZone.getTimeZone(timeZoneId.ifBlank { TimeZone.getDefault().id })
        val calendar = Calendar.getInstance(tz).apply { timeInMillis = now }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        if (hour in 0..4) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        val rawHour = calendar.get(Calendar.HOUR_OF_DAY)
        val rawMinute = calendar.get(Calendar.MINUTE)
        val currentTimeInMinutes = if (rawHour in 0..4) (1440 + rawHour * 60 + rawMinute) else (rawHour * 60 + rawMinute)

        val bpRes = if (readings.bpSys != null && readings.bpDia != null) AlertThresholds.checkBp(readings.bpSys, readings.bpDia) else null
        val pulseRes = if (readings.pulseBpm != null) AlertThresholds.checkPulse(readings.pulseBpm) else null
        val spo2Val = readings.spo2Size ?: readings.spo2Percentage?.toDouble()
        val spo2Res = if (spo2Val != null) AlertThresholds.checkSpO2(spo2Val) else null

        val results = listOfNotNull(bpRes, pulseRes, spo2Res)
        val checkedVitalsNames = mutableListOf<String>()
        if (readings.bpSys != null && bpRes?.isImpossible == false) checkedVitalsNames.add("BP")
        if (readings.pulseBpm != null && pulseRes?.isImpossible == false) checkedVitalsNames.add("Pulse")
        if (spo2Val != null && spo2Res?.isImpossible == false) checkedVitalsNames.add("SpO2")

        val dangerResult = results.firstOrNull { it.level == VitalAlertLevel.DANGER && !it.isImpossible }
        val warningResult = results.firstOrNull { it.level == VitalAlertLevel.WARNING && !it.isImpossible }

        // Count doses Due and Missed
        var dueCount = 0
        var missedCount = 0
        for (med in medicines) {
            val slots = med.selectedTimes.split(",").map { it.trim() }.filter { it.isNotBlank() }
            for (slot in slots) {
                val isTaken = todayDoses.any { it.medicineId == med.id && it.slot.equals(slot, ignoreCase = true) && it.taken }
                val status = MedicineScheduleHelper.calculateDoseStatus(slot, isTaken, currentTimeInMinutes)
                if (status == "Due") dueCount++
                else if (status == "Missed") missedCount++
            }
        }

        val doseSummaryText = when {
            missedCount > 0 && dueCount > 0 -> if (isBn) "$missedCount টি ডোজ মিস হয়েছে, $dueCount টি বাকি" else "$missedCount dose(s) missed, $dueCount due"
            missedCount > 0 -> if (isBn) "$missedCount টি ডোজ মিস হয়েছে" else "$missedCount dose(s) missed"
            dueCount > 0 -> if (isBn) "$dueCount টি ডোজ বাকি" else "$dueCount dose(s) due"
            else -> ""
        }

        return when {
            dangerResult != null -> {
                val vitalName = when {
                    bpRes == dangerResult -> "BP"
                    pulseRes == dangerResult -> "Pulse"
                    spo2Res == dangerResult -> "SpO2"
                    else -> "Vital"
                }
                DailySummaryResult.Danger(
                    if (isBn) "মনোযোগ আবশ্যক: $vitalName সেটিং করা সীমার বাইরে রয়েছে। অসুস্থ বোধ করলে অবিলম্বে চিকিৎসকের সাথে যোগাযোগ করুন বা ইমার্জেন্সিতে কল দিন।"
                    else "Attention required: $vitalName is outside your set limits. If you feel unwell, call your doctor or emergency services."
                )
            }
            warningResult != null -> {
                val vitalName = when {
                    spo2Res == warningResult -> "SpO2"
                    else -> "Vitals"
                }
                DailySummaryResult.Warning(
                    if (isBn) "$vitalName readings are outside your set limits."
                    else "$vitalName readings are outside your set limits."
                )
            }
            results.isNotEmpty() || dueCount > 0 || missedCount > 0 -> {
                val vitalsList = checkedVitalsNames.joinToString(", ")
                val baseMsg = if (vitalsList.isNotBlank()) {
                    if (isBn) "$vitalsList আপনার সেট করা সীমার ভেতরে রয়েছে।" else "$vitalsList are within your set limits."
                } else {
                    if (isBn) "আজকের ওষুধের স্ট্যাটাস:" else "Today's medicine status:"
                }
                val finalMsg = if (doseSummaryText.isNotBlank()) "$baseMsg • $doseSummaryText" else baseMsg
                DailySummaryResult.WithinLimits(finalMsg)
            }
            else -> DailySummaryResult.NoReadings(if (isBn) "আজকের কোনো রিডিং রেকর্ড নেই" else "No readings today")
        }
    }
}
