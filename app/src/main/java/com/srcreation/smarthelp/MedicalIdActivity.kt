package com.srcreation.smarthelp

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import com.srcreation.smarthelp.db.EmergencyContactEntity
import com.srcreation.smarthelp.db.MedicalIdEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.text.SimpleDateFormat
import java.util.*
import android.view.WindowManager
import java.io.File
import java.io.FileOutputStream

class MedicalIdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        ActivityGateHelper.checkGateAndRender(this) {
            setContent {
                HelpsTheme {
                    MedicalIdScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalIdScreen(viewModel: MedicalIdViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val medicalId by viewModel.medicalId.collectAsState(initial = null)
    val contacts by viewModel.allContacts.collectAsState(initial = emptyList())
    val isDark = isSystemInDarkTheme()
    
    var isEditing by rememberSaveable { mutableStateOf(false) }
    
    var name by rememberSaveable { mutableStateOf("") }
    var age by rememberSaveable { mutableStateOf("") }
    var bloodGroup by rememberSaveable { mutableStateOf("") }
    var allergies by rememberSaveable { mutableStateOf("") }
    var chronic by rememberSaveable { mutableStateOf("") }
    var meds by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var height by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }
    
    var showAddContact by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(medicalId) {
        medicalId?.let {
            name = it.fullName
            bloodGroup = it.bloodGroup
            age = it.age
            allergies = it.allergies
            chronic = it.chronicConditions
            meds = it.currentMedications
            weight = it.weight
            height = it.height
            imageUri = it.imageUri
            isEditing = false
        } ?: run {
            isEditing = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.nav_medical_id), fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) Color.White else Color(0xFF1A237E))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        exportMedicalIdToPdf(context, medicalId, contacts)
                    }) {
                        Icon(Icons.Default.PictureAsPdf, "PDF", tint = if (isDark) Color.White else Color(0xFF1A237E))
                    }
                    if (!isEditing && medicalId != null) {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isEditing = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = if (isDark) Color.White else Color(0xFF1A237E))
                        }
                    }
                    LanguageToggleButton()
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { AppBottomBar(context = context, currentTab = 1, backgroundBrush = AppGradientBrush) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
                    
                    AnimatedContent(
                        targetState = isEditing,
                        label = "mode_switch",
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        }
                    ) { editing ->
                        if (editing) {
                            MedicalIdEditForm(
                                name, { name = it },
                                bloodGroup, { bloodGroup = it },
                                age, { age = it },
                                weight, { weight = it },
                                height, { height = it },
                                allergies, { allergies = it },
                                chronic, { chronic = it },
                                meds, { meds = it },
                                imageUri, { imageUri = it },
                                onSave = {
                                    viewModel.saveMedicalId(name, bloodGroup, age, allergies, chronic, meds, weight, height, imageUri)
                                    isEditing = false
                                }
                            )
                        } else {
                            MedicalIdProfileView(medicalId, contacts, onAddContact = { showAddContact = true }, onDeleteContact = { viewModel.deleteContact(it) }, onDeleteProfile = { showDeleteDialog = true })
                        }
                    }
                    
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
        
        if (showAddContact) {
            AddContactDialog(onDismiss = { showAddContact = false }) { cName, phone, rel ->
                viewModel.addContact(cName, phone, rel)
                showAddContact = false
            }
        }

        if (showDeleteDialog) {
            val isBn = Locale.getDefault().language == "bn"
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
                title = { Text(if (isBn) "মেডিকেল আইডি ডিলিট করুন" else "Delete Medical ID", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Unspecified) },
                text = { Text(if (isBn) "আপনি কি নিশ্চিত যে আপনার সেভ করা মেডিকেল আইডি প্রোফাইল মুছে ফেলতে চান?" else "Are you sure you want to delete your saved Medical ID profile?", color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.Unspecified) },
                confirmButton = {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteMedicalId()
                            showDeleteDialog = false
                            isEditing = true
                            val deletedMsg = if (isBn) "মেডিকেল আইডি মুছে ফেলা হয়েছে!" else "Medical ID profile deleted!"
                            Toast.makeText(context, deletedMsg, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text(if (isBn) "ডিলিট করুন" else "Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(id = R.string.cancel), color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun MedicalIdProfileView(data: MedicalIdEntity?, contacts: List<EmergencyContactEntity>, onAddContact: () -> Unit, onDeleteContact: (EmergencyContactEntity) -> Unit, onDeleteProfile: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .drawBehind {
                        drawCircle(
                            color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)).copy(alpha = 0.15f),
                            radius = size.minDimension / 2 * pulseScale,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.5f),
                    border = BorderStroke(2.dp, if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (data?.imageUri != null) {
                            AsyncImage(
                                model = data.imageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = if (!data?.fullName.isNullOrBlank()) data!!.fullName.first().uppercase() else "H",
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(data?.fullName ?: stringResource(id = R.string.profile_not_setup), color = if (isDark) Color.White else Color(0xFF1A237E), fontSize = 24.sp, fontWeight = FontWeight.Black)
            Surface(
                color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF4CAF50), CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(id = R.string.active_medical_id), color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Vital Stats Bento Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileMetricCard(stringResource(id = R.string.blood_group).uppercase(), data?.bloodGroup ?: "N/A", Icons.Default.Bloodtype, Color(0xFFFF5252), Modifier.weight(1f), isDark)
            ProfileMetricCard(stringResource(id = R.string.weight_kg).uppercase(), data?.weight ?: "--", Icons.Default.MonitorWeight, Color(0xFF2196F3), Modifier.weight(1f), isDark)
            ProfileMetricCard(stringResource(id = R.string.height_cm).uppercase(), data?.height ?: "--", Icons.Default.Straighten, Color(0xFF9C27B0), Modifier.weight(1f), isDark)
        }

        // Clinical Details List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.8f))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ModernDetailRow(Icons.Default.HistoryToggleOff, stringResource(id = R.string.age), data?.age ?: "Not Provided", isDark = isDark)
                HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(0.2f))
                ModernDetailRow(Icons.Default.Warning, stringResource(id = R.string.allergies), data?.allergies ?: "No allergies reported", color = Color(0xFFFF9800), isDark = isDark)
                HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(0.2f))
                ModernDetailRow(Icons.Default.History, stringResource(id = R.string.chronic_conditions), data?.chronicConditions ?: "No chronic conditions", color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), isDark = isDark)
                HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(0.2f))
                ModernDetailRow(Icons.Default.Medication, stringResource(id = R.string.regular_medications), data?.currentMedications ?: "No daily medications", color = Color(0xFF4CAF50), isDark = isDark)
            }
        }

        // Emergency Contacts Section
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(id = R.string.emergency_contacts), fontWeight = FontWeight.Black, fontSize = 20.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAddContact()
                    },
                    modifier = Modifier.background(if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), CircleShape).size(36.dp)
                ) {
                    Icon(Icons.Default.Add, null, tint = if (isDark) Color.Black else Color.White, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            if (contacts.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color.Transparent)
                ) {
                    Text(
                        stringResource(id = R.string.no_contacts_added),
                        modifier = Modifier.padding(20.dp),
                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                contacts.forEach { contact ->
                    EmergencyContactCard(contact, isDark, onDelete = { onDeleteContact(contact) })
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        if (data != null) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDeleteProfile()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (Locale.getDefault().language == "bn") "মেডিকেল আইডি ডিলিট করুন" else "Delete Medical ID",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ProfileMetricCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier, isDark: Boolean) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), fontSize = 16.sp, fontWeight = FontWeight.Black)
            Text(label, color = if (isDark) Color.White.copy(alpha = 0.75f) else Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ModernDetailRow(icon: ImageVector, label: String, value: String, color: Color = Color(0xFF1A237E), isDark: Boolean) {
    val effectiveColor = if (isDark && color == Color(0xFF1A237E)) Color(0xFF38BDF8) else color
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(36.dp), shape = RoundedCornerShape(10.dp), color = effectiveColor.copy(alpha = 0.15f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = effectiveColor, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 10.sp, color = if (isDark) Color.White.copy(alpha = 0.75f) else Color.Black, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 14.sp, color = if (isDark) Color.White else Color.Black, fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalIdEditForm(
    name: String, onNameChange: (String) -> Unit,
    blood: String, onBloodChange: (String) -> Unit,
    age: String, onAgeChange: (String) -> Unit,
    weight: String, onWeightChange: (String) -> Unit,
    height: String, onHeightChange: (String) -> Unit,
    allergies: String, onAllergiesChange: (String) -> Unit,
    chronic: String, onChronicChange: (String) -> Unit,
    meds: String, onMedsChange: (String) -> Unit,
    imageUri: String?, onImageChange: (String?) -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { 
            val permanentPath = ImagePickerHelper.saveImageToInternalStorage(context, it, "patient_medical_id")
            if (permanentPath != null) {
                onImageChange(permanentPath)
                Toast.makeText(context, "ছবি সফলভাবে সংরক্ষণ করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    launcher.launch("image/*") 
                },
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.5f),
                border = BorderStroke(2.dp, if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.AddAPhoto, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(32.dp))
                    }
                    
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            border = BorderStroke(1.5.dp, if (isDark) Color.Black else Color.White)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Edit, null, tint = if (isDark) Color.Black else Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        Text(stringResource(id = R.string.personal_details), fontWeight = FontWeight.Black, fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f)),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = onNameChange,
                    label = { Text(stringResource(id = R.string.full_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) },
                    textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                        focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                        unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )
                )

                Text(stringResource(id = R.string.blood_group), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bloodGroups.forEach { group ->
                        FilterChip(
                            selected = blood == group,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBloodChange(group)
                            },
                            label = { Text(group) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent,
                                labelColor = if (isDark) Color.White else Color.Black,
                                selectedContainerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                selectedLabelColor = if (isDark) Color.Black else Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = age,
                    onValueChange = { if (it.all { char -> char.isDigit() }) onAgeChange(it) },
                    label = { Text(stringResource(id = R.string.age)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Default.HistoryToggleOff, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) },
                    textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                        focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                        unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val weightParts = weight.split(" ")
                    val weightVal = weightParts.firstOrNull() ?: ""
                    val currentWeightUnit = weightParts.getOrNull(1) ?: "kg"

                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = weightVal,
                            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) onWeightChange("$it $currentWeightUnit") },
                            label = { Text("Weight") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                                focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                            )
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            listOf("kg", "lb").forEach { unit ->
                                FilterChip(
                                    selected = currentWeightUnit == unit,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onWeightChange("$weightVal $unit")
                                    },
                                    label = { Text(unit, fontSize = 10.sp) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent,
                                        labelColor = if (isDark) Color.White else Color.Black,
                                        selectedContainerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                        selectedLabelColor = if (isDark) Color.Black else Color.White
                                    )
                                )
                            }
                        }
                    }

                    val heightParts = height.split(" ")
                    val heightVal = heightParts.firstOrNull() ?: ""
                    val currentHeightUnit = heightParts.getOrNull(1) ?: "cm"

                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = heightVal,
                            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) onHeightChange("$it $currentHeightUnit") },
                            label = { Text("Height") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black,
                                focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                                focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                            )
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            listOf("cm", "ft.inch", "in").forEach { unit ->
                                FilterChip(
                                    selected = currentHeightUnit == unit,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onHeightChange("$heightVal $unit")
                                    },
                                    label = { Text(unit, fontSize = 9.sp) },
                                    shape = RoundedCornerShape(6.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent,
                                        labelColor = if (isDark) Color.White else Color.Black,
                                        selectedContainerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                        selectedLabelColor = if (isDark) Color.Black else Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(stringResource(id = R.string.medical_history), fontWeight = FontWeight.Black, fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f)),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = allergies, onValueChange = onAllergiesChange,
                    label = { Text(stringResource(id = R.string.allergies)) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
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
                    value = chronic, onValueChange = onChronicChange,
                    label = { Text(stringResource(id = R.string.chronic_conditions)) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
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
                    value = meds, onValueChange = onMedsChange,
                    label = { Text(stringResource(id = R.string.regular_medications)) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
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

        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSave()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
        ) {
            Icon(Icons.Default.Save, null, tint = if (isDark) Color.Black else Color.White)
            Spacer(modifier = Modifier.width(12.dp))
            Text(stringResource(id = R.string.save).uppercase(), fontWeight = FontWeight.Bold, color = if (isDark) Color.Black else Color.White)
        }
    }
}

@Composable
fun EmergencyContactCard(contact: EmergencyContactEntity, isDark: Boolean, onDelete: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)).copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Phone, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(contact.name, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E), fontSize = 16.sp)
                Text("${contact.relation} • ${contact.phone}", fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.75f) else Color.Black)
            }
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
            }) {
                Icon(Icons.Default.Delete, null, tint = Color.Red.copy(0.6f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AddContactDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var cName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var relation by rememberSaveable { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Emergency Contact", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = cName, onValueChange = { cName = it },
                    label = { Text("Name") },
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
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
                    value = phone, onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
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
                    value = relation, onValueChange = { relation = it },
                    label = { Text("Relation") },
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
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
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if(cName.isNotEmpty() && phone.isNotEmpty()) onAdd(cName, phone, relation)
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
            ) {
                Text(stringResource(id = R.string.save), color = if (isDark) Color.Black else Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDismiss()
            }) {
                Text(stringResource(id = R.string.cancel), color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
            }
        },
        containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

fun exportMedicalIdToPdf(context: Context, data: MedicalIdEntity?, contacts: List<EmergencyContactEntity>) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("Smart Help - Medical ID Profile", 50f, 50f, paint)

    paint.textSize = 12f
    paint.isFakeBoldText = false
    val timestamp = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
    canvas.drawText("Generated on: $timestamp", 50f, 75f, paint)
    canvas.drawLine(50f, 95f, 545f, 95f, paint)
    
    var yPos = 130f
    val lineSpacing = 25f

    paint.isFakeBoldText = true
    canvas.drawText("Personal Details:", 50f, yPos, paint)
    paint.isFakeBoldText = false
    yPos += lineSpacing
    
    canvas.drawText("Full Name: ${data?.fullName ?: "N/A"}", 60f, yPos, paint)
    yPos += lineSpacing
    canvas.drawText("Blood Group: ${data?.bloodGroup ?: "N/A"}", 60f, yPos, paint)
    yPos += lineSpacing
    canvas.drawText("Age: ${data?.age ?: "N/A"}", 60f, yPos, paint)
    yPos += lineSpacing
    canvas.drawText("Weight: ${data?.weight ?: "--"}", 60f, yPos, paint)
    yPos += lineSpacing
    canvas.drawText("Height: ${data?.height ?: "--"}", 60f, yPos, paint)
    yPos += lineSpacing * 1.5f

    paint.isFakeBoldText = true
    canvas.drawText("Medical History:", 50f, yPos, paint)
    paint.isFakeBoldText = false
    yPos += lineSpacing
    canvas.drawText("Allergies: ${data?.allergies ?: "None"}", 60f, yPos, paint)
    yPos += lineSpacing
    canvas.drawText("Chronic Conditions: ${data?.chronicConditions ?: "None"}", 60f, yPos, paint)
    yPos += lineSpacing
    canvas.drawText("Current Medications: ${data?.currentMedications ?: "None"}", 60f, yPos, paint)
    yPos += lineSpacing * 1.5f

    paint.isFakeBoldText = true
    canvas.drawText("Emergency Contacts:", 50f, yPos, paint)
    paint.isFakeBoldText = false
    yPos += lineSpacing
    contacts.forEach { contact ->
        canvas.drawText("• ${contact.name} (${contact.relation}): ${contact.phone}", 60f, yPos, paint)
        yPos += lineSpacing
    }

    pdfDocument.finishPage(page)
    val fileName = "Medical_ID_Report_${System.currentTimeMillis()}.pdf"
    
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            }
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            uri?.let { resolver.openOutputStream(it)?.use { outputStream -> pdfDocument.writeTo(outputStream) } }
            Toast.makeText(context, "PDF saved to Documents", Toast.LENGTH_LONG).show()
        } else {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error generating PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    } finally {
        pdfDocument.close()
    }
}
