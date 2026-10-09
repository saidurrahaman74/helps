package com.srcreation.smarthelp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.material.icons.filled.*
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

class NurseDrugDilutionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                DrugDilutionScreen(onBack = { finish() })
            }
        }
    }
}

data class DilutionData(
    val nameEn: String,
    val nameBn: String,
    val category: String, // ALL, HIGH_ALERT, ANTIBIOTIC, ICU, EMERGENCY
    val reconstitutionEn: String,
    val reconstitutionBn: String,
    val furtherDilutionEn: String,
    val furtherDilutionBn: String,
    val compatibleDiluentsEn: String,
    val compatibleDiluentsBn: String,
    val administrationRateEn: String,
    val administrationRateBn: String,
    val nursingAlertEn: String = "",
    val nursingAlertBn: String = "",
    val isHighAlert: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrugDilutionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showUniversalRules by remember { mutableStateOf(false) }

    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }

    val categories = remember(isBn) {
        if (isBn) listOf(
            "ALL" to "সব ড্রাগ (All)",
            "HIGH_ALERT" to "হাই-অ্যালার্ট ড্রাগস 🚨",
            "ANTIBIOTIC" to "এন্টিবায়োটিক (Antibiotics) 💊",
            "ICU" to "আইসিইউ ও ইনোোট্রপস 🏥",
            "EMERGENCY" to "জরুরি ইনজেকশন 💉"
        ) else listOf(
            "ALL" to "All IV Meds",
            "HIGH_ALERT" to "High-Alert Meds 🚨",
            "ANTIBIOTIC" to "Antibiotics 💊",
            "ICU" to "ICU & Inotropes 🏥",
            "EMERGENCY" to "Emergency IV Push 💉"
        )
    }

    val drugList = remember {
        listOf(
            DilutionData(
                nameEn = "Potassium Chloride (KCl)",
                nameBn = "পটাশিয়াম ক্লোরাইড (KCl Injection)",
                category = "HIGH_ALERT",
                reconstitutionEn = "Concentrated liquid ampoule. NEVER give IV push!",
                reconstitutionBn = "প্রচণ্ড ঘনীভূত তরল অ্যাম্পুল। ভুলেও সরাসরি আইভি পুশ করবেন না!",
                furtherDilutionEn = "Must dilute in 250ml - 1000ml infusion bag (NS or D5W). Max conc: 40 mEq/L.",
                furtherDilutionBn = "অবশ্যই ২৫০-১০০০ মিমি স্যালাইন ব্যাগে মেশাতে হবে। সর্বোচ্চ কনসেনট্রেশন ৪০ mEq/L।",
                compatibleDiluentsEn = "Normal Saline (NS), D5W, Ringer's Lactate (LR).",
                compatibleDiluentsBn = "নরমাল স্যালাইন (NS), ৫% ডেক্সট্রোজ (D5W), রিঙ্গার্স ল্যাকটেট (LR)।",
                administrationRateEn = "Max infusion rate: 10 mEq/hr (Peripheral), 20 mEq/hr (Central line).",
                administrationRateBn = "সর্বোচ্চ হার: ১০ mEq/ঘণ্টা (পেরিফেরাল লাইন), ২০ mEq/ঘণ্টা (সেন্ট্রাল লাইন)।",
                nursingAlertEn = "FATAL IF GIVEN IV PUSH! Always use infusion pump. Monitor ECG and Extravasation.",
                nursingAlertBn = "আইভি পুশ দিলে মৃত্যুঝুঁকি! সবসময় ইনফিউশন পাম্প দিয়ে দিন। ইসিজি ও স্যালাইন লিক হচ্ছে কিনা দেখুন।",
                isHighAlert = true
            ),
            DilutionData(
                nameEn = "Amiodarone (Cordarone)",
                nameBn = "অ্যামিওডেরন (Cordarone)",
                category = "HIGH_ALERT",
                reconstitutionEn = "Liquid vial/ampoule (150mg/3ml). No reconstitution needed.",
                reconstitutionBn = "তরল অ্যাম্পুল (১৫০ মিগ্রা/৩ মিমি)। গোলাতে হয় না।",
                furtherDilutionEn = "Dilute 300mg (6ml) in 250ml D5W. ONLY USE D5W (Incompatible with NS).",
                furtherDilutionBn = "৩০০ মিগ্রা (৬ মিমি) ২৫০ মিমি D5W ডেক্সট্রোজে গুলুন। শুধু D5W ব্যবহার করুন (নরমাল স্যালাইনে গুলাবেন না)।",
                compatibleDiluentsEn = "D5W (5% Dextrose) ONLY. Incompatible with NS.",
                compatibleDiluentsBn = "শুধুমাত্র ৫% ডেক্সট্রোজ (D5W)। নরমাল স্যালাইনে ইনকম্প্যাটিবল।",
                administrationRateEn = "Arrest: 300mg IV push. Maintenance: 1mg/min for 6 hrs, then 0.5mg/min.",
                administrationRateBn = "এরেস্ট: ৩০০ মিগ্রা দ্রুত পুশ। মেইনটেন্যান্স: ১ মিগ্রা/মি (৬ ঘণ্টা), এরপর ০.৫ মিগ্রা/মি।",
                nursingAlertEn = "Use in-line filter. Central line required if infusion > 2 hours to avoid phlebitis.",
                nursingAlertBn = "ইন-লাইন ফিল্টার ব্যবহার করুন। ২ ঘণ্টার বেশি ইনফিউশন দিলে সেন্ট্রাল লাইন ব্যবহার করুন।",
                isHighAlert = true
            ),
            DilutionData(
                nameEn = "Dopamine HCl",
                nameBn = "ডোপামিন (Dopamine HCl Injection)",
                category = "ICU",
                reconstitutionEn = "Liquid ampoule 200mg/5ml.",
                reconstitutionBn = "তরল অ্যাম্পুল ২০০ মিগ্রা/৫ মিমি।",
                furtherDilutionEn = "Dilute 200mg (5ml) in 250ml D5W or NS = 800 mcg/ml concentration.",
                furtherDilutionBn = "২০০ মিগ্রা (৫ মিমি) ২৫০ মিমি D5W বা NS-এ গুলুন = ৮০০ ম্যাক্রোগ্রাম/মিমি।",
                compatibleDiluentsEn = "D5W, Normal Saline (NS), LR.",
                compatibleDiluentsBn = "D5W, নরমাল স্যালাইন (NS), রিঙ্গার্স ল্যাকটেট।",
                administrationRateEn = "Renal dose: 2-5 mcg/kg/min; Cardiac: 5-10 mcg/kg/min; Vasopressor: >10 mcg/kg/min.",
                administrationRateBn = "রেনাল ডোজ: ২-৫ ম্যাক্রোগ্রাম/কেজি/মি; কার্ডিয়াক: ৫-১০ ম্যাক্রোগ্রাম/কেজি/মি।",
                nursingAlertEn = "Extravasation causes severe ischemia. Central line preferred. Monitor BP & HR continuously.",
                nursingAlertBn = "শিরা লিক হলে টিস্যু মৃত্যু হতে পারে। সেন্ট্রাল লাইন দিয়ে দেওয়া শ্রেয়।",
                isHighAlert = true
            ),
            DilutionData(
                nameEn = "Dobutamine",
                nameBn = "ডোবুটামিন (Dobutamine Injection)",
                category = "ICU",
                reconstitutionEn = "Liquid ampoule 250mg/20ml.",
                reconstitutionBn = "তরল অ্যাম্পুল ২৫০ মিগ্রা/২০ মিমি।",
                furtherDilutionEn = "Dilute 250mg in 250ml D5W or NS = 1000 mcg/ml concentration.",
                furtherDilutionBn = "২৫০ মিগ্রা ২৫০ মিমি D5W বা NS-এ গুলুন = ১০০০ ম্যাক্রোগ্রাম/মিমি।",
                compatibleDiluentsEn = "D5W, Normal Saline (NS), LR.",
                compatibleDiluentsBn = "D5W, নরমাল স্যালাইন (NS), রিঙ্গার্স ল্যাকটেট।",
                administrationRateEn = "Continuous Infusion: 2.5 to 20 mcg/kg/min titrated to inotropic response.",
                administrationRateBn = "কন্টিনিউয়াস ইনফিউশন: ২.৫ - ২০ ম্যাক্রোগ্রাম/কেজি/মিনিট।",
                nursingAlertEn = "Do NOT mix with alkaline solutions (Sodium Bicarbonate). Monitor heart rate & arrhythmias.",
                nursingAlertBn = "সোডিয়াম বাইকার্বোনেটের মত খারি দ্রবণ দিয়ে গুলাবেন না। ইসিজি মনিটর করুন।",
                isHighAlert = true
            ),
            DilutionData(
                nameEn = "Epinephrine (Adrenaline)",
                nameBn = "এড্রেনালিন / এপিনেফ্রিন (Adrenaline)",
                category = "EMERGENCY",
                reconstitutionEn = "1mg/1ml ampoule (1:1000 concentration).",
                reconstitutionBn = "১ মিগ্রা/১ মিমি অ্যাম্পুল (১:১০০০ কনসেনট্রেশন)।",
                furtherDilutionEn = "IV Push in Arrest: 1mg diluted in 10ml NS (1:10,000 conc). Infusion: 1mg in 250ml D5W.",
                furtherDilutionBn = "কার্ডিয়াক এরেস্টের জন্য ১ মিগ্রা ১০ মিমি স্যালাইনে গুলে (১:১০,০০০) দিন। ইনফিউশনের জন্য ১ মিগ্রা ২৫০ মিমি D5W-এ গুলুন।",
                compatibleDiluentsEn = "D5W, Normal Saline (NS).",
                compatibleDiluentsBn = "D5W, নরমাল স্যালাইন (NS)।",
                administrationRateEn = "Cardiac Arrest: 1mg IV push q3-5 min. Anaphylaxis: 0.3-0.5mg IM. Infusion: 1-10 mcg/min.",
                administrationRateBn = "কার্ডিয়াক এরেস্ট: ১ মিগ্রা আইভি পুশ প্রতি ৩-৫ মিনিটে। অ্যানাফাইল্যাক্সিস: ০.৩-০.৫ মিগ্রা আইএম।",
                nursingAlertEn = "Use 1:10,000 for IV push! Never give 1:1000 directly IV.",
                nursingAlertBn = "আইভি পুশের জন্য অবশ্যই ১:১০,০০০ তৈরি করুন! ১:১০০০ সরাসরি আইভি দেবেন না।",
                isHighAlert = true
            ),
            DilutionData(
                nameEn = "Vancomycin",
                nameBn = "ভ্যানকোমাইসিন (Vancomycin)",
                category = "ANTIBIOTIC",
                reconstitutionEn = "Add 10ml Sterile Water for Injection to 500mg vial (50mg/ml).",
                reconstitutionBn = "৫০০ মিগ্রা ভায়ালে ১০ মিমি স্টেরাইল ওয়াটার যোগ করুন (৫০ মিগ্রা/মিমি)।",
                furtherDilutionEn = "Dilute 500mg in at least 100ml NS/D5W (1000mg in 250ml). Max conc: 5mg/ml.",
                furtherDilutionBn = "৫০০ মিগ্রা ১০০ মিমি স্যালাইনে এবং ১০০০ মিগ্রা ২৫০ মিমি স্যালাইনে গুলুন।",
                compatibleDiluentsEn = "Normal Saline (NS), D5W, Ringer's Lactate (LR).",
                compatibleDiluentsBn = "নরমাল স্যালাইন (NS), ৫% ডেক্সট্রোজ (D5W), রিঙ্গার্স ল্যাকটেট (LR)।",
                administrationRateEn = "Infuse over AT LEAST 60 minutes (or max 10mg/min). Do NOT rush.",
                administrationRateBn = "কমপক্ষে ৬০ মিনিট ধরে ইনফিউশন দিন (সর্বোচ্চ ১০ মিগ্রা/মিনিট)। দ্রুত দেওয়া নিষেধ।",
                nursingAlertEn = "Rapid infusion causes RED MAN SYNDROME (flushing, hypotension). Monitor trough levels.",
                nursingAlertBn = "দ্রুত দিলে রেড ম্যান সিন্ড্রোম (ত্বক লাল হওয়া ও প্রেশার নামা) হয়। ধীর গতিতে চালান।",
                isHighAlert = true
            ),
            DilutionData(
                nameEn = "Ceftriaxone (Rocephin)",
                nameBn = "সেফট্রিয়াক্সন (Ceftriaxone / Rocephin)",
                category = "ANTIBIOTIC",
                reconstitutionEn = "Add 9.6ml Sterile Water for IV (100mg/ml) or 3.5ml Lidocaine for IM.",
                reconstitutionBn = "আইভির জন্য ৯.৬ মিমি স্টেরাইল ওয়াটার যোগ করুন। আইএমের জন্য ৩.৫ মিমি লিডোকেইন।",
                furtherDilutionEn = "Dilute 1g in 50-100ml NS or D5W for intermittent infusion.",
                furtherDilutionBn = "১ গ্রাম ৫০-১ ১০০ মিমি স্যালাইনে গুলুন।",
                compatibleDiluentsEn = "NS, D5W. NEVER mix with Calcium-containing solutions (LR / Ringer's)!",
                compatibleDiluentsBn = "নরমাল স্যালাইন (NS), D5W। ক্যালসিয়ামযুক্ত স্যালাইনে (রিঙ্গার্স/LR) ভুলেও গুলাবেন না!",
                administrationRateEn = "Slow IV Bolus over 2-4 mins OR IV Infusion over 15-30 mins.",
                administrationRateBn = "ধীর আইভি বোলুস (২-৪ মিনিট) অথবা আইভি ইনফিউশন (১৫-৩০ মিনিট)।",
                nursingAlertEn = "CONTRAINDICATED with Calcium IV solutions in neonates (causes fatal lung/kidney precipitates).",
                nursingAlertBn = "ক্যালসিয়ামযুক্ত স্যালাইনের সাথে মেশালে ফুসফুস ও কিডনিতে মারাত্মক প্রিসিপিটেশন হয়!",
                isHighAlert = false
            ),
            DilutionData(
                nameEn = "Insulin Regular (Actrapid)",
                nameBn = "ইনসুলিন রেগুলার (Humulin R / Actrapid)",
                category = "HIGH_ALERT",
                reconstitutionEn = "100 Units/ml liquid vial. No reconstitution needed.",
                reconstitutionBn = "১০০ ইউনিট/মিমি ডায়রেক্ট ভায়াল। গোলাতে হয় না।",
                furtherDilutionEn = "Dilute 100 Units in 100ml Normal Saline = 1 Unit/ml ratio.",
                furtherDilutionBn = "১০০ ইউনিট ১০০ মিমি নরমাল স্যালাইনে (NS) গুলুন = ১ ইউনিট/মিমি রেশিও।",
                compatibleDiluentsEn = "Normal Saline (0.9% NaCl) ONLY.",
                compatibleDiluentsBn = "শুধুমাত্র নরমাল স্যালাইন (0.9% NaCl)।",
                administrationRateEn = "Continuous IV Infusion: 0.1 Units/kg/hr titrated to blood glucose.",
                administrationRateBn = "কন্টিনিউয়াস আইভি ইনফিউশন: ০.১ ইউনিট/কেজি/ঘণ্টা (ব্লাড সুগার দেখে এডজাস্ট করুন)।",
                nursingAlertEn = "Insulin binds to IV tubing. Flush 20ml through tubing before starting. Check glucose hourly.",
                nursingAlertBn = "ইনসুলিন পাইপের গায়ে আটকে যায়। ইনফিউশন চালুর আগে ২০ মিমি ফেলে দিন। প্রতি ঘণ্টায় সুগার মাপুন।",
                isHighAlert = true
            ),
            DilutionData(
                nameEn = "Norepinephrine (Noradrenaline)",
                nameBn = "নরএপিনেফ্রিন (Noradrenaline)",
                category = "ICU",
                reconstitutionEn = "Liquid ampoule 4mg/4ml.",
                reconstitutionBn = "তরল অ্যাম্পুল ৪ মিগ্রা/৪ মিমি।",
                furtherDilutionEn = "Dilute 4mg (4ml) in 250ml D5W or NS = 16 mcg/ml concentration.",
                furtherDilutionBn = "৪ মিগ্রা (৪ মিমি) ২৫০ মিমি D5W বা NS-এ গুলুন = ১৬ ম্যাক্রোগ্রাম/মিমি কনসেনট্রেশন।",
                compatibleDiluentsEn = "D5W or Normal Saline (NS).",
                compatibleDiluentsBn = "D5W অথবা নরমাল স্যালাইন (NS)।",
                administrationRateEn = "Continuous Infusion: 2 to 12 mcg/min titrated to Mean Arterial Pressure (MAP > 65).",
                administrationRateBn = "কন্টিনিউয়াস ইনফিউশন: ২-১২ ম্যাক্রোগ্রাম/মিনিট (MAP > ৬৫ রাখার জন্য টাইট্রেট করুন)।",
                nursingAlertEn = "CENTRAL LINE ONLY! Severe tissue necrosis if extravasated in peripheral vein.",
                nursingAlertBn = "অবশ্যই সেন্ট্রাল লাইন দিয়ে দিন! পেরিফেরাল শিরায় লিক হলে টিস্যু পচে মারাত্মক ক্ষতি হবে।",
                isHighAlert = true
            ),
            DilutionData(
                nameEn = "Calcium Gluconate 10%",
                nameBn = "ক্যালসিয়াম গ্লুকোনেট ১০% (Calcium Gluconate)",
                category = "EMERGENCY",
                reconstitutionEn = "10% liquid ampoule (1g in 10ml).",
                reconstitutionBn = "১০% তরল অ্যাম্পুল (১০ মিমি এ ১ গ্রাম)।",
                furtherDilutionEn = "Dilute 1g (10ml) in 50-100ml NS or D5W.",
                furtherDilutionBn = "১ গ্রাম (১০ মিমি) ৫০-১০০ মিমি নরমাল স্যালাইন বা D5W-এ গুলুন।",
                compatibleDiluentsEn = "NS, D5W. Incompatible with Bicarbonate or Ceftriaxone.",
                compatibleDiluentsBn = "নরমাল স্যালাইন (NS), D5W। বাইকার্বোনেট বা সেফট্রিয়াক্সনের সাথে অসংগতিপূর্ণ।",
                administrationRateEn = "IV Push in Emergency: Slow over 10 minutes (max 1.5ml/min). Never rush.",
                administrationRateBn = "জরুরি অবস্থায় ১০ মিনিট ধরে ধীরগতিতে পুষ্প দিন।",
                nursingAlertEn = "Rapid IV push causes severe bradycardia, cardiac arrest, and hypotension.",
                nursingAlertBn = "দ্রুত আইভি দিলে প্রেশার ধপ করে নেমে যেতে পারে এবং হৃদস্পন্দন বন্ধ হতে পারে।",
                isHighAlert = true
            ),
            DilutionData(
                nameEn = "Nitroglycerin (NTG)",
                nameBn = "নাইট্রোগ্লিসারিন (Nitroglycerin / NTG)",
                category = "ICU",
                reconstitutionEn = "Liquid ampoule 50mg/10ml or 5mg/5ml.",
                reconstitutionBn = "তরল অ্যাম্পুল ৫০ মিগ্রা/১০ মিমি বা ৫ মিগ্রা/৫ মিমি।",
                furtherDilutionEn = "Dilute 50mg in 250ml D5W or NS = 200 mcg/ml concentration.",
                furtherDilutionBn = "৫০ মিগ্রা ২৫০ মিমি D5W বা NS-এ গুলুন = ২০০ ম্যাক্রোগ্রাম/মিমি।",
                compatibleDiluentsEn = "D5W or Normal Saline (NS) in GLASS or non-PVC tubing.",
                compatibleDiluentsBn = "D5W বা নরমাল স্যালাইন (গ্লাস বা বিশেষ নন-পিভিসি ইনফিউশন সেট)।",
                administrationRateEn = "Start 5-10 mcg/min, titrate by 5 mcg/min q3-5 mins until angina relief or target BP.",
                administrationRateBn = "৫-১০ ম্যাক্রোগ্রাম/মিনিট দিয়ে শুরু করুন এবং প্রতি ৩-৫ মিনিটে বাড়ান।",
                nursingAlertEn = "Binds to standard PVC IV tubing. Use non-PVC glass sets. Monitor BP closely for hypotension.",
                nursingAlertBn = "প্লাস্টিকের পাইপে নাইট্রোগ্লিসারিন শোষিত হয়। বিশেষ নন-পিভিসি ইনফিউশন সেট ব্যবহার করুন।",
                isHighAlert = true
            ),
            DilutionData(
                nameEn = "Midazolam",
                nameBn = "মিডাজোলাম (Midazolam Injection)",
                category = "EMERGENCY",
                reconstitutionEn = "Liquid ampoule 5mg/5ml or 15mg/3ml.",
                reconstitutionBn = "তরল অ্যাম্পুল ৫ মিগ্রা/৫ মিমি বা ১৫ মিগ্রা/৩ মিমি।",
                furtherDilutionEn = "For IV Infusion: Dilute 50mg in 50ml NS = 1mg/ml concentration.",
                furtherDilutionBn = "ইনফিউশনের জন্য: ৫০ মিগ্রা ৫০ মিমি স্যালাইনে গুলুন = ১ মিগ্রা/মিমি।",
                compatibleDiluentsEn = "NS, D5W, LR.",
                compatibleDiluentsBn = "নরমাল স্যালাইন (NS), D5W, রিঙ্গার্স ল্যাকটেট।",
                administrationRateEn = "IV Push: Slow over 2 minutes. Sedation Infusion: 0.02-0.1 mg/kg/hr.",
                administrationRateBn = "ধীর আইভি পুশ ২ মিনিট ধরে। সেডেশন ইনফিউশন: ০.০২-০.১ মিগ্রা/কেজি/ঘণ্টা।",
                nursingAlertEn = "Monitor respiratory depression and airway patency constantly. Antidote: Flumazenil.",
                nursingAlertBn = "শ্বাসযন্ত্রের পতন ও শ্বাসনালী পরীক্ষা করুন। প্রতিষেধক: ফ্লুমাজেনিল (Flumazenil)।",
                isHighAlert = true
            ),
            DilutionData(
                nameEn = "Magnesium Sulfate",
                nameBn = "ম্যাগনেসিয়াম সালফেট (Magnesium Sulfate)",
                category = "HIGH_ALERT",
                reconstitutionEn = "Liquid ampoule 50% (500mg/ml).",
                reconstitutionBn = "তরল অ্যাম্পুল ৫০% (৫০০ মিগ্রা/মিমি)।",
                furtherDilutionEn = "Eclampsia Bolus: Dilute 4g in 100ml NS. Infusion: 1-2g/hr in 500ml NS.",
                furtherDilutionBn = "এক্লাম্পসিয়া বোলুস: ৪ গ্রাম ১০০ মিমি নরমাল স্যালাইনে গুলুন। মেইনটেন্যান্স: ১-২ গ্রাম/ঘণ্টা।",
                compatibleDiluentsEn = "NS, D5W.",
                compatibleDiluentsBn = "নরমাল স্যালাইন (NS), D5W।",
                administrationRateEn = "Loading Bolus over 15-20 mins. Maintenance Infusion: 1-2 g/hr.",
                administrationRateBn = "লোডিং বোলুস ১৫-২০ মিনিট ধরে। মেইনটেন্যান্স ইনফিউশন ১-২ গ্রাম/ঘণ্টা।",
                nursingAlertEn = "Monitor Patellar Reflex, Respiratory Rate (> 16/min), and Urine Output (> 30ml/hr). Antidote: Calcium Gluconate.",
                nursingAlertBn = "প্যাটেলার রিফ্লেক্স, শ্বাসপ্রশ্বাস (> ১৬) ও প্রস্রাব পরীক্ষা করুন। টক্সিসিটির প্রতিষেধক: ক্যালসিয়াম গ্লুকোনেট।",
                isHighAlert = true
            )
        )
    }

    val filteredList = remember(searchQuery, selectedCategory, drugList) {
        drugList.filter { drug ->
            val matchesCategory = (selectedCategory == "ALL") || (drug.category == selectedCategory)
            val matchesSearch = searchQuery.isBlank() ||
                    drug.nameEn.contains(searchQuery, ignoreCase = true) ||
                    drug.nameBn.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
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
                                text = if (isBn) "ড্রাগ ডিলিউশন ও রিকনস্টিটিউশন গাইড" else "Drug Dilution Master Guide",
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

                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = if (isDark) 0.25f else 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = if (isDark) 0.5f else 0.3f)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = if (isBn) "🌐 আন্তর্জাতিক আইভি ডিলিউশন ও প্রোটোকল (ISMP / WHO Standard)" else "🌐 International IV Drug Dilution Protocols (ISMP / WHO)",
                            fontSize = if (isDark) 10.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(categories) { _, (code, label) ->
                            val isSelected = selectedCategory == code
                            val chipBg = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.35f))
                            val textColor = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A))

                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedCategory = code
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = chipBg,
                                border = BorderStroke(1.dp, if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.6f))),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                    Text(text = label, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
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
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text(if (isBn) "আইভি ড্রাগ খুঁজুন (যেমন: KCl, Amiodarone, Dopamine, Vancomycin)..." else "Search IV Drug (e.g. KCl, Amiodarone, Dopamine)...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF0284C7)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = null) }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray
                            )
                        )
                    }

                    if (filteredList.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.6f)
                            ) {
                                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isBn) "কোনো ড্রাগ পাওয়া যায়নি। অন্য কিওয়ার্ড সার্চ করুন।" else "No matching IV drugs found.",
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredList) { drug ->
                            DilutionCard(drug = drug, isDark = isDark, isBn = isBn)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
fun DilutionCard(drug: DilutionData, isDark: Boolean, isBn: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
    val strokeColor = if (drug.isHighAlert) Color(0xFFEF4444).copy(alpha = 0.5f) else (if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF0284C7).copy(alpha = 0.2f))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (isDark) 0.dp else 2.dp, shape = RoundedCornerShape(20.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                expanded = !expanded
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.2.dp, strokeColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (drug.isHighAlert) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF0284C7).copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (drug.isHighAlert) Icons.Default.Warning else Icons.Default.Bloodtype,
                                contentDescription = null,
                                tint = if (drug.isHighAlert) Color(0xFFEF4444) else Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isBn) drug.nameBn else drug.nameEn,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = titleColor
                        )
                        if (drug.isHighAlert) {
                            Text(
                                text = if (isBn) "🚨 হাই-অ্যালার্ট আইভি ড্রাগ" else "🚨 High-Alert Medication",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            // Summary Section
            Text(
                text = if (isBn) "💧 ডিলিউশন (Further Dilution):" else "💧 Dilution Protocol:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0284C7)
            )
            Text(
                text = if (isBn) drug.furtherDilutionBn else drug.furtherDilutionEn,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray,
                lineHeight = 16.sp
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.3f))

                    Text(
                        text = if (isBn) "🧪 প্রস্তুতকরণ (Reconstitution):" else "🧪 Reconstitution:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA855F7)
                    )
                    Text(
                        text = if (isBn) drug.reconstitutionBn else drug.reconstitutionEn,
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray
                    )

                    Text(
                        text = if (isBn) "✅ সুসংগত স্যালাইন (Compatible Diluents):" else "✅ Compatible Solutions:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF22C55E)
                    )
                    Text(
                        text = if (isBn) drug.compatibleDiluentsBn else drug.compatibleDiluentsEn,
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray
                    )

                    Text(
                        text = if (isBn) "⏱️ ইনফিউশন স্পিড / হার (Administration Rate):" else "⏱️ Administration Rate:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                    Text(
                        text = if (isBn) drug.administrationRateBn else drug.administrationRateEn,
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray
                    )

                    if (drug.nursingAlertBn.isNotEmpty() || drug.nursingAlertEn.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isBn) "⚠️ নার্সিং অ্যালার্ট (Nursing Safety Alert):" else "⚠️ Nursing Safety Alert:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFEF4444)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isBn) drug.nursingAlertBn else drug.nursingAlertEn,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else Color(0xFF991B1B),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
