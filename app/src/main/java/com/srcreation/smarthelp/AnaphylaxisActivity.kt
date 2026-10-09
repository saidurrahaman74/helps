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

class AnaphylaxisActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                AnaphylaxisScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaphylaxisScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.Healing, null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "তীব্র এলার্জি (অ্যানাফাইলাক্সিস)" else "Severe Allergy (Anaphylaxis)", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                                text = if (isBn) "অ্যানাফাইলাক্সিস কী?" else "What is Anaphylaxis?",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "অ্যানাফাইলাক্সিস হলো একটি তীব্র, দ্রুত বর্ধনশীল এবং জীবননাশক এলার্জিক প্রতিক্রিয়া। খাবার, ওষুধ, পোকামাকড়ের কামড় বা ল্যাটেক্সের কারণে এটি হতে পারে। তাৎক্ষণিক চিকিৎসা না নিলে এটি মারাত্মক হতে পারে।"
                                else 
                                    "Anaphylaxis is a severe, rapidly progressive, and potentially life-threatening allergic reaction triggered by food, medications, insect stings, or latex.",
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
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0C4A6E).copy(alpha = 0.6f) else Color(0xFFF0F9FF)),
                        border = BorderStroke(1.5.dp, Color(0xFF0EA5E9))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = if (isBn) "তাৎক্ষণিক করণীয় (International Protocol)" else "Immediate Action Protocol",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = if (isDark) Color(0xFF7DD3FC) else Color(0xFF0369A1)
                            )
                            Spacer(Modifier.height(10.dp))
                            AllergyStepItem("1", if (isBn) "এপিনেফ্রিন ব্যবহার করুন" else "Use Epinephrine", if (isBn) "রোগীর নিজস্ব এপিনেফ্রিন অটো-ইনজেক্টর (EpiPen) থাকলে দ্রুত ঊরুর বাইরের অংশে প্রয়োগ করুন।" else "If patient has an epinephrine auto-injector (EpiPen), use it immediately on outer mid-thigh.")
                            Spacer(Modifier.height(8.dp))
                            AllergyStepItem("2", if (isBn) "জরুরি সাহায্য নিন" else "Call Emergency", if (isBn) "তক্ষণাৎ হাসপাতালে যোগাযোগ করুন বা জরুরি সেবা ডাকুন।" else "Call emergency medical services immediately, even if symptoms improve.")
                            Spacer(Modifier.height(8.dp))
                            AllergyStepItem("3", if (isBn) "সঠিক পজিশন" else "Proper Positioning", if (isBn) "রোগীকে চিত করে শুইয়ে পা উঁচুতে রাখুন। শ্বাসকষ্ট হলে আধা-বসা অবস্থায় রাখুন।" else "Lay person flat with legs elevated. If breathing is difficult, allow them to sit up.")
                        }
                    }
                }

                // Symptoms
                item {
                    AllergySectionCard(
                        title = if (isBn) "প্রধান লক্ষণসমূহ (Key Symptoms)" else "Key Symptoms",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFEF4444)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            AllergyBullet(if (isBn) "মুখ, ঠোঁট, জিহ্বা বা গলা ফুলে যাওয়া ও শ্বাসনালী সংকুচিত হওয়া।" else "Swelling of lips, tongue, throat, or airway constriction.")
                            AllergyBullet(if (isBn) "ত্বকে চুলকানি, লালচে চাকা (Urticaria) বা র‍্যাশ।" else "Itchy skin hives, rashes, or flushed/pale skin.")
                            AllergyBullet(if (isBn) "শ্বাসের সময় হুইল বা শোঁ শোঁ শব্দ ও তীব্র শ্বাসকষ্ট।" else "Wheezing, shortness of breath, and coughing.")
                            AllergyBullet(if (isBn) "রক্তচাপ কমে যাওয়া, মাথা ঘোরানো বা অজ্ঞান হয়ে যাওয়া।" else "Drop in blood pressure, dizziness, confusion, or fainting.")
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
                                Text(if (isBn) "• দ্রুত এপিনেফ্রিন দেওয়া\n• জরুরি সেবা ডাকা\n• পা উঁচুতে রাখা\n• শান্ত রাখা" else "• Give epinephrine fast\n• Call emergency\n• Elevate legs\n• Keep calm", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF065F46), lineHeight = 18.sp)
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
                                Text(if (isBn) "• শুধু অ্যান্টিহিস্টামিন নির্ভর\n• একা ছেড়ে দেওয়া\n• দাঁড়িয়ে থাকতে দেওয়া\n• দেরি করা" else "• Don't rely on antihistamines alone\n• Don't leave alone\n• Don't let stand up\n• Don't delay", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: ইউরোপিয়ান একাডেমি অফ অ্যালার্জি অ্যান্ড ক্লিনিক্যাল ইমিউনোলজি (EAACI) গাইডলাইন।" else "Reference: European Academy of Allergy and Clinical Immunology (EAACI) Guidelines.",
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
fun AllergySectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun AllergyStepItem(num: String, title: String, desc: String) {
    val isDark = isSystemInDarkTheme()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(28.dp).background(Color(0xFF0EA5E9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(num, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color(0xFF7DD3FC) else Color(0xFF0369A1))
            Spacer(Modifier.height(2.dp))
            Text(desc, fontSize = 13.sp, color = if (isDark) Color.White else Color.DarkGray, lineHeight = 18.sp)
        }
    }
}

@Composable
fun AllergyBullet(text: String) {
    val isDark = isSystemInDarkTheme()
    Row(verticalAlignment = Alignment.Top) {
        Text("•", fontWeight = FontWeight.Black, color = Color(0xFFEF4444), modifier = Modifier.padding(end = 8.dp))
        Text(text, fontSize = 13.sp, color = if (isDark) Color.White else Color.DarkGray, lineHeight = 18.sp)
    }
}
