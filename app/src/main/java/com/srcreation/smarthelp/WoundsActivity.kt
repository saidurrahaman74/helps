package com.srcreation.smarthelp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.util.Locale

class WoundsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                WoundsScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WoundsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val isBn = remember { Locale.getDefault().language == "bn" }

    Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Healing, null, tint = Color(0xFFEF4444), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "ক্ষত ও কাটা-ছেঁড়া গাইড" else "Wounds & Cuts Guide", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = if (isDark) Color.White else Color(0xFF1A237E))
                        }
                    },
                    actions = { LanguageToggleButton() },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
            bottomBar = { AppBottomBar(context = context, currentTab = -1, backgroundBrush = AppGradientBrush) }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overview Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.85f)),
                        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = if (isBn) "ক্ষত ও কাটা-ছেঁড়া প্রাথমিক চিকিৎসা" else "First Aid for Wounds & Cuts",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "ত্বক বা শরীরের টিস্যু ক্ষতিগ্রস্ত হওয়াকে ক্ষত (Wound) বলে। সঠিক সময়ে সঠিকভাবে পরিষ্কার ও পরিচর্যা না করলে ইনফেকশন বা গভীর দাগ হতে পারে।"
                                else 
                                    "A wound is damage to the skin or soft tissue. Proper cleaning and dressing within the golden window prevent infection and scarring.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Step-by-Step Protocol
                item {
                    WoundSectionCard(
                        title = if (isBn) "৪-ধাপের প্রাথমিক চিকিৎসা (4-Step Care)" else "4-Step Wound Care",
                        icon = Icons.Default.FactCheck,
                        color = Color(0xFF10B981)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            WoundStepItem("1", if (isBn) "হাতের পরিচ্ছন্নতা ও নিরাপত্তা" else "Hand Hygiene & Safety", if (isBn) "গ্লাভস পরুন বা হাত ভালো করে সাবান দিয়ে ধুয়ে নিন।" else "Wear gloves or wash hands thoroughly with soap.")
                            WoundStepItem("2", if (isBn) "রক্তপাত নিয়ন্ত্রণ" else "Stop Bleeding", if (isBn) "পরিষ্কার গজ বা কাপড় দিয়ে ১০-১৫ মিনিট সরাসরি চাপ দিয়ে ধরে রাখুন।" else "Apply direct firm pressure with clean gauze for 10-15 mins.")
                            WoundStepItem("3", if (isBn) "ক্ষত পরিষ্কার করা" else "Clean the Wound", if (isBn) "প্রবহমান পরিষ্কার পানি বা স্যালাইন দিয়ে ধুয়ে ফেলুন। ক্ষতের ভেতরে তুলা ব্যবহার করবেন না।" else "Irrigate with clean running water or saline. Do not use cotton directly inside.")
                            WoundStepItem("4", if (isBn) "ড্রেসিং ও সুরক্ষা" else "Dress & Protect", if (isBn) "জীবাণুমুক্ত ব্যান্ডেজ বা গজ দিয়ে ঢেকে রাখুন এবং নিয়মিত পরিবর্তন করুন।" else "Cover with sterile bandage or dressing and change regularly.")
                        }
                    }
                }

                // Wound Types Table
                item {
                    WoundSectionCard(
                        title = if (isBn) "ক্ষতের প্রধান ধরনসমূহ (Types of Wounds)" else "Types of Wounds",
                        icon = Icons.Default.Category,
                        color = Color(0xFF0284C7)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            WoundTypeRow(if (isBn) "ঘষা লাগা (Abrasion)" else "Abrasion", if (isBn) "ত্বকের উপরিভাগ ঘষে উঠে যাওয়া (যেমন: রাস্তায় পড়ে যাওয়া)" else "Top layer of skin scraped off")
                            WoundTypeRow(if (isBn) "কাটা (Incision / Cut)" else "Incision / Cut", if (isBn) "ধারালো বস্তু যেমন ছুরি বা কাঁচ দিয়ে পরিষ্কার কাটা" else "Clean cut by sharp object like knife or glass")
                            WoundTypeRow(if (isBn) "ছিঁড়ে যাওয়া (Laceration)" else "Laceration", if (isBn) "অমসৃণভাবে ত্বক ও মাংস ফেটে যাওয়া বা ছিঁড়ে যাওয়া" else "Jagged or torn tissue injury")
                            WoundTypeRow(if (isBn) "ফুঁড়ে যাওয়া (Puncture)" else "Puncture", if (isBn) "পেরেক, সুচ বা কাঁটা ঢুকে গভীর ও সরু ক্ষত হওয়া" else "Deep narrow wound from nail or needle")
                        }
                    }
                }

                // DOs and DON'Ts
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                            border = BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("✅ DOs", fontWeight = FontWeight.Bold, color = Color(0xFF047857), fontSize = 14.sp)
                                Spacer(Modifier.height(6.dp))
                                Text(if (isBn) "• পরিষ্কার পানি ব্যবহার\n• চাপ দিয়ে রক্ত বন্ধ করা\n• জীবাণুমুক্ত রাখা\n• টিটেনাস টিকা নেওয়া" else "• Use clean water\n• Direct pressure\n• Keep sterile\n• Check tetanus status", fontSize = 12.sp, color = Color(0xFF065F46), lineHeight = 18.sp)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("❌ DON'Ts", fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C), fontSize = 14.sp)
                                Spacer(Modifier.height(6.dp))
                                Text(if (isBn) "• হলুদ বা মাটি লাগানো\n• তুলা ক্ষতে দেওয়া\n• বস্তু নিজে টানা\n• অতিরিক্ত শক্ত বাঁধা" else "• No soil/turmeric\n• No loose cotton\n• Don't pull objects\n• Don't tie too tight", fontSize = 12.sp, color = Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: বিশ্ব স্বাস্থ্য সংস্থা (WHO) ও আন্তর্জাতিক রেড ক্রস ফার্স্ট এইড গাইডলাইন।" else "Reference: World Health Organization (WHO) & International Red Cross First Aid Guidelines.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun WoundSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = if (isDark) Color.White else Color(0xFF1A237E))
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun WoundStepItem(num: String, title: String, desc: String) {
    val isDark = isSystemInDarkTheme()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(32.dp).background(Color(0xFF1A237E), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(num, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
            Spacer(Modifier.height(2.dp))
            Text(desc, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray, lineHeight = 18.sp)
        }
    }
}

@Composable
fun WoundTypeRow(type: String, desc: String) {
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier.fillMaxWidth().background(if (isDark) Color.Black.copy(alpha = 0.2f) else Color(0xFFF8FAFC), RoundedCornerShape(10.dp)).padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(type, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
            Text(desc, fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
        }
    }
}
