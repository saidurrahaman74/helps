package com.srcreation.smarthelp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.util.Locale

class NursePainScaleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                PainScaleScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PainScaleScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var selectedTab by remember { mutableIntStateOf(0) }

    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }

    val tabs = remember(isBn) {
        if (isBn) listOf(
            "NRS & Wong-Baker 😁",
            "FLACC (শিশু/প্যাডি) 👶",
            "CPOT (ICU/ভেন্টিলেটর) 🏥"
        ) else listOf(
            "NRS & Wong-Baker 😁",
            "FLACC (Pediatric) 👶",
            "CPOT (ICU/Ventilated) 🏥"
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = if (isBn) "পেইন অ্যাসেসমেন্ট স্কেল" else "Pain Assessment Scale",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBack()
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (isDark) Color.White else Color(0xFF1A237E))
                            }
                        },
                        actions = { LanguageToggleButton() },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )

                    // Subtag Banner (Placed right below TopBar)
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = if (isDark) 0.25f else 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = if (isDark) 0.5f else 0.3f)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = if (isBn) "🌐 আন্তর্জাতিক পেইন ম্যানেজমেন্ট স্ট্যান্ডার্ড (WHO / Joint Commission)" else "🌐 International Pain Management Standard (WHO / JCI)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    // Single Line Scrollable 3D Tabs
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(tabs) { index, title ->
                            val isSelected = selectedTab == index
                            val chipBg = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.88f))
                            val textColor = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155))

                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedTab = index
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = chipBg,
                                border = BorderStroke(1.dp, if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFCBD5E1))),
                                modifier = Modifier.shadow(if (isSelected) 6.dp else 1.dp, RoundedCornerShape(16.dp))
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textColor,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = { AppBottomBar(context = context, currentTab = -1, isNurseMode = true, backgroundBrush = AppGradientBrush) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    when (selectedTab) {
                        0 -> NrsWongBakerSection(isBn)
                        1 -> FlaccPediatricSection(isBn)
                        2 -> CpotIcuSection(isBn)
                    }
                }
            }
        }
    }
}

// 1. NRS & WONG-BAKER FACES SCALE (0-10)
@Composable
fun NrsWongBakerSection(isBn: Boolean) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var painLevel by remember { mutableIntStateOf(0) }

    PainResultCard(painLevel, isBn)

    // Numerical Rating Scale Slider
    Pain3DCard {
        Text(
            text = if (isBn) "নিউমেরিক্যাল রেটিং স্কেল (০-১০)" else "Numerical Rating Scale (NRS 0-10)",
            fontWeight = FontWeight.Black,
            color = if (isDark) Color.White else Color(0xFF1A237E),
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Slider(
            value = painLevel.toFloat(),
            onValueChange = { painLevel = it.toInt() },
            valueRange = 0f..10f,
            steps = 9,
            colors = SliderDefaults.colors(
                thumbColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                activeTrackColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f)
            )
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (isBn) "ব্যথা নেই (0)" else "No Pain (0)", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
            Text(if (isBn) "মাঝারি (5)" else "Moderate (5)", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
            Text(if (isBn) "অসহ্য ব্যথা (10)" else "Worst Pain (10)", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
        }
    }

    // Wong-Baker FACES Scale
    Pain3DCard {
        Text(
            text = if (isBn) "ওয়াং-বেকার ফেস স্কেল (Wong-Baker FACES®)" else "Wong-Baker FACES® Pain Scale",
            fontWeight = FontWeight.Black,
            color = if (isDark) Color.White else Color(0xFF1A237E),
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val faces = listOf(
                "0" to "😊",
                "2" to "🙂",
                "4" to "😐",
                "6" to "😟",
                "8" to "😢",
                "10" to "😭"
            )
            faces.forEach { (score, emoji) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        painLevel = score.toInt()
                    }
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = CircleShape,
                        color = if (painLevel == score.toInt()) (if (isDark) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color(0xFF1A237E).copy(alpha = 0.15f)) else Color.Transparent,
                        border = if (painLevel == score.toInt()) BorderStroke(1.5.dp, if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else null
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(score, fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (painLevel == score.toInt()) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray))
                }
            }
        }
    }

    // Clinical Guidance Card
    PainAdviceCard(painLevel, isBn)
}

// 2. FLACC PEDIATRIC BEHAVIORAL PAIN SCALE (0-10)
@Composable
fun FlaccPediatricSection(isBn: Boolean) {
    var face by remember { mutableIntStateOf(0) }
    var legs by remember { mutableIntStateOf(0) }
    var activity by remember { mutableIntStateOf(0) }
    var cry by remember { mutableIntStateOf(0) }
    var consolability by remember { mutableIntStateOf(0) }

    val flaccTotal = face + legs + activity + cry + consolability

    PainResultCard(flaccTotal, isBn)

    // F - Face
    PainCriterionCard(
        title = if (isBn) "F - ফেস / মুখের অভিব্যক্তি (Face)" else "F - Face",
        options = if (isBn) listOf(
            "হাসিমুখ / স্বাভাবিক অভিব্যক্তি" to 0,
            "সামান্য কুঁচকানো মুখ / চিন্তিত ভাব" to 1,
            "দাঁত কামড়ানো / মুখ থমথমে ও কুঁচকানো" to 2
        ) else listOf(
            "Relaxed, neutral expression" to 0,
            "Occasional grimace, frown, withdrawn" to 1,
            "Frequent/constant chin quivering, clenched jaw" to 2
        ),
        selectedScore = face
    ) { face = it }

    // L - Legs
    PainCriterionCard(
        title = if (isBn) "L - লেগস / পায়ের নড়াচড়া (Legs)" else "L - Legs",
        options = if (isBn) listOf(
            "পায়ের স্বাভাবিক অবস্থান / শিথিল" to 0,
            "অস্থির / টানটান পা" to 1,
            "লাথি মারা / পা পেটের দিকে টানা" to 2
        ) else listOf(
            "Normal position or relaxed" to 0,
            "Uneasy, restless, tense" to 1,
            "Kicking or legs drawn up" to 2
        ),
        selectedScore = legs
    ) { legs = it }

    // A - Activity
    PainCriterionCard(
        title = if (isBn) "A - এক্টিভিটি / শারীরিক নড়াচড়া (Activity)" else "A - Activity",
        options = if (isBn) listOf(
            "শান্তভাবে শুয়ে আছে / স্বাভাবিক নড়াচড়া" to 0,
            "ছটফট করা / এপাশ-ওপাশ করা" to 1,
            "শরীর বাঁকানো / শক্ত হয়ে থাকা / ঝাকুনি" to 2
        ) else listOf(
            "Lying quietly, normal position" to 0,
            "Squirming, shifting back and forth, tense" to 1,
            "Arched, rigid or jerking" to 2
        ),
        selectedScore = activity
    ) { activity = it }

    // C - Cry
    PainCriterionCard(
        title = if (isBn) "C - ক্রাই / কান্নার আওয়াজ (Cry)" else "C - Cry",
        options = if (isBn) listOf(
            "কোনো কান্না নেই (জাগনা/ঘুমে)" to 0,
            "প্যানপ্যান করা / মাঝেমধ্যে মৃদু কান্না" to 1,
            "অনবরত কান্না / চিত্কার করা" to 2
        ) else listOf(
            "No cry (awake or asleep)" to 0,
            "Moans or whimpers, occasional complaint" to 1,
            "Crying steadily, screams or sobs" to 2
        ),
        selectedScore = cry
    ) { cry = it }

    // C - Consolability
    PainCriterionCard(
        title = if (isBn) "C - কনসোলেবিলিটি / শান্ত করার ক্ষমতা (Consolability)" else "C - Consolability",
        options = if (isBn) listOf(
            "শান্ত ও সন্তুষ্ট" to 0,
            "আদর বা স্পর্শে শান্ত হয়" to 1,
            "শান্ত করা কঠিন / শান্ত হতে চায় না" to 2
        ) else listOf(
            "Content, relaxed" to 0,
            "Reassured by touching, hugging, talking to" to 1,
            "Difficult to console or comfort" to 2
        ),
        selectedScore = consolability
    ) { consolability = it }

    PainAdviceCard(flaccTotal, isBn)
}

// 3. CPOT - CRITICAL-CARE PAIN OBSERVATION TOOL (0-8)
@Composable
fun CpotIcuSection(isBn: Boolean) {
    var facial by remember { mutableIntStateOf(0) }
    var bodyMove by remember { mutableIntStateOf(0) }
    var muscleTension by remember { mutableIntStateOf(0) }
    var ventCompliance by remember { mutableIntStateOf(0) }

    val cpotTotal = facial + bodyMove + muscleTension + ventCompliance

    PainResultCard(cpotTotal, isBn, isCpot = true)

    // 1. Facial Expression
    PainCriterionCard(
        title = if (isBn) "১. মুখের অভিব্যক্তি (Facial Expression)" else "1. Facial Expression",
        options = if (isBn) listOf(
            "শিথিল মুখ (Relaxed)" to 0,
            "টানটান / চিন্তিত মুখ (Tense)" to 1,
            "মুখ কুঁচকানো / ভাজ করা (Grimacing)" to 2
        ) else listOf(
            "Relaxed, neutral face" to 0,
            "Tense, presence of frowning" to 1,
            "Grimacing, tightly closed eyelids" to 2
        ),
        selectedScore = facial
    ) { facial = it }

    // 2. Body Movements
    PainCriterionCard(
        title = if (isBn) "২. শারীরিক নড়াচড়া (Body Movements)" else "2. Body Movements",
        options = if (isBn) listOf(
            "কোনো অস্বাভাবিক নড়াচড়া নেই" to 0,
            "ধীরে নড়াচড়া / প্রটেক্টিভ মুভমেন্ট" to 1,
            "ছটফট করা / এজিটেটেড মুভমেন্ট" to 2
        ) else listOf(
            "No movement or normal position" to 0,
            "Protection or slow cautious movement" to 1,
            "Restlessness, pulling tube, agitation" to 2
        ),
        selectedScore = bodyMove
    ) { bodyMove = it }

    // 3. Muscle Tension
    PainCriterionCard(
        title = if (isBn) "৩. মাসল টেনশন / পেশির টান (Muscle Tension)" else "3. Muscle Tension",
        options = if (isBn) listOf(
            "পেশি শিথিল (Relaxed)" to 0,
            "পেশি শক্ত / রেজিস্ট্যান্স (Tense)" to 1,
            "খুবই শক্ত / ফ্ল্যাক্সড (Very Rigid)" to 2
        ) else listOf(
            "Relaxed (No resistance to passive movement)" to 0,
            "Tense, rigid (Resistance to passive movement)" to 1,
            "Very rigid (Strong resistance)" to 2
        ),
        selectedScore = muscleTension
    ) { muscleTension = it }

    // 4. Ventilator Compliance / Vocalization
    PainCriterionCard(
        title = if (isBn) "৪. ভেন্টিলেটর কমপ্লায়েন্স (Ventilator / Vocalization)" else "4. Ventilator Compliance",
        options = if (isBn) listOf(
            "ভেন্টিলেটরের সাথে মানিয়ে চলছে / কোনো কাশি নেই" to 0,
            "কাশি দেওয়া / ভেন্টিলেটর অ্যালার্ম বাজার উপক্রম" to 1,
            "ভেন্টিলেটরের সাথে ফাইট করা / বাধা দেওয়া" to 2
        ) else listOf(
            "Tolerating ventilator / No cough" to 0,
            "Coughing but tolerating / Alarms stop spontaneously" to 1,
            "Fighting ventilator / Alarms frequently triggered" to 2
        ),
        selectedScore = ventCompliance
    ) { ventCompliance = it }

    PainAdviceCard(cpotTotal, isBn, isCpot = true)
}

// ---------------- 3D HELPER COMPONENTS ----------------

@Composable
fun Pain3DCard(content: @Composable ColumnScope.() -> Unit) {
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.92f),
        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White),
        content = {
            Column(modifier = Modifier.padding(20.dp), content = content)
        }
    )
}

@Composable
fun PainResultCard(level: Int, isBn: Boolean, isCpot: Boolean = false) {
    val isDark = isSystemInDarkTheme()
    val maxScore = if (isCpot) 8 else 10
    val (status, color) = when {
        level == 0 -> (if (isBn) "ব্যথা নেই (No Pain)" else "No Pain (Safe)") to Color(0xFF22C55E)
        level <= (if (isCpot) 2 else 3) -> (if (isBn) "মৃদু ব্যথা (Mild Pain)" else "Mild Pain") to Color(0xFF0284C7)
        level <= (if (isCpot) 5 else 6) -> (if (isBn) "মাঝারি ব্যথা (Moderate Pain)" else "Moderate Pain") to Color(0xFFFF9800)
        else -> (if (isBn) "অসহ্য তীব্র ব্যথা (Severe Pain)" else "Severe / Unbearable Pain") to Color(0xFFEF4444)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else color),
        shape = RoundedCornerShape(28.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.95f),
        border = BorderStroke(1.5.dp, if (isDark) color else color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(if (isBn) "ব্যথার তীব্রতা (Pain Score)" else "Pain Score Intensity", fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$level / $maxScore", fontSize = 48.sp, fontWeight = FontWeight.Black, color = color)
            Surface(
                color = color,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun PainAdviceCard(level: Int, isBn: Boolean, isCpot: Boolean = false) {
    val isDark = isSystemInDarkTheme()
    val advice = when {
        level == 0 -> if (isBn)
            "কোনো পেইনকিলারের প্রয়োজন নেই। নিয়মিত পর্যবেক্ষণ করুন।"
        else
            "No pharmacological intervention required. Reassess periodically."
        level <= (if (isCpot) 2 else 3) -> if (isBn)
            "নন-ফার্মাকোলজিক্যাল চিকিৎসা (ঠান্ডা/গরম সেক, পজিশনিং) এবং হালকা অ্যানালজেসিক (যেমন প্যারাসিটামল) দেওয়া যেতে পারে।"
        else
            "Non-pharmacological comfort measures (positioning, ice/heat). Mild analgesics (e.g., Paracetamol/NSAID) if ordered."
        level <= (if (isCpot) 5 else 6) -> if (isBn)
            "ফার্মাকোলজিক্যাল চিকিৎসা আবশ্যক। মাঝারি অ্যানালজেসিক বা মৃদু ওপিওয়েড প্রয়োগ করুন এবং ডাক্তারকে জানান।"
        else
            "Pharmacological intervention required (WHO Step 2: Moderate analgesics / weak opioids). Notify attending physician."
        else -> if (isBn)
            "জরুরি শক্তিশালী অ্যানালজেসিক (যেমন ওপিওয়েড/মরফিন) প্রদান করুন। ভাইটালস মনিটর করুন ও ডিউটি ডাক্তারকে জরুরি ভিত্তিতে জানান।"
        else
            "IMMEDIATE potent pharmacological intervention required (WHO Step 3: Strong opioids like Morphine/Fentanyl). Urgent physician notification."
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.25f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, contentDescription = null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = if (isBn) "WHO পেইন লেডার ও নার্সিং কেয়ার নির্দেশিকা" else "WHO Analgesic Ladder & Clinical Guidance",
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(advice, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1E293B), lineHeight = 19.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun PainCriterionCard(
    title: String,
    options: List<Pair<String, Int>>,
    selectedScore: Int,
    onSelect: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    Column {
        Text(
            text = title,
            fontWeight = FontWeight.Black,
            color = if (isDark) Color.White else Color(0xFF1A237E),
            fontSize = 15.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (label, score) ->
                val isSelected = selectedScore == score
                val surfaceBg = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.88f))
                val labelColor = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1E293B))
                val ptPillBg = if (isSelected) (if (isDark) Color.Black.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.2f)) else (if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF1A237E).copy(alpha = 0.08f))
                val ptPillText = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                val borderColor = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.4f))

                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelect(score)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = surfaceBg,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            color = labelColor,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = ptPillBg,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "$score Pt",
                                color = ptPillText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
