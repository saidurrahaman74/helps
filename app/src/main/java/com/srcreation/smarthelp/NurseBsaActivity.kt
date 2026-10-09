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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

class NurseBsaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                BsaCalculatorScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BsaCalculatorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var height by remember { mutableStateOf("170") }
    var weight by remember { mutableStateOf("65") }
    var selectedFormula by remember { mutableIntStateOf(0) } // 0: Mosteller, 1: Du Bois, 2: Haycock, 3: Gehan
    var targetDoseMgM2 by remember { mutableStateOf("") } // Optional Chemotherapy dose (mg/m2)

    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }

    val formulas = remember(isBn) {
        if (isBn) listOf(
            "মোস্টেলার (Mosteller - গ্লোবাল)" to 0,
            "ডু বোয়স (Du Bois)" to 1,
            "হেকক (Haycock - শিশু)" to 2,
            "গেহান ও জর্জ (Gehan)" to 3
        ) else listOf(
            "Mosteller (Gold Standard)" to 0,
            "Du Bois (Classic)" to 1,
            "Haycock (Pediatrics)" to 2,
            "Gehan & George" to 3
        )
    }

    val h = height.toDoubleOrNull() ?: 0.0
    val w = weight.toDoubleOrNull() ?: 0.0

    val bsaValue = remember(h, w, selectedFormula) {
        if (h > 0 && w > 0) {
            when (selectedFormula) {
                0 -> sqrt((h * w) / 3600.0) // Mosteller
                1 -> 0.007184 * h.pow(0.725) * w.pow(0.425) // Du Bois
                2 -> 0.024265 * h.pow(0.3964) * w.pow(0.5378) // Haycock
                else -> 0.0235 * h.pow(0.42246) * w.pow(0.51456) // Gehan & George
            }
        } else 0.0
    }

    val totalDrugDoseMg = remember(bsaValue, targetDoseMgM2) {
        val mgM2 = targetDoseMgM2.toDoubleOrNull() ?: 0.0
        if (bsaValue > 0 && mgM2 > 0) bsaValue * mgM2 else 0.0
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
                                text = if (isBn) "বিএসএ ক্যালকুলেটর" else "BSA Calculator",
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
                            text = if (isBn) "🌐 আন্তর্জাতিক বডি সারফেস এরিয়া ও কিমোথেরাপি গাইড (NCI / EORTC)" else "🌐 Body Surface Area & Chemotherapy Dosing Standard (NCI / EORTC)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
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
                    // Result Display Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E)),
                        shape = RoundedCornerShape(28.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.95f),
                        border = BorderStroke(1.5.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color(0xFF1A237E).copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isBn) "আনুমানিক বডি সারফেস এরিয়া (BSA)" else "Estimated Body Surface Area (BSA)",
                                fontSize = 13.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${String.format(Locale.US, "%.2f", bsaValue)} m²",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF1A237E).copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = when (selectedFormula) {
                                        0 -> "Mosteller Formula: √((Ht cm × Wt kg) / 3600)"
                                        1 -> "Du Bois Formula: 0.007184 × Ht^0.725 × Wt^0.425"
                                        2 -> "Haycock Formula (Pediatric): 0.024265 × Ht^0.3964 × Wt^0.5378"
                                        else -> "Gehan & George Formula: 0.0235 × Ht^0.42246 × Wt^0.51456"
                                    },
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Input Form
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(10.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.92f),
                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text(
                                text = if (isBn) "আন্তর্জাতিক ফর্মুলা নির্বাচন করুন:" else "Select International Formula:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                itemsIndexed(formulas) { _, (label, code) ->
                                    val isSel = selectedFormula == code
                                    Surface(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedFormula = code
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSel) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF1A237E).copy(alpha = 0.08f)),
                                        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) (if (isDark) Color.Black else Color.White) else (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = height,
                                    onValueChange = { height = it },
                                    label = { Text(if (isBn) "উচ্চতা (সেমি)" else "Height (cm)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = bsaFieldColors()
                                )

                                OutlinedTextField(
                                    value = weight,
                                    onValueChange = { weight = it },
                                    label = { Text(if (isBn) "ওজন (কেজি)" else "Weight (kg)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = bsaFieldColors()
                                )
                            }

                            HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.4f))

                            // Optional Chemotherapy / High-Alert Drug Dose Calculator
                            Text(
                                text = if (isBn) "কিমোথেরাপি / ড্রাগ ডোজ ক্যালকুলেটর (ঐচ্ছিক):" else "Chemotherapy / Drug Dose Calculator (Optional):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                            )

                            OutlinedTextField(
                                value = targetDoseMgM2,
                                onValueChange = { targetDoseMgM2 = it },
                                label = { Text(if (isBn) "অর্ডারকৃত ডোজ (mg/m²)" else "Ordered Dose (mg/m²)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                colors = bsaFieldColors()
                            )

                            if (totalDrugDoseMg > 0) {
                                Surface(
                                    color = Color(0xFF0284C7).copy(alpha = if (isDark) 0.2f else 0.1f),
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = if (isDark) 0.4f else 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (isBn) "মোট প্রদেয় ড্রাগ ডোজ:" else "Total Calculated Patient Dose:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", totalDrugDoseMg)} mg",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                        )
                                        Text(
                                            text = if (isBn) "(${String.format(Locale.US, "%.2f", bsaValue)} m² × $targetDoseMgM2 mg/m²)" else "(${String.format(Locale.US, "%.2f", bsaValue)} m² × $targetDoseMgM2 mg/m²)",
                                            fontSize = 10.sp,
                                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Reference Info Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBn) "আন্তর্জাতিক রেফারেন্স মান (BSA Averages):" else "International Reference Averages:",
                                    fontWeight = FontWeight.Black,
                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = if (isBn)
                                    "• প্রাপ্তবয়স্ক পুরুষ (Adult Men): ১.৯ m²\n• প্রাপ্তবয়স্ক নারী (Adult Women): ১.৬ m²\n• শিশু (Child 9 yrs): ১.০৭ m²\n• শিশু (Child 1 yr): ০.৪৫ m²\n• নবজাতক (Newborn): ০.২৫ m²"
                                else
                                    "• Adult Men: 1.9 m²\n• Adult Women: 1.6 m²\n• Child (9 yrs): 1.07 m²\n• Infant (1 yr): 0.45 m²\n• Newborn: 0.25 m²",
                                color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1E293B),
                                fontSize = 13.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun bsaFieldColors(): TextFieldColors {
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
