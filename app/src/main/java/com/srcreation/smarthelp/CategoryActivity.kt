package com.srcreation.smarthelp

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

class CategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val categoryId = intent.getStringExtra("CATEGORY_ID") ?: ""
        val categoryTitle = intent.getStringExtra("CATEGORY_TITLE") ?: "Details"
        val categoryColor = intent.getStringExtra("CATEGORY_COLOR") ?: "#1A237E"

        setContent {
            HelpsTheme {
                CategoryDetailScreen(
                    categoryId = categoryId,
                    title = categoryTitle,
                    colorHex = categoryColor,
                    onBack = { finish() },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(categoryId: String, title: String, colorHex: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val themeColor = remember(colorHex) { colorFromHex(colorHex) }
    val intentFromActivity = (context as? AppCompatActivity)?.intent
    val currentProfileId = intentFromActivity?.getIntExtra("PROFILE_ID", 1) ?: 1
    
    val catHospitals = stringResource(id = R.string.cat_hospitals)
    val catFirstAid = stringResource(id = R.string.cat_first_aid)
    val catHealthTips = stringResource(id = R.string.cat_health_tips)
    val catBmi = stringResource(id = R.string.cat_bmi_calculator)
    val catWeight = stringResource(id = R.string.cat_weight_chart)
    val catMilitary = stringResource(id = R.string.cat_military_hospitals)
    val catGov = stringResource(id = R.string.cat_government_hospitals)
    val catPrivate = stringResource(id = R.string.cat_private_hospitals)
    val catSmart = stringResource(id = R.string.cat_smart_tool)

    val subButtons = remember(title) {
        when (title) {
            catHospitals, "Hospitals" -> listOf(
                "Government", "Military", "Private", "Ambulance", "Blood Bank"
            )
            catFirstAid, "First Aid" -> listOf(
                "Emergency Medicine", "Wounds", "Burns", "CPR Guide", "Fractures",
                "Poisoning", "Choking", "Electric Shock", "Snake Bite", "Heat Stroke", 
                "Heat Exhaustion", "Fainting", "Heart Attack", "Bleeding", "Seizure",
                "Dog & Animal Bite", "Anaphylaxis", "Insect & Bee Sting", "Asthma Attack",
                "Drowning First Aid", "Eye Injury & Chemical Splash"
            )
            catHealthTips, "Health Tips" -> listOf(
                "Daily Tips", "Diet Plan", "Exercise", "Mental Health", "Sleep",
                "Skin Care", "Child Health", "Pregnancy", "Vitamins", "Yoga"
            )
            catBmi, "BMI Calculator" -> listOf(
                "Calculate", "Ideal Weight", "Calories", "Weight Goal",
                "Diet Advice", "Water Intake", "Progress Chart", "BMI Info"
            )
            catWeight, "Weight Chart" -> listOf(
                "Underweight Chart", "Normal Weight", "Overweight Chart", "Obesity Guide"
            )
            catMilitary, "Military Hospitals" -> listOf(
                "CMH DHAKA", "CMH CHATTOGRAM", "CMH CUMILLA", "CMH RENDRAPUR", "CMH SAVAR",
                "CMH BOGURA", "CMH RAJSHAHI", "CMH KADIRABAD", "CMH MOMENSHAHI", "CMH JASHORE",
                "CMH BARISAL", "CMH RANGPUR", "CMH SAYEDPUR", "CMH SYLHET"
            )
            catGov, "Government Hospitals" -> listOf(
                "National Institute of Cardiovascular Diseases (NICVD)", "National Institute of Diseases of the Chest & Hospital (NIDCH)", "BIRDEM General Hospital", "Dhaka Medical College", "Chittagong Medical College Hospital", "Sir Salimullah Medical College", "Shaheed Suhrawardy Medical College", "National Institute of Neurosciences & Hospital"
            )
            catPrivate, "Private Hospitals" -> listOf(
                "Evercare Hospital Dhaka", "Square Hospital", "United Hospital", "Labaid Hospital", "Ibn Sina Hospital", "Chattagram Maa-O-Shishu Hospital"
            )
            catSmart, "Smart Tool" -> listOf(
                "BP Tracker", "Insulin tracker", "Insulin Calculator", "Step Counter", "Period Tracker",
                "Medicine Reminder", "Vaccine", "Medical Vault", "Unified Report", "Fluid Calculator", "Doctor Appointment", 
                "Prescription Scanner", "Wearable Sync"
            )
            else -> listOf("Service 1", "Service 2", "Service 3", "Service 4", "Service 5")
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    shadowElevation = 0.dp
                ) {
                    val isDark = isSystemInDarkTheme()
                    CenterAlignedTopAppBar(
                        title = { Text(getLocalizedCategoryTitle(title), fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = if (isDark) Color.White else Color(0xFF1A237E))
                            }
                        },
                        actions = { LanguageToggleButton() },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    )
                }
            },
            bottomBar = { AppBottomBar(context = context, currentTab = -1, backgroundBrush = AppGradientBrush) }
        ) { innerPadding ->
            ScreenEntranceAnimation {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(subButtons) { subTitle ->
                            val iconName = when (title) {
                                "Military Hospitals" -> "CMH"
                                "Private Hospitals", "Government Hospitals" -> "Hospital"
                                else -> subTitle
                            }
                            val icon = getIconByName(iconName)
                            val displaySubTitle = getLocalizedSubTitle(subTitle)

                            SubActionCard(displaySubTitle, themeColor, icon) {
                                val intent = when {
                                    title == catBmi || title == "BMI Calculator" -> Intent(context, BmiToolActivity::class.java).apply {
                                        putExtra("TOOL_NAME", subTitle)
                                        putExtra("PROFILE_ID", currentProfileId)
                                    }
                                    title == catWeight || title == "Weight Chart" -> Intent(context, WeightChartActivity::class.java).apply {
                                        putExtra("CHART_TYPE", subTitle)
                                        putExtra("PROFILE_ID", currentProfileId)
                                    }
                                    title == catHealthTips || title == "Health Tips" || subTitle == "Daily Tips" -> Intent(context, ProTipsActivity::class.java).apply {
                                        putExtra("CATEGORY_FILTER", subTitle)
                                    }
                                    (title == catSmart || title == "Smart Tool") && subTitle == "BP Tracker" -> Intent(context, BpTrackerActivity::class.java).apply { putExtra("PROFILE_ID", currentProfileId) }
                                    (title == catSmart || title == "Smart Tool") && subTitle == "Insulin tracker" -> Intent(context, InsulinTrackerActivity::class.java).apply { putExtra("PROFILE_ID", currentProfileId) }
                                    (title == catSmart || title == "Smart Tool") && subTitle == "Insulin Calculator" -> Intent(context, InsulinCalculatorActivity::class.java)
                                    (title == catSmart || title == "Smart Tool") && subTitle == "Step Counter" -> Intent(context, StepCounterActivity::class.java).apply { putExtra("PROFILE_ID", currentProfileId) }
                                    (title == catSmart || title == "Smart Tool") && subTitle == "Period Tracker" -> Intent(context, PeriodTrackerActivity::class.java).apply { putExtra("PROFILE_ID", currentProfileId) }
                                    (title == catSmart || title == "Smart Tool") && subTitle == "Medicine Reminder" -> Intent(context, MedicineReminderActivity::class.java).apply { putExtra("PROFILE_ID", currentProfileId) }
                                    (title == catSmart || title == "Smart Tool") && subTitle == "Vaccine" -> Intent(context, VaccineActivity::class.java).apply { putExtra("PROFILE_ID", currentProfileId) }
                                    (title == catSmart || title == "Smart Tool") && subTitle == "Medical Vault" -> Intent(context, MedicalVaultActivity::class.java).apply { putExtra("PROFILE_ID", currentProfileId) }
                                    (title == catSmart || title == "Smart Tool") && subTitle == "Unified Report" -> Intent(context, UnifiedReportActivity::class.java)
                                    (title == catSmart || title == "Smart Tool") && subTitle == "Fluid Calculator" -> Intent(context, FluidTrackerActivity::class.java)
                                    (title == catSmart || title == "Smart Tool") && subTitle == "Doctor Appointment" -> Intent(context, AppointmentActivity::class.java)
                                    (title == catSmart || title == "Smart Tool") && subTitle == "Prescription Scanner" -> Intent(context, PrescriptionScannerActivity::class.java)
                                    (title == catSmart || title == "Smart Tool") && subTitle == "Wearable Sync" -> Intent(context, HealthConnectActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Emergency Medicine" -> Intent(context, MedicalEncyclopediaActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Wounds" -> Intent(context, WoundsActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Burns" -> Intent(context, BurnsActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "CPR Guide" -> Intent(context, CprGuideActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Fractures" -> Intent(context, FracturesActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Poisoning" -> Intent(context, PoisoningActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Choking" -> Intent(context, ChokingActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Electric Shock" -> Intent(context, ElectricShockActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Snake Bite" -> Intent(context, SnakeBiteActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Heat Stroke" -> Intent(context, HeatStrokeActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Heat Exhaustion" -> Intent(context, HeatExhaustionActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Fainting" -> Intent(context, FaintingActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Heart Attack" -> Intent(context, HeartAttackActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Bleeding" -> Intent(context, BleedingActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Seizure" -> Intent(context, SeizureActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Dog & Animal Bite" -> Intent(context, AnimalBiteActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Anaphylaxis" -> Intent(context, AnaphylaxisActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Insect & Bee Sting" -> Intent(context, InsectStingActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Asthma Attack" -> Intent(context, AsthmaActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Drowning First Aid" -> Intent(context, DrowningActivity::class.java)
                                    (title == catFirstAid || title == "First Aid") && subTitle == "Eye Injury & Chemical Splash" -> Intent(context, EyeInjuryActivity::class.java)
                                    (title == catHospitals || title == "Hospitals") && subTitle == "Ambulance" -> Intent(context, ContentDisplayActivity::class.java).apply {
                                        putExtra("CATEGORY_ID", categoryId)
                                        putExtra("SUB_CATEGORY", subTitle)
                                        putExtra("CATEGORY_TITLE", title)
                                        putExtra("CATEGORY_COLOR", colorHex)
                                    }
                                    (title == catHospitals || title == "Hospitals") && subTitle == "Blood Bank" -> Intent(context, ContentDisplayActivity::class.java).apply {
                                        putExtra("CATEGORY_ID", categoryId)
                                        putExtra("SUB_CATEGORY", subTitle)
                                        putExtra("CATEGORY_TITLE", title)
                                        putExtra("CATEGORY_COLOR", colorHex)
                                    }
                                    (title == catHospitals || title == "Hospitals") && subTitle == "Military" -> Intent(context, CategoryActivity::class.java).apply {
                                        putExtra("CATEGORY_ID", categoryId)
                                        putExtra("SUB_CATEGORY", subTitle)
                                        putExtra("CATEGORY_TITLE", catMilitary)
                                        putExtra("CATEGORY_COLOR", colorHex)
                                    }
                                    (title == catHospitals || title == "Hospitals") && subTitle == "Government" -> Intent(context, CategoryActivity::class.java).apply {
                                        putExtra("CATEGORY_ID", categoryId)
                                        putExtra("SUB_CATEGORY", subTitle)
                                        putExtra("CATEGORY_TITLE", catGov)
                                        putExtra("CATEGORY_COLOR", colorHex)
                                    }
                                    (title == catHospitals || title == "Hospitals") && subTitle == "Private" -> Intent(context, CategoryActivity::class.java).apply {
                                        putExtra("CATEGORY_ID", categoryId)
                                        putExtra("SUB_CATEGORY", subTitle)
                                        putExtra("CATEGORY_TITLE", catPrivate)
                                        putExtra("CATEGORY_COLOR", colorHex)
                                    }
                                    (title == catPrivate || title == "Private Hospitals") && subTitle == "Evercare Hospital Dhaka" -> Intent(context, HospitalDetailActivity::class.java).apply {
                                        putExtra("HOSPITAL_ID", "evercare")
                                    }
                                    (title == catPrivate || title == "Private Hospitals") && subTitle == "Square Hospital" -> Intent(context, HospitalDetailActivity::class.java).apply {
                                        putExtra("HOSPITAL_ID", "square")
                                    }
                                    (title == catPrivate || title == "Private Hospitals") && subTitle == "Chattagram Maa-O-Shishu Hospital" -> Intent(context, HospitalDetailActivity::class.java).apply {
                                        putExtra("HOSPITAL_ID", "cmosh")
                                    }
                                    (title == catGov || title == "Government Hospitals") && subTitle == "BIRDEM General Hospital" -> Intent(context, HospitalDetailActivity::class.java).apply {
                                        putExtra("HOSPITAL_ID", "birdem")
                                    }
                                    (title == catGov || title == "Government Hospitals") && subTitle == "Dhaka Medical College" -> Intent(context, HospitalDetailActivity::class.java).apply {
                                        putExtra("HOSPITAL_ID", "dmch")
                                    }
                                    (title == catGov || title == "Government Hospitals") && subTitle == "Chittagong Medical College Hospital" -> Intent(context, HospitalDetailActivity::class.java).apply {
                                        putExtra("HOSPITAL_ID", "cmch")
                                    }
                                    (title == catGov || title == "Government Hospitals") && subTitle == "Sir Salimullah Medical College" -> Intent(context, HospitalDetailActivity::class.java).apply {
                                        putExtra("HOSPITAL_ID", "ssmc")
                                    }
                                    (title == catGov || title == "Government Hospitals") && subTitle == "National Institute of Neurosciences & Hospital" -> Intent(context, HospitalDetailActivity::class.java).apply {
                                        putExtra("HOSPITAL_ID", "nins")
                                    }
                                    (title == catGov || title == "Government Hospitals") && subTitle.contains("NICVD") -> Intent(context, HospitalDetailActivity::class.java).apply {
                                        putExtra("HOSPITAL_ID", "nicvd")
                                    }
                                    (title == catGov || title == "Government Hospitals") && subTitle.contains("NIDCH") -> Intent(context, HospitalDetailActivity::class.java).apply {
                                        putExtra("HOSPITAL_ID", "nidch")
                                    }
                                    else -> Intent(context, ContentDisplayActivity::class.java).apply {
                                        putExtra("CATEGORY_ID", categoryId)
                                        putExtra("SUB_CATEGORY", subTitle)
                                        putExtra("CATEGORY_TITLE", title)
                                        putExtra("CATEGORY_COLOR", colorHex)
                                    }
                                }
                                intent?.let { context.startActivity(it) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getLocalizedCategoryTitle(title: String): String {
    val clean = title.trim().lowercase()
    return when {
        clean.contains("bmi") -> stringResource(R.string.cat_bmi_calculator)
        clean.contains("weight chart") -> stringResource(R.string.cat_weight_chart)
        clean.contains("hospital") && clean.contains("gov") -> stringResource(R.string.cat_government_hospitals)
        clean.contains("hospital") && clean.contains("milit") -> stringResource(R.string.cat_military_hospitals)
        clean.contains("hospital") && clean.contains("priv") -> stringResource(R.string.cat_private_hospitals)
        clean.contains("hospital") -> stringResource(R.string.cat_hospitals)
        clean.contains("first aid") -> stringResource(R.string.cat_first_aid)
        clean.contains("health tip") -> stringResource(R.string.cat_health_tips)
        clean.contains("smart tool") -> stringResource(R.string.cat_smart_tool)
        else -> title
    }
}

@Composable
fun getLocalizedSubTitle(subTitle: String): String {
    return when (subTitle.trim().lowercase()) {
        "calculate" -> stringResource(R.string.sub_calculate)
        "ideal weight" -> stringResource(R.string.btn_calculate_ideal)
        "calories" -> stringResource(R.string.btn_calculate_calories)
        "history" -> stringResource(R.string.sub_history)
        "weight goal" -> stringResource(R.string.btn_check_goal)
        "diet advice" -> stringResource(R.string.sub_diet_advice)
        "water intake" -> stringResource(R.string.btn_calculate_water)
        "progress chart" -> stringResource(R.string.sub_progress_chart)
        "pro tips" -> stringResource(R.string.sub_pro_tips)
        "underweight chart", "underweight" -> stringResource(R.string.sub_underweight_chart)
        "normal weight", "normal" -> stringResource(R.string.sub_normal_weight)
        "overweight chart", "overweight" -> stringResource(R.string.sub_overweight_chart)
        "obesity guide", "obese" -> stringResource(R.string.sub_obesity_guide)
        "bmi info" -> stringResource(R.string.what_is_bmi)
        "government" -> stringResource(R.string.sub_government_hospital)
        "military" -> stringResource(R.string.sub_military_hospital)
        "private" -> stringResource(R.string.sub_private_hospital)
        "ambulance" -> stringResource(R.string.tab_ambulance)
        "blood bank" -> stringResource(R.string.tab_blood_bank)
        "emergency medicine" -> stringResource(R.string.sub_emergency_medicine)
        "wounds" -> stringResource(R.string.sub_wounds)
        "burns" -> stringResource(R.string.sub_burns)
        "cpr guide" -> stringResource(R.string.sub_cpr_guide)
        "fractures" -> stringResource(R.string.sub_fractures)
        "poisoning" -> stringResource(R.string.sub_poisoning)
        "choking" -> stringResource(R.string.sub_choking)
        "electric shock" -> stringResource(R.string.sub_electric_shock)
        "snake bite" -> stringResource(R.string.sub_snake_bite)
        "heat stroke" -> stringResource(R.string.sub_heat_stroke)
        "heat exhaustion" -> stringResource(R.string.sub_heat_exhaustion)
        "fainting" -> stringResource(R.string.sub_fainting)
        "heart attack" -> stringResource(R.string.sub_heart_attack)
        "bleeding" -> stringResource(R.string.sub_bleeding)
        "seizure" -> stringResource(R.string.sub_seizure)
        "dog & animal bite", "animal bite" -> stringResource(R.string.sub_dog_bite)
        "anaphylaxis" -> stringResource(R.string.sub_anaphylaxis)
        "insect & bee sting" -> stringResource(R.string.sub_insect_sting)
        "asthma attack" -> stringResource(R.string.sub_asthma_attack)
        "drowning first aid", "drowning" -> stringResource(R.string.sub_drowning)
        "eye injury & chemical splash", "eye injury" -> stringResource(R.string.sub_eye_injury)
        "bp tracker" -> stringResource(R.string.tool_bp_tracker)
        "insulin tracker" -> stringResource(R.string.tool_insulin)
        "insulin calculator" -> stringResource(R.string.tool_insulin_calc)
        "step counter" -> stringResource(R.string.step_counter_title)
        "period tracker" -> stringResource(R.string.sub_period_tracker)
        "medicine reminder" -> stringResource(R.string.med_reminder_title)
        "vaccine" -> stringResource(R.string.sub_vaccine)
        "medical vault" -> stringResource(R.string.sub_medical_vault)
        "unified report" -> stringResource(R.string.sub_unified_report)
        "fluid calculator" -> stringResource(R.string.sub_fluid_calculator)
        "doctor appointment" -> stringResource(R.string.sub_doctor_appointment)
        "prescription scanner" -> stringResource(R.string.sub_prescription_scanner)
        "wearable sync" -> stringResource(R.string.sub_wearable_sync)
        else -> subTitle
    }
}

@Composable
fun SubActionCard(title: String, accentColor: Color, icon: ImageVector, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    val rotateX by animateFloatAsState(
        targetValue = if (isPressed) -10f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "rotateX"
    )

    val rotateY by animateFloatAsState(
        targetValue = if (isPressed) 6f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "rotateY"
    )

    val translationZ by animateFloatAsState(
        targetValue = if (isPressed) 20f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "z"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 135.dp, max = 180.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationX = rotateX
                rotationY = rotateY
                shadowElevation = translationZ
                cameraDistance = 12f * density
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(accentColor.copy(alpha = 0.18f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            val isCardDark = isSystemInDarkTheme()
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = if (isCardDark) Color.White else Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 18.sp,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
