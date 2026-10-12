package com.srcreation.smarthelp

enum class VitalAlertLevel { NORMAL, WARNING, DANGER }

data class VitalAlertResult(
    val level: VitalAlertLevel = VitalAlertLevel.NORMAL,
    val message: String = "",
    val detailText: String = "",
    val isImpossible: Boolean = false
)

/**
 * AlertThresholds contains general clinical default thresholds for vitals monitoring.
 * NOTE: These are general defaults and should be customized with the patient's doctor.
 * This app does not replace a doctor or emergency services.
 */
object AlertThresholds {

    // SpO2 (%): warning < 94, danger < 90, impossible > 100 or <= 0
    fun checkSpO2(value: Double): VitalAlertResult {
        if (value <= 0.0) return VitalAlertResult(VitalAlertLevel.NORMAL, "No reading", "", false)
        if (value > 100.0) return VitalAlertResult(VitalAlertLevel.NORMAL, "Check this value", "SpO2 cannot exceed 100%", true)
        return when {
            value < 90.0 -> VitalAlertResult(VitalAlertLevel.DANGER, "SpO2 ${value.toInt()}%", "below 90%")
            value < 94.0 -> VitalAlertResult(VitalAlertLevel.WARNING, "SpO2 ${value.toInt()}%", "below 94%")
            else -> VitalAlertResult(VitalAlertLevel.NORMAL)
        }
    }

    // Pulse (BPM): danger < 50 or > 120, impossible > 300 or <= 0
    fun checkPulse(bpm: Int): VitalAlertResult {
        if (bpm <= 0) return VitalAlertResult(VitalAlertLevel.NORMAL, "No reading", "", false)
        if (bpm > 300) return VitalAlertResult(VitalAlertLevel.NORMAL, "Check this value", "Pulse above 300 BPM is impossible", true)
        return when {
            bpm < 50 -> VitalAlertResult(VitalAlertLevel.DANGER, "Pulse $bpm BPM", "below 50 BPM")
            bpm > 120 -> VitalAlertResult(VitalAlertLevel.DANGER, "Pulse $bpm BPM", "above 120 BPM")
            else -> VitalAlertResult(VitalAlertLevel.NORMAL)
        }
    }

    // Temperature (°C): warning > 37.8, danger > 39.0, impossible > 45.0 or <= 0
    fun checkTemperature(rawTemp: Double, unit: String): VitalAlertResult {
        if (rawTemp <= 0.0) return VitalAlertResult(VitalAlertLevel.NORMAL, "No reading", "", false)
        val tempC = if (unit.contains("F", ignoreCase = true)) {
            (rawTemp - 32.0) * (5.0 / 9.0)
        } else {
            rawTemp
        }
        if (tempC > 45.0) return VitalAlertResult(VitalAlertLevel.NORMAL, "Check this value", "Temperature above 45°C is impossible", true)
        val displayVal = if (rawTemp == rawTemp.toInt().toDouble()) rawTemp.toInt().toString() else String.format("%.1f", rawTemp)
        return when {
            tempC > 39.0 -> VitalAlertResult(VitalAlertLevel.DANGER, "Temp $displayVal°C", "above 39.0°C")
            tempC > 37.8 -> VitalAlertResult(VitalAlertLevel.WARNING, "Temp $displayVal°C", "above 37.8°C")
            else -> VitalAlertResult(VitalAlertLevel.NORMAL)
        }
    }

    // Blood Pressure (mmHg): danger if systolic > 160 or < 90, or diastolic > 100 or < 60; impossible if sys > 300 or dia > 200 or <= 0
    fun checkBp(sys: Int, dia: Int): VitalAlertResult {
        if (sys <= 0 || dia <= 0) return VitalAlertResult(VitalAlertLevel.NORMAL, "No reading", "", false)
        if (sys > 300 || dia > 200) return VitalAlertResult(VitalAlertLevel.NORMAL, "Check this value", "BP reading is outside physical range", true)
        val sysDanger = sys > 160 || sys < 90
        val diaDanger = dia > 100 || dia < 60
        return when {
            sysDanger || diaDanger -> VitalAlertResult(
                VitalAlertLevel.DANGER,
                "BP $sys/$dia mmHg",
                "outside safe limits (90-160 / 60-100)"
            )
            else -> VitalAlertResult(VitalAlertLevel.NORMAL)
        }
    }

    // Glucose (mg/dL): danger < 70 or > 250, impossible > 1000 or <= 0
    fun checkGlucose(rawGlucose: Double, unit: String): VitalAlertResult {
        if (rawGlucose <= 0.0) return VitalAlertResult(VitalAlertLevel.NORMAL, "No reading", "", false)
        val mgDl = if (unit.contains("mmol", ignoreCase = true)) {
            rawGlucose * 18.0182
        } else {
            rawGlucose
        }
        if (mgDl > 1000.0) return VitalAlertResult(VitalAlertLevel.NORMAL, "Check this value", "Glucose reading above 1000 mg/dL is impossible", true)
        val displayVal = if (rawGlucose == rawGlucose.toInt().toDouble()) rawGlucose.toInt().toString() else String.format("%.1f", rawGlucose)
        return when {
            mgDl < 70.0 -> VitalAlertResult(VitalAlertLevel.DANGER, "Glucose $displayVal $unit", "below 70 mg/dL")
            mgDl > 250.0 -> VitalAlertResult(VitalAlertLevel.DANGER, "Glucose $displayVal $unit", "above 250 mg/dL")
            else -> VitalAlertResult(VitalAlertLevel.NORMAL)
        }
    }
}
