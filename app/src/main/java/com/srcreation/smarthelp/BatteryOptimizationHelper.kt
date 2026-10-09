package com.srcreation.smarthelp

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.isAppInDarkTheme
import java.util.Locale

object BatteryOptimizationHelper {

    fun openBatteryOptimizationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
            return
        } catch (_: Exception) {
            // Fallback to application settings
        }

        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:${context.packageName}".toUri()
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun BatteryOptimizationDialog(onDismiss: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isBn = LocalConfiguration.current.locales[0].language == "bn"
    val context = LocalContext.current
    val isDark = isAppInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BatteryAlert, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isBn) "রিমাইন্ডার ও অ্যালার্ম নিশ্চিত করুন" else "Ensure Reliable Alarms",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (isDark) Color.White else Color(0xFF1A237E)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isBn)
                        "আপনার ফোনে (যেমন: শাওমি, ভিভো, অপ্পো, রিয়েলমি) ব্যাকগ্রাউন্ড ব্যাটারি সেভার অন থাকলে নির্ধারিত সময়ে পিরিয়ড বা ওষুধের অ্যালার্ম বাজতে সমস্যা হতে পারে।\n\nঅনুগ্রহ করে ব্যাটারি অপ্টিমাইজেশন 'Unrestricted' বা বন্ধ রাখুন।"
                    else
                        "Your phone (e.g. Xiaomi, Vivo, Oppo, Realme) may restrict background alarms when Battery Saver is active. Please allow unrestricted battery access for timely alerts.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDismiss()
                    BatteryOptimizationHelper.openBatteryOptimizationSettings(context)
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (isBn) "সেটিংস খুলুন (Open Settings)" else "Open Settings", 
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.Black else Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            }) {
                Text(if (isBn) "পরে করব" else "Later", color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isDark) Color(0xFF0F172A) else Color.White
    )
}
