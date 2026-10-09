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

class PoisoningActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                PoisoningScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoisoningScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.WarningAmber, null, tint = Color(0xFFEF4444), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "বিষক্রিয়া প্রাথমিক চিকিৎসা" else "Poisoning First Aid Guide", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                                text = if (isBn) "বিষক্রিয়া (Poisoning) জরুরি ব্যবস্থাপনা" else "Poisoning Emergency Management",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "বিষক্রিয়া একটি জীবন-সংশয়ী জরুরি অবস্থা। বিষ গিলে ফেলা, ত্বকে লাগা, চোখে যাওয়া বা বিষাক্ত ধোঁয়া শ্বাসের সাথে নেওয়ার ক্ষেত্রে দ্রুত সঠিক পদক্ষেপ নেওয়া অত্যন্ত জরুরি।"
                                else 
                                    "Poisoning is a medical emergency. Whether ingested, inhaled, or absorbed through skin/eyes, taking immediate correct action saves lives.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Step-by-Step Guidelines Card
                item {
                    PoisoningSectionCard(
                        title = if (isBn) "জরুরি পদক্ষেপসমূহ (Emergency Steps)" else "Emergency Steps",
                        icon = Icons.Default.MedicalServices,
                        color = Color(0xFFEF4444)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            PoisoningStepItem("1", if (isBn) "নিরাপত্তা নিশ্চিত করুন" else "Ensure Safety", if (isBn) "বিষের উৎস বা রাসায়নিক থেকে নিজেকে ও রোগীকে নিরাপদ দূরত্বে সরিয়ে নিন।" else "Remove yourself and victim from the source of poison.")
                            PoisoningStepItem("2", if (isBn) "বমি করাবেন না" else "Do Not Induce Vomiting", if (isBn) "চিকিৎসক বা বিষ নিয়ন্ত্রণ কেন্দ্রের নির্দেশ ছাড়া নিজে থেকে বমি করাবেন না (বিশেষ করে অ্যাসিড বা পেট্রোলিয়াম জাতীয় পদার্থে)।" else "Never induce vomiting unless instructed by medical professionals.")
                            PoisoningStepItem("3", if (isBn) "প্যাকেট বা নমুনা সংরক্ষণ করুন" else "Save the Container", if (isBn) "ওষুধের পাতা, বোতল বা রাসায়নিকের প্যাকেটটি সঙ্গে রাখুন, যা চিকিৎসকের জন্য সাহায্যকারী হবে।" else "Keep the medication bottle or container to show doctors.")
                            PoisoningStepItem("4", if (isBn) "চোখ ও ত্বক ধুয়ে ফেলুন" else "Flush Skin / Eyes", if (isBn) "ত্বক বা চোখে বিষ লাগলে প্রচুর পরিষ্কার পানি দিয়ে কমপক্ষে ১৫-২০ মিনিট ধুতে থাকুন।" else "Flush skin or eyes with plenty of clean water for 15-20 mins.")
                        }
                    }
                }

                // Routes of Poisoning
                item {
                    PoisoningSectionCard(
                        title = if (isBn) "বিষ প্রবেশের বিভিন্ন পথ (Routes of Poisoning)" else "Routes of Poisoning",
                        icon = Icons.Default.Category,
                        color = Color(0xFF0284C7)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PoisoningRouteRow(if (isBn) "গিলে ফেলা (Ingestion)" else "Ingestion", if (isBn) "মুখ দিয়ে বিষ প্রবেশ করলে মুখে পানি দিয়ে কুলি করান, বমি করাবেন না।" else "Rinse mouth with water, do not induce vomiting.")
                            PoisoningRouteRow(if (isBn) "শ্বাসের মাধ্যমে (Inhalation)" else "Inhalation", if (isBn) "বিষাক্ত ধোঁয়া বা গ্যাস হলে রোগীকে দ্রুত খোলা বাতাসে ও তাজা বাতাসে নিয়ে যান।" else "Move victim immediately to fresh air.")
                            PoisoningRouteRow(if (isBn) "ত্বক ও চোখ (Skin / Eyes)" else "Skin & Eyes", if (isBn) "দূষিত পোশাক দ্রুত খুলে ফেলুন এবং প্রচুর পানি দিয়ে ধুয়ে ফেলুন।" else "Remove contaminated clothing and flush thoroughly with water.")
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
                                Text(if (isBn) "• তাজা বাতাসে নেওয়া\n• পানি দিয়ে চোখ/ত্বক ধোয়া\n• নমুনা সঙ্গে রাখা\n• শান্ত রাখা" else "• Move to fresh air\n• Flush eyes/skin\n• Save packaging\n• Keep calm", fontSize = 12.sp, color = Color(0xFF065F46), lineHeight = 18.sp)
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
                                Text(if (isBn) "• বমি করানোর চেষ্টা\n• নিজে মুখে কিছু খাওয়ানো\n• ঘরোয়া টোটকা ব্যবহার\n• দেরি করা" else "• Don't induce vomiting\n• Don't give food/milk\n• No home remedies\n• Don't delay care", fontSize = 12.sp, color = Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: বিশ্ব স্বাস্থ্য সংস্থা (WHO) ও আন্তর্জাতিক পয়জন কন্ট্রোল স্ট্যান্ডার্ড।" else "Reference: World Health Organization (WHO) & International Poison Control Standards.",
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
fun PoisoningSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun PoisoningStepItem(num: String, title: String, desc: String) {
    val isDark = isSystemInDarkTheme()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(32.dp).background(Color(0xFFEF4444), CircleShape),
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
fun PoisoningRouteRow(type: String, desc: String) {
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
