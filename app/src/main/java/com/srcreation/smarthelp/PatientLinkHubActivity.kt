package com.srcreation.smarthelp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.text.SimpleDateFormat
import java.util.*

class PatientLinkHubActivity : ComponentActivity() {

    private val viewModel: PatientLinkHubViewModel by viewModels()

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

        // Parse initial tab
        val tabExtra = intent.getExtraValue("TAB")
        viewModel.parseTabExtra(tabExtra)

        setContent {
            HelpsTheme {
                PatientLinkHubScreen(viewModel = viewModel, onBack = { finish() })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val tabExtra = intent.getExtraValue("TAB")
        viewModel.parseTabExtra(tabExtra)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshBadgeCounts()
    }

    override fun onStop() {
        super.onStop()
        viewModel.detachAllTabListeners()
    }

    private fun Intent.getExtraValue(key: String): Any? {
        if (!hasExtra(key)) return null
        val extras = extras ?: return null
        return extras.get(key)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientLinkHubScreen(
    viewModel: PatientLinkHubViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isBn = remember { Locale.getDefault().language == "bn" }

    val selectedTab by viewModel.selectedTab.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val unreadCount by viewModel.unreadInstructionsCount.collectAsState()

    val tabTitles = remember(isBn) {
        if (isBn) listOf(
            "🔗 লিংক কেয়ারগিভার",
            "👁️ ডেটা পারমিশন",
            "📩 বার্তা/নির্দেশনা"
        ) else listOf(
            "🔗 Link Caregiver",
            "👁️ Permissions",
            "📩 Instructions"
        )
    }

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
                                text = if (isBn) "কেয়ারগিভার কানেক্ট হাব" else "Caregiver Connect Hub",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    actions = {
                        LanguageToggleButton()
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Tab Header Bar
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.selectTab(index)
                            },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                    val badgeCount = when (index) {
                                        1 -> pendingCount
                                        2 -> unreadCount
                                        else -> 0
                                    }
                                    if (badgeCount > 0) {
                                        Spacer(Modifier.width(6.dp))
                                        Badge(
                                            containerColor = Color(0xFFEF4444),
                                            contentColor = Color.White
                                        ) {
                                            Text("$badgeCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Tab Content Body
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    when (selectedTab) {
                        0 -> TabLinkCaregiverContent(viewModel = viewModel, isBn = isBn)
                        1 -> TabWhoCanSeeMyDataContent(viewModel = viewModel, isBn = isBn)
                        2 -> TabReceivedInstructionsContent(viewModel = viewModel, isBn = isBn)
                    }
                }
            }
        }
    }
}

@Composable
fun TabLinkCaregiverContent(viewModel: PatientLinkHubViewModel, isBn: Boolean) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val pairingCode by viewModel.pairingCode.collectAsState()
    val codeGenerated by viewModel.codeGenerated.collectAsState()
    val isLoading by viewModel.isGeneratingCode.collectAsState()

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
                    text = if (isBn) "কেয়ারগিভার পেয়ারিং কোড" else "Caregiver Pairing Code",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = if (isBn)
                        "একটি ৬-সংখ্যার কোড তৈরি করুন এবং আপনার কেয়ারগিভারের সাথে শেয়ার করুন।"
                    else
                        "Generate a 6-digit code and share it with your caregiver to link your accounts.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                if (codeGenerated) {
                    Text(
                        text = pairingCode,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 4.sp
                    )

                    Text(
                        text = if (isBn) "কোড ১০ মিনিটের জন্য কার্যকর" else "Code valid for 10 minutes",
                        fontSize = 12.sp,
                        color = Color(0xFFEF4444)
                    )

                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, if (isBn) "আমার স্মার্ট হেল্প কেয়ারগিভার পেয়ারিং কোড: $pairingCode (১০ মিনিট সচল)" else "My Smart Help Caregiver Pairing Code: $pairingCode (Valid 10 mins)")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Code via"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isBn) "কোড শেয়ার করুন" else "Share Code")
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.generatePairingCode(
                                onSuccess = {
                                    Toast.makeText(context, if (isBn) "কোড তৈরি সফল হয়েছে!" else "Code generated!", Toast.LENGTH_SHORT).show()
                                },
                                onError = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.VpnKey, null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "কোড তৈরি করুন" else "Generate Pairing Code",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabWhoCanSeeMyDataContent(viewModel: PatientLinkHubViewModel, isBn: Boolean) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val firestore = remember { FirebaseFirestore.getInstance() }

    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val activeCaregivers by viewModel.activeCaregivers.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
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
            items(pendingRequests, key = { (it["linkId"] as? String) ?: "" }) { item ->
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
                                                    viewModel.refreshBadgeCounts()
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
                                            viewModel.refreshBadgeCounts()
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
            items(activeCaregivers, key = { (it["linkId"] as? String) ?: "" }) { item ->
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
                                                viewModel.refreshBadgeCounts()
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

@Composable
fun TabReceivedInstructionsContent(viewModel: PatientLinkHubViewModel, isBn: Boolean) {
    val instructions by viewModel.instructions.collectAsState()

    var expandedItem by remember { mutableStateOf<InstructionItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (instructions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.MarkEmailRead, null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = if (isBn) "কোনো নতুন নির্দেশ নেই" else "No Instructions Yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isBn) "আপনার যুক্ত থাকা কেয়ারগিভার কোনো বার্তা বা নির্দেশ পাঠালে তা এখানে দেখা যাবে।" else "Instructions sent by your linked caregiver will appear here.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(instructions, key = { it.id }) { item ->
                    PatientInstructionCard(
                        item = item,
                        onOpen = {
                            expandedItem = item
                            if (!item.seen) {
                                viewModel.markInstructionAsSeen(item.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PatientInstructionCard(item: InstructionItem, onOpen: () -> Unit) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val sdf = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val formattedTime = if (item.sentAt > 0L) sdf.format(Date(item.sentAt)) else "N/A"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (item.seen) Icons.Default.MarkEmailRead else Icons.Default.MarkEmailUnread,
                        contentDescription = null,
                        tint = if (item.seen) Color(0xFF10B981) else Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (item.seen) "Read" else "New Instruction",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.seen) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                }

                Text(formattedTime, fontSize = 11.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = item.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color(0xFF1A237E),
                lineHeight = 22.sp
            )
        }
    }
}
