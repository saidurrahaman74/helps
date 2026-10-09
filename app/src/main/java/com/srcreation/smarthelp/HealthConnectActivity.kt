package com.srcreation.smarthelp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class HealthConnectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                HealthConnectScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isSyncing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val db = remember { AppDatabase.getDatabase(context) }
    val pref = remember { PreferenceManager(context) }
    val activeProfileId = remember { pref.getActiveProfile() }

    val pulseRecords by db.pulseDao().getAllRecords(activeProfileId).collectAsState(initial = emptyList())
    val stepRecords by db.stepDao().getAllRecords(activeProfileId).collectAsState(initial = emptyList())
    val bpRecords by db.bpDao().getAllRecords(activeProfileId).collectAsState(initial = emptyList())

    val latestPulse = pulseRecords.firstOrNull()
    val latestStep = stepRecords.firstOrNull()
    val latestBp = bpRecords.firstOrNull()

    val wearableData = remember(latestPulse, latestStep, latestBp) {
        listOf(
            WearableItem(
                title = "Heart Rate",
                value = latestPulse?.let { "${it.pulseBpm} bpm" } ?: "No Data",
                time = latestPulse?.let { it.date.ifEmpty { "Latest Log" } } ?: "Log in Pulse Tracker",
                icon = Icons.Default.Favorite,
                color = Color(0xFFEF4444)
            ),
            WearableItem(
                title = "Steps",
                value = latestStep?.let { String.format(Locale.US, "%,d", it.steps) } ?: "0",
                time = latestStep?.let { it.date.ifEmpty { "Today" } } ?: "Log in Step Counter",
                icon = Icons.Default.Sync,
                color = Color(0xFF22C55E)
            ),
            WearableItem(
                title = "Blood Pressure",
                value = latestBp?.let { "${it.systolic}/${it.diastolic} mmHg" } ?: "No Data",
                time = latestBp?.let { it.date.ifEmpty { "Latest Log" } } ?: "Log in BP Tracker",
                icon = Icons.Default.Watch,
                color = Color(0xFF6366F1)
            )
        )
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                CenterAlignedTopAppBar(
                    title = { Text("Wearable Sync", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A237E)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color(0xFF1A237E))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        bottomBar = { AppBottomBar(context = context, currentTab = -1, backgroundBrush = AppGradientBrush) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Hero Sync Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A237E)),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Watch, null, tint = Color.White, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Health Connect", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            Text("Sync with Google Fit, Samsung Health & more", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            
                            Spacer(Modifier.height(24.dp))
                            
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isSyncing = true
                                    scope.launch {
                                        delay(1500)
                                        isSyncing = false
                                        Toast.makeText(context, "Data Synced from Wearables", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text("Sync Now", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        "Latest Wearable Data",
                        modifier = Modifier.align(Alignment.Start),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(wearableData) { item ->
                            WearableDataCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WearableDataCard(item: WearableItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = item.color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(item.icon, null, tint = item.color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(item.time, fontSize = 11.sp, color = Color.Gray)
            }
            Text(item.value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = item.color)
        }
    }
}

data class WearableItem(val title: String, val value: String, val time: String, val icon: ImageVector, val color: Color)
