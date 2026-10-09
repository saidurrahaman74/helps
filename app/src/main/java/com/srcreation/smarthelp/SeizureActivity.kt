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

class SeizureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                SeizureScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeizureScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.FlashOn, null, tint = Color(0xFF7C3AED), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "খিঁচুনি ও মৃগী রোগ গাইড" else "Seizure & Epilepsy Guide", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                                text = if (isBn) "খিঁচুনি (Seizure) প্রাথমিক চিকিৎসা" else "Seizure First Aid (The 3 S's)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "মস্তিষ্কের বৈদ্যুতিক কার্যকলাপে হঠাৎ ব্যাঘাত ঘটার ফলে খিঁচুনি হতে পারে। খিঁচুনি রোগীর প্রাথমিক চিকিৎসায় আন্তর্জাতিক স্বর্ণসূত্র হলো: Safe (নিরাপদ রাখুন), Side (পাশে কাত করুন), Stay (কাছে থাকুন)।"
                                else 
                                    "A seizure is a sudden surge of electrical activity in the brain. International first aid gold standard follows the 3 S's: Safe, Side, Stay.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White else Color.DarkGray,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // 3 S's Golden Rules Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF2E1065).copy(alpha = 0.6f) else Color(0xFFF3E8FF)),
                        border = BorderStroke(1.5.dp, Color(0xFF7C3AED))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = if (isBn) "৩টি সোনালী নিয়ম: SAFE – SIDE – STAY" else "3 Golden Rules: SAFE – SIDE – STAY",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = if (isDark) Color(0xFFC4B5FD) else Color(0xFF6D28D9)
                            )
                            Spacer(Modifier.height(10.dp))
                            SeizureStepItem("1", if (isBn) "নিরাপদ করুন (SAFE)" else "SAFE", if (isBn) "আশপাশের শক্ত বা তীক্ষ্ণ বস্তু সরিয়ে দিন। মাথায় নরম কিছু (বালিশ বা কাপড়) দিন।" else "Clear dangerous objects away. Cushion the head with something soft.")
                            Spacer(Modifier.height(8.dp))
                            SeizureStepItem("2", if (isBn) "পাশে কাত করুন (SIDE)" else "SIDE", if (isBn) "রোগীকে আলতো করে একপাশে (Recovery Position) কাত করুন যাতে শ্বাসনালি পরিষ্কার থাকে।" else "Gently roll person onto their side to keep airway clear.")
                            Spacer(Modifier.height(8.dp))
                            SeizureStepItem("3", if (isBn) "পাশে থাকুন (STAY)" else "STAY", if (isBn) "খিঁচুনি শেষ না হওয়া পর্যন্ত ও রোগী সম্পূর্ণ স্বাভাবিক না হওয়া পর্যন্ত কাছে থাকুন।" else "Stay with the person until seizure ends and they are fully alert.")
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
                                Text(if (isBn) "• সময় খেয়াল রাখুন\n• মাথা সুরক্ষিত রাখুন\n• ঢিলা পোশাক করুন\n• পাশে কাত করুন" else "• Time the seizure\n• Protect head\n• Loosen tight clothing\n• Side recovery position", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF065F46), lineHeight = 18.sp)
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
                                Text(if (isBn) "• মুখে কিছু দেবেন না\n• চেপে ধরে রাখবেন না\n• জোর করে আটকাবেন না\n• খিঁচুনির সময় পানি দেবেন না" else "• Don't put anything in mouth\n• Don't hold down/restrain\n• Don't give water during seizure\n• Don't panic", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                // When to call emergency
                item {
                    SeizureSectionCard(
                        title = if (isBn) "কখন জরুরি চিকিৎসা নেবেন?" else "When to Call Emergency",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFEF4444)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SeizureBullet(if (isBn) "খিঁচুনি ৫ মিনিটের বেশি স্থায়ী হলে।" else "Seizure lasts longer than 5 minutes.")
                            SeizureBullet(if (isBn) "জীবনে প্রথমবার খিঁচুনি হলে বা গর্ভবতী নারীর ক্ষেত্রে।" else "First-time seizure or pregnant individual.")
                            SeizureBullet(if (isBn) "খিঁচুনি থামার পর স্বাভাবিক জ্ঞান ফিরতে দেরি হলে বা পরপর একাধিক খিঁচুনি হলে।" else "Difficulty breathing or waking up after seizure, or multiple seizures back-to-back.")
                            SeizureBullet(if (isBn) "পানিতে বা আঘাতজনিত কারণে খিঁচুনি হলে।" else "Seizure occurred in water or resulted in physical injury.")
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: এপিলিপসি ফাউন্ডেশন (Epilepsy Foundation) ও বিশ্ব স্বাস্থ্য সংস্থা (WHO) গাইডলাইন।" else "Reference: Epilepsy Foundation & World Health Organization (WHO) Guidelines.",
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
fun SeizureSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun SeizureStepItem(num: String, title: String, desc: String) {
    val isDark = isSystemInDarkTheme()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(28.dp).background(Color(0xFF7C3AED), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(num, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color(0xFFC4B5FD) else Color(0xFF6D28D9))
            Spacer(Modifier.height(2.dp))
            Text(desc, fontSize = 13.sp, color = if (isDark) Color.White else Color.DarkGray, lineHeight = 18.sp)
        }
    }
}

@Composable
fun SeizureBullet(text: String) {
    val isDark = isSystemInDarkTheme()
    Row(verticalAlignment = Alignment.Top) {
        Text("•", fontWeight = FontWeight.Black, color = Color(0xFFEF4444), modifier = Modifier.padding(end = 8.dp))
        Text(text, fontSize = 13.sp, color = if (isDark) Color.White else Color.DarkGray, lineHeight = 18.sp)
    }
}
