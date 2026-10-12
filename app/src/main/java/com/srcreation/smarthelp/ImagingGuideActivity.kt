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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.util.Locale

class ImagingGuideActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                ImagingGuideScreen(onBack = { finish() })
            }
        }
    }
}

data class ImagingGuideData(
    val titleEn: String,
    val titleBn: String,
    val category: String, // NEURO, CHEST, ABD, OBGYN, CARDIO, MSK, NUCLEAR
    val purposeEn: String,
    val purposeBn: String,
    val prepEn: String,
    val prepBn: String,
    val procedureEn: String,
    val procedureBn: String,
    val safetyEn: String,
    val safetyBn: String,
    val isContrast: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagingGuideScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var isBangla by remember { mutableStateOf(Locale.getDefault().language == "bn") }

    var selectedTab by remember { mutableStateOf("ALL") }

    val tabs = remember(isBangla) {
        if (isBangla) listOf(
            "ALL" to "সব পরীক্ষা (All)",
            "NEURO" to "নিউরো / মস্তিষ্ক (Neurology) 🧠",
            "CHEST" to "বুক ও ফুসফুস (Chest) 🫁",
            "ABD" to "পেট ও পরিপাক (Abdomen & GI) 🩺",
            "OBGYN" to "প্রসূতি ও স্ত্রী রোগ (OB-GYN) 👶",
            "CARDIO" to "হৃদপিণ্ড ও রক্তনালী (Cardio) 🫀",
            "MSK" to "হাড় ও জোড় (Orthopedics) 🦴",
            "NUCLEAR" to "নিউক্লিয়ার ও পেট (Nuclear) ⚛️"
        ) else listOf(
            "ALL" to "All Tests",
            "NEURO" to "Neurology 🧠",
            "CHEST" to "Chest & Lungs 🫁",
            "ABD" to "Abdomen & GI 🩺",
            "OBGYN" to "OB-GYN & Pelvic 👶",
            "CARDIO" to "Cardio & Vascular 🫀",
            "MSK" to "Orthopedics & MSK 🦴",
            "NUCLEAR" to "Nuclear & PET ⚛️"
        )
    }

    val guides = remember {
        listOf(
            // 1. NEUROLOGY
            ImagingGuideData(
                titleEn = "MRI Brain (With & Without Contrast)",
                titleBn = "এমআরআই ব্রেন (কনট্রাস্ট সহ ও ছাড়া)",
                category = "NEURO",
                purposeEn = "Golden standard for soft-tissue evaluation of the brain. Detects acute ischemic stroke, multiple sclerosis, brain tumors, hemorrhage, and aneurysms.",
                purposeBn = "মস্তিষ্কের নরম টিস্যু মূল্যায়নের জন্য গোল্ড স্ট্যান্ডার্ড। তীব্র ইস্কেমিক স্ট্রোক, মাল্টিপল স্ক্লেরোসিস, ব্রেন টিউমার, রক্তক্ষরণ এবং অ্যানিউরিজম শনাক্ত করে।",
                prepEn = "Remove all metallic objects, hearing aids, and jewelry. Inform staff of any metallic implants or claustrophobia.",
                prepBn = "সমস্ত ধাতব বস্তু, হিয়ারিং এইড এবং গহনা খুলে ফেলুন। কোনো মেটালিক ইমপ্লান্ট বা ক্লাউস্টোফোবিয়া থাকলে টেকনিশিয়ানকে জানান।",
                isContrast = true,
                procedureEn = "Patient lies supine with head secured in a coil. Scan takes 20-45 minutes with loud tapping sounds; earplugs provided.",
                procedureBn = "রোগী শুয়ে থাকেন এবং মাথা কয়েলে স্থির রাখা হয়। স্ক্যানে ২০-৪৫ মিনিট সময় লাগে এবং জোরে শব্দ হতে পারে, তাই ইয়ারপ্লাগ দেওয়া হয়।",
                safetyEn = "Absolute contraindication: Cardiac pacemakers, cochlear implants, intraocular metal. Contrast uses Gadolinium (safer than iodine).",
                safetyBn = "পরম নিষেধ: কার্ডিয়াক পেসমেকার, কক্লিয়ার ইমপ্লান্ট, চোখে ধাতব কণা। কন্ট্রাস্টে গ্যাডোলিনিয়াম ব্যবহার করা হয় (আয়োডিনের চেয়ে নিরাপদ)।"
            ),
            ImagingGuideData(
                titleEn = "CT Head Non-Contrast (NCCT)",
                titleBn = "সিটি হেড নন-কনট্রাস্ট (মাথার সিটি স্ক্যান)",
                category = "NEURO",
                purposeEn = "Rapid imaging for acute neurological emergencies. First-line test for acute head trauma, skull fractures, and acute intracranial hemorrhage (stroke).",
                purposeBn = "তীব্র স্নায়বিক জরুরি অবস্থার জন্য দ্রুত ইমেজিং। তীব্র মাথার আঘাত, মাথার খুলির ফাটল এবং ইন্ট্রাক্রানিয়াল রক্তক্ষরণের (স্ট্রোক) প্রথম সারির পরীক্ষা।",
                prepEn = "Remove hairpins, earrings, and metal objects around the head/neck.",
                prepBn = "হেয়ারপিন, কানের দুল এবং মাথা/গলার আশপাশের ধাতব বস্তু খুলে ফেলুন।",
                isContrast = false,
                procedureEn = "Patient lies on table; head is positioned inside the CT gantry ring. Scan takes less than 2 minutes.",
                procedureBn = "রোগী টেবিলে শোন এবং মাথা সিটি রিংয়ের ভেতরে স্থাপন করা হয়। স্ক্যান করতে ২ মিনিটেরও কম সময় লাগে।",
                safetyEn = "Low radiation dose. Safe for acute trauma. Inform staff if pregnant.",
                safetyBn = "কম রেডিয়েশন ডোজ। তীব্র আঘাতের জন্য নিরাপদ। গর্ভবতী হলে টেকনিশিয়ানকে জানান।"
            ),
            ImagingGuideData(
                titleEn = "CT Angiography (CTA) Brain & Neck",
                titleBn = "সিটি এনজিওগ্রাফি (সিটএ ব্রেন ও নেক)",
                category = "NEURO",
                purposeEn = "Visualizes cerebral arteries and carotid vessels. Diagnoses intracranial aneurysms, arteriovenous malformations (AVM), and arterial stenosis.",
                purposeBn = "মস্তিষ্কের ধমনী এবং ক্যারোটিড রক্তনালীগুলো দৃশ্যমান করে। ইন্ট্রাক্রানিয়াল অ্যানিউরিজম, এভিএম (AVM) এবং ধমনীর সংকীর্ণতা নির্ণয় করে।",
                prepEn = "Fasting for 4 hours prior. Serum creatinine test required to evaluate kidney function for iodinated contrast.",
                prepBn = "পরীক্ষার আগে ৪ ঘণ্টা উপবাস। আয়োডিনযুক্ত কন্ট্রাস্টের জন্য কিডনির কার্যকারিতা সিরাম ক্রিয়েটিনিন টেস্টের মাধ্যমে যাচাই করতে হবে।",
                isContrast = true,
                procedureEn = "IV iodinated contrast is rapidly injected via power injector timed precisely with arterial phase scanning.",
                procedureBn = "ধমনী পর্যায়ের স্ক্যানের সাথে মিল রেখে পাওয়ার ইনজেক্টরের মাধ্যমে দ্রুত আইভি আয়োডিন কন্ট্রাস্ট ইনজেক্ট করা হয়।",
                safetyEn = "Risk of contrast-induced nephropathy in renal impairment. Check eGFR and iodine allergy.",
                safetyBn = "কিডনি সমস্যায় কন্ট্রাস্ট-প্ররোচিত নেফ্রোপ্যাথির ঝুঁকি রয়েছে। eGFR এবং আয়োডিন অ্যালার্জি পরীক্ষা করুন।"
            ),

            // 2. CHEST & LUNGS
            ImagingGuideData(
                titleEn = "Chest X-Ray (CXR PA View)",
                titleBn = "চেস্ট এক্স-রে (বুকের এক্স-রে পিএ ভিউ)",
                category = "CHEST",
                purposeEn = "Evaluates lung parenchyma, pleura, mediastinum, and cardiac silhouette. Essential for pneumonia, heart failure, TB, and pneumothorax.",
                purposeBn = "ফুসফুস, প্লুরা, mediastinum এবং হার্টের আকার মূল্যায়ন করে। নিউমোনিয়া, হার্ট ফেইলিউর, যক্ষ্মা (টিবি) এবং নিউমোথোরাক্স নির্ণয়ে অপরিহার্য।",
                prepEn = "Remove all jewelry, metal necklaces, and clothing from the waist up.",
                prepBn = "সমস্ত গহনা, ধাতব নেকলেস এবং কোমর থেকে ওপরের পোশাক খুলে ফেলুন।",
                isContrast = false,
                procedureEn = "Patient stands against the film plate, takes a deep breath in, and holds it while the exposure is taken.",
                procedureBn = "রোগী ফিল্ম প্লেটের সামনে দাঁড়িয়ে গভীর শ্বাস নিয়ে তা ধরে রাখেন এবং এক্স-রে এক্সপোজার নেওয়া হয়।",
                safetyEn = "Extremely low radiation dose. Lead shielding required for lower abdomen.",
                safetyBn = "খুব কম রেডিয়েশন ডোজ। তলপেটের জন্য লেড শিল্ড ব্যবহার করা হয়।"
            ),
            ImagingGuideData(
                titleEn = "High-Resolution CT Chest (HRCT)",
                titleBn = "হাই-রেজোলিউশন সিটি চেস্ট (HRCT)",
                category = "CHEST",
                purposeEn = "Detailed evaluation of lung interstitium, airways, and parenchyma. Gold standard for interstitial lung disease (ILD), bronchiectasis, and COVID-19 pneumonia.",
                purposeBn = "ফুসফুসের ইন্টারেস্টিয়াম এবং এয়ারওয়েজের বিশদ মূল্যায়ন। ইন্টারস্টিশিয়াল লাং ডিজিজ (ILD), ব্রংকিয়েক্টাসিস এবং কোভিড-১৯ নিউমোনিয়ার গোল্ড স্ট্যান্ডার্ড।",
                prepEn = "No special dietary restrictions. Remove all metallic items from the chest region.",
                prepBn = "বিশেষ কোনো খাদ্যাভ্যাস নিষেধাজ্ঞা নেই। বুকের অঞ্চলের সমস্ত ধাতব বস্তু অপসারণ করুন।",
                isContrast = false,
                procedureEn = "Thin-slice volumetric CT acquisition of the chest performed during breath-hold.",
                procedureBn = "শ্বাস আটকে রাখার সময় বুকের পাতলা স্লাইসের ভলিউমেট্রিক সিটি স্ক্যান করা হয়।",
                safetyEn = "Higher radiation than standard X-ray. Use low-dose protocols where applicable.",
                safetyBn = "সাধারণ এক্স-রে এর চেয়ে বেশি রেডিয়েশন। প্রয়োজনে লো-ডোজ প্রোটোকল ব্যবহার করা হয়।"
            ),

            // 3. ABDOMEN & GI
            ImagingGuideData(
                titleEn = "CT Abdomen & Pelvis (With IV & Oral Contrast)",
                titleBn = "সিটি অ্যাবডোমেন ও পেলভিস (আইভি ও ওরাল কন্ট্রাস্ট সহ)",
                category = "ABD",
                purposeEn = "Comprehensive evaluation of solid organs (liver, pancreas, kidneys, spleen), bowel loops, appendix, and peritoneal cavity.",
                purposeBn = "যকৃৎ, অগ্ন্যাশয়, কিডনি, প্লীহা, অন্ত্রের লুপ, অ্যাপেন্ডিক্স এবং পেরিটোনিয়াল গহ্বরের ব্যাপক মূল্যায়ন।",
                prepEn = "NPO for 4-6 hours. Drink oral contrast solution 1-2 hours prior to scan as instructed.",
                prepBn = "৪-৬ ঘণ্টা উপবাস থাকতে হবে। নির্দেশনা অনুযায়ী স্ক্যানের ১-২ ঘণ্টা আগে ওরাল কন্ট্রাস্ট সলিউশন পান করতে হবে।",
                isContrast = true,
                procedureEn = "Oral contrast opacifies bowel loops; IV contrast highlights vascular structures and organ perfusion during scanning.",
                procedureBn = "ওরাল কন্ট্রাস্ট অন্ত্র পরিষ্কারভাবে দেখায় এবং আইভি কন্ট্রাস্ট স্ক্যানের সময় রক্তনালী ও অঙ্গের রক্তসঞ্চালন ফুটিয়ে তোলে।",
                safetyEn = "Check renal function (creatinine) and iodine allergy before IV contrast administration.",
                safetyBn = "আইভি কন্ট্রাস্ট দেওয়ার আগে কিডনির কার্যকারিতা (ক্রিয়েটিনিন) এবং আয়োডিন অ্যালার্জি পরীক্ষা করুন।"
            ),
            ImagingGuideData(
                titleEn = "Abdominal Ultrasound (Whole Abdomen)",
                titleBn = "সম্পূর্ণ পেটের আল্ট্রাসাউন্ড (হোল এবডোমেন ইউএসজি)",
                category = "ABD",
                purposeEn = "Assesses liver steatosis/cirrhosis, gallstones, renal calculi, hydronephrosis, splenomegaly, and abdominal aorta.",
                purposeBn = "লিভার ফ্যাটি/সিরোসিস, পিত্তথলির পাথর, কিডনিতে পাথর, হাইড্রোনেফ্রোসিস, প্লীহা বড় হওয়া এবং পেটের মহাধমনী মূল্যায়ন করে।",
                prepEn = "Fasting for 6-8 hours to reduce bowel gas. Full bladder required for lower abdomen/pelvic evaluation.",
                prepBn = "পেটের গ্যাস কমাতে ৬-৮ ঘণ্টা খালি পেটে থাকতে হবে। পেলভিক মূল্যায়নের জন্য ভরা মূত্রথলি (পানি পান করা) প্রয়োজন।",
                isContrast = false,
                procedureEn = "Acoustic gel applied; transducer swept across abdominal quadrants in multiple planes.",
                procedureBn = "জেলের মাধ্যমে বিভিন্ন প্লেনে পেটের চারপাশের কোণায় ট্রান্সডুসার ঘোরানো হয়।",
                safetyEn = "Non-invasive, no ionizing radiation, completely safe.",
                safetyBn = "নন-ইনভেসিভ, কোনো আয়নাইজিং রেডিয়েশন নেই, সম্পূর্ণ নিরাপদ।"
            ),
            ImagingGuideData(
                titleEn = "MRCP (MR Cholangiopancreatography)",
                titleBn = "এমআরসিপি (পিত্তনালী ও অগ্ন্যাশয় নালীর এমআরআই)",
                category = "ABD",
                purposeEn = "Non-invasive imaging of the biliary tree and pancreatic ducts. Detects choledocholithiasis, biliary strictures, and pancreatic divisum.",
                purposeBn = "পিত্তনালী এবং অগ্ন্যাশয়ের নালীর নন-ইনভেসিভ ইমেজিং। পিত্তথলির পাথর, পিত্তনালীর সংকীর্ণতা এবং প্যানক্রিয়াটিক সমস্যা নির্ণয় করে।",
                prepEn = "Fasting for 4-6 hours prior to the scan for optimal gallbladder distension.",
                prepBn = "পিত্তথলি স্পষ্টভাবে দেখার জন্য পরীক্ষার আগে ৪-৬ ঘণ্টা খালি পেটে থাকতে হবে।",
                isContrast = false,
                procedureEn = "Special heavy T2-weighted MRI sequences highlight static fluid in biliary and pancreatic ducts.",
                procedureBn = "বিশেষ হেভি T2-ওয়েইট এমআরআই সিকোয়েন্সের মাধ্যমে পিত্ত ও প্যানক্রিয়াটিক নালীর ফ্লুইড স্পষ্ট ফুটিয়ে তোলা হয়।",
                safetyEn = "Standard MRI safety checklist applies (no pacemakers or ferromagnetic metal).",
                safetyBn = "স্ট্যান্ডার্ড এমআরআই নিরাপত্তা নির্দেশিকা প্রযোজ্য (পেসমেকার বা ফেরোম্যাগনেটিক ধাতু থাকা যাবে না)।"
            ),

            // 4. OB-GYN & PELVIS
            ImagingGuideData(
                titleEn = "First Trimester Dating & NT Scan",
                titleBn = "প্রথম ত্রৈমাসিক ডেটিং ও এনটি স্ক্যান (আল্ট্রাসাউন্ড)",
                category = "OBGYN",
                purposeEn = "Confirms intrauterine pregnancy, gestational age, embryonic heartbeat, and measures nuchal translucency (NT) for chromosomal anomalies.",
                purposeBn = "গর্ভকালীন বয়স, ভ্রূণের হৃদস্পন্দন এবং ডাউন সিনড্রোমসহ ক্রোমোজোমগত ত্রুটির ঝুঁকি নির্ণয়ে নিউচাল ট্রান্সলুসেন্সি (NT) পরিমাপ করে।",
                prepEn = "Moderately full bladder (drink 3-4 glasses of water 1 hour prior and do not empty bladder).",
                prepBn = "মাঝারি ভরা মূত্রথলি প্রয়োজন (পরীক্ষার ১ ঘণ্টা আগে ৩-৪ গ্লাস পানি পান করুন এবং প্রস্রাব করবেন না)।",
                isContrast = false,
                procedureEn = "Performed transabdominally or transvaginally depending on gestational age and clarity needed.",
                procedureBn = "গর্ভকালীন বয়স ও স্পষ্টতার ওপর নির্ভর করে পেটের ওপর দিয়ে (Transabdominal) অথবা ভ্যাজাইনাল প্রোবের মাধ্যমে করা হয়।",
                safetyEn = "Extremely safe diagnostic ultrasound with zero radiation.",
                safetyBn = "কোনো প্রকার রেডিয়েশন ছাড়াই এটি একটি অত্যন্ত নিরাপদ ডায়াগনস্টিক আল্ট্রাসাউন্ড।"
            ),
            ImagingGuideData(
                titleEn = "Anomaly Scan (Level II Ultrasound / Morphology)",
                titleBn = "অ্যানিমেলি স্ক্যান (লেভেল ২ মরফোলজি স্ক্যান)",
                category = "OBGYN",
                purposeEn = "Detailed anatomical survey of the fetus between 18-22 weeks. Evaluates brain, spine, heart chambers, facial profile, limbs, and placenta.",
                purposeBn = "১৮-২২ সপ্তাহের মধ্যে ভ্রূণের বিস্তারিত অঙ্গসংস্থানগত পরীক্ষা। মস্তিষ্ক, মেরুদণ্ড, হৃদপিণ্ডের চেম্বার, মুখাবয়ব, অঙ্গপ্রত্যঙ্গ এবং প্লাসেন্টা মূল্যায়ন করে।",
                prepEn = "Normal hydration. Comfortable two-piece attire is recommended.",
                prepBn = "স্বাভাবিক হাইড্রেশন। ঢিলেঢালা দুই টুকরো পোশাক পরা সুবিধাজনক।",
                isContrast = false,
                procedureEn = "Detailed 2D/3D high-resolution ultrasound examining fetal organs systematically over 30-45 minutes.",
                procedureBn = "৩০-৪৫ মিনিট ধরে পদ্ধতিগতভাবে ভ্রূণের অঙ্গসমূহ পরীক্ষা করার জন্য বিশদ টু-ডি/থ্রি-ডি হাই-রেজোলিউশন আল্ট্রাসাউন্ড করা হয়।",
                safetyEn = "Non-invasive diagnostic imaging, safe for mother and fetus.",
                safetyBn = "নন-ইনভেসিভ ডায়াগনস্টিক ইমেজিং, মা ও ভ্রূণের জন্য সম্পূর্ণ নিরাপদ।"
            ),

            // 5. CARDIOLOGY & VASCULAR
            ImagingGuideData(
                titleEn = "Echocardiogram (TTE / Echo)",
                titleBn = "ইকোকার্ডিওগ্রাম (ইকো - টিটিই)",
                category = "CARDIO",
                purposeEn = "Ultrasound of the heart. Assesses left ventricular ejection fraction (LVEF), valve function, wall motion abnormalities, and pericardial effusion.",
                purposeBn = "হৃদপিণ্ডের আল্ট্রাসাউন্ড। বাম ভেন্ট্রিকুলার ইজেকশন ভগ্নাংশ (LVEF), ভালভ কার্যকারিতা, ওয়াল মোশন এবং পেরিকার্ডিয়াল ইফিউশন মূল্যায়ন করে।",
                prepEn = "No special preparation required. Wear comfortable clothing allowing upper body exposure.",
                prepBn = "বিশেষ কোনো প্রস্তুতির প্রয়োজন নেই। ওপরের দেহ উন্মুক্ত করার সুবিধাজনক পোশাক পরুন।",
                isContrast = false,
                procedureEn = "Ultrasound probe placed on chest wall with gel. ECG leads attached for simultaneous rhythm tracing.",
                procedureBn = "বুকে জেল লাগিয়ে আল্ট্রাসাউন্ড প্রোব রাখা হয়। একই সাথে হৃদস্পন্দন ট্র্যাক করার জন্য ইসিজি লিড যুক্ত করা হয়।",
                safetyEn = "Non-invasive, safe, no radiation.",
                safetyBn = "নন-ইনভেসিভ, নিরাপদ, কোনো রেডিয়েশন নেই।"
            ),
            ImagingGuideData(
                titleEn = "Coronary CT Angiography (CCTA)",
                titleBn = "করোনারি সিটি এনজিওগ্রাফি (সিজিসিটিএ)",
                category = "CARDIO",
                purposeEn = "Non-invasive visualization of coronary arteries. Detects atherosclerotic plaque, luminal stenosis, and coronary anomalies.",
                purposeBn = "করোনারি ধমনীর নন-ইনভেসিভ ভিজ্যুয়ালাইজেশন। এথেরোস্ক্লেরোটিক প্লাক, লুমিনাল স্টেনোসিস এবং করোনারি অসঙ্গতি শনাক্ত করে।",
                prepEn = "Heart rate must be optimized (<65 bpm) using beta-blockers if needed. Avoid caffeine/smoking for 24 hours prior.",
                prepBn = "প্রয়োজনে বিটা-ব্লকার দিয়ে হার্ট রেট অপ্টিমাইজ (<65 bpm) করতে হবে। স্ক্যানের ২৪ ঘণ্টা আগে ক্যাফেইন/ধূমপান বর্জন করুন।",
                isContrast = true,
                procedureEn = "ECG-gated high-speed CT acquisition synchronized with IV iodinated contrast injection during breath-hold.",
                procedureBn = "শ্বাস ধরে রাখার সময় আইভি আয়োডিন কন্ট্রাস্ট ইনজেকশনের সাথে ইসিজি-গেটেড হাই-স্পিড সিটি স্ক্যান করা হয়।",
                safetyEn = "Check kidney function and contrast allergy. Rate-control medication monitored by medical team.",
                safetyBn = "কিডনি কার্যকারিতা এবং কন্ট্রাস্ট অ্যালার্জি চেক করুন। রেট-কন্ট্রোল ওষুধ মেডিকেল টিম দ্বারা পর্যবেক্ষণ করা হয়।"
            ),
            ImagingGuideData(
                titleEn = "Carotid Duplex Ultrasound",
                titleBn = "ক্যারোটিড ডুপ্লেক্স আল্ট্রাসাউন্ড (ঘাড়ের রক্তনালী পরীক্ষা)",
                category = "CARDIO",
                purposeEn = "Evaluates blood flow through carotid arteries in the neck. Assesses atherosclerotic plaque buildup and stroke risk.",
                purposeBn = "ঘাড়ের ক্যারোটিড ধমনীর রক্তপ্রবাহ মূল্যায়ন করে। এথেরোস্ক্লেরোটিক প্লাক জমে যাওয়া এবং স্ট্রোকের ঝুঁকি মূল্যায়ন করে।",
                prepEn = "Wear an open-collar shirt or blouse. Remove necklaces and earrings.",
                prepBn = "কলার খোলা শার্ট বা ব্লাউজ পরুন। নেকলেস এবং কানের দুল খুলে ফেলুন।",
                isContrast = false,
                procedureEn = "Doppler ultrasound probe applied with gel along sides of the neck to measure blood velocity and vessel stenosis.",
                procedureBn = "রক্তের বেগ এবং রক্তনালীর সংকীর্ণতা পরিমাপ করতে গলার দুই পাশে জেলের সাহায্যে ডপলার প্রোব চালানো হয়।",
                safetyEn = "Completely safe, non-invasive vascular test.",
                safetyBn = "সম্পূর্ণ নিরাপদ, নন-ইনভেসিভ ভাসকুলার পরীক্ষা।"
            ),

            // 6. ORTHOPEDICS & MSK
            ImagingGuideData(
                titleEn = "MRI Knee (Meniscus & Ligaments)",
                titleBn = "এমআরআই নি (হাঁটু - মেনিস্কাস ও লিগামেন্ট)",
                category = "MSK",
                purposeEn = "Detailed evaluation of ACL/PCL tears, meniscal injury, cartilage defects, and bone bruises.",
                purposeBn = "এসিএল/পিসিএল (ACL/PCL) টিয়ার, মেনিস্কাস ইনজুরি, কার্টিলেজ ক্ষতি এবং হাড়ের ক্ষত বিশদভাবে মূল্যায়ন করে।",
                prepEn = "Remove all metal objects and jewelry. Wear comfortable clothes without metallic zippers/buttons.",
                prepBn = "সমস্ত ধাতব বস্তু এবং গহনা খুলে ফেলুন। ধাতব জিপার বা বোতামবিহীন আরামদায়ক পোশাক পরিধান করুন।",
                isContrast = false,
                procedureEn = "Knee placed inside a dedicated extremity coil. Patient remains still for 20-30 minutes.",
                procedureBn = "হাঁটু একটি নির্দিষ্ট এক্সট্রিমিটি কয়েলের মধ্যে রাখা হয়। রোগী ২০-৩০ মিনিট স্থির থাকেন।",
                safetyEn = "Ensure no ferromagnetic metal implants or shrapnel in body.",
                safetyBn = "শরীরে কোনো ফেরোম্যাগনেটিক মেটাল ইমপ্লান্ট বা স্প্রাপনেল নেই তা নিশ্চিত করুন।"
            ),
            ImagingGuideData(
                titleEn = "DEXA Scan (Bone Mineral Density)",
                titleBn = "ডেক্সা স্ক্যান (হাড়ের ঘনত্ব পরীক্ষা - বিএমডি)",
                category = "MSK",
                purposeEn = "Diagnoses osteoporosis and assesses fracture risk by measuring bone mineral density at spine and hip.",
                purposeBn = "মেরুদণ্ড এবং হিপের হাড়ের খনিজ ঘনত্ব পরিমাপ করে অস্টিওপরোসিস নির্ণয় এবং ফ্র্যাকচারের ঝুঁকি মূল্যায়ন করে।",
                prepEn = "Avoid calcium supplements for 24 hours prior. Wear clothing without metal buttons or belts.",
                prepBn = "পরীক্ষার ২৪ ঘণ্টা আগে ক্যালসিয়াম সাপ্লিমেন্ট এড়িয়ে চলুন। ধাতব বোতাম বা বেল্টবিহীন পোশাক পরুন।",
                isContrast = false,
                procedureEn = "Patient lies on a padded table while a scanner arm passes overhead measuring bone density.",
                procedureBn = "রোগী প্যাডেড টেবিলে শুয়ে থাকেন এবং স্ক্যানার আর্ম ওপর দিয়ে যাতায়াত করে হাড়ের ঘনত্ব পরিমাপ করে।",
                safetyEn = "Very low X-ray dose (less than standard background radiation).",
                safetyBn = "খুব কম এক্স-রে ডোজ (সাধারণ ব্যাকগ্রাউন্ড রেডিয়েশনের চেয়েও কম)।"
            ),

            // 7. NUCLEAR MEDICINE
            ImagingGuideData(
                titleEn = "PET-CT Scan (FDG - Whole Body)",
                titleBn = "পেট-সিটি স্ক্যান (এফডিজি - হোল বডি)",
                category = "NUCLEAR",
                purposeEn = "Molecular imaging evaluating glucose metabolism. Locates primary tumors, cancer staging, recurrence, and treatment response.",
                purposeBn = "গ্লুকোজ মেটাবলিজম মূল্যায়নকারী মলিকিউলার ইমেজিং। প্রাথমিক টিউমার, ক্যান্সার স্টেজিং, পুনরাবৃত্তি এবং চিকিৎসার কার্যকারিতা নির্ধারণ করে।",
                prepEn = "Fasting for 6 hours. Blood glucose must be <200 mg/dL. Avoid strenuous exercise 24 hours prior.",
                prepBn = "৬ ঘণ্টা উপবাস থাকতে হবে। রক্তের গ্লুকোজ <200 mg/dL হতে হবে। ২৪ ঘণ্টা আগে কঠোর ব্যায়াম এড়িয়ে চলুন।",
                isContrast = true,
                procedureEn = "Radiotracer (18F-FDG) injected IV; patient rests in quiet room for 60 mins before 25-minute PET-CT scan.",
                procedureBn = "রেডিওট্রেসার (18F-FDG) শিরায় দেওয়া হয়; ২৫ মিনিটের পেট-সিটি স্ক্যানের আগে রোগী শান্ত ঘরে ৬০ মিনিট বিশ্রাম নেন।",
                safetyEn = "Drink plenty of water post-scan to flush tracer. Avoid close contact with pregnant women/infants for 12 hours.",
                safetyBn = "ট্রেসার বের করার জন্য স্ক্যানের পরে প্রচুর পানি পান করুন। ১২ ঘণ্টা গর্ভবতী নারী ও শিশুদের কাছ থেকে দূরত্ব বজায় রাখুন।"
            ),
            ImagingGuideData(
                titleEn = "Bone Scan (Technetium-99m MDP)",
                titleBn = "বোন স্ক্যান (হাড়ের স্কিন্টিগ্রাফি)",
                category = "NUCLEAR",
                purposeEn = "Nuclear medicine test evaluating osteoblastic activity. Detects bone metastases, osteomyelitis, occult fractures, and Paget's disease.",
                purposeBn = "অস্টিওব্লাস্টিক কার্যকলাপ মূল্যায়নকারী নিউক্লিয়ার মেডিসিন পরীক্ষা। হাড়ের ক্যান্সার মেটাস্টেসিস, অস্টিওলাইটিস, সূক্ষ্ম ফ্র্যাকচার এবং পেজেট রোগ শনাক্ত করে।",
                prepEn = "No fasting required. Drink plenty of water between tracer injection and scan (hydration is vital).",
                prepBn = "খালি পেটে থাকার প্রয়োজন নেই। ট্রেসার ইনজেকশন এবং স্ক্যানের মধ্যবর্তী সময়ে প্রচুর পানি পান করুন (হাইড্রেশন অত্যন্ত জরুরি)।",
                isContrast = false,
                procedureEn = "Radiotracer (Tc-99m MDP) injected IV; imaging is performed 2-3 hours later while tracer incorporates into bone matrix.",
                procedureBn = "রেডিওট্রেসার (Tc-99m MDP) শিরায় ইনজেক্ট করা হয়; ট্রেসার হাড়ের সাথে মিশে যাওয়ার জন্য ২-৩ ঘণ্টা পর স্ক্যান করা হয়।",
                safetyEn = "Very low radiation exposure. Empty bladder frequently post-injection to reduce pelvic radiation.",
                safetyBn = "খুব কম রেডিয়েশন। পেলভিক রেডিয়েশন কমাতে ইনজেকশনের পরে ঘন ঘন প্রস্রাব করুন।"
            )
        )
    }

    val filteredList = remember(selectedTab, guides) {
        if (selectedTab == "ALL") guides else guides.filter { it.category == selectedTab }
    }

    Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MedicalServices, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isBangla) "মেডিকেল ইমেজিং গাইড" else "Medical Imaging Guide",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isBangla = !isBangla
                            }
                        ) {
                            Text(
                                text = if (isBangla) "EN" else "বাংলা",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = { AppBottomBar(context = context, currentTab = -1, backgroundBrush = AppGradientBrush) }
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Filter Tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(tabs) { (tag, label) ->
                        FilterChip(
                            selected = selectedTab == tag,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedTab = tag
                            },
                            label = { Text(label, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Guides List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 40.dp)
                ) {
                    items(filteredList) { guide ->
                        ImagingGuideCard(guide = guide, isBangla = isBangla, isDark = isDark)
                    }
                }
            }
        }
    }
}

@Composable
fun ImagingGuideCard(guide: ImagingGuideData, isBangla: Boolean, isDark: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val title = if (isBangla) guide.titleBn else guide.titleEn
    val purpose = if (isBangla) guide.purposeBn else guide.purposeEn
    val prep = if (isBangla) guide.prepBn else guide.prepEn
    val procedure = if (isBangla) guide.procedureBn else guide.procedureEn
    val safety = if (isBangla) guide.safetyBn else guide.safetyEn

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                expanded = !expanded
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, if (guide.isContrast) Color(0xFFF59E0B).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1A237E)
                        )
                        if (guide.isContrast) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isBangla) "কন্ট্রাস্ট" else "Contrast",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = purpose,
                        fontSize = 13.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.75f) else Color.DarkGray,
                        lineHeight = 18.sp
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Preparation
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (isBangla) "প্রস্তুতি (Preparation):" else "Preparation:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                        )
                        Text(
                            text = prep,
                            fontSize = 13.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray,
                            lineHeight = 18.sp
                        )
                    }

                    // Procedure
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (isBangla) "পরীক্ষার প্রক্রিয়া (Procedure):" else "Procedure:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                        )
                        Text(
                            text = procedure,
                            fontSize = 13.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray,
                            lineHeight = 18.sp
                        )
                    }

                    // Safety & Warnings
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Text(
                                text = if (isBangla) "নিরাপত্তা ও সতর্কতা (Safety):" else "Safety & Precautions:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                        Text(
                            text = safety,
                            fontSize = 13.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
