package com.srcreation.smarthelp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.BmiEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.absoluteValue

class BmiToolActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val category = intent.getStringExtra("CATEGORY_TITLE") ?: "Health Calculator"
        val toolName = intent.getStringExtra("TOOL_NAME") ?: ""
        
        setContent {
            HelpsTheme {
                BmiToolScreen(category, toolName) { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BmiToolScreen(category: String, toolName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: BmiViewModel = viewModel()

    val strBmiCalc = stringResource(id = R.string.tool_bmi_calc)
    val strIdealWeight = stringResource(id = R.string.btn_calculate_ideal)
    val strCalories = stringResource(id = R.string.btn_calculate_calories)
    val strWater = stringResource(id = R.string.btn_calculate_water)
    val strGoal = stringResource(id = R.string.btn_check_goal)
    val strDietAdvice = stringResource(id = R.string.diet_advice_title)
    val strChart = stringResource(id = R.string.cat_weight_chart)
    val strBmiInfo = stringResource(id = R.string.what_is_bmi)

    var activeTab by remember {
        mutableIntStateOf(
            when (toolName.trim().lowercase()) {
                "calculate", "bmi calculator", "calculate bmi" -> 0
                "ideal weight", "calculate ideal weight" -> 1
                "calories", "calculate calories" -> 2
                "water intake", "calculate water intake" -> 3
                "weight goal", "check goal", "health goal" -> 4
                "diet advice", "diet plan", "diet", "nutrition advice" -> 5
                "history", "weight chart", "progress chart", "progress" -> 6
                "bmi info", "what is bmi?" -> 7
                else -> 0
            }
        )
    }

    var selectedBmiCategoryIndex by remember { mutableIntStateOf(1) }

    val displayTitle = when (activeTab) {
        0 -> strBmiCalc
        1 -> strIdealWeight
        2 -> strCalories
        3 -> strWater
        4 -> strGoal
        5 -> strDietAdvice
        6 -> strChart
        7 -> strBmiInfo
        else -> toolName.ifEmpty { category }
    }

    val isNurse = viewModel.profileId == 2
    val isDark = isAppInDarkTheme()

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, shadowElevation = 0.dp) {
                CenterAlignedTopAppBar(
                    title = { Text(displayTitle, fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) Color.White else Color(0xFF1A237E)) } },
                    actions = { LanguageToggleButton() },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = if (isDark) Color.White else Color(0xFF1A237E),
                        navigationIconContentColor = if (isDark) Color.White else Color(0xFF1A237E),
                        actionIconContentColor = if (isDark) Color.White else Color(0xFF1A237E)
                    )
                )
            }
        },
        bottomBar = { AppBottomBar(context = context, currentTab = 1, isNurseMode = isNurse, backgroundBrush = AppGradientBrush) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                when (activeTab) {
                    0 -> BmiCalculatorScreen(viewModel) { calculatedBmi ->
                        val catIdx = when {
                            calculatedBmi < 18.5 -> 0
                            calculatedBmi < 25.0 -> 1
                            calculatedBmi < 30.0 -> 2
                            else -> 3
                        }
                        selectedBmiCategoryIndex = catIdx
                        activeTab = 5
                    }
                    1 -> IdealWeightScreen(viewModel)
                    2 -> CaloriesScreen(viewModel)
                    3 -> WaterIntakeScreen(viewModel)
                    4 -> HealthGoalScreen(viewModel)
                    5 -> DietAdviceScreen(initialCategoryIndex = selectedBmiCategoryIndex)
                    6 -> ProgressChartScreen(viewModel, onNavigateToCalculator = { activeTab = 0 })
                    7 -> BmiInfoScreen()
                    else -> BmiCalculatorScreen(viewModel)
                }
            }
        }
    }
}

// ---------------- 3D CUSTOM UI COMPONENTS ----------------

@Composable
fun ThreeDCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF1A237E).copy(alpha = 0.25f),
                ambientColor = if (isDark) Color.Transparent else Color.White
            ),
        shape = RoundedCornerShape(28.dp),
        color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.88f),
        border = if (isDark) {
            BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
        } else {
            BorderStroke(1.5.dp, Brush.verticalGradient(listOf(Color.White, Color.White.copy(alpha = 0.4f))))
        }
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            content = content
        )
    }
}

@Composable
fun ThreeDButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = Icons.Default.Calculate
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "3d_button_scale")

    val textColor = if (isDark) Color(0xFF0F172A) else Color(0xFF1A237E)

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            isPressed = true
            onClick()
            isPressed = false
        },
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(
            1.5.dp,
            if (isDark) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color.White
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) listOf(Color.White, Color(0xFFE2E8F0)) else listOf(Color.White, Color(0xFFF1F5F9))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun ThreeDGenderSelector(gender: String, onGenderChange: (String) -> Unit, maleStr: String, femaleStr: String) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier.fillMaxWidth(), 
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf("Male" to maleStr, "Female" to femaleStr).forEach { (id, label) ->
            val isSelected = gender == id
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onGenderChange(id)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White),
                border = BorderStroke(1.5.dp, if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFE2E8F0)))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label, 
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFF334155)),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun UnitToggle(label: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            fontSize = 13.sp, 
            fontWeight = FontWeight.Bold, 
            color = if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFF334155),
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = CircleShape,
            color = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else Color.White),
            modifier = Modifier.shadow(4.dp, CircleShape)
        ) {
            Row(modifier = Modifier.padding(3.dp)) {
                options.forEach { opt ->
                    val isSelected = opt == selected
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelect(opt)
                        },
                        modifier = Modifier
                            .height(30.dp)
                            .width(52.dp)
                            .shadow(if (isSelected) 6.dp else 0.dp, CircleShape),
                        shape = CircleShape,
                        color = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else Color.Transparent
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = opt, 
                                color = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF64748B)),
                                fontSize = 11.sp, 
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- HELPER & SCREENS ----------------

fun parseHeightToCm(heightInput: String, unit: String): Double {
    val input = heightInput.trim()
    if (input.isBlank()) return 0.0
    val doubleVal = input.toDoubleOrNull() ?: 0.0
    return when (unit.lowercase()) {
        "cm" -> doubleVal
        "inch" -> doubleVal * 2.54
        "ft.inch", "ft/in", "feet" -> {
            val parts = input.split(".", ",")
            val feet = parts.getOrNull(0)?.toDoubleOrNull() ?: doubleVal
            val inches = if (parts.size > 1) parts[1].toDoubleOrNull() ?: 0.0 else 0.0
            val totalInches = (feet * 12) + inches
            totalInches * 2.54
        }
        else -> doubleVal
    }
}

fun formatHeightDisplay(heightCm: Double): String {
    if (heightCm <= 0) return "--"
    val totalInches = heightCm / 2.54
    val feet = (totalInches / 12).toInt()
    val inches = (totalInches % 12).toInt()
    return "${heightCm.toInt()} cm (${feet}ft ${inches}in)"
}

@Composable
fun BmiCalculatorScreen(
    viewModel: BmiViewModel,
    onViewDietPlan: ((Double) -> Unit)? = null
) {
    val context = LocalContext.current
    val prefManager = remember { PreferenceManager(context) }
    val isMetricPref = prefManager.isMetric()
    val isDark = isSystemInDarkTheme()

    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var weightUnit by rememberSaveable { mutableStateOf(if (isMetricPref) "kg" else "lbs") }
    var heightUnit by rememberSaveable { mutableStateOf(if (isMetricPref) "cm" else "ft.inch") }
    var result by remember { mutableStateOf<Double?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ThreeDCard {
            UnitToggle(stringResource(id = R.string.weight_unit), listOf("kg", "lbs"), weightUnit) { weightUnit = it }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = weight, 
                onValueChange = { weight = it }, 
                label = { Text(stringResource(id = R.string.weight_label) + " ($weightUnit)") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            UnitToggle(stringResource(id = R.string.height_unit), listOf("cm", "ft.inch", "inch"), heightUnit) { heightUnit = it }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = height, 
                onValueChange = { height = it }, 
                label = { Text(stringResource(id = R.string.height_label) + " ($heightUnit)") }, 
                placeholder = { Text(if (heightUnit == "cm") "e.g. 170 cm" else if (heightUnit == "inch") "e.g. 66 inch" else "e.g. 5.6 (5ft 6in)", color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Gray) },
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            ThreeDButton(
                text = stringResource(id = R.string.btn_calculate_bmi),
                icon = Icons.Default.Calculate,
                onClick = {
                    val w = weight.toDoubleOrNull()
                    val hCm = parseHeightToCm(height, heightUnit)
                    if (w != null && hCm > 0) {
                        val wKg = if (weightUnit == "kg") w else w * 0.453592
                        val hM = hCm / 100.0
                        val bmi = wKg / (hM * hM)
                        result = bmi
                        val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
                        viewModel.addBmiRecord(bmi, wKg, hCm, date)
                    }
                }
            )
        }

        result?.let { 
            Spacer(modifier = Modifier.height(28.dp))
            BmiResultCard(it, onViewDietPlan) 
        }
    }
}

@Composable
fun IdealWeightScreen(viewModel: BmiViewModel) {
    val context = LocalContext.current
    val prefManager = remember { PreferenceManager(context) }
    val isMetricPref = prefManager.isMetric()
    val isDark = isSystemInDarkTheme()

    var height by rememberSaveable { mutableStateOf("") }
    var heightUnit by rememberSaveable { mutableStateOf(if (isMetricPref) "cm" else "inch") }
    var gender by rememberSaveable { mutableStateOf("Male") }
    var resultUnit by rememberSaveable { mutableStateOf(if (isMetricPref) "kg" else "lbs") }
    var idealWeight by remember { mutableStateOf<Double?>(null) }

    val maleStr = stringResource(id = R.string.male)
    val femaleStr = stringResource(id = R.string.female)
    val statusIdealStr = stringResource(id = R.string.status_ideal_weight)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ThreeDCard {
            Text(stringResource(id = R.string.select_gender), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFF334155))
            Spacer(modifier = Modifier.height(8.dp))
            ThreeDGenderSelector(gender, { gender = it }, maleStr, femaleStr)
            
            Spacer(modifier = Modifier.height(16.dp))
            UnitToggle(stringResource(id = R.string.height_unit), listOf("cm", "ft.inch", "inch"), heightUnit) { heightUnit = it }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = height, 
                onValueChange = { height = it }, 
                label = { Text(stringResource(id = R.string.height_label) + " ($heightUnit)") }, 
                placeholder = { Text(if (heightUnit == "cm") "e.g. 170 cm" else if (heightUnit == "inch") "e.g. 66 inch" else "e.g. 5.6 (5ft 6in)", color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Gray) },
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            ThreeDButton(
                text = stringResource(id = R.string.btn_calculate_ideal),
                icon = Icons.Default.FitnessCenter,
                onClick = {
                    val hCm = parseHeightToCm(height, heightUnit)
                    if (hCm > 0) {
                        val calculated: Double
                        if (hCm > 152.4) {
                            val inchesOverFiveFeet = (hCm - 152.4) / 2.54
                            calculated = if (gender == "Male") 52.0 + (1.9 * inchesOverFiveFeet) else 49.0 + (1.7 * inchesOverFiveFeet)
                        } else {
                            calculated = if (gender == "Male") 52.0 else 49.0
                        }
                        idealWeight = calculated
                        val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
                        viewModel.addHealthGoal(statusIdealStr, calculated, "kg", date)
                    }
                }
            )
        }

        idealWeight?.let { 
            Spacer(modifier = Modifier.height(28.dp))
            UnitToggle(stringResource(id = R.string.result_format_label), listOf("kg", "lbs"), resultUnit) { resultUnit = it }
            Spacer(modifier = Modifier.height(12.dp))
            val displayWeight = if (resultUnit == "kg") it else it * 2.20462
            Surface(
                modifier = Modifier.shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(id = R.string.ideal_weight_result_label), fontSize = 14.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
                    Text("%.1f $resultUnit".format(displayWeight), fontSize = 38.sp, fontWeight = FontWeight.Black, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF22C55E))
                }
            }
        }
    }
}

@Composable
fun CaloriesScreen(viewModel: BmiViewModel) {
    val context = LocalContext.current
    val prefManager = remember { PreferenceManager(context) }
    val isMetricPref = prefManager.isMetric()
    val isDark = isSystemInDarkTheme()

    var age by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var weightUnit by rememberSaveable { mutableStateOf(if (isMetricPref) "kg" else "lbs") }
    var heightUnit by rememberSaveable { mutableStateOf(if (isMetricPref) "cm" else "inch") }
    var gender by rememberSaveable { mutableStateOf("Male") }
    var activityLevel by remember { mutableDoubleStateOf(1.2) }
    var result by remember { mutableStateOf<Double?>(null) }

    val maleStr = stringResource(id = R.string.male)
    val femaleStr = stringResource(id = R.string.female)

    val activities = listOf(
        stringResource(id = R.string.sedentary) to 1.2,
        stringResource(id = R.string.light_active) to 1.375,
        stringResource(id = R.string.moderate_active) to 1.55,
        stringResource(id = R.string.very_active) to 1.725
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ThreeDCard {
            ThreeDGenderSelector(gender, { gender = it }, maleStr, femaleStr)
            
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = age, 
                onValueChange = { age = it }, 
                label = { Text(stringResource(id = R.string.age_label)) }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            UnitToggle(stringResource(id = R.string.weight_unit), listOf("kg", "lbs"), weightUnit) { weightUnit = it }
            OutlinedTextField(
                value = weight, 
                onValueChange = { weight = it }, 
                label = { Text(stringResource(id = R.string.weight_label) + " ($weightUnit)") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            UnitToggle(stringResource(id = R.string.height_unit), listOf("cm", "ft.inch", "inch"), heightUnit) { heightUnit = it }
            OutlinedTextField(
                value = height, 
                onValueChange = { height = it }, 
                label = { Text(stringResource(id = R.string.height_label) + " ($heightUnit)") }, 
                placeholder = { Text(if (heightUnit == "cm") "e.g. 170 cm" else if (heightUnit == "inch") "e.g. 66 inch" else "e.g. 5.6 (5ft 6in)", color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Gray) },
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(id = R.string.activity_level_label), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFF334155))
            activities.forEach { (label, value) -> 
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activityLevel = value }
                        .padding(vertical = 4.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) { 
                    RadioButton(selected = activityLevel == value, onClick = { activityLevel = value })
                    Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.Black) 
                } 
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            ThreeDButton(
                text = stringResource(id = R.string.btn_calculate_calories),
                icon = Icons.Default.LocalFireDepartment,
                onClick = {
                    val a = age.toDoubleOrNull(); val wInput = weight.toDoubleOrNull(); val hCm = parseHeightToCm(height, heightUnit)
                    if (a != null && wInput != null && hCm > 0) {
                        val w = if (weightUnit == "kg") wInput else wInput * 0.453592
                        val bmr = if (gender == "Male") (10 * w) + (6.25 * hCm) - (5 * a) + 5 else (10 * w) + (6.25 * hCm) - (5 * a) - 161
                        result = bmr * activityLevel
                        val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
                        viewModel.addHealthGoal("Daily Calories", result!!, "kcal", date)
                    }
                }
            )
        }

        result?.let { 
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                modifier = Modifier.shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(id = R.string.daily_maintenance_label, it), fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), textAlign = TextAlign.Center) 
                }
            }
        }
    }
}

@Composable
fun WaterIntakeScreen(viewModel: BmiViewModel) {
    val context = LocalContext.current
    val prefManager = remember { PreferenceManager(context) }
    val isMetricPref = prefManager.isMetric()
    val isDark = isSystemInDarkTheme()

    var weight by rememberSaveable { mutableStateOf("") }
    var weightUnit by rememberSaveable { mutableStateOf(if (isMetricPref) "kg" else "lbs") }
    var result by remember { mutableStateOf<Double?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ThreeDCard {
            UnitToggle(stringResource(id = R.string.weight_unit), listOf("kg", "lbs"), weightUnit) { weightUnit = it }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = weight, 
                onValueChange = { weight = it }, 
                label = { Text(stringResource(id = R.string.weight_label) + " ($weightUnit)") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            ThreeDButton(
                text = stringResource(id = R.string.btn_calculate_water),
                icon = Icons.Default.WaterDrop,
                onClick = {
                    val wInput = weight.toDoubleOrNull()
                    if (wInput != null) {
                        val wKg = if (weightUnit == "kg") wInput else wInput * 0.453592
                        result = wKg * 33.0 // 33ml per kg
                        val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
                        viewModel.addHealthGoal("Water Goal", result!!, "ml", date)
                    }
                }
            )
        }

        result?.let { 
            Spacer(modifier = Modifier.height(28.dp))
            Surface(
                modifier = Modifier.shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(id = R.string.recommended_water), fontSize = 14.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("%.0f ml / %.1f L".format(it, it/1000.0), fontSize = 34.sp, fontWeight = FontWeight.Black, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)) 
                }
            }
        }
    }
}

@Composable
fun HealthGoalScreen(viewModel: BmiViewModel) {
    val isDark = isSystemInDarkTheme()
    var currentWeight by rememberSaveable { mutableStateOf("") }
    var targetWeight by rememberSaveable { mutableStateOf("") }
    var weightUnit by rememberSaveable { mutableStateOf("kg") }
    var targetDiff by remember { mutableStateOf<Double?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ThreeDCard {
            UnitToggle(stringResource(id = R.string.weight_unit), listOf("kg", "lbs"), weightUnit) { weightUnit = it }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currentWeight, 
                onValueChange = { currentWeight = it }, 
                label = { Text(stringResource(R.string.weight_current)) }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = targetWeight, 
                onValueChange = { targetWeight = it }, 
                label = { Text(stringResource(R.string.weight_target)) }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            ThreeDButton(
                text = stringResource(id = R.string.btn_check_goal),
                icon = Icons.Default.Flag,
                onClick = {
                    val cw = currentWeight.toDoubleOrNull(); val tw = targetWeight.toDoubleOrNull()
                    if (cw != null && tw != null) {
                        targetDiff = tw - cw
                    }
                }
            )
        }

        targetDiff?.let { diff ->
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                modifier = Modifier.shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.9f),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (diff > 0) stringResource(R.string.goal_gain, diff, weightUnit) else stringResource(R.string.goal_lose, diff.absoluteValue, weightUnit),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressChartScreen(viewModel: BmiViewModel, onNavigateToCalculator: (() -> Unit)? = null) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val history by viewModel.allBmiRecords.collectAsState(initial = emptyList())
    val goals by viewModel.allHealthGoals.collectAsState(initial = emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRecordIndex by remember { mutableIntStateOf(0) }

    val latestRecord = history.firstOrNull()
    val oldestRecord = history.lastOrNull()
    val weightDiff = if (latestRecord != null && oldestRecord != null && history.size > 1) {
        latestRecord.weight - oldestRecord.weight
    } else null

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Title & Action Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.progress_chart_title),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = if (isDark) Color.White else Color(0xFF1A237E)
                        )
                        Text(
                            text = stringResource(R.string.progress_chart_subtitle),
                            fontSize = 12.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showAddDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = if (isDark) Color.Black else Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.progress_add_entry_btn), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.Black else Color.White)
                        }
                    }
                }
            }

            if (latestRecord != null) {
                // 3D Stat Summary Cards Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val statusColor = when {
                            latestRecord.bmi < 18.5 -> Color(0xFF0284C7)
                            latestRecord.bmi < 25.0 -> Color(0xFF22C55E)
                            latestRecord.bmi < 30.0 -> Color(0xFFFF9800)
                            else -> Color(0xFFEF4444)
                        }

                        val statusText = when {
                            latestRecord.bmi < 18.5 -> stringResource(R.string.underweight)
                            latestRecord.bmi < 25.0 -> stringResource(R.string.normal)
                            latestRecord.bmi < 30.0 -> stringResource(R.string.overweight)
                            else -> stringResource(R.string.obese)
                        }

                        // Stat 1: Current BMI
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(8.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.92f),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.progress_latest_bmi), fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("%.1f".format(latestRecord.bmi), fontSize = 22.sp, fontWeight = FontWeight.Black, color = statusColor)
                                Surface(color = statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
                                    Text(statusText, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = statusColor)
                                }
                            }
                        }

                        // Stat 2: Weight Change
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(8.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.92f),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.progress_weight_change), fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                val diffStr = if (weightDiff != null) {
                                    val prefix = if (weightDiff > 0) "+" else ""
                                    "$prefix%.1f kg".format(weightDiff)
                                } else "0 kg"
                                val diffColor = if ((weightDiff ?: 0.0) <= 0) Color(0xFF22C55E) else Color(0xFFEF4444)
                                Text(diffStr, fontSize = 20.sp, fontWeight = FontWeight.Black, color = diffColor)
                                Text("%.1f kg".format(latestRecord.weight), fontSize = 10.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Medium)
                            }
                        }

                        // Stat 3: Total Logs
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(8.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.92f),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.progress_total_logs), fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${history.size}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                                Text(stringResource(R.string.progress_entries_saved), fontSize = 10.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                // Interactive 3D Spline Line Chart Component
                item {
                    BmiTrendChart(
                        records = history,
                        selectedIndex = selectedRecordIndex,
                        onSelectIndex = { selectedRecordIndex = it }
                    )
                }

                // Smart AI Health Assessment Insight Card
                item {
                    BmiInsightCard(latestRecord, weightDiff)
                }

                // Spectrum Indicator Bar
                item {
                    BmiSpectrumBar(latestRecord.bmi)
                }
            }

            // Goals Progress Card
            if (goals.isNotEmpty()) {
                item {
                    Text(stringResource(R.string.progress_goal_target), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                }
                items(goals) { goal ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFF22C55E).copy(alpha = 0.12f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(goal.type, fontWeight = FontWeight.ExtraBold, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), fontSize = 15.sp)
                                    Text(goal.date, fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("%.1f ${goal.unit}".format(goal.value), fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF22C55E))
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.deleteHealthGoal(goal.id)
                                        Toast.makeText(context, R.string.progress_deleted_record, Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }

            // History Log Timeline Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(id = R.string.sub_history),
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = if (isDark) Color.White else Color(0xFF1A237E)
                    )
                    if (history.isNotEmpty()) {
                        Text(
                            stringResource(R.string.progress_entry_count, history.size),
                            fontSize = 12.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            items(history) { record ->
                val statusColor = when {
                    record.bmi < 18.5 -> Color(0xFF0284C7)
                    record.bmi < 25.0 -> Color(0xFF22C55E)
                    record.bmi < 30.0 -> Color(0xFFFF9800)
                    else -> Color(0xFFEF4444)
                }
                val statusRes = when {
                    record.bmi < 18.5 -> R.string.underweight
                    record.bmi < 25.0 -> R.string.normal
                    record.bmi < 30.0 -> R.string.overweight
                    else -> R.string.obese
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.88f),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(record.date, fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(id = R.string.your_bmi, "%.1f".format(record.bmi)), fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(color = statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
                                    Text(stringResource(statusRes), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor)
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("%.1f kg".format(record.weight), fontWeight = FontWeight.Black, fontSize = 15.sp, color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155))
                            Text(formatHeightDisplay(record.height), fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.deleteBmiRecord(record.id)
                                Toast.makeText(context, R.string.progress_deleted_record, Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }

            if (history.isEmpty() && goals.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(R.string.progress_no_data),
                                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { showAddDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(stringResource(R.string.progress_add_entry_btn), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.Black else Color.White)
                                    }

                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.addSampleRecords()
                                            Toast.makeText(context, R.string.progress_demo_added, Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Text(stringResource(R.string.progress_add_sample_btn), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                if (onNavigateToCalculator != null) {
                                    OutlinedButton(
                                        onClick = { onNavigateToCalculator() },
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                                    ) {
                                        Text(stringResource(id = R.string.tool_bmi_calc), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddBmiLogDialog(
                onDismiss = { showAddDialog = false },
                onSave = { weightKg, heightCm ->
                    val hM = heightCm / 100.0
                    val bmi = weightKg / (hM * hM)
                    val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
                    viewModel.addBmiRecord(bmi, weightKg, heightCm, date)
                    Toast.makeText(context, R.string.progress_entry_saved, Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddBmiLogDialog(
    onDismiss: () -> Unit,
    onSave: (Double, Double) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var weightInput by rememberSaveable { mutableStateOf("") }
    var heightInput by rememberSaveable { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = if (isDark) Color(0xFF0F172A) else Color.White,
            border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFFCBD5E1))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    stringResource(R.string.progress_add_entry_dialog_title),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text(stringResource(R.string.weight_label) + " (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    label = { Text(stringResource(R.string.height_label) + " (cm)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel), color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val w = weightInput.toDoubleOrNull()
                            val h = heightInput.toDoubleOrNull()
                            if (w != null && h != null && h > 0) {
                                onSave(w, h)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.save), fontWeight = FontWeight.Bold, color = if (isDark) Color.Black else Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BmiInsightCard(record: BmiEntity, weightDiff: Double?) {
    val isDark = isSystemInDarkTheme()
    val (statusText, adviceText, color) = when {
        record.bmi < 18.5 -> Triple(
            stringResource(R.string.insight_underweight_status),
            stringResource(R.string.insight_underweight_advice),
            Color(0xFF0284C7)
        )
        record.bmi < 25.0 -> Triple(
            stringResource(R.string.insight_normal_status),
            stringResource(R.string.insight_normal_advice),
            Color(0xFF22C55E)
        )
        record.bmi < 30.0 -> Triple(
            stringResource(R.string.insight_overweight_status),
            stringResource(R.string.insight_overweight_advice),
            Color(0xFFFF9800)
        )
        else -> Triple(
            stringResource(R.string.insight_obese_status),
            stringResource(R.string.insight_obese_advice),
            Color(0xFFEF4444)
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = color),
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.9f),
        border = BorderStroke(1.5.dp, color.copy(alpha = if (isDark) 0.5f else 0.3f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = color.copy(alpha = 0.12f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(stringResource(R.string.progress_health_assessment), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    Text(statusText, fontSize = 14.sp, fontWeight = FontWeight.Black, color = color)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = adviceText,
                fontSize = 12.sp,
                color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155),
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun BmiTrendChart(
    records: List<BmiEntity>,
    selectedIndex: Int = 0,
    onSelectIndex: (Int) -> Unit = {}
) {
    if (records.isEmpty()) return
    val isDark = isSystemInDarkTheme()
    val chronological = remember(records) { records.take(10).reversed() }

    val minBmi = remember(chronological) { (chronological.minOfOrNull { it.bmi } ?: 15.0).coerceAtMost(15.0) }
    val maxBmi = remember(chronological) { (chronological.maxOfOrNull { it.bmi } ?: 35.0).coerceAtLeast(35.0) }

    val activeRecord = chronological.getOrNull(selectedIndex.coerceIn(0, chronological.lastIndex)) ?: chronological.last()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .shadow(10.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        stringResource(R.string.progress_trend_label),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else Color(0xFF1A237E)
                    )
                    Text(
                        "${activeRecord.date}: BMI %.1f | %.1f kg".format(activeRecord.bmi, activeRecord.weight),
                        fontSize = 11.sp,
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF1A237E).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        stringResource(R.string.progress_record_count, chronological.size),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height - 24.dp.toPx()
                val topPadding = 16.dp.toPx()

                if (chronological.size < 2) {
                    val single = chronological.firstOrNull()
                    if (single != null) {
                        drawCircle(if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), radius = 8.dp.toPx(), center = Offset(width / 2, height / 2))
                    }
                    return@Canvas
                }

                val points = chronological.mapIndexed { index, record ->
                    val x = (width / (chronological.size - 1)) * index
                    val normalizedY = (record.bmi - minBmi) / (maxBmi - minBmi)
                    val y = topPadding + (height - topPadding) * (1f - normalizedY.toFloat())
                    Offset(x, y)
                }

                val path = Path()
                points.forEachIndexed { i, p ->
                    if (i == 0) path.moveTo(p.x, p.y)
                    else {
                        val prev = points[i - 1]
                        val controlX1 = (prev.x + p.x) / 2
                        val controlY1 = prev.y
                        val controlX2 = (prev.x + p.x) / 2
                        val controlY2 = p.y
                        path.cubicTo(controlX1, controlY1, controlX2, controlY2, p.x, p.y)
                    }
                }

                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(points.last().x, size.height)
                    lineTo(points.first().x, size.height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(if (isDark) Color(0xFF38BDF8).copy(alpha = 0.35f) else Color(0xFF3B82F6).copy(alpha = 0.35f), Color.Transparent)
                    )
                )

                drawPath(
                    path = path,
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                points.forEachIndexed { idx, point ->
                    val statusColor = when {
                        chronological[idx].bmi < 18.5 -> Color(0xFF0284C7)
                        chronological[idx].bmi < 25.0 -> Color(0xFF22C55E)
                        chronological[idx].bmi < 30.0 -> Color(0xFFFF9800)
                        else -> Color(0xFFEF4444)
                    }
                    drawCircle(if (isDark) Color(0xFF1E293B) else Color.White, radius = 6.dp.toPx(), center = point)
                    drawCircle(statusColor, radius = 4.dp.toPx(), center = point)
                }
            }
        }
    }
}

@Composable
fun BmiSpectrumBar(latestBmi: Double) {
    val isDark = isSystemInDarkTheme()
    val normalizedPosition = remember(latestBmi) {
        ((latestBmi - 12.0) / (40.0 - 12.0)).coerceIn(0.0, 1.0).toFloat()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.progress_zone_spectrum),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0284C7),
                                Color(0xFF22C55E),
                                Color(0xFFFF9800),
                                Color(0xFFEF4444)
                            )
                        )
                    )
            )

            BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(20.dp)) {
                val xPos = maxWidth * normalizedPosition
                Icon(
                    imageVector = Icons.Default.ArrowDropUp,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                    modifier = Modifier
                        .offset(x = xPos - 12.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

@Composable
fun DietAdviceScreen(initialCategoryIndex: Int = 1) {
    val isDark = isSystemInDarkTheme()
    var selectedCategory by remember { mutableIntStateOf(initialCategoryIndex) }

    val categories = listOf(
        stringResource(R.string.underweight),
        stringResource(R.string.normal),
        stringResource(R.string.overweight),
        stringResource(R.string.obese)
    )

    val (macros, mealPlanRes, cardColor) = when (selectedCategory) {
        0 -> Triple(
            Triple(55, 25, 20),
            R.string.diet_underweight_plan,
            Color(0xFF0284C7)
        )
        1 -> Triple(
            Triple(50, 30, 20),
            R.string.diet_normal_plan,
            Color(0xFF22C55E)
        )
        2 -> Triple(
            Triple(40, 35, 25),
            R.string.diet_overweight_plan,
            Color(0xFFFF9800)
        )
        else -> Triple(
            Triple(30, 40, 30),
            R.string.diet_obese_plan,
            Color(0xFFEF4444)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ThreeDCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Restaurant, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    stringResource(R.string.diet_advice_title),
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = if (isDark) Color.White else Color(0xFF1A237E)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.diet_select_category), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFF334155))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEachIndexed { index, catName ->
                    val isSelected = selectedCategory == index
                    Surface(
                        onClick = { selectedCategory = index },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) cardColor else (if (isDark) Color(0xFF1E293B) else Color.White),
                        border = BorderStroke(1.dp, if (isSelected) cardColor else (if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFCBD5E1)))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                catName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else (if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155)),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ThreeDCard {
            Text(stringResource(R.string.diet_macro_title), fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroChip(stringResource(R.string.diet_carbs, macros.first), Color(0xFF3B82F6))
                MacroChip(stringResource(R.string.diet_protein, macros.second), Color(0xFF22C55E))
                MacroChip(stringResource(R.string.diet_fats, macros.third), Color(0xFFEAB308))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ThreeDCard {
            Text(stringResource(R.string.diet_meal_plan_title), fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stringResource(mealPlanRes),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF1E293B),
                lineHeight = 22.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ThreeDCard {
            Text(stringResource(R.string.diet_swaps_title), fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
            Spacer(modifier = Modifier.height(12.dp))

            FoodSwapRow(
                stringResource(R.string.swap_from_1),
                stringResource(R.string.swap_to_1),
                stringResource(R.string.swap_reason_1)
            )
            FoodSwapRow(
                stringResource(R.string.swap_from_2),
                stringResource(R.string.swap_to_2),
                stringResource(R.string.swap_reason_2)
            )
            FoodSwapRow(
                stringResource(R.string.swap_from_3),
                stringResource(R.string.swap_to_3),
                stringResource(R.string.swap_reason_3)
            )
            FoodSwapRow(
                stringResource(R.string.swap_from_4),
                stringResource(R.string.swap_to_4),
                stringResource(R.string.swap_reason_4)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ThreeDCard {
            Text(stringResource(R.string.diet_golden_rules), fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
            Spacer(modifier = Modifier.height(10.dp))

            Text(stringResource(R.string.diet_rule_1), fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155), lineHeight = 20.sp)
            Text(stringResource(R.string.diet_rule_2), fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155), lineHeight = 20.sp)
            Text(stringResource(R.string.diet_rule_3), fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155), lineHeight = 20.sp)
            Text(stringResource(R.string.diet_rule_4), fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155), lineHeight = 20.sp)
        }
    }
}

@Composable
fun MacroChip(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@Composable
fun FoodSwapRow(from: String, to: String, reason: String) {
    val isDark = isSystemInDarkTheme()
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(from, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
            Text(" ➔ ", fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
            Text(to, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF22C55E))
        }
        Text("• $reason", fontSize = 10.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
    }
}

@Composable
fun BmiInfoScreen() {
    val isDark = isSystemInDarkTheme()
    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        ThreeDCard {
            Text(stringResource(id = R.string.what_is_bmi), fontWeight = FontWeight.Black, fontSize = 20.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.bmi_explanation), fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF475569))
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(id = R.string.bmi_categories), fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
            Spacer(modifier = Modifier.height(12.dp))
            BmiInfoRow(stringResource(R.string.underweight), "< 18.5", Color(0xFF64748B))
            BmiInfoRow(stringResource(R.string.normal), "18.5 - 24.9", Color(0xFF22C55E))
            BmiInfoRow(stringResource(R.string.overweight), "25 - 29.9", Color(0xFFF59E0B))
            BmiInfoRow(stringResource(R.string.obese), "> 30", Color(0xFFEF4444))
        }
    }
}

@Composable
fun BmiInfoRow(label: String, range: String, color: Color) {
    val isDark = isSystemInDarkTheme()
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
        Text(range, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.Black)
    }
}

@Composable
fun BmiResultCard(bmi: Double, onViewDietPlan: ((Double) -> Unit)? = null) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val (status, color) = when {
        bmi < 18.5 -> stringResource(id = R.string.underweight) to Color(0xFF64748B)
        bmi < 25.0 -> stringResource(id = R.string.normal) to Color(0xFF22C55E)
        bmi < 30.0 -> stringResource(id = R.string.overweight) to Color(0xFFF59E0B)
        else -> stringResource(id = R.string.obese) to Color(0xFFEF4444)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = color),
        shape = RoundedCornerShape(32.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.92f),
        border = BorderStroke(2.dp, color.copy(alpha = if (isDark) 0.6f else 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.your_bmi, "%.1f".format(bmi)), 
                fontSize = 38.sp, 
                fontWeight = FontWeight.Black, 
                color = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = color, 
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.shadow(6.dp, RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = status.uppercase(), 
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), 
                    color = Color.White, 
                    fontSize = 13.sp, 
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (onViewDietPlan != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onViewDietPlan(bmi)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.diet_btn_view_plan),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
