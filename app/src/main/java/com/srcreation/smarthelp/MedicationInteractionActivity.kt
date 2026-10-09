package com.srcreation.smarthelp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import com.srcreation.smarthelp.ui.theme.isAppInDarkTheme
import java.util.Locale

class MedicationInteractionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                MedicationInteractionScreen(onBack = { finish() })
            }
        }
    }
}

data class InternationalDrug(
    val name: String,
    val generic: String,
    val form: String, // Tab, Cap, Inj, Lotion, Inhaler, Syrup, Eye Drop, IV Fluid
    val category: String
)

data class ClinicalDrugInteraction(
    val drug1: String,
    val drug2: String,
    val severity: String, // SEVERE, MODERATE, MINOR
    val mechanismEn: String,
    val mechanismBn: String,
    val nursingActionEn: String,
    val nursingActionBn: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationInteractionScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isAppInDarkTheme()
    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn" ) }

    val allDrugs = listOf(
        // Tablets & Capsules
        InternationalDrug("Aspirin", "Acetylsalicylic Acid", "Tab", "Antiplatelet / Analgesic"),
        InternationalDrug("Ibuprofen", "Ibuprofen", "Tab", "NSAID"),
        InternationalDrug("Diclofenac", "Diclofenac Sodium", "Tab", "NSAID"),
        InternationalDrug("Paracetamol", "Acetaminophen", "Tab/Syrup", "Analgesic"),
        InternationalDrug("Warfarin", "Warfarin Sodium", "Tab", "Anticoagulant"),
        InternationalDrug("Clopidogrel", "Clopidogrel", "Tab", "Antiplatelet"),
        InternationalDrug("Metformin", "Metformin HCl", "Tab", "Antidiabetic"),
        InternationalDrug("Gliclazide", "Gliclazide", "Tab", "Antidiabetic"),
        InternationalDrug("Losartan", "Losartan Potassium", "Tab", "Antihypertensive"),
        InternationalDrug("Amlodipine", "Amlodipine Besylate", "Tab", "Antihypertensive"),
        InternationalDrug("Metoprolol", "Metoprolol Succinate", "Tab", "Beta Blocker"),
        InternationalDrug("Propranolol", "Propranolol HCl", "Tab", "Beta Blocker"),
        InternationalDrug("Omeprazole", "Omeprazole", "Cap", "PPI"),
        InternationalDrug("Pantoprazole", "Pantoprazole", "Tab", "PPI"),
        InternationalDrug("Atorvastatin", "Atorvastatin", "Tab", "Statin"),
        InternationalDrug("Digoxin", "Digoxin", "Tab", "Cardiac Glycoside"),
        InternationalDrug("Prednisolone", "Prednisolone", "Tab", "Corticosteroid"),
        InternationalDrug("Azithromycin", "Azithromycin", "Tab/Susp", "Antibiotic"),
        InternationalDrug("Ciprofloxacin", "Ciprofloxacin", "Tab", "Antibiotic"),
        InternationalDrug("Doxycycline", "Doxycycline", "Cap", "Antibiotic"),
        InternationalDrug("Fluconazole", "Fluconazole", "Cap", "Antifungal"),
        InternationalDrug("Montelukast", "Montelukast", "Tab", "Antiasthmatic"),
        InternationalDrug("Cetirizine", "Cetirizine HCl", "Tab", "Antihistamine"),

        // Injections & Critical Care (Inj)
        InternationalDrug("Insulin Regular", "Human Insulin", "Inj", "Antidiabetic"),
        InternationalDrug("Insulin Glargine", "Long-acting Insulin", "Inj", "Antidiabetic"),
        InternationalDrug("Furosemide (Lasix)", "Furosemide", "Inj/Tab", "Diuretic"),
        InternationalDrug("Gentamicin", "Gentamicin Sulfate", "Inj", "Aminoglycoside Antibiotic"),
        InternationalDrug("Ceftriaxone", "Ceftriaxone Sodium", "Inj", "Cephalosporin Antibiotic"),
        InternationalDrug("Ampicillin", "Ampicillin", "Inj", "Antibiotic"),
        InternationalDrug("Heparin", "Unfractionated Heparin", "Inj", "Anticoagulant"),
        InternationalDrug("Enoxaparin", "LMWH", "Inj", "Anticoagulant"),
        InternationalDrug("Morphine", "Morphine Sulfate", "Inj", "Opioid Analgesic"),
        InternationalDrug("Pethidine", "Meperidine", "Inj", "Opioid Analgesic"),
        InternationalDrug("Adrenaline", "Epinephrine", "Inj", "Sympathomimetic / Anaphylaxis"),
        InternationalDrug("Noradrenaline", "Norepinephrine", "Inj", "Vasopressor"),
        InternationalDrug("Dopamine", "Dopamine HCl", "Inj", "Inotrope"),
        InternationalDrug("Dobutamine", "Dobutamine", "Inj", "Inotrope"),
        InternationalDrug("Midazolam", "Midazolam", "Inj", "Benzodiazepine Sedative"),
        InternationalDrug("Diazepam", "Diazepam", "Inj/Tab", "Benzodiazepine"),
        InternationalDrug("Potassium Chloride (KCL)", "KCL Concentrate", "Inj/IV", "Electrolyte"),
        InternationalDrug("Methotrexate", "Methotrexate", "Inj/Tab", "Antimetabolite / DMARD"),

        // Topical, Inhalers & IV Fluids
        InternationalDrug("Hydrocortisone", "Hydrocortisone", "Lotion/Oint", "Topical Steroid"),
        InternationalDrug("Betamethasone", "Betamethasone", "Lotion/Cream", "Topical Steroid"),
        InternationalDrug("Salbutamol", "Albuterol", "Inhaler", "Bronchodilator"),
        InternationalDrug("Budesonide", "Budesonide", "Inhaler", "Inhaled Steroid"),
        InternationalDrug("Ciprofloxacin Drops", "Ciprofloxacin 0.3%", "Eye Drop", "Ophthalmic Antibiotic"),
        InternationalDrug("Normal Saline 0.9%", "0.9% NaCl", "IV Fluid", "Crystalloid Fluid"),
        InternationalDrug("Ringer's Lactate (RL)", "Hartmann's Solution", "IV Fluid", "Crystalloid Fluid")
    )

    val clinicalInteractions = listOf(
        ClinicalDrugInteraction("Gentamicin", "Furosemide (Lasix)", "SEVERE",
            "Concurrent use severely increases risks of ototoxicity (hearing loss) and acute renal failure.",
            "একসঙ্গে ব্যবহারে শ্রবণশক্তি বিনাশ (Ototoxicity) এবং কিডনি বিকল (Acute Renal Failure) হওয়ার মারাত্মক ঝুঁকি বাড়ে।",
            "Monitor serum creatinine, urine output, and audiometry. Avoid co-administration if possible.",
            "ইউরিন আউটপুট, ক্রিয়েটিনিন পরীক্ষা করুন এবং সম্ভব হলে একসঙ্গে ব্যবহার এড়িয়ে চলুন।"),

        ClinicalDrugInteraction("Ceftriaxone", "Ringer's Lactate (RL)", "SEVERE",
            "Risk of fatal calcium-ceftriaxone precipitation in lungs and kidneys, especially in neonates and IV lines.",
            "আইভি লাইনে বা শরীরে প্রাণঘাতী ক্যালসিয়াম-সেফট্রিয়াক্সন ক্রিস্টাল জমাট বাঁধার ঝুঁকি থাকে।",
            "Never mix or administer Ceftriaxone with Calcium/RL solutions in the same IV line.",
            "একই আইভি লাইনে সেফট্রিয়াক্সন এবং আরএল/ক্যালসিয়াম দ্রবণ কখনোই মেশাবেন না।"),

        ClinicalDrugInteraction("Aspirin", "Warfarin", "SEVERE",
            "Potent synergistic antiplatelet and anticoagulant effect causing high risk of fatal internal hemorrhage.",
            "রক্ত জমাট বাঁধা মারাত্মকভাবে বন্ধ হয়ে জীবনঘাতী অভ্যন্তরীণ রক্তক্ষরণের ঝুঁকি বাড়ে।",
            "Monitor INR closely. Check for signs of GI bleeding, hematuria, or hematoma.",
            "আইএনআর (INR) এবং রক্তক্ষরণের লক্ষণ (কালো পায়খানা, প্রস্রাবে রক্ত) সতর্কতার সাথে পর্যবেক্ষণ করুন।"),

        ClinicalDrugInteraction("Adrenaline", "Propranolol", "SEVERE",
            "Blockade of beta-2 receptors leaves alpha-1 receptors unopposed, causing severe hypertensive crisis and bradycardia.",
            "বিটা রিসেপ্টর ব্লক থাকায় রক্তচাপ বিপজ্জনকভাবে বৃদ্ধি পাবে (Hypertensive Crisis) এবং হার্ট রেট অতিরিক্ত কমে যাবে।",
            "Avoid combination; use selective beta-blockers or alternative vasopressors under continuous ECG monitoring.",
            "একসঙ্গে ব্যবহার এড়ান এবং অনবরত ইসিজি ও রক্তচাপ মনিটর করুন।"),

        ClinicalDrugInteraction("Midazolam", "Morphine", "SEVERE",
            "Profound additive central nervous system and respiratory depression.",
            "কেন্দ্রীয় স্নায়ুতন্ত্র ও শ্বাসযন্ত্রের মারাত্মক পতন (Respiratory Depression) হতে পারে।",
            "Keep Naloxone and Flumazenil available. Continuous SpO2 and capnography monitoring required.",
            "SpO2 ও শ্বাসের গতি মনিটর করুন এবং ন্যালোক্সোন/ফ্লুমাজেনিল প্রস্তুত রাখুন।"),

        ClinicalDrugInteraction("Digoxin", "Furosemide (Lasix)", "MODERATE",
            "Furosemide-induced hypokalemia significantly increases Digoxin toxicity and fatal cardiac arrhythmias.",
            "ফুরোসেমাইডজনিত পটাশিয়াম কমে যাওয়ায় ডিগক্সিন পয়জনিং ও হার্ট অ্যাটাকের মারাত্মক ঝুঁকি তৈরি হয়।",
            "Monitor serum potassium levels regularly. Administer oral/IV potassium supplements if K+ < 3.5 mEq/L.",
            "রক্তের পটাশিয়াম লেভেল চেক করুন এবং প্রয়োজন হলে পটাশিয়াম সাপ্লিমেন্ট দিন।"),

        ClinicalDrugInteraction("Potassium Chloride (KCL)", "Losartan", "MODERATE",
            "Increased risk of severe hyperkalemia leading to cardiac arrest.",
            "রক্তে পটাশিয়ামের মাত্রা বিপজ্জনকভাবে বেড়ে গিয়ে হার্ট অ্যাটাকের ঝুঁকি তৈরি হতে পারে।",
            "Check K+ levels prior to infusion. Avoid routine potassium supplementation unless hypokalemic.",
            "পটাশিয়াম সাপ্লিমেন্ট দেওয়ার আগে রক্তে পটাশিয়ামের মাত্রা পরীক্ষা করুন।"),

        ClinicalDrugInteraction("Omeprazole", "Clopidogrel", "MODERATE",
            "Omeprazole inhibits CYP2C19, reducing conversion of Clopidogrel to active form, lowering cardioprotection.",
            "ওমেপ্রাজল ক্লোপিডোগ্রেলের সক্রিয়তা কমিয়ে দিয়ে পুনরায় হার্ট অ্যাটাক বা স্ট্রোকের ঝুঁকি বাড়ায়।",
            "Switch PPI to Pantoprazole or H2-blocker Famotidine which do not inhibit CYP2C19.",
            "ওমেপ্রাজলের পরিবর্তে প্যান্টোপ্রাজল বা ফ্যামোটিডিন ব্যবহার করুন।"),

        ClinicalDrugInteraction("Ciprofloxacin", "Omeprazole", "MODERATE",
            "Altered gastric pH reduces Ciprofloxacin bioavailability and antibacterial efficacy.",
            "পাকস্থলীর এসিড কমে যাওয়ায় সিপ্রোফ্লোক্সাসিনের শোষন ও অ্যান্টিবায়োটিক কার্যকারিতা কমে যায়।",
            "Administer Ciprofloxacin at least 2 hours before or 6 hours after antacids/PPIs.",
            "সিপ্রোফ্লোক্সাসিন খাওয়ার অন্তত ২ ঘণ্টা আগে বা ৬ ঘণ্টা পরে পিপিআই/এন্টাসিড দিন।")
    )

    var searchQuery by remember { mutableStateOf("") }
    var selectedFormFilter by remember { mutableStateOf("All") }
    var selectedDrugs by remember { mutableStateOf(setOf<InternationalDrug>()) }

    val forms = listOf("All", "Tab", "Cap", "Inj", "Lotion", "Inhaler", "IV Fluid", "Eye Drop")

    val filteredDrugList = allDrugs.filter { drug ->
        val matchesForm = selectedFormFilter == "All" || drug.form.equals(selectedFormFilter, true)
        val matchesQuery = searchQuery.isBlank() || 
                drug.name.contains(searchQuery, true) || 
                drug.generic.contains(searchQuery, true) || 
                drug.category.contains(searchQuery, true)
        matchesForm && matchesQuery
    }

    val activeInteractions = mutableListOf<ClinicalDrugInteraction>()
    val selectedList = selectedDrugs.toList()
    for (i in selectedList.indices) {
        for (j in i + 1 until selectedList.size) {
            val d1 = selectedList[i].name
            val d2 = selectedList[j].name
            val match = clinicalInteractions.find { 
                (it.drug1.contains(d1, true) || d1.contains(it.drug1, true)) &&
                (it.drug2.contains(d2, true) || d2.contains(it.drug2, true)) ||
                (it.drug1.contains(d2, true) || d2.contains(it.drug1, true)) &&
                (it.drug2.contains(d1, true) || d1.contains(it.drug2, true))
            }
            if (match != null) {
                activeInteractions.add(match)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, shadowElevation = 0.dp) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isBn) "ইন্টারন্যাশনাল ক্লিনিক্যাল ড্রাগ ইন্টারঅ্যাকশন" else "Clinical Drug Interaction Suite",
                            fontSize = 16.sp,
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
            }
        },
        bottomBar = { AppBottomBar(context = context, currentTab = -1, isNurseMode = true, backgroundBrush = AppGradientBrush) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.9f),
                            border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = if (isBn) "🔍 আন্তর্জাতিক ড্রাগ সার্চ ও সিলেকশন (${allDrugs.size}+ উপলব্ধ)" else "Search International Drugs (${allDrugs.size}+ Available)",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (isDark) Color.White else Color(0xFF1A237E)
                                )
                                Spacer(Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    label = { Text(if (isBn) "ওষুধ বা জেনেনরিক খুঁজুন (যেমন: Insulin, Ceftriaxone, Aspirin)..." else "Search Brand / Generic / Class...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF0284C7)) },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Clear, null)
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    singleLine = true
                                )

                                Spacer(Modifier.height(12.dp))
                                Text(if (isBn) "ড্রাগ ফরম ফিল্টার:" else "Filter by Dosage Form:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(Modifier.height(6.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    forms.forEach { form ->
                                        FilterChip(
                                            selected = selectedFormFilter == form,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                selectedFormFilter = form
                                            },
                                            label = { Text(form, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(14.dp))
                                Text(
                                    text = if (isBn) "সিলেক্ট করুন (${selectedDrugs.size} নির্বাচিত):" else "Select Medicines (${selectedDrugs.size} Selected):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                )
                                Spacer(Modifier.height(8.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp).verticalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    filteredDrugList.forEach { drug ->
                                        val isSelected = selectedDrugs.contains(drug)
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                selectedDrugs = if (isSelected) selectedDrugs - drug else selectedDrugs + drug
                                            },
                                            label = { Text("[${drug.form}] ${drug.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            leadingIcon = {
                                                if (isSelected) Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = if (isBn) "⚠️ ক্লিনিক্যাল মিথস্ক্রিয়া ও নার্সিং গাইড" else "Clinical Interaction & Nursing Protocol",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = if (isDark) Color.White else Color(0xFF1A237E)
                        )
                    }

                    if (selectedDrugs.size < 2) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.6f)
                            ) {
                                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isBn) "ক্লিনিক্যাল ইন্টারঅ্যাকশন বিশ্লেষণের জন্য অন্তত ২টি ওষুধ সিলেক্ট করুন" else "Select at least 2 medications above for clinical analysis",
                                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    } else if (activeInteractions.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF22C55E).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(36.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = if (isBn) "নির্বাচিত ওষুধগুলোর মধ্যে কোনো গুরুতর ড্রাইগ ইন্টারঅ্যাকশন পাওয়া যায়নি" else "No major clinical interactions detected between selected drugs",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Color(0xFF0F172A),
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(activeInteractions) { interaction ->
                            val sevColor = when (interaction.severity) {
                                "SEVERE" -> Color(0xFFEF4444)
                                "MODERATE" -> Color(0xFFF59E0B)
                                else -> Color(0xFF0284C7)
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = if (isDark) Color(0xFF1E293B) else Color.White,
                                border = BorderStroke(1.5.dp, sevColor.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("${interaction.drug1} ⚡ ${interaction.drug2}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                                        Surface(color = sevColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                                            Text(interaction.severity, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = sevColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    Text(if (isBn) "🔬 মেকানিজম (Mechanism):" else "🔬 Mechanism of Action:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = sevColor)
                                    Text(if (isBn) interaction.mechanismBn else interaction.mechanismEn, fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray, lineHeight = 17.sp)

                                    Spacer(Modifier.height(8.dp))
                                    Text(if (isBn) "🩺 নার্সিং অ্যাকশন ও মনিটরিং:" else "🩺 Nursing Action & Management:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0284C7))
                                    Text(if (isBn) interaction.nursingActionBn else interaction.nursingActionEn, fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray, lineHeight = 17.sp)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }
        }
    }
}
