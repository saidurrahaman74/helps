package com.srcreation.smarthelp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

class CprGuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                CprGuideScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CprGuideScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.Emergency, null, tint = Color(0xFFEF4444), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "সিপিআর গাইড (AHA স্ট্যান্ডার্ড)" else "CPR Guide (AHA Standard)", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                // International Chain of Survival Header
                item {
                    Text(
                        text = if (isBn) "আন্তর্জাতিক চেইন অব সারভাইভাল (AHA 2025)" else "International Chain of Survival (AHA 2025)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else Color(0xFF1A237E)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SurvivalStepCard("1", if (isBn) "সাড়া পরীক্ষা" else "Assess", Modifier.weight(1f))
                        SurvivalStepCard("2", if (isBn) "জরুরি সাহায্য" else "Get Help", Modifier.weight(1f))
                        SurvivalStepCard("3", if (isBn) "বুক চাপ" else "Compress", Modifier.weight(1f))
                        SurvivalStepCard("4", if (isBn) "AED শক" else "AED Shock", Modifier.weight(1f))
                    }
                }

                // Step 1: When to start
                item {
                    InternationalSectionCard(
                        title = if (isBn) "১. কখন সি.পি.আর (CPR) শুরু করবেন?" else "1. When to Start CPR",
                        icon = Icons.Default.HelpOutline,
                        color = Color(0xFF0284C7)
                    ) {
                        Text(
                            text = if (isBn) 
                                "• ব্যক্তি সাড়া না দিলে (Unresponsive)\n• স্বাভাবিক শ্বাস না নিলে বা শুধু গ্যাস্পিং (অনিয়মিত হাঁপান) করলে\n• পালস খুঁজতে গিয়ে অপ্রয়োজনীয় সময় নষ্ট করবেন না (সর্বোচ্চ ১০ সেকেন্ড)।"
                            else 
                                "• Person is unresponsive / unconscious.\n• No normal breathing or only gasping.\n• Do not waste time checking pulse (max 10 seconds).",
                            fontSize = 14.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray,
                            lineHeight = 22.sp
                        )
                    }
                }

                // Step 2: Adult CPR Steps (CAB)
                item {
                    InternationalSectionCard(
                        title = if (isBn) "২. প্রাপ্তবয়স্কের CPR প্রোটোকল (CAB)" else "2. Adult CPR Protocol (CAB)",
                        icon = Icons.Default.FitnessCenter,
                        color = Color(0xFF10B981)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            CprStepItem("C", if (isBn) "কম্প্রেশন (Compressions)" else "Compressions", if (isBn) "বুকের মাঝখানে প্রতি মিনিটে ১০০-১২০ বার গভীর চাপ দিন (৫-৬ সেমি)।" else "Push hard and fast in center of chest (100-120/min, 5-6 cm depth).")
                            CprStepItem("A", if (isBn) "এয়ারওয়ে (Airway)" else "Airway", if (isBn) "মাথা পেছনের দিকে ও চিবুক ওপরে তুলে শ্বাসনালি খুলুন।" else "Tilt head back, lift chin to open airway.")
                            CprStepItem("B", if (isBn) "ব্রিথিং (Breathing)" else "Breathing", if (isBn) "প্রশিক্ষণ থাকলে ৩০টি চাপের পর ২টি রেসকিউ ব্রিথ দিন (Hands-Only CPR ও অত্যন্ত কার্যকর)।" else "Give 2 rescue breaths after 30 compressions if trained (Hands-only CPR is also highly effective).")
                        }
                    }
                }

                // Step 3: AED Usage
                item {
                    InternationalSectionCard(
                        title = if (isBn) "৩. এ.ই.ডি (AED) ব্যবহার নির্দেশিকা" else "3. AED Usage Guidelines",
                        icon = Icons.Default.FlashOn,
                        color = Color(0xFFF59E0B)
                    ) {
                        Text(
                            text = if (isBn)
                                "1. AED মেশিন চালু করুন এবং ভয়েস ইন্সট্রাকশন শুনুন।\n2. রোগীর বুকের পোশাক সরিয়ে প্যাডগুলো সঠিক স্থানে লাগান।\n3. মেশিন হার্ট রিদম বিশ্লেষণ করার সময় কেউ রোগীকে স্পর্শ করবেন না।\n4. শক দিতে বললে নিরাপদ দূরত্বে থেকে শক বাটন চাপুন।\n5. শক দেওয়ার পর உடனடியாக আবার ৫ চক্র (২ মিনিট) CPR শুরু করুন।"
                            else
                                "1. Turn on AED and follow voice prompts.\n2. Expose chest and apply adhesive pads.\n3. Clear the patient during rhythm analysis.\n4. Deliver shock if advised by device.\n5. Immediately resume CPR for 2 minutes after shock.",
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
                                Text(if (isBn) "• শক্ত সমতল পৃষ্ঠে শুইয়ে দিন\n• হাত সোজা রাখুন\n• বুক সম্পূর্ণ উঠতে দিন\n• বিরতি সর্বনিম্ন রাখুন" else "• Firm flat surface\n• Straight elbows\n• Full recoil\n• Minimal interruptions", fontSize = 12.sp, color = Color(0xFF065F46), lineHeight = 18.sp)
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
                                Text(if (isBn) "• ধীরে বা অগভীরে চাপবেন না\n• মুখে পানি ঢালবেন না\n• অযথা পালস খুঁজতে যাবেন না\n• আতঙ্কিত হবেন না" else "• Don't push too slow\n• Don't give water if unconscious\n• Don't waste time checking pulse\n• Don't panic", fontSize = 12.sp, color = Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: আমেরিকান হার্ট অ্যাসোসিয়েশন (AHA) ও ইউরোপিয়ান রিসাসিটেশন কাউন্সিল (ERC) স্ট্যান্ডার্ড।" else "Reference: American Heart Association (AHA) & European Resuscitation Council (ERC) Standards.",
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
fun SurvivalStepCard(step: String, label: String, modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(28.dp).background(Color(0xFF1A237E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(step, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun InternationalSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun CprStepItem(letter: String, title: String, desc: String) {
    val isDark = isSystemInDarkTheme()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(32.dp).background(Color(0xFF1A237E), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(letter, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
            Spacer(Modifier.height(2.dp))
            Text(desc, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray, lineHeight = 18.sp)
        }
    }
}
