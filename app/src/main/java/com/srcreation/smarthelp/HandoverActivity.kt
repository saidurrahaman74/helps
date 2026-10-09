package com.srcreation.smarthelp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HandoverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                HandoverScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandoverScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val db = AppDatabase.getDatabase(context)
    val clipboardManager = LocalClipboardManager.current
    
    val profileId = (context as? Activity)?.intent?.getIntExtra("PROFILE_ID", 2) ?: 2
    
    var situation by rememberSaveable { mutableStateOf("") }
    var background by rememberSaveable { mutableStateOf("") }
    var assessment by rememberSaveable { mutableStateOf("") }
    var recommendation by rememberSaveable { mutableStateOf("") }
    
    var patientInfo by remember { mutableStateOf("Patient: Loading...") }
    var vitalInfo by remember { mutableStateOf("Vitals: Loading...") }

    LaunchedEffect(Unit) {
        val profile = db.profileDao().getProfileById(profileId)
        val latestBp = db.bpDao().getAllRecords(profileId).first().firstOrNull()
        val latestInsulin = db.insulinDao().getAllRecords(profileId).first().firstOrNull()
        
        patientInfo = "Patient: ${profile?.name ?: "N/A"} (${profile?.relation ?: "N/A"})"
        vitalInfo = buildString {
            latestBp?.let { append("Last BP: ${it.systolic}/${it.diastolic} mmHg. ") }
            latestInsulin?.let { append("Last Sugar: ${it.glucose} ${it.unit}. ") }
            if (isEmpty()) append("No vital records found today.")
        }
    }

    fun generateFinalReport(): String {
        val today = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        return buildString {
            append("📋 SBAR HANDOVER REPORT\n")
            append("Generated: $today\n")
            append("---------------------------\n")
            append("$patientInfo\n")
            append("$vitalInfo\n\n")
            
            append("S (SITUATION): ${situation.ifBlank { "Unstable/Stable as per shift." }}\n\n")
            append("B (BACKGROUND): ${background.ifBlank { "Admitted for observation/treatment." }}\n\n")
            append("A (ASSESSMENT): ${assessment.ifBlank { "Progressing/Improving." }}\n\n")
            append("R (RECOMMENDATION): ${recommendation.ifBlank { "Monitor vitals and continue meds." }}\n")
            
            append("\n---------------------------\n")
            append("Shared via Smart Help Nurse Pro")
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(id = R.string.sbar_handover), fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    actions = { LanguageToggleButton() },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(stringResource(id = R.string.sbar_subtitle), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    SbarField(stringResource(id = R.string.situation_label), situation) { situation = it }
                    SbarField(stringResource(id = R.string.background_label), background) { background = it }
                    SbarField(stringResource(id = R.string.assessment_label), assessment) { assessment = it }
                    SbarField(stringResource(id = R.string.recommendation_label), recommendation) { recommendation = it }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        val toastMsg = stringResource(id = R.string.toast_handover_copied)
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                clipboardManager.setText(AnnotatedString(generateFinalReport()))
                                Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(id = R.string.btn_copy))
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, generateFinalReport())
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, null))
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(id = R.string.btn_share))
                        }
                    }
                    
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun SbarField(label: String, value: String, onValueChange: (String) -> Unit) {
    val isDark = isSystemInDarkTheme()
    Column {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            placeholder = { Text(stringResource(id = R.string.placeholder_enter_details), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.5f),
                unfocusedContainerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
    }
}
