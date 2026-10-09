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

class FracturesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                FracturesScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FracturesScreen(onBack: () -> Unit) {
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
                            Icon(Icons.Default.HealthAndSafety, null, tint = Color(0xFF6366F1), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "হাড় ভাঙা ও ফ্রে্যাকচার গাইড" else "Fractures & Bone Injury Guide", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) 
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
                                text = if (isBn) "হাড় ভাঙা বা ফ্রে্যাকচার প্রাথমিক চিকিৎসা" else "First Aid for Bone Fractures",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (isBn) 
                                    "হাড় ভেঙে গেলে বা ফ্রে্যাকচার সন্দেহ হলে আক্রান্ত অঙ্গ নাড়াচাড়া করা থেকে বিরত থাকুন। সঠিকভাবে স্প্লিন্টিং (Immobilization) না করলে রক্তনালী বা স্নায়ু ক্ষতিগ্রস্ত হতে পারে।"
                                else 
                                    "If a fracture is suspected, immobilize the injured area immediately. Moving a broken bone can cause further damage to surrounding blood vessels and nerves.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Step-by-Step Protocol
                item {
                    FractureSectionCard(
                        title = if (isBn) "৪-ধাপের ফ্রে্যাকচার ব্যবস্থাপনা (4-Step Management)" else "4-Step Fracture Management",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF6366F1)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            FractureStepItem("1", if (isBn) "নড়াচড়া বন্ধ করুন" else "Immobilize", if (isBn) "আহত অঙ্গ যে অবস্থায় আছে সেভাবেই স্থির রাখুন। সোজা করার চেষ্টা করবেন না।" else "Keep injured limb in the position found. Do not try to straighten it.")
                            FractureStepItem("2", if (isBn) "স্প্লিন্ট বা বাঁধন দিন" else "Splinting", if (isBn) "কাঠ বা শক্ত কার্ডবোর্ড দিয়ে ভাঙা অংশের ওপর ও নিচের জয়েন্টসহ স্থির করুন।" else "Use rigid splints (wood/cardboard) to immobilize joints above and below fracture.")
                            FractureStepItem("3", if (isBn) "বরফ বা ঠান্ডা সেঁক" else "Cold Compress", if (isBn) "ফোলা ও ব্যথা কমাতে কাপড়ে মোড়ানো বরফ ১৫-২০ মিনিট দিন (সরাসরি ত্বকে নয়)।" else "Apply cloth-wrapped ice pack for 15-20 mins to reduce swelling and pain.")
                            FractureStepItem("4", if (isBn) "হাসপাতালে স্থানান্তর" else "Hospital Transfer", if (isBn) "খোলা ফ্রে্যাকচারে (হাড় বের হয়ে থাকলে) জীবাণুমুক্ত গজ দিয়ে ঢেকে দ্রুত হাসপাতালে নিন।" else "For open fractures, cover gently with sterile gauze and transfer to hospital.")
                        }
                    }
                }

                // CMS Check (Circulation, Motor, Sensory)
                item {
                    FractureSectionCard(
                        title = if (isBn) "রক্ত চলাচল ও অনুভূতি পরীক্ষা (CMS Check)" else "CMS Check (Circulation, Motor, Sensory)",
                        icon = Icons.Default.MonitorHeart,
                        color = Color(0xFF0284C7)
                    ) {
                        Text(
                            text = if (isBn)
                                "স্প্লিন্ট বাঁধার আগে ও পরে অবশ্যই পরীক্ষা করুন:\n• আঙুলের রং স্বাভাবিক ও গরম আছে কি না (Circulation)\n• আঙুল নড়াচড়া করতে পারে কি না (Motor)\n• অবশ বা ঝিনঝিন করছে কি না (Sensory)\n* যদি আঙুল নীল বা ঠান্ডা হয়ে যায়, তবে বাঁধন কিছুটা ঢিলা করুন।"
                            else
                                "Check before and after splinting:\n• Skin color and warmth (Circulation)\n• Ability to wiggle fingers/toes (Motor)\n• Numbness or tingling (Sensory)\n* If digits turn blue or cold, loosen the bandage immediately.",
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
                                Text(if (isBn) "• অঙ্গ স্থির রাখা\n• বরফ সেঁক দেওয়া\n• খোলা ক্ষতে গজ দেওয়া\n• দ্রুত সাহায্য নেওয়া" else "• Keep limb still\n• Cold compress\n• Cover open wounds\n• Get medical help", fontSize = 12.sp, color = Color(0xFF065F46), lineHeight = 18.sp)
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
                                Text(if (isBn) "• হাড় সোজা করা\n• রোগীকে হাঁটানো\n• মালি করা\n• খুব শক্ত বাঁধা" else "• Don't straighten bone\n• Don't let walk\n• No massage\n• Don't tie too tight", fontSize = 12.sp, color = Color(0xFF991B1B), lineHeight = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = if (isBn) "রেফারেন্স: অর্থোপেডিক ট্রমা অ্যাসোসিয়েশন (OTA) ও আন্তর্জাতিক রেড ক্রস স্ট্যান্ডার্ড।" else "Reference: Orthopaedic Trauma Association (OTA) & International Red Cross Standards.",
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
fun FractureSectionCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
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
fun FractureStepItem(num: String, title: String, desc: String) {
    val isDark = isSystemInDarkTheme()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(32.dp).background(Color(0xFF6366F1), CircleShape),
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
