package com.srcreation.smarthelp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import kotlinx.coroutines.launch
import java.util.*

class CaregiverLinkHubActivity : AppCompatActivity() {

    private val viewModel: CaregiverLinkHubViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gate: Logged-out check only (redirect to LoginActivity)
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()

        val tabExtra = intent.getExtraValue("TAB")
        val patientIdExtra = intent.getStringExtra("PATIENT_ID")
        viewModel.parseTabExtra(tabExtra, patientIdExtra)

        setContent {
            HelpsTheme {
                CaregiverLinkHubScreen(viewModel = viewModel, onBack = { finish() })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val tabExtra = intent.getExtraValue("TAB")
        val patientIdExtra = intent.getStringExtra("PATIENT_ID")
        viewModel.parseTabExtra(tabExtra, patientIdExtra)
    }

    override fun onStart() {
        super.onStart()
        val dashboardViewModel: CaregiverDashboardViewModel by viewModels()
        dashboardViewModel.onForegroundResume()
        if (BuildConfig.DEBUG) {
            android.util.Log.d("Caregiver", "Activity onStart completed. Active listeners: ${dashboardViewModel.getActiveListenersSummary()}")
        }
    }

    override fun onStop() {
        super.onStop()
        val dashboardViewModel: CaregiverDashboardViewModel by viewModels()
        dashboardViewModel.onBackgroundStop()
        viewModel.detachAllTabListeners()
        if (BuildConfig.DEBUG) {
            android.util.Log.d("Caregiver", "Activity onStop completed. Active listeners: ${dashboardViewModel.getActiveListenersSummary()}")
        }
    }

    private fun Intent.getExtraValue(key: String): Any? {
        if (!hasExtra(key)) return null
        val extras = extras ?: return null
        return extras.get(key)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverLinkHubScreen(
    viewModel: CaregiverLinkHubViewModel,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isBn = remember { Locale.getDefault().language == "bn" }
    val isDark = isSystemInDarkTheme()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val selectedTab by viewModel.selectedTab.collectAsState()
    val isLoadingInitial by viewModel.isLoadingInitial.collectAsState()
    val hasPending by viewModel.hasPendingLinks.collectAsState()
    val dashboardViewModel: CaregiverDashboardViewModel = viewModel()
    val dashSelectedPatient by dashboardViewModel.selectedPatient.collectAsState()
    val dashLinkedPatients by dashboardViewModel.linkedPatients.collectAsState()

    // Single source of truth: sync dashboard's selected patient to Hub ViewModel in ONE DIRECTION only
    LaunchedEffect(dashSelectedPatient) {
        dashSelectedPatient?.let { pInfo ->
            viewModel.setSelectedPatient(HubSelectedPatient(pInfo.patientId, pInfo.name, pInfo.status, pInfo.linkId))
        } ?: viewModel.setSelectedPatient(null)
    }

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
                    Surface(modifier = Modifier.size(54.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Badge, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isBn) "রোগীর তথ্য (Patient Info)" else "Patient Info",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color.White else Color(0xFF1A237E)
                    )
                    Text(
                        text = if (isBn) "সংযুক্ত রোগীদের তালিকা" else "Active Linked Patients",
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))

                if (dashLinkedPatients.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isBn) "কোনো সংযুক্ত রোগী নেই" else "No linked patients found",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                } else {
                    dashLinkedPatients.forEach { pInfo ->
                        val pid = pInfo.patientId
                        val pName = pInfo.name
                        val isSelected = dashSelectedPatient?.patientId == pid

                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = pName,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else (if (isDark) Color.White else Color.Black)
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                dashboardViewModel.selectPatient(pInfo)
                                scope.launch { drawerState.close() }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))

                // Feature Navigation Items
                val navItems = listOf(
                    Triple(if (isBn) "ড্যাশবোর্ড" else "Dashboard", Icons.Default.Dashboard, 1),
                    Triple(if (isBn) "নির্দেশনা পাঠান" else "Send Instruction", Icons.AutoMirrored.Filled.Send, 2),
                    Triple(if (isBn) "হিস্ট্রি ও চার্ট" else "History & Charts", Icons.Default.History, 3),
                    Triple(if (isBn) "নতুন রোগী যুক্ত করুন" else "Link New Patient", Icons.Default.PersonAdd, 0)
                )

                navItems.forEach { (label, icon, tabIdx) ->
                    val isTabSelected = selectedTab == tabIdx
                    NavigationDrawerItem(
                        label = { Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                        selected = isTabSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.selectTab(tabIdx)
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(icon, null, tint = if (isTabSelected) MaterialTheme.colorScheme.primary else Color.Gray) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }
        }
    ) {
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
                                    text = if (isBn) "পেশেন্ট কানেক্ট" else "Connect Patient",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Patient Info", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        actions = {
                            LanguageToggleButton()
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            ) { innerPadding ->
                if (isLoadingInitial) {
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {

                        // Tab Content Body
                        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                            when (selectedTab) {
                                0 -> TabLinkPatientContent(isBn = isBn)
                                1 -> {
                                    CaregiverDashboardScreen(
                                        viewModel = dashboardViewModel,
                                        onBack = {},
                                        onLogout = {},
                                        onLinkPatient = { viewModel.selectTab(0) },
                                        showPatientSelectorCard = false,
                                        showTopBar = false
                                    )
                                }
                                2 -> TabSendInstructionContent(viewModel = dashboardViewModel, isBn = isBn)
                                3 -> {
                                    val pid = dashSelectedPatient?.patientId ?: ""
                                    val name = dashSelectedPatient?.name ?: ""
                                    if (pid.isBlank()) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(if (isBn) "কোনো রোগী নির্বাচিত নেই" else "No patient selected")
                                        }
                                    } else {
                                        val historyViewModel: CaregiverHistoryViewModel = viewModel(key = pid)
                                        LaunchedEffect(pid, name) {
                                            historyViewModel.initPatient(pid, name)
                                        }
                                        CaregiverHistoryScreen(viewModel = historyViewModel, onBack = {}, showTopBar = false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabLinkPatientContent(isBn: Boolean) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val pairingRepo = remember { PairingRepository(context) }
    var codeInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isBn) "রোগী যুক্ত করুন (Link Patient)" else "Link Patient with Code",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = if (isBn) "রোগীর অ্যাপ থেকে প্রাপ্ত ৬-সংখ্যার পেয়ারিং কোডটি এখানে লিখুন।" else "Enter the 6-digit pairing code obtained from your patient's app.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { if (it.length <= 6) codeInput = it },
                    label = { Text(if (isBn) "৬-সংখ্যার কোড" else "6-Digit Code") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            if (codeInput.length < 6) {
                                Toast.makeText(context, if (isBn) "সঠিক ৬-সংখ্যার কোড দিন" else "Enter valid 6-digit code", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    pairingRepo.linkWithCode(codeInput)
                                    Toast.makeText(context, if (isBn) "অনুরোধ সফলভাবে পাঠানো হয়েছে! রোগীর অনুমোদনের জন্য অপেক্ষা করুন।" else "Link request sent successfully! Waiting for patient approval.", Toast.LENGTH_LONG).show()
                                    codeInput = ""
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.localizedMessage ?: "Failed to link", Toast.LENGTH_LONG).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Link, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isBn) "অনুরোধ পাঠান" else "Send Link Request", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TabSendInstructionContent(viewModel: CaregiverDashboardViewModel, isBn: Boolean) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val selectedPatient by viewModel.selectedPatient.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val instructionsRepo = remember { InstructionsRepository() }

    var instructionText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = if (isBn) "নির্দেশনা পাঠান (Send Instruction)" else "Send Care Note / Instruction",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${if (isBn) "রোগী:" else "Patient:"} ${selectedPatient?.name ?: "N/A"}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = instructionText,
                    onValueChange = { instructionText = it },
                    label = { Text(if (isBn) "আপনার বার্তা বা নির্দেশ লিখুন..." else "Type instructions or care notes...") },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    shape = RoundedCornerShape(16.dp)
                )

                if (isSending) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = {
                            val pid = selectedPatient?.patientId ?: return@Button
                            if (instructionText.isBlank()) {
                                Toast.makeText(context, if (isBn) "অনুগ্রহ করে কিছু লিখুন" else "Please enter instruction text", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isSending = true
                            coroutineScope.launch {
                                try {
                                    instructionsRepo.sendInstruction(pid, instructionText.trim())
                                    Toast.makeText(context, if (isBn) "নির্দেশনা সফলভাবে পাঠানো হয়েছে!" else "Instruction sent successfully!", Toast.LENGTH_SHORT).show()
                                    instructionText = ""
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.localizedMessage ?: "Failed to send", Toast.LENGTH_LONG).show()
                                } finally {
                                    isSending = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isBn) "নির্দেশনা পাঠান" else "Send Instruction", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
