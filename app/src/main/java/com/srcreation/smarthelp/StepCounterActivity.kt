package com.srcreation.smarthelp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.srcreation.smarthelp.db.StepEntity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.text.SimpleDateFormat
import java.util.*

class StepCounterActivity : AppCompatActivity(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var _steps = mutableIntStateOf(0)
    private var initialBaseline = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        val profileId = intent.getIntExtra("PROFILE_ID", 1)
        val prefs = PreferenceManager(this)
        val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val savedDate = prefs.getStepBaselineDate(profileId)
        if (savedDate != today) {
            prefs.setStepBaselineDate(profileId, today)
            prefs.setStepBaseline(profileId, -1)
        }
        initialBaseline = prefs.getStepBaseline(profileId)

        setContent {
            HelpsTheme {
                StepCounterScreen(sensorSteps = _steps.intValue, onBack = { finish() })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stepSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val raw = event.values[0].toInt()
            val prefs = PreferenceManager(this)
            val profileId = intent.getIntExtra("PROFILE_ID", 1)
            if (initialBaseline == -1) {
                initialBaseline = raw
                prefs.setStepBaseline(profileId, initialBaseline)
            }
            _steps.intValue = (raw - initialBaseline).coerceAtLeast(0)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepCounterScreen(sensorSteps: Int, viewModel: StepViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val prefs = remember { PreferenceManager(context) }
    var stepGoal by remember { mutableIntStateOf(prefs.getStepGoal()) }
    var manualSteps by remember { mutableIntStateOf(0) }
    var showGoalDialog by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    val steps = sensorSteps + manualSteps

    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }

    var hasPermission by remember {
        mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED else true)
    }
    var showDisclosure by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    val records by viewModel.allRecords.collectAsState(initial = emptyList())
    
    // Save current steps daily & sync with Caregiver
    LaunchedEffect(steps) {
        if (steps > 0) {
            val date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            viewModel.addStepRecord(steps, date)

            // Caregiver Sync
            CaregiverSyncRepository.syncVitals(
                context = context,
                type = "STEP",
                title = "দৈনিক হাঁটা (Step Tracker)",
                valueText = "$steps / $stepGoal steps",
                statusTag = if (steps >= stepGoal) "Goal Reached! 🎉" else "In Progress"
            )
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasPermission = isGranted
    }

    // Goal Setting Dialog
    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text(if (isBn) "দৈনিক হাঁটার লক্ষ্য পরিবর্তন" else "Change Daily Step Goal", fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (isBn) "আপনার পছন্দমতো দৈনিক স্টেপস গোল সিলেক্ট করুন:" else "Select your target daily step goal:", fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    listOf(4000, 5000, 6000, 8000, 10000, 12000).forEach { goalOpt ->
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                stepGoal = goalOpt
                                prefs.setStepGoal(goalOpt)
                                showGoalDialog = false
                                Toast.makeText(context, if (isBn) "লক্ষ্যমাত্রা $goalOpt স্টেপস সেট করা হয়েছে!" else "Goal set to $goalOpt steps!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                        ) {
                            Text(if (isBn) "$goalOpt স্টেপস / দিন" else "$goalOpt Steps / Day", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text(if (isBn) "বাতিল" else "Cancel", color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray) }
            },
            containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Prominent Disclosure Dialog for Play Store Compliance
    if (showDisclosure) {
        AlertDialog(
            onDismissRequest = { showDisclosure = false },
            title = { Text(stringResource(R.string.step_disclosure_title), color = if (isDark) Color.White else Color.Black) },
            text = { Text(stringResource(R.string.step_disclosure_body), color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.DarkGray) },
            confirmButton = {
                Button(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDisclosure = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))) { Text(stringResource(R.string.step_disclosure_agree), color = if (isDark) Color.Black else Color.White) }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDisclosure = false
                }) { Text(stringResource(R.string.step_disclosure_dismiss), color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray) }
            },
            containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    LaunchedEffect(Unit) {
        if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            showDisclosure = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.step_counter_title), fontSize = 18.sp, color = if (isDark) Color.White else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = if (isDark) Color.White else MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showResetConfirmDialog = true
                    }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Reset Steps", tint = Color(0xFFEF4444))
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
            if (showResetConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showResetConfirmDialog = false },
                    title = { Text(if (isBn) "স্টেপস হিস্ট্রি ডিলিট করুন" else "Reset Step Records", fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color.Black) },
                    text = { Text(if (isBn) "আপনি কি নিশ্চিত যে আপনার সেভ করা সমস্ত হাঁটার হিস্ট্রি ডিলিট/রিসেট করতে চান?" else "Are you sure you want to clear/reset all recorded step history?", color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.DarkGray) },
                    confirmButton = {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.clearAllStepRecords {
                                    val toastMsg = if (isBn) "সমস্ত স্টেপস হিস্ট্রি ডিলিট করা হয়েছে!" else "All step records cleared!"
                                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                }
                                showResetConfirmDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text(if (isBn) "ডিলিট করুন" else "Clear History", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetConfirmDialog = false }) {
                            Text(stringResource(id = R.string.cancel), color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                        }
                    },
                    containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
                    shape = RoundedCornerShape(24.dp)
                )
            }
            if (!hasPermission) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Button(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDisclosure = true
                    }, colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))) { Text(stringResource(R.string.step_authorize_btn), color = if (isDark) Color.Black else Color.White) }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Step Progress Circle with Goal Edit capability
                    StepProgressCircle(steps, stepGoal, isBn, isDark, onGoalClick = { showGoalDialog = true })
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulate Walking Button (Great for Testing / Emulators)
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            manualSteps += 100
                            Toast.makeText(context, if (isBn) "+১০০ স্টেপ যোগ করা হয়েছে!" else "+100 Steps added!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)),
                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color(0xFF1A237E).copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "🚶‍♂️ হাঁটা সিমুলেট করুন (+১০০ স্টেপസ്)" else "🚶‍♂️ Simulate Walking (+100 Steps)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatCard(
                            label = stringResource(R.string.step_distance),
                            value = "%.2f".format(steps * 0.0008),
                            unit = stringResource(R.string.step_distance_unit),
                            icon = Icons.Default.Route,
                            color = Color(0xFF2196F3),
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = stringResource(R.string.step_calories),
                            value = "${(steps * 0.04).toInt()}",
                            unit = stringResource(R.string.step_calories_unit),
                            icon = Icons.Default.Whatshot,
                            color = Color(0xFFFF9800),
                            isDark = isDark,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Active Walking Time & Water Hydration Bonus Card
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f)),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color(0xFF0284C7).copy(alpha = 0.15f)) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp)) }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(if (isBn) "সক্রিয় হাঁটার সময় (Active Walk Time)" else "Active Walking Time", fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(
                                        if (isBn) "${steps / 100} মিনিট (~${(steps / 100.0 * 75).toInt()} মিটার)" else "${steps / 100} mins (~${(steps / 100.0 * 75).toInt()} meters)",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = if (isDark) Color.White else Color(0xFF1A237E)
                                    )
                                }
                            }

                            HorizontalDivider(color = if (isDark) Color(0xFF1E293B) else Color.LightGray.copy(alpha = 0.4f))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color(0xFF22C55E).copy(alpha = 0.15f)) {
                                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(20.dp)) }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(if (isBn) "অতিরিক্ত পানি পানের পরামর্শ (Hydration Bonus)" else "Hydration Bonus Recommendation", fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
                                    Text(
                                        if (isBn) "+${(steps / 3000 * 250).coerceAtLeast(0)} ml এক্সট্রা পানি পান করুন" else "+${(steps / 3000 * 250).coerceAtLeast(0)} ml extra water recommended",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color(0xFF22C55E)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.6f)), border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)) {
                        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color(0xFF4CAF50).copy(alpha = 0.15f)) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.DirectionsRun, contentDescription = null, tint = Color(0xFF4CAF50)) }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(stringResource(R.string.step_tracking_status), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                                Text(stringResource(R.string.step_live_active), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF4CAF50))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = stringResource(R.string.step_weekly_activity), fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Start).padding(start = 28.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    WeeklyStepBar(records, stepGoal, isBn, isDark)

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showResetConfirmDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.2.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "স্টেপস হিস্ট্রি ডিলিট/রিসেট করুন" else "Clear Step Records History",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun StepProgressCircle(steps: Int, goal: Int, isBn: Boolean, isDark: Boolean, onGoalClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val progress = (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        Canvas(modifier = Modifier.size(200.dp)) {
            drawCircle(color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.3f), style = Stroke(width = 16.dp.toPx()))
            drawArc(color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), startAngle = -90f, sweepAngle = 360 * animatedProgress, useCenter = false, style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onGoalClick()
            }
        ) {
            Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(32.dp))
            Text(text = "$steps", fontSize = 42.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E))
            Text(text = stringResource(R.string.step_goal_format, goal), fontSize = 14.sp, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7), fontWeight = FontWeight.Bold)
            Text(text = if (isBn) "🎯 লক্ষ্য পরিবর্তন (Tap)" else "🎯 Tap to Change Goal", fontSize = 10.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, unit: String, icon: ImageVector, color: Color, isDark: Boolean, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.6f)), border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E))
                Text(" $unit", fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
            }
            Text(label, fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
        }
    }
}

@Composable
fun WeeklyStepBar(records: List<StepEntity>, goal: Int, isBn: Boolean, isDark: Boolean) {
    val recentRecords = records.takeLast(7)

    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.6f)), border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (recentRecords.isEmpty()) {
                Text(
                    text = if (isBn) "কোনো হিস্ট্রি রেকর্ড নেই" else "No recorded history yet",
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                )
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    recentRecords.forEach { record ->
                        val ratio = (record.steps.toFloat() / goal.toFloat()).coerceIn(0.1f, 1f)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${record.steps}", fontSize = 9.sp, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.width(14.dp).height(80.dp * ratio).background(color = if (record.steps >= goal) Color(0xFF4CAF50) else (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)).copy(alpha = 0.6f), shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = record.date.take(3), fontSize = 10.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
