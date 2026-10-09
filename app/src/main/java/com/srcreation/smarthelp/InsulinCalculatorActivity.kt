package com.srcreation.smarthelp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme

class InsulinCalculatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                InsulinCalculatorScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsulinCalculatorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    var selectedMeal by remember { mutableStateOf("Fasting") }
    var selectedDoseType by remember { mutableStateOf("Medium") }
    var selectedUnit by remember { mutableStateOf("mg/dL") }
    var sugarInput by remember { mutableStateOf("") }

    val rawResult = remember(sugarInput, selectedUnit, selectedDoseType) {
        calculateInsulinResult(sugarInput, selectedUnit, selectedDoseType)
    }

    val displayResult = when (rawResult) {
        "CALL DOCTOR" -> stringResource(id = R.string.insulin_call_doctor)
        else -> rawResult
    }

    Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(id = R.string.insulin_calc_title), fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    actions = { LanguageToggleButton() },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
            bottomBar = { AppBottomBar(context = context, currentTab = -1, backgroundBrush = AppGradientBrush) }
        ) { innerPadding ->
            ScreenEntranceAnimation {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header
                    Text(
                        text = stringResource(id = R.string.insulin_type_actrapid),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )

                    // Meal Selection
                    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(id = R.string.insulin_select_meal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val meals = listOf(
                                "Fasting" to stringResource(id = R.string.insulin_fasting),
                                "Before Meal" to stringResource(id = R.string.food_before_meal),
                                "After Meal" to stringResource(id = R.string.food_after_meal)
                            )
                            meals.forEach { (key, label) ->
                                FilterChip(
                                    selected = selectedMeal == key,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedMeal = key
                                    },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    // Dose Type Selection
                    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(id = R.string.insulin_dose_sensitivity), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val sensitivities = listOf(
                                "Low" to stringResource(id = R.string.insulin_sensitivity_low),
                                "Medium" to stringResource(id = R.string.insulin_sensitivity_medium),
                                "High" to stringResource(id = R.string.insulin_sensitivity_high)
                            )
                            sensitivities.forEach { (key, label) ->
                                FilterChip(
                                    modifier = Modifier.weight(1f),
                                    selected = selectedDoseType == key,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedDoseType = key
                                    },
                                    label = { 
                                        Text(
                                            text = label,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) 
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (key == "High") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }

                    // Unit Selection & Sugar Input
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(id = R.string.insulin_sugar_level), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("mg/dL", "mmol/L").forEach { unit ->
                                        FilterChip(
                                            selected = selectedUnit == unit,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                selectedUnit = unit
                                            },
                                            label = { Text(unit, fontSize = 10.sp) },
                                            modifier = Modifier.height(32.dp)
                                        )
                                    }
                                }
                            }
                            
                            OutlinedTextField(
                                value = sugarInput,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.all { it.isDigit() || it == '.' || it == ',' }) {
                                        if (input.count { it == '.' || it == ',' } <= 1) {
                                            sugarInput = input
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(stringResource(id = R.string.insulin_enter_value)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    cursorColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    // Result Display
                    Card(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (rawResult == "CALL DOCTOR") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(id = R.string.insulin_recommended_dose),
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = displayResult,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = if (rawResult == "CALL DOCTOR") 28.sp else 48.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            if (rawResult != "0 Units" && rawResult != "--" && rawResult != "CALL DOCTOR") {
                                Text(
                                    text = stringResource(id = R.string.insulin_units_actrapid),
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    // Warning Note
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(id = R.string.insulin_disclaimer),
                            modifier = Modifier.padding(12.dp),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

fun calculateInsulinResult(input: String, unit: String, doseType: String): String {
    if (input.isEmpty()) return "--"
    
    // Handle comma as decimal separator and parse
    val normalizedInput = input.replace(',', '.')
    val sugar = normalizedInput.toDoubleOrNull() ?: return "Invalid"
    
    // Convert to mg/dL for consistent logic (1 mmol/L ≈ 18 mg/dL)
    val sugarMg = if (unit == "mmol/L") sugar * 18.0 else sugar

    return when {
        sugarMg < 0 -> "Invalid"
        sugarMg <= 150 -> "0 Units"
        sugarMg <= 200 -> when (doseType) {
            "Low" -> "2 Units"
            "Medium" -> "4 Units"
            else -> "8 Units"
        }
        sugarMg <= 250 -> when (doseType) {
            "Low" -> "4 Units"
            "Medium" -> "8 Units"
            else -> "12 Units"
        }
        sugarMg <= 300 -> when (doseType) {
            "Low" -> "6 Units"
            "Medium" -> "10 Units"
            else -> "16 Units"
        }
        sugarMg <= 350 -> when (doseType) {
            "Low" -> "8 Units"
            "Medium" -> "12 Units"
            else -> "20 Units"
        }
        sugarMg <= 400 -> when (doseType) {
            "Low" -> "10 Units"
            "Medium" -> "16 Units"
            else -> "24 Units"
        }
        else -> "CALL DOCTOR"
    }
}
