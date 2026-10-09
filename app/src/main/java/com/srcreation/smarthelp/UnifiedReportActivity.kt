package com.srcreation.smarthelp

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.*
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class UnifiedReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                UnifiedReportScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedReportScreen(viewModel: UnifiedReportViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    
    val bp by viewModel.bpRecords.collectAsState(initial = emptyList())
    val insulin by viewModel.insulinRecords.collectAsState(initial = emptyList())
    val steps by viewModel.stepRecords.collectAsState(initial = emptyList())
    val meds by viewModel.medicineRecords.collectAsState(initial = emptyList())
    val medicalId by viewModel.medicalId.collectAsState(initial = null)

    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var showShareSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Unified Report", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) Color.White else Color(0xFF1A237E))
                    }
                },
                actions = { LanguageToggleButton() },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { AppBottomBar(context = context, currentTab = 1, backgroundBrush = AppGradientBrush) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color(0xFF1A237E)),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Analytics, null, tint = if (isDark) Color(0xFF38BDF8) else Color.White, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Complete Health Summary", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Combining all your tracked metrics into one file.", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                ReportMetricItem("Blood Pressure Records", bp.size.toString(), Icons.Default.MonitorHeart, isDark)
                ReportMetricItem("Insulin Readings", insulin.size.toString(), Icons.Default.Bloodtype, isDark)
                ReportMetricItem("Days of Step Tracking", steps.size.toString(), Icons.Default.DirectionsWalk, isDark)
                ReportMetricItem("Active Medicines", meds.size.toString(), Icons.Default.Medication, isDark)

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch {
                            val uri = ReportManager(context).generateDoctorConsultationReport()
                            if (uri != null) {
                                ReportShareHelper.sharePdfUri(context, uri, "ALL")
                            } else {
                                Toast.makeText(context, "Failed to generate report PDF", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Share, null, tint = Color.White)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.btn_generate_doctor_report), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("This report is strictly for information. Consult a doctor for medical advice.", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontStyle = FontStyle.Italic)
            }
        }
    }

    if (showShareSheet && generatedPdfFile != null) {
        ReportShareBottomSheet(
            file = generatedPdfFile!!,
            isDark = isDark,
            onDismiss = { showShareSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportShareBottomSheet(
    file: File,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)).copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Share Doctor's Report",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else Color(0xFF1A237E)
            )

            Text(
                text = "Send your complete medical summary to your doctor or family",
                fontSize = 12.sp,
                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // 1. WhatsApp Button
            ShareOptionButton(
                title = "Share via WhatsApp 🟢",
                subtitle = "Send PDF directly to doctor on WhatsApp",
                icon = Icons.Default.Send,
                bgColor = Color(0xFF25D366),
                isDark = isDark,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    ReportShareHelper.sharePdfFile(context, file, "WHATSAPP")
                    onDismiss()
                }
            )

            Spacer(Modifier.height(12.dp))

            // 2. IMO Button
            ShareOptionButton(
                title = "Share via IMO 🔵",
                subtitle = "Send PDF via IMO Messenger",
                icon = Icons.Default.Chat,
                bgColor = Color(0xFF0088CC),
                isDark = isDark,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    ReportShareHelper.sharePdfFile(context, file, "IMO")
                    onDismiss()
                }
            )

            Spacer(Modifier.height(12.dp))

            // 3. Email Button
            ShareOptionButton(
                title = "Send via Email ✉️",
                subtitle = "Attach PDF to email for hospital or specialist",
                icon = Icons.Default.Email,
                bgColor = Color(0xFFEA4335),
                isDark = isDark,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    ReportShareHelper.sharePdfFile(context, file, "EMAIL")
                    onDismiss()
                }
            )

            Spacer(Modifier.height(12.dp))

            // 4. All Apps (System Share)
            ShareOptionButton(
                title = "All Apps (System Share) 📲",
                subtitle = "Share using any app on your phone",
                icon = Icons.Default.Share,
                bgColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                isDark = isDark,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    ReportShareHelper.sharePdfFile(context, file, "ALL")
                    onDismiss()
                }
            )

            Spacer(Modifier.height(12.dp))

            // 5. Save to Downloads
            ShareOptionButton(
                title = "Save to Phone Storage 📂",
                subtitle = "Save PDF file to Downloads/SmartHelp folder",
                icon = Icons.Default.Download,
                bgColor = Color(0xFF475569),
                isDark = isDark,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    ReportShareHelper.saveFileToDownloads(context, file, file.name)
                    onDismiss()
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun ShareOptionButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    bgColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(18.dp),
        color = bgColor.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, bgColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = bgColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1E293B)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
            }
        }
    }
}

@Composable
fun ReportMetricItem(label: String, count: String, icon: ImageVector, isDark: Boolean) {
    val activeColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = activeColor.copy(0.15f)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = activeColor, modifier = Modifier.size(20.dp)) }
        }
        Spacer(Modifier.width(16.dp))
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium, color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray)
        Text(count, fontWeight = FontWeight.Black, color = activeColor, fontSize = 18.sp)
    }
}

fun generateUnifiedPdf(
    context: Context,
    medicalId: MedicalIdEntity?,
    bp: List<BpEntity>,
    insulin: List<InsulinEntity>,
    steps: List<StepEntity>,
    meds: List<MedicineEntity>
): File? {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    var y = 50f
    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("Smart Help - Unified Health Report", 50f, y, paint)
    
    y += 40f
    paint.textSize = 14f
    paint.isFakeBoldText = false
    val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
    canvas.drawText("Generated on: $date", 50f, y, paint)
    
    y += 30f
    canvas.drawLine(50f, y, 545f, y, paint)
    
    // Medical ID Section
    y += 40f
    paint.isFakeBoldText = true
    paint.textSize = 18f
    canvas.drawText("Patient Profile (Medical ID)", 50f, y, paint)
    paint.isFakeBoldText = false
    paint.textSize = 12f
    y += 25f
    canvas.drawText("Name: ${medicalId?.fullName ?: "Not Set"}", 50f, y, paint)
    canvas.drawText("Blood Group: ${medicalId?.bloodGroup ?: "N/A"}", 300f, y, paint)
    y += 20f
    canvas.drawText("Age: ${medicalId?.age ?: "N/A"}", 50f, y, paint)
    canvas.drawText("Weight/Height: ${medicalId?.weight ?: "--"} kg / ${medicalId?.height ?: "--"} cm", 300f, y, paint)
    
    y += 30f
    canvas.drawLine(50f, y, 545f, y, paint)
    
    // BP Section
    y += 40f
    paint.isFakeBoldText = true
    canvas.drawText("Recent Blood Pressure Logs", 50f, y, paint)
    paint.isFakeBoldText = false
    y += 25f
    bp.take(5).forEach { r ->
        canvas.drawText("${r.date} (${r.timeCategory}): ${r.systolic}/${r.diastolic} mmHg", 60f, y, paint)
        y += 20f
    }
    
    // Insulin Section
    y += 20f
    paint.isFakeBoldText = true
    canvas.drawText("Recent Insulin/Glucose Logs", 50f, y, paint)
    paint.isFakeBoldText = false
    y += 25f
    insulin.take(5).forEach { r ->
        canvas.drawText("${r.date}: Glucose ${r.glucose} ${r.unit}, Dose ${r.dose} ${r.iUnit}", 60f, y, paint)
        y += 20f
    }
    
    // Medicine Section
    y += 20f
    paint.isFakeBoldText = true
    canvas.drawText("Active Medication Schedule", 50f, y, paint)
    paint.isFakeBoldText = false
    y += 25f
    meds.filter { it.isActive }.forEach { m ->
        canvas.drawText("• ${m.name} (${m.category}) - ${m.selectedTimes}", 60f, y, paint)
        y += 20f
    }

    pdfDocument.finishPage(page)
    val fileName = "Master_Health_Report_${System.currentTimeMillis()}.pdf"
    
    return try {
        val file = File(context.cacheDir, fileName)
        FileOutputStream(file).use { os -> pdfDocument.writeTo(os) }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to generate PDF report", Toast.LENGTH_SHORT).show()
        null
    } finally {
        pdfDocument.close()
    }
}
