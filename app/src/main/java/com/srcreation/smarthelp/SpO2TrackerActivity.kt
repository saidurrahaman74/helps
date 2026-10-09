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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.SpO2Entity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.text.SimpleDateFormat
import java.util.*

class SpO2TrackerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                SpO2TrackerScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpO2TrackerScreen(viewModel: SpO2ViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var spo2Input by rememberSaveable { mutableStateOf("") }
    var pulseInput by rememberSaveable { mutableStateOf("") }
    var breathingStatus by rememberSaveable { mutableStateOf("Normal Breathing") }
    var patientName by rememberSaveable { mutableStateOf("") }
    var patientAge by rememberSaveable { mutableStateOf("") }

    val records by viewModel.allRecords.collectAsState(initial = emptyList())
    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }
    val isDark = isSystemInDarkTheme()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val spo2Primary = Color(0xFF0284C7)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isBn) "অক্সিজেন স্যাচুরেশন (SpO2)" else "Oxygen Saturation (SpO2)", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) Color.White else Color(0xFF1A237E))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        exportSpO2ToPdf(context, records, patientName, patientAge, isBn)
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = spo2Primary)
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
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Patient Name & Age Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = patientName,
                            onValueChange = { patientName = it },
                            label = { Text(if (isBn) "রোগীর নাম (Patient Name)" else "Patient Name") },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
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
                            value = patientAge,
                            onValueChange = { patientAge = it },
                            label = { Text(if (isBn) "বয়স (Age)" else "Age") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                // New SpO2 Input Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)),
                    border = BorderStroke(1.2.dp, spo2Primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = spo2Primary.copy(alpha = 0.15f)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Air, null, tint = spo2Primary, modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = if (isBn) "অক্সিজেন স্যাচুরেশন রিডিং" else "New SpO2 Reading",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Breathing Status Selector
                        Text(if (isBn) "শ্বাসের অবস্থা (Breathing Status):" else "Breathing Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val statuses = listOf(
                                "Normal" to (if (isBn) "স্বাভাবিক" else "Normal"), 
                                "Shortness" to (if (isBn) "শ্বাসকষ্ট" else "Shortness"), 
                                "O2 Support" to (if (isBn) "অক্সিজেন" else "O2 Support")
                            )
                            statuses.forEach { (key, label) ->
                                FilterChip(
                                    selected = breathingStatus == key,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        breathingStatus = key
                                    },
                                    label = { Text(label, fontSize = 10.sp, textAlign = TextAlign.Center, maxLines = 1) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = spo2Input,
                                onValueChange = { if (it.all { char -> char.isDigit() }) spo2Input = it },
                                label = { Text(if (isBn) "SpO2 (%)" else "SpO2 (%)", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = if (isDark) Color.White else Color.Black),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDark) Color.White else Color.Black,
                                    unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                    focusedBorderColor = if (isDark) Color(0xFF38BDF8) else spo2Primary,
                                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                                    focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                    unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                                )
                            )

                            OutlinedTextField(
                                value = pulseInput,
                                onValueChange = { if (it.all { char -> char.isDigit() }) pulseInput = it },
                                label = { Text(if (isBn) "পাল্স (BPM)" else "Pulse (BPM)", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = if (isDark) Color.White else Color.Black),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDark) Color.White else Color.Black,
                                    unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                    focusedBorderColor = if (isDark) Color(0xFF38BDF8) else spo2Primary,
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
                                val spo2 = spo2Input.toIntOrNull()
                                val pulse = pulseInput.toIntOrNull() ?: 75
                                if (spo2 != null && spo2 in 50..100) {
                                    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                                    val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                    viewModel.addSpO2Record(spo2, pulse, breathingStatus, date, time)
                                    spo2Input = ""
                                    pulseInput = ""
                                    Toast.makeText(context, if (isBn) "অক্সিজেন স্যাচুরেশন সেভ হয়েছে! 🟢" else "SpO2 record saved successfully!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, if (isBn) "সঠিক SpO2 শতাংশ দিন (৫০ - ১০০)" else "Enter valid SpO2 percentage (50 - 100)", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = spo2Primary)
                        ) {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "SpO2 সেভ করুন" else "Save SpO2 Record", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Trend Chart
                if (records.isNotEmpty()) {
                    AdvancedTrendChart(
                        values = records.takeLast(10).map { it.spo2Percentage.toFloat() },
                        label = if (isBn) "📈 SpO2 স্যাচুরেশন ট্রেন্ড (%)" else "📈 SpO2 Saturation Trend (%)",
                        lineColor = spo2Primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // History Section
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBn) "পূর্ববর্তী SpO2 রেকর্ডসমূহ" else "Recorded SpO2 History",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = if (isDark) Color.White else Color(0xFF1A237E)
                    )

                    if (records.isNotEmpty()) {
                        TextButton(onClick = { showDeleteConfirmDialog = true }) {
                            Text(if (isBn) "সব মুছুন" else "Clear All", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

Spacer(modifier = Modifier.height(8.dp))

                if (records.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.5f))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isBn) "কোনো SpO2 রেকর্ড নেই। উপরে নতুন রিডিং যুক্ত করুন।" else "No SpO2 records logged yet. Add your reading above.",
                                fontSize = 13.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    records.forEach { record ->
                        SpO2RecordCard(record, isBn) {
                            viewModel.deleteRecord(record)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
                title = { Text(if (isBn) "সমস্ত হিস্ট্রি ডিলিট করুন" else "Clear All SpO2 History", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E)) },
                text = { Text(if (isBn) "আপনি কি নিশ্চিত যে আপনার সেভ করা সমস্ত SpO2 রেকর্ড মুছে ফেলতে চান?" else "Are you sure you want to clear all recorded SpO2 history?", color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.Black) },
                confirmButton = {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.clearAllRecords {
                                Toast.makeText(context, if (isBn) "সমস্ত রেকর্ড মুছে ফেলা হয়েছে" else "All records cleared", Toast.LENGTH_SHORT).show()
                            }
                            showDeleteConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text(if (isBn) "ডিলিট করুন" else "Delete All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text(stringResource(id = R.string.cancel), color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun SpO2RecordCard(record: SpO2Entity, isBn: Boolean, onDelete: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val (statusText, statusColor) = when {
        record.spo2Percentage < 81 -> Pair(if (isBn) "বিপদজনক হাইপক্সিয়া (<81%)" else "Danger Hypoxia (<81%)", Color(0xFFDC2626))
        record.spo2Percentage < 90 -> Pair(if (isBn) "হাইপোক্সিয়া (<90%)" else "Hypoxia Alert (<90%)", Color(0xFFEF4444))
        record.spo2Percentage in 90..94 -> Pair(if (isBn) "সতর্কতা (90-94%)" else "Borderline (90-94%)", Color(0xFFF59E0B))
        else -> Pair(if (isBn) "স্বাভাবিক (95-100%)" else "Normal (95-100%)", Color(0xFF22C55E))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Air, null, tint = statusColor, modifier = Modifier.size(24.dp))
                    }
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "${record.spo2Percentage}% SpO2", fontWeight = FontWeight.Black, fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                        Surface(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(text = "Pulse: ${record.pulseRate} bpm • ${record.date} at ${record.time}", fontSize = 11.sp, color = Color.Gray)
                }
            }

            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
            }) {
                Icon(Icons.Default.Delete, "Delete", tint = Color.Gray.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

fun exportSpO2ToPdf(context: Context, records: List<SpO2Entity>, patientName: String, patientAge: String, isBn: Boolean) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    paint.textSize = 20f
    paint.isFakeBoldText = true
    canvas.drawText("Smart Help - Oxygen Saturation (SpO2) Report", 50f, 50f, paint)

    paint.textSize = 11f
    paint.isFakeBoldText = false
    canvas.drawText("Generated on: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())}", 50f, 75f, paint)

    paint.isFakeBoldText = true
    canvas.drawText("Patient Details:", 50f, 110f, paint)
    paint.isFakeBoldText = false
    canvas.drawText("Name: ${patientName.ifBlank { "N/A" }}", 50f, 130f, paint)
    canvas.drawText("Age: ${patientAge.ifBlank { "N/A" }}", 250f, 130f, paint)

    canvas.drawLine(50f, 155f, 545f, 155f, paint)
    paint.isFakeBoldText = true
    canvas.drawText("SpO2 Saturation Records (%):", 50f, 185f, paint)
    paint.isFakeBoldText = false

    var yPos = 215f
    records.forEach { record ->
        if (yPos > 800f) return@forEach
        val status = when {
            record.spo2Percentage < 81 -> "Danger Hypoxia (<81%)"
            record.spo2Percentage < 90 -> "Hypoxia Alert (<90%)"
            record.spo2Percentage in 90..94 -> "Borderline (90-94%)"
            else -> "Normal (95-100%)"
        }
        canvas.drawText("${record.date} (${record.time}) - ${record.spo2Percentage}% SpO2 (Pulse: ${record.pulseRate} bpm) - $status", 50f, yPos, paint)
        yPos += 22f
    }

    pdfDocument.finishPage(page)
    val fileName = "SpO2_Report_${System.currentTimeMillis()}.pdf"

    try {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/SmartHelp")
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { os ->
                pdfDocument.writeTo(os)
                Toast.makeText(context, if (isBn) "পিডিএফ ডাউনলোডস ফোল্ডারে সেভ হয়েছে!" else "PDF saved to Downloads/SmartHelp", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}
