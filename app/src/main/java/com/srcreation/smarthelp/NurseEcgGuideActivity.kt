package com.srcreation.smarthelp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.util.Locale

class NurseEcgGuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                EcgGuideScreen(onBack = { finish() })
            }
        }
    }
}

data class EcgRhythmData(
    val titleEn: String,
    val titleBn: String,
    val category: String,
    val rate: String,
    val pWaveEn: String,
    val pWaveBn: String,
    val prInterval: String,
    val qrsDuration: String,
    val significanceEn: String,
    val significanceBn: String,
    val actionEn: String,
    val actionBn: String,
    val isEmergency: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcgGuideScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var selectedCategory by remember { mutableStateOf("ALL") }

    val isBn = remember { Locale.getDefault().language == "bn" || Locale.getDefault().language == "bn" }

    val categoryTabs = remember(isBn) {
        if (isBn) listOf(
            "ALL" to "সব রিদম (All)",
            "MI" to "ইসকেমিয়া ও STEMI 🫀",
            "ARRHYTHMIA" to "অ্যারোথমিয়া (SVT/AFib) ⚡",
            "BLOCK" to "AV হার্ট ব্লক (Blocks) 🔌",
            "ARREST" to "জরুরি এরেস্ট (VT/VF) 🚨",
            "RULE" to "ইসিজি ৫-স্টেপ রুলস 📚"
        ) else listOf(
            "ALL" to "All Rhythms",
            "MI" to "Ischemia & STEMI 🫀",
            "ARRHYTHMIA" to "Arrhythmias ⚡",
            "BLOCK" to "AV Heart Blocks 🔌",
            "ARREST" to "Arrest & VT/VF 🚨",
            "RULE" to "5-Step ECG Rules 📚"
        )
    }

    val rhythms = remember {
        listOf(
            // 1. ISCHEMIA & STEMI
            EcgRhythmData(
                titleEn = "ST-Elevation MI (STEMI)",
                titleBn = "এসটি-এলিভেশন মায়োকার্ডিয়াল ইনফার্কশন (STEMI - হার্ট অ্যাটাক)",
                category = "MI",
                rate = "60-120 bpm (Variable)",
                pWaveEn = "Normal or altered",
                pWaveBn = "স্বাভাবিক বা পরিবর্তিত",
                prInterval = "0.12 - 0.20 sec",
                qrsDuration = "0.08 - 0.12 sec",
                significanceEn = "ACUTE MYOCARDIAL INFARCTION. Tombstone ST elevation in contiguous leads.",
                significanceBn = "তীব্র হৃদরোগ বা হার্ট অ্যাটাক। ইসিজির ২ বা ততধিক লিডে ST সেগমেন্ট উপরে উঠে যায় (Tombstone ST)।",
                actionEn = "EMERGENCY: Activate Cath Lab / Code STEMI. Give MONA (Morphine, Oxygen, Nitrates, Aspirin). Continuous ECG.",
                actionBn = "জরুরি: ক্যাথ ল্যাব কালার দিয়ে Code STEMI অ্যালার্ট দিন। MONA প্রোটোকল অনুসরণ করুন এবং রোগীর অক্সিজেন দিন।",
                isEmergency = true
            ),
            EcgRhythmData(
                titleEn = "Hyperkalemia (High Potassium)",
                titleBn = "হাইপারক্যালিমিয়া (রক্তে পটাসিয়াম বৃদ্ধি)",
                category = "MI",
                rate = "Variable",
                pWaveEn = "Flattened or absent",
                pWaveBn = "চ্যাপ্টা বা অনুপস্থিত",
                prInterval = "Prolonged (> 0.20s)",
                qrsDuration = "Widened (> 0.12s)",
                significanceEn = "Tall peaked T-waves, wide QRS. Can progress to Sine Wave and Cardiac Arrest.",
                significanceBn = "উঁচু তীক্ষ্ণ T-ওয়েভ ও চওড়া QRS কম্প্লেক্স। দ্রুত চিকিৎসা না দিলে কার্ডিয়াক এরেস্ট হতে পারে।",
                actionEn = "Notify MD immediately. Prepare Calcium Gluconate 10%, Insulin + D50, Salbutamol, and Dialysis.",
                actionBn = "ডাক্তারকে জানান। ক্যালসিয়াম গ্লুকোনেট ১০%, ইনসুলিন+গ্লুকোজ এবং ডায়ালাইসিস রেডি করুন।",
                isEmergency = true
            ),

            // 2. ARRHYTHMIAS
            EcgRhythmData(
                titleEn = "Atrial Fibrillation (AFib)",
                titleBn = "অ্যাট্রিয়াল ফাইব্রিলেশন (AFib)",
                category = "ARRHYTHMIA",
                rate = "Atrial 350-600, Ventricular variable",
                pWaveEn = "No P-waves (Fibrillatory f-waves)",
                pWaveBn = "কোনো সুস্পষ্ট P-ওয়েভ নেই (ছোট কাঁপাকাঁপি ওয়েভ)",
                prInterval = "Not measurable",
                qrsDuration = "< 0.10 sec (Irregularly Irregular)",
                significanceEn = "Irregularly irregular rhythm. High risk for LA thrombus and Stroke.",
                significanceBn = "অনিয়মিত হূদস্পন্দন। বাম অলিন্দে রক্ত জমাট বেঁধে স্ট্রোকের চরম ঝুঁকি বাড়ায়।",
                actionEn = "Assess ventricular rate. Administer Beta-Blockers / Diltiazem. Monitor Anticoagulation (Warfarin/NOAC).",
                actionBn = "হার্ট রেট চেক করুন। বিটা-ব্লকার দেওয়া ও রক্তের পাতলা রাখার ওষুধ (এন্টিকোয়াগুল্যান্ট) মনিটর করুন।",
                isEmergency = false
            ),
            EcgRhythmData(
                titleEn = "Supraventricular Tachycardia (SVT)",
                titleBn = "সুপ্রাসিরিয়াস ট্যাকিকার্ডিয়া (SVT)",
                category = "ARRHYTHMIA",
                rate = "150 - 250 bpm",
                pWaveEn = "Hidden in preceding T-wave",
                pWaveBn = "T-ওয়েভের পেছনে ঢাকা পড়ে যায়",
                prInterval = "Not measurable",
                qrsDuration = "Narrow (< 0.10 sec)",
                significanceEn = "Extremely rapid heart rate originating above ventricles. Decreases cardiac output.",
                significanceBn = "অত্যন্ত দ্রুত স্পন্দন। হূৎপিণ্ডের পাম্পিং ক্ষমতা কমিয়ে মাথা ঘোরা ও প্রেশার নামিয়ে দেয়।",
                actionEn = "Perform Vagal Maneuvers (Valsalva). Administer Adenosine 6mg rapid IV push with saline flush.",
                actionBn = "ভেগাল ম্যানুভার করান। অ্যাডেনোসিন ৬ মিগ্রা দ্রুত আইভি পুশ করে সাথে সাথে নরমাল স্যালাইন দিয়ে ফ্ল্যাশ করুন।",
                isEmergency = true
            ),

            // 3. AV BLOCKS
            EcgRhythmData(
                titleEn = "1st Degree AV Block",
                titleBn = "ফার্স্ট ডিগ্রি এভি হার্ট ব্লক (1st Degree Block)",
                category = "BLOCK",
                rate = "60 - 100 bpm",
                pWaveEn = "Normal, precedes each QRS",
                pWaveBn = "স্বাভাবিক, প্রতিটি QRS এর আগে থাকে",
                prInterval = "Consistently Prolonged (> 0.20 sec / 5 small boxes)",
                qrsDuration = "< 0.10 sec",
                significanceEn = "Delayed conduction through AV node. Usually benign.",
                significanceBn = "এভি নোডের মধ্য দিয়ে বিদ্যুৎ পরিবহনে সামান্য বিলম্ব। সাধারণত ক্ষতিকারক নয়।",
                actionEn = "Monitor PR interval. Check for Digoxin or Beta-blocker toxicity.",
                actionBn = "PR ইন্টারভাল চেক করুন। ডিগক্সিন বা বিটা-ব্লকার ওষুধের সাইড-ইফেক্ট আছে কিনা দেখুন।",
                isEmergency = false
            ),
            EcgRhythmData(
                titleEn = "3rd Degree Complete Heart Block (CHB)",
                titleBn = "থার্ড ডিগ্রি কমপ্লিট হার্ট ব্লক (3rd Degree Complete Block)",
                category = "BLOCK",
                rate = "20 - 40 bpm",
                pWaveEn = "P-P regular, independent of QRS",
                pWaveBn = "P-ওয়েভ এবং QRS কম্প্লেক্স সম্পূর্ণ স্বাধীন",
                prInterval = "No relation (P and QRS dissociated)",
                qrsDuration = "Wide (> 0.12 sec) or escape",
                significanceEn = "Complete AV dissociation. Severe bradycardia, fainting, and low cardiac output.",
                significanceBn = "অলিন্দ ও নিলয়ের মধ্যে সংযোগ সম্পূর্ণ বিচ্ছিন্ন। ব্র্যাডিকার্ডিয়া, অজ্ঞান হওয়া ও রক্তের প্রেশার কমে যাওয়া।",
                actionEn = "EMERGENCY: Apply Transcutaneous Pacemaker immediately. Administer Atropine 1mg / Isoprenaline. Prepare for permanent pacemaker.",
                actionBn = "জরুরি: পেসমেকার (Transcutaneous Pacing) রেডি করুন। অ্যাট্রোপিন ১ মিগ্রা বা আইসোপ্রেনালিন দিন এবং স্থায়ী পেসমেকার প্রিপারেশন নিন।",
                isEmergency = true
            ),

            // 4. ARREST & VT/VF
            EcgRhythmData(
                titleEn = "Ventricular Tachycardia (Pulseless VT)",
                titleBn = "ভেন্ট্রিকুলার ট্যাকিকার্ডিয়া (VT)",
                category = "ARREST",
                rate = "150 - 250 bpm",
                pWaveEn = "Absent or obscured",
                pWaveBn = "অনুপস্থিত",
                prInterval = "None",
                qrsDuration = "Wide and Bizarre (> 0.12 sec)",
                significanceEn = "Life-threatening rhythm. High risk of converting to V-Fib.",
                significanceBn = "জীবনঘাতী ভয়ানক রিদম। দ্রুত পদক্ষেপ না নিলে কার্ডিয়াক এরেস্টে পরিণত হয়।",
                actionEn = "CHECK PULSE. If Pulseless: START CPR & DEFIBRILLATE. If pulse present: Amiodarone 150mg IV.",
                actionBn = "তাত্ক্ষণিক পালস চেক করুন! পালস না থাকলে: সিপিআর (CPR) শুরু করুন এবং ডিফাইব্রিলেটর (Shock) দিন।",
                isEmergency = true
            ),
            EcgRhythmData(
                titleEn = "Ventricular Fibrillation (VFib)",
                titleBn = "ভেন্ট্রিকুলার ফাইব্রিলেশন (VFib - কার্ডিয়াক এরেস্ট)",
                category = "ARREST",
                rate = "Not measurable (Chaotic)",
                pWaveEn = "None",
                pWaveBn = "নেই",
                prInterval = "None",
                qrsDuration = "Chaotic squiggly line",
                significanceEn = "CARDIAC ARREST. No effective cardiac output. Clinical Death.",
                significanceBn = "কার্ডিয়াক এরেস্ট! হূৎপিণ্ডে কোনো কার্যকর পাম্পিং নেই। রোগী শারীরিকভাবে মৃতপ্রায়।",
                actionEn = "IMMEDIATE CPR & UNSYNCHRONIZED SHOCK (DEFIBRILLATION 200J). Give Epinephrine 1mg q3-5m. Code Blue!",
                actionBn = "জরুরি Code Blue! অবিলম্বে CPR শুরু করুন এবং ২০০ জুলে ডিফাইব্রিলেটর শট দিন। এপিনেফ্রিন ১ মিগ্রা দিন।",
                isEmergency = true
            ),

            // 5. ECG 5-STEP RULES
            EcgRhythmData(
                titleEn = "Nurse 5-Step ECG Interpretation Rule",
                titleBn = "নার্সদের জন্য ইসিজি রিডিং ৫-স্টেপ মাস্টার রুল",
                category = "RULE",
                rate = "Step 1: Calculate Rate (300 ÷ Big Boxes or Count QRS in 6 sec × 10)",
                pWaveEn = "Step 2: Check Rhythm (Regular vs Irregular)",
                pWaveBn = "স্টেপ ২: রিদম চেক করুন (নিয়মিত নাকি অনিয়মিত)",
                prInterval = "Step 3: P-Wave (Present & Upright in Lead II)",
                qrsDuration = "Step 4: PR Interval (0.12 - 0.20 sec / 3-5 small boxes)",
                significanceEn = "Step 5: QRS Duration (< 0.10 sec / < 2.5 small boxes). Check ST segment for elevation/depression.",
                significanceBn = "স্টেপ ৫: QRS সময়কাল (< ০.১০ সেকেন্ড)। ST সেগমেন্ট উপরে বা নিচে নেমেছে কিনা পরীক্ষা করুন।",
                actionEn = "Always compare with patient's baseline ECG and clinical symptoms (Chest pain, SOB, Dizziness).",
                actionBn = "সবসময় রোগীর পূর্বের ইসিজি এবং শারীরিক লক্ষণের (বুকে ব্যথা, শ্বাসকষ্ট, মাথা ঘোরা) সাথে মিলিয়ে দেখুন।",
                isEmergency = false
            )
        )
    }

    val filteredRhythms = remember(selectedCategory, rhythms) {
        if (selectedCategory == "ALL") rhythms else rhythms.filter { it.category == selectedCategory }
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
                                text = if (isBn) "ইসিজি গাইড ও ইন্টারপ্রিটেশন" else "ECG Interpretation Guide",
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
                            text = if (isBn) "🌐 আন্তর্জাতিক কার্ডিওভাসকুলার নার্সিং গাইডলাইন (AHA / ACC)" else "🌐 International Cardiovascular Nursing Guide (AHA / ACC)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    // Single Line Scrollable 3D Category Tabs
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(categoryTabs) { _, (code, label) ->
                            val isSelected = selectedCategory == code
                            val chipBg = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.88f))
                            val textColor = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155))

                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedCategory = code
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = chipBg,
                                border = BorderStroke(1.dp, if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFCBD5E1))),
                                modifier = Modifier.shadow(if (isSelected) 6.dp else 1.dp, RoundedCornerShape(16.dp))
                            ) {
                                Text(
                                    text = label,
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredRhythms) { rhythm ->
                        EcgRhythmCard(rhythm, isBn)
                    }

                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            }
        }
    }
}

@Composable
fun EcgRhythmCard(rhythm: EcgRhythmData, isBn: Boolean) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var expanded by remember { mutableStateOf(false) }

    val cardColor = if (rhythm.isEmergency) (if (isDark) Color(0xFF451A03) else Color(0xFFFFEBEE).copy(alpha = 0.7f)) else (if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.35f))
    val borderColor = if (rhythm.isEmergency) (if (isDark) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.5f)) else (if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.6f))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                expanded = !expanded
            },
        shape = RoundedCornerShape(22.dp),
        color = cardColor,
        border = BorderStroke(1.2.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    if (rhythm.isEmergency) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        text = if (isBn) rhythm.titleBn else rhythm.titleEn,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = if (rhythm.isEmergency) (if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B)) else (if (isDark) Color.White else Color(0xFF0F172A)),
                        lineHeight = 20.sp
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isDark) Color.White else Color(0xFF0F172A)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFF0F172A).copy(alpha = 0.15f))
                    Spacer(Modifier.height(12.dp))

                    EcgDetailRow(if (isBn) "হৃদস্পন্দন রেট:" else "Heart Rate:", rhythm.rate)
                    EcgDetailRow(if (isBn) "পি-ওয়েভ (P-Wave):" else "P-Wave:", if (isBn) rhythm.pWaveBn else rhythm.pWaveEn)
                    EcgDetailRow(if (isBn) "PR ইন্টারভাল:" else "PR Interval:", rhythm.prInterval)
                    EcgDetailRow(if (isBn) "QRS ডিউরেশন:" else "QRS Duration:", rhythm.qrsDuration)

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = if (isBn) "ক্লিনিক্যাল গুরুত্ব:" else "Clinical Significance:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF334155)
                    )
                    Text(
                        text = if (isBn) rhythm.significanceBn else rhythm.significanceEn,
                        fontSize = 13.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A),
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (rhythm.isEmergency) (if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.4f) else Color(0xFFDC2626).copy(alpha = 0.12f)) else (if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF0F172A).copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isBn) "জরুরি নার্সিং পদক্ষেপ (Nursing Action):" else "Emergency Nursing Action:",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = if (rhythm.isEmergency) (if (isDark) Color(0xFFFCA5A5) else Color(0xFF991B1B)) else (if (isDark) Color(0xFF38BDF8) else Color(0xFF0F172A))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isBn) rhythm.actionBn else rhythm.actionEn,
                                fontSize = 13.sp,
                                color = if (isDark) Color.White else Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EcgDetailRow(label: String, value: String) {
    val isDark = isSystemInDarkTheme()
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF334155), modifier = Modifier.width(130.dp))
        Text(value, fontWeight = FontWeight.Black, fontSize = 12.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
    }
}
