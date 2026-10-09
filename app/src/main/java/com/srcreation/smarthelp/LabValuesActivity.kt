package com.srcreation.smarthelp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.LabRecordEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.text.SimpleDateFormat
import java.util.*

class LabValuesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                LabValuesScreen(onBack = { finish() })
            }
        }
    }
}

data class LabTestData(
    val nameEn: String,
    val nameBn: String,
    val minVal: Double,
    val maxVal: Double,
    val unit: String,
    val categoryEn: String,
    val categoryBn: String,
    val clinicalNoteEn: String = "",
    val clinicalNoteBn: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabValuesScreen(viewModel: LabValuesViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Catalog, 1 = Trends
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialogForTest by remember { mutableStateOf<LabTestData?>(null) }

    val myRecords by viewModel.allRecords.collectAsState(initial = emptyList())
    val isBn = Locale.getDefault().language == "bn"

    val allTests = remember {
        listOf(
            // 1. Hematology & Iron Profile
            LabTestData("Hemoglobin (Male)", "হিমোগ্লোবিন (পুরুষ)", 13.5, 17.5, "g/dL", "Hematology", "হেম্যাটোলজি (রক্তকণিকা)", "Indicates O2 carrying capacity. Low = Anemia; High = Polycythemia.", "রক্তাল্পতা (Anemia) বা পলিসাইথেমিয়া চিহ্নিত করে।"),
            LabTestData("Hemoglobin (Female)", "হিমোগ্লোবিন (নারী)", 12.0, 15.5, "g/dL", "Hematology", "হেম্যাটোলজি (রক্তকণিকা)", "Indicates O2 carrying capacity. Low = Anemia.", "কম হলে রক্তাল্পতা (Anemia) নির্দেশ করে।"),
            LabTestData("WBC Count (Total)", "শ্বেত রক্তকণিকা (TLC)", 4500.0, 11000.0, "/mcL", "Hematology", "হেম্যাটোলজি (রক্তকণিকা)", "High = Infection/Inflammation; Low = Immunosuppression.", "বেশি হলে ইনফেকশন/প্রদাহ; কম হলে রোগপ্রতিরোধ ক্ষমতা হ্রাস।"),
            LabTestData("Platelet Count", "প্লেটলেট / অনুচক্রিকা", 150000.0, 450000.0, "/mcL", "Hematology", "হেম্যাটোলজি (রক্তকণিকা)", "Crucial for blood clotting. Low = Dengue/Bleeding risk.", "রক্ত জমাট বাঁধতে সাহায্য করে। ডেঙ্গুতে কমতে পারে।"),
            LabTestData("ESR (Male)", "ইএসআর (পুরুষ)", 0.0, 15.0, "mm/1st hr", "Hematology", "হেম্যাটোলজি (রক্তকণিকা)", "Marker for chronic inflammation or systemic disease.", "দীর্ঘস্থায়ী প্রদাহ বা সংক্রমণের মার্কার।"),
            LabTestData("ESR (Female)", "ইএসআর (নারী)", 0.0, 20.0, "mm/1st hr", "Hematology", "হেম্যাটোলজি (রক্তকণিকা)", "Marker for chronic inflammation or systemic disease.", "দীর্ঘস্থায়ী প্রদাহ বা সংক্রমণের মার্কার।"),
            LabTestData("Serum Ferritin", "সিরাম ফেরিটিন (আয়রন সঞ্চয়)", 20.0, 250.0, "ng/mL", "Hematology", "হেম্যাটোলজি (রক্তকণিকা)", "Reflects total iron stores in the body.", "শরীরে আয়রনের সঞ্চিত পরিমাণ নির্দেশ করে।"),

            // 2. Coagulation & Bleeding Profile
            LabTestData("Prothrombin Time (PT)", "প্রোথ্রম্বিন টাইম (PT)", 11.0, 13.5, "seconds", "Coagulation Profile", "রক্ত জমাট বাঁধার পরীক্ষা", "Extrinsic clotting pathway. High = Warfarin use or liver failure.", "রক্ত জমাট বাঁধার ধীরগতি নির্দেশ করে।"),
            LabTestData("INR (Warfarin Monitor)", "আইএনআর (INR Value)", 0.8, 1.1, "ratio", "Coagulation Profile", "রক্ত জমাট বাঁধার পরীক্ষা", "Target 2.0-3.0 for Warfarin therapy.", "ওয়ারফারিন ওষুধের মাত্রা পর্যবেক্ষণে ব্যবহৃত হয়।"),
            LabTestData("APTT (Coagulation)", "এপিটিটি (APTT)", 25.0, 35.0, "seconds", "Coagulation Profile", "রক্ত জমাট বাঁধার পরীক্ষা", "Intrinsic clotting pathway. Monitors Heparin therapy.", "হেপারিন ইনজেকশনের কার্যকারিতা পর্যবেক্ষণে ব্যবহৃত হয়।"),
            LabTestData("D-Dimer (Thrombosis)", "ডি-ডাইমার (D-Dimer)", 0.0, 0.5, "mcg/mL", "Coagulation Profile", "রক্ত জমাট বাঁধার পরীক্ষা", "High = Pulmonary Embolism, DVT, or DIC clot formation.", "বেশি হলে পালমোনারি এমবলিজম বা ডিভিটি রক্ত জমাট বাঁধা নির্দেশ করে।"),

            // 3. Biochemistry & Diabetes
            LabTestData("Fasting Blood Glucose", "ফাস্টিং ব্লাড সুগার (খালি পেটে)", 70.0, 99.0, "mg/dL", "Biochemistry & Diabetes", "বায়োকেমিস্ট্রি ও ডায়াবেটিস", "70-99 Normal, 100-125 Prediabetes, >=126 Diabetes.", "১০০-১২৫ প্রি-ডায়াবেটিস, ১২৬ বা বেশি হলে ডায়াবেটিস।"),
            LabTestData("2h Postprandial Glucose", "খাওয়ার ২ ঘন্টা পর সুগার", 70.0, 139.0, "mg/dL", "Biochemistry & Diabetes", "বায়োকেমিস্ট্রি ও ডায়াবেটিস", "140-199 Impaired Glucose Tolerance, >=200 Diabetes.", "১৪০-১৯৯ ইমপেয়ার্ড গ্লুকোজ, ২০০ এর বেশি হলে ডায়াবেটিস।"),
            LabTestData("HbA1c (Glycated Hb)", "এইচবিএ১সি (৩ মাসের গড় সুগার)", 4.0, 5.6, "%", "Biochemistry & Diabetes", "বায়োকেমিস্ট্রি ও ডায়াবেটিস", "<5.7% Normal, 5.7-6.4% Prediabetes, >=6.5% Diabetes.", "৫.৭-৬.৪% প্রি-ডায়াবেটিস, ৬.৫% বা বেশি হলে ডায়াবেটিস।"),
            LabTestData("Serum Creatinine (Male)", "সিরাম ক্রিয়েটিনিন (পুরুষ)", 0.7, 1.3, "mg/dL", "Biochemistry & Diabetes", "বায়োকেমিস্ট্রি ও ডায়াবেটিস", "Kidney function indicator. High = Impaired renal function.", "কিডনির কর্মক্ষমতা সূচক। বেশি হলে কিডনি জটিলতা।"),
            LabTestData("Serum Creatinine (Female)", "সিরাম ক্রিয়েটিনিন (নারী)", 0.6, 1.1, "mg/dL", "Biochemistry & Diabetes", "বায়োকেমিস্ট্রি ও ডায়াবেটিস", "Kidney function indicator. High = Impaired renal function.", "কিডনির কর্মক্ষমতা সূচক। বেশি হলে কিডনি জটিলতা।"),
            LabTestData("Blood Urea Nitrogen (BUN)", "ব্লাড ইউরিয়া নাইট্রোজেন", 7.0, 20.0, "mg/dL", "Biochemistry & Diabetes", "বায়োকেমিস্ট্রি ও ডায়াবেটিস", "High = Dehydration, Renal failure, or High protein intake.", "ডিহাইড্রেশন বা কিডনি বৈকল্যে বৃদ্ধি পায়।"),
            LabTestData("Serum Uric Acid", "সিরাম ইউরিক এসিড", 3.5, 7.2, "mg/dL", "Biochemistry & Diabetes", "বায়োকেমিস্ট্রি ও ডায়াবেটিস", "High = Gout risk / Kidney stones.", "বেশি হলে গাউট (গাউটি আর্থ্রাইটিস) বা কিডনি স্টোন ঝুঁকি।"),

            // 4. Electrolytes & Minerals
            LabTestData("Sodium (Na+)", "সোডিয়াম (Na+)", 135.0, 145.0, "mEq/L", "Electrolytes & Minerals", "ইলেক্ট্রোলাইটস ও খনিজ", "Critical for fluid balance. Low = Hyponatremia (confusion/seizure).", "শরীরের তরল ভারসাম্য বজায় রাখে। কম হলে বিভ্রান্তি/খিঁচুনি।"),
            LabTestData("Potassium (K+)", "পটাশিয়াম (K+)", 3.5, 5.1, "mEq/L", "Electrolytes & Minerals", "ইলেক্ট্রোলাইটস ও খনিজ", "Critical for cardiac rhythm. High/Low = Arrhythmia risk.", "হৃদস্পন্দন নিয়ন্ত্রণ করে। অনিয়মিত পটাশিয়াম বিপজ্জনক।"),
            LabTestData("Chloride (Cl-)", "ক্লোরাইড (Cl-)", 96.0, 106.0, "mEq/L", "Electrolytes & Minerals", "ইলেক্ট্রোলাইটস ও খনিজ", "Helps maintain acid-base balance.", "শরীরের এসিড-ক্ষার ভারসাম্য বজায় রাখে।"),
            LabTestData("Serum Calcium (Total)", "সিরাম ক্যালসিয়াম", 8.5, 10.2, "mg/dL", "Electrolytes & Minerals", "ইলেক্ট্রোলাইটস ও খনিজ", "Essential for bone & muscle nerve conduction.", "অস্থি, পেশী ও স্নায়ু সংকেত পরিবহনে গুরুত্বপূর্ণ।"),
            LabTestData("Serum Phosphate", "সিরাম ফসফেট", 2.5, 4.5, "mg/dL", "Electrolytes & Minerals", "ইলেক্ট্রোলাইটস ও খনিজ", "Bone mineralization and cellular energy.", "হাড় গঠন ও কোষীয় শক্তির জন্য প্রয়োজনীয়।"),
            LabTestData("Vitamin D (25-OH)", "ভিটিমিন ডি (25-OH Vitamin D)", 30.0, 100.0, "ng/mL", "Electrolytes & Minerals", "ইলেক্ট্রোলাইটস ও খনিজ", "Deficiency (<20 ng/mL) causes bone weakness & osteomalacia.", "কম হলে হাড়ের দুর্বলতা ও অস্টিওম্যালেসিয়া হয়।"),
            LabTestData("Vitamin B12", "ভিটামিন বি১২ (Cobalamin)", 200.0, 900.0, "pg/mL", "Electrolytes & Minerals", "ইলেক্ট্রোলাইটস ও খনিজ", "Low = Megaloblastic anemia & peripheral neuropathy.", "কম হলে রক্তশূন্যতা ও স্নায়ুবিক দুর্বলতা দেখা দেয়।"),

            // 5. Liver & Pancreas Function Tests
            LabTestData("ALT (SGPT)", "এএলটি / এসজিপিটি", 7.0, 56.0, "U/L", "Liver & Pancreas", "যকৃত ও অগ্ন্যাশয়", "Specific marker for liver cell injury (Hepatitis/Fatty liver).", "হেপাটাইটিস বা ফ্যাটি লিভারে বৃদ্ধি পায়।"),
            LabTestData("AST (SGOT)", "এএসটি / এসজিওটি", 8.0, 48.0, "U/L", "Liver & Pancreas", "যকৃত ও অগ্ন্যাশয়", "Enzyme found in liver, heart, and muscle cells.", "লিভার, হৃদপিণ্ড বা পেশীর ক্ষতিতে বৃদ্ধি পায়।"),
            LabTestData("Total Bilirubin", "টোটাল বিলিরুবিন", 0.1, 1.2, "mg/dL", "Liver & Pancreas", "যকৃত ও অগ্ন্যাশয়", "High = Jaundice / Hemolysis / Biliary obstruction.", "বেশি হলে জন্ডিস বা পিত্তনালীর বাধা নির্দেশ করে।"),
            LabTestData("Serum Albumin", "সিরাম অ্যালবুমিন", 3.5, 5.5, "g/dL", "Liver & Pancreas", "যকৃত ও অগ্ন্যাশয়", "Main blood protein. Low = Malnutrition / Cirrhosis.", "লিভার প্রোটিন। কম হলে পুষ্টিহীনতা বা লিভার সিরোসিস।"),
            LabTestData("Serum Amylase", "সিরাম অ্যামাইলেজ (প্যানক্রিয়াস)", 30.0, 110.0, "U/L", "Liver & Pancreas", "যকৃত ও অগ্ন্যাশয়", "High = Acute pancreatitis.", "বেশি হলে তীব্র অগ্ন্যাশয় প্রদাহ (Acute Pancreatitis) নির্দেশ করে।"),
            LabTestData("Serum Lipase", "সিরাম লাইপেজ (প্যানক্রিয়াস)", 10.0, 140.0, "U/L", "Liver & Pancreas", "যকৃত ও অগ্ন্যাশয়", "Highly specific marker for acute pancreatitis.", "অগ্ন্যাশয় প্রদাহের অত্যন্ত নিখুঁত স্পেসিফিক মার্কার।"),

            // 6. Lipid Profile
            LabTestData("Total Cholesterol", "টোটাল কোলেস্টেরল", 100.0, 199.0, "mg/dL", "Lipid Profile (Blood Fat)", "লিপিড প্রফাইল (রক্তে চর্বি)", "Desirable: <200 mg/dL. High = Cardiovascular risk.", "২০০ এর নিচে থাকা বাঞ্ছনীয়। বেশি হলে হৃদরোগ ঝুঁকি।"),
            LabTestData("HDL Cholesterol (Good)", "এইচডিএল কোলেস্টেরল (ভালো)", 40.0, 60.0, "mg/dL", "Lipid Profile (Blood Fat)", "লিপিড প্রফাইল (রক্তে চর্বি)", "Cardioprotective. Higher level is desirable.", "হৃদপিণ্ডকে সুরক্ষা দেয়। বেশি থাকা ভালো।"),
            LabTestData("LDL Cholesterol (Bad)", "এলডিএল কোলেস্টেরল (খারাপ)", 50.0, 99.0, "mg/dL", "Lipid Profile (Blood Fat)", "লিপিড প্রফাইল (রক্তে চর্বি)", "Atherogenic. Optimal: <100 mg/dL.", "ধমনীতে চর্বি জমায়। ১০০ এর নিচে থাকা ভালো।"),
            LabTestData("Triglycerides", "ট্রাইগ্লিসারাইডস", 50.0, 149.0, "mg/dL", "Lipid Profile (Blood Fat)", "লিপিড প্রফাইল (রক্তে চর্বি)", "Normal: <150 mg/dL. High = Pancreatitis/Heart risk.", "১৫০ এর নিচে স্বাভাবিক। বেশি হলে প্যানক্রিয়াস প্রদাহ ঝুঁকি।"),

            // 7. Thyroid & Hormones
            LabTestData("TSH (Thyroid Stimulating)", "টিএসএইচ (TSH)", 0.4, 4.0, "mIU/L", "Thyroid & Hormones", "থাইরয়েড ও হরমোন", "High = Hypothyroidism; Low = Hyperthyroidism.", "বেশি হলে হাইপোথাইরয়েডিজম; কম হলে হাইপারথাইরয়েডিজম।"),
            LabTestData("Free T4 (FT4)", "ফ্রি টি৪ (FT4)", 0.8, 1.8, "ng/dL", "Thyroid & Hormones", "থাইরয়েড ও হরমোন", "Active thyroid hormone level.", "সক্রিয় থাইরয়েড হরমোনের মাত্রা।"),
            LabTestData("Serum Prolactin", "সিরাম প্রোল্যাকটিন", 2.0, 18.0, "ng/mL", "Thyroid & Hormones", "থাইরয়েড ও হরমোন", "High = Hyperprolactinemia, pituitary adenoma.", "বেশি হলে অনিয়মিত পিরিয়ড বা পিটুইটারি টিউমার ঝুঁকি।"),

            // 8. Cardiac & Sepsis Markers
            LabTestData("hs-CRP (Inflammation)", "সি-রিয়্যাক্টিভ প্রোটিন (hs-CRP)", 0.0, 1.0, "mg/L", "Cardiac & Sepsis Markers", "কার্ডিয়াক ও সেপসিস মার্কার", "Acute phase reactant. High = Infection or Cardiac risk.", "তীব্র ইনফেকশন বা হার্ট অ্যাটাকের ঝুঁকি মার্কার।"),
            LabTestData("Troponin I (High Sens)", "ট্রোপোনিন আই (Troponin I)", 0.0, 0.04, "ng/mL", "Cardiac & Sepsis Markers", "কার্ডিয়াক ও সেপসিস মার্কার", "Gold standard marker for Myocardial Infarction (Heart Attack).", "হার্ট অ্যাটাক (মায়োকার্ডিয়াল ইনফার্কশন) শনাক্তকরণের গোল্ড স্ট্যান্ডার্ড।"),
            LabTestData("NT-proBNP (Heart Failure)", "এনটি-প্রোবিএনপি (Heart Failure)", 0.0, 125.0, "pg/mL", "Cardiac & Sepsis Markers", "কার্ডিয়াক ও সেপসিস মার্কার", "High = Acute or chronic Heart Failure.", "বেশি হলে হার্ট ফেলিয়ার (Heart Failure) নির্দেশ করে।"),
            LabTestData("Procalcitonin (Sepsis)", "প্রোক্যালসিটোনিন (Sepsis)", 0.0, 0.15, "ng/mL", "Cardiac & Sepsis Markers", "কার্ডিয়াক ও সেপসিস মার্কার", "Specific marker for systemic bacterial infection & sepsis.", "রক্তে তীব্র ব্যাকটেরিয়াল ইনফেকশন ও সেপসিস শনাক্ত করে।"),

            // 9. Arterial Blood Gas (ABG - ICU Reference)
            LabTestData("Arterial pH (ABG)", "রক্তের পিএইচ (Arterial pH)", 7.35, 7.45, "pH", "Arterial Blood Gas (ABG)", "এবিজি (ABG - আইসিইউ রেফারেন্স)", "Acid-base indicator. <7.35 Acidosis, >7.45 Alkalosis.", "<৭.৩৫ অ্যাসিডোসিস, >৭.৪৫ অ্যালকালোসিস।"),
            LabTestData("pCO2 (Carbon Dioxide)", "পিএইচ কার্বন ডাই অক্সাইড (pCO2)", 35.0, 45.0, "mmHg", "Arterial Blood Gas (ABG)", "এবিজি (ABG - আইসিইউ রেফারেন্স)", "Respiratory component of acid-base.", "শ্বসন সংক্রান্ত এসিড-ক্ষার ব্যালেন্স নির্দেশ করে।"),
            LabTestData("HCO3 (Bicarbonate)", "বাইকার্বোনেট (HCO3)", 22.0, 26.0, "mEq/L", "Arterial Blood Gas (ABG)", "এবিজি (ABG - আইসিইউ রেফারেন্স)", "Metabolic component of acid-base.", "মেটাবলিক এসিড-ক্ষার ব্যালেন্স নির্দেশ করে।"),

            // 10. Urine Routine & Kidney Microalbumin
            LabTestData("Urine Specific Gravity", "প্রস্রাবের স্পেসিফিক গ্র্যাভিটি", 1.005, 1.030, "ratio", "Urine Examination", "প্রস্রাব পরীক্ষা (Urine R/E)", "Indicates urine concentration ability.", "প্রস্রাবের ঘনত্ব ও কিডনির জল সমতা নির্দেশ করে।"),
            LabTestData("Urine Microalbumin", "প্রস্রাবে মাইক্রোঅ্যালবুমিন", 0.0, 30.0, "mg/24hr", "Urine Examination", "প্রস্রাব পরীক্ষা (Urine R/E)", "Early marker for diabetic nephropathy.", "ডায়াবেটিক রোগীদের কিডনি ক্ষতির শুরুর লক্ষণ।")
        )
    }

    val filteredList = remember(searchQuery, isBn) {
        if (searchQuery.isBlank()) allTests
        else allTests.filter { 
            it.nameEn.contains(searchQuery, ignoreCase = true) || 
            it.nameBn.contains(searchQuery, ignoreCase = true) ||
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
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.tool_lab_values),
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
            }
        },
        bottomBar = { AppBottomBar(context = context, currentTab = -1, isNurseMode = true, backgroundBrush = AppGradientBrush) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = AppGradientBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Tab Row (0: Catalog, 1: My History / Trends)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = if (isSystemInDarkTheme()) Color(0xFF0F172A).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
                    contentColor = if (isSystemInDarkTheme()) Color(0xFF38BDF8) else Color(0xFF1A237E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedTab = 0
                        },
                        text = {
                            Text(
                                if (isBn) "ল্যাব টেস্ট ক্যাটালগ" else "Reference Catalog",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedTab = 1
                        },
                        text = {
                            Text(
                                if (isBn) "আমার টেস্ট ট্র্যান্ডস" else "My Value Trends",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Catalog Tab
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (isBn) "ল্যাব টেস্ট বা ক্যাটাগরি খুঁজুন (যেমন: Hb, Creatinine, Troponin)..." else "Search test (e.g. Hb, Creatinine, Troponin)...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF0284C7)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
                            unfocusedTextColor = if (isSystemInDarkTheme()) Color.White else Color.Black,
                            focusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            unfocusedBorderColor = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.3f) else Color.LightGray
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredList) { test ->
                            LabTestCatalogCard(
                                test = test,
                                isDark = isSystemInDarkTheme(),
                                isBn = isBn,
                                onAddRecord = { showAddDialogForTest = test }
                            )
                        }
                    }
                } else {
                    // Trends Tab
                    if (myRecords.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBn) "কোনো সংরক্ষিত টেস্ট রেকর্ড পাওয়া যায়নি। ক্যাটালগ থেকে '+' বাটন চেপে টেস্ট রেকর্ড যুক্ত করুন।" else "No saved lab records found. Tap '+' on any catalog item to add your values.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val grouped = myRecords.groupBy { it.testName }
                            items(grouped.keys.toList()) { testName ->
                                val records = grouped[testName] ?: emptyList()
                                LabTrendCard(
                                    testName = testName,
                                    records = records,
                                    isDark = isSystemInDarkTheme(),
                                    isBn = isBn,
                                    onDelete = { record -> viewModel.deleteRecord(record) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Value Dialog
    showAddDialogForTest?.let { test ->
        var inputVal by remember { mutableStateOf("") }
        val isDark = isSystemInDarkTheme()

        AlertDialog(
            onDismissRequest = { showAddDialogForTest = null },
            title = {
                Text(
                    text = if (isBn) "${test.nameBn} রেকর্ড যুক্ত করুন" else "Log Value for ${test.nameEn}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isDark) Color.White else Color(0xFF1A237E)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isBn) "স্বাভাবিক মাত্রা: ${test.minVal} - ${test.maxVal} ${test.unit}" else "Normal Range: ${test.minVal} - ${test.maxVal} ${test.unit}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = { inputVal = it },
                        label = { Text(if (isBn) "আপনার টেস্টের প্রাপ্ত মান (${test.unit})" else "Observed Value (${test.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = inputVal.toDoubleOrNull()
                        if (num != null) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.addRecord(
                                testName = if (isBn) test.nameBn else test.nameEn,
                                resultValue = num,
                                unit = test.unit,
                                normalMin = test.minVal,
                                normalMax = test.maxVal,
                                date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
                            )
                            Toast.makeText(context, if (isBn) "টেস্ট মান সংরক্ষিত হয়েছে!" else "Record saved successfully!", Toast.LENGTH_SHORT).show()
                            showAddDialogForTest = null
                        } else {
                            Toast.makeText(context, if (isBn) "সঠিক সংখ্যা লিখুন" else "Enter valid number", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text(if (isBn) "সংরক্ষণ করুন" else "Save Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialogForTest = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = if (isDark) Color(0xFF0F172A) else Color.White
        )
    }
}

@Composable
fun LabTestCatalogCard(
    test: LabTestData,
    isDark: Boolean,
    isBn: Boolean,
    onAddRecord: () -> Unit
) {
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
                Text(
                    text = if (isBn) test.categoryBn else test.categoryEn,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isBn) test.nameBn else test.nameEn,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (isDark) Color.White else Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isBn) "স্বাভাবিক মাত্রা: ${test.minVal} - ${test.maxVal} ${test.unit}" else "Ref Range: ${test.minVal} - ${test.maxVal} ${test.unit}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                if (test.clinicalNoteBn.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isBn) test.clinicalNoteBn else test.clinicalNoteEn,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 15.sp
                    )
                }
            }

            IconButton(
                onClick = onAddRecord,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0284C7).copy(alpha = 0.15f))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun LabTrendCard(
    testName: String,
    records: List<LabRecordEntity>,
    isDark: Boolean,
    isBn: Boolean,
    onDelete: (LabRecordEntity) -> Unit
) {
    val sorted = remember(records) { records.sortedBy { it.timestamp } }
    val latest = sorted.lastOrNull()

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Column {
                    Text(text = testName, fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                    Text(
                        text = if (isBn) "সর্বশেষ টেস্ট: ${latest?.resultValue ?: "--"} ${latest?.unit ?: ""} (${latest?.date ?: ""})" else "Latest: ${latest?.resultValue ?: "--"} ${latest?.unit ?: ""} (${latest?.date ?: ""})",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                Icon(Icons.Default.ShowChart, contentDescription = null, tint = Color(0xFF0284C7))
            }

            if (sorted.size >= 2) {
                Spacer(modifier = Modifier.height(16.dp))
                // Mini Trend Line Chart
                val points = sorted.map { it.resultValue.toFloat() }
                val minP = points.minOrNull() ?: 0f
                val maxP = points.maxOrNull() ?: 100f
                val rangeP = if (maxP == minP) 1f else (maxP - minP)

                Canvas(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                    val width = size.width
                    val height = size.height
                    val stepX = width / (points.size - 1).coerceAtLeast(1)

                    val path = Path()
                    points.forEachIndexed { index, value ->
                        val x = index * stepX
                        val y = height - ((value - minP) / rangeP * (height - 20f) + 10f)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(color = Color(0xFF0284C7), radius = 5f, center = Offset(x, y))
                    }
                    drawPath(path = path, color = Color(0xFF0284C7), style = Stroke(width = 3.dp.toPx()))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Record Log Items
            records.forEach { record ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${record.resultValue} ${record.unit}  •  ${record.date}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.DarkGray
                    )
                    IconButton(onClick = { onDelete(record) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
