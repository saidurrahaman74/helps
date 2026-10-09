package com.srcreation.smarthelp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID



class CaregiverInstructionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val patientId = intent.getStringExtra("PATIENT_ID") ?: ""
        val patientName = intent.getStringExtra("PATIENT_NAME") ?: "Patient"

        setContent {
            HelpsTheme {
                CaregiverInstructionsScreen(
                    patientId = patientId,
                    patientName = patientName,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverInstructionsScreen(
    patientId: String,
    patientName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val isDark = isSystemInDarkTheme()
    val isBn = remember { Locale.getDefault().language == "bn" }

    var instructionText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var instructionsList by remember { mutableStateOf<List<InstructionItem>>(emptyList()) }

    // Real-time listener for sent instructions status
    DisposableEffect(patientId) {
        if (patientId.isBlank()) return@DisposableEffect onDispose {}

        val listener: ListenerRegistration = firestore.collection("patients").document(patientId)
            .collection("instructions")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        InstructionItem(
                            id = doc.getString("id") ?: doc.id,
                            text = doc.getString("text") ?: "",
                            sentAt = doc.getLong("sentAt") ?: 0L,
                            seen = doc.getBoolean("seen") ?: false
                        )
                    }.sortedByDescending { it.sentAt }
                    instructionsList = list
                }
            }

        onDispose {
            listener.remove()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isBn) "রোগীকে নির্দেশনা পাঠান" else "Send Instructions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = patientName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Input Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.85f)),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = if (isBn) "নতুন নির্দেশনা লিখুন" else "Write New Instruction",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Color.White else Color(0xFF1A237E)
                        )

                        OutlinedTextField(
                            value = instructionText,
                            onValueChange = { instructionText = it },
                            placeholder = { Text(if (isBn) "যেমন: সময়মতো ওষুধ খাবেন ও পানি পান করবেন..." else "e.g., Take medicine on time and drink water...") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(16.dp)
                        )

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val text = instructionText.trim()
                                if (text.isBlank() || patientId.isBlank()) {
                                    Toast.makeText(context, if (isBn) "দয়া করে কিছু লিখুন" else "Please type an instruction", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isSending = true
                                val id = UUID.randomUUID().toString()
                                val data = hashMapOf(
                                    "id" to id,
                                    "text" to text,
                                    "sentAt" to System.currentTimeMillis(),
                                    "seen" to false
                                )

                                firestore.collection("patients").document(patientId)
                                    .collection("instructions").document(id)
                                    .set(data)
                                    .addOnSuccessListener {
                                        isSending = false
                                        instructionText = ""
                                        Toast.makeText(context, if (isBn) "নির্দেশনা পাঠানো হয়েছে! ✓" else "Instruction sent! ✓", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        isSending = false
                                        Toast.makeText(context, "Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isSending
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "নির্দেশনা পাঠান" else "Send Instruction", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Previously Sent Instructions Header
                Text(
                    text = if (isBn) "📨 পাঠানো নির্দেশনাসমূহ (Sent History)" else "📨 Sent Instructions History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF1A237E)
                )

                if (instructionsList.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = if (isBn) "কোনো নির্দেশনা পাঠানো হয়নি" else "No instructions sent yet",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(instructionsList) { item ->
                            SentInstructionCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SentInstructionCard(item: InstructionItem) {
    val isDark = isSystemInDarkTheme()
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val timeStr = sdf.format(Date(item.sentAt))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, if (item.seen) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFF59E0B).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(timeStr, fontSize = 11.sp, color = Color.Gray)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = (if (item.seen) Color(0xFF10B981) else Color(0xFFF59E0B)).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (item.seen) "Seen ✓" else "Unseen ⏳",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.seen) Color(0xFF047857) else Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Text(item.text, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color.White else Color(0xFF1A237E))
        }
    }
}
