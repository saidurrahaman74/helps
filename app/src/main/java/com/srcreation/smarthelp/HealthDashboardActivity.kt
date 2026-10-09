package com.srcreation.smarthelp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.util.Locale

class HealthDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                HealthDashboardScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDashboardScreen(
    onBack: () -> Unit,
    bpViewModel: BpViewModel = viewModel(),
    insulinViewModel: InsulinViewModel = viewModel(),
    stepViewModel: StepViewModel = viewModel()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    // Collect active user database records
    val bpRecords by bpViewModel.allRecords.collectAsState(initial = emptyList())
    val insulinRecords by insulinViewModel.allRecords.collectAsState(initial = emptyList())
    val stepRecords by stepViewModel.allRecords.collectAsState(initial = emptyList())
    
    // Resolve dynamic values or default if empty
    val latestBp = remember(bpRecords) {
        bpRecords.firstOrNull()?.let { "${it.systolic}/${it.diastolic}" } ?: "--/--"
    }
    val bpStatus = remember(bpRecords) {
        bpRecords.firstOrNull()?.let {
            if (it.systolic < 120 && it.diastolic < 80) "Normal"
            else if (it.systolic <= 139 || it.diastolic <= 89) "Prehypertension"
            else "High"
        } ?: "No Data"
    }
    val bpColor = remember(bpStatus) {
        when(bpStatus) {
            "Normal" -> Color(0xFF22C55E)
            "Prehypertension" -> Color(0xFFFF9800)
            else -> Color(0xFFEF4444)
        }
    }
    
    val latestSugar = remember(insulinRecords) {
        insulinRecords.firstOrNull()?.glucose ?: "--"
    }
    val sugarUnit = remember(insulinRecords) {
        insulinRecords.firstOrNull()?.unit ?: "mg/dL"
    }
    val sugarStatus = remember(insulinRecords) {
        insulinRecords.firstOrNull()?.mealTime ?: "No Data"
    }
    
    val latestSteps = remember(stepRecords) {
        stepRecords.firstOrNull()?.let { String.format(Locale.US, "%,d", it.steps) } ?: "0"
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                CenterAlignedTopAppBar(
                    title = { Text("Health Dashboard", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A237E)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFF1A237E))
                        }
                    },
                    actions = {
                        val scope = rememberCoroutineScope()
                        val reportManager = remember { ReportManager(context) }
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch { reportManager.generateMasterReport() }
                        }) {
                            Icon(Icons.Default.PictureAsPdf, "Master Report", tint = Color(0xFF1A237E))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        bottomBar = { AppBottomBar(context = context, currentTab = -1, backgroundBrush = AppGradientBrush) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Dynamic Summary Section from User Database
                HealthMetricSummary(
                    title = "Blood Pressure",
                    value = latestBp,
                    unit = "mmHg",
                    status = bpStatus,
                    color = bpColor,
                    icon = Icons.Default.MonitorHeart
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                HealthMetricSummary(
                    title = "Blood Sugar",
                    value = latestSugar,
                    unit = sugarUnit,
                    status = sugarStatus,
                    color = Color(0xFF0284C7),
                    icon = Icons.Default.WaterDrop
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                HealthMetricSummary(
                    title = "Daily Steps",
                    value = latestSteps,
                    unit = "steps",
                    status = if(latestSteps != "0") "Active Today" else "No Data",
                    color = Color(0xFFFF9800),
                    icon = Icons.Default.Speed
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Weekly Activity Trend",
                    modifier = Modifier.align(Alignment.Start),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    fontSize = 18.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Modern Spline Chart / Wave Graph
                Card(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Activity Levels", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                            Text("This Week", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            val width = size.width
                            val height = size.height
                            
                            // Points to draw a smooth curved wave graph
                            val points = listOf(
                                Offset(0f, height * 0.7f),
                                Offset(width * 0.15f, height * 0.4f),
                                Offset(width * 0.35f, height * 0.6f),
                                Offset(width * 0.55f, height * 0.25f),
                                Offset(width * 0.75f, height * 0.5f),
                                Offset(width * 0.9f, height * 0.15f),
                                Offset(width, height * 0.3f)
                            )
                            
                            val path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val p0 = points[i - 1]
                                    val p1 = points[i]
                                    // Smooth bezier curve calculations
                                    cubicTo(
                                        (p0.x + p1.x) / 2, p0.y,
                                        (p0.x + p1.x) / 2, p1.y,
                                        p1.x, p1.y
                                    )
                                }
                            }
                            
                            // Draw the gradient line
                            drawPath(
                                path = path,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF0284C7), Color(0xFF6366F1), Color(0xFFFF9800))
                                ),
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )
                            
                            // Draw indicator glowing circles over main vertices
                            points.forEachIndexed { index, offset ->
                                if (index == 3 || index == 5) {
                                    drawCircle(
                                        color = if (index == 3) Color(0xFF6366F1) else Color(0xFFFF9800),
                                        radius = 6.dp.toPx(),
                                        center = offset
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 3.dp.toPx(),
                                        center = offset
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Days Labels X-Axis
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                                Text(day, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun HealthMetricSummary(title: String, value: String, unit: String, status: String, color: Color, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(color.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(title, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        Text(" $unit", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 3.dp), fontWeight = FontWeight.Medium)
                    }
                }
            }
            
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    status,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}
