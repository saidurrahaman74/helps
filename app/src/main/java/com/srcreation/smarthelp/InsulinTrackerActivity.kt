package com.srcreation.smarthelp

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.InsulinEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.text.SimpleDateFormat
import java.util.*

class InsulinTrackerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ActivityGateHelper.checkGateAndRender(this) {
            setContent {
                HelpsTheme {
                    InsulinTrackerScreen(onBack = { finish() })
                }
            }
        }
    }
}

@Composable
fun getLocalizedMealContext(meal: String): String {
    return when (meal.trim().lowercase()) {
        "fasting" -> stringResource(R.string.insulin_fasting)
        "before meal" -> stringResource(R.string.food_before_meal)
        "after meal" -> stringResource(R.string.food_after_meal)
        else -> meal
    }
}

@Composable
fun getLocalizedDayTime(dayTime: String): String {
    return when (dayTime.trim().lowercase()) {
        "morning" -> stringResource(R.string.time_morning)
        "noon" -> stringResource(R.string.time_noon)
        "evening" -> stringResource(R.string.time_evening)
        "night" -> stringResource(R.string.time_night)
        else -> dayTime
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsulinTrackerScreen(viewModel: InsulinViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var glucoseLevel by rememberSaveable { mutableStateOf("") }
    var glucoseUnit by rememberSaveable { mutableStateOf("mg/dL") }
    var insulinDose by rememberSaveable { mutableStateOf("") }
    var insulinUnit by rememberSaveable { mutableStateOf("Units") }
    var selectedMealTime by rememberSaveable { mutableStateOf("Fasting") }
    var selectedDayTime by rememberSaveable { mutableStateOf("Morning") }
    val insulinRecords by viewModel.allRecords.collectAsState(initial = emptyList())
    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }
    var recordToDelete by remember { mutableStateOf<InsulinEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.tool_insulin), fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = if (isDark) Color.White else Color(0xFF1A237E))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        exportInsulinToPdf(context, insulinRecords, "", "")
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = if (isDark) Color.White else Color(0xFF1A237E))
                    }
                    LanguageToggleButton()
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { AppBottomBar(context = context, currentTab = 1, backgroundBrush = AppGradientBrush) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (insulinRecords.isNotEmpty()) {
                    AdvancedLineChart(
                        dataPoints = insulinRecords.takeLast(7).map { it.glucose.toFloatOrNull() ?: 0f },
                        label = stringResource(R.string.insulin_chart_label),
                        lineColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        targetRange = 70f..140f
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                InsulinSummaryCard(insulinRecords.firstOrNull(), isDark)
                Spacer(modifier = Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.8f)),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(stringResource(R.string.insulin_add_title), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.insulin_meal_context), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Fasting", "Before Meal", "After Meal").forEach { time ->
                                val isSelected = selectedMealTime == time
                                Surface(
                                    modifier = Modifier.weight(1f).height(45.dp).clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedMealTime = time
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(getLocalizedMealContext(time), color = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White else Color.Black), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(stringResource(R.string.insulin_time_of_day), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Morning", "Noon", "Evening", "Night").forEach { dayTime ->
                                val isSelected = selectedDayTime == dayTime
                                Surface(
                                    modifier = Modifier.weight(1f).height(45.dp).clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedDayTime = dayTime
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(getLocalizedDayTime(dayTime), color = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White else Color.Black), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.insulin_glucose_unit), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FilterChip(
                                        selected = glucoseUnit == "mg/dL", 
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            glucoseUnit = "mg/dL"
                                        }, 
                                        label = { Text("mg/dL", fontSize = 10.sp, maxLines = 1) }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    FilterChip(
                                        selected = glucoseUnit == "mmol/L", 
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            glucoseUnit = "mmol/L"
                                        }, 
                                        label = { Text("mmol/L", fontSize = 10.sp, maxLines = 1) }
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.insulin_unit_label), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    FilterChip(
                                        selected = insulinUnit == "Units", 
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            insulinUnit = "Units"
                                        }, 
                                        label = { Text("Units", fontSize = 10.sp, maxLines = 1) }
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    FilterChip(
                                        selected = insulinUnit == "IU", 
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            insulinUnit = "IU"
                                        }, 
                                        label = { Text("IU", fontSize = 10.sp, maxLines = 1) }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = glucoseLevel, onValueChange = { glucoseLevel = it },
                                label = { Text(stringResource(R.string.insulin_glucose_hint)) },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDark) Color.White else Color.Black,
                                    unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                    focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                                    focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                    unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                                )
                            )
                            OutlinedTextField(
                                value = insulinDose, onValueChange = { insulinDose = it },
                                label = { Text(stringResource(R.string.insulin_dose_hint)) },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDark) Color.White else Color.Black,
                                    unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                    focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                                    focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                    unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (glucoseLevel.isNotEmpty()) {
                                    val now = Date()
                                    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(now)
                                    val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)
                                    viewModel.addRecord(glucoseLevel, glucoseUnit, insulinDose, insulinUnit, selectedMealTime, selectedDayTime, date, time)
                                    glucoseLevel = ""; insulinDose = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                        ) {
                            Icon(Icons.Default.MedicalInformation, contentDescription = null, tint = if (isDark) Color.Black else Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.insulin_btn_save), fontWeight = FontWeight.Bold, color = if (isDark) Color.Black else Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.insulin_log_history), fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E))
                    Text(stringResource(R.string.insulin_records_count, insulinRecords.size), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                }
                Spacer(modifier = Modifier.height(12.dp))
                val groupedRecords = insulinRecords.groupBy { it.date }
                groupedRecords.forEach { (date, dailyLogs) ->
                    Text(date, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
                    dailyLogs.forEach { record ->
                        DoctorFriendlyLogCard(record, isDark, onDelete = {
                            recordToDelete = record
                            showDeleteDialog = true
                        })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showDeleteDialog && recordToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(if (isBn) "লগ ডিলিট নিশ্চিতকরণ" else "Delete Log Confirmation", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E)) },
                text = {
                    Text(
                        if (isBn) "আপনি কি নিশ্চিত যে ${recordToDelete!!.date} (${recordToDelete!!.time}) এর ইনসুলিন/গ্লুকোজ রেকর্ডটি ডিলিট করতে চান?"
                        else "Are you sure you want to delete the log recorded on ${recordToDelete!!.date} (${recordToDelete!!.time})?",
                        color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.DarkGray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteRecord(recordToDelete!!)
                            showDeleteDialog = false
                            recordToDelete = null
                            Toast.makeText(context, if (isBn) "রেকর্ড ডিলিট করা হয়েছে" else "Log deleted successfully", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text(if (isBn) "ডিলিট করুন" else "Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(if (isBn) "বাতিল" else "Cancel", color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    }
                },
                containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun DoctorFriendlyLogCard(record: InsulinEntity, isDark: Boolean, onDelete: () -> Unit) {
    val glucoseValue = record.glucose.toDoubleOrNull() ?: 0.0
    val isHigh = if (record.unit == "mg/dL") glucoseValue > 140.0 else glucoseValue > 7.8
    val isLow = if (record.unit == "mg/dL") glucoseValue < 70.0 else glucoseValue < 3.9
    val statusColor = when {
        isHigh -> Color(0xFFF44336)
        isLow -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else statusColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(statusColor, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${getLocalizedDayTime(record.dayTime)} - ${getLocalizedMealContext(record.mealTime)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(record.time, fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Log", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = if (isDark) Color(0xFF1E293B) else Color.LightGray.copy(alpha = 0.3f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.insulin_label_glucose), fontSize = 10.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(record.glucose, fontSize = 24.sp, fontWeight = FontWeight.Black, color = statusColor)
                        Text(" ${record.unit}", fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.insulin_label_dose), fontSize = 10.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
                    if (record.dose.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(record.dose, fontSize = 24.sp, fontWeight = FontWeight.Black, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                            Text(" ${record.iUnit}", fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                        }
                    } else {
                        Text(stringResource(R.string.insulin_none), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    }
                }
            }
            if (isHigh || isLow) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isHigh) stringResource(R.string.insulin_advice_high) else stringResource(R.string.insulin_advice_low),
                        modifier = Modifier.padding(8.dp),
                        fontSize = 11.sp,
                        color = statusColor,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InsulinSummaryCard(lastRecord: InsulinEntity?, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color(0xFF1A237E)),
        border = BorderStroke(1.5.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.insulin_latest_reading), color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = lastRecord?.glucose ?: "--", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(" ${lastRecord?.unit ?: "mg/dL"}", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp, modifier = Modifier.padding(bottom = 10.dp))
            }
            if (lastRecord != null && lastRecord.dose.isNotEmpty()) {
                Text(text = stringResource(R.string.insulin_administered, lastRecord.dose, lastRecord.iUnit), color = if (isDark) Color(0xFF38BDF8) else Color.Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

fun exportInsulinToPdf(context: Context, records: List<InsulinEntity>, patientName: String, patientAge: String) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    paint.textSize = 20f
    paint.isFakeBoldText = true
    canvas.drawText("Smart Help - Insulin & Glucose Report", 50f, 50f, paint)

    paint.textSize = 11f
    paint.isFakeBoldText = false
    
    val timestamp = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
    canvas.drawText("Generated on: $timestamp", 50f, 78f, paint)
    canvas.drawLine(50f, 95f, 545f, 95f, paint)
    
    var yPos = 125f
    val rowHeight = 25f

    // Table Header
    paint.isFakeBoldText = true
    canvas.drawText("Date", 40f, yPos, paint)
    canvas.drawText("Time", 140f, yPos, paint)
    canvas.drawText("Meal/Day", 230f, yPos, paint)
    canvas.drawText("Glucose", 360f, yPos, paint)
    canvas.drawText("Insulin", 480f, yPos, paint)
    paint.isFakeBoldText = false
    
    yPos += 30f
    canvas.drawLine(40f, yPos - 15f, 555f, yPos - 15f, paint)

    records.forEach { r ->
        if (yPos > 800f) return@forEach 
        canvas.drawText(r.date, 40f, yPos, paint)
        canvas.drawText(r.time, 140f, yPos, paint)
        canvas.drawText("${r.mealTime}/${r.dayTime}", 230f, yPos, paint)
        canvas.drawText("${r.glucose} ${r.unit}", 360f, yPos, paint)
        canvas.drawText(if(r.dose.isNotEmpty()) "${r.dose} ${r.iUnit}" else "None", 480f, yPos, paint)
        yPos += rowHeight
    }

    pdfDocument.finishPage(page)

    val fileName = "SmartHelp_Insulin_Report_${System.currentTimeMillis()}.pdf"
    
    try {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/SmartHelp")
        }
        val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                pdfDocument.writeTo(os)
                Toast.makeText(context, "PDF saved to Downloads/SmartHelp", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}
