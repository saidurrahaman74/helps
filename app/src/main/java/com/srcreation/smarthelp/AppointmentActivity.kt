package com.srcreation.smarthelp

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.AppointmentEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme

class AppointmentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                AppointmentScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentScreen(viewModel: AppointmentViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val appointments by viewModel.allAppointments.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(id = R.string.appointment_title), fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showAddDialog = true
                },
                containerColor = if (isDark) Color(0xFF0284C7) else Color(0xFF1A237E),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, stringResource(id = R.string.add_appointment))
            }
        },
        bottomBar = { AppBottomBar(context = context, currentTab = 1, backgroundBrush = AppGradientBrush) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                if (appointments.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(appointments) { appointment ->
                            AppointmentCard(
                                appointment = appointment,
                                onDelete = { viewModel.deleteAppointment(appointment) },
                                onComplete = { 
                                    viewModel.updateAppointment(appointment.copy(isCompleted = !appointment.isCompleted))
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    val saveSuccess = stringResource(id = R.string.save)

    if (showAddDialog) {
        AddAppointmentDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { doc, spec, cham, date, time, note ->
                viewModel.addAppointment(doc, spec, cham, date, time, note)
                showAddDialog = false
                Toast.makeText(context, saveSuccess, Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun AppointmentCard(appointment: AppointmentEntity, onDelete: () -> Unit, onComplete: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (appointment.isCompleted) Color(0xFF22C55E).copy(alpha = 0.08f) else (if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.6f))
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (appointment.isCompleted) Color(0xFF22C55E).copy(alpha = 0.3f) else (if (isDark) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f))
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF1A237E).copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(appointment.doctorName, fontWeight = FontWeight.Black, fontSize = 17.sp, color = if (isDark) Color.White else Color.Black)
                        Text(appointment.specialty, fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    }
                }
                
                IconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                }) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.2f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                InfoItem(Icons.Default.CalendarToday, appointment.appointmentDate, Modifier.weight(1f))
                InfoItem(Icons.Default.AccessTime, appointment.appointmentTime, Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            InfoItem(Icons.Default.LocationOn, appointment.chamberName)
            
            if (!appointment.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color.Black.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        appointment.note, 
                        modifier = Modifier.padding(12.dp),
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        lineHeight = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                data = CalendarContract.Events.CONTENT_URI
                                putExtra(CalendarContract.Events.TITLE, "Doctor Visit: ${appointment.doctorName} (${appointment.specialty})")
                                putExtra(CalendarContract.Events.EVENT_LOCATION, appointment.chamberName)
                                putExtra(CalendarContract.Events.DESCRIPTION, appointment.note ?: "Consultation visit with ${appointment.doctorName}")
                                putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Calendar app not found", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Event, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_add_to_calendar), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onComplete()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (appointment.isCompleted) Color(0xFF22C55E) else Color(0xFF1A237E)
                    )
                ) {
                    Icon(if (appointment.isCompleted) Icons.Default.CheckCircle else Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (appointment.isCompleted) stringResource(id = R.string.status_visited) else stringResource(id = R.string.status_mark_completed), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(icon, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E).copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 13.sp, color = if (isDark) Color.White else Color.Black, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AddAppointmentDialog(onDismiss: () -> Unit, onAdd: (String, String, String, String, String, String?) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var doctor by rememberSaveable { mutableStateOf("") }
    var specialty by rememberSaveable { mutableStateOf("") }
    var chamber by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
        title = { Text(stringResource(id = R.string.add_appointment), fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(
                        value = doctor,
                        onValueChange = { doctor = it },
                        label = { Text(stringResource(id = R.string.doctor_name)) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDark) Color.White else Color.Black,
                            unfocusedTextColor = if (isDark) Color.White else Color.Black,
                            focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                            focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = specialty,
                        onValueChange = { specialty = it },
                        label = { Text(stringResource(id = R.string.specialty_label)) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDark) Color.White else Color.Black,
                            unfocusedTextColor = if (isDark) Color.White else Color.Black,
                            focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                            focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = chamber,
                        onValueChange = { chamber = it },
                        label = { Text(stringResource(id = R.string.chamber_name)) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDark) Color.White else Color.Black,
                            unfocusedTextColor = if (isDark) Color.White else Color.Black,
                            focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                            focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text(stringResource(id = R.string.date_label)) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                                focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                            )
                        )
                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text(stringResource(id = R.string.time_label)) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                                focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                            )
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(stringResource(id = R.string.note_label)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDark) Color.White else Color.Black,
                            unfocusedTextColor = if (isDark) Color.White else Color.Black,
                            focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                            focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (doctor.isNotBlank()) onAdd(doctor, specialty, chamber, date, time, note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF0284C7) else Color(0xFF1A237E))
            ) {
                Text(stringResource(id = R.string.btn_schedule))
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
fun EmptyState() {
    val isDark = isSystemInDarkTheme()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.15f) else Color(0xFF1A237E).copy(alpha = 0.05f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.EventAvailable, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E).copy(alpha = 0.4f), modifier = Modifier.size(50.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(stringResource(id = R.string.no_appointments), fontWeight = FontWeight.Bold, color = Color.Gray)
        Text(stringResource(id = R.string.tap_to_add_appointment), fontSize = 12.sp, color = Color.LightGray)
    }
}
