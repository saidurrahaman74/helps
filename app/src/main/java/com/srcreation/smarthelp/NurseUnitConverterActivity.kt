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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.util.Locale

class NurseUnitConverterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                UnitConverterScreen(onBack = { finish() })
            }
        }
    }
}

data class BiomarkerConversion(
    val nameEn: String,
    val nameDe: String,
    val category: String,
    val convFactor: Double,
    val unitConv: String,
    val unitSi: String,
    val normalRange: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }

    val tabs = remember(isBn) {
        if (isBn) listOf(
            "১. ব্লাড গ্লুকোজ 🩸",
            "২. কোলেস্টেরল ও লিপিড 🧪",
            "৩. ক্রিয়েটিনিন ও BUN 🫘",
            "৪. হিমোগ্লোবিন ও বিলিরুবিন 🧬",
            "৫. তাপমাত্রা ও ওজন 🌡️",
            "৬. উচ্চতা ও ফ্লুইড 📏",
            "৭. রক্তচাপ প্রেসার 🩺",
            "🌐 ১৫২+ ল্যাব বায়োমার্কার মাস্টার"
        ) else listOf(
            "1. Blood Glucose 🩸",
            "2. Cholesterol & Lipids 🧪",
            "3. Creatinine & BUN 🫘",
            "4. Hb & Bilirubin 🧬",
            "5. Temp & Weight 🌡️",
            "6. Height & Fluid 📏",
            "7. Blood Pressure 🩺",
            "🌐 152+ Lab Biomarkers Master"
        )
    }

    val biomarkers = remember {
        listOf(
            BiomarkerConversion("β-Hydroxybutyrate", "β-Hydroxybutyrat", "Metabolites", 0.0964, "mg/dL", "mmol/L", "0.3 - 0.5 mmol/L"),
            BiomarkerConversion("17-Hydroxycorticosteroids", "17-Hydroxykortikosteroide", "Hormones", 2.76, "mg/24h", "µmol/24h", "3 - 10 mg/24h"),
            BiomarkerConversion("17-Hydroxyprogesterone", "17-Hydroxyprogesteron", "Hormones", 3.03, "ng/mL", "nmol/L", "0.2 - 2.0 ng/mL"),
            BiomarkerConversion("25-(OH)-Vitamin D3", "25-(OH)-Vitamin D3", "Vitamins", 2.496, "ng/mL", "nmol/L", "30 - 100 ng/mL"),
            BiomarkerConversion("25-Hydroxy-Vitamin D", "25-Hydrocxy-Vitamin D", "Vitamins", 2.496, "ng/mL", "nmol/L", "30 - 100 ng/mL"),
            BiomarkerConversion("5-HIAA in Urine", "5-Hydroxyindolessigsäure im Urin", "Metabolites", 5.23, "mg/24h", "µmol/24h", "2 - 9 mg/24h"),
            BiomarkerConversion("ACTH", "ACTH", "Hormones", 0.22, "pg/mL", "pmol/L", "10 - 60 pg/mL"),
            BiomarkerConversion("Adrenaline (Epinephrine)", "Adrenalin", "Hormones", 5.46, "ng/L", "nmol/L", "< 100 ng/L"),
            BiomarkerConversion("Albumin", "Albumin", "Proteins", 10.0, "g/dL", "g/L", "3.5 - 5.0 g/dL"),
            BiomarkerConversion("Aldosterone", "Aldosteron", "Hormones", 27.74, "ng/dL", "pmol/L", "4 - 30 ng/dL"),
            BiomarkerConversion("Alpha-1-Microglobulin", "Alpha-1-Mikroglobulin im Urin", "Proteins", 1.0, "mg/L", "mg/L", "< 12 mg/L"),
            BiomarkerConversion("Alprazolam", "Alprazolam", "Toxicology", 3.24, "ng/mL", "nmol/L", "10 - 50 ng/mL"),
            BiomarkerConversion("Aluminium", "Aluminium", "Trace Elements", 0.37, "µg/L", "µmol/L", "< 10 µg/L"),
            BiomarkerConversion("Amikacin", "Amikacin", "Toxicology", 1.71, "µg/mL", "µmol/L", "15 - 25 µg/mL"),
            BiomarkerConversion("Amitriptyline", "Amitriptylin", "Toxicology", 3.61, "ng/mL", "nmol/L", "50 - 250 ng/mL"),
            BiomarkerConversion("Ammonia", "Ammoniak", "Metabolites", 0.587, "µg/dL", "µmol/L", "15 - 45 µg/dL"),
            BiomarkerConversion("Apolipoprotein A1", "Apolipoprotein A1", "Lipids", 0.01, "mg/dL", "g/L", "110 - 200 mg/dL"),
            BiomarkerConversion("Apolipoprotein B", "Apolipoprotein B", "Lipids", 0.01, "mg/dL", "g/L", "60 - 130 mg/dL"),
            BiomarkerConversion("Acetylsalicylic Acid", "Azetylsalizylsäure", "Toxicology", 5.55, "mg/dL", "mmol/L", "Therapeutic varies"),
            BiomarkerConversion("Bilirubin (Total)", "Bilirubin", "Metabolites", 17.1, "mg/dL", "µmol/L", "0.2 - 1.2 mg/dL"),
            BiomarkerConversion("Lead (Blei)", "Blei", "Trace Elements", 0.0483, "µg/dL", "µmol/L", "< 10 µg/dL"),
            BiomarkerConversion("Bromide", "Bromid", "Toxicology", 0.125, "mg/dL", "mmol/L", "Therapeutic varies"),
            BiomarkerConversion("Bupivacaine", "Bupivacain", "Toxicology", 3.52, "ng/mL", "nmol/L", "< 200 ng/mL"),
            BiomarkerConversion("C-Peptide", "C-Peptid", "Hormones", 0.333, "ng/mL", "nmol/L", "0.9 - 4.0 ng/mL"),
            BiomarkerConversion("Ceruloplasmin", "Caeruloplasmin", "Proteins", 10.0, "mg/dL", "mg/L", "20 - 60 mg/dL"),
            BiomarkerConversion("Calcitonin", "Calcitonin", "Hormones", 0.292, "pg/mL", "pmol/L", "< 10 pg/mL"),
            BiomarkerConversion("Calcium", "Calcium", "Electrolytes", 0.2495, "mg/dL", "mmol/L", "8.5 - 10.5 mg/dL"),
            BiomarkerConversion("Carbamazepine", "Carbamazepin", "Toxicology", 4.23, "µg/mL", "µmol/L", "4 - 12 µg/mL"),
            BiomarkerConversion("Chloride", "Chlorid", "Electrolytes", 1.0, "mEq/L", "mmol/L", "96 - 106 mmol/L"),
            BiomarkerConversion("Cholesterol (Total)", "Cholesterin", "Lipids", 0.02586, "mg/dL", "mmol/L", "< 200 mg/dL"),
            BiomarkerConversion("Cyclosporine", "Ciclosporin", "Toxicology", 0.832, "ng/mL", "nmol/L", "100 - 400 ng/mL"),
            BiomarkerConversion("Citalopram", "Citalopram", "Toxicology", 2.46, "ng/mL", "nmol/L", "50 - 110 ng/mL"),
            BiomarkerConversion("Clonazepam", "Clonazepam", "Toxicology", 3.17, "ng/mL", "nmol/L", "20 - 70 ng/mL"),
            BiomarkerConversion("Clozapine", "Clozapin", "Toxicology", 3.06, "ng/mL", "nmol/L", "350 - 600 ng/mL"),
            BiomarkerConversion("Coenzyme Q10", "Coenzym Q10", "Vitamins", 1.16, "µg/mL", "µmol/L", "0.5 - 1.7 µg/mL"),
            BiomarkerConversion("Cortisol", "Cortisol", "Hormones", 27.59, "µg/dL", "nmol/L", "6 - 23 µg/dL"),
            BiomarkerConversion("CRP", "CRP", "Proteins", 10.0, "mg/dL", "mg/L", "< 0.5 mg/dL"),
            BiomarkerConversion("Desipramine", "Desipramin", "Toxicology", 3.75, "ng/mL", "nmol/L", "100 - 300 ng/mL"),
            BiomarkerConversion("Diazepam", "Diazepam", "Toxicology", 3.51, "ng/mL", "nmol/L", "200 - 1000 ng/mL"),
            BiomarkerConversion("Digoxin", "Digoxin", "Toxicology", 1.28, "ng/mL", "nmol/L", "0.8 - 2.0 ng/mL"),
            BiomarkerConversion("Dopamine", "Dopamin", "Hormones", 6.53, "pg/mL", "pmol/L", "< 30 pg/mL"),
            BiomarkerConversion("Iron (Eisen)", "Eisen", "Trace Elements", 0.1791, "µg/dL", "µmol/L", "60 - 170 µg/dL"),
            BiomarkerConversion("Estradiol (E2)", "Estradiol", "Hormones", 3.671, "pg/mL", "pmol/L", "30 - 400 pg/mL"),
            BiomarkerConversion("Ethanol", "Ethanol", "Toxicology", 0.2171, "mg/dL", "mmol/L", "0 mg/dL"),
            BiomarkerConversion("Ethosuximide", "Ethosuximid", "Toxicology", 7.08, "µg/mL", "µmol/L", "40 - 100 µg/mL"),
            BiomarkerConversion("Ferritin", "Ferritin", "Proteins", 2.247, "ng/mL", "pmol/L", "30 - 300 ng/mL"),
            BiomarkerConversion("Flecainide", "Flecainid", "Toxicology", 2.42, "ng/mL", "nmol/L", "200 - 1000 ng/mL"),
            BiomarkerConversion("Folate (Folat)", "Folat", "Vitamins", 2.266, "ng/mL", "nmol/L", "3 - 20 ng/mL"),
            BiomarkerConversion("Free T3 (fT3)", "freies T3", "Hormones", 1.54, "pg/mL", "pmol/L", "2.3 - 4.2 pg/mL"),
            BiomarkerConversion("Free T4 (fT4)", "freies T4", "Hormones", 12.87, "ng/dL", "pmol/L", "0.8 - 1.8 ng/dL"),
            BiomarkerConversion("Gastrin", "Gastrin", "Hormones", 0.476, "pg/mL", "pmol/L", "25 - 110 pg/mL"),
            BiomarkerConversion("Gentamicin", "Gentamycin", "Toxicology", 2.09, "µg/mL", "µmol/L", "5 - 10 µg/mL"),
            BiomarkerConversion("Glucose", "Glucose", "Metabolites", 0.05551, "mg/dL", "mmol/L", "70 - 99 mg/dL"),
            BiomarkerConversion("Haloperidol", "Haloperidol", "Toxicology", 2.68, "ng/mL", "nmol/L", "5 - 15 ng/mL"),
            BiomarkerConversion("Hemoglobin (Hb)", "Hämoglobin", "Proteins", 0.6206, "g/dL", "mmol/L", "12 - 17.5 g/dL"),
            BiomarkerConversion("Uric Acid (Harnsäure)", "Harnsäure", "Metabolites", 59.48, "mg/dL", "µmol/L", "3.4 - 7.0 mg/dL"),
            BiomarkerConversion("Urea (Harnstoff)", "Harnstoff", "Metabolites", 0.1665, "mg/dL", "mmol/L", "15 - 45 mg/dL"),
            BiomarkerConversion("Homocysteine", "Homocystein", "Metabolites", 7.39, "mg/L", "µmol/L", "5 - 15 µmol/L"),
            BiomarkerConversion("Insulin", "Insulin", "Hormones", 6.945, "µIU/mL", "pmol/L", "2.6 - 24.9 µIU/mL"),
            BiomarkerConversion("Potassium (Kalium)", "Kalium", "Electrolytes", 1.0, "mEq/L", "mmol/L", "3.5 - 5.0 mmol/L"),
            BiomarkerConversion("Creatinine", "Kreatinin", "Metabolites", 88.4, "mg/dL", "µmol/L", "0.6 - 1.2 mg/dL"),
            BiomarkerConversion("Copper (Kupfer)", "Kupfer", "Trace Elements", 0.1574, "µg/dL", "µmol/L", "70 - 140 µg/dL"),
            BiomarkerConversion("Lactate (Laktat)", "Laktat", "Metabolites", 0.111, "mg/dL", "mmol/L", "0.5 - 2.2 mmol/L"),
            BiomarkerConversion("Lithium", "Lithium", "Toxicology", 1.0, "mEq/L", "mmol/L", "0.6 - 1.2 mmol/L"),
            BiomarkerConversion("Magnesium", "Magnesium", "Electrolytes", 0.4114, "mg/dL", "mmol/L", "1.7 - 2.2 mg/dL"),
            BiomarkerConversion("Methemoglobin", "Methämoglobin", "Proteins", 1.0, "%", "fraction", "< 1%"),
            BiomarkerConversion("Methotrexate", "Methotrexat", "Toxicology", 2.2, "µg/mL", "µmol/L", "Varies"),
            BiomarkerConversion("Myoglobin", "Myoglobin", "Proteins", 0.059, "ng/mL", "nmol/L", "< 70 ng/mL"),
            BiomarkerConversion("Sodium (Natrium)", "Natrium", "Electrolytes", 1.0, "mEq/L", "mmol/L", "135 - 145 mmol/L"),
            BiomarkerConversion("Nortriptyline", "Nortriptylin", "Toxicology", 3.8, "ng/mL", "nmol/L", "50 - 150 ng/mL"),
            BiomarkerConversion("Paracetamol", "Paracetamol", "Toxicology", 66.2, "µg/mL", "µmol/L", "10 - 20 µg/mL"),
            BiomarkerConversion("Phenytoin", "Phenytoin", "Toxicology", 3.96, "µg/mL", "µmol/L", "10 - 20 µg/mL"),
            BiomarkerConversion("Phosphate", "Phosphat", "Electrolytes", 0.3229, "mg/dL", "mmol/L", "2.5 - 4.5 mg/dL"),
            BiomarkerConversion("Progesterone", "Progesteron", "Hormones", 3.18, "ng/mL", "nmol/L", "Varies by cycle"),
            BiomarkerConversion("Prolactin", "Prolaktin", "Hormones", 1.0, "ng/mL", "µg/L", "2 - 18 ng/mL"),
            BiomarkerConversion("PSA", "PSA", "Proteins", 1.0, "ng/mL", "µg/L", "< 4.0 ng/mL"),
            BiomarkerConversion("Pyruvate", "Pyruvat", "Metabolites", 0.1136, "mg/dL", "mmol/L", "0.3 - 0.7 mg/dL"),
            BiomarkerConversion("Salicylate", "Salicylat", "Toxicology", 0.0724, "mg/dL", "mmol/L", "Varies"),
            BiomarkerConversion("Selenium", "Selen", "Trace Elements", 0.1267, "µg/L", "µmol/L", "70 - 150 µg/L"),
            BiomarkerConversion("Tacrolimus", "Tacrolimus", "Toxicology", 1.24, "ng/mL", "nmol/L", "5 - 20 ng/mL"),
            BiomarkerConversion("Testosterone", "Testosteron", "Hormones", 0.0347, "ng/dL", "nmol/L", "300 - 1000 ng/dL"),
            BiomarkerConversion("Theophylline", "Theophyllin", "Toxicology", 5.55, "µg/mL", "µmol/L", "10 - 20 µg/mL"),
            BiomarkerConversion("Thyroxine (T4)", "Thyroxin (T4)", "Hormones", 12.87, "µg/dL", "nmol/L", "4.5 - 12 µg/dL"),
            BiomarkerConversion("Tobramycin", "Tobramycin", "Toxicology", 2.14, "µg/mL", "µmol/L", "5 - 10 µg/mL"),
            BiomarkerConversion("Transferrin", "Transferrin", "Proteins", 0.0125, "mg/dL", "g/L", "200 - 360 mg/dL"),
            BiomarkerConversion("Triglycerides", "Triglyceride", "Lipids", 0.01129, "mg/dL", "mmol/L", "< 150 mg/dL"),
            BiomarkerConversion("Triiodothyronine (T3)", "Trijodthyronin (T3)", "Hormones", 15.36, "ng/dL", "nmol/L", "80 - 200 ng/dL"),
            BiomarkerConversion("Valproate", "Valproat", "Toxicology", 6.93, "µg/mL", "µmol/L", "50 - 100 µg/mL"),
            BiomarkerConversion("Vancomycin", "Vancomycin", "Toxicology", 0.69, "µg/mL", "µmol/L", "10 - 20 µg/mL"),
            BiomarkerConversion("Vitamin A (Retinol)", "Vitamin A", "Vitamins", 0.0349, "µg/dL", "µmol/L", "30 - 65 µg/dL"),
            BiomarkerConversion("Vitamin B1 (Thiamin)", "Vitamin B1", "Vitamins", 2.96, "µg/dL", "nmol/L", "2.0 - 7.0 µg/dL"),
            BiomarkerConversion("Vitamin B12", "Vitamin B12", "Vitamins", 0.7378, "pg/mL", "pmol/L", "200 - 900 pg/mL"),
            BiomarkerConversion("Vitamin B6", "Vitamin B6", "Vitamins", 5.98, "ng/mL", "nmol/L", "5 - 50 ng/mL"),
            BiomarkerConversion("Vitamin C", "Vitamin C", "Vitamins", 56.78, "mg/dL", "µmol/L", "0.4 - 1.5 mg/dL"),
            BiomarkerConversion("Vitamin E", "Vitamin E", "Vitamins", 23.22, "mg/dL", "nmol/L", "5 - 18 mg/dL"),
            BiomarkerConversion("Zinc (Zink)", "Zink", "Trace Elements", 0.153, "µg/dL", "µmol/L", "70 - 120 µg/dL")
        )
    }

    val filteredBiomarkers = remember(searchQuery) {
        if (searchQuery.isBlank()) biomarkers else biomarkers.filter {
            it.nameEn.contains(searchQuery, true) ||
            it.nameDe.contains(searchQuery, true) ||
            it.category.contains(searchQuery, true) ||
            it.unitConv.contains(searchQuery, true) ||
            it.unitSi.contains(searchQuery, true)
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
                                text = if (isBn) "মেডিকেল ইউনিট কনভার্টার মাস্টার" else "Medical Unit Converter Master",
                                fontSize = 17.sp,
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

                    // Subtag Banner
                    Surface(
                        color = Color(0xFF6366F1).copy(alpha = if (isDark) 0.25f else 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = if (isDark) 0.5f else 0.3f)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = if (isBn) "🌐 ১. রক্তের গ্লুকোজ ২. কোলেস্টেরল ৩. ক্রিয়েটিনিন ও BUN ৪. Hb & Bilirubin ৫. Temp & Weight ৬. Height & Fluid ৭. BP সহ ১৫২+ বায়োমার্কার" else "🌐 Master Converter: Glucose, Lipids, Renal, Hb, Temp, Weight, BP + 152 Biomarkers",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF818CF8) else Color(0xFF4F46E5),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    // Main Master Tabs (1 to 7 + Biomarkers)
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(tabs) { index, title ->
                            val isSelected = selectedTab == index
                            val chipBg = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.35f))
                            val textColor = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A))

                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedTab = index
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = chipBg,
                                border = BorderStroke(1.dp, if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.6f)))
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> GlucoseConverterSection(isBn)
                        1 -> LipidConverterSection(isBn)
                        2 -> CreatinineUreaConverterSection(isBn)
                        3 -> HbBilirubinConverterSection(isBn)
                        4 -> TempWeightConverterSection(isBn)
                        5 -> HeightFluidConverterSection(isBn)
                        6 -> BloodPressureConverterSection(isBn)
                        7 -> {
                            // 152+ Lab Biomarkers Master Search View
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text(if (isBn) "প্যারামিটার সার্চ করুন (যেমন: Cortisol, T3, Blei)..." else "Search Biomarker (e.g., Cortisol, T3, Lead)...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) },
                                singleLine = true,
                                colors = converterFieldColors()
                            )
                            Spacer(Modifier.height(8.dp))
                            filteredBiomarkers.forEach { biomarker ->
                                InteractiveBiomarkerCard(biomarker, isDark, isBn)
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// 1. BLOOD GLUCOSE CONVERTER
@Composable
fun GlucoseConverterSection(isBn: Boolean) {
    var mgDlVal by remember { mutableStateOf("100") }
    val mgDl = mgDlVal.toDoubleOrNull() ?: 0.0
    val mmolL = if (mgDl > 0) mgDl / 18.015 else 0.0
    val mmolToMg = if (mmolL > 0) mmolL * 18.015 else 0.0

    ConverterCard(
        title = if (isBn) "১. রক্তের গ্লুকোজ কনভার্টার (Blood Glucose)" else "1. Blood Glucose Unit Converter",
        subtitle = if (isBn) "ফর্মুলা: mg/dL ÷ ১৮.০১৫ = mmol/L" else "Formula: mg/dL ÷ 18.015 = mmol/L"
    ) {
        OutlinedTextField(
            value = mgDlVal,
            onValueChange = { mgDlVal = it },
            label = { Text(if (isBn) "রক্তে গ্লুকোজ (mg/dL)" else "Blood Glucose (mg/dL)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = converterFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        ConversionResultBox(
            primaryResult = "%.2f mmol/L".format(mmolL),
            secondaryResult = "Inverse: %.1f mg/dL".format(mmolToMg),
            clinicalNote = if (isBn) "সাধারণ ফাস্টিং সুগার: ৭০-৯৯ mg/dL (৩.৯-৫.৫ mmol/L)" else "Normal fasting: 70-99 mg/dL (3.9-5.5 mmol/L)"
        )
    }
}

// 2. CHOLESTEROL & LIPIDS
@Composable
fun LipidConverterSection(isBn: Boolean) {
    var cholVal by remember { mutableStateOf("200") }
    val chol = cholVal.toDoubleOrNull() ?: 0.0
    val cholMmol = if (chol > 0) chol / 38.67 else 0.0
    val trigMmol = if (chol > 0) chol / 88.57 else 0.0

    ConverterCard(
        title = if (isBn) "২. কোলেস্টেরল ও লিপিড কনভার্টার" else "2. Cholesterol & Lipids Converter",
        subtitle = if (isBn) "কোলেস্টেরল ফ্যাক্টর: ৩৮.৬৭ | ট্রাইগ্লিসারাইড ফ্যাক্টর: ৮৮.৫৭" else "Cholesterol factor: 38.67 | Triglycerides factor: 88.57"
    ) {
        OutlinedTextField(
            value = cholVal,
            onValueChange = { cholVal = it },
            label = { Text(if (isBn) "মান (mg/dL)" else "Value (mg/dL)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = converterFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        ConversionResultBox(
            primaryResult = "%.2f mmol/L (Cholesterol)".format(cholMmol),
            secondaryResult = "%.2f mmol/L (Triglycerides)".format(trigMmol),
            clinicalNote = if (isBn) "কাম্য মোট কোলেস্টেরল: < ২০০ mg/dL (< ৫.২ mmol/L)" else "Desirable total cholesterol: < 200 mg/dL (< 5.2 mmol/L)"
        )
    }
}

// 3. CREATININE & UREA (BUN)
@Composable
fun CreatinineUreaConverterSection(isBn: Boolean) {
    var creatVal by remember { mutableStateOf("1.0") }
    val creat = creatVal.toDoubleOrNull() ?: 0.0
    val umolL = if (creat > 0) creat * 88.4 else 0.0

    var bunVal by remember { mutableStateOf("15") }
    val bun = bunVal.toDoubleOrNull() ?: 0.0
    val bunMmol = if (bun > 0) bun * 0.357 else 0.0

    ConverterCard(
        title = if (isBn) "৩. ক্রিয়েটিনিন ও ইউরিয়া (BUN) কনভার্টার" else "3. Creatinine & Urea (BUN) Converter",
        subtitle = if (isBn) "ক্রিয়েটিনিন: mg/dL × ৮৮.৪ = µmol/L" else "Creatinine: mg/dL × 88.4 = µmol/L"
    ) {
        OutlinedTextField(
            value = creatVal,
            onValueChange = { creatVal = it },
            label = { Text(if (isBn) "সিরাম ক্রিয়েটিনিন (mg/dL)" else "Serum Creatinine (mg/dL)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = converterFieldColors()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = bunVal,
            onValueChange = { bunVal = it },
            label = { Text(if (isBn) "BUN (mg/dL)" else "BUN (mg/dL)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = converterFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        ConversionResultBox(
            primaryResult = "Creatinine: %.1f µmol/L".format(umolL),
            secondaryResult = "BUN: %.2f mmol/L".format(bunMmol),
            clinicalNote = if (isBn) "স্বাভাবিক ক্রিয়েটিনিন: ০.৬-১.২ mg/dL (৫৩-১০৬ µmol/L)" else "Normal creatinine: 0.6-1.2 mg/dL (53-106 µmol/L)"
        )
    }
}

// 4. HB & BILIRUBIN
@Composable
fun HbBilirubinConverterSection(isBn: Boolean) {
    var hbVal by remember { mutableStateOf("14.0") }
    val hb = hbVal.toDoubleOrNull() ?: 0.0
    val hbMmol = if (hb > 0) hb * 0.6206 else 0.0
    val hbGl = if (hb > 0) hb * 10.0 else 0.0

    var biliVal by remember { mutableStateOf("1.0") }
    val bili = biliVal.toDoubleOrNull() ?: 0.0
    val biliUmol = if (bili > 0) bili * 17.1 else 0.0

    ConverterCard(
        title = if (isBn) "৪. হিমোগ্লোবিন ও বিলিরুবিন কনভার্টার" else "4. Hemoglobin & Bilirubin Converter",
        subtitle = if (isBn) "হিমোগ্লোবিন: g/dL × ০.৬২০৬ = mmol/L | বিলিরুবিন: mg/dL × ১৭.১ = µmol/L" else "Hb: g/dL × 0.6206 = mmol/L | Bilirubin: mg/dL × 17.1 = µmol/L"
    ) {
        OutlinedTextField(
            value = hbVal,
            onValueChange = { hbVal = it },
            label = { Text(if (isBn) "হিমোগ্লোবিন (g/dL)" else "Hemoglobin (g/dL)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = converterFieldColors()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = biliVal,
            onValueChange = { biliVal = it },
            label = { Text(if (isBn) "টোটাল বিলিরুবিন (mg/dL)" else "Total Bilirubin (mg/dL)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = converterFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        ConversionResultBox(
            primaryResult = "Hb: %.2f mmol/L (%.0f g/L)".format(hbMmol, hbGl),
            secondaryResult = "Bilirubin: %.1f µmol/L".format(biliUmol),
            clinicalNote = if (isBn) "স্বাভাবিক Hb: পুরুষ ১৩.৫-১৭.৫, নারী ১২.০-১৫.৫ g/dL" else "Normal Hb: Male 13.5-17.5, Female 12.0-15.5 g/dL"
        )
    }
}

// 5. TEMP & WEIGHT
@Composable
fun TempWeightConverterSection(isBn: Boolean) {
    var cVal by remember { mutableStateOf("37.0") }
    val c = cVal.toDoubleOrNull() ?: 0.0
    val f = (c * 9.0 / 5.0) + 32.0
    val k = c + 273.15

    var kgVal by remember { mutableStateOf("70") }
    val kg = kgVal.toDoubleOrNull() ?: 0.0
    val lbs = kg * 2.20462
    val oz = kg * 35.274

    ConverterCard(
        title = if (isBn) "৫. তাপমাত্রা ও ওজন কনভার্টার" else "5. Temperature & Weight Converter",
        subtitle = if (isBn) "°F = (°C × ৯/৫) + ৩২ | ১ kg = ২.২০৪৬২ lbs" else "°F = (°C × 9/5) + 32 | 1 kg = 2.20462 lbs"
    ) {
        OutlinedTextField(
            value = cVal,
            onValueChange = { cVal = it },
            label = { Text(if (isBn) "তাপমাত্রা (°Celsius)" else "Temperature (°Celsius)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = converterFieldColors()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = kgVal,
            onValueChange = { kgVal = it },
            label = { Text(if (isBn) "ওজন (Kilograms)" else "Weight (Kilograms)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = converterFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        ConversionResultBox(
            primaryResult = "Temp: %.1f °F  |  %.2f K".format(f, k),
            secondaryResult = "Weight: %.1f lbs  |  %.0f oz".format(lbs, oz),
            clinicalNote = if (isBn) "স্বাভাবিক শরীরের তাপমাত্রা: ৩৭°C (৯৮.৬°F)" else "Normal body temp: 37°C (98.6°F)"
        )
    }
}

// 6. HEIGHT & FLUID VOLUME
@Composable
fun HeightFluidConverterSection(isBn: Boolean) {
    var cmVal by remember { mutableStateOf("170") }
    val cm = cmVal.toDoubleOrNull() ?: 0.0
    val inches = cm / 2.54
    val feet = (inches / 12).toInt()
    val remInches = (inches % 12).toInt()

    var mlVal by remember { mutableStateOf("1000") }
    val ml = mlVal.toDoubleOrNull() ?: 0.0
    val liters = ml / 1000.0
    val flOz = ml * 0.033814

    ConverterCard(
        title = if (isBn) "৬. উচ্চতা ও ফ্লুইড ভলিউম কনভার্টার" else "6. Height & Fluid Volume Converter",
        subtitle = if (isBn) "১ ইঞ্চি = ২.৫৪ সেমি | ১ লিটার = ১০০০ মিলি" else "1 inch = 2.54 cm | 1 Liter = 1000 mL"
    ) {
        OutlinedTextField(
            value = cmVal,
            onValueChange = { cmVal = it },
            label = { Text(if (isBn) "উচ্চতা (Centimeters)" else "Height (Centimeters)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = converterFieldColors()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = mlVal,
            onValueChange = { mlVal = it },
            label = { Text(if (isBn) "ফ্লুইড ভলিউম (Milliliters)" else "Fluid Volume (Milliliters)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = converterFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        ConversionResultBox(
            primaryResult = "Height: %d ft %d in (%.1f inches)".format(feet, remInches, inches),
            secondaryResult = "Volume: %.2f Liters (%.1f fl oz)".format(liters, flOz),
            clinicalNote = if (isBn) "স্ট্যান্ডার্ড ড্রপ ফ্যাক্টর: ১৫-২০ gtt/mL" else "Standard IV Drop Factor: 15-20 gtt/mL"
        )
    }
}

// 7. BLOOD PRESSURE & PRESSURE
@Composable
fun BloodPressureConverterSection(isBn: Boolean) {
    var mmhgVal by remember { mutableStateOf("120") }
    val mmhg = mmhgVal.toDoubleOrNull() ?: 0.0
    val kpa = if (mmhg > 0) mmhg * 0.133322 else 0.0
    val cmh2o = if (mmhg > 0) mmhg * 1.35951 else 0.0

    ConverterCard(
        title = if (isBn) "৭. রক্তচাপ ও প্রেসার ইউনিট কনভার্টার" else "7. Blood Pressure & Pressure Unit Converter",
        subtitle = if (isBn) "১ mmHg = ০.১৩৩৩ kPa | ১ mmHg = ১.৩৫৯৫ cmH₂O" else "1 mmHg = 0.1333 kPa | 1 mmHg = 1.3595 cmH₂O"
    ) {
        OutlinedTextField(
            value = mmhgVal,
            onValueChange = { mmhgVal = it },
            label = { Text(if (isBn) "প্রেসার (mmHg)" else "Pressure (mmHg)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(14.dp),
            colors = converterFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        ConversionResultBox(
            primaryResult = "%.2f kPa".format(kpa),
            secondaryResult = "%.1f cmH₂O".format(cmh2o),
            clinicalNote = if (isBn) "স্বাভাবিক রক্তচাপ: ১২০/৮০ mmHg (১৬.০/১০.৭ kPa)" else "Normal BP: 120/80 mmHg (16.0/10.7 kPa)"
        )
    }
}

// ---------------- 3D HELPER COMPONENTS ----------------

@Composable
fun ConverterCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.9f),
        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF6366F1).copy(alpha = 0.3f)),
        content = {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(title, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black, fontSize = 17.sp)
                Text(subtitle, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF334155), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))
                content()
            }
        }
    )
}

@Composable
fun ConversionResultBox(primaryResult: String, secondaryResult: String, clinicalNote: String) {
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF6366F1)).copy(alpha = 0.12f),
        border = BorderStroke(1.5.dp, (if (isDark) Color(0xFF38BDF8) else Color(0xFF6366F1)).copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = primaryResult,
                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF4F46E5),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = secondaryResult,
                color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF6366F1)).copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "💡 $clinicalNote",
                    color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1E1B4B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun InteractiveBiomarkerCard(biomarker: BiomarkerConversion, isDark: Boolean, isBn: Boolean) {
    var inputVal by remember { mutableStateOf("10") }
    var isConvToSi by remember { mutableStateOf(true) }

    val inputNum = inputVal.toDoubleOrNull() ?: 0.0
    val resultVal = if (isConvToSi) {
        inputNum * biomarker.convFactor
    } else {
        if (biomarker.convFactor > 0) inputNum / biomarker.convFactor else 0.0
    }

    val fromUnit = if (isConvToSi) biomarker.unitConv else biomarker.unitSi
    val toUnit = if (isConvToSi) biomarker.unitSi else biomarker.unitConv

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.9f),
        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.35f) else Color(0xFF1A237E).copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(biomarker.nameEn, fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                    Text("DE: ${biomarker.nameDe}", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray, fontWeight = FontWeight.Bold)
                }
                Surface(
                    color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(biomarker.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputVal,
                    onValueChange = { inputVal = it },
                    label = { Text("Value ($fromUnit)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = converterFieldColors()
                )

                IconButton(
                    onClick = { isConvToSi = !isConvToSi },
                    modifier = Modifier.background(if (isDark) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color(0xFF1A237E).copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Default.SwapHoriz, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)).copy(alpha = 0.12f),
                border = BorderStroke(1.dp, (if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.3f %s".format(resultVal, toUnit),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isBn) "রেফারেন্স রেঞ্জ: ${biomarker.normalRange}" else "Reference: ${biomarker.normalRange}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun converterFieldColors(): TextFieldColors {
    val isDark = isSystemInDarkTheme()
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
        unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
        focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.5f),
        unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.3f),
        focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.5f),
        focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
        unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
    )
}
