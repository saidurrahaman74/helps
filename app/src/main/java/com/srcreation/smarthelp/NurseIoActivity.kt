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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.IoRecordEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NurseIoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                IoChartScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IoChartScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val db = AppDatabase.getDatabase(context)
    val pref = PreferenceManager(context)
    val scope = rememberCoroutineScope()
    
    val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    val activeProfileId = pref.getActiveProfile()
    val records by db.ioDao().getRecordsByDate(activeProfileId, today).collectAsState(initial = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }
    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }

    val totalIntake = records.filter { it.type == "Intake" }.sumOf { it.amountMl }
    val totalOutput = records.filter { it.type == "Output" }.sumOf { it.amountMl }
    val balance = totalIntake - totalOutput

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, shadowElevation = 0.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(id = R.string.intake_output_chart), fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
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
                                exportIoToPdf(context, records, totalIntake, totalOutput, balance, pref.getPatientName(), pref.getPatientAge())
                            }) {
                                Icon(Icons.Default.PictureAsPdf, "PDF Report", tint = if (isDark) Color.White else Color(0xFF1A237E))
                            }
                            LanguageToggleButton()
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )

                    // Subtag Banner
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = if (isDark) 0.25f else 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = if (isDark) 0.5f else 0.3f)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = if (isBn) "🌐 আন্তর্জাতিক ফ্লুইড ব্যালেন্স ও আই/ও মনিটরিং (WHO / INS Standard)" else "🌐 International Fluid Balance & I/O Monitoring (WHO / INS)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        bottomBar = { AppBottomBar(context = context, currentTab = -1, isNurseMode = true, backgroundBrush = AppGradientBrush) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showAddDialog = true
                },
                containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                contentColor = if (isDark) Color.Black else Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, stringResource(id = R.string.btn_add_record))
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        FluidBalanceSummaryCard(totalIntake, totalOutput, balance, isBn)
                    }

                    item {
                        Text(
                            text = if (isBn) "আজকের ফ্লুইড রেকর্ডস (Today's I/O Logs)" else "Today's Fluid Logs",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = if (isDark) Color.White else Color(0xFF1A237E),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    if (records.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                                Text(stringResource(id = R.string.no_fluid_records), color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(records) { record ->
                        IoRecordItem(record) {
                            scope.launch { db.ioDao().deleteRecord(record) }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddDialog) {
        AddIoRecordDialog(
            isBn = isBn,
            onDismiss = { showAddDialog = false },
            onSave = { type, category, amount ->
                scope.launch {
                    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    db.ioDao().insertRecord(
                        IoRecordEntity(
                            profileId = activeProfileId,
                            type = type,
                            category = category,
                            amountMl = amount,
                            time = time,
                            date = today
                        )
                    )
                    showAddDialog = false
                }
            }
        )
    }
}

@Composable
fun FluidBalanceSummaryCard(intake: Int, output: Int, balance: Int, isBn: Boolean) {
    val isDark = isSystemInDarkTheme()
    val balanceStatus = when {
        balance > 1000 -> if (isBn) "⚠️ অতিরিক্ত ফ্লুইড ওভারলোড (Positive Overload)" else "⚠️ Positive Fluid Overload"
        balance < -500 -> if (isBn) "⚠️ ডিহাইড্রেশন বা হাইপোভোলিমিয়া (Negative Balance)" else "⚠️ Negative Fluid Balance (Dehydration)"
        else -> if (isBn) "✅ আদর্শ ফ্লুইড ব্যালেন্স (Normal Balanced)" else "✅ Normal Balanced Fluid Status"
    }
    val balanceColor = when {
        balance > 1000 -> Color(0xFFFF9800)
        balance < -500 -> Color(0xFFEF4444)
        else -> Color(0xFF22C55E)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp), spotColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E)),
        shape = RoundedCornerShape(28.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.95f),
        border = BorderStroke(1.5.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color(0xFF1A237E).copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryMetric(if (isBn) "মোট ইনটেক (Total Intake)" else "Total Intake", "$intake ml", Color(0xFF0284C7))
                SummaryMetric(if (isBn) "মোট আউটপুট (Total Output)" else "Total Output", "$output ml", Color(0xFFEF4444))
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = if (isDark) Color.White.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(if (isBn) "নেট ফ্লুইড ব্যালেন্স" else "Net Fluid Balance", fontWeight = FontWeight.Black, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = balanceStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = balanceColor
                    )
                }
                Text(
                    text = "${if (balance > 0) "+" else ""}$balance ml",
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    color = balanceColor
                )
            }
        }
    }
}

@Composable
fun SummaryMetric(label: String, value: String, color: Color) {
    val isDark = isSystemInDarkTheme()
    Column {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
fun IoRecordItem(record: IoRecordEntity, onDelete: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val isIntake = record.type == "Intake"
    val accentColor = if (isIntake) Color(0xFF0284C7) else Color(0xFFEF4444)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(42.dp), shape = CircleShape, color = accentColor.copy(alpha = 0.12f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isIntake) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(record.category, fontWeight = FontWeight.Black, fontSize = 15.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                Text("⏰ ${record.time} hrs", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
            }
            
            Text("${record.amountMl} ml", fontWeight = FontWeight.Black, color = accentColor, fontSize = 17.sp)
            
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
            }) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444))
            }
        }
    }
}

@Composable
fun AddIoRecordDialog(isBn: Boolean, onDismiss: () -> Unit, onSave: (String, String, Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val intakeLabel = if (isBn) "ইনটেক (Intake)" else "Intake"
    val outputLabel = if (isBn) "আউটপুট (Output)" else "Output"
    
    var type by remember { mutableStateOf(intakeLabel) }
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    val categories = if (type == intakeLabel) {
        if (isBn) listOf("মৌখিক তরল (Oral Fluid)", "আইভি ফ্লুইড (IV Fluid)", "এনজি ফিড (NG Feed)", "ব্লাড প্রোডাক্ট (Blood)", "অন্যান্য (Other)")
        else listOf("Oral Fluid", "IV Fluid", "Blood Products", "NG Tube Feed", "TPN / Other")
    } else {
        if (isBn) listOf("প্রস্রাব (Urine)", "বমি (Vomitus)", "ড্রেনেজ (Drainage)", "পায়খানা (Stool)", "রক্তপাত (Blood Loss)")
        else listOf("Urine Output", "Vomitus", "Surgical Drainage", "Stool / Diarrhea", "Blood Loss")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
        title = { Text(if (isBn) "নতুন ফ্লুইড রেকর্ড যোগ করুন" else "Add Fluid Record", fontWeight = FontWeight.Black, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Type Switcher
                Row(modifier = Modifier.fillMaxWidth().background(if (isDark) Color.White.copy(0.1f) else Color.Gray.copy(0.1f), RoundedCornerShape(14.dp)).padding(4.dp)) {
                    listOf(intakeLabel, outputLabel).forEach { t ->
                        val selected = type == t
                        Surface(
                            modifier = Modifier.weight(1f).clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                type = t
                                category = ""
                            },
                            color = if (selected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(t, modifier = Modifier.padding(10.dp), textAlign = TextAlign.Center, color = if (selected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.9f) else Color.Black), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Text(if (isBn) "ক্যাটাগরি নির্বাচন করুন:" else "Select Category:", fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                category = cat
                            },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent,
                                labelColor = if (isDark) Color.White.copy(alpha = 0.9f) else Color.Black,
                                selectedContainerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                selectedLabelColor = if (isDark) Color.Black else Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amount = it },
                    label = { Text(if (isBn) "পরিমাণ (ml)" else "Amount (ml)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.5f),
                        focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                        unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent,
                        unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val amt = amount.toIntOrNull() ?: 0
                    val finalType = if (type == intakeLabel) "Intake" else "Output"
                    if (category.isNotEmpty() && amt > 0) onSave(finalType, category, amt) 
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
            ) {
                Text(if (isBn) "সংরক্ষণ (Save)" else "Save", fontWeight = FontWeight.Bold, color = if (isDark) Color.Black else Color.White)
            }
        },
        dismissButton = { 
            TextButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            }) { 
                Text(if (isBn) "বাতিল" else "Cancel", color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold) 
            } 
        },
        shape = RoundedCornerShape(24.dp)
    )
}

fun exportIoToPdf(context: Context, records: List<IoRecordEntity>, totalIntake: Int, totalOutput: Int, balance: Int, patientName: String, patientAge: String) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.BLACK
        canvas.drawText("Intake / Output Fluid Chart Report", 40f, 50f, paint)

        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Patient: ${patientName.ifEmpty { "Patient" }} | Age: $patientAge", 40f, 80f, paint)
        val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        canvas.drawText("Date: $today", 40f, 100f, paint)

        paint.isFakeBoldText = true
        canvas.drawText("Total Intake: $totalIntake ml  |  Total Output: $totalOutput ml  |  Net Balance: $balance ml", 40f, 130f, paint)

        var y = 170f
        paint.isFakeBoldText = true
        canvas.drawText("Time", 40f, y, paint)
        canvas.drawText("Type", 120f, y, paint)
        canvas.drawText("Category", 220f, y, paint)
        canvas.drawText("Amount", 420f, y, paint)

        paint.isFakeBoldText = false
        y += 20f

        records.forEach { record ->
            if (y > 800f) return@forEach
            canvas.drawText(record.time, 40f, y, paint)
            canvas.drawText(record.type, 120f, y, paint)
            canvas.drawText(record.category, 220f, y, paint)
            canvas.drawText("${record.amountMl} ml", 420f, y, paint)
            y += 20f
        }

        pdfDocument.finishPage(page)

        val fileName = "IO_Report_${System.currentTimeMillis()}.pdf"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
        }

        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                pdfDocument.writeTo(os)
            }
            Toast.makeText(context, "PDF Report saved to Downloads!", Toast.LENGTH_LONG).show()
        }
        pdfDocument.close()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to save PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
