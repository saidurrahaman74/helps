package com.srcreation.smarthelp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.util.Locale

class NurseOxygenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                OxygenDurationScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OxygenDurationScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var selectedTab by remember { mutableIntStateOf(0) }

    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }

    val tabs = remember(isBn) {
        if (isBn) listOf(
            "সিলিন্ডার সময় (Duration) ⏱️",
            "FiO₂ ও অক্সিজেন ডিভাইস 🫁",
            "COPD ও SpO₂ গাইড 🎯"
        ) else listOf(
            "Cylinder Duration ⏱️",
            "FiO₂ & O₂ Devices 🫁",
            "COPD & SpO₂ Targets 🎯"
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = if (isBn) "অক্সিজেন ডিউরেশন ও FiO₂ মাস্টার" else "Oxygen Duration & FiO₂ Master",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBack()
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (isDark) Color.White else Color(0xFF1A237E))
                            }
                        },
                        actions = { LanguageToggleButton() },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )

                    // Subtag Banner
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = if (isDark) 0.25f else 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = if (isDark) 0.5f else 0.3f)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = if (isBn) "🌐 আন্তর্জাতিক অক্সিজেন থেরাপি ও সিলিন্ডার স্ট্যান্ডার্ড (AARC / WHO)" else "🌐 International Oxygen Therapy & Cylinder Standard (AARC / WHO)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    // Scrollable Single Line 3D Tabs
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(tabs) { index, title ->
                            val isSelected = selectedTab == index
                            val chipBg = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.35f))
                            val textColor = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A))

                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedTab = index
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = chipBg,
                                border = BorderStroke(1.dp, if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.6f)))
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textColor,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = { AppBottomBar(context = context, currentTab = -1, isNurseMode = true, backgroundBrush = AppGradientBrush) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> OxygenCylinderSection(isBn)
                        1 -> Fio2CalculatorSection(isBn)
                        2 -> SpO2TargetSection(isBn)
                    }
                }
            }
        }
    }
}

// 1. OXYGEN CYLINDER DURATION CALCULATOR
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OxygenCylinderSection(isBn: Boolean) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var pressurePsi by remember { mutableStateOf("1500") }
    var flowRateLpm by remember { mutableStateOf("4") }
    var selectedCylinderType by remember { mutableStateOf("E") }

    val cylinderFactors = mapOf(
        "D (0.16)" to Pair("D", 0.16),
        "E (0.28)" to Pair("E", 0.28),
        "M (1.56)" to Pair("M", 1.56),
        "G (2.41)" to Pair("G", 2.41),
        "H/K (3.14)" to Pair("H/K", 3.14)
    )

    Oxygen3DCard {
        OxygenTitle(
            if (isBn) "সিলিন্ডার স্থায়ীত্ব ক্যালকুলেটর" else "Oxygen Cylinder Duration Calculator",
            if (isBn) "ফর্মুলা: ((প্রেশার PSI - ২০০) × ফ্যাক্টর) ÷ ফ্লো রেট (L/min)" else "Formula: ((Pressure PSI - 200) × Factor) ÷ Flow Rate (L/min)"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(if (isBn) "সিলিন্ডারের সাইজ সিলেক্ট করুন:" else "Select Cylinder Tank Size:", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(cylinderFactors.keys.toList()) { _, key ->
                val typeCode = cylinderFactors[key]?.first ?: "E"
                val isSel = selectedCylinderType == typeCode
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedCylinderType = typeCode
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF1A237E).copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                ) {
                    Text(
                        text = key,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) (if (isDark) Color.Black else Color.White) else (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = pressurePsi,
            onValueChange = { pressurePsi = it },
            label = { Text(if (isBn) "বর্তমান প্রেশার (PSI - গিজ প্রেশার)" else "Current Gauge Pressure (PSI)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = oxygenFieldColors()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = flowRateLpm,
            onValueChange = { flowRateLpm = it },
            label = { Text(if (isBn) "অক্সিজেন ফ্লো রেট (L/min)" else "Flow Rate (L/min)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = oxygenFieldColors()
        )
    }

    val pressure = pressurePsi.toDoubleOrNull() ?: 0.0
    val flow = flowRateLpm.toDoubleOrNull() ?: 0.0
    val factor = cylinderFactors.values.find { it.first == selectedCylinderType }?.second ?: 0.28
    val safeResidual = 200.0 // Standard safe residual pressure in PSI

    val durationMinutes = if (pressure > safeResidual && flow > 0) {
        ((pressure - safeResidual) * factor) / flow
    } else 0.0

    val hours = (durationMinutes / 60).toInt()
    val minutes = (durationMinutes % 60).toInt()

    val isLowPressure = pressure in 1.0..500.0 || durationMinutes in 1.0..30.0

    val subtext = if (durationMinutes > 0) {
        if (isLowPressure) {
            if (isBn) "⚠️ সতর্কতা: সিলিন্ডার দ্রুত শেষ হয়ে আসছে! নতুন সিলিন্ডার রেডি করুন।" else "⚠️ WARNING: Cylinder low! Prepare replacement tank immediately."
        } else {
            if (isBn) "নিরাপদ রেসিডিউয়াল প্রেশার ২০০ PSI রেখে অবশিষ্ট সময় হিসাব করা হয়েছে।" else "Calculated leaving 200 PSI safe residual pressure threshold."
        }
    } else {
        if (isBn) "হিসাব করতে সিলিন্ডার প্রেশার ও ফ্লো রেট লিখুন" else "Enter pressure and flow rate to calculate duration"
    }

    OxygenResultCard(
        label = if (isBn) "অবশিষ্ট অক্সিজেন সময়" else "Estimated Remaining Duration",
        result = if (hours > 0 || minutes > 0) "${hours}h ${minutes}m" else "0m",
        subtext = subtext,
        isWarning = isLowPressure
    )
}

// 2. FiO2 CALCULATOR & OXYGEN DELIVERY DEVICES
@Composable
fun Fio2CalculatorSection(isBn: Boolean) {
    var flowLpm by remember { mutableStateOf("4") }

    val flowVal = flowLpm.toDoubleOrNull() ?: 0.0
    // Nasal Cannula FiO2 Formula: 20% + (4 * Flow Rate L/min)
    val nasalFio2 = if (flowVal > 0) (20.0 + (4.0 * flowVal)).coerceIn(21.0, 44.0) else 21.0

    Oxygen3DCard {
        OxygenTitle(
            if (isBn) "ন্যাজাল ক্যানুলা FiO₂ ক্যালকুলেটর" else "Nasal Cannula FiO₂ Calculator",
            if (isBn) "ফর্মুলা: FiO₂ (%) = ২০% + (৪ × ফ্লো রেট L/min)" else "Formula: FiO₂ (%) = 20% + (4 × Flow Rate L/min)"
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = flowLpm,
            onValueChange = { flowLpm = it },
            label = { Text(if (isBn) "ন্যাজাল ক্যানুলা ফ্লো রেট (1-6 L/min)" else "Nasal Cannula Flow (1-6 L/min)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = oxygenFieldColors()
        )
    }

    OxygenResultCard(
        label = if (isBn) "প্রদেয় অক্সিজেন কনসেনট্রেশন (FiO₂)" else "Estimated Delivered FiO₂",
        result = "${nasalFio2.toInt()}%",
        subtext = if (isBn) "সাধারণ বাতাসের স্বাভাবিক FiO₂ হলো ২১%।" else "Room air baseline FiO₂ is 21%."
    )

    // Device Reference Card
    Oxygen3DCard {
        OxygenTitle(
            if (isBn) "আন্তর্জাতিক অক্সিজেন ডেলিভারি ডিভাইস গাইড" else "International Oxygen Delivery Devices Reference",
            if (isBn) "ডিভাইস অনুযায়ী প্রদেয় ফ্লো রেট ও FiO₂" else "Flow rate and delivered FiO₂ percentage range"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            DeviceRow(
                device = if (isBn) "১. ন্যাজাল ক্যানুলা (Nasal Cannula)" else "1. Nasal Cannula",
                flow = "1 - 6 L/min",
                fio2 = "24% - 44%",
                isBn = isBn
            )
            DeviceRow(
                device = if (isBn) "২. সিম্পল ফেস মাস্ক (Simple Mask)" else "2. Simple Face Mask",
                flow = "5 - 8 L/min",
                fio2 = "40% - 60%",
                isBn = isBn
            )
            DeviceRow(
                device = if (isBn) "৩. ভেনচুরি মাস্ক (Venturi Mask)" else "3. Venturi Mask",
                flow = "2 - 15 L/min",
                fio2 = "24%, 28%, 31%, 35%, 40%, 50%",
                isBn = isBn
            )
            DeviceRow(
                device = if (isBn) "৪. নন-রিব্রিদার মাস্ক (NRB Mask)" else "4. Non-Rebreather (NRB)",
                flow = "10 - 15 L/min",
                fio2 = "60% - 90%",
                isBn = isBn
            )
            DeviceRow(
                device = if (isBn) "৫. হাই-ফ্লো ক্যানুলা (HFNC)" else "5. High-Flow Nasal Cannula",
                flow = "20 - 60 L/min",
                fio2 = "21% - 100%",
                isBn = isBn
            )
        }
    }
}

// 3. SpO2 TARGETS & COPD GUIDELINES
@Composable
fun SpO2TargetSection(isBn: Boolean) {
    val isDark = isSystemInDarkTheme()
    Oxygen3DCard {
        OxygenTitle(
            if (isBn) "SpO₂ টার্গেট ও সিওপিডি (COPD) গাইডলাইন" else "SpO₂ Target & COPD Oxygenation Guidelines",
            if (isBn) "রোগীর টাইপ অনুযায়ী স্যাচুরেশন লক্ষ্যমাত্রা" else "AARC / BTS target oxygen saturation ranges"
        )

        Spacer(modifier = Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                color = Color(0xFF22C55E).copy(alpha = if (isDark) 0.2f else 0.12f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = if (isDark) 0.4f else 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBn) "🎯 সাধারণ রোগী (Normal Adult Patients):" else "🎯 General Patient SpO₂ Target:",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBn) "লক্ষ্যমাত্রা: 94% - 98% SpO₂। হাইপোক্সিয়া প্রতিরোধে পর্যাপ্ত অক্সিজেন দিন।" else "Target: 94% - 98% SpO₂. Administer oxygen to treat hypoxemia.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                color = Color(0xFFFF9800).copy(alpha = if (isDark) 0.2f else 0.12f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = if (isDark) 0.4f else 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (isBn) "⚠️ সিওপিডি ও রেসপিরোটরি ফেইলিউর (COPD / Hypercapnic Risk):" else "⚠️ COPD / Hypercapnic Risk Patients:",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBn) "লক্ষ্যমাত্রা: 88% - 92% SpO₂। অতিরিক্ত অক্সিজেন দিলে হাইপারক্যাপনিয়া ও রেসপিরোটরি এরেস্ট হতে পারে।" else "Target: 88% - 92% SpO₂. Avoid high FIO₂ to prevent CO₂ retention and respiratory arrest.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceRow(device: String, flow: String, fio2: String, isBn: Boolean) {
    val isDark = isSystemInDarkTheme()
    Surface(
        color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.12f) else Color(0xFF1A237E).copy(alpha = 0.06f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(device, fontWeight = FontWeight.Black, fontSize = 13.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isBn) "ফ্লো: $flow" else "Flow: $flow", fontSize = 11.sp, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                Text(if (isBn) "FiO₂: $fio2" else "FiO₂: $fio2", fontSize = 11.sp, color = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------- 3D HELPER COMPONENTS ----------------

@Composable
fun Oxygen3DCard(content: @Composable ColumnScope.() -> Unit) {
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.88f),
        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.6f)),
        content = {
            Column(modifier = Modifier.padding(20.dp), content = content)
        }
    )
}

@Composable
fun OxygenTitle(title: String, subtitle: String) {
    val isDark = isSystemInDarkTheme()
    Column {
        Text(title, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black, fontSize = 17.sp)
        Text(subtitle, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF334155), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OxygenResultCard(label: String, result: String, subtext: String, isWarning: Boolean = false) {
    val isDark = isSystemInDarkTheme()
    val resultColor = if (isWarning) Color(0xFFEF4444) else (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.92f),
        border = BorderStroke(1.5.dp, resultColor.copy(alpha = if (isDark) 0.5f else 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF334155), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(result, color = resultColor, fontSize = 46.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = resultColor.copy(alpha = if (isDark) 0.15f else 0.1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, resultColor.copy(alpha = if (isDark) 0.3f else 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(if (isWarning) Icons.Default.Warning else Icons.Default.Info, contentDescription = null, tint = resultColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = subtext,
                        color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun oxygenFieldColors(): TextFieldColors {
    val isDark = isSystemInDarkTheme()
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
        focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.35f),
        unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.25f),
        focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.6f),
        focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
        unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
    )
}
