package com.srcreation.smarthelp

import android.app.Activity
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.MedicineEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import kotlinx.coroutines.launch
import java.util.*

class MedicineReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                MedicineReminderScreen(onBack = { finish() })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

val TigerIndigo: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF38BDF8) else Color(0xFF1A237E)
val TigerCyan = Color(0xFF00E5FF)
val TigerNeonGreen = Color(0xFF00C853)
val TigerNeonRed = Color(0xFFFF1744)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineReminderScreen(viewModel: MedicineViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val intentProfileId = (context as? Activity)?.intent?.getIntExtra("PROFILE_ID", 1) ?: 1
    
    LaunchedEffect(intentProfileId) {
        viewModel.setProfileId(intentProfileId)
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pref = remember { PreferenceManager(context) }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var medVolume by remember { mutableFloatStateOf(pref.getMedVolume()) }
    var medVibration by remember { mutableStateOf(pref.isMedVibrationEnabled()) }
    var medBgColor by remember { mutableStateOf(pref.getMedAlarmBackground()) }
    var medAlarmTone by remember { mutableStateOf(pref.getMedAlarmTone()) }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            uri?.let {
                pref.setMedAlarmTone(it.toString())
                medAlarmTone = it.toString()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                } catch (_: Exception) {}
            }
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showRefillDialogForMed by remember { mutableStateOf<MedicineEntity?>(null) }
    val medicineList by viewModel.allMedicines.collectAsState(initial = null)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            val isDark = isSystemInDarkTheme()
            ModalDrawerSheet(
                drawerContainerColor = if (isDark) Color(0xFF0F172A) else Color.White,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = TigerIndigo.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.ic_medicine_reminder),
                                contentDescription = null,
                                tint = TigerIndigo,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(id = R.string.med_reminder_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = TigerIndigo
                    )
                    Text(
                        text = "Reminder Tool",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TigerCyan
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))

                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text("Alarm Settings", fontWeight = FontWeight.Black, fontSize = 14.sp, color = TigerIndigo)
                    Spacer(Modifier.height(20.dp))

                    // Volume
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Volume", fontSize = 14.sp, modifier = Modifier.weight(1f))
                    }
                    Slider(
                        value = medVolume,
                        onValueChange = { 
                            medVolume = it
                            pref.setMedVolume(it)
                        },
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(thumbColor = TigerIndigo, activeTrackColor = TigerIndigo)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Vibration
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Vibration", fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Switch(
                            checked = medVibration,
                            onCheckedChange = {
                                medVibration = it
                                pref.setMedVibration(it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = TigerIndigo, checkedTrackColor = TigerIndigo.copy(0.3f))
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Background Color
                    Text("Alarm Background", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("#1A237E", "#EF4444", "#22C55E", "#F59E0B").forEach { colorHex ->
                            val color = Color(android.graphics.Color.parseColor(colorHex))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (medBgColor == colorHex) 2.dp else 0.dp,
                                        color = if (medBgColor == colorHex) TigerCyan else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        medBgColor = colorHex
                                        pref.setMedAlarmBackground(colorHex)
                                    }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Alarm Tone
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Tone")
                                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, medAlarmTone?.let { Uri.parse(it) })
                            }
                            ringtonePickerLauncher.launch(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black,
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MusicNote, null, tint = Color.White)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Alarm Tone", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                val toneName = if (medAlarmTone != null) {
                                    try {
                                        RingtoneManager.getRingtone(context, Uri.parse(medAlarmTone)).getTitle(context)
                                    } catch (e: Exception) { "Custom Tone" }
                                } else "Default Ringtone"
                                Text(toneName, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
            val intentProfileId = (context as? Activity)?.intent?.getIntExtra("PROFILE_ID", 1) ?: 1
            val isWomenProfile = intentProfileId == 3
            val isBn = remember { Locale.getDefault().language == "bn" }

            val topTitle = if (isWomenProfile) {
                if (isBn) "নারী চিকিৎসা ও ঔষধ রিমাইন্ডার" else "Women Medicine Reminder"
            } else {
                stringResource(id = R.string.med_reminder_title)
            }

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(topTitle, fontSize = 18.sp, fontWeight = FontWeight.Black, color = TigerIndigo) },
                        navigationIcon = {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Default.Menu, null, tint = TigerIndigo)
                            }
                        },
                        actions = { LanguageToggleButton() },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                },
                bottomBar = { AppBottomBar(context = context, currentTab = 1, isWomenMode = isWomenProfile, backgroundBrush = AppGradientBrush) },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showAddDialog = true
                        },
                        containerColor = TigerIndigo,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            ) { innerPadding ->
                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    if (medicineList == null) {
                        MedicineReminderSkeleton()
                    } else {
                        MedicineListHeader(medicineList!!)
                        Spacer(modifier = Modifier.height(12.dp))

                        val scope = rememberCoroutineScope()
                        // 💊 Visual Pill Box Schedule Grid
                        VisualPillBoxGrid(
                            medicines = medicineList!!,
                            onMarkSlotTaken = { slotName, meds ->
                                meds.forEach { med ->
                                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                        MedicineRepository(context).markDoseTaken(med.id, slotName)
                                    }

                                    // 🌐 Caregiver Sync
                                    CaregiverSyncRepository.syncMedicineTaken(
                                        context = context,
                                        medicineName = med.name,
                                        dosage = "${med.dosageValue} ${med.dosageUnit}".trim(),
                                        timeCategory = slotName,
                                        status = "TAKEN"
                                    )
                                }
                                Toast.makeText(context, "✓ $slotName medicines marked as taken!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(id = R.string.active_schedule),
                            modifier = Modifier.padding(horizontal = 24.dp),
                            color = TigerIndigo.copy(0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp, start = 20.dp, end = 20.dp, top = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(medicineList!!) { med ->
                                MedicineTigerCard(
                                    med = med,
                                    onDelete = { viewModel.deleteMedicine(med) },
                                    onRefill = { showRefillDialogForMed = med }
                                )
                            }
                        }
                    }
                }

                if (showAddDialog) {
                    AddMedicineTigerDialog(
                        existingMeds = medicineList ?: emptyList(),
                        onDismiss = { showAddDialog = false },
                        onAdd = { name, dosageValue, dosageUnit, cat, food, times, custom, days, cont, stock ->
                            viewModel.addMedicine(name, dosageValue, dosageUnit, cat, food, times, custom, days, cont, stock)
                            showAddDialog = false
                        }
                    )
                }

                showRefillDialogForMed?.let { medToRefill ->
                    RefillMedicineDialog(
                        med = medToRefill,
                        onDismiss = { showRefillDialogForMed = null },
                        onRefill = { addQty ->
                            val newTotal = medToRefill.currentStock + addQty
                            val newInitial = (medToRefill.initialStock + addQty).coerceAtLeast(newTotal)
                            viewModel.updateMedicine(medToRefill.copy(currentStock = newTotal, initialStock = newInitial))
                            showRefillDialogForMed = null
                            Toast.makeText(context, "Stock refilled for ${medToRefill.name}! Current stock: $newTotal", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MedicineReminderSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Stats Card Skeleton
        SkeletonItem(modifier = Modifier.fillMaxWidth(), height = 150.dp, cornerRadius = 32.dp)
        
        // Active Schedule Text Skeleton
        SkeletonItem(width = 120.dp, height = 16.dp)

        // Medicine Cards Skeleton
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).shimmerLoadingAnimation())
                            Spacer(Modifier.width(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                SkeletonItem(width = 150.dp, height = 18.dp)
                                SkeletonItem(width = 100.dp, height = 12.dp)
                            }
                        }
                        SkeletonItem(modifier = Modifier.fillMaxWidth(), height = 8.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineListHeader(list: List<MedicineEntity>) {
    val lowStockCount = list.count { it.currentStock <= 5 }
    val total = list.size
    val takenToday = list.count { isTakenToday(it.lastTakenTimestamp) }
    val progress = if (total > 0) takenToday.toFloat() / total.toFloat() else 0f
    
    val missedDoses = remember(list) { getMissedDoseCount(list) }

    Column {
        Card(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = TigerIndigo),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(70.dp)) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(70.dp),
                        color = TigerNeonGreen,
                        strokeWidth = 6.dp,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(stringResource(id = R.string.todays_progress), color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    Text(stringResource(id = R.string.med_taken_count, takenToday, total), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    
                    if (missedDoses > 0) {
                        Text(
                            text = "Missed Doses: $missedDoses",
                            color = TigerNeonRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = if(progress == 1f) TigerNeonGreen.copy(0.2f) else Color.White.copy(0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = if(progress == 1f) " " + stringResource(id = R.string.all_done) + " " else " " + stringResource(id = R.string.keep_going) + " ",
                            color = if(progress == 1f) TigerNeonGreen else Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        if (lowStockCount > 0) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TigerNeonRed.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, TigerNeonRed.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = TigerNeonRed)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(id = R.string.low_stock_msg, lowStockCount),
                        color = TigerNeonRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun VisualPillBoxGrid(
    medicines: List<MedicineEntity>,
    onMarkSlotTaken: (slotName: String, meds: List<MedicineEntity>) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()

    val slots = remember(medicines) {
        listOf(
            Triple("Morning", R.string.slot_morning, Color(0xFFFFB74D)),
            Triple("Noon", R.string.slot_noon, Color(0xFF4FC3F7)),
            Triple("Evening", R.string.slot_evening, Color(0xFF81C784)),
            Triple("Night", R.string.slot_night, Color(0xFFBA68C8))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.pill_box_title),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = if (isDark) Color.White else TigerIndigo,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            slots.forEach { (slotKey, titleRes, slotColor) ->
                val slotMeds = medicines.filter { it.isActive && it.selectedTimes.contains(slotKey, ignoreCase = true) }
                if (slotMeds.isNotEmpty()) {
                    val allTakenToday = slotMeds.all { isTakenToday(it.lastTakenTimestamp) }

                    Card(
                        modifier = Modifier.width(220.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else slotColor.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else slotColor.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(titleRes),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isDark) Color.White else Color(0xFF0F172A)
                                )
                                if (allTakenToday) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            slotMeds.forEach { med ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(slotColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "${med.name} (${med.dosageValue} ${med.dosageUnit})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onMarkSlotTaken(slotKey, slotMeds)
                                },
                                enabled = !allTakenToday,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (allTakenToday) Color(0xFF22C55E) else slotColor
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(32.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    if (allTakenToday) stringResource(R.string.slot_completed) else stringResource(R.string.btn_mark_slot_taken),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getLocalizedCategory(category: String): String {
    return when (category.trim().lowercase()) {
        "tablet" -> stringResource(R.string.cat_tablet)
        "capsule" -> stringResource(R.string.cat_capsule)
        "cream" -> stringResource(R.string.cat_cream)
        "ointment" -> stringResource(R.string.cat_ointment)
        "spray" -> stringResource(R.string.cat_spray)
        "drop" -> stringResource(R.string.cat_drop)
        "lotion" -> stringResource(R.string.cat_lotion)
        "injection" -> stringResource(R.string.cat_injection)
        "other", "none" -> stringResource(R.string.cat_none)
        else -> category
    }
}

@Composable
fun getLocalizedFoodRelation(food: String): String {
    return when (food.trim().lowercase()) {
        "before meal" -> stringResource(R.string.food_before_meal)
        "after meal" -> stringResource(R.string.food_after_meal)
        else -> food
    }
}

@Composable
fun getLocalizedTimeSlot(time: String): String {
    val clean = time.trim().lowercase()
    return when {
        clean.contains("morning") -> stringResource(R.string.time_morning)
        clean.contains("noon") -> stringResource(R.string.time_noon)
        clean.contains("evening") -> stringResource(R.string.time_evening)
        clean.contains("night") -> stringResource(R.string.time_night)
        clean.contains("4") -> stringResource(R.string.time_4_times)
        clean.contains("set") || clean.contains("custom") -> stringResource(R.string.time_set_time)
        else -> time
    }
}

fun isTakenToday(timestamp: Long): Boolean {
    if (timestamp == 0L) return false
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun MedicineTigerCard(med: MedicineEntity, onDelete: () -> Unit, onRefill: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val isLowStock = med.currentStock <= 3
    val stockColor = if (isLowStock) TigerNeonRed else TigerNeonGreen
    val isTakenToday = isTakenToday(med.lastTakenTimestamp)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = TigerIndigo.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(getTigerIcon(med.category), null, tint = TigerIndigo, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(med.name, fontWeight = FontWeight.Black, fontSize = 18.sp, color = TigerIndigo)
                        if (med.dosageValue.isNotBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = TigerIndigo.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "${med.dosageValue} ${med.dosageUnit}",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TigerIndigo
                                )
                            }
                        }
                    }
                    Text("${getLocalizedCategory(med.category)} • ${getLocalizedFoodRelation(med.foodRelation)}", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    Text(
                        text = stringResource(id = if (isTakenToday) R.string.status_taken else R.string.status_pending),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTakenToday) TigerNeonGreen else TigerNeonRed
                    )
                }
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                }) {
                    Icon(Icons.Default.Delete, null, tint = Color.LightGray)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Time Tags
            OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                med.selectedTimes.split(",").forEach { rawTime ->
                    val timeTag = rawTime.trim()
                    if (timeTag.isNotBlank()) {
                        val displayTag = if ((timeTag.contains("Set", ignoreCase = true) || timeTag.contains("Custom", ignoreCase = true)) && !med.customTime.isNullOrBlank()) {
                            "${getLocalizedTimeSlot("Set Time")} (${med.customTime})"
                        } else getLocalizedTimeSlot(timeTag)

                        Surface(
                            color = TigerIndigo.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, TigerIndigo.copy(alpha = 0.1f))
                        ) {
                            Text(
                                text = displayTag,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TigerIndigo
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stock Bar & Refill Button
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(id = R.string.med_stock_left, med.currentStock),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) TigerNeonRed else Color.Gray
                    )
                    Spacer(Modifier.height(4.dp))
                    val progress = (med.currentStock.toFloat() / med.initialStock.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                        color = stockColor,
                        trackColor = Color.Gray.copy(0.1f)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRefill()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isLowStock) TigerNeonRed else TigerIndigo),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(stringResource(R.string.btn_refill_stock), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineTigerDialog(
    existingMeds: List<MedicineEntity> = emptyList(),
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String, List<String>, String?, Int, Boolean, Int) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var name by remember { mutableStateOf("") }
    var dosageValue by remember { mutableStateOf("") }
    var dosageUnit by remember { mutableStateOf("mg") }
    var category by remember { mutableStateOf("Tablet") }
    var foodRelation by remember { mutableStateOf("After Meal") }
    val selectedTimes = remember { mutableStateListOf<String>() }
    var customTime by remember { mutableStateOf<String?>(null) }
    var durationDays by remember { mutableStateOf("7") }
    var isContinuous by remember { mutableStateOf(false) }
    var stock by remember { mutableStateOf("30") }

    val categories = listOf("Tablet", "Capsule", "Cream", "Ointment", "Spray", "Drop", "Lotion","Injection" ,"None")
    val units = listOf("mg", "mcg", "gm", "ml", "unit","Vial","Amp")
    val foodOptions = listOf("Before Meal", "After Meal")
    val timeOptions = listOf("Morning", "Noon", "Evening", "Night", "4 Times (6 Hourly)", "Set Time")

    val conflict = remember(name, existingMeds) {
        if (name.length >= 2) DrugInteractionEngine.checkConflict(name, existingMeds) else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isSystemInDarkTheme()) Color(0xFF0F172A) else Color.White,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 20.dp)
            .imePadding(),
        confirmButton = {
            Button(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (name.isNotBlank() && selectedTimes.isNotEmpty()) {
                        onAdd(name, dosageValue, dosageUnit, category, foodRelation, selectedTimes.toList(), customTime, durationDays.toIntOrNull() ?: 0, isContinuous, stock.toIntOrNull() ?: 0)
                    }
                },
                enabled = name.isNotBlank() && selectedTimes.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(id = R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            }) { Text(stringResource(id = R.string.cancel)) }
        },
        title = { Text(stringResource(id = R.string.add_med_title), fontWeight = FontWeight.Black, color = TigerIndigo) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 4.dp), 
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name and Dosage Row
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(id = R.string.med_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // ⚠️ Live Drug Conflict Warning
                    conflict?.let { conf ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(stringResource(R.string.drug_conflict_title), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        stringResource(conf.messageResId, conf.newMedName, conf.existingMedName),
                                        fontSize = 11.sp,
                                        color = Color(0xFFC62828)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = dosageValue,
                            onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) dosageValue = it },
                            label = { Text(stringResource(R.string.med_strength_label)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            placeholder = { Text(stringResource(R.string.med_strength_placeholder)) }
                        )

                        // Unit Selection
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.width(100.dp)
                        ) {
                            OutlinedTextField(
                                value = dosageUnit,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                units.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit) },
                                        onClick = {
                                            dosageUnit = unit
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Category Selection
                Text(stringResource(id = R.string.med_category_label), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                category = cat
                            },
                            label = { Text(getLocalizedCategory(cat)) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Food Relation
                Text(stringResource(id = R.string.med_food_relation), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    foodOptions.forEach { opt ->
                        FilterChip(
                            selected = foodRelation == opt,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                foodRelation = opt
                            },
                            label = { Text(getLocalizedFoodRelation(opt)) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Dosage Times
                Text(stringResource(id = R.string.med_dosage_times), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    timeOptions.forEach { opt ->
                        val isSetTime = opt.contains("Set", ignoreCase = true) || opt.contains("Custom", ignoreCase = true)
                        FilterChip(
                            selected = selectedTimes.contains(opt),
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (selectedTimes.contains(opt)) {
                                    selectedTimes.remove(opt)
                                    if (isSetTime) customTime = null
                                } else {
                                    selectedTimes.add(opt)
                                    if (isSetTime) {
                                        val now = Calendar.getInstance()
                                        val initH = customTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: now.get(Calendar.HOUR_OF_DAY)
                                        val initM = customTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: now.get(Calendar.MINUTE)
                                        TimePickerDialog(context, { _, h, m ->
                                            customTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                                        }, initH, initM, false).show()
                                    }
                                }
                            },
                            label = { Text(getLocalizedTimeSlot(opt), maxLines = 1) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                
                val hasSetTimeSelected = selectedTimes.any { it.contains("Set", ignoreCase = true) || it.contains("Custom", ignoreCase = true) }
                if (hasSetTimeSelected) {
                    val displayTime = if (customTime != null) {
                        val parts = customTime!!.split(":")
                        if (parts.size == 2) {
                            val h = parts[0].trim().toIntOrNull() ?: 12
                            val m = parts[1].trim().toIntOrNull() ?: 0
                            val amPm = if (h >= 12) "PM" else "AM"
                            val displayH = if (h % 12 == 0) 12 else h % 12
                            String.format(Locale.getDefault(), "%02d:%02d %s", displayH, m, amPm)
                        } else customTime!!
                    } else "Tap to choose time"

                    Surface(
                        color = TigerIndigo.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val now = Calendar.getInstance()
                                val initH = customTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: now.get(Calendar.HOUR_OF_DAY)
                                val initM = customTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: now.get(Calendar.MINUTE)
                                TimePickerDialog(context, { _, h, m ->
                                    customTime = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                                }, initH, initM, false).show()
                            }
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, tint = TigerIndigo, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Selected Custom Time: $displayTime", color = TigerIndigo, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                // Duration
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(id = R.string.med_duration), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f), color = Color.Gray)
                    Text(stringResource(id = R.string.med_continuous), fontSize = 12.sp)
                    Checkbox(checked = isContinuous, onCheckedChange = { isContinuous = it })
                }
                
                if (!isContinuous) {
                    OutlinedTextField(
                        value = durationDays,
                        onValueChange = { if (it.all { c -> c.isDigit() }) durationDays = it },
                        label = { Text(stringResource(id = R.string.med_days_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // Stock
                OutlinedTextField(
                    value = stock,
                    onValueChange = { if (it.all { c -> c.isDigit() }) stock = it },
                    label = { Text(stringResource(id = R.string.med_stock)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text(stringResource(id = R.string.med_stock_placeholder)) },
                    singleLine = true
                )
            }
        },
        shape = RoundedCornerShape(32.dp)
    )
}

fun getMissedDoseCount(medicines: List<MedicineEntity>): Int {
    var count = 0
    val now = Calendar.getInstance()
    val curH = now.get(Calendar.HOUR_OF_DAY)
    val curM = now.get(Calendar.MINUTE)

    medicines.forEach { med ->
        val takenToday = isTakenToday(med.lastTakenTimestamp)
        val times = med.selectedTimes.split(",")
        times.forEach { rawKey ->
            val timeKey = rawKey.trim()
            val is4Times = timeKey.contains("4", ignoreCase = true) || timeKey.contains("6", ignoreCase = true)
            val isCustom = timeKey.contains("Set", ignoreCase = true) || timeKey.contains("Custom", ignoreCase = true)

            when {
                timeKey.equals("Morning", ignoreCase = true) -> if (isMissed(8, 0, curH, curM, med.lastTakenTimestamp, takenToday)) count++
                timeKey.equals("Noon", ignoreCase = true) -> if (isMissed(14, 0, curH, curM, med.lastTakenTimestamp, takenToday)) count++
                timeKey.equals("Evening", ignoreCase = true) -> if (isMissed(18, 0, curH, curM, med.lastTakenTimestamp, takenToday)) count++
                timeKey.equals("Night", ignoreCase = true) -> if (isMissed(22, 0, curH, curM, med.lastTakenTimestamp, takenToday)) count++
                is4Times -> {
                    listOf(6 to 0, 12 to 0, 18 to 0, 0 to 0).forEach { (h, m) ->
                        if (isMissed(h, m, curH, curM, med.lastTakenTimestamp, takenToday)) count++
                    }
                }
                isCustom -> {
                    med.customTime?.split(":")?.let {
                        if (it.size == 2) {
                            val h = it[0].trim().toIntOrNull() ?: 8
                            val m = it[1].trim().toIntOrNull() ?: 0
                            if (isMissed(h, m, curH, curM, med.lastTakenTimestamp, takenToday)) count++
                        }
                    }
                }
            }
        }
    }
    return count
}

@Composable
fun RefillMedicineDialog(
    med: MedicineEntity,
    onDismiss: () -> Unit,
    onRefill: (Int) -> Unit
) {
    var addQtyInput by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.refill_dialog_title) + ": ${med.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.current_stock, med.currentStock, med.dosageUnit), fontSize = 12.sp, color = Color.Gray)
                OutlinedTextField(
                    value = addQtyInput,
                    onValueChange = { addQtyInput = it },
                    label = { Text(stringResource(R.string.refill_quantity_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val addQty = addQtyInput.toIntOrNull()
                    if (addQty != null && addQty > 0) {
                        onRefill(addQty)
                    }
                },
                enabled = (addQtyInput.toIntOrNull() ?: 0) > 0
            ) {
                Text(stringResource(R.string.btn_refill_stock))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.cancel)) }
        }
    )
}

fun isMissed(h: Int, m: Int, curH: Int, curM: Int, lastTakenTs: Long, takenToday: Boolean): Boolean {
    if (curH > h || (curH == h && curM > m)) {
        if (!takenToday) return true
        val lastTaken = Calendar.getInstance().apply { timeInMillis = lastTakenTs }
        val lastH = lastTaken.get(Calendar.HOUR_OF_DAY)
        val lastM = lastTaken.get(Calendar.MINUTE)
        return lastH < h || (lastH == h && lastM < m)
    }
    return false
}

@Composable
fun getTigerIcon(category: String): ImageVector {
    return when (category) {
        "Tablet" -> ImageVector.vectorResource(id = R.drawable.ic_medicine_reminder)
        "Capsule" -> Icons.Default.Medication
        "Cream", "Ointment" -> Icons.Default.AutoFixHigh
        "Spray", "Lotion" -> Icons.Default.Opacity
        "Drop" -> Icons.Default.WaterDrop
        else -> Icons.Default.MedicalServices
    }
}
