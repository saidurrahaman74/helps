package com.srcreation.smarthelp

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
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
                    onLogout = {
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
    onLogout: () -> Unit,
    onLinkPatient: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val isBn = remember { Locale.getDefault().language == "bn" }

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
                    actions = {
                        IconButton(onClick = onLinkPatient) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Link Patient", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(Icons.Default.PersonSearch, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = if (isBn) "কোনো রোগী সংযুক্ত নেই" else "No Patient Linked Yet",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBn) "আপনার রোগীর সাথে সংযুক্ত হতে নিচের বাটনে ক্লিক করে পেয়ারিং কোডটি লিখুন।" else "Link a patient using their 6-digit pairing code to view real-time health data.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBn) "সংযুক্ত রোগী:" else "Monitored Patient:",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = selectedPatient?.name ?: "Select Patient",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
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
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp).fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isBn) "স্বাস্থ্য হিস্ট্রি ও চার্ট দেখুন" else "View History & Charts", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

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
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp).fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                            ) {
                                Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isBn) "রোগীকে নির্দেশনা পাঠান" else "Send Instruction to Patient", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Section Title: Real-time Vitals
                    item {
                        Text(
                            text = if (isBn) "⚡ রিয়েল-টাইম ভাইটালস (Real-time Vitals)" else "⚡ Real-Time Vitals",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White else Color(0xFF1A237E)
                        )
                    }

                    // Vitals Cards Grid/List
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // 1. BP
                            RealTimeVitalCard(
                                title = if (isBn) "ব্লাড প্রেসার (BP)" else "Blood Pressure (BP)",
                                value = vitals.bp?.valueText ?: "N/A",
                                timeText = viewModel.formatTime(vitals.bp?.timestamp ?: 0L),
                                icon = Icons.Default.Favorite,
                                color = Color(0xFFEF4444)
                            )

                            // 2. Pulse
                            RealTimeVitalCard(
                                title = if (isBn) "পাল্স / হার্ট রেট (Pulse)" else "Pulse / Heart Rate",
                                value = vitals.pulse?.valueText ?: "N/A",
                                timeText = viewModel.formatTime(vitals.pulse?.timestamp ?: 0L),
                                icon = Icons.Default.FavoriteBorder,
                                color = Color(0xFFEC4899)
                            )

                            // 3. SpO2
                            RealTimeVitalCard(
                                title = if (isBn) "অক্সিজেন স্যাচুরেশন (SpO2)" else "Oxygen Saturation (SpO2)",
                                value = vitals.spo2?.valueText ?: "N/A",
                                timeText = viewModel.formatTime(vitals.spo2?.timestamp ?: 0L),
                                icon = Icons.Default.Air,
                                color = Color(0xFF10B981)
                            )

                            // 4. Insulin / Glucose
                            RealTimeVitalCard(
                                title = if (isBn) "রক্তের গ্লুকোজ / ডায়াবেটিস" else "Blood Glucose",
                                value = vitals.insulin?.valueText ?: "N/A",
                                timeText = viewModel.formatTime(vitals.insulin?.timestamp ?: 0L),
                                icon = Icons.Default.InvertColors,
                                color = Color(0xFF0284C7)
                            )

                            // 5. Temperature
                            RealTimeVitalCard(
                                title = if (isBn) "শরীরের তাপমাত্রা (Temperature)" else "Body Temperature",
                                value = vitals.temp?.valueText ?: "N/A",
                                timeText = viewModel.formatTime(vitals.temp?.timestamp ?: 0L),
                                icon = Icons.Default.Thermostat,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }

                    // Section Title: Medicines
                    item {
                        Text(
                            text = if (isBn) "💊 আজকের ওষুধের তালিকা (Today's Medicines)" else "💊 Today's Medicines",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White else Color(0xFF1A237E)
                        )
                    }

                    if (medicines.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Text(
                                    text = if (isBn) "আজকের কোনো ওষুধ রেকর্ড নেই" else "No medicine records found for today",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@Composable
fun RealTimeVitalCard(
    title: String,
    value: String,
    timeText: String,
    icon: ImageVector,
    color: Color
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
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
                    modifier = Modifier.size(42.dp).background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, fontSize = 13.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E))
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Last Read", fontSize = 10.sp, color = Color.Gray)
                Text(timeText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
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
                val (statusText, badgeColor, textColor) = when (status) {
                    "Taken" -> Triple("Taken ✓ (${formatTime(slotInfo.takenAt)})", Color(0xFF10B981), Color(0xFF047857))
                    "Missed" -> Triple("Missed ⚠️", Color(0xFFEF4444), Color(0xFFB91C1C))
                    else -> Triple("Due", Color(0xFFF59E0B), Color(0xFFB45309))
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (status) {
                                "Taken" -> Icons.Default.CheckCircle
                                "Missed" -> Icons.Default.Error
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
