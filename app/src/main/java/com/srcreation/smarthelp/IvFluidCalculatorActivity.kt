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

class IvFluidCalculatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                IvFluidScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IvFluidScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var selectedTab by remember { mutableIntStateOf(0) }

    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }

    val tabs = remember(isBn) {
        if (isBn) listOf(
            "ড্রিপ রেট (gtt/min) 💧",
            "সময় / মেয়াদ (Hours) ⏱️",
            "ইনফিউজড ভলিউম (ml) 🧪",
            "ফ্লুইড (৪-২-১ রুল) 🍼"
        ) else listOf(
            "Drip Rate (gtt/min) 💧",
            "Duration (Hours) ⏱️",
            "Volume Delivered (ml) 🧪",
            "4-2-1 Maintenance 🍼"
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
                                text = "IV Fluid Master",
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
                            text = if (isBn) "🌐 আন্তর্জাতিক আইভি ফ্লুইড ইনফিউশন স্ট্যান্ডার্ড (INS / WHO)" else "🌐 International INS / WHO Infusion Standard",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    // Single Line Scrollable 3D Tabs
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
                        0 -> IvDripRateSection(isBn)
                        1 -> IvTimeCalculatorSection(isBn)
                        2 -> IvVolumeDeliveredSection(isBn)
                        3 -> IvPediatricFluidSection(isBn)
                    }
                }
            }
        }
    }
}

// 1. DRIP RATE CALCULATOR (gtt/min & ml/hr)
@Composable
fun IvDripRateSection(isBn: Boolean) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var volumeMl by remember { mutableStateOf("1000") }
    var timeHours by remember { mutableStateOf("8") }
    var dropFactor by remember { mutableStateOf("20") }

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

    Iv3DCard {
        IvTitle(
            if (isBn) "আইভি ড্রিপ রেট (gtt/min)" else "IV Drip Rate Calculator",
            if (isBn) "ফর্মুলা: (মোট ফ্লুইড × ড্রপ ফ্যাক্টর) ÷ (ঘণ্টা × ৬০)" else "Formula: (Total Vol × Drop Factor) ÷ (Hours × 60)"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = volumeMl,
                onValueChange = { volumeMl = it },
                label = { Text(if (isBn) "মোট ফ্লুইড (ml)" else "Total Fluid (ml)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = ivFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = timeHours,
                onValueChange = { timeHours = it },
                label = { Text(if (isBn) "সময় (ঘণ্টা)" else "Time (Hours)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = ivFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(if (isBn) "আইভি ড্রিপ সেট সিলেক্ট করুন:" else "Select IV Set Drop Factor:", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(dropSets) { _, set ->
                val isSel = dropFactor == set.second
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        dropFactor = set.second
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

    val v = volumeMl.toDoubleOrNull() ?: 0.0
    val t = timeHours.toDoubleOrNull() ?: 1.0
    val f = dropFactor.toDoubleOrNull() ?: 20.0

    val gttMin = if (v > 0 && t > 0) (v * f) / (t * 60) else 0.0
    val mlHr = if (v > 0 && t > 0) v / t else 0.0

    val subtext = if (gttMin > 0) {
        if (isBn) "ইনফিউশন পাম্প ফ্লো রেট: ${String.format(Locale.US, "%.1f", mlHr)} মিমি/ঘণ্টা | প্রতিটি ড্রপ ইন্টারভাল: ${String.format(Locale.US, "%.1f", 60.0 / gttMin)} সেকেন্ড"
        else "Infusion Pump Rate: ${String.format(Locale.US, "%.1f", mlHr)} ml/hr | Drop Interval: ${String.format(Locale.US, "%.1f", 60.0 / gttMin)} sec/drop"
    } else if (isBn) "হিসাব করতে মোট ফ্লুইড ও সময় লিখুন" else "Enter volume and time to calculate"

    IvResultCard(
        label = if (isBn) "আইভি ড্রিপ রেট (gtt/min)" else "Required IV Drip Rate",
        result = "${gttMin.toInt()} gtt/min",
        subtext = subtext
    )
}

// 2. INFUSION DURATION / TIME CALCULATOR
@Composable
fun IvTimeCalculatorSection(isBn: Boolean) {
    var volumeMl by remember { mutableStateOf("1000") }
    var gttMin by remember { mutableStateOf("42") }
    var dropFactor by remember { mutableStateOf("20") }

    Iv3DCard {
        IvTitle(
            if (isBn) "ইনফিউশন সময় ক্যালকুলেটর" else "Infusion Duration Calculator",
            if (isBn) "ফর্মুলা: (মোট ফ্লুইড × ড্রপ ফ্যাক্টর) ÷ (gtt/min × ৬০)" else "Formula: (Total Vol × Drop Factor) ÷ (gtt/min × 60)"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = volumeMl,
                onValueChange = { volumeMl = it },
                label = { Text(if (isBn) "মোট ফ্লুইড (ml)" else "Total Fluid (ml)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = ivFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = gttMin,
                onValueChange = { gttMin = it },
                label = { Text(if (isBn) "ড্রিপ রেট (gtt/min)" else "Drip Rate (gtt/min)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = ivFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
        }
    }

    val v = volumeMl.toDoubleOrNull() ?: 0.0
    val gtt = gttMin.toDoubleOrNull() ?: 0.0
    val f = dropFactor.toDoubleOrNull() ?: 20.0

    val totalHours = if (v > 0 && gtt > 0) (v * f) / (gtt * 60) else 0.0
    val hoursPart = totalHours.toInt()
    val minsPart = ((totalHours - hoursPart) * 60).toInt()

    val subtext = if (totalHours > 0) {
        if (isBn) "ফ্লুইড শেষ হতে সময় লাগবে $hoursPart ঘণ্টা $minsPart মিনিট।"
        else "Total duration required: $hoursPart Hours $minsPart Mins."
    } else if (isBn) "হিসাব করতে ড্রিপ রেট দিন" else "Enter drip rate to calculate duration"

    IvResultCard(
        label = if (isBn) "প্রয়োজনীয় সময়" else "Infusion Duration",
        result = "$hoursPart hrs $minsPart mins",
        subtext = subtext
    )
}

// 3. TOTAL VOLUME DELIVERED CALCULATOR
@Composable
fun IvVolumeDeliveredSection(isBn: Boolean) {
    var gttMin by remember { mutableStateOf("30") }
    var hours by remember { mutableStateOf("6") }
    var dropFactor by remember { mutableStateOf("20") }

    Iv3DCard {
        IvTitle(
            if (isBn) "মোট ইনফিউজড ফ্লুইড (ml)" else "Total Infused Volume (ml)",
            if (isBn) "ফর্মুলা: (gtt/min × মিনিট × ৬০) ÷ ড্রপ ফ্যাক্টর" else "Formula: (gtt/min × Mins) ÷ Drop Factor"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = gttMin,
                onValueChange = { gttMin = it },
                label = { Text(if (isBn) "ড্রিপ রেট (gtt/min)" else "Drip Rate (gtt/min)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = ivFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = hours,
                onValueChange = { hours = it },
                label = { Text(if (isBn) "অতিক্রান্ত সময় (ঘণ্টা)" else "Elapsed Hours") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = ivFieldColors(),
                shape = RoundedCornerShape(14.dp)
            )
        }
    }

    val gtt = gttMin.toDoubleOrNull() ?: 0.0
    val hr = hours.toDoubleOrNull() ?: 0.0
    val f = dropFactor.toDoubleOrNull() ?: 20.0

    val totalVolumeMl = if (gtt > 0 && hr > 0) (gtt * hr * 60) / f else 0.0

    val subtext = if (totalVolumeMl > 0) {
        if (isBn) "রোগী $hr ঘণ্টায় মোট ${totalVolumeMl.toInt()} মিমি ফ্লুইড গ্রহণ করেছেন।"
        else "Patient received ${totalVolumeMl.toInt()} ml fluid in $hr hours."
    } else if (isBn) "হিসাব করতে সময় ও রেট লিখুন" else "Enter rate and hours to calculate"

    IvResultCard(
        label = if (isBn) "মোট গ্রহণকৃত ফ্লুইড" else "Total Delivered Volume",
        result = "${totalVolumeMl.toInt()} ml",
        subtext = subtext
    )
}

// 4. PEDIATRIC 4-2-1 MAINTENANCE FLUID
@Composable
fun IvPediatricFluidSection(isBn: Boolean) {
    var weight by remember { mutableStateOf("") }

    Iv3DCard {
        IvTitle(
            if (isBn) "দৈনিক ফ্লুইড (৪-২-১ রুল)" else "Pediatric 4-2-1 Fluid Rule",
            if (isBn) "আন্তর্জাতিক হলিডে-সিগার শিশু ও বয়স্ক ফ্লুইড" else "Holliday-Segar Fluid Maintenance Standard"
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text(if (isBn) "রোগীর ওজন (কেজি)" else "Patient Weight (kg)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = ivFieldColors(),
            shape = RoundedCornerShape(14.dp)
        )
    }

    val w = weight.toDoubleOrNull() ?: 0.0
    val mlHr = when {
        w <= 0 -> 0.0
        w <= 10 -> w * 4.0
        w <= 20 -> 40.0 + (w - 10.0) * 2.0
        else -> 60.0 + (w - 20.0) * 1.0
    }

    val mlDay = mlHr * 24.0

    val subtext = if (mlHr > 0) {
        if (isBn) "২৪ ঘণ্টার মোট আইভি ফ্লুইড চাহিদা: ${mlDay.toInt()} মিমি/দিন।"
        else "24-Hour Total Requirement: ${mlDay.toInt()} ml/day."
    } else if (isBn) "হিসাব করতে রোগীর ওজন দিন" else "Enter weight to calculate"

    IvResultCard(
        label = if (isBn) "ইনফিউশন স্পিড" else "Infusion Rate",
        result = "${mlHr.toInt()} ml/hr",
        subtext = subtext
    )
}

// ---------------- 3D HELPER COMPONENTS ----------------

@Composable
fun Iv3DCard(content: @Composable ColumnScope.() -> Unit) {
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
fun IvTitle(title: String, subtitle: String) {
    val isDark = isSystemInDarkTheme()
    Column {
        Text(title, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(subtitle, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun IvResultCard(label: String, result: String, subtext: String) {
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
fun ivFieldColors(): TextFieldColors {
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
