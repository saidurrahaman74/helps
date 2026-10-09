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

class EyeInjuryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                EyeInjuryScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EyeInjuryScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.Visibility, null, tint = Color(0xFF0EA5E9), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "চোখের আঘাত ও রাসায়নিক স্প্ল্যাশ গাইড" else "Eye Injury & Chemical Splash Guide", fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                                text = if (isBn) "চোখে আঘাত ও কেমিক্যালের প্রাথমিক চিকিৎসা" else "Eye Injury & Chemical Splash First Aid",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "চোখ অত্যন্ত সংবেদনশীল অঙ্গ। রাসায়নিক পদার্থ ছিটকে পড়লে বা চোখে কোনো আঘাত পেলে তাৎক্ষণিক সঠিক পদক্ষেপে দৃষ্টিশক্তি রক্ষা করা সম্ভব।"
                                else 
                                    "The eye is extremely sensitive. Immediate proper action during chemical splashes or physical trauma can save vision.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White else Color.DarkGray,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Chemical Splash Protocol Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0C4A6E).copy(alpha = 0.6f) else Color(0xFFE0F2FE)),
                        border = BorderStroke(1.5.dp, Color(0xFF0EA5E9))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = if (isBn) "১. চোখে কেমিক্যাল বা রাসায়নিক পড়লে" else "1. Chemical Splash in Eye",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = if (isDark) Color(0xFF7DD3FC) else Color(0xFF0369A1)
                            )
                            Spacer(Modifier.height(10.dp))
                            EyeStepItem("1", if (isBn) "প্রচুর পানি দিয়ে ধোয়া" else "Flush with Water", if (isBn) "তাত্ক্ষণিকভাবে পরিষ্কার রানিং ট্যাপের পানি বা স্যালাইন দিয়ে চোখ কমপক্ষে ১৫-২০ মিনিট ধুতে থাকুন।" else "Immediately flush the eye with clean running water or saline for 15-20 minutes.")
                            Spacer(Modifier.height(8.dp))
                            EyeStepItem("2", if (isBn) "পলক খোলা রাখা" else "Keep Eyelids Open", if (isBn) "ধোয়ার সময় আঙুল দিয়ে চোখের পাতা ফাঁকা রাখুন যাতে ভেতরের কেমিক্যাল বের হয়ে যায়।" else "Hold eyelids open wide while flushing to rinse thoroughly behind lids.")
                            Spacer(Modifier.height(8.dp))
                            EyeStepItem("3", if (isBn) "কনট্যাক্ট লেন্স খোলা" else "Remove Contacts", if (isBn) "চোখে কনট্যাক্ট লেন্স থাকলে দ্রুত তা খুলে ফেলুন।" else "Remove contact lenses immediately if present.")
                        }
                    }
                }

                // Foreign Object / Blow Card
                item {
                    EyeSectionCard(
                        title = if (isBn) "২. চোখে কণা পড়া বা আঘাত পেলে" else "2. Foreign Object or Blow to Eye",
                        icon = Icons.Default.Healing,
                        color = Color(0xFF10B981)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            EyeBullet(if (isBn) "ধুলোবালি পড়লে চোখ কচলাবেন না (Rubbing scratches the cornea)। বারবার পলক ফেলুন যাতে পানির সাথে ময়লা বের হয়ে যায়।" else "Do not rub the eye. Blink repeatedly to let tears flush out dust.")
                            EyeBullet(if (isBn) "বস্তু আটকে থাকলে পানি দিয়ে ধুয়ে ফেলার চেষ্টা করুন, কিন্তু জোর করে বের করবেন না।" else "If particle is embedded, flush gently. Do not force it out.")
                            EyeBullet(if (isBn) "চোখে ভারী আঘাত বা বলের বাড়ি লাগলে আলতোভাবে ঠান্ডা সেঁক দিন (সরাসরি চাপ দেবেন না)।" else "For physical blows, apply a cold compress gently without pressing on the eyeball.")
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
                                Text(if (isBn) "• ১৫-২০ মিনিট পানি দিয়ে ধোয়া\n• পাতা খোলা রাখা\n• ঠান্ডা সেঁক দেওয়া\n• দ্রুত চিকিৎসকের কাছে যাওয়া" else "• Flush 15-20 mins\n• Keep eyelids open\n• Cold compress\n• See eye specialist fast", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF065F46), lineHeight = 18.sp)
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
                                Text(if (isBn) "• চোখ কচকচ করে ডলা\n• চোখে ওষুধ বা ড্রপ দেওয়া\n• বস্তু জোর করে বের করা\n• ব্যান্ডেজে অতিরিক্ত চাপ দেওয়া" else "• Don't rub eyes\n• Don't apply unprescribed drops\n• Don't force object out\n• Don't apply pressure", fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: আমেরিকান একাডেমি অফ অপথালমোলজি (AAO) ও বিশ্ব স্বাস্থ্য সংস্থা গাইডলাইন।" else "Reference: American Academy of Ophthalmology (AAO) & WHO Guidelines.",
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
fun EyeSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun EyeStepItem(num: String, title: String, desc: String) {
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
fun EyeBullet(text: String) {
    val isDark = isSystemInDarkTheme()
    Row(verticalAlignment = Alignment.Top) {
        Text("•", fontWeight = FontWeight.Black, color = Color(0xFF0EA5E9), modifier = Modifier.padding(end = 8.dp))
        Text(text, fontSize = 13.sp, color = if (isDark) Color.White else Color.DarkGray, lineHeight = 18.sp)
    }
}
