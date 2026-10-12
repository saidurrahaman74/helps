package com.srcreation.smarthelp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.text.SimpleDateFormat
import java.util.*

class WhoCanSeeMyDataActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                WhoCanSeeMyDataScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhoCanSeeMyDataScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val firestore = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val uid = auth.currentUser?.uid
    val isBn = remember { Locale.getDefault().language == "bn" }

    var pendingRequests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var activeCaregivers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    DisposableEffect(uid) {
        if (uid == null) return@DisposableEffect onDispose {}

        val pendingReg = firestore.collection("links")
            .whereEqualTo("patientId", uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    pendingRequests = snapshot.documents.map { doc ->
                        mapOf(
                            "linkId" to doc.id,
                            "name" to (doc.getString("caregiverName") ?: "Caregiver"),
                            "createdAt" to (doc.getLong("createdAt") ?: System.currentTimeMillis())
                        )
                    }
                }
            }

        val activeReg = firestore.collection("links")
            .whereEqualTo("patientId", uid)
            .whereEqualTo("status", "active")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    activeCaregivers = snapshot.documents.map { doc ->
                        mapOf(
                            "linkId" to doc.id,
                            "name" to (doc.getString("caregiverName") ?: "Caregiver")
                        )
                    }
                }
            }

        onDispose {
            pendingReg.remove()
            activeReg.remove()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isBn) "কে আমাদের ডেটা দেখতে পারে" else "Who Can See My Data",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    actions = {
                        LanguageToggleButton()
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (pendingRequests.isEmpty() && activeCaregivers.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isBn) "কোনো পেন্ডিং অনুরোধ বা সক্রিয় কেয়ারগিভার নেই।" else "No pending requests or active caregivers.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                if (pendingRequests.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isBn) "পেন্ডিং অনুরোধসমূহ" else "Pending Requests",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(pendingRequests) { item ->
                        val linkId = item["linkId"] as? String ?: ""
                        val name = item["name"] as? String ?: "Caregiver"
                        val createdAt = item["createdAt"] as? Long ?: 0L
                        val timeStr = if (createdAt > 0) SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(createdAt)) else ""

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text(text = if (isBn) "অনুরোধ করা হয়েছে: $timeStr" else "Requested on: $timeStr", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            AlertDialog.Builder(context)
                                                .setTitle(if (isBn) "অনুরোধ বাতিল করুন" else "Decline Request")
                                                .setMessage(if (isBn) "$name এর অনুরোধ বাতিল করতে চান?" else "Are you sure you want to decline request from $name?")
                                                .setPositiveButton(if (isBn) "বাতিল" else "Decline") { _, _ ->
                                                    firestore.collection("links").document(linkId).delete()
                                                        .addOnSuccessListener {
                                                            Toast.makeText(context, if (isBn) "অনুরোধ বাতিল করা হয়েছে" else "Request declined", Toast.LENGTH_SHORT).show()
                                                        }
                                                }
                                                .setNegativeButton(if (isBn) "ফিরে যান" else "Cancel", null)
                                                .show()
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (isBn) "বাতিল" else "Decline")
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            firestore.collection("links").document(linkId)
                                                .update(mapOf("status" to "active", "approvedAt" to System.currentTimeMillis()))
                                                .addOnSuccessListener {
                                                    Toast.makeText(context, if (isBn) "অনুরোধ গৃহীত হয়েছে! কেয়ারগিভার এখন আপনার ডেটা দেখতে পারবেন।" else "Request accepted! Caregiver can now view your data.", Toast.LENGTH_LONG).show()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(context, "Failed to accept: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                                }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (isBn) "গ্রহণ করুন" else "Accept")
                                    }
                                }
                            }
                        }
                    }
                }

                if (activeCaregivers.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "সক্রিয় কেয়ারগিভারগণ" else "Active Caregivers",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(activeCaregivers) { item ->
                        val linkId = item["linkId"] as? String ?: ""
                        val name = item["name"] as? String ?: "Caregiver"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        AlertDialog.Builder(context)
                                            .setTitle(if (isBn) "অ্যাক্সেস মুছে ফেলুন" else "Remove Access")
                                            .setMessage(if (isBn) "আপনি কি নিশ্চিত যে আপনি $name এর অ্যাক্সেস সরাতে চান?" else "Are you sure you want to remove access for $name?")
                                            .setPositiveButton(if (isBn) "সরান" else "Remove") { _, _ ->
                                                firestore.collection("links").document(linkId).delete()
                                                    .addOnSuccessListener {
                                                        Toast.makeText(context, if (isBn) "অ্যাক্সেস মুছে ফেলা হয়েছে" else "Access removed", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                            .setNegativeButton(if (isBn) "বাতিল" else "Cancel", null)
                                            .show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isBn) "সরান" else "Remove")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
