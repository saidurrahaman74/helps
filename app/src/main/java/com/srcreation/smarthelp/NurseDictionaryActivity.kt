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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.util.Locale

class NurseDictionaryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                DictionaryScreen(onBack = { finish() })
            }
        }
    }
}

data class MedicalTerm(
    val term: String,
    val definitionEn: String,
    val definitionBn: String,
    val category: String // Abbreviation, Clinical Sign, Pathology, Procedure, Pharmacology
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }
    val isBn = remember { Locale.getDefault().language == "bn" }

    val dictionaryData = remember {
        listOf(
            // 1. Prescription & Latin Abbreviations
            MedicalTerm("stat", "Immediately / Right now without delay.", "অবিলম্বে / এখনই কোনো বিলম্ব ছাড়া।", "Abbreviation"),
            MedicalTerm("prn (pro re nata)", "As needed when necessary.", "প্রয়োজন অনুযায়ী (যেমন ব্যথার জন্য)।", "Abbreviation"),
            MedicalTerm("bid (bis in die)", "Twice a day (every 12 hours).", "দিনে ২ বার (প্রতি ১২ ঘন্টায়)।", "Abbreviation"),
            MedicalTerm("tid (ter in die)", "Three times a day (every 8 hours).", "দিনে ৩ বার (প্রতি ৮ ঘন্টায়)।", "Abbreviation"),
            MedicalTerm("qid (quater in die)", "Four times a day (every 6 hours).", "দিনে ৪ বার (প্রতি ৬ ঘন্টায়)।", "Abbreviation"),
            MedicalTerm("q4h / q6h / q8h", "Every 4 / 6 / 8 hours continuously.", "প্রতি ৪ / ৬ / ৮ ঘণ্টা পর পর।", "Abbreviation"),
            MedicalTerm("npo (nil per os)", "Nothing by mouth (no food/water).", "মুখে কিছুই খাওয়া যাবে না (এনপিও)।", "Abbreviation"),
            MedicalTerm("ac (ante cibum)", "Before meals.", "খাবারের পূর্বে।", "Abbreviation"),
            MedicalTerm("pc (post cibum)", "After meals.", "খাবারের পরে।", "Abbreviation"),
            MedicalTerm("hs (hora somni)", "At bedtime / night.", "ঘুমানোর পূর্বে / রাতে।", "Abbreviation"),
            MedicalTerm("po (per os)", "By mouth / orally.", "মুখের মাধ্যমে সেব্য।", "Abbreviation"),
            MedicalTerm("ad lib", "As desired / freely.", "ইচ্ছামতো / প্রয়োজন অনুযায়ী।", "Abbreviation"),
            MedicalTerm("gtt (guttae)", "Drops (e.g. eye drops, drip drops).", "ড্রপ বা ফোঁটা (চোখের ড্রপ/আইভি ড্রপ)।", "Abbreviation"),
            MedicalTerm("iv (intravenous)", "Injected into a vein.", "শিরার মাধ্যমে আইভি ইনজেকশন।", "Abbreviation"),
            MedicalTerm("im (intramuscular)", "Injected into a muscle.", "পেশীর মাধ্যমে আইএম ইনজেকশন।", "Abbreviation"),
            MedicalTerm("sc (subcutaneous)", "Injected into fatty tissue under skin.", "ত্বকের নিচের ফ্যাটি টিস্যুতে ইনজেকশন।", "Abbreviation"),
            MedicalTerm("id (intradermal)", "Injected into the dermis layer of skin.", "ত্বকের অভ্যন্তরীণ স্তরে (ইন্ট্রাডার্মাল)।", "Abbreviation"),
            MedicalTerm("sos (si opus sit)", "In emergency / if needed urgently.", "জরুরি প্রয়োজনে সেব্য।", "Abbreviation"),
            MedicalTerm("Rx", "Prescription / Take thou.", "প্রেসক্রিপশন বা সেবন নির্দেশিকা।", "Abbreviation"),
            MedicalTerm("Dx", "Diagnosis.", "রোগ নির্ণয় (ডায়াগনোসিস)।", "Abbreviation"),
            MedicalTerm("Hx", "Patient Medical History.", "রোগীর পূর্ববর্তী শারীরিক ইতিহাস।", "Abbreviation"),
            MedicalTerm("Tx", "Treatment / Therapy.", "চিকিৎসা বা থেরাপি।", "Abbreviation"),

            // 2. Clinical Signs & Symptoms
            MedicalTerm("Aphasia", "Inability to speak or understand speech due to brain lesion.", "মস্তিষ্কের ক্ষতির কারণে কথা বলতে বা বুঝতে না পারা।", "Clinical Sign"),
            MedicalTerm("Dyspnea", "Difficult, labored, or painful breathing.", "শ্বাসকষ্ট বা কষ্টদায়ক শ্বাসপ্রশ্বাস।", "Clinical Sign"),
            MedicalTerm("Orthopnea", "Shortness of breath that occurs when lying flat.", "সোজা হয়ে শুয়ে থাকলে তীব্র শ্বাসকষ্ট।", "Clinical Sign"),
            MedicalTerm("PND (Paroxysmal Nocturnal Dyspnea)", "Sudden awakening from sleep with severe dyspnea.", "রাতে ঘুমের মধ্যে হঠাৎ তীব্র শ্বাসকষ্টে জেগে ওঠা।", "Clinical Sign"),
            MedicalTerm("Tachypnea", "Abnormally rapid breathing rate (>20 breaths/min).", "অতিরিক্ত দ্রুত শ্বাসপ্রশ্বাস (প্রতি মিনিটে ২০ এর বেশি)।", "Clinical Sign"),
            MedicalTerm("Bradypnea", "Abnormally slow breathing rate (<12 breaths/min).", "অস্বাভাবিক ধীর শ্বাসপ্রশ্বাস (প্রতি মিনিটে ১২ এর কম)।", "Clinical Sign"),
            MedicalTerm("Cyanosis", "Bluish discoloration of skin/mucosa due to low oxygen.", "রক্তে অক্সিজেনের অভাবে ত্বক বা ঠোঁট নীল হওয়া।", "Clinical Sign"),
            MedicalTerm("Edema", "Swelling caused by excess interstitial fluid.", "টিস্যুতে অতিরিক্ত তরল জমে শরীর বা পা ফোলা।", "Clinical Sign"),
            MedicalTerm("Ascites", "Abnormal accumulation of fluid in the peritoneal cavity.", "পেটের গহ্বরে অতিরিক্ত পানি বা তরল জমা।", "Clinical Sign"),
            MedicalTerm("Jaundice", "Yellowish staining of skin and sclera from high bilirubin.", "রক্তে বিলিরুবিন বেড়ে ত্বক ও চোখ হলুদ হওয়া।", "Clinical Sign"),
            MedicalTerm("Erythema", "Redness of the skin caused by capillary congestion.", "ত্বকে কৈশিক নালীর প্রদাহে লালচে ভাব।", "Clinical Sign"),
            MedicalTerm("Pruritus", "Severe itching sensation of the skin.", "ত্বকের তীব্র চুলকানি বা ইচিং।", "Clinical Sign"),
            MedicalTerm("Febrile", "Having symptoms of fever / Pyrexia.", "জ্বর বা শরীরের তাপমাত্রা বৃদ্ধি।", "Clinical Sign"),
            MedicalTerm("Tachycardia", "Abnormally rapid heart rate (>100 bpm).", "অতিরিক্ত দ্রুত হৃদস্পন্দন (প্রতি মিনিটে ১০০ এর বেশি)।", "Clinical Sign"),
            MedicalTerm("Bradycardia", "Abnormally slow heart rate (<60 bpm).", "অস্বাভাবিক ধীর হৃদস্পন্দন (প্রতি মিনিটে ৬০ এর কম)।", "Clinical Sign"),
            MedicalTerm("Arrhythmia", "Irregular pattern or rhythm of heartbeat.", "হৃদস্পন্দনের অনিয়মিত ছন্দ বা গতি।", "Clinical Sign"),
            MedicalTerm("Palpitation", "Unpleasant awareness of rapid or irregular heartbeat.", "হৃদপিণ্ড দ্রুত বা অনিয়মিত স্পন্দিত হওয়ার অনুভূতি।", "Clinical Sign"),
            MedicalTerm("Paresthesia", "Abnormal burning or prickling sensation (numbness).", "ত্বকে পিন ফোটার মতো বা অবশ অনুভূতি।", "Clinical Sign"),
            MedicalTerm("Dysphagia", "Difficulty or pain in swallowing food or liquid.", "খাবার বা তরল গিলতে কষ্ট বা ব্যথা।", "Clinical Sign"),
            MedicalTerm("Ataxia", "Lack of voluntary coordination of muscle movements.", "শরীরের ভারসাম্য ও পেশীর সমন্বয়হীনতা।", "Clinical Sign"),
            MedicalTerm("Vertigo", "Sensation of spinning or room whirling.", "মাথা ঘোরা বা চারপাশ ঘোরে মনে হওয়া।", "Clinical Sign"),
            MedicalTerm("Syncope", "Temporary loss of consciousness / fainting.", "সাময়িকভাবে জ্ঞান হারানো বা মূর্ছা যাওয়া।", "Clinical Sign"),
            MedicalTerm("Anuria", "Absence of urine excretion (<100 ml/24h).", "মূত্র উৎপাদন প্রায় বন্ধ হওয়া (দিনে ১০০ মিলির কম)।", "Clinical Sign"),
            MedicalTerm("Oliguria", "Abnormally low urine output (<400 ml/24h).", "মূত্র উৎপাদন স্বাভাবিকের চেয়ে কম (দিনে ৪০০ মিলির কম)।", "Clinical Sign"),
            MedicalTerm("Polyuria", "Excessive production of urine (>2500 ml/24h).", "অতিরিক্ত প্রস্রাব উৎপাদন (দিনে ২৫০০ মিলির বেশি)।", "Clinical Sign"),
            MedicalTerm("Dysuria", "Painful or difficult urination.", "প্রস্রাবের সময় তীব্র জ্বালাপোড়া বা ব্যথা।", "Clinical Sign"),
            MedicalTerm("Hematuria", "Presence of blood in the urine.", "প্রস্রাবের সাথে লাল রক্ত যাওয়া।", "Clinical Sign"),
            MedicalTerm("Hematemesis", "Vomiting of fresh blood.", "মুখ দিয়ে তাজা রক্ত বমি হওয়া।", "Clinical Sign"),
            MedicalTerm("Hemoptysis", "Coughing up blood from respiratory tract.", "কাশির সাথে রক্ত আসা।", "Clinical Sign"),
            MedicalTerm("Melena", "Dark tarry stools containing altered blood.", "পাকস্থলী/অন্ত্রের রক্তপাতে কালো রঙের পায়খানা।", "Clinical Sign"),

            // 3. Pathology & Diseases
            MedicalTerm("Sepsis", "Life-threatening organ dysfunction from infection.", "সংক্রমণের কারণে শরীরের প্রাণঘাতী অঙ্গ অকার্যকারিতা।", "Pathology"),
            MedicalTerm("Ischemia", "Inadequate blood supply to an organ or tissue.", "অঙ্গে বা টিস্যুতে পর্যাপ্ত রক্ত সরবরাহের অভাব।", "Pathology"),
            MedicalTerm("Infarction", "Tissue necrosis due to ischemia (e.g. MI).", "রক্ত সরবরাহের অভাবে টিস্যুর বিনাশ বা মৃত্যু।", "Pathology"),
            MedicalTerm("Necrosis", "Uncontrolled cell and tissue death.", "কোষ বা টিস্যুর অনিয়ন্ত্রিত ক্ষয় ও মৃত্যু।", "Pathology"),
            MedicalTerm("Atrophy", "Wasting away or decrease in size of tissue.", "টিস্যু বা অঙ্গের আকার ও ওজন হ্রাস।", "Pathology"),
            MedicalTerm("Hypertrophy", "Increase in the size of cells resulting in organ enlargement.", "কোষের আকার বৃদ্ধির ফলে অঙ্গের আয়তন বৃদ্ধি।", "Pathology"),
            MedicalTerm("Stenosis", "Abnormal narrowing of a tubular structure or valve.", "নালী, ধমনী বা ভালভ অস্বাভাবিক সংকুচিত হওয়া।", "Pathology"),
            MedicalTerm("Aneurysm", "Abnormal ballooning or bulge in blood vessel wall.", "রক্তনালীর দেওয়াল দুর্বল হয়ে বেলুনের মতো ফোলা।", "Pathology"),
            MedicalTerm("Thrombosis", "Formation of a blood clot inside a blood vessel.", "রক্তনালীর ভেতরে রক্ত জমাট বাঁধা (Thrombus)।", "Pathology"),
            MedicalTerm("Embolism", "Obstruction of an artery by a circulating clot or bubble.", "রক্তস্রোতে ভেসে আসা চর্বি বা জমাট রক্তে ধমনী ব্লক হওয়া।", "Pathology"),
            MedicalTerm("Cirrhosis", "Irreversible scarring and fibrosis of liver tissue.", "লিভারের টিস্যু স্থায়ীভাবে ক্ষতিগ্রস্ত ও শক্ত হয়ে যাওয়া।", "Pathology"),
            MedicalTerm("Benign", "Non-cancerous tumor that does not spread.", "ক্যান্সারহীন বা ক্ষতিকর নয় এমন টিউমার।", "Pathology"),
            MedicalTerm("Malignant", "Cancerous growth that invades nearby tissues.", "মারাত্মক ক্যান্সারযুক্ত টিউমার।", "Pathology"),
            MedicalTerm("Metastasis", "Spread of cancer cells from primary site to distant organs.", "ক্যান্সার কোষ শরীরের অন্য অঙ্গে ছড়িয়ে পড়া।", "Pathology"),
            MedicalTerm("Acidosis", "Excess acid in body fluids (pH < 7.35).", "শরীরের তরলে অতিরিক্ত এসিড জমে যাওয়া (পিএইচ < ৭.৩৫)।", "Pathology"),
            MedicalTerm("Alkalosis", "Excess base in body fluids (pH > 7.45).", "শরীরের তরলে ক্ষারের পরিমাণ বৃদ্ধি পাওয়া (পিএইচ > ৭.৪৫)।", "Pathology"),
            MedicalTerm("Hypoxemia", "Low oxygen levels in arterial blood.", "ধমনীর রক্তে অক্সিজেনের মাত্রা কমে যাওয়া।", "Pathology"),
            MedicalTerm("Hyperglycemia", "High blood glucose levels.", "রক্তে শর্করার মাত্রা অস্বাভাবিক বৃদ্ধি পাওয়া।", "Pathology"),
            MedicalTerm("Hypoglycemia", "Low blood glucose levels.", "রক্তে শর্করার মাত্রা অতিরিক্ত কমে যাওয়া।", "Pathology"),
            MedicalTerm("Hyperkalemia", "High blood potassium levels.", "রক্তে পটাশিয়ামের মাত্রা বেড়ে যাওয়া।", "Pathology"),
            MedicalTerm("Hypokalemia", "Low blood potassium levels.", "রক্তে পটাশিয়ামের মাত্রা কমে যাওয়া।", "Pathology"),

            // 4. Nursing Procedures & Interventions
            MedicalTerm("Aseptic Technique", "Procedures used to keep environment free from pathogens.", "সংক্রমণ রোধে জীবাণুমুক্ত নার্সিং পদ্ধতি।", "Procedure"),
            MedicalTerm("Debridement", "Surgical removal of dead or infected wound tissue.", "ক্ষত স্থান থেকে পচা বা মৃত টিস্যু কেটে পরিষ্কার করা।", "Procedure"),
            MedicalTerm("Intubation", "Insertion of an endotracheal tube into trachea for airway.", "শ্বাসপ্রশ্বাস চালুর জন্য শ্বাসনালীতে টিউব স্থাপন।", "Procedure"),
            MedicalTerm("Extubation", "Removal of endotracheal tube after airway recovery.", "শ্বাসনালী থেকে এন্ডোট্রাকিয়াল টিউব খুলে ফেলা।", "Procedure"),
            MedicalTerm("Cannulation", "Inserting a venous cannula for IV fluid/medication.", "রক্তনালীতে আইভি ক্যানুলা প্রবেশ করানো।", "Procedure"),
            MedicalTerm("Phlebotomy", "Puncturing a vein to withdraw blood samples.", "পরীক্ষার জন্য রগ থেকে রক্ত সংগ্রহ করা।", "Procedure"),
            MedicalTerm("Auscultation", "Listening to internal body sounds using a stethoscope.", "স্টেথোস্কোপ দিয়ে বুক বা পেটের শব্দ শোনা।", "Procedure"),
            MedicalTerm("Palpation", "Examining body with hands to feel organs/tenderness.", "হাত দিয়ে স্পর্শ করে অঙ্গে ব্যথা বা ফোলা পরীক্ষা।", "Procedure"),
            MedicalTerm("Percussion", "Tapping body parts with fingers to assess resonance.", "আঙ্গুল দিয়ে শরীরে টোকা দিয়ে অভ্যন্তরীণ শব্দ পরীক্ষা।", "Procedure"),
            MedicalTerm("Catheterization", "Inserting a catheter into urinary bladder to drain urine.", "প্রস্রাব বের করার জন্য ক্যাথেটার স্থাপন।", "Procedure"),
            MedicalTerm("Tracheostomy", "Surgical opening in windpipe for breathing.", "শ্বাসনালীতে সার্জিক্যাল ছিদ্র করে শ্বাস চালনা।", "Procedure"),
            MedicalTerm("Paracentesis", "Needle aspiration of fluid from peritoneal cavity.", "সুই দিয়ে পেটের গহ্বর থেকে অতিরিক্ত পানি বের করা।", "Procedure"),
            MedicalTerm("Thoracentesis", "Needle aspiration of fluid from pleural cavity around lungs.", "সুই দিয়ে ফুসফুসের চারপাশ থেকে জমা পানি বের করা।", "Procedure"),
            MedicalTerm("Dialysis", "Artificial filtering of blood metabolic waste.", "মেশিনের সাহায্যে রক্তের বর্জ্য কৃত্রিমভাবে ছেঁকে ফেলা।", "Procedure"),
            MedicalTerm("Palliative Care", "Care focused on symptom relief for terminal illnesses.", "দুরারোগ্য রোগে রোগীর ব্যথা ও কষ্ট লাঘবে যত্ন।", "Procedure"),
            MedicalTerm("CPR (Cardiopulmonary Resuscitation)", "Emergency life-saving procedure using chest compressions.", "বুকে চাপ দিয়ে কৃত্রিম শ্বাস-প্রশ্বাস চালুর জীবনরক্ষাকারী পদ্ধতি।", "Procedure"),
            MedicalTerm("Defibrillation", "Delivering electric shock to restore normal heart rhythm.", "বৈদ্যুতিক শক দিয়ে হার্টের স্বাভাবিক ছন্দ ফিরিয়ে আনা।", "Procedure"),
            MedicalTerm("Triage", "Sorting patients based on severity of illness/injury.", "রোগের তীব্রতা অনুযায়ী রোগীদের অগ্রাধিকার ভিত্তিতে বাছাই।", "Procedure"),
            MedicalTerm("Venipuncture", "Drawing blood from a vein for diagnostic tests.", "ল্যাব পরীক্ষার জন্য শিরা থেকে রক্ত সংগ্রহ করা।", "Procedure"),

            // 5. Pharmacology Terminology
            MedicalTerm("Antipyretic", "Medication used to reduce fever.", "জ্বর কমানোর ওষুধ (যেমন: প্যারাসিটামল)।", "Pharmacology"),
            MedicalTerm("Analgesic", "Medication used to relieve pain (painkiller).", "ব্যথা নাশক বা ব্যথা কমানোর ওষুধ।", "Pharmacology"),
            MedicalTerm("Antibiotic", "Medication that destroys or inhibits bacterial growth.", "ব্যাকটেরিয়া ধ্বংস বা বৃদ্ধি রোধকারী ওষুধ।", "Pharmacology"),
            MedicalTerm("Bronchodilator", "Medication that widens airways in lungs.", "ফুসফুসের শ্বাসনালী প্রসারিতকারী ওষুধ (ইনহেলার)।", "Pharmacology"),
            MedicalTerm("Diuretic", "Medication that increases urine output to reduce fluid overload.", "প্রস্রাবের পরিমাণ বাড়িয়ে শরীর থেকে অতিরিক্ত পানি বের করার ওষুধ।", "Pharmacology"),
            MedicalTerm("Anticoagulant", "Blood thinner that prevents clot formation.", "রক্ত জমাট বাঁধা রোধকারী ওষুধ (রক্ত পাতলা করার ওষুধ)।", "Pharmacology"),
            MedicalTerm("Vasopressor", "Medication that constricts blood vessels to raise blood pressure.", "রক্তনালী সংকুচিত করে রক্তচাপ বাড়ায় (যেমন: নরএপিনেফ্রিন)।", "Pharmacology"),
            MedicalTerm("Inotrope", "Medication that alters the force of heart muscle contractions.", "হৃদপেশীর সংকোচনের শক্তি পরিবর্তনকারী ওষুধ (যেমন: ডোপামিন)।", "Pharmacology"),
            MedicalTerm("Antiemetic", "Medication that prevents nausea and vomiting.", "বমি ভাব ও বমি রোধক ওষুধ।", "Pharmacology"),
            MedicalTerm("Antihistamine", "Medication used to treat allergy symptoms.", "অ্যালার্জি ও হাঁচি-কাশি নিবারক ওষুধ।", "Pharmacology")
        )
    }

    val filteredList = remember(searchQuery, isBn) {
        if (searchQuery.isBlank()) dictionaryData
        else dictionaryData.filter { 
            it.term.contains(searchQuery, ignoreCase = true) || 
            it.definitionEn.contains(searchQuery, ignoreCase = true) ||
            it.definitionBn.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = if (isBn) "মেডিকেল ডিকশনারি (WHO / JCAHO Standard)" else "Medical Dictionary (WHO/JCAHO)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBack()
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        actions = { LanguageToggleButton() },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (isBn) "মেডিকেল শব্দ খুঁজুন (যেমন: Sepsis, Triage, Stat, Edema)..." else "Search medical term (e.g. Sepsis, Triage, Stat)...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }
            }
        },
        bottomBar = { AppBottomBar(context = LocalContext.current, currentTab = -1, isNurseMode = true, backgroundBrush = AppGradientBrush) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = AppGradientBrush)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList) { term ->
                    DictionaryCard(data = term, isDark = isSystemInDarkTheme(), isBn = isBn)
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun DictionaryCard(data: MedicalTerm, isDark: Boolean, isBn: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                expanded = !expanded
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = data.term,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = data.definitionEn,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray,
                lineHeight = 18.sp
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBn) "বাংলা অর্থ ও ব্যাখ্যা:" else "Bangla Meaning & Explanation:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = data.definitionBn,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
