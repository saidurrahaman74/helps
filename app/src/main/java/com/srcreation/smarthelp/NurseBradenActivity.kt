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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.util.Locale

class NurseBradenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                BradenScaleScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BradenScaleScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var sensory by remember { mutableIntStateOf(4) }
    var moisture by remember { mutableIntStateOf(4) }
    var activity by remember { mutableIntStateOf(4) }
    var mobility by remember { mutableIntStateOf(4) }
    var nutrition by remember { mutableIntStateOf(4) }
    var friction by remember { mutableIntStateOf(3) }

    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }

    val totalScore = sensory + moisture + activity + mobility + nutrition + friction

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
                                text = stringResource(id = R.string.braden_scale_title),
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
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) Color.White else Color(0xFF1A237E))
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
                            text = if (isBn) "🌐 প্রেশার সোর (Pressure Ulcer) ঝুঁকি নিরূপণ" else "🌐 Pressure Ulcer Risk Assessment",
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
                    BradenResultCard(totalScore, isBn)

                    // Recommendation Section
                    BradenRecommendationCard(totalScore, isBn)

                    // 1. Sensory Perception
                    BradenSelectionSection(
                        title = if (isBn) "১. অনুভূতি ও সাড়া দেওয়া (Sensory Perception)" else "1. Sensory Perception",
                        options = if (isBn) listOf(
                            "সম্পূর্ণ সীমিত (চেতনাহীন / সাড়াহীন)" to 1,
                            "খুবই সীমিত (শুধু ব্যথায় সামান্য সাড়া দেয়)" to 2,
                            "সামান্য সীমিত (কথায় সাড়া দেয়)" to 3,
                            "কোনো সমস্যা নেই (স্বাভাবিক অনুভূত)" to 4
                        ) else listOf(
                            "Completely Limited" to 1,
                            "Very Limited" to 2,
                            "Slightly Limited" to 3,
                            "No Impairment" to 4
                        ),
                        selectedScore = sensory
                    ) { sensory = it }

                    // 2. Moisture
                    BradenSelectionSection(
                        title = if (isBn) "২. ত্বকের আর্দ্রতা ও ভিজা ভাব (Moisture)" else "2. Moisture",
                        options = if (isBn) listOf(
                            "সর্বদা ভেজা থাকে (ঘাম/প্রস্রাব)" to 1,
                            "প্রায়ই ভেজা থাকে" to 2,
                            "মাঝে মাঝে ভেজা থাকে" to 3,
                            "কদাচিৎ ভেজা (সাধারণত শুকনো)" to 4
                        ) else listOf(
                            "Constantly Moist" to 1,
                            "Often Moist" to 2,
                            "Occasionally Moist" to 3,
                            "Rarely Moist" to 4
                        ),
                        selectedScore = moisture
                    ) { moisture = it }

                    // 3. Activity
                    BradenSelectionSection(
                        title = if (isBn) "৩. শারীরিক এক্টিভিটি (Activity)" else "3. Activity",
                        options = if (isBn) listOf(
                            "সম্পূর্ণ শয্যাশায়ী (বিছানায় সীমাবদ্ধ)" to 1,
                            "চেয়ারে সীমাবদ্ধ (হাঁটতে পারে না)" to 2,
                            "মাঝে মাঝে হাঁটে" to 3,
                            "নিয়মিত হাঁটাচলা করে" to 4
                        ) else listOf(
                            "Bedfast" to 1,
                            "Chairfast" to 2,
                            "Walks Occasionally" to 3,
                            "Walks Frequently" to 4
                        ),
                        selectedScore = activity
                    ) { activity = it }

                    // 4. Mobility
                    BradenSelectionSection(
                        title = if (isBn) "৪. অবস্থান পরিবর্তনের ক্ষমতা (Mobility)" else "4. Mobility",
                        options = if (isBn) listOf(
                            "সম্পূর্ণ নড়াচড়ায় অক্ষম" to 1,
                            "খুবই সীমিত নড়াচড়া" to 2,
                            "সামান্য সীমিত" to 3,
                            "কোনো সীমাবদ্ধতা নেই (স্বাভাবিক)" to 4
                        ) else listOf(
                            "Completely Immobile" to 1,
                            "Very Limited" to 2,
                            "Slightly Limited" to 3,
                            "No Limitation" to 4
                        ),
                        selectedScore = mobility
                    ) { mobility = it }

                    // 5. Nutrition
                    BradenSelectionSection(
                        title = if (isBn) "৫. খাদ্য গ্রহণ ও পুষ্টি (Nutrition)" else "5. Nutrition",
                        options = if (isBn) listOf(
                            "খুবই অপ্রতুল (খাবার কম খায়)" to 1,
                            "সম্ভবত অপর্যাপ্ত" to 2,
                            "পর্যাপ্ত খাবার খায়" to 3,
                            "চমৎকার পুষ্টিকর খাদ্যগ্রহণ" to 4
                        ) else listOf(
                            "Very Poor" to 1,
                            "Probably Inadequate" to 2,
                            "Adequate" to 3,
                            "Excellent" to 4
                        ),
                        selectedScore = nutrition
                    ) { nutrition = it }

                    // 6. Friction and Shear
                    BradenSelectionSection(
                        title = if (isBn) "৬. ত্বক ঘর্ষণ ও চাপ (Friction and Shear)" else "6. Friction and Shear",
                        options = if (isBn) listOf(
                            "সমস্যা রয়েছে (বিছানায় ঘষা খায়)" to 1,
                            "সম্ভাব্য সমস্যা রয়েছে" to 2,
                            "কোনো দৃশ্যমান সমস্যা নেই" to 3
                        ) else listOf(
                            "Problem" to 1,
                            "Potential Problem" to 2,
                            "No Apparent Problem" to 3
                        ),
                        selectedScore = friction
                    ) { friction = it }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun BradenResultCard(score: Int, isBn: Boolean) {
    val isDark = isSystemInDarkTheme()
    val (status, color) = when {
        score >= 19 -> (if (isBn) "ঝুঁকি নেই (স্বাভাবিক)" else "No Risk (Safe)") to Color(0xFF22C55E)
        score >= 15 -> (if (isBn) "মৃদু ঝুঁকি (Mild Risk)" else "Mild Risk") to Color(0xFF0284C7)
        score >= 13 -> (if (isBn) "মাঝারি ঝুঁকি (Moderate Risk)" else "Moderate Risk") to Color(0xFFFF9800)
        score >= 10 -> (if (isBn) "উচ্চ ঝুঁকি (High Risk)" else "High Risk") to Color(0xFFFF5252)
        else -> (if (isBn) "অত্যধিক গুরুতর ঝুঁকি (Severe Risk)" else "Severe / Very High Risk") to Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else color.copy(alpha = 0.12f)),
        border = BorderStroke(1.5.dp, if (isDark) color else color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(id = R.string.braden_total_score), fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$score / 23", fontSize = 48.sp, fontWeight = FontWeight.Black, color = color)
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
        }
    }
}

@Composable
fun BradenRecommendationCard(score: Int, isBn: Boolean) {
    val isDark = isSystemInDarkTheme()
    val advice = when {
        score >= 19 -> if (isBn)
            "নিয়মিত ত্বকের যত্ন, সঠিক পুষ্টি এবং প্রোটোকল অনুযায়ী অবস্থান পরিবর্তন বজায় রাখুন।"
        else
            "Continue routine skin care, hydration, and repositioning as per protocol."
        score >= 15 -> if (isBn)
            "প্রতি ২ ঘণ্টা পর পর রোগীকে পাশ ফেরান। প্রেশার-রিডিউসিং ম্যাট্রেস ব্যবহার করুন এবং ত্বক শুকনো ও পরিষ্কার রাখুন।"
        else
            "Implement a turning schedule every 2 hours. Use pressure-redistribution mattress surface. Keep skin dry and clean."
        score >= 13 -> if (isBn)
            "রোগীকে পাশ ফেরানোর ফ্রিকোয়েন্সি বাড়ান। ৩০° ল্যাটেরাল পজিশনে ফোম ওয়েজ ব্যবহার করুন এবং হাই-প্রোটিন ডায়েট নিশ্চিত করুন।"
        else
            "Increase turning frequency (q2h). Use foam wedges for 30° lateral positioning. Optimize high-protein nutrition."
        else -> if (isBn)
            "জরুরি প্রেশার-রিলিফ প্রোটোকল চালু করুন। অল্টারনেটিং-প্রেশার এয়ার ম্যাট্রেস ব্যবহার করুন। হাই প্রোটিন ডায়েট ও ক্ষত প্রতিরোধ স্পেশালিস্ট পরামর্শ নিন।"
        else
            "Strict pressure-relief protocol. Specialized alternating-pressure air mattress required. High protein & vitamin intake. Immediate dermatology / wound care consult."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFF0284C7).copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF0284C7).copy(alpha = 0.25f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, contentDescription = null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(stringResource(id = R.string.nursing_intervention), fontWeight = FontWeight.Black, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7), fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(advice, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1E293B), lineHeight = 19.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun BradenSelectionSection(
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
                val labelColor = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1E293B))
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
                                text = "$score Pt",
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
