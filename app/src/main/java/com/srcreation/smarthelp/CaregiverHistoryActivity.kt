package com.srcreation.smarthelp

import android.os.Bundle
import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaregiverHistoryActivity : ComponentActivity() {
    private val viewModel: CaregiverHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val patientId = intent.getStringExtra("PATIENT_ID") ?: ""
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Patient"

        if (patientId.isNotBlank()) {
            viewModel.initPatient(patientId, patientName)
        }

        setContent {
            HelpsTheme {
                CaregiverHistoryScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverHistoryScreen(
    viewModel: CaregiverHistoryViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val isBn = remember { Locale.getDefault().language == "bn" }

    val patientName by viewModel.patientName.collectAsState()
    val selectedVital by viewModel.selectedVital.collectAsState()
    val historyRecords by viewModel.vitalsHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var activePresetDays by remember { mutableIntStateOf(7) }

    Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isBn) "স্বাস্থ্য হিস্ট্রি ও চার্ট" else "Health History & Chart",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = patientName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Vital Type Chips Selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (isBn) "টাইপ সিলেক্ট করুন:" else "Select Vital Type:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "BP" to (if (isBn) "ব্লাড প্রেসার" else "BP"),
                                "PULSE" to (if (isBn) "পাল্স" else "Pulse"),
                                "SPO2" to (if (isBn) "অক্সিজেন" else "SpO2"),
                                "GLUCOSE" to (if (isBn) "সুগার" else "Glucose"),
                                "TEMPERATURE" to (if (isBn) "তাপমাত্রা" else "Temp"),
                                "STEPS" to (if (isBn) "স্টেপস" else "Steps")
                            ).forEach { (typeKey, label) ->
                                FilterChip(
                                    selected = selectedVital == typeKey,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.setVitalType(typeKey)
                                    },
                                    label = { Text(label, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // Date Range Preset Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(7 to "7 Days", 30 to "30 Days", 90 to "3 Months").forEach { (days, label) ->
                            Button(
                                onClick = {
                                    activePresetDays = days
                                    viewModel.setPresetRange(days)
                                },
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (activePresetDays == days) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (activePresetDays == days) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // MPAndroidChart LineChart Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.85f)),
                        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = when (selectedVital) {
                                    "BP" -> if (isBn) "ব্লাড প্রেসার ট্রেন্ড (Systolic / Diastolic)" else "BP Trend (Systolic / Diastolic)"
                                    else -> "$selectedVital Trend"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Spacer(Modifier.height(16.dp))

                            if (isLoading) {
                                Box(modifier = Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            } else if (historyRecords.isEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isBn) "কোনো রেকর্ড পাওয়া যায়নি" else "No records found for selected period",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                MpAndroidLineChart(
                                    records = historyRecords,
                                    isBp = selectedVital == "BP",
                                    isDark = isDark,
                                    formatDateOnly = viewModel::formatDateOnly
                                )
                            }
                        }
                    }
                }

                // Section Title: History List
                item {
                    Text(
                        text = if (isBn) "📋 রেকর্ডের তালিকা (History List)" else "📋 History Records List",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else Color(0xFF1A237E)
                    )
                }

                if (historyRecords.isEmpty() && !isLoading) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                text = if (isBn) "তালিকা খালি" else "No records available",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Reversed list for recent-first order in list
                    items(historyRecords.reversed()) { record ->
                        VitalHistoryCard(record = record, formatTime = viewModel::formatDate)
                    }
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun MpAndroidLineChart(
    records: List<VitalHistoryRecord>,
    isBp: Boolean,
    isDark: Boolean,
    formatDateOnly: (Long) -> String
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val systolicColor = AndroidColor.parseColor("#EF4444") // Red
    val diastolicColor = AndroidColor.parseColor("#3B82F6") // Blue
    val textColor = if (isDark) AndroidColor.WHITE else AndroidColor.parseColor("#1A237E")

    AndroidView(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    this.textColor = textColor
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val idx = value.toInt()
                            return if (idx in records.indices) {
                                formatDateOnly(records[idx].timestamp)
                            } else ""
                        }
                    }
                }

                axisLeft.apply {
                    this.textColor = textColor
                    setDrawGridLines(true)
                    gridColor = if (isDark) AndroidColor.parseColor("#334155") else AndroidColor.parseColor("#E2E8F0")
                }

                axisRight.isEnabled = false
                legend.apply {
                    this.textColor = textColor
                }
            }
        },
        update = { chart ->
            if (records.isEmpty()) return@AndroidView

            val entries1 = mutableListOf<Entry>()
            val entries2 = mutableListOf<Entry>()

            records.forEachIndexed { idx, item ->
                entries1.add(Entry(idx.toFloat(), item.value.toFloat()))
                if (isBp) {
                    entries2.add(Entry(idx.toFloat(), item.value2.toFloat()))
                }
            }

            val dataSets = mutableListOf<LineDataSet>()

            if (isBp) {
                // Two lines for BP (Systolic & Diastolic)
                val sysDataSet = LineDataSet(entries1, "Systolic").apply {
                    color = systolicColor
                    setCircleColor(systolicColor)
                    lineWidth = 2.5f
                    circleRadius = 4f
                    valueTextColor = textColor
                    valueTextSize = 10f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }

                val diaDataSet = LineDataSet(entries2, "Diastolic").apply {
                    color = diastolicColor
                    setCircleColor(diastolicColor)
                    lineWidth = 2.5f
                    circleRadius = 4f
                    valueTextColor = textColor
                    valueTextSize = 10f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }

                dataSets.add(sysDataSet)
                dataSets.add(diaDataSet)
            } else {
                // Single line for other vitals
                val dataSet = LineDataSet(entries1, records.firstOrNull()?.type ?: "Value").apply {
                    color = primaryColor
                    setCircleColor(primaryColor)
                    lineWidth = 2.5f
                    circleRadius = 4f
                    valueTextColor = textColor
                    valueTextSize = 10f
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                }
                dataSets.add(dataSet)
            }

            chart.data = LineData(dataSets.castToList())
            chart.invalidate()
        }
    )
}

@Suppress("UNCHECKED_CAST")
private fun <T> List<T>.castToList(): List<com.github.mikephil.charting.interfaces.datasets.ILineDataSet> {
    return this as List<com.github.mikephil.charting.interfaces.datasets.ILineDataSet>
}

@Composable
fun VitalHistoryCard(record: VitalHistoryRecord, formatTime: (Long) -> String) {
    val isDark = isSystemInDarkTheme()
    val isBp = record.type.equals("BP", ignoreCase = true)
    val displayValue = if (isBp) "${record.value.toInt()}/${record.value2.toInt()} ${record.unit}" else "${record.value} ${record.unit}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(38.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (record.type) {
                            "BP" -> Icons.Default.Favorite
                            "PULSE" -> Icons.Default.FavoriteBorder
                            "SPO2" -> Icons.Default.Air
                            "GLUCOSE" -> Icons.Default.InvertColors
                            "TEMPERATURE" -> Icons.Default.Thermostat
                            else -> Icons.Default.DirectionsWalk
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(record.type, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E))
                    Text(formatTime(record.timestamp), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = displayValue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
