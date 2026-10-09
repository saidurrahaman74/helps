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

class ChokingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                ChokingScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChokingScreen(onBack: () -> Unit) {
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
                            Text(if (isBn) "শ্বাসরোধ ও হেইমলিক গাইড" else "Choking & Heimlich Guide", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                                text = if (isBn) "হেইমলিক ম্যানুভার (Heimlich Maneuver)" else "Heimlich Maneuver for Choking",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "গলায় খাবার বা কোনো বস্তু আটকে শ্বাসনালি বন্ধ হয়ে গেলে (Choking) দ্রুত হেইমলিক ম্যানুভার বা অ্যাবডোমিনাল থ্রাস্ট দিতে হয়।"
                                else 
                                    "When food or a foreign object blocks the airway (choking), immediate abdominal thrusts (Heimlich Maneuver) can save a life.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Severity Check
                item {
                    ChokingSectionCard(
                        title = if (isBn) "গুরুতর চোখিং কীভাবে চিনবেন?" else "Recognizing Severe Choking",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFEF4444)
                    ) {
                        Text(
                            text = if (isBn) 
                                "• কথা বলতে, কাশতে বা শ্বাস নিতে না পারা\n• উভয় হাত দিয়ে গলা চেপে ধরা (Universal sign of choking)\n• মুখ ও ত্বক নীলচে বা ফ্যাকাশে হয়ে যাওয়া\n\n* দ্রষ্টব্য: ব্যক্তি জোরে কাশতে পারলে তাকে কাশতে দিন, পেটে চাপ দেবেন না।"
                            else 
                                "• Inability to speak, cough, or breathe\n• Clutching the throat with both hands (Universal sign)\n• Skin turning bluish or pale\n\n* Note: If person can cough forcefully, encourage coughing; do not perform thrusts yet.",
                            fontSize = 14.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray,
                            lineHeight = 22.sp
                        )
                    }
                }

                // Adult & Child > 1 yr protocol
                item {
                    ChokingSectionCard(
                        title = if (isBn) "প্রাপ্তবয়স্ক ও শিশুর ক্ষেত্রে (৫+৫ পদ্ধতি)" else "Adult & Child Protocol (5+5 Method)",
                        icon = Icons.Default.ChildCare,
                        color = Color(0xFF0284C7)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ChokingStepItem("1", if (isBn) "৫টি ব্যাক ব্লো (5 Back Blows)" else "5 Back Blows", if (isBn) "ব্যক্তিকে সামান্য সামনে ঝুঁকিয়ে কাঁধের দুই হাড়ের মাঝে হাতের তালু দিয়ে ৫টি দৃঢ় আঘাত করুন।" else "Bend person slightly forward and deliver 5 sharp back blows between shoulder blades.")
                            ChokingStepItem("2", if (isBn) "৫টি অ্যাবডোমিনাল থ্রাস্ট" else "5 Abdominal Thrusts", if (isBn) "পেছনে দাঁড়িয়ে নাভির ঠিক ওপরে মুষ্টিবদ্ধ হাত রেখে ভেতরের দিকে ও ওপরের দিকে ৫টি দ্রুত চাপ দিন।" else "Stand behind, place clenched fist just above navel, and pull inward and upward 5 times.")
                            ChokingStepItem("3", if (isBn) "পুনরাবৃত্তি করুন" else "Repeat", if (isBn) "বস্তু বের না হওয়া পর্যন্ত ৫টি ব্যাক ব্লো ও ৫টি থ্রাস্ট পালা করে চালিয়ে যান।" else "Alternate between 5 back blows and 5 thrusts until object is expelled or help arrives.")
                        }
                    }
                }

                // Infant < 1 yr protocol
                item {
                    ChokingSectionCard(
                        title = if (isBn) "এক বছরের কম শিশুর ক্ষেত্রে" else "Infant Protocol (< 1 Year)",
                        icon = Icons.Default.ChildCare,
                        color = Color(0xFF10B981)
                    ) {
                        Text(
                            text = if (isBn) 
                                "• হেইমলিক ম্যানুভার করবেন না।\n• শিশুকে উপুড় করে আপনার বাহুর ওপর রাখুন (মাথা শরীরের চেয়ে নিচে)।\n• পিঠের মাঝখানে ৫টি ব্যাক স্ল্যাপ (Back Slaps) দিন।\n• এরপর চিৎ করে ঘুরিয়ে দুই আঙুল দিয়ে বুকের মাঝখানে ৫টি চেস্ট থ্রাস্ট দিন।"
                            else 
                                "• Do not perform abdominal thrusts on infants.\n• Hold infant face down along your forearm (head lower than chest).\n• Give 5 gentle back slaps between shoulder blades.\n• Turn face up and give 5 chest thrusts with two fingers.",
                            fontSize = 14.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray,
                            lineHeight = 22.sp
                        )
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
                                Text(if (isBn) "• ব্যাক ব্লো ও থ্রাস্ট দিন\n• কাশতে উৎসাহ দিন\n• সচেতন থাকুন\n• দ্রুত সাহায্য নিন" else "• Back blows & thrusts\n• Encourage cough\n• Stay calm\n• Get help fast", fontSize = 12.sp, color = Color(0xFF065F46), lineHeight = 18.sp)
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
                                Text(if (isBn) "• আঙুল দিয়ে খোঁচাবেন না\n• পানি বা তরল দেবেন না\n• শিশুকে পেটে চাপ দেবেন না\n• আতঙ্কিত হবেন না" else "• Don't blind finger sweep\n• No water/liquids\n• No infant abdominal thrusts\n• Don't panic", fontSize = 12.sp, color = Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: আমেরিকান রেড ক্রস ও আন্তর্জাতিক রিসাসিটেশন স্ট্যান্ডার্ড।" else "Reference: American Red Cross & International Resuscitation Standards.",
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
fun ChokingSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun ChokingStepItem(num: String, title: String, desc: String) {
    val isDark = isSystemInDarkTheme()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(32.dp).background(Color(0xFF0284C7), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(num, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7))
            Spacer(Modifier.height(2.dp))
            Text(desc, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray, lineHeight = 18.sp)
        }
    }
}
