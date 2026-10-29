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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.BpEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BpTrackerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ActivityGateHelper.checkGateAndRender(this) {
            setContent {
                HelpsTheme {
                    BpTrackerScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BpTrackerScreen(viewModel: BpViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var systolic by rememberSaveable { mutableStateOf("") }
    var diastolic by rememberSaveable { mutableStateOf("") }
    var selectedTime by rememberSaveable { mutableStateOf("Morning") }
    var recordToDelete by remember { mutableStateOf<BpEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val bpRecords by viewModel.allRecords.collectAsState(initial = emptyList())
    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }
    val isDark = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.tool_bp_tracker), fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) },
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
                        exportToPdf(context, bpRecords, "", "")
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
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Advanced Interactive Chart
                if (bpRecords.isNotEmpty()) {
                    AdvancedTrendChart(
                        values = bpRecords.takeLast(7).map { it.systolic.toFloat() },
                        label = stringResource(id = R.string.health_progress) + " (SYS)"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.8f)),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(stringResource(R.string.bp_new_log_title), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Morning", "Noon", "Evening", "Night").forEach { time ->
                                val isSelected = selectedTime == time
                                Surface(
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .height(42.dp)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedTime = time
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) (if (isDark) Color(0xFF0284C7) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                        Text(getLocalizedBpTimeSlot(time), color = if (isSelected) Color.White else (if (isDark) Color.White.copy(alpha = 0.9f) else Color.Black), fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBn) "রক্তচাপের পরিমাণ টাইপ বা মুখে বলুন:" else "Enter or Speak BP Reading:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            VoiceInputIconButton(
                                promptText = "কথা বলুন (যেমন: ১২০ বাই ৮০)...",
                                iconSize = 18.dp
                            ) { spokenText, numbers, _ ->
                                if (numbers.size >= 2) {
                                    systolic = numbers[0].toString()
                                    diastolic = numbers[1].toString()
                                } else if (numbers.size == 1) {
                                    systolic = numbers[0].toString()
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = systolic,
                                onValueChange = { if (it.all { char -> char.isDigit() }) systolic = it },
                                label = { Text("SYS", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = if (isDark) Color.White else Color.Black),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDark) Color.White else Color.Black,
                                    unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                    focusedBorderColor = if (isDark) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                                    focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                    unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                                )
                            )
                            OutlinedTextField(
                                value = diastolic,
                                onValueChange = { if (it.all { char -> char.isDigit() }) diastolic = it },
                                label = { Text("DIA", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = if (isDark) Color.White else Color.Black),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isDark) Color.White else Color.Black,
                                    unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                    focusedBorderColor = if (isDark) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                                    focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                    unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                                )
                            )
                        }

                        val sysVal = systolic.toIntOrNull() ?: 0
                        val diaVal = diastolic.toIntOrNull() ?: 0
                        if (sysVal > 0 && diaVal > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            VisualRangeMeter(vitalType = "BP", val1 = sysVal.toDouble(), val2 = diaVal.toDouble(), unit = "mmHg")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (systolic.isNotEmpty() && diastolic.isNotEmpty()) {
                                    val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                                    viewModel.addRecord(
                                        systolic.toIntOrNull() ?: 0,
                                        diastolic.toIntOrNull() ?: 0,
                                        selectedTime,
                                        date
                                    )
                                    systolic = ""; diastolic = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF0284C7) else Color(0xFF1A237E),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.bp_save_analysis), fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        val lastRecord = bpRecords.firstOrNull()
                        if (lastRecord != null) {
                            Spacer(modifier = Modifier.height(20.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.bp_last_check), fontSize = 12.sp, color = if (isDark) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                                val (category, color) = getLocalizedBpCategory(lastRecord.systolic, lastRecord.diastolic, isDark)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${lastRecord.systolic}/${lastRecord.diastolic}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = color
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = color.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = category,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(stringResource(R.string.bp_logs_by_date), fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E), modifier = Modifier.align(Alignment.Start).padding(start = 24.dp))
                Spacer(modifier = Modifier.height(12.dp))
                
                val groupedRecords = bpRecords.groupBy { it.date }
                groupedRecords.forEach { (date, records) ->
                    DailyLogTable(date, records, isDark = isDark, onDeleteRecord = { record ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        recordToDelete = record
                        showDeleteDialog = true
                    })
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showDeleteDialog && recordToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
            title = { Text("রক্তচাপ লগ ডিলিট করুন", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E)) },
            text = {
                Text("আপনি কি নিশ্চিতভাবে ${recordToDelete!!.date} (${getLocalizedBpTimeSlot(recordToDelete!!.timeCategory)}) এর রেকর্ড (${recordToDelete!!.systolic}/${recordToDelete!!.diastolic} mmHg) ডিলিট করতে চান?", color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.Black)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecord(recordToDelete!!)
                        showDeleteDialog = false
                        recordToDelete = null
                        Toast.makeText(context, "রেকর্ড সফলভাবে ডিলিট করা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("ডিলিট (Delete)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("বাতিল", color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                }
            }
        )
    }
}

@Composable
fun getLocalizedBpTimeSlot(time: String): String {
    return when (time.trim().lowercase()) {
        "morning" -> stringResource(R.string.time_morning)
        "noon" -> stringResource(R.string.time_noon)
        "evening" -> stringResource(R.string.time_evening)
        "night" -> stringResource(R.string.time_night)
        else -> time
    }
}

@Composable
fun getLocalizedBpCategory(sys: Int, dia: Int, isDark: Boolean = isSystemInDarkTheme()): Pair<String, Color> {
    val isBn = LocalConfiguration.current.locales[0].language == "bn"
    val normalColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)
    return when {
        sys > 170 || dia > 110 -> (if (isBn) "বেশি বিপজ্জনক" else "Danger High") to Color(0xFFEF4444)
        sys >= 140 || dia >= 90 -> stringResource(R.string.bp_cat_high) to Color(0xFFF87171)
        sys >= 130 || dia >= 81 -> stringResource(R.string.bp_cat_mild_high) to Color(0xFFF59E0B)
        sys >= 121 || dia >= 70 -> stringResource(R.string.bp_cat_normal) to normalColor
        sys >= 100 || dia >= 61 -> stringResource(R.string.bp_cat_optimal) to Color(0xFF10B981)
        else -> stringResource(R.string.bp_cat_low) to Color(0xFF0284C7)
    }
}

@Composable
fun DailyLogTable(date: String, records: List<BpEntity>, isDark: Boolean = isSystemInDarkTheme(), onDeleteRecord: (BpEntity) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.width(65.dp)) {
                Text(
                    text = date, 
                    fontWeight = FontWeight.Bold, 
                    color = if (isDark) Color.White else Color(0xFF1A237E), 
                    fontSize = 11.sp
                )
            }
            
            VerticalDivider(modifier = Modifier.height(35.dp), color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("Morning", "Noon", "Evening", "Night").forEach { time ->
                    val record = records.find { it.timeCategory.equals(time, ignoreCase = true) }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(52.dp)
                            .clickable(enabled = record != null) {
                                if (record != null) {
                                    onDeleteRecord(record)
                                }
                            }
                    ) {
                        Text(getLocalizedBpTimeSlot(time), fontSize = 8.sp, color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black, fontWeight = FontWeight.Bold, maxLines = 1)
                        if (record != null) {
                            val (category, color) = getLocalizedBpCategory(record.systolic, record.diastolic, isDark)
                            Text("${record.systolic}/${record.diastolic}", fontWeight = FontWeight.Black, fontSize = 10.sp, color = color, maxLines = 1)
                            Text(category, fontSize = 7.sp, color = color, fontWeight = FontWeight.Bold, maxLines = 1)
                        } else {
                            Text("--/--", color = Color.LightGray, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

fun exportToPdf(context: Context, records: List<BpEntity>, patientName: String, patientAge: String) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    paint.textSize = 20f
    paint.isFakeBoldText = true
    canvas.drawText("Smart Help - Medical BP Report", 50f, 50f, paint)

    paint.textSize = 13f
    paint.isFakeBoldText = true
    canvas.drawText("Patient Name: ${patientName.ifEmpty { "N/A" }}   |   Age: ${patientAge.ifEmpty { "N/A" }}", 50f, 78f, paint)

    paint.textSize = 11f
    paint.isFakeBoldText = false
    
    canvas.drawText("Generated on: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())}", 50f, 102f, paint)
    canvas.drawLine(50f, 115f, 545f, 115f, paint)
    var yPos = 140f

    records.groupBy { it.date }.forEach { (date, dailyRecords) ->
        canvas.drawText("Date: $date", 50f, yPos, paint)
        yPos += 20f
        dailyRecords.forEach { r ->
            canvas.drawText("  • ${r.timeCategory}: ${r.systolic}/${r.diastolic} mmHg", 60f, yPos, paint)
            yPos += 20f
        }
        yPos += 10f
        if (yPos > 800f) return@forEach 
    }

    pdfDocument.finishPage(page)

    val fileName = "SmartHelp_BP_Report_${System.currentTimeMillis()}.pdf"
    
    try {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/SmartHelp")
        }
        
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
                Toast.makeText(context, "PDF saved to Downloads/SmartHelp", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}
