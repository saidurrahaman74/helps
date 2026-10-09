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

class AnimalBiteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                AnimalBiteScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalBiteScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.Pets, null, tint = Color(0xFFD97706), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "প্রাণীর কামড় ও জলাতঙ্ক গাইড" else "Animal Bite & Rabies Guide", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                                text = if (isBn) "কুকুর বা প্রাণীর কামড়ের প্রাথমিক চিকিৎসা" else "Dog & Animal Bite First Aid",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "কুকুর, বিড়াল বা অন্যান্য প্রাণীর কামড়ে জলাতঙ্ক (Rabies) ও ব্যাকটেরিয়াল সংক্রমণের মারাত্মক ঝুঁকি থাকে। বিশ্ব স্বাস্থ্য সংস্থার (WHO) নিয়ম অনুযায়ী তাৎক্ষণিক পানি দিয়ে ধোয়া ও সময়মতো টিকা নেওয়া জীবন রক্ষা করে।"
                                else 
                                    "Animal bites carry severe risks of Rabies and bacterial infections. Immediate WHO-protocol washing and timely vaccination are life-saving.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White else Color.DarkGray,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // WHO Protocol Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF451A03).copy(alpha = 0.6f) else Color(0xFFFFFBEB)),
                        border = BorderStroke(1.5.dp, Color(0xFFD97706))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = if (isBn) "ডাব্লিউএইচও (WHO) তাৎক্ষণিক প্রোটোকল" else "WHO Immediate Protocol",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309)
                            )
                            Spacer(Modifier.height(10.dp))
                            BiteStepItem("1", if (isBn) "সাবান ও পানি দিয়ে ধোয়া" else "Wash with Soap & Water", if (isBn) "ক্ষতস্থানটি প্রচুর পরিমাণে সাবান এবং রানিং ট্যাপের পানি দিয়ে অন্তত ১৫ মিনিট ধরে ধুয়ে ফেলুন।" else "Wash wound thoroughly with soap and plenty of running water for at least 15 minutes.")
                            Spacer(Modifier.height(8.dp))
                            BiteStepItem("2", if (isBn) "অ্যান্টিসেপটিক ব্যবহার" else "Apply Antiseptic", if (isBn) "পানি দিয়ে ধোয়ার পর পোভিডন আয়োডিন (Betadine) বা অ্যালকোহল বেসড অ্যান্টিসেপটিক লাগান।" else "Apply povidone-iodine (Betadine) or antiseptic solution after washing.")
                            Spacer(Modifier.height(8.dp))
                            BiteStepItem("3", if (isBn) "চিকিৎসকের পরামর্শ" else "Medical Evaluation", if (isBn) "অবিলম্বে হাসপাতালে গিয়ে জলাতঙ্কের টিকা (ARV) ও টিটেনাস টিকা নিন।" else "Visit hospital immediately for Rabies Vaccine (ARV) and Tetanus shot.")
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
                                Text(if (isBn) "• ১৫ মিনিট পানি দিয়ে ধোয়া\n• সাবান ব্যবহার করা\n• অ্যান্টিসেপটিক লাগানো\n• ২৪ ঘণ্টার মধ্যে টিকা নেওয়া" else "• Wash 15 mins\n• Use soap\n• Apply antiseptic\n• Vaccine within 24 hrs", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF065F46), lineHeight = 18.sp)
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
                                Text(if (isBn) "• হলুদ বা মাটি লাগানো\n• মরিচ বা পাতা দেওয়া\n• শক্ত করে ব্যান্ডেজ করা\n• অবহেলা করা" else "• No turmeric/soil\n• No chili/leaves\n• Don't tie too tight\n• Don't delay vaccine", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                // Rabies warning
                item {
                    BiteSectionCard(
                        title = if (isBn) "জলাতঙ্ক (Rabies) নিয়ে জরুরি সতর্কতা" else "Crucial Rabies Warning",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFEF4444)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            BiteBullet(if (isBn) "জলাতঙ্ক একটি ১০০% মৃত্যুঝুঁকিপূর্ণ কিন্তু প্রতিরোধযোগ্য রোগ।" else "Rabies is 100% fatal once symptoms appear, but 100% preventable with timely vaccination.")
                            BiteBullet(if (isBn) "পোষা প্রাণী হলেও নিয়ম মেনে টিকা নিতে হবে।" else "Even vaccinated pets can carry risks; consult a doctor immediately.")
                            BiteBullet(if (isBn) "ডেসকট বা গভীর কামড়ে ভ্যাকসিন ছাড়াও র‍্যাবিস ইমিউনোগ্লোবুলিন (RIG) লাগতে পারে।" else "Severe or deep bites may require Rabies Immunoglobulin (RIG) alongside vaccines.")
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: বিশ্ব স্বাস্থ্য সংস্থা (WHO) র‍্যাবিস প্রিভেনশন গাইডলাইন।" else "Reference: World Health Organization (WHO) Rabies Prevention Guidelines.",
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
fun BiteSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun BiteStepItem(num: String, title: String, desc: String) {
    val isDark = isSystemInDarkTheme()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(28.dp).background(Color(0xFFD97706), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(num, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309))
            Spacer(Modifier.height(2.dp))
            Text(desc, fontSize = 13.sp, color = if (isDark) Color.White else Color.DarkGray, lineHeight = 18.sp)
        }
    }
}

@Composable
fun BiteBullet(text: String) {
    val isDark = isSystemInDarkTheme()
    Row(verticalAlignment = Alignment.Top) {
        Text("•", fontWeight = FontWeight.Black, color = Color(0xFFEF4444), modifier = Modifier.padding(end = 8.dp))
        Text(text, fontSize = 13.sp, color = if (isDark) Color.White else Color.DarkGray, lineHeight = 18.sp)
    }
}
