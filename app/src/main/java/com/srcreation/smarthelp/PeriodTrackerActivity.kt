package com.srcreation.smarthelp

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.PeriodEntity
import com.srcreation.smarthelp.db.PeriodLogEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class PeriodTrackerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ActivityGateHelper.checkGateAndRender(this) {
            setContent {
                HelpsTheme {
                    PeriodTrackerScreen(onBack = { finish() })
                }
            }
        }
    }
}

val PeriodPrimary = Color(0xFFFF4081)
val OvulationColor = Color(0xFF00BFA5)
val FertileColor = Color(0xFF00E5FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodTrackerScreen(viewModel: PeriodViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val calendar = Calendar.getInstance()
    val isBn = remember { Locale.getDefault().language == "bn" }
    val isDark = isSystemInDarkTheme()
    
    val settings by viewModel.settings.collectAsState(initial = null)
    
    var lastPeriodDate by remember { mutableStateOf<Date?>(null) }
    var cycleLength by remember { mutableFloatStateOf(28f) }
    var periodDuration by remember { mutableFloatStateOf(5f) }
    var isInitialized by remember { mutableStateOf(false) }
    
    LaunchedEffect(settings) {
        settings?.let {
            if (!isInitialized) {
                lastPeriodDate = it.lastPeriodDate?.let { ts -> Date(ts) }
                cycleLength = it.cycleLength.toFloat()
                periodDuration = it.periodDuration.toFloat()
                isInitialized = true
            }
        }
    }

    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var daysUntilNext by remember { mutableIntStateOf(0) }
    var nextPeriodStart by remember { mutableStateOf("---") }
    var nextPeriodEnd by remember { mutableStateOf("---") }
    var ovulationStart by remember { mutableStateOf("---") }
    var ovulationEnd by remember { mutableStateOf("---") }
    var isOverdue by remember { mutableStateOf(false) }

    LaunchedEffect(lastPeriodDate, cycleLength, periodDuration) {
        lastPeriodDate?.let { date ->
            val cycleDays = cycleLength.toInt()
            val durationDays = periodDuration.toInt()
            
            // Next Period Calculation
            val nextStartCal = Calendar.getInstance().apply { time = date; add(Calendar.DAY_OF_YEAR, cycleDays) }
            val nextEndCal = Calendar.getInstance().apply { time = nextStartCal.time; add(Calendar.DAY_OF_YEAR, durationDays - 1) }
            nextPeriodStart = sdf.format(nextStartCal.time)
            nextPeriodEnd = sdf.format(nextEndCal.time)
            
            // Ovulation Window Calculation (Approx 14 days before next period)
            val ovulationPeakCal = Calendar.getInstance().apply { time = nextStartCal.time; add(Calendar.DAY_OF_YEAR, -14) }
            val ovStartCal = Calendar.getInstance().apply { time = ovulationPeakCal.time; add(Calendar.DAY_OF_YEAR, -3) }
            val ovEndCal = Calendar.getInstance().apply { time = ovulationPeakCal.time; add(Calendar.DAY_OF_YEAR, 2) }
            ovulationStart = sdf.format(ovStartCal.time)
            ovulationEnd = sdf.format(ovEndCal.time)

            val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
            val diffInDays = TimeUnit.MILLISECONDS.toDays(nextStartCal.timeInMillis - today.timeInMillis).toInt()
            if (diffInDays < 0) { daysUntilNext = abs(diffInDays); isOverdue = true } else { daysUntilNext = diffInDays; isOverdue = false }
            
            viewModel.saveSettings(date.time, cycleDays, durationDays)
        }
    }

    var showLogDialog by remember { mutableStateOf(false) }
    val allLogs by viewModel.allLogs.collectAsState(initial = emptyList())

    val datePickerDialog = DatePickerDialog(context, { _, year, month, dayOfMonth ->
        val selected = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
        lastPeriodDate = selected.time
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    val periodGradient = Brush.verticalGradient(
        colors = if (isDark) listOf(Color(0xFF020617), Color(0xFF2A081D), Color(0xFF020617))
        else listOf(Color(0xFFFFF1F0), Color(0xFFFFE4E1))
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.period_tracker_title), fontSize = 18.sp, color = if (isDark) Color.White else PeriodPrimary, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = if (isDark) Color.White else PeriodPrimary) }
                },
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        exportPeriodToPdf(context, lastPeriodDate, cycleLength.toInt(), periodDuration.toInt(), allLogs)
                    }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Report", tint = PeriodPrimary)
                    }
                    LanguageToggleButton()
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { AppBottomBar(context = context, currentTab = -1, isWomenMode = true, backgroundBrush = periodGradient) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showLogDialog = true
                },
                containerColor = PeriodPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Symptoms")
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = periodGradient).padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Current Phase Guide Card
                val currentPhase = getCyclePhase(lastPeriodDate, cycleLength.toInt(), isBn)
                PhaseGuideCard(currentPhase)

                Spacer(modifier = Modifier.height(16.dp))
                
                // Active Date Selection Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f)
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(id = R.string.period_current_cycle), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.CalendarMonth, null, tint = PeriodPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (lastPeriodDate == null) stringResource(id = R.string.period_no_date_selected)
                                       else "${sdf.format(lastPeriodDate!!)} - ${sdf.format(Date(lastPeriodDate!!.time + (periodDuration.toInt() - 1) * 86400000L))}",
                                fontWeight = FontWeight.ExtraBold,
                                color = PeriodPrimary,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                datePickerDialog.show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PeriodPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(id = R.string.period_btn_update_start), fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                CycleStatusRing(daysUntilNext, isOverdue, lastPeriodDate != null, isBn)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Predictions Section
                Text(text = stringResource(id = R.string.period_predictions), fontWeight = FontWeight.Black, color = PeriodPrimary, modifier = Modifier.align(Alignment.Start).padding(start = 28.dp))
                Spacer(modifier = Modifier.height(12.dp))
                
                PredictionCard(
                    title = stringResource(id = R.string.period_next_period),
                    start = nextPeriodStart,
                    end = nextPeriodEnd,
                    color = PeriodPrimary,
                    icon = Icons.Default.WaterDrop
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                PredictionCard(
                    title = stringResource(id = R.string.period_fertile_window),
                    start = ovulationStart,
                    end = ovulationEnd,
                    color = OvulationColor,
                    icon = Icons.Default.Favorite
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = stringResource(id = R.string.period_symptom_logs), fontWeight = FontWeight.Black, color = PeriodPrimary, modifier = Modifier.align(Alignment.Start).padding(start = 28.dp))
                Spacer(modifier = Modifier.height(12.dp))
                if (allLogs.isEmpty()) {
                    Text(stringResource(id = R.string.period_no_logs_hint), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    allLogs.take(5).forEach { log ->
                        SymptomLogCard(log)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(text = stringResource(id = R.string.period_cycle_settings), fontWeight = FontWeight.Black, color = PeriodPrimary, modifier = Modifier.align(Alignment.Start).padding(start = 28.dp))
                Spacer(modifier = Modifier.height(12.dp))
                InputSettingsCard(cycle = cycleLength.toInt(), duration = periodDuration.toInt(), isBn = isBn, onCycleChange = { cycleLength = it }, onDurationChange = { periodDuration = it })
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
        
        if (showLogDialog) {
            SymptomLogDialog(
                isBn = isBn,
                onDismiss = { showLogDialog = false }
            ) { mood, flow, pain, symp ->
                viewModel.addLog(mood, flow, pain, symp)
                showLogDialog = false
            }
        }
    }
}

@Composable
fun PhaseGuideCard(phase: CyclePhase) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else phase.color.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, phase.color.copy(alpha = if (isDark) 0.5f else 0.25f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(phase.color.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(phase.icon, null, tint = phase.color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(stringResource(id = R.string.period_current_phase), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = phase.color)
                Text(phase.name, fontSize = 17.sp, fontWeight = FontWeight.Black, color = phase.color)
                Text(phase.desc, fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 15.sp)
            }
        }
    }
}

@Composable
fun SymptomLogCard(log: PeriodLogEntity) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(log.date, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Mood: ${log.mood}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PeriodPrimary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Flow: ${log.flow}", fontSize = 11.sp, color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface)
                Text("Pain: ${log.pain}", fontSize = 11.sp, color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface)
            }
            if (log.symptoms.isNotEmpty()) {
                Text("Symptoms: ${log.symptoms}", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant, fontStyle = FontStyle.Italic)
            }
        }
    }
}

@Composable
fun SymptomLogDialog(isBn: Boolean, onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var mood by rememberSaveable { mutableStateOf("Happy") }
    var flow by rememberSaveable { mutableStateOf("Medium") }
    var pain by rememberSaveable { mutableStateOf("None") }
    var symp by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
        title = { Text(stringResource(id = R.string.period_log_symptoms_title), fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Unspecified) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(id = R.string.period_mood), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Unspecified)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val moodList = listOf("Happy" to "😊 Happy", "Sad" to "😔 Sad", "Irritated" to "😠 Irritated")
                    moodList.forEach { (mKey, mLabel) ->
                        FilterChip(
                            selected = mood == mKey,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                mood = mKey
                            },
                            label = { Text(mLabel, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent,
                                labelColor = if (isDark) Color.White else Color.Black,
                                selectedContainerColor = PeriodPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Text(stringResource(id = R.string.period_flow), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Unspecified)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val flowList = listOf("Light" to "Light", "Medium" to "Medium", "Heavy" to "Heavy")
                    flowList.forEach { (fKey, fLabel) ->
                        FilterChip(
                            selected = flow == fKey,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                flow = fKey
                            },
                            label = { Text(fLabel, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent,
                                labelColor = if (isDark) Color.White else Color.Black,
                                selectedContainerColor = PeriodPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Text(stringResource(id = R.string.period_pain_level), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Unspecified)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val painList = listOf("None" to "None", "Mild" to "Mild", "Severe" to "Severe")
                    painList.forEach { (pKey, pLabel) ->
                        FilterChip(
                            selected = pain == pKey,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                pain = pKey
                            },
                            label = { Text(pLabel, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent,
                                labelColor = if (isDark) Color.White else Color.Black,
                                selectedContainerColor = PeriodPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = symp, onValueChange = { symp = it },
                    label = { Text(stringResource(id = R.string.period_other_symptoms)) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedBorderColor = PeriodPrimary,
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.5f),
                        focusedLabelColor = PeriodPrimary,
                        unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent,
                        unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent
                    )
                )
            }
        },
        confirmButton = { Button(onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSave(mood, flow, pain, symp)
        }, colors = ButtonDefaults.buttonColors(containerColor = PeriodPrimary)) { Text(stringResource(id = R.string.save), color = Color.White) } },
        dismissButton = { TextButton(onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onDismiss()
        }) { Text(stringResource(id = R.string.cancel), color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray) } }
    )
}

data class CyclePhase(val name: String, val desc: String, val color: Color, val icon: ImageVector)

fun getCyclePhase(startDate: Date?, cycleLength: Int, isBn: Boolean): CyclePhase {
    if (startDate == null) return CyclePhase(
        if (isBn) "তারিখ দেওয়া হয়নি" else "Unknown",
        if (isBn) "বর্তমান দশা দেখতে পিরিয়ডের তারিখ সিলেক্ট করুন।" else "Please select your last period date to see phase.",
        Color.Gray, Icons.Default.Info
    )
    
    val today = Calendar.getInstance()
    val start = Calendar.getInstance().apply { time = startDate }
    val dayInCycle = (((today.timeInMillis - start.timeInMillis) / 86400000L) % cycleLength).toInt()
    
    return when (dayInCycle) {
        in 0..5 -> CyclePhase(if (isBn) "ঋতুস্রাব দশা (Menstrual Phase)" else "Menstrual Phase", if (isBn) "বর্তমানে পিরিয়ড চলমান।" else "Your period is currently ongoing.", PeriodPrimary, Icons.Default.Opacity)
        in 6..12 -> CyclePhase(if (isBn) "ফোলিকুলার দশা (Follicular Phase)" else "Follicular Phase", if (isBn) "শরীর ও ডিম্বাশয় ডিম্বস্ফোটনের জন্য প্রস্তুত হচ্ছে।" else "Body is preparing for ovulation.", FertileColor, Icons.Default.WbSunny)
        in 13..15 -> CyclePhase(if (isBn) "ওভিউলেশন দশা (Ovulation Phase)" else "Ovulation Phase", if (isBn) "উর্বরতার সর্বোচ্চ সময় (গর্ভধারণের সম্ভাবনা বেশি)।" else "Peak fertility time. High chance of pregnancy.", OvulationColor, Icons.Default.Favorite)
        else -> CyclePhase(if (isBn) "লুটিয়াল দশা (Luteal Phase)" else "Luteal Phase", if (isBn) "সাইকেল সমাপ্তির দিকে (PMS বা মেজাজ পরিবর্তন হতে পারে)।" else "Cycle is winding down. PMS might occur.", Color(0xFF9C27B0), Icons.Default.Bedtime)
    }
}

fun exportPeriodToPdf(context: Context, startDate: Date?, cycle: Int, duration: Int, logs: List<PeriodLogEntity>) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    paint.textSize = 22f
    paint.isFakeBoldText = true
    canvas.drawText("Smart Help - Period Health Report", 50f, 50f, paint)

    paint.textSize = 12f
    paint.isFakeBoldText = false
    canvas.drawText("Generated on: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())}", 50f, 80f, paint)
    
    paint.isFakeBoldText = true
    canvas.drawText("Cycle Configuration:", 50f, 120f, paint)
    paint.isFakeBoldText = false
    canvas.drawText("Last Period: ${startDate?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it) } ?: "N/A"}", 50f, 140f, paint)
    canvas.drawText("Average Cycle: $cycle Days", 50f, 160f, paint)
    canvas.drawText("Period Duration: $duration Days", 50f, 180f, paint)

    canvas.drawLine(50f, 210f, 545f, 210f, paint)
    paint.isFakeBoldText = true
    canvas.drawText("Daily Symptom Logs:", 50f, 240f, paint)
    paint.isFakeBoldText = false

    var yPos = 270f
    logs.forEach { log ->
        if (yPos > 800f) return@forEach
        canvas.drawText("${log.date} - Mood: ${log.mood}, Flow: ${log.flow}, Pain: ${log.pain}", 50f, yPos, paint)
        yPos += 20f
        if (log.symptoms.isNotEmpty()) {
            canvas.drawText("  Symptoms: ${log.symptoms}", 50f, yPos, paint)
            yPos += 20f
        }
        yPos += 10f
    }

    pdfDocument.finishPage(page)
    val fileName = "Period_Report_${System.currentTimeMillis()}.pdf"
    
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
                Toast.makeText(context, "PDF saved to Downloads/SmartHelp", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}

@Composable
fun CycleStatusRing(days: Int, isOverdue: Boolean, hasData: Boolean, isBn: Boolean) {
    val isDark = isSystemInDarkTheme()
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        Surface(
            modifier = Modifier.size(220.dp), 
            shape = CircleShape, 
            color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.4f),
            border = BorderStroke(8.dp, if (!hasData) (if (isDark) Color.White.copy(alpha = 0.2f) else Color.Gray.copy(0.1f)) else if (isOverdue) Color.Red.copy(0.2f) else PeriodPrimary.copy(0.2f))
        ) {}
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (!hasData) {
                Text(if (isBn) "তারিখ সেভ করুন" else "SET DATE", fontSize = 22.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.6f))
            } else {
                Text(
                    text = if (isOverdue) stringResource(id = R.string.period_overdue_by) else stringResource(id = R.string.period_next_in),
                    fontSize = 11.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(text = "$days", fontSize = 64.sp, fontWeight = FontWeight.Black, color = if (isOverdue) Color.Red else PeriodPrimary)
                Text(
                    text = stringResource(id = R.string.period_days),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOverdue) Color.Red else PeriodPrimary,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
fun PredictionCard(title: String, start: String, end: String, color: Color, icon: ImageVector) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), 
        shape = RoundedCornerShape(24.dp), 
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(44.dp), shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.15f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(22.dp)) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 13.sp, color = color, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(start, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.padding(horizontal = 8.dp).size(14.dp), tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.LightGray)
                    Text(end, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
fun InputSettingsCard(cycle: Int, duration: Int, isBn: Boolean, onCycleChange: (Float) -> Unit, onDurationChange: (Float) -> Unit) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(stringResource(id = R.string.period_avg_cycle, cycle), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface)
            Slider(value = cycle.toFloat(), onValueChange = onCycleChange, valueRange = 21f..45f, colors = SliderDefaults.colors(thumbColor = PeriodPrimary, activeTrackColor = PeriodPrimary))
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(id = R.string.period_length, duration), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface)
            Slider(value = duration.toFloat(), onValueChange = onDurationChange, valueRange = 3f..10f, colors = SliderDefaults.colors(thumbColor = PeriodPrimary, activeTrackColor = PeriodPrimary))
        }
    }
}
