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

class InsectStingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                InsectStingScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsectStingScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.BugReport, null, tint = Color(0xFFCA8A04), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "পোকা ও মৌমাছির কামড় গাইড" else "Insect & Bee Sting Guide", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                                text = if (isBn) "পোকা বা মৌমাছির হুল ফোটার প্রাথমিক চিকিৎসা" else "Insect & Bee Sting First Aid",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "মৌমাছির, বোলতা বা অন্যান্য পোকার কামড়ে সাধারণত হালকা ব্যথা, লালভাব ও ফোলাভাব হয়। তবে সংবেদনশীল ব্যক্তিদের ক্ষেত্রে মারাত্মক অ্যানাফাইল্যাক্সিস (অ্যালার্জিক শক) হতে পারে।"
                                else 
                                    "Bee, wasp, or insect stings cause localized pain, redness, and swelling. In sensitive individuals, it can trigger severe allergic reactions.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White else Color.DarkGray,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // International Protocol Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF451A03).copy(alpha = 0.6f) else Color(0xFFFEFCE8)),
                        border = BorderStroke(1.5.dp, Color(0xFFCA8A04))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = if (isBn) "ধাপে ধাপে প্রাথমিক চিকিৎসা" else "Step-by-Step First Aid",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = if (isDark) Color(0xFFFDE047) else Color(0xFF854D0E)
                            )
                            Spacer(Modifier.height(10.dp))
                            StingStepItem("1", if (isBn) "হুল ফেলে দিন (Scrape Stinger)" else "Remove Stinger", if (isBn) "মৌমাছির হুল ত্বকে থাকলে চিমটি দিয়ে না টেনে নখ বা ক্রেডিট কার্ডের ধার দিয়ে হালকাভাবে আঁচড়ে ফেলে দিন।" else "If stinger is present, scrape it off gently with a fingernail or card edge. Do not use tweezers.")
                            Spacer(Modifier.height(8.dp))
                            StingStepItem("2", if (isBn) "সাবান ও পানি দিয়ে ধোয়া" else "Wash with Soap & Water", if (isBn) "আক্রান্ত স্থানটি সাবান ও পরিষ্কার পানি দিয়ে আলতোভাবে ধুয়ে ফেলুন।" else "Gently wash the sting area with mild soap and clean water.")
                            Spacer(Modifier.height(8.dp))
                            StingStepItem("3", if (isBn) "ঠান্ডা সেঁক দিন (Cold Compress)" else "Apply Cold Compress", if (isBn) "ফোলা ও ব্যথা কমাতে বরফ বা ঠান্ডা ভেজা কাপড় ১০ মিনিট সেঁক দিন।" else "Apply a cold pack or ice wrapped in cloth for 10 minutes to reduce swelling.")
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
                                Text(if (isBn) "• হুল আঁচড়ে তোলা\n• ঠান্ডা সেঁক দেওয়া\n• সাবান দিয়ে ধোয়া\n• লক্ষণ পর্যবেক্ষণ" else "• Scrape out stinger\n• Cold compress\n• Wash with soap\n• Monitor symptoms", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF065F46), lineHeight = 18.sp)
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
                                Text(if (isBn) "• চিমটি দিয়ে হুল টানা\n• ঘষে চুলকানো\n• মাটি বা টুথপেস্ট লাগান\n• অবহেলা করা" else "• No tweezers on stinger\n• Don't scratch\n• No mud/toothpaste\n• Don't ignore breathing issues", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                // Allergy warning
                item {
                    StingSectionCard(
                        title = if (isBn) "মারাত্মক অ্যালার্জি বা অ্যানাফাইল্যাক্সিস লক্ষণ" else "Severe Allergy (Anaphylaxis) Signs",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFEF4444)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            StingBullet(if (isBn) "মুখ, ঠোঁট, জিভ বা গলা ফুলে যাওয়া ও শ্বাসকষ্ট হওয়া।" else "Swelling of lips, tongue, throat, or difficulty breathing.")
                            StingBullet(if (isBn) "মাথা ঘোরা, অজ্ঞান হয়ে যাওয়া বা অতিরিক্ত দুর্বলতা।" else "Dizziness, fainting, or severe drop in blood pressure.")
                            StingBullet(if (isBn) "পুরো শরীরে চাকা চাকা লাল র‍্যাশ বা চুলকানি (Hives)।" else "Widespread hives, itching, or skin flushing.")
                            StingBullet(if (isBn) "এমন লক্ষণ দেখা দিলে তাৎক্ষণিক জরুরি হাসপাতালে যান।" else "If these occur, seek emergency medical care immediately.")
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: মেয়ো ক্লিনিক (Mayo Clinic) ও রেড ক্রস ফার্স্ট এইড গাইডলাইন।" else "Reference: Mayo Clinic & Red Cross First Aid Guidelines.",
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
fun StingSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun StingStepItem(num: String, title: String, desc: String) {
    val isDark = isSystemInDarkTheme()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(28.dp).background(Color(0xFFCA8A04), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(num, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color(0xFFFDE047) else Color(0xFF854D0E))
            Spacer(Modifier.height(2.dp))
            Text(desc, fontSize = 13.sp, color = if (isDark) Color.White else Color.DarkGray, lineHeight = 18.sp)
        }
    }
}

@Composable
fun StingBullet(text: String) {
    val isDark = isSystemInDarkTheme()
    Row(verticalAlignment = Alignment.Top) {
        Text("•", fontWeight = FontWeight.Black, color = Color(0xFFEF4444), modifier = Modifier.padding(end = 8.dp))
        Text(text, fontSize = 13.sp, color = if (isDark) Color.White else Color.DarkGray, lineHeight = 18.sp)
    }
}
