package com.srcreation.smarthelp

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.VaccineEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.text.SimpleDateFormat
import java.util.*

class VaccineActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                VaccineScreen(onBack = { finish() })
            }
        }
    }
}

val VaccineTaken = Color(0xFF00C853)
val VaccinePending = Color(0xFFFF9100)
val VaccineDeepBlue = Color(0xFF1A237E)
val VaccineNeonTeal = Color(0xFF00E5FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineScreen(viewModel: VaccineViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showAddDialog by remember { mutableStateOf(false) }
    val vaccineList by viewModel.allVaccines.collectAsState(initial = emptyList())

    Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Vaccine Tracker", fontSize = 18.sp, color = Color(0xFF1A237E), fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFF1A237E))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = { AppBottomBar(context = context, currentTab = 1, backgroundBrush = AppGradientBrush) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showAddDialog = true
                    },
                    containerColor = VaccineDeepBlue,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                
                // Marvelous Summary Header
                VaccineStatusHeader(vaccineList)

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Vaccination History",
                        color = VaccineDeepBlue,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        "${vaccineList.size} Records",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp, start = 20.dp, end = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(vaccineList) { vaccine ->
                        VaccineModernCard(vaccine) {
                            viewModel.updateVaccine(vaccine.copy(isTaken = !vaccine.isTaken))
                        }
                    }
                }
            }

            if (showAddDialog) {
                AddVaccineDialog(onDismiss = { showAddDialog = false }) { name, dose, date, isTaken ->
                    viewModel.addVaccine(name, dose, date, isTaken)
                    showAddDialog = false
                }
            }
        }
    }
}

@Composable
fun VaccineStatusHeader(list: List<VaccineEntity>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VaccineStatCard("Completed", list.count { it.isTaken }.toString(), VaccineTaken, Modifier.weight(1f))
        VaccineStatCard("Pending", list.count { !it.isTaken }.toString(), VaccinePending, Modifier.weight(1f))
    }
}

@Composable
fun VaccineStatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            Modifier.padding(20.dp).fillMaxWidth(), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (label == "Completed") Icons.Default.CheckCircle else Icons.Default.Timer,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Black, color = color)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = VaccineDeepBlue.copy(0.6f))
        }
    }
}

@Composable
fun VaccineModernCard(vaccine: VaccineEntity, onToggle: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val statusColor = if (vaccine.isTaken) VaccineTaken else VaccinePending

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, statusColor.copy(0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Neon Glow Icon
            Surface(
                modifier = Modifier.size(60.dp),
                shape = RoundedCornerShape(18.dp),
                color = statusColor.copy(0.1f),
                border = BorderStroke(1.dp, statusColor.copy(0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (vaccine.isTaken) Icons.Default.Verified else Icons.Default.Vaccines,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    vaccine.name, 
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 17.sp, 
                    color = VaccineDeepBlue,
                    maxLines = 1
                )
                Text(
                    vaccine.doseInfo, 
                    fontSize = 12.sp, 
                    color = VaccineDeepBlue.copy(0.6f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = (if(vaccine.isTaken) VaccineTaken else Color.Gray).copy(0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.EventAvailable, 
                            null, 
                            modifier = Modifier.size(10.dp), 
                            tint = if(vaccine.isTaken) VaccineTaken else Color.Gray
                        )
                        Text(
                            " ${vaccine.date}", 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold,
                            color = if(vaccine.isTaken) VaccineTaken else Color.Gray
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                },
                modifier = Modifier.background(statusColor.copy(0.1f), CircleShape).size(40.dp)
            ) {
                Icon(
                    imageVector = if (vaccine.isTaken) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVaccineDialog(onDismiss: () -> Unit, onAdd: (String, String, String, Boolean) -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var name by remember { mutableStateOf("") }
    var doseInfo by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("Select Date") }
    var isTaken by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            date = "$day ${SimpleDateFormat("MMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(Calendar.MONTH, month) }.time)} $year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if(name.isNotEmpty()) onAdd(name, doseInfo, date, isTaken)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VaccineDeepBlue)
            ) {
                Text("Save Record")
            }
        },
        dismissButton = { 
            TextButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            }) { Text("Cancel", color = Color.Gray) } 
        },
        title = { Text("💉 New Vaccination Log", fontWeight = FontWeight.Bold, color = VaccineDeepBlue) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()), 
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Vaccine Name") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = doseInfo, 
                    onValueChange = { doseInfo = it }, 
                    label = { Text("Dose Type (e.g. Booster)") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        datePicker.show()
                    },
                    color = Color.LightGray.copy(0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray.copy(0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = VaccineDeepBlue, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(date, color = VaccineDeepBlue, fontWeight = FontWeight.Medium)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically, 
                    modifier = Modifier.fillMaxWidth().clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isTaken = !isTaken
                    }.padding(vertical = 8.dp)
                ) {
                    Switch(
                        checked = isTaken, 
                        onCheckedChange = { isTaken = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = VaccineTaken)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("I have already taken this", fontSize = 14.sp, color = VaccineDeepBlue, fontWeight = FontWeight.Medium)
                }
            }
        }
    )
}
