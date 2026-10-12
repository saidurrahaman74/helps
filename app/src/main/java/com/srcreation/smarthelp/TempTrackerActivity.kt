package com.srcreation.smarthelp

import android.app.Application
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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.TempEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class TempTrackerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ActivityGateHelper.checkGateAndRender(this) {
            setContent {
                HelpsTheme {
                    TempTrackerScreen(onBack = { finish() })
                }
            }
        }
    }
}

class TempViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.tempDao()
    
    private val profileId = savedStateHandle.get<Int>("PROFILE_ID") ?: 1

    val allRecords: Flow<List<TempEntity>> = dao.getAllRecords(profileId)

    fun addRecord(temp: Double, unit: String, method: String, status: String) {
        viewModelScope.launch {
            val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            val time = SimpleDateFormat("HHmm", Locale.getDefault()).format(Date())
            dao.insertRecord(
                TempEntity(
                    profileId = profileId,
                    tempValue = temp,
                    unit = unit,
                    bodySite = method,
                    status = status,
                    date = date,
                    time = time
                )
            )

            val tempF = if (unit == "°C") (temp * 9.0/5.0) + 32.0 else temp
            val isHighFever = tempF >= 100.4
            CaregiverSyncRepository.syncVitals(
                context = getApplication(),
                type = "TEMP",
                title = "শারীরিক তাপমাত্রা (Temperature)",
                valueText = "$temp $unit ($method)",
                statusTag = if (isHighFever) "High Fever" else status
            )

            val syncRepo = SyncRepository(getApplication())
            syncRepo.syncAllUnsynced()
            syncRepo.triggerSync()
        }
    }
    
    fun deleteRecord(record: TempEntity) {
        viewModelScope.launch {
            DeletionSync.deleteTemp(getApplication(), record)
        }
    }

    fun clearAllRecords(onCleared: () -> Unit) {
        viewModelScope.launch {
            val list = dao.getFullTempList().filter { it.profileId == profileId }
            for (record in list) {
                DeletionSync.deleteTemp(getApplication(), record)
            }
            onCleared()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempTrackerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val viewModel: TempViewModel = viewModel()
    val records by viewModel.allRecords.collectAsState(initial = emptyList())
    val isDark = isSystemInDarkTheme()
    
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, shadowElevation = 0.dp) {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(id = R.string.card_temp), fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
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
                            exportTempReportToPdf(context, records)
                        }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF Report", tint = if (isDark) Color.White else Color(0xFF1A237E))
                        }
                        LanguageToggleButton()
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        bottomBar = { AppBottomBar(context = context, currentTab = 1, backgroundBrush = AppGradientBrush) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).clip(RoundedCornerShape(16.dp))
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text(
                            text = stringResource(id = R.string.temp_tab_add),
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                        )
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text(
                            text = stringResource(id = R.string.temp_tab_trends),
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                        )
                    }
                }

                ScreenEntranceAnimation {
                    if (selectedTab == 0) {
                        TempInputContent(viewModel, records.firstOrNull(), records.size, records)
                    } else {
                        TempTrendsContent(records, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun TempInputContent(viewModel: TempViewModel, lastRecord: TempEntity?, totalCount: Int, allRecords: List<TempEntity>) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    var tempInput by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf("°F") }
    var method by rememberSaveable { mutableStateOf("Oral") }
    var ageGroup by rememberSaveable { mutableStateOf("Adult") }

    val currentTemp = tempInput.toDoubleOrNull() ?: 98.6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Temperature Interpretation Card
        TempInterpretationCard(currentTemp, unit, ageGroup, isBn)

        // Input Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)),
            border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(id = R.string.temp_select_unit), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("°F", "°C").forEach { u ->
                            FilterChip(
                                selected = unit == u,
                                onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); unit = u },
                                label = { Text(u, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = tempInput,
                        onValueChange = { tempInput = it },
                        label = { Text(stringResource(id = R.string.temp_hint_value) + " ($unit)", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = if (isDark) Color.White else Color.Black),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDark) Color.White else Color.Black,
                            unfocusedTextColor = if (isDark) Color.White else Color.Black,
                            focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                            focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    VoiceInputIconButton(
                        promptText = "কথা বলুন (যেমন: ৯৯.৫ ডিগ্রি)...",
                        iconSize = 20.dp,
                        tintColor = Color(0xFFF59E0B)
                    ) { _, _, decimal ->
                        if (decimal != null && decimal > 0) {
                            tempInput = decimal.toString()
                        }
                    }
                }

                // Measurement Method
                Text(stringResource(id = R.string.temp_method_select), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val methods = listOf("Oral" to stringResource(R.string.temp_method_mouth), "Axillary" to stringResource(R.string.temp_method_armpit), "Ear" to stringResource(R.string.temp_method_ear), "Forehead" to stringResource(R.string.temp_method_forehead))
                    methods.forEach { (mKey, mLabel) ->
                        FilterChip(
                            selected = method == mKey,
                            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); method = mKey },
                            label = { Text(mLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent,
                                labelColor = if (isDark) Color.White else Color.Black,
                                selectedContainerColor = Color(0xFFF59E0B),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val t = tempInput.toDoubleOrNull()
                        if (t != null && t in 30.0..115.0) {
                            val (status, _, _) = getTempStatus(t, unit, ageGroup, isBn)
                            viewModel.addRecord(t, unit, method, status)
                            tempInput = ""
                            Toast.makeText(context, if (isBn) "টেম্পারেচার রেকর্ড সেভ হয়েছে! 🟢" else "Temperature record saved!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, if (isBn) "সঠিক তাপমাত্রা লিখুন" else "Enter valid temperature", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(id = R.string.temp_btn_save), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
        
        Spacer(Modifier.height(40.dp))
    }
}

fun getTempInC(temp: Double, unit: String): Double {
    return if (unit == "°C") temp else (temp - 32) * 5 / 9
}

@Composable
fun TempInterpretationCard(temp: Double, unit: String, age: String, isBn: Boolean) {
    val (status, color, actionId) = getTempStatus(temp, unit, age, isBn)
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    val statusText = when(status) {
        "Hypothermia" -> stringResource(id = R.string.temp_level_hypo)
        "Normal" -> stringResource(id = R.string.temp_level_normal)
        "Low-grade Fever" -> stringResource(id = R.string.temp_level_low_fever)
        "High Fever" -> stringResource(id = R.string.temp_level_high_fever)
        "Danger Hyperpyrexia", "বিপদজনক Hyperpyrexia" -> status
        else -> stringResource(id = R.string.temp_level_hyper)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) color.copy(alpha = 0.2f) else color.copy(alpha = 0.1f)),
        border = BorderStroke(2.dp, color.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(statusText.uppercase(), fontWeight = FontWeight.Black, color = color, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Text(text = stringResource(id = actionId), textAlign = TextAlign.Center, fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.Black)
        }
    }
}

fun getTempStatus(temp: Double, unit: String, age: String, isBn: Boolean): Triple<String, Color, Int> {
    val tempInC = getTempInC(temp, unit)
    val tempF = if (unit == "°C") (temp * 9.0/5.0) + 32.0 else temp
    
    val isNormal = when(age) {
        "Newborn" -> tempInC in 36.5..37.9
        "Infant" -> tempInC in 36.7..37.8
        "Child", "Adult" -> tempInC in 36.1..37.2
        "Senior" -> tempInC in 35.6..36.8
        else -> tempInC in 36.1..37.2
    }

    return when {
        tempF > 105.0 || tempInC > 40.5 -> Triple(if (isBn) "বিপদজনক Hyperpyrexia" else "Danger Hyperpyrexia", Color(0xFF7F1D1D), R.string.temp_action_hyper)
        tempInC < 35.0 -> Triple("Hypothermia", Color(0xFF0284C7), R.string.temp_action_hypo)
        isNormal -> Triple("Normal", Color(0xFF22C55E), R.string.temp_action_normal)
        tempInC <= 37.9 -> Triple("Low-grade Fever", Color(0xFFFACC15), R.string.temp_action_low_fever)
        tempInC <= 40.0 -> Triple("High Fever", Color(0xFFEF4444), R.string.temp_action_high_fever)
        else -> Triple("Hyperpyrexia", Color(0xFF7F1D1D), R.string.temp_action_hyper)
    }
}

@Composable
fun TempTrendsContent(records: List<TempEntity>, viewModel: TempViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isBn = remember { Locale.getDefault().language == "bn" }
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (records.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(id = R.string.temp_trends_title), fontSize = 15.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E))
                TextButton(onClick = { showDeleteConfirmDialog = true }) {
                    Text(if (isBn) "সব মুছুন" else "Clear All", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            TempGraph(records.take(7).reversed())
        }
        
        Text(stringResource(id = R.string.temp_history_title), fontSize = 15.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E))
        
        records.forEach { record ->
            TempRecordCard(record) {
                viewModel.deleteRecord(record)
            }
        }
        Spacer(Modifier.height(80.dp))
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(if (isBn) "সমস্ত তাপমাত্রা হিস্ট্রি ডিলিট করুন" else "Clear All Temperature History", fontWeight = FontWeight.Bold) },
            text = { Text(if (isBn) "আপনি কি নিশ্চিত যে আপনার সেভ করা সমস্ত টেম্পারেচার রেকর্ড মুছে ফেলতে চান?" else "Are you sure you want to clear all recorded temperature history?") },
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
                    Text(stringResource(id = R.string.cancel))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun TempGraph(records: List<TempEntity>) {
    val haptic = LocalHapticFeedback.current
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(records) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, tween(1200, easing = EaseInOutQuart))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(if (isDark) Color(0xFF0F172A).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.88f), RoundedCornerShape(24.dp))
            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Core Temp Trend (°F)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
                if (selectedIndex != -1 && selectedIndex < records.size) {
                    val r = records[selectedIndex]
                    Text(
                        text = "${r.tempValue} ${r.unit}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                if (records.size <= 1) return@detectTapGestures
                                val stepX = size.width / (records.size - 1).coerceAtLeast(1)
                                val index = (offset.x / stepX).toInt().coerceIn(0, records.size - 1)
                                if (selectedIndex != index) {
                                    selectedIndex = index
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        }
                ) {
                    if (records.isEmpty()) return@Canvas
                    val width = size.width
                    val height = size.height

                    val temps = records.map { if (it.unit == "°C") (it.tempValue * 9.0/5.0) + 32.0 else it.tempValue }
                    val maxVal = (temps.maxOrNull() ?: 105.0).coerceAtLeast(102.0)
                    val minVal = (temps.minOrNull() ?: 95.0).coerceAtMost(98.0)
                    val range = (maxVal - minVal).coerceAtLeast(1.0)
                    val stepX = width / (records.size - 1).coerceAtLeast(1)

                    val points = temps.mapIndexed { i, t ->
                        Offset((i * stepX).toFloat(), (height - (((t - minVal) / range) * height * 0.7) * animatedProgress.value - (height * 0.15)).toFloat())
                    }

                    val path = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            val p0 = points[i - 1]; val p1 = points[i]
                            cubicTo(p0.x + (p1.x - p0.x) / 2, p0.y, p0.x + (p1.x - p0.x) / 2, p1.y, p1.x, p1.y)
                        }
                    }

                    drawPath(path, brush = Brush.verticalGradient(listOf(Color(0xFFEF4444).copy(0.3f), Color.Transparent)), style = Fill)
                    drawPath(path, color = Color(0xFFEF4444), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))

                    points.forEachIndexed { i, p ->
                        val isSelected = i == selectedIndex
                        drawCircle(
                            if (isSelected) Color.White else Color(0xFFEF4444),
                            radius = if (isSelected) 8.dp.toPx() else 4.dp.toPx(),
                            center = p
                        )
                        if (isSelected) {
                            drawCircle(Color(0xFFEF4444), radius = 8.dp.toPx(), center = p, style = Stroke(3.dp.toPx()))
                            drawLine(
                                color = Color(0xFFEF4444).copy(alpha = 0.3f),
                                start = Offset(p.x, 0f),
                                end = Offset(p.x, height),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TempRecordCard(r: TempEntity, onDelete: () -> Unit = {}) {
    val haptic = LocalHapticFeedback.current
    val isBn = remember { Locale.getDefault().language == "bn" }
    val isDark = isSystemInDarkTheme()
    val (status, color, _) = getTempStatus(r.tempValue, r.unit, "Adult", isBn)
    
    val statusText = when(status) {
        "Hypothermia" -> stringResource(id = R.string.temp_level_hypo)
        "Normal" -> stringResource(id = R.string.temp_level_normal)
        "Low-grade Fever" -> stringResource(id = R.string.temp_level_low_fever)
        "High Fever" -> stringResource(id = R.string.temp_level_high_fever)
        "Danger Hyperpyrexia", "বিপদজনক Hyperpyrexia" -> status
        else -> status
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else color.copy(alpha = 0.25f))
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
                    color = color.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Thermostat, null, tint = color, modifier = Modifier.size(24.dp))
                    }
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "${r.tempValue} ${r.unit}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                        Surface(
                            color = color.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(text = "${r.bodySite} • ${r.date} at ${r.time}", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                }
            }

            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
            }) {
                Icon(Icons.Default.Delete, "Delete", tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
            }
        }
    }
}

fun exportTempReportToPdf(context: Context, records: List<TempEntity>) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    paint.textSize = 22f
    paint.isFakeBoldText = true
    canvas.drawText("Smart Help - Temperature Health Report", 50f, 50f, paint)

    paint.textSize = 12f
    paint.isFakeBoldText = false
    canvas.drawText("Generated on: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())}", 50f, 80f, paint)

    canvas.drawLine(50f, 105f, 545f, 105f, paint)
    paint.isFakeBoldText = true
    canvas.drawText("Temperature Records:", 50f, 135f, paint)
    paint.isFakeBoldText = false

    var yPos = 165f
    records.forEach { record ->
        if (yPos > 800f) return@forEach
        canvas.drawText("${record.date} (${record.time}) - ${record.tempValue} ${record.unit} [${record.bodySite}] - ${record.status}", 50f, yPos, paint)
        yPos += 22f
    }

    pdfDocument.finishPage(page)
    val fileName = "Temp_Report_${System.currentTimeMillis()}.pdf"

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
                Toast.makeText(context, "PDF saved to Downloads/SmartHealth", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    } finally {
        pdfDocument.close()
    }
}
