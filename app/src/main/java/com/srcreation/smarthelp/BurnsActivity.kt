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

class BurnsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                BurnsScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BurnsScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.Whatshot, null, tint = Color(0xFFE65100), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "পোড়া ও স্ক্যাল্ড গাইড" else "Burns & Scalds Guide", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                                text = if (isBn) "বার্ন (Burn) বনাম স্ক্যাল্ড (Scald)" else "Burns vs. Scalds",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "• বার্ন (Burn): শুষ্ক তাপ যেমন আগুন, গরম লোহা, বিদ্যুৎ বা কেমিক্যালের কারণে হয়ে থাকে।\n• স্ক্যাল্ড (Scald): গরম তরল বা বাষ্প যেমন ফোটানো পানি, গরম স্যুপ বা তেল ও স্টিম থেকে হয়ে থাকে。"
                                else 
                                    "• Burn: Caused by dry heat (fire, hot iron, electricity, chemicals).\n• Scald: Caused by wet heat or steam (boiling water, hot oil, hot soup).",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // 4-Step Golden Rule Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color(0xFFFFF7ED)),
                        border = BorderStroke(1.5.dp, Color(0xFFF97316))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = if (isBn) "স্বর্ণসূত্র: STOP – COOL – COVER – CALL" else "Golden Rule: STOP – COOL – COVER – CALL",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = if (isDark) Color(0xFFFB923C) else Color(0xFFC2410C)
                            )
                            Spacer(Modifier.height(10.dp))
                            BurnStepItem("1", if (isBn) "থামুন (STOP)" else "STOP", if (isBn) "আগুনের উৎস থেকে দূরে সরুন। পোশাকে আগুন লাগলে মাটিতে শুয়ে গড়িয়ে নিন。" else "Stop source of heat. Drop and roll if clothes catch fire.")
                            Spacer(Modifier.height(8.dp))
                            BurnStepItem("2", if (isBn) "ঠান্ডা করুন (COOL)" else "COOL", if (isBn) "পোড়া অংশে কমপক্ষে ২০ মিনিট ঠান্ডা প্রবাহমান পানি দিন。" else "Cool burn under cool running water for at least 20 minutes.")
                            Spacer(Modifier.height(8.dp))
                            BurnStepItem("3", if (isBn) "ঢেকে দিন (COVER)" else "COVER", if (isBn) "পরিষ্কার, জীবাণুমুক্ত নন-স্টিক গজ বা পাতলা কাপড় দিয়ে আলতোভাবে ঢেকে দিন。" else "Cover loosely with clean, sterile, non-stick dressing or cloth.")
                            Spacer(Modifier.height(8.dp))
                            BurnStepItem("4", if (isBn) "সহায়তা নিন (CALL)" else "CALL", if (isBn) "বড়, গভীর বা রাসায়নিক পোড়ায় দ্রুত হাসপাতালে যোগাযোগ করুন。" else "Seek emergency medical help for severe, deep, or chemical burns.")
                        }
                    }
                }

                // Degrees of Burn Table
                item {
                    BurnSectionCard(
                        title = if (isBn) "পোড়ার গভীরতা ও শ্রেণিবিভাগ (Degrees of Burns)" else "Degrees of Burns",
                        icon = Icons.Default.Equalizer,
                        color = Color(0xFFF97316)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            BurnDegreeRow(if (isBn) "প্রথম ডিগ্রি (Superficial)" else "1st Degree (Superficial)", if (isBn) "শুধু ত্বকের উপরিস্তর লাল হয় ও ব্যথা করে (যেমন: হালকা রোদে পোড়া)।" else "Red, painful, outer layer of skin only.")
                            BurnDegreeRow(if (isBn) "দ্বিতীয় ডিগ্রি (Partial-Thickness)" else "2nd Degree (Partial-Thickness)", if (isBn) "ফোসকা পড়ে, তীব্র ব্যথা ও ত্বক লালচে-ভেজা দেখায়।" else "Blisters, severe pain, moist pink/red skin.")
                            BurnDegreeRow(if (isBn) "তৃতীয় ডিগ্রি (Full-Thickness)" else "3rd Degree (Full-Thickness)", if (isBn) "ত্বক সাদা, কালো বা চামড়াহীন হয়ে যায়; স্নায়ু নষ্ট হওয়ায় ব্যথা কম হতে পারে।" else "White/charred skin, painless due to nerve damage.")
                        }
                    }
                }

                // DOs and DON'Ts
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFFECFDF5)),
                            border = BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("✅ DOs", fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF34D399) else Color(0xFF047857), fontSize = 14.sp)
                                Spacer(Modifier.height(6.dp))
                                Text(if (isBn) "• ২০ মিনিট ঠান্ডা পানি\n• আলতো কাপড় দিয়ে ঢাকা\n• ফোসকা অক্ষত রাখা\n• ঘড়ি ও আংটি খোলা" else "• 20 mins cool water\n• Loose clean cover\n• Keep blisters intact\n• Remove rings/watches", fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF065F46), lineHeight = 18.sp)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.5f) else Color(0xFFFEF2F2)),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("❌ DON'Ts", fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFFF87171) else Color(0xFFB91C1C), fontSize = 14.sp)
                                Spacer(Modifier.height(6.dp))
                                Text(if (isBn) "• বরফ বা বরফজল ব্যবহার\n• টুথপেস্ট বা তেল লাগানো\n• ফোসকা ফাটানো\n• লেগে থাকা কাপড় টানা" else "• No ice or ice-water\n• No toothpaste/oil\n• Don't pop blisters\n• Don't pull stuck cloth", fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: আমেরিকান বার্ন অ্যাসোসিয়েশন (ABA) ও বিশ্ব স্বাস্থ্য সংস্থা (WHO) গাইডলাইন।" else "Reference: American Burn Association (ABA) & World Health Organization (WHO) Guidelines.",
                        fontSize = 11.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray,
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
fun BurnSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun BurnStepItem(num: String, title: String, desc: String) {
    val isDark = isSystemInDarkTheme()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(28.dp).background(Color(0xFFEA580C), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(num, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color(0xFF38BDF8) else Color(0xFFEA580C))
            Spacer(Modifier.height(2.dp))
            Text(desc, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray, lineHeight = 18.sp)
        }
    }
}

@Composable
fun BurnDegreeRow(type: String, desc: String) {
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
