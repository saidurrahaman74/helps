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

class HeatStrokeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                HeatStrokeScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatStrokeScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.WbSunny, null, tint = Color(0xFFF97316), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "হিট স্ট্রোক গাইড" else "Heat Stroke Guide", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                                text = if (isBn) "হিট স্ট্রোক (Heat Stroke) পরিচিতি" else "What is Heat Stroke?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "হিট স্ট্রোক একটি অত্যন্ত বিপজ্জনক ও জীবন-সংশয়ী জরুরি অবস্থা। অতিরিক্ত গরম ও আর্দ্রতার কারণে শরীরের তাপমাত্রা নিয়ন্ত্রণের ক্ষমতা নষ্ট হয়ে যায় এবং শরীরের তাপমাত্রা দ্রুত ১০৪° ফারেনহাইট (৪০° সেলসিয়াস) বা তার বেশি হতে পারে।"
                                else 
                                    "Heat stroke is a life-threatening medical emergency caused by prolonged exposure to high temperatures. The body's temperature regulation fails, pushing core temperature to 104°F (40°C) or higher.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Symptoms
                item {
                    HeatStrokeSectionCard(
                        title = if (isBn) "হিট স্ট্রোকের প্রধান লক্ষণসমূহ (Symptoms)" else "Key Symptoms",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFEF4444)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HeatStrokeBullet(if (isBn) "উচ্চ শরীরের তাপমাত্রা (১০৪° ফা বা তার বেশি)" else "High body temperature (104°F or higher)")
                            HeatStrokeBullet(if (isBn) "মানসিক বিভ্রান্তি, প্রলাপ বকা বা অসংলগ্ন কথাবার্তা" else "Confusion, agitation, or slurred speech")
                            HeatStrokeBullet(if (isBn) "অজ্ঞান হয়ে যাওয়া বা খিঁচুনি হওয়া" else "Loss of consciousness or seizures")
                            HeatStrokeBullet(if (isBn) "ত্বক খুব গরম ও শুষ্ক হওয়া (কখনও অতিরিক্ত ঘাম থাকতে পারে)" else "Hot, dry skin or profuse sweating")
                            HeatStrokeBullet(if (isBn) "তীব্র মাথাব্যথা, মাথা ঘোরা ও বমিভাব" else "Severe headache, dizziness, and nausea")
                        }
                    }
                }

                // Rapid Cooling Protocols
                item {
                    HeatStrokeSectionCard(
                        title = if (isBn) "তাৎক্ষণিক প্রাথমিক চিকিৎসা ও কুলিং" else "Rapid Cooling Protocol",
                        icon = Icons.Default.AcUnit,
                        color = Color(0xFF0284C7)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            HeatStrokeStepItem("1", if (isBn) "ছায়ায় নিন" else "Move to Shade", if (isBn) "রোগীকে দ্রুত কোনো ঠান্ডা, ছায়াযুক্ত স্থান বা এসি রুমে সরিয়ে নিন।" else "Promptly move person to a cool, shaded place or AC room.")
                            HeatStrokeStepItem("2", if (isBn) "পোশাক ঢিলা করুন" else "Loosen Clothing", if (isBn) "অতিরিক্ত ও আঁটসাঁট পোশাক খুলে ফেলুন বা ঢিলা করে দিন।" else "Remove or loosen heavy outer clothing.")
                            HeatStrokeStepItem("3", if (isBn) "দ্রুত ঠান্ডা করুন" else "Rapid Cooling", if (isBn) "শরীরের ওপর ঠান্ডা পানি স্প্রে করুন, ভেজা তোয়ালে জড়িয়ে দিন এবং ফ্যানের বাতাস দিন। সম্ভব হলে বরফ পানি ব্যবহার করুন।" else "Spray cool water, apply wet towels, and fan. Use ice packs on neck, armpits, and groin.")
                            HeatStrokeStepItem("4", if (isBn) "মেডিকেল সাহায্য" else "Medical Care", if (isBn) "অবিলম্বে নিকটস্থ হাসপাতালে স্থানান্তরের ব্যবস্থা করুন।" else "Arrange for prompt transfer to the nearest hospital.")
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
                                Text(if (isBn) "• দ্রুত ঠান্ডা করুন\n• ছায়ায় রাখুন\n• ভেজা তোয়ালে ব্যবহার\n• সচেতন হলে পানি দিন" else "• Rapid cooling\n• Move to shade\n• Wet towels & fan\n• Water if conscious", fontSize = 12.sp, color = Color(0xFF065F46), lineHeight = 18.sp)
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
                                Text(if (isBn) "• অচেতনকে পানি দেবেন না\n• প্যারাসিটামল দেবেন না\n• একা ফেলে রাখবেন না\n• গরম কম্বল দেবেন না" else "• No water if unconscious\n• No paracetamol\n• Don't leave alone\n• No heavy blankets", fontSize = 12.sp, color = Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: বিশ্ব স্বাস্থ্য সংস্থা (WHO) ও সেন্টার ফর ডিজিজ কন্ট্রোল অ্যান্ড প্রিভেনশন (CDC) গাইডলাইন।" else "Reference: World Health Organization (WHO) & Centers for Disease Control and Prevention (CDC) Guidelines.",
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
fun HeatStrokeSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun HeatStrokeStepItem(num: String, title: String, desc: String) {
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
fun HeatStrokeBullet(text: String) {
    val isDark = isSystemInDarkTheme()
    Row(verticalAlignment = Alignment.Top) {
        Text("•", fontWeight = FontWeight.Black, color = Color(0xFFEF4444), modifier = Modifier.padding(end = 8.dp))
        Text(text, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray, lineHeight = 18.sp)
    }
}
