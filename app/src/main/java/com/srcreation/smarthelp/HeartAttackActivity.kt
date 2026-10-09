package com.srcreation.smarthelp

import android.content.Intent
import android.net.Uri
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

class HeartAttackActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                HeartAttackScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartAttackScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.Favorite, null, tint = Color(0xFFEF4444), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "হার্ট অ্যাটাক গাইড (AHA)" else "Heart Attack Guide (AHA)", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                // Symptoms
                item {
                    HeartAttackSectionCard(
                        title = if (isBn) "হার্ট অ্যাটাকের লক্ষণসমূহ (Symptoms)" else "Heart Attack Symptoms",
                        icon = Icons.Default.MedicalServices,
                        color = Color(0xFFEF4444)
                    ) {
                        Text(
                            text = if (isBn)
                                "• বুকের মাঝখানে তীব্র চাপ, ভারীভাব বা জ্বালাপোড়া\n• ব্যথা বাম হাত, ঘাড়, চোয়াল, কাঁধ বা পিঠে ছড়িয়ে পড়া\n• ঠান্ডা ঘাম, মাথা ঘোরা, বমিভাব ও তীব্র শ্বাসকষ্ট\n• বিশেষ দ্রষ্টব্য: নারী, বৃদ্ধ ও ডায়াবেটিস রোগীদের ক্ষেত্রে বুকব্যথা ছাড়াও কেবল চরম ক্লান্তি বা শ্বাসকষ্ট হতে পারে।"
                            else
                                "• Intense chest pressure, squeezing, or fullness\n• Pain radiating to left arm, neck, jaw, or back\n• Cold sweat, dizziness, nausea, and shortness of breath\n• Note: Women, elderly, and diabetic patients may experience atypical symptoms like severe fatigue.",
                            fontSize = 14.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray,
                            lineHeight = 22.sp
                        )
                    }
                }

                // Immediate Action Steps
                item {
                    HeartAttackSectionCard(
                        title = if (isBn) "তাৎক্ষণিক করণীয় (Immediate Action)" else "Immediate Action Steps",
                        icon = Icons.Default.FlashOn,
                        color = Color(0xFF10B981)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            HeartAttackStepItem("1", if (isBn) "বিশ্রাম নিন ও বসুন" else "Rest & Sit Down", if (isBn) "রোগীকে শান্তভাবে বসিয়ে দিন (চিত করে শোয়ানোর চেয়ে আধা-বসা ভালো)। কোনো হাঁটাচলা বা সিঁড়ি ভাঙতে দেবেন না।" else "Help patient sit comfortably. Avoid walking or stairs.")
                            HeartAttackStepItem("2", if (isBn) "পোশাক ঢিলা করুন" else "Loosen Clothing", if (isBn) "শার্টের কলার, টাই বা বেল্ট ঢিলা করুন এবং বাতাস চলাচলের ব্যবস্থা করুন।" else "Loosen collar, tie, and ensure good ventilation.")
                            HeartAttackStepItem("3", if (isBn) "অ্যাসপিরিন (যদি নিরাপদ হয়)" else "Aspirin (If Safe)", if (isBn) "অ্যালার্জি বা রক্তপাতের সমস্যা না থাকলে, চিকিৎসকের পরামর্শ অনুযায়ী ১৬০-৩২৫ মিলিগ্রাম অ্যাসপিরিন চিবিয়ে খেতে দিন।" else "If no allergy or bleeding risk, chew 160-325mg aspirin as directed.")
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
                                Text(if (isBn) "• দ্রুত ৯৯৯ কল করুন\n• অ্যাম্বুলেন্সে নিন\n• আসপিরিন চিবান\n• শান্ত রাখুন" else "• Call 999 fast\n• Use ambulance\n• Chew aspirin\n• Keep calm", fontSize = 12.sp, color = Color(0xFF065F46), lineHeight = 18.sp)
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
                                Text(if (isBn) "• নিজে গাড়ি চালাবেন না\n• হাঁটাহাঁটি করবেন না\n• অপেক্ষা করবেন না\n• খাবার বা পানি দেবেন না" else "• Don't drive self\n• Don't walk around\n• Don't wait & watch\n• Don't give food/water", fontSize = 12.sp, color = Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: আমেরিকান কলেজ অফ কার্ডিওলজি (ACC) ও আমেরিকান হার্ট অ্যাসোসিয়েশন (AHA) গাইডলাইন।" else "Reference: American College of Cardiology (ACC) & American Heart Association (AHA) Guidelines.",
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
fun HeartAttackSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun HeartAttackStepItem(number: String, title: String, desc: String) {
    val isDark = isSystemInDarkTheme()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(32.dp).background(Color(0xFF1A237E), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
            Spacer(Modifier.height(2.dp))
            Text(desc, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray, lineHeight = 18.sp)
        }
    }
}
