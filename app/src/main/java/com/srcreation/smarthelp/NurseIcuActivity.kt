package com.srcreation.smarthelp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import com.srcreation.smarthelp.ui.theme.isAppInDarkTheme
import java.util.Locale

class NurseIcuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                IcuSuiteScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IcuSuiteScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isAppInDarkTheme()
    var selectedCategory by remember { mutableIntStateOf(0) }

    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }

    val categories = remember(isBn) {
        if (isBn) listOf(
            "১. ABG ও ভেন্টিলেটর 🫁",
            "২. হিমোডাইনামিকস ও শক 🩸",
            "৩. নিউরো & সিডেশন 🧠",
            "৪. সেপসিস & SOFA 🦠",
            "৫. রেনাল & ফ্লুইড 💧",
            "৬. ইলেক্ট্রলাইট কারেকশন 🧪",
            "৭. NICU / PICU 👶",
            "৮. ট্রমা & DIC 🩸"
        ) else listOf(
            "1. ABG & Vent 🫁",
            "2. Hemodynamics 🩸",
            "3. Neuro & Sedation 🧠",
            "4. Sepsis & SOFA 🦠",
            "5. Renal & Fluid 💧",
            "6. Electrolytes 🧪",
            "7. NICU / PICU 👶",
            "8. Trauma & DIC 🩸"
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
                                text = if (isBn) "ICU & Critical Care Suite 🏥" else "ICU & Critical Care Suite 🏥",
                                fontSize = 17.sp,
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

                    Surface(
                        color = Color(0xFFEF4444).copy(alpha = if (isDark) 0.25f else 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = if (isBn) "🌐 আন্তর্জাতিক আইসিইউ ও ক্রিটিক্যাল কেয়ার প্রটোকল (AACN & ESICM Standard)" else "🌐 International ICU Protocol (AACN & ESICM Standard)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(categories) { index, title ->
                            val isSelected = selectedCategory == index
                            val chipBg = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.35f))
                            val textColor = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A))

                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedCategory = index
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
                    when (selectedCategory) {
                        0 -> AbgVentSection(isBn, isDark)
                        1 -> HemodynamicsSection(isBn, isDark)
                        2 -> NeuroSedationSection(isBn, isDark)
                        3 -> SepsisSofaSection(isBn, isDark)
                        4 -> RenalFluidSection(isBn, isDark)
                        5 -> ElectrolytesSection(isBn, isDark)
                        6 -> NicuPicuSection(isBn, isDark)
                        7 -> TraumaDicSection(isBn, isDark)
                    }
                }
            }
        }
    }
}

// 1. ABG & VENTILATOR SUITE
@Composable
fun AbgVentSection(isBn: Boolean, isDark: Boolean) {
    var phVal by remember { mutableStateOf("7.30") }
    var paco2Val by remember { mutableStateOf("50") }
    var hco3Val by remember { mutableStateOf("24") }
    var pao2Val by remember { mutableStateOf("80") }
    var fio2Percent by remember { mutableStateOf("40") } // e.g. 40%
    var rrVal by remember { mutableStateOf("28") }
    var vtMlVal by remember { mutableStateOf("350") }

    val ph = phVal.toDoubleOrNull() ?: 7.40
    val paco2 = paco2Val.toDoubleOrNull() ?: 40.0
    val hco3 = hco3Val.toDoubleOrNull() ?: 24.0
    val pao2 = pao2Val.toDoubleOrNull() ?: 90.0
    val fio2Dec = (fio2Percent.toDoubleOrNull() ?: 21.0) / 100.0

    // ABG Interpretation Logic
    val abgStatus = remember(ph, paco2, hco3) {
        val primary = when {
            ph < 7.35 -> if (paco2 > 45) "Respiratory Acidosis" else "Metabolic Acidosis"
            ph > 7.45 -> if (paco2 < 35) "Respiratory Alkalosis" else "Metabolic Alkalosis"
            else -> "Normal ABG / Compensated"
        }
        primary
    }

    // P/F Ratio calculation
    val pfRatio = if (fio2Dec > 0) pao2 / fio2Dec else 0.0
    val ardsCategory = when {
        pfRatio > 300 -> if (isBn) "নরমাল (No ARDS)" else "Normal (>300)"
        pfRatio in 201.0..300.0 -> if (isBn) "মাইল্ড ARDS (Mild ARDS)" else "Mild ARDS (201-300)"
        pfRatio in 101.0..200.0 -> if (isBn) "মডারেট ARDS (Moderate ARDS)" else "Moderate ARDS (101-200)"
        else -> if (isBn) "সিভিয়ার ARDS (Severe ARDS)" else "Severe ARDS (≤100)"
    }

    // RSBI calculation for weaning (RR / Vt in Liters)
    val rr = rrVal.toDoubleOrNull() ?: 20.0
    val vtLiters = (vtMlVal.toDoubleOrNull() ?: 500.0) / 1000.0
    val rsbi = if (vtLiters > 0) rr / vtLiters else 0.0
    val rsbiStatus = if (rsbi < 105) (if (isBn) "উইনিং সফল হওয়ার সম্ভাবনা বেশি (RSBI < 105)" else "Favorable Weaning (RSBI < 105)") else (if (isBn) "উইনিং ঝুঁকিপূর্ণ (RSBI ≥ 105)" else "High Risk for Weaning Failure")

    IcuCard(
        title = if (isBn) "১. আর্টারিয়াল ব্লাড গ্যাস (ABG) & P/F Ratio" else "1. ABG Interpreter & P/F Ratio",
        subtitle = if (isBn) "pH, PaCO2, HCO3, PaO2 & FiO2 দিয়ে এবিজি ও এআরডিএস এসেসমেন্ট" else "Automatic ABG, ARDS Severity & RSBI Weaning Index",
        isDark = isDark
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = phVal, onValueChange = { phVal = it },
                label = { Text("pH") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
            OutlinedTextField(
                value = paco2Val, onValueChange = { paco2Val = it },
                label = { Text("PaCO₂") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
            OutlinedTextField(
                value = hco3Val, onValueChange = { hco3Val = it },
                label = { Text("HCO₃⁻") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = pao2Val, onValueChange = { pao2Val = it },
                label = { Text("PaO₂ (mmHg)") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
            OutlinedTextField(
                value = fio2Percent, onValueChange = { fio2Percent = it },
                label = { Text("FiO₂ (%)") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = rrVal, onValueChange = { rrVal = it },
                label = { Text("RR (/min)") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
            OutlinedTextField(
                value = vtMlVal, onValueChange = { vtMlVal = it },
                label = { Text("Tidal Vol (mL)") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
        }

        Spacer(Modifier.height(14.dp))

        IcuResultBox(
            primaryText = "ABG: $abgStatus",
            secondaryText = "P/F Ratio: %.0f mmHg (%s)".format(pfRatio, ardsCategory),
            noteText = "RSBI: %.0f (weaning: %s)".format(rsbi, rsbiStatus),
            isDark = isDark
        )
    }
}

// 2. HEMODYNAMICS & SHOCK SUITE
@Composable
fun HemodynamicsSection(isBn: Boolean, isDark: Boolean) {
    var sysVal by remember { mutableStateOf("120") }
    var diaVal by remember { mutableStateOf("80") }
    var hrVal by remember { mutableStateOf("110") }

    val sys = sysVal.toDoubleOrNull() ?: 120.0
    val dia = diaVal.toDoubleOrNull() ?: 80.0
    val hr = hrVal.toDoubleOrNull() ?: 80.0

    // MAP: (SBP + 2*DBP)/3
    val map = (sys + 2 * dia) / 3.0
    val pulsePressure = sys - dia
    val shockIndex = if (sys > 0) hr / sys else 0.0

    val siStatus = when {
        shockIndex < 0.5 -> "Normal"
        shockIndex in 0.5..0.7 -> "Normal Range (0.5 - 0.7)"
        shockIndex in 0.71..0.9 -> "Elevated Risk"
        else -> if (isBn) "⚠️ শক/রক্তক্ষরণের উচ্চ ঝুঁকি (Shock/Hemorrhage)" else "⚠️ High Risk of Occult Shock"
    }

    IcuCard(
        title = if (isBn) "২. হিমোডাইনামিকস ও শক ইনডেক্স (MAP & Shock Index)" else "2. Hemodynamics & Shock Index",
        subtitle = if (isBn) "MAP, পালস প্রেসার এবং শক ইনডেক্স থেকে শকের ঝুঁকি নিরূপণ" else "Mean Arterial Pressure & Shock Index",
        isDark = isDark
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = sysVal, onValueChange = { sysVal = it },
                label = { Text("Systolic BP") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
            OutlinedTextField(
                value = diaVal, onValueChange = { diaVal = it },
                label = { Text("Diastolic BP") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
            OutlinedTextField(
                value = hrVal, onValueChange = { hrVal = it },
                label = { Text("Heart Rate") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
        }

        Spacer(Modifier.height(14.dp))

        IcuResultBox(
            primaryText = "MAP: %.1f mmHg (Target: ≥ 65)".format(map),
            secondaryText = "Pulse Pressure: %.0f mmHg | Shock Index: %.2f".format(pulsePressure, shockIndex),
            noteText = "Status: $siStatus",
            isDark = isDark
        )
    }
}

// 3. NEURO-ICU & SEDATION
@Composable
fun NeuroSedationSection(isBn: Boolean, isDark: Boolean) {
    var mapVal by remember { mutableStateOf("85") }
    var icpVal by remember { mutableStateOf("15") }
    var rassScore by remember { mutableIntStateOf(-1) }

    val map = mapVal.toDoubleOrNull() ?: 85.0
    val icp = icpVal.toDoubleOrNull() ?: 15.0
    val cpp = map - icp

    val cppStatus = when {
        cpp >= 60 && cpp <= 70 -> if (isBn) "স্বাভাবিক সিপিপি (Target Range)" else "Optimal CPP (60-70 mmHg)"
        cpp < 60 -> if (isBn) "⚠️ ইসকেমিয়ার ঝুঁকি (Ischemia Risk)" else "⚠️ Low CPP (Ischemia Risk)"
        else -> if (isBn) "হাই পারফিউশন প্রেশার" else "High CPP (>70 mmHg)"
    }

    val rassDesc = when (rassScore) {
        4 -> "+4 Combative (আক্রমণাত্মক)"
        3 -> "+3 Very Agitated"
        2 -> "+2 Agitated"
        1 -> "+1 Restless"
        0 -> "0 Alert and Calm (স্বাভাবিক)"
        -1 -> "-1 Drowsy"
        -2 -> "-2 Light Sedation"
        -3 -> "-3 Moderate Sedation"
        -4 -> "-4 Deep Sedation"
        else -> "-5 Unarousable"
    }

    IcuCard(
        title = if (isBn) "৩. নিউরো-আইসিইউ (CPP) & RASS সিডেশন স্কেল" else "3. Neuro-ICU (CPP) & RASS Sedation Scale",
        subtitle = if (isBn) "সিইআরবিরাল পারফিউশন প্রেশার (CPP = MAP - ICP) ও ভেন্টিলেটর সিডেশন" else "Cerebral Perfusion Pressure & RASS Sedation Score",
        isDark = isDark
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = mapVal, onValueChange = { mapVal = it },
                label = { Text("MAP (mmHg)") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
            OutlinedTextField(
                value = icpVal, onValueChange = { icpVal = it },
                label = { Text("ICP (mmHg)") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(if (isBn) "RASS সিডেশন স্কোর নির্বাচন করুন (-৫ থেকে +৪):" else "Select RASS Sedation Level (-5 to +4):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black)
        
        Slider(
            value = rassScore.toFloat(),
            onValueChange = { rassScore = it.toInt() },
            valueRange = -5f..4f,
            steps = 8
        )

        Spacer(Modifier.height(14.dp))

        IcuResultBox(
            primaryText = "CPP: %.1f mmHg".format(cpp),
            secondaryText = "CPP Status: $cppStatus",
            noteText = "RASS Level: $rassDesc",
            isDark = isDark
        )
    }
}

// 4. SEPSIS & SOFA SCORE
@Composable
fun SepsisSofaSection(isBn: Boolean, isDark: Boolean) {
    var rrVal by remember { mutableStateOf("24") }
    var sbpVal by remember { mutableStateOf("95") }
    var isGcsAltered by remember { mutableStateOf(true) }

    val rr = rrVal.toDoubleOrNull() ?: 20.0
    val sbp = sbpVal.toDoubleOrNull() ?: 120.0

    var qSofaScore = 0
    if (rr >= 22) qSofaScore++
    if (sbp <= 100) qSofaScore++
    if (isGcsAltered) qSofaScore++

    val qSofaStatus = if (qSofaScore >= 2) (if (isBn) "⚠️ সেপসিসের উচ্চ ঝুঁকি (High Risk Sepsis - SOFA Evaluate)" else "⚠️ High Risk for Sepsis (qSOFA ≥ 2)") else (if (isBn) "কম ঝুঁকি (Low Risk qSOFA < 2)" else "Low Risk for Sepsis")

    IcuCard(
        title = if (isBn) "৪. সেপসিস বেডসাইড স্ক্রিনিং (qSOFA Score)" else "4. Sepsis-3 Bedside Screening (qSOFA)",
        subtitle = if (isBn) "Sepsis-3 প্রটোকল অনুযায়ী অর্গান ডিসফাংশন স্ক্রিনিং" else "Quick SOFA score for sepsis organ failure",
        isDark = isDark
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = rrVal, onValueChange = { rrVal = it },
                label = { Text("Resp Rate (/min)") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
            OutlinedTextField(
                value = sbpVal, onValueChange = { sbpVal = it },
                label = { Text("Systolic BP (mmHg)") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isGcsAltered, onCheckedChange = { isGcsAltered = it })
            Text(if (isBn) "মানসিক বিভ্রান্তি / Altered Mental Status (GCS < 15)" else "Altered Mental Status (GCS < 15)", fontSize = 12.sp, color = if (isDark) Color.White else Color.Black)
        }

        Spacer(Modifier.height(14.dp))

        IcuResultBox(
            primaryText = "qSOFA Score: $qSofaScore / 3",
            secondaryText = qSofaStatus,
            noteText = if (isBn) "নোট: qSOFA ≥ ২ হলে আইসিইউ মনিটরিং ও পূর্ণাঙ্গ SOFA এসেসমেন্ট প্রয়োজন।" else "Note: qSOFA ≥ 2 warrants full ICU SOFA evaluation & sepsis management.",
            isDark = isDark
        )
    }
}

// 5. RENAL & FLUID RESUSCITATION
@Composable
fun RenalFluidSection(isBn: Boolean, isDark: Boolean) {
    var weightKgVal by remember { mutableStateOf("70") }
    var burnPercentVal by remember { mutableStateOf("30") }

    val weight = weightKgVal.toDoubleOrNull() ?: 70.0
    val burnPercent = burnPercentVal.toDoubleOrNull() ?: 30.0

    // Parkland Formula: 4 mL * Weight (kg) * % TBSA
    val parklandMl = 4.0 * weight * burnPercent
    val first8HrMl = parklandMl / 2.0
    val next16HrMl = parklandMl / 2.0

    IcuCard(
        title = if (isBn) "৫. পার্কল্যান্ড বার্ন রিসাসিটেশন (Parkland Burn Formula)" else "5. Parkland Burn Resuscitation Formula",
        subtitle = if (isBn) "দগ্ধ (Burn) রোগীদের জন্য ২৪ ঘণ্টার ফ্লুইড হিসাব (৪ mL × kg × %TBSA)" else "24-Hour Parkland Fluid Requirement Calculator",
        isDark = isDark
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = weightKgVal, onValueChange = { weightKgVal = it },
                label = { Text("Weight (kg)") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
            OutlinedTextField(
                value = burnPercentVal, onValueChange = { burnPercentVal = it },
                label = { Text("% TBSA Burn") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
        }

        Spacer(Modifier.height(14.dp))

        IcuResultBox(
            primaryText = "Total 24h Fluid: %.0f mL Ringer's Lactate".format(parklandMl),
            secondaryText = "First 8 Hours: %.0f mL (Rate: %.0f mL/hr)".format(first8HrMl, first8HrMl / 8.0),
            noteText = "Next 16 Hours: %.0f mL (Rate: %.0f mL/hr)".format(next16HrMl, next16HrMl / 16.0),
            isDark = isDark
        )
    }
}

// 6. ELECTROLYTE CORRECTIONS
@Composable
fun ElectrolytesSection(isBn: Boolean, isDark: Boolean) {
    var caVal by remember { mutableStateOf("7.5") }
    var albVal by remember { mutableStateOf("2.5") }

    val ca = caVal.toDoubleOrNull() ?: 8.5
    val alb = albVal.toDoubleOrNull() ?: 4.0

    // Corrected Ca = Ca + 0.8 * (4.0 - Albumin)
    val correctedCa = ca + 0.8 * (4.0 - alb)

    IcuCard(
        title = if (isBn) "৬. কারেক্টেড সিরাম ক্যালসিয়াম (Corrected Calcium)" else "6. Corrected Serum Calcium",
        subtitle = if (isBn) "হাইপোঅ্যালবুমিনেমিয়ায় সঠিক ক্যালসিয়াম হিসাব: Ca + ০.৮ × (৪.০ - Albumin)" else "Corrects total serum calcium for low albumin levels",
        isDark = isDark
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = caVal, onValueChange = { caVal = it },
                label = { Text("Measured Ca (mg/dL)") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
            OutlinedTextField(
                value = albVal, onValueChange = { albVal = it },
                label = { Text("Albumin (g/dL)") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
        }

        Spacer(Modifier.height(14.dp))

        IcuResultBox(
            primaryText = "Corrected Calcium: %.2f mg/dL".format(correctedCa),
            secondaryText = if (correctedCa in 8.5..10.5) "Normal Calcium Range (8.5 - 10.5)" else if (correctedCa < 8.5) "Hypocalcemia (< 8.5 mg/dL)" else "Hypercalcemia (> 10.5 mg/dL)",
            noteText = if (isBn) "নরমাল অ্যালবুমিন স্ট্যান্ডার্ড: ৪.০ g/dL" else "Standard Normal Albumin Baseline: 4.0 g/dL",
            isDark = isDark
        )
    }
}

// 7. NICU / PICU CRITICAL CARE
@Composable
fun NicuPicuSection(isBn: Boolean, isDark: Boolean) {
    var ageYrsVal by remember { mutableStateOf("4") }
    val age = ageYrsVal.toDoubleOrNull() ?: 1.0

    // Uncuffed ETT = (Age / 4) + 4, Cuffed = (Age / 4) + 3.5
    val uncuffedEtt = (age / 4.0) + 4.0
    val cuffedEtt = (age / 4.0) + 3.5
    val depthCm = uncuffedEtt * 3.0

    IcuCard(
        title = if (isBn) "৭. প্যাডিয়াট্রিক ইটিটি (ETT Size & Depth)" else "7. Pediatric ETT Tube Size & Depth",
        subtitle = if (isBn) "শিশুদের এন্ডোট্র্যাকিয়াল টিউব সাইজ ও ঢোকানোর গভীরতা (cm)" else "Endotracheal Tube Size & Insertion Depth",
        isDark = isDark
    ) {
        OutlinedTextField(
            value = ageYrsVal, onValueChange = { ageYrsVal = it },
            label = { Text(if (isBn) "শিশুর বয়স (Years)" else "Child Age (Years)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
        )

        Spacer(Modifier.height(14.dp))

        IcuResultBox(
            primaryText = "Uncuffed ETT: %.1f mm  |  Cuffed: %.1f mm".format(uncuffedEtt, cuffedEtt),
            secondaryText = "Insertion Depth: %.1f cm (at lip)".format(depthCm),
            noteText = if (isBn) "নোট: ২ বছরের নিচের শিশুদের জন্য আনকাফড ইটিটি পছন্দনীয়।" else "Note: Uncuffed ETT preferred in infants < 2 years.",
            isDark = isDark
        )
    }
}

// 8. TRAUMA & DIC SCORE
@Composable
fun TraumaDicSection(isBn: Boolean, isDark: Boolean) {
    var gcsVal by remember { mutableStateOf("14") }
    var sysVal by remember { mutableStateOf("110") }
    var rrVal by remember { mutableStateOf("18") }

    val gcs = gcsVal.toDoubleOrNull() ?: 15.0
    val sys = sysVal.toDoubleOrNull() ?: 120.0
    val rr = rrVal.toDoubleOrNull() ?: 20.0

    // RTS formula points
    val gcsCoded = if (gcs in 13.0..15.0) 4 else if (gcs in 9.0..12.0) 3 else if (gcs in 6.0..8.0) 2 else if (gcs in 4.0..5.0) 1 else 0
    val sysCoded = if (sys > 89) 4 else if (sys in 76.0..89.0) 3 else if (sys in 50.0..75.0) 2 else if (sys in 1.0..49.0) 1 else 0
    val rrCoded = if (rr in 10.0..29.0) 4 else if (rr > 29) 3 else if (rr in 6.0..9.0) 2 else if (rr in 1.0..5.0) 1 else 0

    val rtsScore = 0.7326 * gcsCoded + 0.8431 * sysCoded + 0.2908 * rrCoded

    IcuCard(
        title = if (isBn) "৮. রিভাইজড ট্রমা স্কোর (Revised Trauma Score - RTS)" else "8. Revised Trauma Score (RTS)",
        subtitle = if (isBn) "ট্রমা রোগীদের গ্লাসগো কমা স্কেল, সিস্টোলিক বিপি ও ব্রিদিং রেট থেকে সার্ভাইভাল ঝুঁকি" else "Physiological Trauma Severity Score (GCS + SBP + RR)",
        isDark = isDark
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = gcsVal, onValueChange = { gcsVal = it },
                label = { Text("GCS (3-15)") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
            OutlinedTextField(
                value = sysVal, onValueChange = { sysVal = it },
                label = { Text("Systolic BP") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
            OutlinedTextField(
                value = rrVal, onValueChange = { rrVal = it },
                label = { Text("RR (/min)") }, modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp), colors = icuFieldColors()
            )
        }

        Spacer(Modifier.height(14.dp))

        IcuResultBox(
            primaryText = "RTS Score: %.2f / 7.84".format(rtsScore),
            secondaryText = if (rtsScore >= 7.0) (if (isBn) "উচ্চ সার্ভাইভাল সম্ভাবনা (High Survival Probability)" else "High Survival Probability") else (if (isBn) "⚠️ ট্রমা সেন্টার ট্রান্সফার প্রয়োজন (Major Trauma)" else "⚠️ Major Trauma - Immediate Transfer"),
            noteText = if (isBn) "সর্বোচ্চ RTS স্কোর ৭.৮৪। কম স্কোর ট্রমার উচ্চ তীব্রতা নির্দেশ করে।" else "Max RTS = 7.84. Lower score indicates higher mortality risk.",
            isDark = isDark
        )
    }
}

// ---------------- HELPER COMPONENTS ----------------

@Composable
fun IcuCard(title: String, subtitle: String, isDark: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.92f),
        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFFEF4444).copy(alpha = 0.3f)),
        content = {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(title, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black, fontSize = 17.sp)
                Text(subtitle, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF334155), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))
                content()
            }
        }
    )
}

@Composable
fun IcuResultBox(primaryText: String, secondaryText: String, noteText: String, isDark: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = (if (isDark) Color(0xFF38BDF8) else Color(0xFFEF4444)).copy(alpha = 0.12f),
        border = BorderStroke(1.5.dp, (if (isDark) Color(0xFF38BDF8) else Color(0xFFEF4444)).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = primaryText,
                color = if (isDark) Color(0xFF38BDF8) else Color(0xFFDC2626),
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = secondaryText,
                color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = (if (isDark) Color(0xFF38BDF8) else Color(0xFFEF4444)).copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "💡 $noteText",
                    color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF7F1D1D),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun icuFieldColors(): TextFieldColors {
    val isDark = isAppInDarkTheme()
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
        focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.5f),
        unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.3f),
        focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFFEF4444),
        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.5f),
        focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFFEF4444),
        unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
    )
}
