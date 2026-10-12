package com.srcreation.smarthelp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.srcreation.smarthelp.db.AppDatabase
import androidx.work.OneTimeWorkRequestBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.FragmentActivity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import androidx.core.net.toUri
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                SettingsScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val prefManager = remember { PreferenceManager(context) }
    
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showFinalClearConfirm by remember { mutableStateOf(false) }
    var unsyncedCountState by remember { mutableIntStateOf(0) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var showWidgetGuideDialog by remember { mutableStateOf(false) }
    var showBatteryDialog by remember { mutableStateOf(false) }

    var isBiometricEnabled by remember { mutableStateOf(prefManager.isBiometricLockEnabled()) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var currentThemeMode by remember { mutableIntStateOf(prefManager.getThemeMode()) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var currentPin by remember { mutableStateOf(prefManager.getAppLock() ?: "") }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val json = BackupManager(context).createBackup()
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(json.toByteArray())
                    }
                    Toast.makeText(context, "Backup saved", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                val success = BackupManager(context).restoreBackup(it)
                if (success) {
                    Toast.makeText(context, "Data restored. Please restart app.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Restore failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun clearAppCache() {
        try {
            context.cacheDir.deleteRecursively()
            Toast.makeText(context, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(context, "Error clearing cache", Toast.LENGTH_SHORT).show()
        }
    }

    val isFromNurse = (context as? Activity)?.intent?.getBooleanExtra("FROM_NURSE", false) ?: false

    val isDark = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.nav_settings), fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
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
        bottomBar = { AppBottomBar(context = context, currentTab = 4, isNurseMode = isFromNurse, backgroundBrush = AppGradientBrush) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Medical / Caregiver Profile Card
                    val currentUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
                    val userEmail = currentUser?.email ?: if (Locale.getDefault().language == "bn") "ইমেইল যুক্ত করা হয়নি" else "No Email Associated"
                    val userUid = currentUser?.uid ?: "N/A"
                    val isBnSetting = remember { Locale.getDefault().language == "bn" }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.9f) else Color.White,
                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF1A237E).copy(alpha = 0.2f)),
                        shadowElevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Surface(
                                        modifier = Modifier.size(52.dp),
                                        shape = CircleShape,
                                        color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)).copy(alpha = 0.15f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isFromNurse) Icons.Default.SupervisorAccount else Icons.Default.Badge,
                                                contentDescription = null,
                                                tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = if (isFromNurse) (if (isBnSetting) "কেয়ারগিভার প্রোফাইল" else "Caregiver Profile") else (if (isBnSetting) "মেডিকেল প্রোফাইল" else "Medical Profile"),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 17.sp,
                                            color = if (isDark) Color.White else Color(0xFF1A237E)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF22C55E).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = if (isFromNurse) "CAREGIVER ID: ${userUid.take(8).uppercase()}" else "PATIENT ID: ${userUid.take(8).uppercase()}",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF22C55E)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                            Spacer(Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                Text(
                                    text = userEmail,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray
                                )
                            }

                            Spacer(Modifier.height(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                                Text(
                                    text = "UID: $userUid",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    maxLines = 1
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    BackgroundCheckScheduler.cancelWorkers(context)
                                    AuthRepository().logout()
                                    val intent = Intent(context, LoginActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                    context.startActivity(intent)
                                    (context as? Activity)?.finish()
                                },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                border = BorderStroke(1.2.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isBnSetting) "লগআউট করুন (Logout)" else "Logout",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }

                    SettingsSection(stringResource(id = R.string.settings_general))
                    SettingsItem(stringResource(id = R.string.settings_app_language), Icons.Default.Language) {
                        showLanguageDialog = true
                    }
                    val themeSubtitle = when (currentThemeMode) {
                        AppCompatDelegate.MODE_NIGHT_YES -> if (Locale.getDefault().language == "bn") "ডার্ক মোড" else "Dark Mode"
                        AppCompatDelegate.MODE_NIGHT_NO -> if (Locale.getDefault().language == "bn") "লাইট মোড" else "Light Mode"
                        else -> if (Locale.getDefault().language == "bn") "অটো (সিস্টেম ডিফল্ট)" else "Auto (System Default)"
                    }
                    SettingsItem(if (Locale.getDefault().language == "bn") "অ্যাপ থিম মোড" else "App Theme Mode", Icons.Default.BrightnessMedium, subtitle = themeSubtitle) {
                        showThemeDialog = true
                    }
                    SettingsItem(if (Locale.getDefault().language == "bn") "হোমস্ক্রিন উইজেট গাইড" else "Home Screen Widget Guide", Icons.Default.Widgets) {
                        showWidgetGuideDialog = true
                    }
                    SettingsItem(if (Locale.getDefault().language == "bn") "ব্যাটারি ও অ্যালার্ম অপ্টিমাইজেশন" else "Battery & Alarm Optimization", Icons.Default.BatteryChargingFull) {
                        showBatteryDialog = true
                    }
                    
                    SettingsSection(if (Locale.getDefault().language == "bn") "ব্যাকগ্রাউন্ড চেক" else "Background Checks")
                    var alertChecksEnabled by remember { mutableStateOf(PreferenceManager(context).isAlertChecksEnabled()) }
                    SettingsToggleItem(
                        title = if (Locale.getDefault().language == "bn") "অ্যালার্ট চেক (Alert Checks)" else "Alert checks",
                        icon = Icons.Default.NotificationsActive,
                        checked = alertChecksEnabled
                    ) { enabled ->
                        alertChecksEnabled = enabled
                        PreferenceManager(context).setAlertChecksEnabled(enabled)
                        if (enabled) {
                            BackgroundCheckScheduler.scheduleWorkers(context)
                        } else {
                            BackgroundCheckScheduler.cancelWorkers(context)
                        }
                    }
                    Text(
                        text = if (Locale.getDefault().language == "bn")
                            "চেকগুলো প্রতি ১৫ মিনিটে চলে, ব্যাটারি সেভারের কারণে দেরি হতে পারে বা বাদ পড়তে পারে, এবং জরুরি অবস্থায় এগুলোর উপর নির্ভর করা যাবে না।"
                        else
                            "Checks run about every 15 minutes, can be delayed or skipped by battery saver, and must not be relied on in an emergency.",
                        fontSize = 11.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            BatteryOptimizationHelper.openBatteryOptimizationSettings(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.BatteryChargingFull, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (Locale.getDefault().language == "bn") "ব্যাকগ্রাউন্ড অ্যাক্টিভিটি অনুমোদন করুন" else "Allow background activity")
                    }

                    if (BuildConfig.DEBUG) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val workRequest1 = OneTimeWorkRequestBuilder<CaregiverCheckWorker>().build()
                                val workRequest2 = OneTimeWorkRequestBuilder<PatientCheckWorker>().build()
                                androidx.work.WorkManager.getInstance(context).enqueue(listOf(workRequest1, workRequest2))
                                Toast.makeText(context, "Enqueued debug background check workers", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Run alert check now (Debug)")
                        }
                    }

                    SettingsSection(stringResource(id = R.string.settings_security))
                    SettingsItem(
                        title = stringResource(id = R.string.settings_app_lock),
                        icon = Icons.Default.Lock,
                        subtitle = if (currentPin.isEmpty()) "Off" else "On"
                    ) {
                        showPinSetupDialog = true
                    }

                    SettingsSection(stringResource(id = R.string.settings_account_data))
                    SettingsItem(stringResource(id = R.string.settings_backup), Icons.Default.CloudUpload) {
                        createBackupLauncher.launch("SmartHelp_Backup_${System.currentTimeMillis()}.json")
                    }
                    SettingsItem(stringResource(id = R.string.settings_restore), Icons.Default.Restore) {
                        restoreBackupLauncher.launch(arrayOf("application/json"))
                    }
                    SettingsItem(stringResource(id = R.string.settings_clear_cache), Icons.Default.DeleteSweep) {
                        clearAppCache()
                    }
                    
                    SettingsItem(
                        title = if (Locale.getDefault().language == "bn") "এই ফোনের তথ্য মুছে ফেলুন" else "Clear this phone's data",
                        icon = Icons.Default.DeleteForever,
                        subtitle = "Irreversible action"
                    ) {
                        val db = AppDatabase.getDatabase(context)
                        scope.launch(Dispatchers.IO) {
                            val count = db.bpDao().getUnsyncedRecords().size +
                                    db.insulinDao().getUnsyncedRecords().size +
                                    db.stepDao().getUnsyncedRecords().size +
                                    db.tempDao().getUnsyncedRecords().size +
                                    db.pulseDao().getUnsyncedRecords().size +
                                    db.spo2Dao().getUnsyncedRecords().size +
                                    db.medicineDao().getUnsyncedMedicines().size +
                                    db.medicineDoseDao().getUnsyncedDoses().size
                            withContext(Dispatchers.Main) {
                                unsyncedCountState = count
                                showDeleteConfirm = true
                            }
                        }
                    }
                    
                    SettingsSection(stringResource(id = R.string.settings_help_support))
                    SettingsItem(
                        title = if (Locale.getDefault().language == "bn") "নতুন ফিচারের পরামর্শ দিন (Suggest A Feature)" else "Suggest A Feature",
                        icon = Icons.Default.Lightbulb
                    ) {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:srcreationsolution@gmail.com".toUri()
                            putExtra(Intent.EXTRA_SUBJECT, "Smart Help - Feature Suggestion")
                        }
                        try { context.startActivity(intent) } catch (_: Exception) { Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show() }
                    }

                    SettingsItem(
                        title = if (Locale.getDefault().language == "bn") "সমস্যা রিপোর্ট করুন (Report an Issue)" else "Report an Issue",
                        icon = Icons.Default.BugReport
                    ) {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:srcreationsolution@gmail.com".toUri()
                            putExtra(Intent.EXTRA_SUBJECT, "Smart Help - Bug Report / Issue")
                        }
                        try { context.startActivity(intent) } catch (_: Exception) { Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show() }
                    }

                    SettingsItem(
                        title = if (Locale.getDefault().language == "bn") "বন্ধু ও পরিবারকে ইনভাইট করুন (Invite Friend)" else "Invite Friend Smart Help",
                        icon = Icons.Default.Share
                    ) {
                        val shareText = if (Locale.getDefault().language == "bn")
                            "স্মার্ট হেল্প (Smart Help) অ্যাপটি ব্যবহার করে আপনার পরিবার ও রোগীর স্বাস্থ্য পরিচালনা করুন। ডাউনলোড করুন: https://play.google.com/store/apps/details?id=${context.packageName}"
                        else
                            "Manage your health and patient care with Smart Help app! Download now: https://play.google.com/store/apps/details?id=${context.packageName}"
                        
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Invite Friend via"))
                    }

                    SettingsItem(stringResource(id = R.string.settings_rate_app), Icons.Default.Star) {
                        val intent = Intent(Intent.ACTION_VIEW, "market://details?id=${context.packageName}".toUri())
                        try { context.startActivity(intent) } catch (_: Exception) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=${context.packageName}".toUri()))
                        }
                    }
                    SettingsItem(stringResource(id = R.string.settings_privacy_policy), Icons.Default.PrivacyTip) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, "https://srcreationsolution.blogspot.com/p/privacy-policy.html".toUri()))
                    }
                    SettingsItem(stringResource(id = R.string.settings_terms), Icons.Default.Description) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, "https://srcreationsolution.blogspot.com/p/terms.html".toUri()))
                    }
                    SettingsItem(if (Locale.getDefault().language == "bn") "মেডিকেল ডিসক্লেইমার" else "Medical Disclaimer", Icons.Default.VerifiedUser) {
                        showDisclaimerDialog = true
                    }
                    SettingsItem(stringResource(id = R.string.settings_feedback), Icons.Default.Feedback) {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:srcreationsolution@gmail.com".toUri()
                            putExtra(Intent.EXTRA_SUBJECT, "Smart Help App Feedback")
                        }
                        try { context.startActivity(intent) } catch (_: Exception) { Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show() }
                    }
                    SettingsItem(stringResource(id = R.string.settings_about), Icons.Default.Info) {
                        showAboutDialog = true
                    }
                    
                    Spacer(Modifier.height(40.dp))
                    Text(
                        stringResource(id = R.string.app_version, "1.0.24"),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )
                    Text(
                        stringResource(id = R.string.developed_by),
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1A237E).copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    if (showWidgetGuideDialog) {
        val isBn = Locale.getDefault().language == "bn"
        AlertDialog(
            onDismissRequest = { showWidgetGuideDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "হোমস্ক্রিন উইজেট যুক্ত করার নিয়ম" else "How to Add Home Screen Widget",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isBn)
                            "১. আপনার ফোনের ডিসপ্লে বা হোমস্ক্রিনের যেকোনো ফাঁকা জায়গায় চেপে ধরে রাখুন (Long Press)।\n\n২. স্ক্রিনের নিচে থাকা 'Widgets' (উইজেট) অপশন সিলেক্ট করুন।\n\n৩. তালিকার 'Smart Help' বা 'Helps' অ্যাপ নির্বাচন করুন।\n\n৪. 'Smart Help Medicine Widget' টেনে এনে (Drag & Drop) হোমস্ক্রিনে বসান।\n\n৫. এখন অ্যাপ না খুলেই হোমস্ক্রিন থেকে ১-ট্যাপে ওষুধ খাওয়া নিশ্চিত করুন এবং ইমার্জেন্সি কিউআর কোড দেখুন!"
                        else
                            "1. Long press on any empty space on your phone's Home Screen.\n\n2. Select 'Widgets' from the bottom menu.\n\n3. Scroll or search for 'Smart Help' app.\n\n4. Drag & Drop the 'Smart Help Medicine Widget' onto your screen.\n\n5. Enjoy 1-Tap medicine intake and Emergency QR access right from your phone's home screen!",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showWidgetGuideDialog = false }) {
                    Text(if (isBn) "বুঝেছি (Got It)" else "Got It")
                }
            }
        )
    }

    if (showBatteryDialog) {
        BatteryOptimizationDialog { showBatteryDialog = false }
    }

    if (showDisclaimerDialog) {
        val isBn = Locale.getDefault().language == "bn"
        AlertDialog(
            onDismissRequest = { showDisclaimerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "মেডিকেল ডিসক্লেইমার" else "Medical Disclaimer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isBn)
                            "স্মার্ট হেল্প (Helps) অ্যাপ্লিকেশনটি শুধুমাত্র শিক্ষা, সাধারণ তথ্যাবলী এবং ব্যক্তিগত ট্র্যাকিংয়ের উদ্দেশ্যে তৈরি করা হয়েছে।\n\nএই অ্যাপে প্রদর্শিত ইনসুলিন ক্যালকুলেটর, আইভি ড্রিপ হিসাব, ল্যাব রেফারেন্স, ড্রাগ ইনফরমেশন বা ফার্স্ট এইড পরামর্শ কোনো অবস্থাতেই রেজিস্টার্ড ডাক্তারের সরাসরি পরামর্শ, ডায়াগনোসিস বা চিকিৎসার বিকল্প নয়।"
                        else
                            "Smart Help is designed for educational reference, informational purposes, and personal tracking only.\n\nThe medical calculators, lab references, drug interaction checks, and guidelines provided in this app do NOT constitute professional medical advice, diagnosis, or treatment. Always consult a qualified registered physician before making any clinical decisions.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showDisclaimerDialog = false }) {
                    Text(if (isBn) "বুঝেছি (I Agree)" else "I Understand")
                }
            }
        )
    }

    if (showAboutDialog) {
        val isBanglaLang = Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn")
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBanglaLang) "Smart Help (স্মার্ট হেল্প)" else "Smart Help Companion",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = if (isDark) Color.White else Color(0xFF1A237E)
                        )
                        Text(
                            text = if (isBanglaLang) "v1.0.24 | ডিজিটাল স্বাস্থ্য সহকারী" else "v1.0.24 | Digital Healthcare Companion",
                            fontSize = 11.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isBanglaLang)
                            "স্মার্ট হেল্প (Smart Help) হলো আপনার ও আপনার পরিবারের স্বাস্থ্য সুরক্ষা, জরুরি চিকিৎসা এবং ক্লিনিক্যাল সহায়তার জন্য তৈরি একটি সর্বাধুনিক অল-ইন-ওয়ান ডিজিটাল স্বাস্থ্য সহকারী। এটি রোগী, পরিবার এবং নার্স ও চিকিৎসকদের জন্য ১০০% নিরাপদ ও অফলাইন-বান্ধব অভিজ্ঞতা প্রদান করে।"
                        else
                            "Smart Help is an advanced all-in-one digital healthcare assistant designed for comprehensive family health management, emergency triage, clinical support, and real-time care coordination. It provides 100% secure, offline-friendly medical tools for patients, families, and healthcare professionals.",
                        fontSize = 13.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155),
                        lineHeight = 18.sp
                    )
                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.5f))
                    Text(
                        text = if (isBanglaLang) "🌟 প্রধান ফিচারসমূহ:" else "🌟 Key Capabilities:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)
                    )
                    Text(
                        text = if (isBanglaLang)
                            "• মেডিকেল রিপোর্ট চেকার ও হেলথ সার্ভিস\n• জরুরি ট্রায়াড, ফার্স্ট এইড ও ১-ট্যাপ SOS হেল্পলাইন\n• স্মার্ট মেডিসিন রিমাইন্ডার, অ্যালার্ম ও পিল ট্র্যাকার\n• ভাইটালস ট্র্যাকার (রক্তচাপ, পালস, SpO2, তাপমাত্রা, সুগার)\n• ডায়াবেটিস ইনসুলিন ও ফ্লুইড ক্যালকুলেটর ও ট্র্যাকার\n• নার্স প্রো হাব ও ক্লিনিক্যাল ক্যালকুলেটর (GCS, Apgar, BSA ইত্যাদি)\n• স্মার্ট ডায়েট, ফুড স্ক্যানার ও ম্যানুয়াল ফুড এন্ট্রি\n• পিরিয়ড, প্রেগনেন্সি ও উইমেন কেয়ার ট্র্যাকার\n• ডিজিটাল মেডিকেল আইডি, কিউআর কোড ও মেডিকেল ভল্ট\n• কেয়ারগিভার পেয়ারিং ও রিয়েল-টাইম সিঙ্ক"
                        else
                            "• Medical Report Checker & Health Service\n• Emergency Triage, First Aid & 1-Tap SOS Helpline\n• Smart Medicine Reminders, Alarms & Refill Tracker\n• Vitals Tracker (BP, Pulse, SpO2, Temperature, Glucose)\n• Insulin & IV Fluid Calculators & Loggers\n• Nurse Pro Clinical Toolkit & Calculators (GCS, Apgar, BSA, etc.)\n• Smart Diet, Food Scanner & Manual Food Entry\n• Period, Pregnancy & Women Care Trackers\n• Digital Medical ID, Emergency QR & Medical Vault\n• Caregiver Pairing & Real-Time Family Sync",
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF475569),
                        lineHeight = 18.sp
                    )
                    HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.5f))
                    Text("Developed by SR Creation Solution\nPackage: com.srcreation.smarthelp", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isBanglaLang) "বন্ধ করুন (Close)" else "Close", fontWeight = FontWeight.Bold, color = if (isDark) Color.Black else Color.White)
                }
            },
            containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }


    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(id = R.string.settings_app_language), fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                            showLanguageDialog = false
                            (context as? Activity)?.recreate()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                    ) {
                        Text("English", fontWeight = FontWeight.Bold, color = if (isDark) Color.Black else Color.White)
                    }

                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("bn"))
                            showLanguageDialog = false
                            (context as? Activity)?.recreate()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                    ) {
                        Text("বাংলা", fontWeight = FontWeight.Bold, color = if (isDark) Color.Black else Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showLanguageDialog = false
                }) { Text(stringResource(id = R.string.cancel), color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray) }
            },
            containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(if (Locale.getDefault().language == "bn") "অ্যাপ থিম মোড নির্বাচন করুন" else "Select App Theme Mode", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val isBn = Locale.getDefault().language == "bn"
                    
                    // Auto
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                                prefManager.setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                                showThemeDialog = false
                                (context as? Activity)?.recreate()
                                Toast.makeText(context, if (isBn) "অটো থিম সেট করা হয়েছে" else "Auto theme applied", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.BrightnessAuto, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(20.dp))
                            Text(if (isBn) "অটো (সিস্টেম ডিফল্ট)" else "Auto (System Default)", fontWeight = FontWeight.Medium, color = if (isDark) Color.White else Color.Black)
                        }
                        RadioButton(
                            selected = currentThemeMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                                prefManager.setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                                showThemeDialog = false
                                (context as? Activity)?.recreate()
                                Toast.makeText(context, if (isBn) "অটো থিম সেট করা হয়েছে" else "Auto theme applied", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // Light
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentThemeMode = AppCompatDelegate.MODE_NIGHT_NO
                                prefManager.setThemeMode(AppCompatDelegate.MODE_NIGHT_NO)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                                showThemeDialog = false
                                (context as? Activity)?.recreate()
                                Toast.makeText(context, if (isBn) "লাইট মোড সেট করা হয়েছে" else "Light mode applied", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.LightMode, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(20.dp))
                            Text(if (isBn) "লাইট মোড (Light)" else "Light Mode", fontWeight = FontWeight.Medium, color = if (isDark) Color.White else Color.Black)
                        }
                        RadioButton(
                            selected = currentThemeMode == AppCompatDelegate.MODE_NIGHT_NO,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentThemeMode = AppCompatDelegate.MODE_NIGHT_NO
                                prefManager.setThemeMode(AppCompatDelegate.MODE_NIGHT_NO)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                                showThemeDialog = false
                                (context as? Activity)?.recreate()
                                Toast.makeText(context, if (isBn) "লাইট মোড সেট করা হয়েছে" else "Light mode applied", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // Dark
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentThemeMode = AppCompatDelegate.MODE_NIGHT_YES
                                prefManager.setThemeMode(AppCompatDelegate.MODE_NIGHT_YES)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                showThemeDialog = false
                                (context as? Activity)?.recreate()
                                Toast.makeText(context, if (isBn) "ডার্ক মোড সেট করা হয়েছে" else "Dark mode applied", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.DarkMode, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(20.dp))
                            Text(if (isBn) "ডার্ক মোড (Dark)" else "Dark Mode", fontWeight = FontWeight.Medium, color = if (isDark) Color.White else Color.Black)
                        }
                        RadioButton(
                            selected = currentThemeMode == AppCompatDelegate.MODE_NIGHT_YES,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentThemeMode = AppCompatDelegate.MODE_NIGHT_YES
                                prefManager.setThemeMode(AppCompatDelegate.MODE_NIGHT_YES)
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                showThemeDialog = false
                                (context as? Activity)?.recreate()
                                Toast.makeText(context, if (isBn) "ডার্ক মোড সেট করা হয়েছে" else "Dark mode applied", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = if (isDark) Color(0xFF0F172A) else Color.White
        )
    }

    if (showPinSetupDialog) {
        AppLockSetupDialog(
            currentPin = currentPin,
            onDismiss = { showPinSetupDialog = false },
            onSave = { newPin, question, answer ->
                prefManager.setAppLock(newPin)
                prefManager.setSecurityQuestion(question, answer)
                currentPin = newPin
                showPinSetupDialog = false
                Toast.makeText(context, "App lock secured!", Toast.LENGTH_SHORT).show()
            },
            onRemove = {
                prefManager.setAppLock(null)
                currentPin = ""
                showPinSetupDialog = false
                Toast.makeText(context, "App lock removed", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showDeleteConfirm) {
        val isBn = Locale.getDefault().language == "bn"
        val cloudNote = stringResource(id = R.string.confirm_clear_cloud_note)
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (isBn) "এই ফোনের তথ্য মুছে ফেলুন" else "Clear This Phone's Data", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text((if (isBn) "সতর্কতা: আপনার কাছে $unsyncedCountState টি অ-সিঙ্ককৃত ডেটা রয়েছে যা স্থায়ীভাবে মুছে যাবে। আপনি কি এগিয়ে যেতে চান?" else "Warning: You have $unsyncedCountState unsynced records on this phone that will be permanently lost if you clear local data. Are you sure you want to proceed?") + "\n\n$cloudNote") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        showFinalClearConfirm = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(if (isBn) "এগিয়ে যান" else "Proceed", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }

    if (showFinalClearConfirm) {
        val isBn = Locale.getDefault().language == "bn"
        val cloudNote = stringResource(id = R.string.confirm_clear_cloud_note)
        AlertDialog(
            onDismissRequest = { showFinalClearConfirm = false },
            title = { Text(if (isBn) "চূড়ান্ত নিশ্চয়তা" else "Final Confirmation", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text((if (isBn) "এটি একটি অপরিবর্তনযোগ্য কাজ। আপনি কি সত্যিই এই ফোনের সমস্ত ডেটা মুছে ফেলতে চান?" else "This action is irreversible. Are you completely sure you want to clear all data on this phone?") + "\n\n$cloudNote") },
            confirmButton = {
                Button(
                    onClick = {
                        prefManager.clearAllData(context) {
                            LocalDataOwnership.resetOwnerUidToUnset(context)
                            Toast.makeText(context, if (isBn) "সমস্ত তথ্য সফলভাবে মুছে ফেলা হয়েছে।" else "All data on this phone deleted successfully.", Toast.LENGTH_LONG).show()
                            showFinalClearConfirm = false
                            (context as? Activity)?.finishAffinity()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(if (isBn) "হ্যাঁ, মুছে ফেলুন" else "Yes, Clear Everything", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinalClearConfirm = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun AppLockSetupDialog(
    currentPin: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
    onRemove: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var step by remember { mutableIntStateOf(1) } // 1: PIN, 2: Confirm, 3: Security Question
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var selectedQuestion by remember { mutableStateOf("What is your pet's name?") }
    var answer by remember { mutableStateOf("") }
    
    val isDark = isSystemInDarkTheme()
    
    val questions = listOf(
        "What is your pet's name?",
        "What is your mother's maiden name?",
        "What was the name of your first school?",
        "In what city were you born?",
        "What is your favorite book?"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
        title = {
            Text(
                text = when(step) {
                    1 -> "Set App Lock PIN"
                    2 -> "Confirm Your PIN"
                    else -> "Security Question"
                },
                fontWeight = FontWeight.Black,
                color = if (isDark) Color.White else Color(0xFF1A237E)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when (step) {
                    1, 2 -> {
                        Text("Enter 4-digit PIN to secure your health records.", fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                        OutlinedTextField(
                            value = if (step == 1) pin else confirmPin,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) { if (step == 1) pin = it else confirmPin = it } },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray
                            )
                        )
                    }
                    3 -> {
                        Text("This will be used to reset your PIN if forgotten.", fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    expanded = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(selectedQuestion, color = if (isDark) Color.White else Color.Black, maxLines = 1)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                containerColor = if (isDark) Color(0xFF0F172A) else Color.White
                            ) {
                                questions.forEach { q ->
                                    DropdownMenuItem(
                                        text = { Text(q, color = if (isDark) Color.White else Color.Black) },
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedQuestion = q; expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        OutlinedTextField(
                            value = answer,
                            onValueChange = { answer = it },
                            placeholder = { Text("Your Answer") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray
                            )
                        )
                    }
                }
                
                if (currentPin.isNotEmpty() && step == 1) {
                    TextButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRemove()
                    }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                        Text("Remove Existing Lock")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    when (step) {
                        1 -> if (pin.length == 4) step = 2
                        2 -> if (pin == confirmPin) step = 3 else pin = "" // mismatch, reset
                        3 -> if (answer.isNotBlank()) onSave(pin, selectedQuestion, answer)
                    }
                },
                enabled = when(step) {
                    1 -> pin.length == 4
                    2 -> confirmPin.length == 4
                    else -> answer.isNotBlank()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                    contentColor = if (isDark) Color.Black else Color.White
                )
            ) {
                Text(if (step == 3) "Finish" else "Next")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            }) { Text(stringResource(id = R.string.cancel), color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray) }
        },
        shape = RoundedCornerShape(28.dp)
    )
}


@Composable
fun SettingsGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.9f) else Color.White,
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.25f) else Color(0xFF1A237E).copy(alpha = 0.12f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String, 
    icon: ImageVector, 
    checked: Boolean, 
    iconColor: Color? = null,
    onCheckedChange: (Boolean) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val tint = iconColor ?: (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = tint.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)
            )
        )
    }
}

@Composable
fun SettingsSection(title: String) {
    val isDark = isSystemInDarkTheme()
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Black,
        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsItem(
    title: String, 
    icon: ImageVector, 
    subtitle: String? = null, 
    iconColor: Color? = null,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val tint = iconColor ?: (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = tint.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
            if (subtitle != null) {
                Text(subtitle, fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = if (isDark) Color.White.copy(alpha = 0.4f) else Color.LightGray, modifier = Modifier.size(20.dp))
    }
}
