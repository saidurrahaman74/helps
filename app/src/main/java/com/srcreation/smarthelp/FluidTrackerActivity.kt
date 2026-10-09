package com.srcreation.smarthelp

import android.app.Application
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.WaterLogEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FluidTrackerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                FluidTrackerScreen(onBack = { finish() })
            }
        }
    }
}

class FluidViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.waterDao()
    private val today = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
    
    private val profileId = savedStateHandle.get<Int>("PROFILE_ID") ?: 1

    val todayLogs: Flow<List<WaterLogEntity>> = dao.getLogsByDate(profileId, today)
    val medicalId = db.medicalIdDao().getMedicalId(profileId)

    fun addWater(ml: Int, currentTotal: Int, targetMl: Int) {
        viewModelScope.launch {
            dao.insertLog(WaterLogEntity(profileId = profileId, amountMl = ml, date = today))

            // 🌐 Caregiver Sync
            val newTotal = currentTotal + ml
            CaregiverSyncRepository.syncVitals(
                context = getApplication(),
                type = "FLUID",
                title = "দৈনিক তরল খাবার (Fluid Intake)",
                valueText = "$newTotal / $targetMl ml",
                statusTag = if (newTotal >= targetMl) "Limit Reached" else "Normal"
            )
        }
    }

    fun removeLog(log: WaterLogEntity) {
        viewModelScope.launch {
            dao.deleteLog(log)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluidTrackerScreen(viewModel: FluidViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val logs by viewModel.todayLogs.collectAsState(initial = emptyList())
    val medicalId by viewModel.medicalId.collectAsState(initial = null)
    val isDark = isSystemInDarkTheme()

    var selectedCondition by remember { mutableStateOf("STANDARD") } // CKD, HYPERTENSION, DIABETES, STANDARD

    val totalDrank = logs.sumOf { it.amountMl }
    val targetMl = remember(medicalId, selectedCondition) {
        when (selectedCondition) {
            "CKD" -> 1200 // Restricted limit for Kidney Disease
            "HYPERTENSION" -> 1800 // Restricted limit for High BP / Heart Failure
            "DIABETES" -> 2500
            else -> {
                val weight = medicalId?.weight?.toIntOrNull() ?: 70
                (weight * 35).coerceAtLeast(2000)
            }
        }
    }
    val progress = (totalDrank.toFloat() / targetMl).coerceIn(0f, 1f)
    val isNearLimit = totalDrank >= (targetMl * 0.85)

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                CenterAlignedTopAppBar(
                    title = { Text("Fluid & Clinical Diet Advisor", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
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
            }
        },
        bottomBar = { AppBottomBar(context = context, currentTab = 1, backgroundBrush = AppGradientBrush) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Clinical Condition Selector
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.8f)),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    stringResource(R.string.clinical_condition_title),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = selectedCondition == "STANDARD",
                                        onClick = { selectedCondition = "STANDARD" },
                                        label = { Text(stringResource(R.string.condition_standard), fontSize = 11.sp) }
                                    )
                                    FilterChip(
                                        selected = selectedCondition == "CKD",
                                        onClick = { selectedCondition = "CKD" },
                                        label = { Text(stringResource(R.string.condition_ckd), fontSize = 11.sp) }
                                    )
                                    FilterChip(
                                        selected = selectedCondition == "HYPERTENSION",
                                        onClick = { selectedCondition = "HYPERTENSION" },
                                        label = { Text(stringResource(R.string.condition_hypertension), fontSize = 11.sp) }
                                    )
                                    FilterChip(
                                        selected = selectedCondition == "DIABETES",
                                        onClick = { selectedCondition = "DIABETES" },
                                        label = { Text(stringResource(R.string.condition_diabetes), fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    // Water Tank Visual
                    item {
                        AdvancedLiquidTank(progress, totalDrank, targetMl)
                    }

                    // Fluid Restriction Warning Banner
                    if (isNearLimit) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF7F1D1D).copy(alpha = 0.3f) else Color(0xFFFFEBEE)),
                                border = BorderStroke(1.dp, if (isDark) Color(0xFFEF4444).copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFFFCA5A5) else Color.Red,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        stringResource(R.string.fluid_warning_near_limit, totalDrank, targetMl),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFC62828)
                                    )
                                }
                            }
                        }
                    }

                    // Disease-Specific Nutrition Guide Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color(0xFFE0F2FE)),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF0284C7).copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        stringResource(R.string.diet_guideline_title),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                val adviceText = when (selectedCondition) {
                                    "CKD" -> stringResource(R.string.diet_ckd_advice)
                                    "HYPERTENSION" -> stringResource(R.string.diet_hypertension_advice)
                                    "DIABETES" -> stringResource(R.string.diet_diabetes_advice)
                                    else -> "• Drink plain water regularly throughout the day.\n• Avoid sugary energy drinks.\n• Maintain balanced diet."
                                }

                                Text(adviceText, fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A))
                            }
                        }
                    }

                    // Quick Add Buttons
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            WaterAddCard("250ml", Icons.Default.LocalDrink, Modifier.weight(1f)) {
                                viewModel.addWater(250, totalDrank, targetMl)
                            }
                            WaterAddCard("500ml", Icons.Default.WaterDrop, Modifier.weight(1f)) {
                                viewModel.addWater(500, totalDrank, targetMl)
                            }
                        }
                    }

                    item {
                        Text(
                            "Today's Water Intake Log",
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1A237E),
                            fontSize = 15.sp
                        )
                    }

                    if (logs.isEmpty()) {
                        item {
                            Text("No fluid entries logged today.", fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                        }
                    } else {
                        items(logs.reversed()) { log ->
                            WaterLogItem(log) { viewModel.removeLog(log) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WaterAddCard(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = modifier.clickable {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF0284C7).copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7), modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
        }
    }
}

@Composable
fun WaterLogItem(log: WaterLogEntity, onDelete: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color(0xFF0284C7).copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)).copy(0.15f), shape = CircleShape, modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.WaterDrop, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("${log.amountMl} ml", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                    Text(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(log.timestamp)), fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                }
            }
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
            }) {
                Icon(Icons.Default.Delete, null, tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Red.copy(0.6f), modifier = Modifier.size(20.dp))
            }
        }
    }
}
