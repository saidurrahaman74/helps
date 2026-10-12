package com.srcreation.smarthelp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CaregiverDashboardActivity : ComponentActivity() {
    private val viewModel: CaregiverDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authRepo = AuthRepository()

        if (!authRepo.isUserLoggedIn()) {
            Log.e("Caregiver", "CaregiverDashboardActivity created without logged in user. Redirecting to LoginActivity.")
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }

        setContent {
            HelpsTheme {
                CaregiverDashboardScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onLogout = {
                        BackgroundCheckScheduler.cancelWorkers(this@CaregiverDashboardActivity)
                        authRepo.logout()
                        val intent = Intent(this@CaregiverDashboardActivity, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    },
                    onLinkPatient = {
                        startActivity(Intent(this@CaregiverDashboardActivity, LinkPatientActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverDashboardScreen(
    viewModel: CaregiverDashboardViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onLinkPatient: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }

    val linkedPatients by viewModel.linkedPatients.collectAsState()
    val selectedPatient by viewModel.selectedPatient.collectAsState()
    val vitals by viewModel.vitalsState.collectAsState()
    val medicines by viewModel.medicinesState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isEmpty by viewModel.isEmpty.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var dropdownExpanded by remember { mutableStateOf(false) }
    var hasAutoOpenedLink by remember { mutableStateOf(false) }

    LaunchedEffect(isEmpty, isLoading, errorMessage) {
        if (!isLoading && isEmpty && errorMessage == null && !hasAutoOpenedLink) {
            hasAutoOpenedLink = true
            onLinkPatient()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SupervisorAccount, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isBn) "কেয়ারগিভার ড্যাশবোর্ড" else "Caregiver Dashboard",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    actions = {
                        IconButton(onClick = onLinkPatient) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Link Patient", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val intent = Intent(context, SettingsActivity::class.java).apply {
                                putExtra("FROM_NURSE", true)
                            }
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.error)
                            Text(
                                text = if (isBn) "তথ্য লোড করতে ত্রুটি হয়েছে" else "Error Loading Caregiver Data",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = errorMessage ?: "An unknown error occurred",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.listenToLinkedPatients() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (isBn) "পুনরায় চেষ্টা করুন" else "Retry")
                            }
                        }
                    }
                }
            } else if (isEmpty || linkedPatients.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f))
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(Icons.Default.PersonSearch, null, modifier = Modifier.size(64.dp), tint = if (isDark) Color(0xFF38BDF8) else MaterialTheme.colorScheme.primary)
                            Text(
                                text = if (isBn) "কোনো রোগী সংযুক্ত নেই" else "No Patient Linked Yet",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1A237E)
                            )
                            Text(
                                text = if (isBn) "আপনার রোগীর সাথে সংযুক্ত হতে নিচের বাটনে ক্লিক করে পেয়ারিং কোডটি লিখুন।" else "Link a patient using their 6-digit pairing code to view real-time health data.",
                                fontSize = 14.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.75f) else Color.DarkGray,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onLinkPatient,
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (isBn) "রোগী যুক্ত করুন" else "Link Patient", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Patient Selector Spinner / Dropdown Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isBn) "সংযুক্ত রোগী:" else "Monitored Patient:",
                                            fontSize = 12.sp,
                                            color = if (isDark) Color.White.copy(alpha = 0.75f) else Color.DarkGray
                                        )
                                        Text(
                                            text = selectedPatient?.name ?: "Select Patient",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)
                                        )
                                    }

                                    if (linkedPatients.size > 1) {
                                        Box {
                                            IconButton(onClick = { dropdownExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, null, tint = MaterialTheme.colorScheme.primary)
                                            }
                                            DropdownMenu(
                                                expanded = dropdownExpanded,
                                                onDismissRequest = { dropdownExpanded = false }
                                            ) {
                                                linkedPatients.forEach { p ->
                                                    DropdownMenuItem(
                                                        text = { Text(p.name, fontWeight = FontWeight.Bold) },
                                                        onClick = {
                                                            viewModel.selectPatient(p)
                                                            dropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    IconButton(onClick = onLinkPatient) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Patient", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        selectedPatient?.let { p ->
                                            val intent = Intent(context, CaregiverHistoryActivity::class.java).apply {
                                                putExtra("PATIENT_ID", p.patientId)
                                                putExtra("PATIENT_NAME", p.name)
                                            }
                                            context.startActivity(intent)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (isBn) "স্বাস্থ্য হিস্ট্রি ও চার্ট দেখুন" else "View History & Charts", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        selectedPatient?.let { p ->
                                            val intent = Intent(context, CaregiverInstructionsActivity::class.java).apply {
                                                putExtra("PATIENT_ID", p.patientId)
                                                putExtra("PATIENT_NAME", p.name)
                                            }
                                            context.startActivity(intent)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (isBn) "রোগীকে নির্দেশনা পাঠান" else "Send Instruction to Patient", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Section Title: Real-time Vitals
                    if (selectedPatient?.status == "pending") {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B))
                            ) {
                                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Schedule, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(12.dp))
                                    Text(if (isBn) "অনুমোদনের অপেক্ষায়" else "Waiting for approval", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E))
                                    Spacer(Modifier.height(6.dp))
                                    Text(if (isBn) "রোগীর অনুমোদনের জন্য এই সংযোগ অনুরোধটি অপেক্ষমাণ রয়েছে।" else "This connection request is pending patient approval.", textAlign = TextAlign.Center, fontSize = 14.sp, color = if (isDark) Color.White.copy(alpha = 0.75f) else Color.DarkGray)
                                    Spacer(Modifier.height(20.dp))
                                    Button(
                                        onClick = {
                                            selectedPatient?.let { viewModel.cancelPendingRequest(it.linkId) }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(if (isBn) "অনুরোধ বাতিল করুন" else "Cancel Request", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        // 🚨 Top Danger Alert Banner
                        if (vitals.activeDangerAlerts.isNotEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                    border = BorderStroke(1.5.dp, Color(0xFFEF4444))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = if (isBn) "🚨 সতর্কবার্তা (Active Danger Alerts)" else "🚨 Active Danger Alerts",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 16.sp,
                                                color = Color(0xFF991B1B)
                                            )
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        vitals.activeDangerAlerts.forEach { alertMsg ->
                                            Text("• $alertMsg", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C), lineHeight = 18.sp)
                                        }
                                        Spacer(Modifier.height(10.dp))
                                        Text(
                                            text = "This app does not replace a doctor or emergency services.",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }

                        // AI Health Insights Summary Card
                        item {
                            AiHealthInsightsCard(vitals = vitals, isBn = isBn, isDark = isDark)
                        }

                        // Header: Real-time Vitals & Last Updated
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBn) "⚡ রিয়েল-টাইম ভাইটালস" else "⚡ Real-Time Vitals",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (vitals.lastUpdatedText.isNotBlank()) {
                                    Text(
                                        text = vitals.lastUpdatedText,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Vitals Cards Grid/List
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // 1. BP
                                RealTimeVitalCard(
                                    title = if (isBn) "ব্লাড প্রেসার (BP)" else "Blood Pressure (BP)",
                                    item = vitals.bp,
                                    icon = Icons.Default.Favorite,
                                    defaultColor = Color(0xFFEF4444)
                                )

                                // 2. Pulse
                                RealTimeVitalCard(
                                    title = if (isBn) "পাল্স / হার্ট রেট (Pulse)" else "Pulse / Heart Rate",
                                    item = vitals.pulse,
                                    icon = Icons.Default.FavoriteBorder,
                                    defaultColor = Color(0xFFEC4899)
                                )

                                // 3. SpO2
                                RealTimeVitalCard(
                                    title = if (isBn) "অক্সিজেন স্যাচুরেশন (SpO2)" else "Oxygen Saturation (SpO2)",
                                    item = vitals.spo2,
                                    icon = Icons.Default.Air,
                                    defaultColor = Color(0xFF10B981)
                                )

                                // 4. Insulin / Glucose
                                RealTimeVitalCard(
                                    title = if (isBn) "রক্তের গ্লুকোজ / ডায়াবেটিস" else "Blood Glucose",
                                    item = vitals.insulin,
                                    icon = Icons.Default.InvertColors,
                                    defaultColor = Color(0xFF0284C7)
                                )

                                // 5. Temperature
                                RealTimeVitalCard(
                                    title = if (isBn) "শরীরের তাপমাত্রা (Temperature)" else "Body Temperature",
                                    item = vitals.temp,
                                    icon = Icons.Default.Thermostat,
                                    defaultColor = Color(0xFFF59E0B)
                                )
                            }
                        }

                        // 🔔 Recent Alerts (Last 24 Hours) Section
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                Text(
                                    text = if (isBn) "🔔 সম্প্রতি পাওয়া অ্যালার্ট (গত ২৪ ঘণ্টা)" else "🔔 Recent Alerts (Last 24 Hours)",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(8.dp))
                                if (vitals.recentAlerts24h.isEmpty()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)),
                                        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                                    ) {
                                        Text(
                                            text = if (isBn) "গত ২৪ ঘণ্টায় কোনো ডেঞ্জার অ্যালার্ট পাওয়া যায়নি" else "No alerts in the last 24 hours",
                                            fontSize = 14.sp,
                                            color = if (isDark) Color.White.copy(alpha = 0.75f) else Color.DarkGray,
                                            modifier = Modifier.padding(16.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            vitals.recentAlerts24h.forEach { alertText ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Warning, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(alertText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) Color.White else Color(0xFF7F1D1D))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section Title: Medicines
                        item {
                            Text(
                                text = if (isBn) "💊 আজকের ওষুধের তালিকা (Today's Medicines)" else "💊 Today's Medicines",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (medicines.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)),
                                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = if (isBn) "আজকের কোনো ওষুধ রেকর্ড নেই" else "No medicine records found for today",
                                        fontSize = 14.sp,
                                        color = if (isDark) Color.White.copy(alpha = 0.75f) else Color.DarkGray,
                                        modifier = Modifier.padding(16.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            items(medicines) { med ->
                                CaregiverMedicineCard(med = med, formatTime = viewModel::formatTime)
                            }
                        }

                        item { Spacer(Modifier.height(40.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun RealTimeVitalCard(
    title: String,
    item: VitalItem?,
    icon: ImageVector,
    defaultColor: Color
) {
    val isDark = isSystemInDarkTheme()
    val value = item?.valueText ?: "N/A"
    val level = item?.alertLevel ?: VitalAlertLevel.NORMAL
    val timeAgo = item?.timeAgoText ?: "No recent data"
    val isImpossible = item?.isImpossible ?: false

    val cardBg = when {
        isImpossible -> if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.85f)
        level == VitalAlertLevel.DANGER -> if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.6f) else Color(0xFFFEE2E2)
        level == VitalAlertLevel.WARNING -> if (isDark) Color(0xFF78350F).copy(alpha = 0.6f) else Color(0xFFFEF3C7)
        else -> if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.85f)
    }

    val cardBorder = when {
        isImpossible -> Color.Gray
        level == VitalAlertLevel.DANGER -> Color(0xFFEF4444)
        level == VitalAlertLevel.WARNING -> Color(0xFFF59E0B)
        else -> if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0)
    }

    val cardColor = when {
        isImpossible -> Color.Gray
        level == VitalAlertLevel.DANGER -> Color(0xFFEF4444)
        level == VitalAlertLevel.WARNING -> Color(0xFFF59E0B)
        else -> defaultColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(42.dp).background(cardColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = cardColor, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E))
                    if (isImpossible) {
                        Text("Check this value", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    } else if (level != VitalAlertLevel.NORMAL && !item?.alertText.isNullOrBlank()) {
                        Text(item?.alertText ?: "", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (level == VitalAlertLevel.DANGER) Color(0xFFEF4444) else Color(0xFFD97706))
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timeAgo,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (timeAgo == "No recent data") Color.Gray else (if (isDark) Color.White.copy(alpha = 0.8f) else Color.DarkGray)
                )
            }
        }
    }
}

@Composable
fun CaregiverMedicineCard(med: MedicineSnapshot, formatTime: (Long) -> String) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(med.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E))
                    Text(med.dose, fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))

            val slotsToDisplay = if (med.slots.isNotEmpty()) {
                med.slots
            } else {
                med.time.split(",").map { it.trim() }.filter { it.isNotBlank() }.map { 
                    DoseSlotSnapshot(slot = it, taken = med.taken, takenAt = med.takenAt, status = if (med.taken) "Taken" else "Due")
                }
            }

            slotsToDisplay.forEach { slotInfo ->
                val status = slotInfo.status.ifBlank { if (slotInfo.taken) "Taken" else "Due" }
                val (statusText, badgeColor, textColor) = when {
                    status.equals("Taken", ignoreCase = true) -> Triple("Taken ✓ (${formatTime(slotInfo.takenAt)})", Color(0xFF10B981), Color(0xFF047857))
                    status.equals("Missed", ignoreCase = true) -> Triple("Missed ⚠️", Color(0xFFEF4444), Color(0xFFB91C1C))
                    else -> Triple("Due", Color(0xFFF59E0B), Color(0xFFB45309))
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                status.equals("Taken", ignoreCase = true) -> Icons.Default.CheckCircle
                                status.equals("Missed", ignoreCase = true) -> Icons.Default.Error
                                else -> Icons.Default.Schedule
                            },
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = slotInfo.slot,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF1A237E)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = badgeColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiHealthInsightsCard(vitals: VitalsSnapshot, isBn: Boolean, isDark: Boolean) {
    val haptic = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }

    val summaryText = remember(vitals, isBn) {
        val dangers = vitals.activeDangerAlerts
        when {
            dangers.isNotEmpty() -> {
                if (isBn) {
                    "⚠️ সতর্কতা: রোগীর রিয়েল-টাইম ভাইটালসে ${dangers.size}টি ঝুঁকিপূর্ণ রিডিং পাওয়া গেছে। অবিলম্বে চিকিৎসকের পরামর্শ নিন।"
                } else {
                    "⚠️ Warning: Detected ${dangers.size} critical vitals reading(s). Immediate medical review is advised."
                }
            }
            vitals.bp == null && vitals.pulse == null && vitals.spo2 == null -> {
                if (isBn) {
                    "ℹ️ এই রোগীর জন্য কোনো সাম্প্রতিক ভাইটাল ডেটা উপলব্ধ নেই।"
                } else {
                    "ℹ️ No recent vital sign readings available for this patient yet."
                }
            }
            else -> {
                if (isBn) {
                    "✅ রোগীর ভাইটাল সাইনসমূহ (BP, Pulse, SpO2, Glucose) স্থিতিশীল রয়েছে। নিয়মিত পর্যবেক্ষণ চালিয়ে যান।"
                } else {
                    "✅ Patient's vital signs (BP, Pulse, SpO2, Glucose) are currently stable within normal clinical ranges. Continue regular monitoring."
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                expanded = !expanded
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
        ),
        border = BorderStroke(
            1.5.dp,
            if (vitals.activeDangerAlerts.isNotEmpty()) Color(0xFFEF4444)
            else if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF1A237E).copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        color = (if (vitals.activeDangerAlerts.isNotEmpty()) Color(0xFFEF4444) else Color(0xFF0284C7)).copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (vitals.activeDangerAlerts.isNotEmpty()) Color(0xFFEF4444) else Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isBn) "✨ এআই হেলথ ইনসাইটস" else "✨ AI Health Insights",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Color.White else Color(0xFF1A237E)
                        )
                        Text(
                            text = if (isBn) "রিয়েল-টাইম ক্লিনিক্যাল সামারি" else "Real-time clinical trend summary",
                            fontSize = 11.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                        )
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )
                }
            }

            AnimatedVisibility(visible = expanded || vitals.activeDangerAlerts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider(
                        color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.3f)
                    )
                    Text(
                        text = summaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
