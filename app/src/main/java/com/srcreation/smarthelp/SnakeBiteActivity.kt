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

class SnakeBiteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                SnakeBiteScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeBiteScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "সাপের কামড় প্রাথমিক চিকিৎসা" else "Snake Bite First Aid", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                                text = if (isBn) "সাপের কামড়: জরুরি চিকিৎসা প্রোটোকল" else "Snake Bite: Emergency Protocol",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "সাপের কামড় সবসময় একটি জীবন-সংশয়ী জরুরি অবস্থা। বিষধর হোক বা না হোক, রোগীকে দ্রুত হাসপাতালে নেওয়া এবং অঙ্গ স্থির রাখা সবচেয়ে গুরুত্বপূর্ণ।"
                                else 
                                    "A snake bite is always a medical emergency. Keeping the patient calm, immobilizing the limb, and transferring to a hospital rapidly are vital.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Step-by-Step Protocol
                item {
                    SnakeSectionCard(
                        title = if (isBn) "প্রাথমিক চিকিৎসার প্রধান ধাপসমূহ (Steps)" else "Immediate Action Steps",
                        icon = Icons.Default.FactCheck,
                        color = Color(0xFFEF4444)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            SnakeStepItem("1", if (isBn) "নিরাপদে দূরে সরুন" else "Move to Safety", if (isBn) "সাপের নাগাল থেকে দূরে যান। সাপ ধরতে বা মারতে যাবেন না।" else "Move away from the snake. Do not try to catch or kill it.")
                            SnakeStepItem("2", if (isBn) "অঙ্গ স্থির রাখুন" else "Immobilize Limb", if (isBn) "কামড় খাওয়া হাত বা পা লড়াচড়া করতে দেবেন না (স্প্লিন্ট ব্যবহার করতে পারেন)।" else "Keep the bitten limb still to slow venom spread.")
                            SnakeStepItem("3", if (isBn) "আঁটসাঁট জিনিস খুলুন" else "Remove Constrictions", if (isBn) "হাতের আংটি, ঘড়ি বা পায়ের জুতা ও আঁটসাঁট পোশাক খুলে ফেলুন।" else "Remove rings, watches, shoes, and tight clothing before swelling begins.")
                            SnakeStepItem("4", if (isBn) "দ্রুত হাসপাতালে নিন" else "Rush to Hospital", if (isBn) "রোগীকে হাঁটাতে দেবেন না; স্ট্রেচার বা বাহনে করে দ্রুত হাসপাতালে নিন।" else "Do not let patient walk; transport via vehicle or stretcher immediately.")
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
                                Text(if (isBn) "• শান্ত রাখা\n• অঙ্গ স্থির রাখা\n• ঘড়ি/আংটি খোলা\n• দ্রুত হাসপাতাল" else "• Keep calm\n• Immobilize limb\n• Remove jewelry\n• Fast hospital transfer", fontSize = 12.sp, color = Color(0xFF065F46), lineHeight = 18.sp)
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
                                Text(if (isBn) "• ক্ষত কাটা বা চোষা\n• টুর্নিকেট বা দড়ি বাঁধা\n• বরফ বা রাসায়নিক\n• ওঝা/বৈদ্যের কাছে যাওয়া" else "• Don't cut or suck\n• No tourniquets\n• No ice/chemicals\n• Avoid traditional delay", fontSize = 12.sp, color = Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: বিশ্ব স্বাস্থ্য সংস্থা (WHO) স্নেক বাইট ম্যানেজমেন্ট গাইডলাইন।" else "Reference: World Health Organization (WHO) Snakebite Management Guidelines.",
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
fun SnakeSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun SnakeStepItem(num: String, title: String, desc: String) {
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
