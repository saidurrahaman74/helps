package com.srcreation.smarthelp

import android.app.Activity
import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale
import java.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.MedicineEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.absoluteValue

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AuthRepository().updateTimeZoneOnAppStart()

        val pref = PreferenceManager(this)
        pref.setActiveProfile(1) // Ensure Patient mode is active

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        ActivityGateHelper.checkGateAndRender(this) {
            setContent {
                HelpsTheme {
                    HomeScreen()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(wellnessViewModel: WellnessViewModel = viewModel()) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        wellnessViewModel.fetchCurrentProfile()
    }

    val authRepo = remember { AuthRepository() }
    val patientUid = authRepo.uid() ?: ""

    LaunchedEffect(patientUid) {
        if (patientUid.isNotBlank()) {
            SyncWorker.enqueue(context)
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val isDark = isSystemInDarkTheme()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = if (isDark) Color(0xFF0F172A) else Color.White,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Surface(modifier = Modifier.size(64.dp), shape = CircleShape, color = Color(0xFFEF4444).copy(alpha = 0.1f)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(14.dp)) {
                            PatientIconDrawing(Color(0xFFEF4444))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = stringResource(id = R.string.patient_pro_toolkit), fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E))
                    Text(text = stringResource(id = R.string.health_assistant), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))

                val isBn = remember { Locale.getDefault().language == "bn" }
                val authRepo = remember { AuthRepository() }
                val patientUid = authRepo.uid() ?: ""
                val pendingCountState = remember { mutableStateOf(0) }
                
                DisposableEffect(patientUid) {
                    if (patientUid.isBlank()) {
                        return@DisposableEffect object : androidx.compose.runtime.DisposableEffectResult {
                            override fun dispose() {}
                        }
                    }
                    val reg = FirebaseFirestore.getInstance().collection("links")
                        .whereEqualTo("patientId", patientUid)
                        .whereEqualTo("status", "pending")
                        .addSnapshotListener { snap: com.google.firebase.firestore.QuerySnapshot?, _ ->
                            pendingCountState.value = snap?.size() ?: 0
                        }
                    onDispose { reg.remove() }
                }

                val menuItems = listOf(
                    Triple(stringResource(id = R.string.nav_medical_id), Icons.Default.Person, Intent(context, MedicalIdActivity::class.java)),
                    Triple(stringResource(id = R.string.nav_emergency_qr), Icons.Default.QrCode2, Intent(context, MedicalQrActivity::class.java)),
                    Triple(stringResource(id = R.string.nav_patient_vault), Icons.Default.Inventory, Intent(context, MedicalVaultActivity::class.java).apply { putExtra("PROFILE_ID", 1) }),
                    Triple(stringResource(id = R.string.nav_master_report), Icons.AutoMirrored.Filled.Assignment, Intent(context, UnifiedReportActivity::class.java))
                )

                menuItems.forEach { (label, icon, intent) ->
                    NavigationDrawerItem(
                        label = { Text(text = label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isDark) Color.White else Color.Black) },
                        selected = false,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch { drawerState.close() }
                            AppNavigator.launchIntent(context, intent)
                        },
                        icon = { Icon(imageVector = icon, contentDescription = label, tint = if (isDark) Color.White else Color.Black) },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                QuickModeSwitcher(
                    currentMode = "patient",
                    onModeClick = { mode ->
                        scope.launch { drawerState.close() }
                        val intent = when(mode) {
                            "nurse" -> Intent(context, NurseHubActivity::class.java)
                            "women" -> Intent(context, WomenCareActivity::class.java)
                            else -> null
                        }
                        intent?.let { context.startActivity(it.apply { flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT }) }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    val isDark = isSystemInDarkTheme()
                    CenterAlignedTopAppBar(
                        title = { Text(text = stringResource(id = R.string.patient_help_title), fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
                        navigationIcon = {
                            IconButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); scope.launch { drawerState.open() } }) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = if (isDark) Color.White else Color(0xFF1A237E), modifier = Modifier.size(26.dp))
                            }
                        },
                        actions = {
                            val authRepo = remember { AuthRepository() }
                            val instructionsRepo = remember { InstructionsRepository() }
                            val patientUid = authRepo.uid() ?: ""
                            val unreadCount by instructionsRepo.getUnreadCountFlow(patientUid).collectAsState(initial = 0)

                            Box {
                                IconButton(onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    context.startActivity(Intent(context, PatientInstructionsActivity::class.java))
                                }) {
                                    Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = "Caregiver Instructions", tint = if (isDark) Color.White else Color(0xFF1A237E), modifier = Modifier.size(24.dp))
                                }
                                if (unreadCount > 0) {
                                    Badge(
                                        modifier = Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp),
                                        containerColor = Color(0xFFEF4444)
                                    ) {
                                        Text(unreadCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            IconButton(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); context.startActivity(Intent(context, NotificationActivity::class.java)) }) {
                                Icon(imageVector = Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = if (isDark) Color.White else Color(0xFF1A237E), modifier = Modifier.size(26.dp))
                            }

                            LanguageToggleButton()
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    )
                },
                bottomBar = { AppBottomBar(context = context, currentTab = 1, isNurseMode = false, backgroundBrush = AppGradientBrush) }
            ) { innerPadding ->
                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    val smartInsights by wellnessViewModel.smartInsights.collectAsState(initial = null)
                    
                    if (smartInsights == null) {
                        HomeScreenSkeleton()
                    } else {
                        ScreenEntranceAnimation {
                            DashboardContent(smartInsights = smartInsights!!) { category ->
                                val intent = when (category.id) {
                                    "dash_01" -> Intent(context, HealthDashboardActivity::class.java)
                                    "med_01" -> Intent(context, MedicineReminderActivity::class.java).apply { putExtra("PROFILE_ID", 1) }

                                    "bp_01" -> Intent(context, BpTrackerActivity::class.java).apply { putExtra("PROFILE_ID", 1) }
                                    "pulse_01" -> Intent(context, PulseTrackerActivity::class.java).apply { putExtra("PROFILE_ID", 1) }
                                    "spo2_01" -> Intent(context, SpO2TrackerActivity::class.java).apply { putExtra("PROFILE_ID", 1) }
                                    "temp_01" -> Intent(context, TempTrackerActivity::class.java).apply { putExtra("PROFILE_ID", 1) }
                                    "ins_01" -> Intent(context, InsulinTrackerActivity::class.java).apply { putExtra("PROFILE_ID", 1) }
                                    "step_01" -> Intent(context, StepCounterActivity::class.java).apply { putExtra("PROFILE_ID", 1) }
                                    "water_01" -> Intent(context, FluidTrackerActivity::class.java).apply { putExtra("PROFILE_ID", 1) }
                                    "vault_01" -> Intent(context, MedicalVaultActivity::class.java).apply { putExtra("PROFILE_ID", 1) }
                                    "bmi_01" -> Intent(context, CategoryActivity::class.java).apply { putExtra("CATEGORY_TITLE", "BMI Calculator"); putExtra("CATEGORY_COLOR", "#A855F7"); putExtra("PROFILE_ID", 1) }
                                    "chart_01" -> Intent(context, CategoryActivity::class.java).apply { putExtra("CATEGORY_TITLE", "Weight Chart"); putExtra("CATEGORY_COLOR", "#EC4899"); putExtra("PROFILE_ID", 1) }
                                    "ai_report_01" -> Intent(context, AiReportCheckerActivity::class.java)
                                    "diet_01" -> Intent(context, SmartDietActivity::class.java)
                                    "emergency_triage_01" -> Intent(context, CategoryActivity::class.java).apply {
                                        putExtra("CATEGORY_TITLE", "First Aid")
                                        putExtra("CATEGORY_COLOR", "#EF4444")
                                        putExtra("PROFILE_ID", 1)
                                    }
                                    "pro_tips_01" -> Intent(context, ProTipsActivity::class.java)
                                    else -> Intent(context, CategoryActivity::class.java).apply { 
                                        putExtra("CATEGORY_ID", category.id)
                                        putExtra("CATEGORY_TITLE", category.title)
                                        putExtra("CATEGORY_COLOR", category.colorHex)
                                        putExtra("PROFILE_ID", 1)
                                    }
                                }
                                AppNavigator.launchIntent(context, intent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreenSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header Skeleton
        val isDark = isSystemInDarkTheme()
        Card(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.3f))
        ) {
            Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(70.dp).clip(CircleShape).shimmerLoadingAnimation())
                Spacer(Modifier.width(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonItem(width = 150.dp, height = 18.dp)
                    SkeletonItem(width = 100.dp, height = 12.dp)
                }
            }
        }

        // Search Bar Skeleton
        SkeletonItem(modifier = Modifier.fillMaxWidth(), height = 56.dp, cornerRadius = 24.dp)

        // Graph Skeleton
        SkeletonItem(modifier = Modifier.fillMaxWidth(), height = 200.dp, cornerRadius = 32.dp)

        // Cards Skeleton
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = false
        ) {
            items(6) {
                SkeletonItem(modifier = Modifier.fillMaxWidth(), height = 125.dp, cornerRadius = 32.dp)
            }
        }
    }
}

@Composable
fun SmartDashboardHeader(
    wellnessScore: Int, 
    nextMed: MedicineEntity?,
    onTakeNow: ((MedicineEntity) -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val statusColor = when {
        wellnessScore >= 80 -> Color(0xFF22C55E)
        wellnessScore >= 50 -> Color(0xFF0284C7)
        else -> Color(0xFFFF9800)
    }

    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(68.dp)) {
                    CircularProgressIndicator(
                        progress = { wellnessScore.toFloat() / 100f },
                        modifier = Modifier.fillMaxSize(),
                        color = statusColor,
                        strokeWidth = 6.dp,
                        trackColor = statusColor.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "$wellnessScore", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E))
                        Text("%", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = statusColor.copy(alpha = 0.15f), 
                            shape = RoundedCornerShape(6.dp), 
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = if (wellnessScore >= 80) stringResource(R.string.health_optimal) else if (wellnessScore >= 50) stringResource(R.string.health_good) else stringResource(R.string.health_needs_attention), 
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Black, 
                                color = statusColor, 
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (nextMed != null) stringResource(R.string.next_med_label, nextMed.name) else stringResource(R.string.smart_health_hub), 
                        fontSize = 15.sp, 
                        fontWeight = FontWeight.Black, 
                        color = if (isDark) Color.White else Color(0xFF1A237E), 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (nextMed != null) "${nextMed.dosageValue} ${nextMed.dosageUnit} • ${getLocalizedFoodRelation(nextMed.foodRelation)}" else stringResource(R.string.daily_tracking_active), 
                        fontSize = 11.sp, 
                        color = Color.Gray, 
                        maxLines = 1, 
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (nextMed != null && onTakeNow != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TigerIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.time_to_take_dosage),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF334155)
                        )
                    }
                    
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTakeNow(nextMed)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.btn_take_now),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardContent(smartInsights: List<SmartInsight>, viewModel: WellnessViewModel = viewModel(), onActionClick: (InfoCardData) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { PreferenceManager(context) }
    val activePairingCode = prefs.getPairingCode()
    val patientUid = remember(activePairingCode) { activePairingCode.ifEmpty { prefs.getPatientUid() } }
    val caregiverNudges by CaregiverSyncRepository.getNudgesFlow(patientUid).collectAsState(initial = emptyList())
    val wellnessScore by viewModel.wellnessScore.collectAsState(initial = 0)
    val wellnessTrend by viewModel.weeklyWellnessTrend.collectAsState(initial = listOf(50f, 50f, 50f, 50f, 50f, 50f, 50f))
    val nextMed by viewModel.nextMedicine.collectAsState(initial = null)
    var searchQuery by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenResults = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = spokenResults?.firstOrNull()
            if (!text.isNullOrBlank()) {
                searchQuery = text
                Toast.makeText(context, "🎤 \"$text\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "কথা বলুন... / Speak now...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "ভয়েস অনুসন্ধান পাওয়া যায়নি (Voice search unavailable)", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(patientUid) {
        if (patientUid.isNotEmpty()) {
            CaregiverSyncRepository.getNudgesFlow(patientUid).collect { nudges ->
                val latest = nudges.firstOrNull()
                val message = latest?.get("message") as? String ?: ""
                val sender = latest?.get("patientName") as? String ?: "Caregiver"
                if (message.isNotBlank()) {
                    Toast.makeText(context, "💬 কেয়ারগিভার বার্তা ($sender): $message", Toast.LENGTH_LONG).show()
                    NotificationHelper.showNotification(
                        context,
                        "🔔 কেয়ারগিভার বার্তা ($sender)",
                        message
                    )
                }
            }
        }
    }
    
    val cardMed = stringResource(id = R.string.card_medicine)
    val cardMedDesc = stringResource(id = R.string.card_medicine_desc)
    val cardBp = stringResource(id = R.string.card_bp)
    val cardBpDesc = stringResource(id = R.string.card_bp_desc)
    val cardTemp = stringResource(id = R.string.card_temp)
    val cardTempDesc = stringResource(id = R.string.card_temp_desc)
    val cardInsulin = stringResource(id = R.string.card_insulin)
    val cardInsulinDesc = stringResource(id = R.string.card_insulin_desc)
    val cardSteps = stringResource(id = R.string.card_steps)
    val cardStepsDesc = stringResource(id = R.string.card_steps_desc)

    val cardBmi = stringResource(id = R.string.card_bmi)
    val cardBmiDesc = stringResource(id = R.string.card_bmi_desc)
    val cardWeight = stringResource(id = R.string.card_weight)
    val cardWeightDesc = stringResource(id = R.string.card_weight_desc)
    val cardProTips = stringResource(id = R.string.card_pro_tips)
    val cardProTipsDesc = stringResource(id = R.string.card_pro_tips_desc)

    val isBn = Locale.getDefault().language == "bn"
    val trackers = remember(cardMed, cardMedDesc, cardBp, cardBpDesc, cardTemp, cardTempDesc, cardInsulin, cardInsulinDesc, cardSteps, cardStepsDesc, isBn) {
        listOf(
            InfoCardData("med_01", cardMed, cardMedDesc, "Medicine Reminder", "#7C4DFF"),
            InfoCardData("bp_01", cardBp, cardBpDesc, "MonitorHeart", "#EF4444"),
            InfoCardData("pulse_01", if (isBn) "পাল্স ট্র্যাকার" else "Pulse Tracker", if (isBn) "হার্ট রেট ও বিপিএম মনিটর" else "Monitor heart rate & BPM", "Favorite", "#EF4444"),
            InfoCardData("spo2_01", if (isBn) "অক্সিজেন স্যাচুরেশন" else "SpO2 Tracker", if (isBn) "ব্লাড অক্সিজেন ও রেস্পিরেটরি রেট" else "Blood oxygen & saturation", "Air", "#22C55E"),
            InfoCardData("temp_01", cardTemp, cardTempDesc, "Thermostat", "#F59E0B"),
            InfoCardData("ins_01", cardInsulin, cardInsulinDesc, "Vaccines", "#0284C7"),
            InfoCardData("step_01", cardSteps, cardStepsDesc, "DirectionsWalk", "#22C55E")
        )
    }

    val services = remember(cardBmi, cardBmiDesc, cardWeight, cardWeightDesc, cardProTips, cardProTipsDesc, isBn) {
        listOf(
            InfoCardData("diet_01", if (isBn) "স্মার্ট ডায়েট ও ক্যালরি" else "Smart Diet & Calories", if (isBn) "ক্যালরি ও নিউট্রিশন ডায়েরি" else "Calories & nutrition diary", "LocalDining", "#22C55E"),
            InfoCardData("emergency_triage_01", if (isBn) "গোল্ডেন আওয়ার ইমার্জেন্সি গাইড" else "Golden Hour Emergency Guide", if (isBn) "সিপিআর, হার্ট অ্যাটাক ও রক্তক্ষরণ ফার্স্ট এইড" else "CPR, bleeding & emergency triage checklist", "MedicalServices", "#EF4444"),
            InfoCardData("bmi_01", cardBmi, cardBmiDesc, "BMI", "#A855F7"),
            InfoCardData("chart_01", cardWeight, cardWeightDesc, "BarChart", "#EC4899"),
            InfoCardData("pro_tips_01", cardProTips, cardProTipsDesc, "Lightbulb", "#F59E0B")
        )
    }

    val filteredTrackers = trackers.filter { it.title.contains(searchQuery, true) || it.desc.contains(searchQuery, true) }
    val filteredServices = services.filter { it.title.contains(searchQuery, true) || it.desc.contains(searchQuery, true) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            Column {
                GlassmorphicCard(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = TigerIndigo)
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = searchQuery, onValueChange = { searchQuery = it },
                            placeholder = { Text(stringResource(R.string.search_tools_hint), color = Color.Gray, fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                            maxLines = 1
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            launchVoiceSearch()
                        }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = TigerIndigo)
                        }
                    }
                }

                GlanceableDailySummaryCard()

                SmartDashboardHeader(
                    wellnessScore = wellnessScore, 
                    nextMed = nextMed,
                    onTakeNow = { med ->
                        viewModel.markMedicineAsTaken(med)
                        Toast.makeText(context, "Marked as taken: ${med.name} 💊", Toast.LENGTH_SHORT).show()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (searchQuery.isBlank()) {
                    AdvancedTrendChart(values = wellnessTrend, label = if (isBn) "ওয়েলনেস ট্রেন্ড" else "Wellness Trend", modifier = Modifier.fillMaxWidth().height(200.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Column {
                        val currentMed = nextMed
                        val greetingText = stringResource(id = getGreetingText())
                        val assistantMsg = if (currentMed != null) stringResource(R.string.home_med_time_msg, currentMed.name) else stringResource(R.string.home_routine_good_msg)
                        Text(text = greetingText, fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1A237E))
                        Text(text = assistantMsg, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Vitals 1-Tap Action Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickVitalsPill("🩺 BP Log", Color(0xFFEF4444)) {
                            context.startActivity(Intent(context, BpTrackerActivity::class.java).apply { putExtra("PROFILE_ID", 1) })
                        }
                        QuickVitalsPill("🩸 Sugar Log", Color(0xFF0284C7)) {
                            context.startActivity(Intent(context, InsulinTrackerActivity::class.java).apply { putExtra("PROFILE_ID", 1) })
                        }
                        QuickVitalsPill("💧 Water", Color(0xFF0EA5E9)) {
                            context.startActivity(Intent(context, FluidTrackerActivity::class.java))
                        }
                        QuickVitalsPill("👟 Steps", Color(0xFF22C55E)) {
                            context.startActivity(Intent(context, StepCounterActivity::class.java).apply { putExtra("PROFILE_ID", 1) })
                        }
                        QuickVitalsPill("🌡️ Temp", Color(0xFFF59E0B)) {
                            context.startActivity(Intent(context, TempTrackerActivity::class.java).apply { putExtra("PROFILE_ID", 1) })
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        if (caregiverNudges.isNotEmpty()) {
            val latestNudge = caregiverNudges.firstOrNull() ?: caregiverNudges.last()
            val message = latestNudge["message"] as? String ?: ""
            val senderName = latestNudge["patientName"] as? String ?: "Caregiver"
            if (message.isNotBlank()) {
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF7C4DFF).copy(alpha = if (isSystemInDarkTheme()) 0.25f else 0.15f)),
                        border = BorderStroke(1.2.dp, Color(0xFF7C4DFF)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (isBn) "💬 কেয়ারগিভার বার্তা ($senderName)" else "💬 Caregiver Message ($senderName)", fontWeight = FontWeight.Black, color = Color(0xFF7C4DFF), fontSize = 14.sp)
                                Text(message, fontSize = 13.sp, color = if (isSystemInDarkTheme()) Color.White else Color.Black)
                            }
                        }
                    }
                }
            }
        }

        if (filteredTrackers.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) { CategoryHeader(stringResource(id = R.string.daily_trackers), Color(0xFFEF4444)) }
            items(filteredTrackers) { action -> InfoCard(data = action) { onActionClick(action) } }
        }

        if (filteredServices.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) { CategoryHeader(stringResource(id = R.string.quick_services), Color(0xFF22C55E)) }
            items(filteredServices) { action -> InfoCard(data = action) { onActionClick(action) } }
        }
    }
}

@Composable
fun SmartInsightsSection(insights: List<SmartInsight>) {
    Column {
        Text(text = stringResource(id = R.string.smart_insights), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = if (isSystemInDarkTheme()) Color.White else Color(0xFF1A237E))
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { insights.forEach { SmartInsightCard(it) } }
    }
}

@Composable
fun SmartInsightCard(insight: SmartInsight) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = insight.color.copy(alpha = if(isDark) 0.15f else 0.08f)),
        border = BorderStroke(1.dp, insight.color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = insight.color.copy(alpha = 0.2f)) {
                Box(contentAlignment = Alignment.Center) { Icon(imageVector = insight.icon, contentDescription = null, tint = insight.color, modifier = Modifier.size(20.dp)) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = insight.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color.White else Color.Black)
                Text(text = insight.message, fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.DarkGray, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun QuickVitalsPill(title: String, color: Color, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.2.dp, color.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun GlanceableDailySummaryCard() {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val isBn = Locale.getDefault().language == "bn"

    val db = remember { com.srcreation.smarthelp.db.AppDatabase.getDatabase(context) }
    var summaryResult by remember { mutableStateOf<DailySummaryResult>(DailySummaryResult.NoReadings("No readings today")) }

    LaunchedEffect(Unit) {
        val isBlocked = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            LocalDataOwnership.isOwnershipBlocked(context)
        }
        if (isBlocked) {
            summaryResult = DailySummaryResult.Hidden
            return@LaunchedEffect
        }

        val now = System.currentTimeMillis()
        val tzId = java.util.TimeZone.getDefault().id

        val bpList = db.bpDao().getFullBpList()
        val pulseList = db.pulseDao().getFullPulseList()
        val spo2List = db.spo2Dao().getFullSpO2List()
        val medicines = db.medicineDao().getFullMedicineList()

        val cal = Calendar.getInstance().apply { timeInMillis = now }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        if (hour in 0..4) cal.add(Calendar.DAY_OF_YEAR, -1)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val targetDate = dateFormat.format(cal.time)

        val todayDoses = db.medicineDoseDao().getDosesByDate(targetDate)

        val readings = VitalsReadings(
            bpSys = bpList.lastOrNull()?.systolic,
            bpDia = bpList.lastOrNull()?.diastolic,
            pulseBpm = pulseList.lastOrNull()?.pulseBpm,
            spo2Percentage = spo2List.lastOrNull()?.spo2Percentage
        )

        summaryResult = DailySummaryLogic.computeDailySummary(
            now = now,
            timeZoneId = tzId,
            medicines = medicines,
            todayDoses = todayDoses,
            readings = readings,
            isGateBlocked = false,
            isBn = isBn
        )
    }

    if (summaryResult == DailySummaryResult.Hidden) return

    val (bgColor, borderColor, textColor) = when (summaryResult) {
        is DailySummaryResult.Danger -> Triple(Color(0xFFFEF2F2), Color(0xFFEF4444), Color(0xFF991B1B))
        is DailySummaryResult.Warning -> Triple(Color(0xFFFEF3C7), Color(0xFFF59E0B), Color(0xFF92400E))
        is DailySummaryResult.WithinLimits -> Triple(Color(0xFFECFDF5), Color(0xFF10B981), Color(0xFF047857))
        is DailySummaryResult.NoReadings, DailySummaryResult.Hidden -> Triple(
            if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
            if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1),
            if (isDark) Color.White.copy(alpha = 0.8f) else Color(0xFF334155)
        )
    }

    val summaryText = when (val res = summaryResult) {
        is DailySummaryResult.Danger -> res.msg
        is DailySummaryResult.Warning -> res.msg
        is DailySummaryResult.WithinLimits -> res.msg
        is DailySummaryResult.NoReadings -> res.msg
        DailySummaryResult.Hidden -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (summaryResult) {
                    is DailySummaryResult.Danger -> Icons.Default.Warning
                    is DailySummaryResult.Warning -> Icons.Default.Info
                    is DailySummaryResult.WithinLimits -> Icons.Default.CheckCircle
                    else -> Icons.Default.HealthAndSafety
                },
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = summaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                lineHeight = 18.sp
            )
        }
    }
}
