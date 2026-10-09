package com.srcreation.smarthelp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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

class NurseDoseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                NurseDoseScreen { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NurseDoseScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var selectedTab by remember { mutableIntStateOf(0) }

    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }

    val tabs = remember(isBn) {
        if (isBn) listOf(
            "সিরাপ ও ওরাল 🍼",
            "ইনজেকশন (IV/IM) 💉",
            "আইভি ড্রিপ রেট (gtt) 💧",
            "ফ্লুইড (৪-২-১ রুল) 🧪",
            "কিডনি ডোজ (CrCl) 🫘"
        ) else listOf(
            "Oral / Syrup 🍼",
            "Injection (IV/IM) 💉",
            "IV Drip Rate (gtt) 💧",
            "Fluid (4-2-1) 🧪",
            "Renal CrCl 🫘"
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
                                text = stringResource(id = R.string.tool_dose_calc),
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

                    // Subtag Banner (Placed right below TopBar)
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = if (isDark) 0.25f else 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = if (isDark) 0.5f else 0.3f)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = if (isBn) "🌐 আন্তর্জাতিক ক্লিনিক্যাল ডোজ সিস্টেম" else "🌐 International Clinical Dosing System",
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
                            val chipBg = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.88f))
                            val textColor = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155))

                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedTab = index
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = chipBg,
                                border = BorderStroke(1.dp, if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFCBD5E1))),
                                modifier = Modifier.shadow(if (isSelected) 6.dp else 1.dp, RoundedCornerShape(16.dp))
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
                        0 -> SyrupDoseCalculator(isBn)
                        1 -> InjectionDoseCalculator(isBn)
                        2 -> IvDripRateCalculator(isBn)
                        3 -> MaintenanceFluidCalculator(isBn)
                        4 -> RenalCrClCalculator(isBn)
                    }
                }
            }
        }
    }
}

// 1. PEDIATRIC & ADULT WEIGHT-BASED ORAL DOSE
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyrupDoseCalculator(isBn: Boolean) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var weight by remember { mutableStateOf("") }
    var concentrationMg by remember { mutableStateOf("120") }
    var concentrationMl by remember { mutableStateOf("5") }
    var targetDoseMgKg by remember { mutableStateOf("15") }
    var frequency by remember { mutableStateOf(if (isBn) "TDS / Q8H (দিনে ৩ বার)" else "TDS / Q8H (3 Times/Day)") }
    var durationDays by remember { mutableStateOf("5") }

    val presets = listOf(
        "Paracetamol (120mg/5ml)" to Triple("120", "5", "15"),
        "Paracetamol DS (250mg/5ml)" to Triple("250", "5", "15"),
        "Amoxicillin (125mg/5ml)" to Triple("125", "5", "20"),
        "Amoxicillin (250mg/5ml)" to Triple("250", "5", "20"),
        "Azithromycin (200mg/5ml)" to Triple("200", "5", "10"),
        "Ibuprofen (100mg/5ml)" to Triple("100", "5", "10"),
        "Cefixime (100mg/5ml)" to Triple("100", "5", "8")
    )

    Dose3DCard {
        DoseSectionTitle(
            if (isBn) "ওরাল ড্রাগ ডোজ (ওজন অনুযায়ী)" else "Oral Drug Dose (Weight-Based)",
            if (isBn) "আন্তর্জাতিক শিশু ও বয়স্কদের সিরাপ গাইড" else "International Pediatric & Adult Syrup Guide"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Drug Presets Chips
        Text(if (isBn) "কমন ড্রাগ প্রিসেটসমূহ:" else "International Drug Presets:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(presets) { _, preset ->
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        concentrationMg = preset.second.first
                        concentrationMl = preset.second.second
                        targetDoseMgKg = preset.second.third
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF1A237E).copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                ) {
                    Text(
                        text = preset.first,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text(if (isBn) "রোগীর ওজন (কেজি)" else "Patient Weight (kg)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = doseFieldColors(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = targetDoseMgKg,
                onValueChange = { targetDoseMgKg = it },
                label = { Text(if (isBn) "ডোজ (মিগ্রা/কেজি)" else "Dose (mg/kg)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = doseFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = concentrationMg,
                onValueChange = { concentrationMg = it },
                label = { Text(if (isBn) "স্টক (মিগ্রা)" else "Stock (mg)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = doseFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = concentrationMl,
                onValueChange = { concentrationMl = it },
                label = { Text(if (isBn) "প্রতি (মিমি)" else "per (ml)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = doseFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val frequencies = if (isBn) listOf("OD / Q24H (দিনে ১ বার)", "BD / Q12H (দিনে ২ বার)", "TDS / Q8H (দিনে ৩ বার)", "QID / Q6H (দিনে ৪ বার)")
        else listOf("OD / Q24H (Once Daily)", "BD / Q12H (Twice Daily)", "TDS / Q8H (3 Times/Day)", "QID / Q6H (4 Times/Day)")
        var expanded by remember { mutableStateOf(false) }

        Text(if (isBn) "সেবন মাত্রা / ফ্রিকোয়েন্সি:" else "Frequency:", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
        Box {
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    expanded = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
            ) {
                Text(frequency, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                frequencies.forEach { f ->
                    DropdownMenuItem(text = { Text(f, fontSize = 12.sp) }, onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        frequency = f
                        expanded = false
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = durationDays,
            onValueChange = { durationDays = it },
            label = { Text(if (isBn) "মেয়াদ (দিন)" else "Duration (Days)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = doseFieldColors(),
            shape = RoundedCornerShape(14.dp)
        )
    }

    val w = weight.toDoubleOrNull() ?: 0.0
    val d = targetDoseMgKg.toDoubleOrNull() ?: 0.0
    val cmg = concentrationMg.toDoubleOrNull() ?: 1.0
    val cml = concentrationMl.toDoubleOrNull() ?: 1.0

    val singleDoseMg = w * d
    val singleDoseMl = if (cmg > 0) (singleDoseMg / cmg) * cml else 0.0

    val timesPerDay = when {
        frequency.contains("Q24H") || frequency.contains("OD") -> 1
        frequency.contains("Q12H") || frequency.contains("BD") -> 2
        frequency.contains("Q8H") || frequency.contains("TDS") -> 3
        else -> 4
    }

    val totalDailyMg = singleDoseMg * timesPerDay

    val prescription = if (singleDoseMl > 0) {
        if (isBn)
            "নির্দেশনা: দিন ${String.format(Locale.US, "%.1f", singleDoseMl)} মিমি (${String.format(Locale.US, "%.0f", singleDoseMg)} মিগ্রা) ওরাল ${frequency.split(" ")[0]} (${durationDays} দিনের জন্য)। দৈনিক মোট: ${String.format(Locale.US, "%.0f", totalDailyMg)} মিগ্রা/দিন"
        else
            "Rx: Give ${String.format(Locale.US, "%.1f", singleDoseMl)} ml (${String.format(Locale.US, "%.0f", singleDoseMg)} mg) PO ${frequency.split(" ")[0]} for $durationDays Days. (Total Daily: ${String.format(Locale.US, "%.0f", totalDailyMg)} mg/day)"
    } else {
        if (isBn) "হিসাব করতে রোগীর ওজন দিন" else "Enter weight to calculate dose"
    }

    ResultCard(if (isBn) "একক ডোজ ভলিউম" else "Single Dose Volume", "${String.format(Locale.US, "%.2f", singleDoseMl)} ml", prescription)
}

// 2. PARENTERAL & INJECTION DOSE (IV/IM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InjectionDoseCalculator(isBn: Boolean) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var desiredMg by remember { mutableStateOf("") }
    var vialMg by remember { mutableStateOf("1000") }
    var diluentMl by remember { mutableStateOf("10") }

    val injPresets = listOf(
        "Ceftriaxone (1000mg/10ml)" to Pair("1000", "10"),
        "Hydrocortisone (100mg/2ml)" to Pair("100", "2"),
        "Furosemide (20mg/2ml)" to Pair("20", "2"),
        "Ondansetron (4mg/2ml)" to Pair("4", "2"),
        "Tramadol (100mg/2ml)" to Pair("100", "2"),
        "Amikacin (500mg/2ml)" to Pair("500", "2")
    )

    Dose3DCard {
        DoseSectionTitle(
            if (isBn) "ইনজেকশন ডোজ (IV/IM)" else "Parenteral / Injection Dose",
            if (isBn) "ইনজেকশন ভলিউম ও ডাইলিউশন ক্যালকুলেটর" else "Injection Volume & Reconstitution Guide"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(if (isBn) "কমন ইনজেকশন প্রিসেটসমূহ:" else "Common Injection Presets:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(injPresets) { _, preset ->
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        vialMg = preset.second.first
                        diluentMl = preset.second.second
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0284C7).copy(alpha = if (isDark) 0.25f else 0.1f),
                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = if (isDark) 0.5f else 0.2f))
                ) {
                    Text(
                        text = preset.first,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = desiredMg,
            onValueChange = { desiredMg = it },
            label = { Text(if (isBn) "কাঙ্ক্ষিত ডোজ (মিগ্রা)" else "Desired Dose (mg)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = doseFieldColors(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = vialMg,
                onValueChange = { vialMg = it },
                label = { Text(if (isBn) "ভায়াল স্টক (মিগ্রা)" else "Vial Stock (mg)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = doseFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = diluentMl,
                onValueChange = { diluentMl = it },
                label = { Text(if (isBn) "ডাইলুয়েন্ট (মিমি)" else "Diluent Vol (ml)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = doseFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
        }
    }

    val d = desiredMg.toDoubleOrNull() ?: 0.0
    val v = vialMg.toDoubleOrNull() ?: 1.0
    val m = diluentMl.toDoubleOrNull() ?: 1.0

    val withdrawalVolume = if (d > 0 && v > 0) (d / v) * m else 0.0

    val instruction = if (withdrawalVolume > 0) {
        if (isBn)
            "রিকনস্টিটিউট ভায়াল থেকে ${String.format(Locale.US, "%.2f", withdrawalVolume)} মিমি সিরিঞ্জে তুলুন। স্লো আইভি পুশ করুন (৩-৫ মিনিটে)।"
        else
            "Withdraw ${String.format(Locale.US, "%.2f", withdrawalVolume)} ml from reconstituted vial. Slow IV Push (over 3-5 mins)."
    } else {
        if (isBn) "হিসাব করতে কাঙ্ক্ষিত ডোজ লিখুন" else "Enter desired dose to calculate volume"
    }

    ResultCard(if (isBn) "সিরিঞ্জে নেওয়ার ভলিউম" else "Withdrawal Volume", "${String.format(Locale.US, "%.2f", withdrawalVolume)} ml", instruction)
}

// 3. IV DRIP RATE & DROP CALCULATOR (gtt/min & ml/hr)
@Composable
fun IvDripRateCalculator(isBn: Boolean) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var totalVolumeMl by remember { mutableStateOf("1000") }
    var timeHours by remember { mutableStateOf("8") }
    var dropFactorGtt by remember { mutableStateOf("20") }

    val dropSets = if (isBn) listOf(
        "অ্যাডাল্ট ম্যাক্রোড্রিপ (20 gtt/ml)" to "20",
        "অ্যাডাল্ট ম্যাক্রোড্রিপ (15 gtt/ml)" to "15",
        "প্যাডিয়েট্রিক মাইক্রোড্রিপ (60 gtt/ml)" to "60",
        "ব্লাড ট্রান্সফিউশন (10 gtt/ml)" to "10"
    ) else listOf(
        "Adult Macrodrip (20 gtt/ml)" to "20",
        "Adult Macrodrip (15 gtt/ml)" to "15",
        "Pediatric Microdrip (60 gtt/ml)" to "60",
        "Blood Transfusion (10 gtt/ml)" to "10"
    )

    Dose3DCard {
        DoseSectionTitle(
            if (isBn) "আইভি ড্রিপ রেট ক্যালকুলেটর" else "IV Drip Rate Calculator",
            if (isBn) "আইভি ড্রিপ রেট ও ড্রপ কাউন্টার (gtt/min)" else "IV Drip Rate & Flow Counter (gtt/min)"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = totalVolumeMl,
                onValueChange = { totalVolumeMl = it },
                label = { Text(if (isBn) "মোট ফ্লুইড (মিমি)" else "Total Fluid (ml)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = doseFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = timeHours,
                onValueChange = { timeHours = it },
                label = { Text(if (isBn) "সময় (ঘণ্টা)" else "Time (Hours)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = doseFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(if (isBn) "আইভি ড্রিপ সেট সিলেক্ট করুন:" else "Select IV Set Drop Factor:", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(dropSets) { _, set ->
                val isSel = dropFactorGtt == set.second
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        dropFactorGtt = set.second
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) Color(0xFF22C55E) else (if (isDark) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFF22C55E).copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, Color(0xFF22C55E))
                ) {
                    Text(
                        text = set.first,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else (if (isDark) Color(0xFF4ADE80) else Color(0xFF22C55E)),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    val vol = totalVolumeMl.toDoubleOrNull() ?: 0.0
    val hr = timeHours.toDoubleOrNull() ?: 1.0
    val gttFactor = dropFactorGtt.toDoubleOrNull() ?: 20.0

    val dropsPerMin = if (vol > 0 && hr > 0) (vol * gttFactor) / (hr * 60) else 0.0
    val mlPerHour = if (vol > 0 && hr > 0) vol / hr else 0.0

    val instruction = if (dropsPerMin > 0) {
        if (isBn)
            "আইভি ড্রিপ সেট করুন ${dropsPerMin.toInt()} ড্রপ/মি (ইনফিউশন পাম্পে: ${String.format(Locale.US, "%.1f", mlPerHour)} মিমি/ঘণ্টা)।"
        else
            "Set IV Drip at ${dropsPerMin.toInt()} drops/min (${String.format(Locale.US, "%.1f", mlPerHour)} ml/hr on Infusion Pump)."
    } else {
        if (isBn) "হিসাব করতে মোট ফ্লুইড ও সময় লিখুন" else "Enter volume and hours to calculate"
    }

    ResultCard(if (isBn) "আইভি ড্রিপ রেট" else "IV Drop Rate", "${dropsPerMin.toInt()} gtt/min", instruction)
}

// 4. MAINTENANCE FLUID (4-2-1 RULE)
@Composable
fun MaintenanceFluidCalculator(isBn: Boolean) {
    var weight by remember { mutableStateOf("") }

    Dose3DCard {
        DoseSectionTitle(
            if (isBn) "দৈনিক আইভি ফ্লুইড (৪-২-১ রুল)" else "Maintenance Fluid (4-2-1 Rule)",
            if (isBn) "হলিডে-সিগার গাইড অনুযায়ী আইভি ইনফিউশন" else "Holliday-Segar Daily IV Infusion Guide"
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text(if (isBn) "রোগীর ওজন (কেজি)" else "Patient Weight (kg)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = doseFieldColors(),
            shape = RoundedCornerShape(14.dp)
        )
    }

    val w = weight.toDoubleOrNull() ?: 0.0
    val mlPerHour = when {
        w <= 0 -> 0.0
        w <= 10 -> w * 4.0
        w <= 20 -> 40.0 + (w - 10.0) * 2.0
        else -> 60.0 + (w - 20.0) * 1.0
    }

    val mlPer24hr = mlPerHour * 24.0

    val instruction = if (mlPerHour > 0) {
        if (isBn)
            "আইভি ইনফিউশন চালু রাখুন ${mlPerHour.toInt()} মিমি/ঘণ্টা (২৪ ঘণ্টার মোট চাহিদা: ${mlPer24hr.toInt()} মিমি/দিন)।"
        else
            "Maintain IV Infusion at ${mlPerHour.toInt()} ml/hr (Total 24-Hour Requirement: ${mlPer24hr.toInt()} ml/day)."
    } else {
        if (isBn) "হিসাব করতে ওজন লিখুন" else "Enter weight to calculate maintenance fluid"
    }

    ResultCard(if (isBn) "ইনফিউশন রেট" else "Infusion Rate", "${mlPerHour.toInt()} ml/hr", instruction)
}

// 5. RENAL DOSE ADJUSTMENT (CrCl / COCKCROFT-GAULT)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenalCrClCalculator(isBn: Boolean) {
    val isDark = isSystemInDarkTheme()
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var serumCreatinine by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }

    Dose3DCard {
        DoseSectionTitle(
            if (isBn) "কিডনি ক্লিয়ারেন্স ক্যালকুলেটর (CrCl)" else "Renal CrCl Calculator (Cockcroft-Gault)",
            if (isBn) "কিডনি ফাংশন অনুযায়ী ড্রাগ ডোজ সামঞ্জস্য" else "Drug Dose Adjustment by Renal Function"
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                onClick = { gender = "Male" },
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (gender == "Male") (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (isBn) "পুরুষ ♂" else "Male ♂", color = if (gender == "Male") (if (isDark) Color.Black else Color.White) else (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)), fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                onClick = { gender = "Female" },
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (gender == "Female") Color(0xFFEC4899) else (if (isDark) Color(0xFF1E293B) else Color.White),
                border = BorderStroke(1.dp, Color(0xFFEC4899))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (isBn) "নারী ♀" else "Female ♀", color = if (gender == "Female") Color.White else Color(0xFFEC4899), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text(if (isBn) "বয়স (বছর)" else "Age (Years)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = doseFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text(if (isBn) "ওজন (কেজি)" else "Weight (kg)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = doseFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = serumCreatinine,
            onValueChange = { serumCreatinine = it },
            label = { Text(if (isBn) "সিরম ক্রিয়েটিনিন (mg/dL)" else "Serum Creatinine (mg/dL)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = doseFieldColors(),
            shape = RoundedCornerShape(14.dp)
        )
    }

    val a = age.toDoubleOrNull() ?: 0.0
    val w = weight.toDoubleOrNull() ?: 0.0
    val scr = serumCreatinine.toDoubleOrNull() ?: 0.0

    val crCl = if (a > 0 && w > 0 && scr > 0) {
        val base = ((140.0 - a) * w) / (72.0 * scr)
        if (gender == "Female") base * 0.85 else base
    } else 0.0

    val stageText = when {
        crCl <= 0 -> if (isBn) "ইনপুট দিন" else "Awaiting Input"
        crCl >= 90 -> if (isBn) "স্বাভাবিক কিডনি ফাংশন (eGFR > 90 ml/min)" else "Normal Renal Function (eGFR > 90 ml/min)"
        crCl >= 60 -> if (isBn) "সামান্য মৃদু দুর্বলতা (60-89 ml/min)" else "Mild Renal Impairment (60-89 ml/min)"
        crCl >= 30 -> if (isBn) "মাঝারি দুর্বলতা (30-59 ml/min) - ডোজ ৫০% কমান" else "Moderate Impairment (30-59 ml/min) - Reduce Dose 50%"
        else -> if (isBn) "গুরুতর দুর্বলতা (<30 ml/min) - বিশেষজ্ঞ পরামর্শ নিন" else "Severe Impairment (<30 ml/min) - Consult Nephrologist"
    }

    ResultCard(if (isBn) "ক্রিয়েটিনিন ক্লিয়ারেন্স" else "Creatinine Clearance", "${crCl.toInt()} ml/min", stageText)
}

// ---------------- 3D HELPER COMPONENTS ----------------

@Composable
fun Dose3DCard(content: @Composable ColumnScope.() -> Unit) {
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.92f),
        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White),
        content = {
            Column(modifier = Modifier.padding(20.dp), content = content)
        }
    )
}

@Composable
fun DoseSectionTitle(title: String, subtitle: String) {
    val isDark = isSystemInDarkTheme()
    Column {
        Text(title, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(subtitle, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ResultCard(label: String, result: String, subtext: String) {
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E)),
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.95f),
        border = BorderStroke(1.5.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color(0xFF1A237E).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(result, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), fontSize = 38.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF1A237E).copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = subtext,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun doseFieldColors(): TextFieldColors {
    val isDark = isSystemInDarkTheme()
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = if (isDark) Color.White else Color.Black,
        unfocusedTextColor = if (isDark) Color.White else Color.Black,
        focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.5f),
        focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
        unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
        focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent,
        unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent
    )
}
