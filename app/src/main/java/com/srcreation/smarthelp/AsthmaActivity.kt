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

class AsthmaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                AsthmaScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsthmaScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.Air, null, tint = Color(0xFF0284C7), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "হাঁপানি আক্রমণ (Asthma) গাইড" else "Asthma Attack Guide", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                                text = if (isBn) "তীব্র হাঁপানি আক্রমণ (Severe Asthma Attack)" else "Severe Asthma Attack First Aid",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "হাঁপানি আক্রমণে শ্বাসনালি সংকুচিত হয়ে যায় এবং শ্বাস নিতে তীব্র কষ্ট হয়। জিআইএনা (GINA) আন্তর্জাতিক গাইডলাইন অনুযায়ী দ্রুত ইনহেলার ব্যবহার ও সঠিক পজিশন জীবন রক্ষা করতে পারে।"
                                else 
                                    "An asthma attack causes airways to narrow, making breathing extremely difficult. GINA international guidelines emphasize calm positioning and rescue inhaler use.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White else Color.DarkGray,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // GINA Step-by-Step Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0C4A6E).copy(alpha = 0.6f) else Color(0xFFE0F2FE)),
                        border = BorderStroke(1.5.dp, Color(0xFF0284C7))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = if (isBn) "জিআইএনা (GINA) ৪-ধাপের প্রথম চিকিৎসা" else "GINA 4-Step First Aid",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = if (isDark) Color(0xFF7DD3FC) else Color(0xFF0369A1)
                            )
                            Spacer(Modifier.height(10.dp))
                            AsthmaStepItem("1", if (isBn) "সোজা করে বসান" else "Sit Upright", if (isBn) "রোগীকে সোজা করে বসিয়ে দিন, শুইয়ে দেবেন না। কিছুটা সামনে ঝুঁকতে বলুন।" else "Sit the person upright. Do not lay them flat. Lean slightly forward.")
                            Spacer(Modifier.height(8.dp))
                            AsthmaStepItem("2", if (isBn) "শান্ত রাখুন ও বাতাস দিন" else "Calm & Fresh Air", if (isBn) "রোগীকে শান্ত রাখুন এবং আশপাশ থেকে ভিড় সরিয়ে পর্যাপ্ত ফ্রেশ বাতাসের ব্যবস্থা করুন।" else "Keep them calm and remove surrounding crowd for fresh air.")
                            Spacer(Modifier.height(8.dp))
                            AsthmaStepItem("3", if (isBn) "রিসকিউ ইনহেলার দিন" else "Rescue Inhaler", if (isBn) "দ্রুত রিলিভার ইনহেলার (যেমন Salbutamol) ভালোভাবে ঝাকিয়ে স্পেসার সহ বা সরাসরি ৪ বার পাফ দিন।" else "Shake rescue inhaler (Albuterol/Salbutamol) and give 4 puffs.")
                            Spacer(Modifier.height(8.dp))
                            AsthmaStepItem("4", if (isBn) "৪ মিনিট অপেক্ষা ও পুনরাবৃত্তি" else "Wait & Repeat", if (isBn) "৪ মিনিট অপেক্ষা করুন। অবস্থার উন্নতি না হলে আরও ৪ বার পাফ দিন এবং হাসপাতালে নিন।" else "Wait 4 minutes. If no improvement, give another 4 puffs and seek help.")
                        }
                    }
                }

                // DOs and DON'Ts
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF065F46).copy(alpha = 0.5f) else Color(0xFFECFDF5)),
                            border = BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("✅ DOs", fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857), fontSize = 14.sp)
                                Spacer(Modifier.height(6.dp))
                                Text(if (isBn) "• সোজা করে বসানো\n• ইনহেলার ব্যবহার\n• শান্ত রাখা\n• ফ্রেশ বাতাস" else "• Sit upright\n• Use rescue inhaler\n• Keep calm\n• Fresh air", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF065F46), lineHeight = 18.sp)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.5f) else Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("❌ DON'Ts", fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C), fontSize = 14.sp)
                                Spacer(Modifier.height(6.dp))
                                Text(if (isBn) "• শুইয়ে দেওয়া\n• ভিড় জমানো\n• আতঙ্কিত হওয়া\n• ইনহেলার দিতে দেরি করা" else "• Don't lay flat\n• Don't crowd\n• Don't panic\n• Don't delay inhaler", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                // When to call emergency
                item {
                    AsthmaSectionCard(
                        title = if (isBn) "কখন জরুরি সাহায্য নেবেন?" else "When to Call Emergency",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFEF4444)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            AsthmaBullet(if (isBn) "ইনহেলার ব্যবহারের পরও শ্বাসকষ্ট না কমলে।" else "Breathing does not improve after rescue inhaler puffs.")
                            AsthmaBullet(if (isBn) "রোগী সম্পূর্ণ বাক্য বলতে না পারলে (মাঝপথে থেমে গেলে)।" else "Person cannot speak in full sentences.")
                            AsthmaBullet(if (isBn) "ঠোঁট বা আঙুলের নখ নীল হয়ে গেলে (Cyanosis)।" else "Lips or fingernails turn blue (Cyanosis).")
                            AsthmaBullet(if (isBn) "শ্বাস নিতে বুক ভেতরে ঢুকে গেলে বা অতিরিক্ত দুর্বল হয়ে পড়লে।" else "Chest drawing in with breaths or extreme exhaustion.")
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: গ্লোবাল ইনিশিয়েটিভ ফর অ্যাজমা (GINA) স্ট্যান্ডার্ড।" else "Reference: Global Initiative for Asthma (GINA) Standards.",
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
fun AsthmaSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun AsthmaStepItem(num: String, title: String, desc: String) {
    val isDark = isSystemInDarkTheme()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(28.dp).background(Color(0xFF0284C7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(num, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color(0xFF7DD3FC) else Color(0xFF0369A1))
            Spacer(Modifier.height(2.dp))
            Text(desc, fontSize = 13.sp, color = if (isDark) Color.White else Color.DarkGray, lineHeight = 18.sp)
        }
    }
}

@Composable
fun AsthmaBullet(text: String) {
    val isDark = isSystemInDarkTheme()
    Row(verticalAlignment = Alignment.Top) {
        Text("•", fontWeight = FontWeight.Black, color = Color(0xFFEF4444), modifier = Modifier.padding(end = 8.dp))
        Text(text, fontSize = 13.sp, color = if (isDark) Color.White else Color.DarkGray, lineHeight = 18.sp)
    }
}
