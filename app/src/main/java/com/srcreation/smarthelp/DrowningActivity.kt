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

class DrowningActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                DrowningScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrowningScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.Water, null, tint = Color(0xFF0284C7), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "পানিতে ডোবা ও উদ্ধার গাইড" else "Drowning & Water Rescue Guide", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                                text = if (isBn) "পানিতে ডুবে যাওয়ার প্রাথমিক চিকিৎসা" else "Drowning First Aid & Rescue",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "পানিতে ডোবা একটি জরুরি জীবনঘাতী অবস্থা যেখানে ফুসফুসে পানি ঢুকে অক্সিজেন সরবরাহ বন্ধ হয়ে যায়। আন্তর্জাতিক প্রোটোকল অনুযায়ী দ্রুত উদ্ধার, কৃত্রিম শ্বাস এবং সিপিআর জীবন বাঁচাতে পারে।"
                                else 
                                    "Drowning is a life-threatening emergency where water enters the lungs, cutting off oxygen. International protocol emphasizes fast rescue and immediate CPR.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White else Color.DarkGray,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Water Rescue Principles Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0C4A6E).copy(alpha = 0.6f) else Color(0xFFE0F2FE)),
                        border = BorderStroke(1.5.dp, Color(0xFF0284C7))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = if (isBn) "নিরাপদ উদ্ধারের নিয়ম: REACH – THROW – ROW – GO" else "Safe Rescue Principles: REACH – THROW – ROW – GO",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = if (isDark) Color(0xFF7DD3FC) else Color(0xFF0369A1)
                            )
                            Spacer(Modifier.height(10.dp))
                            DrowningStepItem("1", if (isBn) "হাত বাড়িয়ে দেওয়া (REACH)" else "REACH", if (isBn) "পড়ন্ত বা সাঁতার না জানা ব্যক্তিকে হাত, লাঠি বা তোয়ালে বাড়িয়ে দিয়ে টানুন।" else "Extend a hand, pole, or towel to pull the person in.")
                            Spacer(Modifier.height(8.dp))
                            DrowningStepItem("2", if (isBn) "বস্তু ছুড়ে দেওয়া (THROW)" else "THROW", if (isBn) "লাইফবয়, দড়ি বা ভাসমান কোনো বস্তু তার দিকে ছুড়ে দিন।" else "Throw a life ring, rope, or floating object toward them.")
                            Spacer(Modifier.height(8.dp))
                            DrowningStepItem("3", if (isBn) "নৌকা ব্যবহার (ROW)" else "ROW", if (isBn) "নৌকা বা ভাসানযান দিয়ে কাছে যান।" else "Use a boat or watercraft if available to reach them safely.")
                            Spacer(Modifier.height(8.dp))
                            DrowningStepItem("4", if (isBn) "সাঁতার কেটে যাওয়া (GO)" else "GO", if (isBn) "সাঁতার জানা না থাকলে নিজে পানিতে নামবেন না; এটি শেষ বিকল্প।" else "Enter water yourself only if trained; this is the last resort.")
                        }
                    }
                }

                // Immediate First Aid after rescue
                item {
                    DrowningSectionCard(
                        title = if (isBn) "উদ্ধারের পর তাৎক্ষণিক করণীয়" else "Immediate Post-Rescue Care",
                        icon = Icons.Default.MedicalServices,
                        color = Color(0xFF10B981)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            DrowningBullet(if (isBn) "রোগীকে সমতল ও শক্ত জায়গায় শুইয়ে দিন।" else "Place the person flat on a firm, flat surface.")
                            DrowningBullet(if (isBn) "শ্বাস-প্রশ্বাস ও নাড়ি পরীক্ষা করুন।" else "Check breathing and pulse immediately.")
                            DrowningBullet(if (isBn) "শ্বাস না নিলে তাৎক্ষণিক CPR শুরু করুন (প্রথমে ২টি রেসকিউ ব্রিথ, এরপর ৩০টি বুক চাপ)।" else "If not breathing, start CPR immediately (2 rescue breaths followed by 30 compressions).")
                            DrowningBullet(if (isBn) "মুখ থেকে পানি বের করার জন্য পেটে চাপ দিয়ে বা ঝুলিয়ে সময় নষ্ট করবেন না।" else "Do not waste time trying to drain water by pressing the abdomen or hanging upside down.")
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
                                Text(if (isBn) "• দ্রুত উদ্ধার করুন\n• তাৎক্ষণিক CPR দিন\n• পাশ ফিরিয়ে রাখুন\n• হাসপাতালে নিন" else "• Rescue fast\n• Start CPR at once\n• Recovery position\n• Get hospital care", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF065F46), lineHeight = 18.sp)
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
                                Text(if (isBn) "• পানি বের করতে সময় নষ্ট\n• নিজের জীবন ঝুঁকি\n• পেট চেপে ধরা\n• অবহেলা করা" else "• Don't delay CPR\n• Don't risk own life\n• Don't press abdomen\n• Don't ignore symptoms", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: আন্তর্জাতিক রেড ক্রস ও বিশ্ব স্বাস্থ্য সংস্থা (WHO) জল নিরাপত্তা গাইডলাইন।" else "Reference: International Red Cross & World Health Organization (WHO) Water Safety Guidelines.",
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
fun DrowningSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun DrowningStepItem(num: String, title: String, desc: String) {
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
fun DrowningBullet(text: String) {
    val isDark = isSystemInDarkTheme()
    Row(verticalAlignment = Alignment.Top) {
        Text("•", fontWeight = FontWeight.Black, color = Color(0xFF0284C7), modifier = Modifier.padding(end = 8.dp))
        Text(text, fontSize = 13.sp, color = if (isDark) Color.White else Color.DarkGray, lineHeight = 18.sp)
    }
}
