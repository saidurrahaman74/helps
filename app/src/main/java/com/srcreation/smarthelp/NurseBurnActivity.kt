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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

class NurseBurnActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                BurnCalculatorScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurnCalculatorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    
    var weight by remember { mutableStateOf("") }
    var head by remember { mutableStateOf(false) }
    var chest by remember { mutableStateOf(false) }
    var abdomen by remember { mutableStateOf(false) }
    var upperBack by remember { mutableStateOf(false) }
    var lowerBack by remember { mutableStateOf(false) }
    var leftArm by remember { mutableStateOf(false) }
    var rightArm by remember { mutableStateOf(false) }
    var leftLeg by remember { mutableStateOf(false) }
    var rightLeg by remember { mutableStateOf(false) }
    var groin by remember { mutableStateOf(false) }

    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }

    val totalPercentage = (if (head) 9 else 0) +
            (if (chest) 9 else 0) +
            (if (abdomen) 9 else 0) +
            (if (upperBack) 9 else 0) +
            (if (lowerBack) 9 else 0) +
            (if (leftArm) 9 else 0) +
            (if (rightArm) 9 else 0) +
            (if (leftLeg) 18 else 0) +
            (if (rightLeg) 18 else 0) +
            (if (groin) 1 else 0)

    val wt = weight.toDoubleOrNull() ?: 0.0
    // Parkland Formula: 4ml * weight (kg) * TBSA (%)
    val totalFluidMl = if (wt > 0 && totalPercentage > 0) 4.0 * wt * totalPercentage else 0.0
    val first8HoursMl = totalFluidMl / 2.0
    val next16HoursMl = totalFluidMl / 2.0
    val rateFirst8 = first8HoursMl / 8.0
    val rateNext16 = next16HoursMl / 16.0

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
                                text = if (isBn) "বার্ন ও পার্কল্যান্ড ফ্লুইড ক্যালকুলেটর" else "Burn & Parkland Formula Calculator",
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
                            text = if (isBn) "🌐 আন্তর্জাতিক বার্ন সেন্টার ও পার্কল্যান্ড ফর্মুলা (ABA Standard)" else "🌐 International Burn Center & Parkland Formula (ABA)",
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
                    // Result Card
                    BurnResultCard(totalPercentage, totalFluidMl, rateFirst8, rateNext16, isBn)

                    // Patient Weight Input for Parkland Fluid Resuscitation
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.28f),
                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = if (isBn) "রোগীর ওজন ইনপুট করুন (পার্কল্যান্ড ফ্লুইডের জন্য):" else "Patient Weight (for Parkland Resuscitation):",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = weight,
                                onValueChange = { weight = it },
                                label = { Text(if (isBn) "ওজন (kg)" else "Weight (kg)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                                    unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                                    focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.25f),
                                    unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.15f),
                                    focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.6f),
                                    focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                    unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                                )
                            )
                        }
                    }

                    Text(
                        text = if (isBn) "পোড়া অঙ্গ নির্বাচন করুন (রুল অফ নাইনস - Rule of 9s):" else "Select Affected Body Parts (Rule of 9s):",
                        color = if (isDark) Color.White else Color(0xFF1A237E),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    // Selection List
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BurnPartItem(if (isBn) "মাথা ও ঘাড় (Head & Neck - 9%)" else "Head & Neck (9%)", head) { head = it }
                        BurnPartItem(if (isBn) "বুক / সামনের ওপরের অংশ (Chest - 9%)" else "Chest / Front Upper Trunk (9%)", chest) { chest = it }
                        BurnPartItem(if (isBn) "পেট / সামনের নিচের অংশ (Abdomen - 9%)" else "Abdomen / Front Lower Trunk (9%)", abdomen) { abdomen = it }
                        BurnPartItem(if (isBn) "পিঠের ওপরের অংশ (Upper Back - 9%)" else "Upper Back (9%)", upperBack) { upperBack = it }
                        BurnPartItem(if (isBn) "পিঠের নিচের অংশ (Lower Back - 9%)" else "Lower Back (9%)", lowerBack) { lowerBack = it }
                        BurnPartItem(if (isBn) "বাম হাত (Left Arm - 9%)" else "Left Arm (Full - 9%)", leftArm) { leftArm = it }
                        BurnPartItem(if (isBn) "ডান হাত (Right Arm - 9%)" else "Right Arm (Full - 9%)", rightArm) { rightArm = it }
                        BurnPartItem(if (isBn) "বাম পা (Left Leg - 18%)" else "Left Leg (Full - 18%)", leftLeg) { leftLeg = it }
                        BurnPartItem(if (isBn) "ডান পা (Right Leg - 18%)" else "Right Leg (Full - 18%)", rightLeg) { rightLeg = it }
                        BurnPartItem(if (isBn) "কুঁচকি / যৌনাঙ্গ (Groin - 1%)" else "Genitalia / Groin (1%)", groin) { groin = it }
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun BurnResultCard(percentage: Int, totalFluid: Double, rate8: Double, rate16: Double, isBn: Boolean) {
    val isDark = isSystemInDarkTheme()
    val statusColor = when {
        percentage > 20 -> Color(0xFFEF4444)
        percentage > 10 -> Color(0xFFFF9800)
        else -> Color(0xFF0284C7)
    }

    val statusText = when {
        percentage > 20 -> if (isBn) "⚠️ মেজর বার্ন - আইভি ফ্লুইড ও আইসিইউ কেয়ার জরুরি!" else "⚠️ MAJOR BURN - ICU Care & IV Resuscitation Required"
        percentage > 10 -> if (isBn) "⚠️ মডারেট বার্ন - ফ্লুইড মনিটরিং প্রয়োজন" else "⚠️ Moderate Burn - Fluid Monitoring Required"
        percentage > 0 -> if (isBn) "✅ মাইনর বার্ন - প্রাথমিক চিকিৎসা" else "✅ Minor Burn - First Aid & Wound Care"
        else -> if (isBn) "অঙ্গ নির্বাচন করুন" else "Select affected areas above"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.28f),
        border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(if (isBn) "মোট পোড়া শরীর (TBSA %)" else "Total Body Surface Area (TBSA)", fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF334155), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$percentage%", fontSize = 52.sp, fontWeight = FontWeight.Black, color = statusColor)
            
            Surface(
                color = statusColor,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text(
                    text = statusText,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            if (totalFluid > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isBn) "🧪 পার্কল্যান্ড ফর্মুলা ফ্লুইড রিসাসিটেশন (Parkland Formula):" else "🧪 Parkland Formula Fluid Resuscitation:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isBn) "২৪ ঘণ্টার মোট ফ্লুইড: ${totalFluid.toInt()} ml (Ringer's Lactate)" else "Total 24h Fluid: ${totalFluid.toInt()} ml (Ringer's Lactate)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isBn) "• প্রথম ৮ ঘণ্টায় দিন: ${rate8.toInt()} ml/hr (${(totalFluid/2).toInt()} ml)" else "• First 8 Hours Rate: ${rate8.toInt()} ml/hr (${(totalFluid/2).toInt()} ml)",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isBn) "• পরবর্তী ১৬ ঘণ্টায় দিন: ${rate16.toInt()} ml/hr (${(totalFluid/2).toInt()} ml)" else "• Next 16 Hours Rate: ${rate16.toInt()} ml/hr (${(totalFluid/2).toInt()} ml)",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFF818CF8) else Color(0xFF1A237E),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BurnPartItem(label: String, isSelected: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val isDark = isSystemInDarkTheme()
    val containerBg = if (isSelected) {
        if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.7f) else Color(0xFFEF4444).copy(alpha = 0.18f)
    } else {
        if (isDark) Color(0xFF334155) else Color.White.copy(alpha = 0.88f)
    }

    val borderColor = if (isSelected) {
        Color(0xFFEF4444)
    } else {
        if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFFCBD5E1)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isSelected) 6.dp else 2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = containerBg,
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                color = if (isSelected) (if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B)) else (if (isDark) Color.White else Color(0xFF0F172A)),
                fontSize = 14.sp
            )
            Switch(
                checked = isSelected,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFEF4444),
                    uncheckedThumbColor = if (isDark) Color.White else Color.Gray,
                    uncheckedTrackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                    uncheckedBorderColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color.Gray
                )
            )
        }
    }
}
