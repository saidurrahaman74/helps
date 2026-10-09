package com.srcreation.smarthelp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.util.Locale

class MedicalAbbreviationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                AbbreviationScreen(onBack = { finish() })
            }
        }
    }
}

data class AbbrData(
    val term: String,
    val fullFormEn: String,
    val fullFormBn: String,
    val categoryEn: String,
    val categoryBn: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbbreviationScreen(onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var searchQuery by remember { mutableStateOf("") }
    val isBn = remember { Locale.getDefault().language == "bn" }

    val allAbbrs = remember {
        listOf(
            // 1. Prescription Frequency & Administration Route
            AbbrData("AC", "Before Meals (Ante Cibum)", "খাবারের পূর্বে (Ante Cibum)", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("PC", "After Meals (Post Cibum)", "খাবারের পরে (Post Cibum)", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("BID", "Twice a Day (every 12 hours)", "দিনে ২ বার (প্রতি ১২ ঘন্টায়)", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("TID", "Three Times a Day (every 8 hours)", "দিনে ৩ বার (প্রতি ৮ ঘন্টায়)", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("QID / QDS", "Four Times a Day (every 6 hours)", "দিনে ৪ বার (প্রতি ৬ ঘন্টায়)", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("OD", "Once Daily", "দিনে ১ বার", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("HS", "At Bedtime (Hora Somni)", "ঘুমানোর পূর্বে / রাতে", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("PRN", "As Needed / When Necessary", "প্রয়োজন অনুযায়ী", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("STAT", "Immediately / Right Now", "অবিলম্বে / এখনই", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("SOS", "If Needed Urgently", "জরুরি প্রয়োজনে", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("Q4H / Q6H / Q8H", "Every 4 / 6 / 8 Hours", "প্রতি ৪ / ৬ / ৮ ঘণ্টা পর পর", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("NPO", "Nothing by Mouth (Nil Per Os)", "মুখে কিছুই খাওয়া যাবে না", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("PO", "By Mouth / Orally (Per Os)", "মুখের মাধ্যমে সেব্য", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("IV", "Intravenous Injection/Infusion", "শিরার মাধ্যমে (আইভি)", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("IM", "Intramuscular Injection", "পেশীর মাধ্যমে (আইএম)", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("SC / SubQ", "Subcutaneous Injection", "ত্বকের ফ্যাটি টিস্যুতে ইনজেকশন", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("SL", "Sublingual (Under the tongue)", "জিহ্বার নিচে সেব্য", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("ID", "Intradermal Injection", "ত্বকের অভ্যন্তরীণ স্তরে ইনজেকশন", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("PR", "Per Rectum (Rectal)", "মলদ্বারের মাধ্যমে", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("PV", "Per Vaginam (Vaginal)", "যোনিপথের মাধ্যমে", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("IVPB", "Intravenous Piggyback", "আইভি লাইনে সংযোগকৃত অতিরক্ত আইভি ড্রিপ", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("GTT", "Drops (Guttae)", "ড্রপ বা ফোঁটা", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("Rx", "Prescription / Treatment Order", "প্রেসক্রিপশন নির্দেশিকা", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("Dx", "Diagnosis", "রোগ নির্ণয় (ডায়াগনোসিস)", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("Hx", "Medical History", "রোগীর শারীরিক ইতিহাস", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("Tx", "Treatment / Therapy", "চিকিৎসা বা থেরাপি", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("Fx", "Fracture", "অস্থি ভঙ্গ বা ফ্র্যাকচার", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),
            AbbrData("Sx", "Symptoms", "রোগের লক্ষণ বা উপসর্গ", "Frequency & Route", "ওষুধের মাত্রা ও পথ"),

            // 2. Clinical Vital Signs & Measurements
            AbbrData("BP", "Blood Pressure", "রক্তচাপ (ব্ল্যাড প্রেশার)", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("HR / PR", "Heart Rate / Pulse Rate", "হৃদস্পন্দনের হার / পালস রেট", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("RR", "Respiratory Rate", "শ্বাসপ্রশ্বাসের হার (প্রতি মিনিটে)", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("SpO2", "Peripheral Oxygen Saturation", "রক্তে অক্সিজেনের সম্পৃক্ততা", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("Temp", "Body Temperature", "শরীরের তাপমাত্রা", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("MAP", "Mean Arterial Pressure", "ধমনীর গড় রক্তচাপ (৭০-১০ সকালে mmHg)", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("CVP", "Central Venous Pressure", "সেন্ট্রাল ভেনাস প্রেশার (২-৮ mmHg)", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("BMI", "Body Mass Index", "শরীরের ভর সূচক (বিএমআই)", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("BSA", "Body Surface Area", "শরীরের ত্বকীয় পৃষ্ঠের ক্ষেত্রফল (m²)", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("GCS", "Glasgow Coma Scale", "মস্তিষ্কের চেতনার মাত্রা স্কেল (৩-১৫)", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("BGL / RBS", "Random Blood Glucose Level", "র্যান্ডম ব্লাড সুগার লেভেল", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("FBG / FBS", "Fasting Blood Glucose / Sugar", "খালি পেটে ব্লাড সুগার লেভেল", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("2hPP", "2-Hour Postprandial Glucose", "খাওয়ার ২ ঘন্টা পর ব্লাড সুগার", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("I/O", "Intake and Output Chart", "তরল গ্রহণ ও বর্জ্য নির্গমন চার্ট", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("ICP", "Intracranial Pressure", "মস্তিষ্কের ভেতরের চাপ", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),
            AbbrData("SaO2", "Arterial Oxygen Saturation", "ধমনীর রক্তে অক্সিজেনের হার", "Clinical & Vitals", "ভিটাল ও পরিমাপ"),

            // 3. Clinical Conditions & Diseases
            AbbrData("MI", "Myocardial Infarction (Heart Attack)", "হৃদরোগ / হার্ট অ্যাটাক", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("CVA", "Cerebrovascular Accident (Stroke)", "মস্তিষ্কে স্ট্রোক", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("CHF", "Congestive Heart Failure", "কনজেস্টিভ হার্ট ফেইলুর", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("DVT", "Deep Vein Thrombosis", "পায়ের গভীর রগে রক্ত জমাট বাঁধা", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("PE", "Pulmonary Embolism", "ফুসফুসের ধমনীতে জমাট রক্ত ব্লক", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("DKA", "Diabetic Ketoacidosis", "ডায়াবেটিক কিটোঅ্যাসিডোসিস", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("ARDS", "Acute Respiratory Distress Syndrome", "তীব্র শ্বাসযন্ত্রীয় অকার্যকারিতা", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("COPD", "Chronic Obstructive Pulmonary Disease", "ফুসফুসের দীর্ঘস্থায়ী শ্বাসকষ্ট রোগ", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("UTI", "Urinary Tract Infection", "মূত্রনালীর সংক্রমণ (ইউটিআই)", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("ESRD", "End-Stage Renal Disease", "ক্রনিক কিডনি রোগের শেষ পর্যায়", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("CKD / AKI", "Chronic Kidney Disease / Acute Kidney Injury", "দীর্ঘস্থায়ী বা তীব্র কিডনি রোগ", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("BPH", "Benign Prostatic Hyperplasia", "প্রোস্টেট গ্রন্থি বৃদ্ধি", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("DIC", "Disseminated Intravascular Coagulation", "অনিয়ন্ত্রিত রক্ত জমাট বাঁধা ব্যাধি", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("HTN", "Hypertension (High BP)", "উচ্চ রক্তচাপ (হাইপারটেনশন)", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("DM", "Diabetes Mellitus", "ডায়াবেটিস মেলাইটাস", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("CAD", "Coronary Artery Disease", "করোনারি ধমনীর রোগ", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("GERD", "Gastroesophageal Reflux Disease", "গ্যাস্ট্রিক এসিড রিফ্লাক্স রোগ", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("PUD", "Peptic Ulcer Disease", "পেপটিক আলসার বা পেটের ক্ষত", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),
            AbbrData("DNR", "Do Not Resuscitate", "কৃত্রিম পুনরুজ্জীবন না করার নির্দেশ", "Clinical Conditions", "ক্লিনিকাল অবস্থা ও রোগ"),

            // 4. Hospital Departments & Units
            AbbrData("ICU", "Intensive Care Unit", "নিবিড় পরিচর্যা কেন্দ্র (আইসিইউ)", "Hospital Units", "হাসপাতাল বিভাগ"),
            AbbrData("CCU", "Coronary Care Unit", "করোনারি কেয়ার ইউনিট (হৃদরোগ আইসিইউ)", "Hospital Units", "হাসপাতাল বিভাগ"),
            AbbrData("HDU", "High Dependency Unit", "হাই ডিপেন্ডেন্সি ইউনিট", "Hospital Units", "হাসপাতাল বিভাগ"),
            AbbrData("NICU", "Neonatal Intensive Care Unit", "নবজাতক নিবিড় পরিচর্যা কেন্দ্র", "Hospital Units", "হাসপাতাল বিভাগ"),
            AbbrData("PICU", "Pediatric Intensive Care Unit", "শিশু নিবিড় পরিচর্যা কেন্দ্র", "Hospital Units", "হাসপাতাল বিভাগ"),
            AbbrData("ER / ED", "Emergency Room / Department", "জরুরি বিভাগ (ইমার্জেন্সি)", "Hospital Units", "হাসপাতাল বিভাগ"),
            AbbrData("OR / OT", "Operating Room / Operation Theatre", "অপারেশন থিয়েটার (ওটি)", "Hospital Units", "হাসপাতাল বিভাগ"),
            AbbrData("PACU", "Post-Anesthesia Care Unit", "অপারেশন পরবর্তী রিকভারি রুম", "Hospital Units", "হাসপাতাল বিভাগ"),
            AbbrData("OPD", "Outpatient Department", "বহিঃবিভাগ (আউটডোর)", "Hospital Units", "হাসপাতাল বিভাগ"),
            AbbrData("IPD", "Inpatient Department", "অন্তঃবিভাগ (ইনডোর ভর্তি)", "Hospital Units", "হাসপাতাল বিভাগ"),

            // 5. Fluids, IV & Laboratory Tests
            AbbrData("NS", "Normal Saline (0.9% Sodium Chloride)", "নরমাল স্যালাইন (০.৯% সোডিয়াম ক্লোরাইড)", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("RL", "Ringer's Lactate Solution", "রিঙ্গার্স ল্যাকটেট স্যালাইন", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("D5W", "5% Dextrose in Water", "৫% ডেক্সট্রোজ ওয়াটার", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("DNS", "Dextrose Normal Saline", "ডেক্সট্রোজ নরমাল স্যালাইন", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("KCL", "Potassium Chloride Infusion", "পটাশিয়াম ক্লোরাইড ইনফিউশন", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("CBC", "Complete Blood Count", "সম্পূর্ণ রক্তকণিকা পরীক্ষা (সিবিসি)", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("ESR", "Erythrocyte Sedimentation Rate", "ইএসআর পরীক্ষা", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("ABG", "Arterial Blood Gas Analysis", "ধমনীর রক্ত গ্যাস এনালাইসিস", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("LFT", "Liver Function Test", "লিভার ফাংশন টেস্ট (যকৃত পরীক্ষা)", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("KFT / RFT", "Kidney Function Test", "কিডনি ফাংশন টেস্ট", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("RFT", "Renal Function Test", "রেনাল ফাংশন টেস্ট", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("TFT", "Thyroid Function Test", "থাইরয়েড ফাংশন টেস্ট", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("PT/INR", "Prothrombin Time / INR", "প্রোথ্রম্বিন টাইম ও আইএনআর", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("APTT", "Activated Partial Thromboplastin Time", "এপিটিটি টেস্ট", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("PSA", "Prostate-Specific Antigen", "প্রোস্টেট স্পেসিফিক অ্যান্টিজেন", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("CRP", "C-Reactive Protein", "সি-রিয়্যাক্টিভ প্রোটিন", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("LDH", "Lactate Dehydrogenase", "ল্যাকটেট ডিহাইড্রোজিনেজ", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),
            AbbrData("CK-MB", "Creatine Kinase-MB", "ক্রিয়েটিন কাইনেজ এমবি (হার্ট এনজাইম)", "Fluids & Labs", "স্যালাইন ও ল্যাব পরীক্ষা"),

            // 6. Radiology & Imaging Abbreviations
            AbbrData("CXR", "Chest X-Ray", "বুকের এক্স-রে", "Radiology", "রেডিওলজি ও ইমেজিং"),
            AbbrData("USG", "Ultrasonography", "আল্ট্রাসনোগ্রাফি (ইউএসজি)", "Radiology", "রেডিওলজি ও ইমেজিং"),
            AbbrData("CT", "Computed Tomography Scan", "সিটি স্ক্যান", "Radiology", "রেডিওলজি ও ইমেজিং"),
            AbbrData("MRI", "Magnetic Resonance Imaging", "এমআরআই স্ক্যান", "Radiology", "রেডিওলজি ও ইমেজিং"),
            AbbrData("PET", "Positron Emission Tomography", "পেট স্ক্যান (ক্যানসার নির্ণয়)", "Radiology", "রেডিওলজি ও ইমেজিং"),
            AbbrData("ECG / EKG", "Electrocardiogram", "ইলেকট্রোকার্ডিওগ্রাম (ইসিজি)", "Radiology", "রেডিওলজি ও ইমেজিং"),
            AbbrData("EEG", "Electroencephalogram", "মস্তিষ্কের ইইজি পরীক্ষা", "Radiology", "রেডিওলজি ও ইমেজিং"),
            AbbrData("Echo", "Echocardiogram", "ইকোকার্ডিওগ্রাম (হৃদপিণ্ডের ছবি)", "Radiology", "রেডিওলজি ও ইমেজিং"),
            AbbrData("KUB", "Kidneys, Ureters, and Bladder X-Ray", "কিডনি ও মূত্রনালীর এক্স-রে", "Radiology", "রেডিওলজি ও ইমেজিং")
        )
    }

    val filteredList = remember(searchQuery, isBn) {
        if (searchQuery.isBlank()) allAbbrs
        else allAbbrs.filter { 
            it.term.contains(searchQuery, ignoreCase = true) || 
            it.fullFormEn.contains(searchQuery, ignoreCase = true) ||
            it.fullFormBn.contains(searchQuery, ignoreCase = true) ||
            it.categoryEn.contains(searchQuery, ignoreCase = true) ||
            it.categoryBn.contains(searchQuery, ignoreCase = true)
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
                                text = stringResource(id = R.string.tool_medical_abbr),
                                fontSize = 18.sp,
                                color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1A237E),
                                fontWeight = FontWeight.Black
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBack()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    tint = if (isSystemInDarkTheme()) Color.White else Color(0xFF1A237E)
                                )
                            }
                        },
                        actions = { LanguageToggleButton() },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (isBn) "সংক্ষিপ্ত রূপ খুঁজুন (যেমন: ICU, CBC, BID, NPO)..." else "Search abbreviation (e.g. ICU, CBC, BID)...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF0284C7)) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
                            unfocusedTextColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
                            focusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            unfocusedBorderColor = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.3f) else Color.LightGray
                        )
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)
            ) {
                items(filteredList) { abbr ->
                    AbbrCard(abbr = abbr, isDark = isSystemInDarkTheme(), isBn = isBn)
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun AbbrCard(abbr: AbbrData, isDark: Boolean, isBn: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color(0xFF0284C7).copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = abbr.term,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = if (isBn) abbr.categoryBn else abbr.categoryEn,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isBn) abbr.fullFormBn else abbr.fullFormEn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
