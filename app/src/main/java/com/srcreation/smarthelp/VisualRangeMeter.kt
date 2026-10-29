package com.srcreation.smarthelp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

object RangeMeterScale {
    const val BP_SYS_MIN = 40f
    const val BP_SYS_MAX = 220f
    const val PULSE_MIN = 30f
    const val PULSE_MAX = 200f
    const val SPO2_MIN = 70f
    const val SPO2_MAX = 100f
    const val GLUCOSE_MGDL_MIN = 40f
    const val GLUCOSE_MGDL_MAX = 400f
    const val TEMP_C_MIN = 35.0f
    const val TEMP_C_MAX = 42.0f
}

@Composable
fun VisualRangeMeter(
    vitalType: String,
    val1: Double,
    val2: Double = 0.0,
    unit: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isBn = remember { Locale.getDefault().language == "bn" }

    // Evaluate with AlertThresholds
    val alertResult = remember(vitalType, val1, val2, unit) {
        when (vitalType.uppercase()) {
            "BP" -> AlertThresholds.checkBp(val1.toInt(), val2.toInt())
            "PULSE" -> AlertThresholds.checkPulse(val1.toInt())
            "SPO2" -> AlertThresholds.checkSpO2(val1)
            "GLUCOSE" -> AlertThresholds.checkGlucose(val1, unit)
            "TEMPERATURE", "TEMP" -> AlertThresholds.checkTemperature(val1, unit)
            else -> VitalAlertResult(VitalAlertLevel.NORMAL)
        }
    }

    // Do not show meter for invalid or impossible values or zero/negative
    if (val1 <= 0.0 || alertResult.isImpossible) return

    val (badgeColor, statusLabel) = when (alertResult.level) {
        VitalAlertLevel.DANGER -> Pair(Color(0xFFDC2626), if (isBn) "সীমার বাইরে" else "Outside set limits")
        VitalAlertLevel.WARNING -> Pair(Color(0xFFB45309), if (isBn) "সতর্কতা" else "Caution")
        VitalAlertLevel.NORMAL -> Pair(Color(0xFF047857), if (isBn) "সীমার ভেতরে" else "Within set limits")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isBn) "স্ট্যাটাস ইন্ডিকেটর" else "Status Indicator",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Color bar indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(5.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(if (alertResult.level == VitalAlertLevel.DANGER) 1f else if (alertResult.level == VitalAlertLevel.WARNING) 0.65f else 0.35f)
                        .background(badgeColor, RoundedCornerShape(5.dp))
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = if (isBn) "সীমা সাধারণ ডিফল্ট, কোনো চিকিৎসা পরামর্শ নয়।" else "Limits are general defaults, not medical advice.",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}
