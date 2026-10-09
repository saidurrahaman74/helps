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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.util.Locale

class NurseApgarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                ApgarCalculatorScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApgarCalculatorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var appearance by remember { mutableIntStateOf(2) }
    var pulse by remember { mutableIntStateOf(2) }
    var grimace by remember { mutableIntStateOf(2) }
    var activity by remember { mutableIntStateOf(2) }
    var respiration by remember { mutableIntStateOf(2) }

    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }

    val totalScore = appearance + pulse + grimace + activity + respiration

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
                                text = "APGAR Score",
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
                            text = if (isBn) "🌐 নবজাতকের স্বাস্থ্য ও অ্যাপগার স্কেল (Neonatal APGAR Score)" else "🌐 Newborn APGAR Clinical Score Assessment",
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
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Result Header
                    ApgarResultCard(totalScore, isBn)

                    // 1. Appearance (Skin Color)
                    ApgarSelectionSection(
                        title = if (isBn) "A - অ্যাピアরেন্স / ত্বকের রঙ (Appearance)" else "A - Appearance (Skin Color)",
                        options = if (isBn) listOf(
                            "সম্পূর্ণ গোলাপি (স্বাভাবিক)" to 2,
                            "শরীর গোলাপি, হাত-পা নীলচে" to 1,
                            "সম্পূর্ণ নীল বা ফ্যাকাশে (সায়ানোসটিক)" to 0
                        ) else listOf(
                            "Pink all over (Normal)" to 2,
                            "Pink body, Blue extremities" to 1,
                            "Blue / Pale all over (Cyanotic)" to 0
                        ),
                        selectedScore = appearance
                    ) { appearance = it }

                    // 2. Pulse (Heart Rate)
                    ApgarSelectionSection(
                        title = if (isBn) "P - পালস / হৃদস্পন্দন (Pulse)" else "P - Pulse (Heart Rate)",
                        options = if (isBn) listOf(
                            "> ১০০ বিপিএম (স্বাভাবিক)" to 2,
                            "< ১০০ বিপিএম (ধীর স্পন্দন)" to 1,
                            "হৃদস্পন্দন অনুপস্থিত (০ বিপিএম)" to 0
                        ) else listOf(
                            "> 100 bpm (Normal)" to 2,
                            "< 100 bpm (Slow)" to 1,
                            "Absent (0 bpm)" to 0
                        ),
                        selectedScore = pulse
                    ) { pulse = it }

                    // 3. Grimace (Reflex Irritability)
                    ApgarSelectionSection(
                        title = if (isBn) "G - গ্রিমেস / রিফ্লেক্স প্রতিক্রিয়া (Grimace)" else "G - Grimace (Reflex Response)",
                        options = if (isBn) listOf(
                            "জোরালো কান্না / হাঁচি / কাশি" to 2,
                            "মুখ কুঁচকানো / মৃদু কান্না" to 1,
                            "কোনো প্রতিক্রিয়া নেই" to 0
                        ) else listOf(
                            "Cough / Sneeze / Vigorous cry" to 2,
                            "Grimace / Feeble cry" to 1,
                            "No response (Flaccid)" to 0
                        ),
                        selectedScore = grimace
                    ) { grimace = it }

                    // 4. Activity (Muscle Tone)
                    ApgarSelectionSection(
                        title = if (isBn) "A - এক্টিভিটি / মাসল টোন (Activity)" else "A - Activity (Muscle Tone)",
                        options = if (isBn) listOf(
                            "সক্রিয় নড়াচড়া ও হাত-পা ছোঁড়া" to 2,
                            "হাত-পা সামান্য ভাজ করে রাখা" to 1,
                            "সম্পূর্ণ শিথিল ও অসাড় (Limp)" to 0
                        ) else listOf(
                            "Active movement / Flexed limbs" to 2,
                            "Some flexion of extremities" to 1,
                            "Limp / Flaccid" to 0
                        ),
                        selectedScore = activity
                    ) { activity = it }

                    // 5. Respiration (Breathing)
                    ApgarSelectionSection(
                        title = if (isBn) "R - রেসপিরেশন / শ্বাসপ্রশ্বাস (Respiration)" else "R - Respiration (Breathing)",
                        options = if (isBn) listOf(
                            "জোরালো সুষম কান্না ও শ্বাসপ্রশ্বাস" to 2,
                            "ধীর, অনিয়মিত বা দুর্বল শ্বাস" to 1,
                            "শ্বাসপ্রশ্বাস অনুপস্থিত (Apnea)" to 0
                        ) else listOf(
                            "Strong cry / Regular breathing" to 2,
                            "Weak / Slow / Irregular breathing" to 1,
                            "Absent (Apnea)" to 0
                        ),
                        selectedScore = respiration
                    ) { respiration = it }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun ApgarResultCard(score: Int, isBn: Boolean) {
    val isDark = isSystemInDarkTheme()
    val (status, color, subtext) = when {
        score >= 7 -> Triple(
            if (isBn) "স্বাভাবিক ও সুস্থ নবজাতক" else "Normal / Healthy Newborn",
            Color(0xFF22C55E),
            if (isBn) "নবজাতক ঝুঁকিমুক্ত। সাধারণ পোস্ট-ন্যাটাল যত্ন ও বুকের দুধ প্রদান অব্যাহত রাখুন।" else "Newborn is in good condition. Continue routine post-natal care and breastfeeding."
        )
        score >= 4 -> Triple(
            if (isBn) "মাঝারি সংকটাপন্ন (Moderately Depressed)" else "Moderately Depressed",
            Color(0xFFFF9800),
            if (isBn) "তাত্ক্ষণিক এয়ারওয়ে সাকশন দিন, শরীর গরম রাখুন এবং অক্সিজেন ও উদ্দীপনা প্রদান করুন।" else "Provide immediate airway suctioning, tactile stimulation, and supplemental oxygen."
        )
        else -> Triple(
            if (isBn) "গুরুতর সংকটাপন্ন (Severely Depressed)" else "Severely Depressed (Critical)",
            Color(0xFFEF4444),
            if (isBn) "জরুরি কোড ব্লু / NRP রিসাসিটেশন শুরু করুন! ব্যাগ-মাস্ক ভেন্টিলেশন ও সিসিইউ সাপোর্ট প্রয়োজন।" else "IMMEDIATE NRP Resuscitation required! Start Bag-Valve-Mask ventilation and Call NICU."
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else color),
        shape = RoundedCornerShape(28.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.95f),
        border = BorderStroke(1.5.dp, if (isDark) color else color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(if (isBn) "মোট অ্যাপগার স্কোর (APGAR Score)" else "Total APGAR Score", fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$score / 10", fontSize = 48.sp, fontWeight = FontWeight.Black, color = color)
            Surface(
                color = color,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                color = color.copy(alpha = if (isDark) 0.2f else 0.1f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, color.copy(alpha = if (isDark) 0.4f else 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = subtext,
                        color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A),
                        fontSize = 12.sp,
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
fun ApgarSelectionSection(
    title: String,
    options: List<Pair<String, Int>>,
    selectedScore: Int,
    onSelect: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    Column {
        Text(
            text = title,
            fontWeight = FontWeight.Black,
            color = if (isDark) Color.White else Color(0xFF1A237E),
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (label, score) ->
                val isSelected = selectedScore == score
                val surfaceBg = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.88f))
                val labelColor = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A))
                val ptPillBg = if (isSelected) (if (isDark) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.2f)) else (if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF1A237E).copy(alpha = 0.08f))
                val ptPillText = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                val borderColor = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.4f))

                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelect(score)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = surfaceBg,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            color = labelColor,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = ptPillBg,
                            shape = CircleShape
                        ) {
                            Text(
                                text = if (score > 0) "+$score" else "0",
                                color = ptPillText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
