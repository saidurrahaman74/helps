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

class HeatExhaustionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                HeatExhaustionScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatExhaustionScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.WbSunny, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "হিট এক্সহশন গাইড" else "Heat Exhaustion Guide", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                                text = if (isBn) "হিট এক্সহশন কী?" else "What is Heat Exhaustion?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "অতিরিক্ত গরমে শরীর থেকে अत्यधिक ঘাম ও পানি-লবণ বেরিয়ে যাওয়ার ফলে হিট এক্সহশন হয়। এটি হিট স্ট্রোকে রূপ নিতে পারে, তাই সময়মতো ঠান্ডা করা অত্যন্ত জরুরি।"
                                else 
                                    "Heat exhaustion occurs when the body loses excessive water and salt through heavy sweating in hot environments. Prompt cooling is essential to prevent heat stroke.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Symptoms
                item {
                    HeatSectionCard(
                        title = if (isBn) "প্রধান লক্ষণসমূহ (Symptoms)" else "Key Symptoms",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFF59E0B)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            HeatBullet(if (isBn) "অতিরিক্ত ঘাম ও দুর্বলতা" else "Heavy sweating and fatigue")
                            HeatBullet(if (isBn) "মাথা ঘোরা ও মাথাব্যথা" else "Dizziness and headache")
                            HeatBullet(if (isBn) "বমিভাব ও বমি হওয়া" else "Nausea and vomiting")
                            HeatBullet(if (isBn) "পেশিতে টান বা ক্র্যাম্প" else "Muscle cramps")
                            HeatBullet(if (isBn) "ঠান্ডা, ফ্যাকাশে ও স্যাঁতসেঁতে ত্বক" else "Cool, pale, clammy skin")
                        }
                    }
                }

                // Immediate Action Steps
                item {
                    HeatSectionCard(
                        title = if (isBn) "তাৎক্ষণিক করণীয় (Immediate Actions)" else "Immediate Actions",
                        icon = Icons.Default.Thermostat,
                        color = Color(0xFF10B981)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            HeatStepItem("1", if (isBn) "ছায়ায় বা ঠান্ডা স্থানে নিন" else "Move to Cool Place", if (isBn) "রোগীকে দ্রুত রোদ থেকে সরিয়ে ছায়া বা এসিযুক্ত ঠান্ডা স্থানে শুইয়ে দিন।" else "Immediately move person to shade or air-conditioned room.")
                            HeatStepItem("2", if (isBn) "পোশাক ঢিলা করুন" else "Loosen Clothing", if (isBn) "আঁটসাঁট বা অতিরিক্ত পোশাক খুলে দিন যাতে বাতাস চলাচল করতে পারে।" else "Remove tight or extra clothing to aid cooling.")
                            HeatStepItem("3", if (isBn) "শরীর ঠান্ডা করুন" else "Cool the Body", if (isBn) "ঠান্ডা ভেজা কাপড় কপাল ও ঘাড়ে দিন এবং ফ্যানের বাতাস দিন।" else "Apply cool damp cloths to forehead and neck, use fan.")
                            HeatStepItem("4", if (isBn) "তরল পান করান" else "Hydrate", if (isBn) "সচেতন থাকলে অল্প অল্প করে ঠান্ডা পানি বা ওরাল স্যালাইন (ORS) পান করান।" else "If conscious, sip cool water or oral rehydration solution (ORS).")
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
                                Text(if (isBn) "• ঠান্ডা স্থানে বিশ্রাম\n• ভেজা কাপড় দিয়ে মোছা\n• ওরাল স্যালাইন পান\n• আরামদায়ক পোশাক" else "• Rest in cool area\n• Sponge with damp cloth\n• Sip ORS / water\n• Loose clothing", fontSize = 12.sp, color = Color(0xFF065F46), lineHeight = 18.sp)
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
                                Text(if (isBn) "• ক্যাফেইন বা অ্যালকোহল\n• অচেতন অবস্থায় পান করানো\n• গরম রোদে রাখা\n• অবহেলা করা" else "• No caffeine / alcohol\n• No fluids if unconscious\n• Avoid hot sun\n• Don't ignore symptoms", fontSize = 12.sp, color = Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: মার্কিন যুক্তরাষ্ট্রের রোগ নিয়ন্ত্রণ ও প্রতিরোধ কেন্দ্র (CDC) এবং বিশ্ব স্বাস্থ্য সংস্থা (WHO) গাইডলাইন।" else "Reference: Centers for Disease Control and Prevention (CDC) & World Health Organization (WHO) Guidelines.",
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
fun HeatSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun HeatStepItem(num: String, title: String, desc: String) {
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
fun HeatBullet(text: String) {
    val isDark = isSystemInDarkTheme()
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
        Text("•", fontWeight = FontWeight.Black, color = Color(0xFFF59E0B), modifier = Modifier.padding(end = 8.dp))
        Text(text, fontSize = 14.sp, color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray, lineHeight = 20.sp)
    }
}
