package com.srcreation.smarthelp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.util.Locale

class NurseIdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                NurseIdScreen { finish() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NurseIdScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefManager = remember { PreferenceManager(context) }
    val haptic = LocalHapticFeedback.current
    val isBn = remember { Locale.getDefault().language == "bn" }

    var isEditing by rememberSaveable { mutableStateOf(prefManager.getNurseName().isBlank()) }
    var showDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }

    var name by rememberSaveable { mutableStateOf(prefManager.getNurseName()) }
    var hospital by rememberSaveable { mutableStateOf(prefManager.getNurseHospital()) }
    var department by rememberSaveable { mutableStateOf(prefManager.getNurseDepartment()) }
    var designation by rememberSaveable { mutableStateOf(prefManager.getNurseDesignation()) }
    var employeeId by rememberSaveable { mutableStateOf(prefManager.getNurseEmployeeId()) }
    var imageUri by rememberSaveable { mutableStateOf(prefManager.getNurseImageUri()) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { 
            val permanentPath = ImagePickerHelper.saveImageToInternalStorage(context, it, "nurse_id")
            if (permanentPath != null) {
                imageUri = permanentPath
                prefManager.setNurseImageUri(permanentPath)
                val toastText = if (isBn) "প্রোফাইল ছবি আপডেট হয়েছে!" else "Profile photo updated!"
                Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, shadowElevation = 0.dp) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isBn) "ডিজিটাল নার্স আইডি ব্যাজ" else stringResource(id = R.string.nurse_id_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    actions = { 
                        if (!isEditing) {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { showDeleteConfirmDialog = true }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF4444))
                            }
                        }
                        LanguageToggleButton() 
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
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isEditing) {
                        NurseIdEditForm(
                            name = name, onNameChange = { name = it },
                            hospital = hospital, onHospitalChange = { hospital = it },
                            department = department, onDeptChange = { department = it },
                            designation = designation, onDesigChange = { designation = it },
                            employeeId = employeeId, onIdChange = { employeeId = it },
                            imageUri = imageUri,
                            isBn = isBn,
                            onPickImage = { launcher.launch("image/*") },
                            onSave = {
                                prefManager.setNurseId(name, hospital, department, designation, employeeId, imageUri)
                                isEditing = false
                                val savedText = if (isBn) "নার্স আইডি সফলভাবে সেভ হয়েছে!" else "Nurse ID saved successfully!"
                                Toast.makeText(context, savedText, Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        NurseIdCardView(
                            name = name,
                            hospital = hospital,
                            department = department,
                            designation = designation,
                            employeeId = employeeId,
                            imageUri = imageUri,
                            isBn = isBn,
                            onEdit = { isEditing = true },
                            onDelete = { showDeleteConfirmDialog = true }
                        )
                    }
                }
            }
        }

        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text(if (isBn) "নার্স প্রোফাইল ডিলিট করুন" else "Delete Nurse Profile", fontWeight = FontWeight.Bold) },
                text = { Text(if (isBn) "আপনি কি নিশ্চিত যে আপনার সেভ করা নার্স আইডি প্রোফাইল মুছে ফেলতে চান?" else "Are you sure you want to delete/reset your saved Nurse ID profile?") },
                confirmButton = {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            prefManager.setNurseId("", "", "", "", "", null)
                            name = ""; hospital = ""; department = ""; designation = ""; employeeId = ""; imageUri = null
                            isEditing = true
                            showDeleteConfirmDialog = false
                            val deletedText = if (isBn) "নার্স আইডি প্রোফাইল মুছে ফেলা হয়েছে" else "Nurse ID profile cleared"
                            Toast.makeText(context, deletedText, Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text(if (isBn) "মুছে ফেলুন (Delete)" else "Delete Profile")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun NurseIdCardView(
    name: String,
    hospital: String,
    department: String,
    designation: String,
    employeeId: String,
    imageUri: String?,
    isBn: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.85f)
        ),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Official Badge Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isBn) "যাচাইকৃত নার্স আইডি" else "VERIFIED CLINICAL NURSE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.padding(25.dp)) {
                        NurseIconDrawing(MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = name.ifBlank { if (isBn) "নার্সের নাম" else "Nurse Name" },
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = designation.ifBlank { if (isBn) "পদবী (যেমন: সিনিয়র স্টাফ নার্স)" else "Senior Staff Nurse" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0284C7),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                IdInfoRow(Icons.Default.LocalHospital, if (isBn) "হাসপাতাল" else "Hospital", hospital)
                IdInfoRow(Icons.Default.Business, if (isBn) "ডিপার্টমেন্ট" else "Department", department)
                IdInfoRow(Icons.Default.Badge, if (isBn) "রেজিস্ট্রেশন / আই ডি" else "Reg / Staff ID", employeeId)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Digital QR Code verification bar
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.QrCode2, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isBn) "ডিজিটাল শিফট ভেরিফিকেশন আইডি প্রস্তুত" else "Digital Shift Verification Badge Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun IdInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(text = "$label:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(
            text = value.ifBlank { "---" },
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun NurseIdEditForm(
    name: String, onNameChange: (String) -> Unit,
    hospital: String, onHospitalChange: (String) -> Unit,
    department: String, onDeptChange: (String) -> Unit,
    designation: String, onDesigChange: (String) -> Unit,
    employeeId: String, onIdChange: (String) -> Unit,
    imageUri: String?,
    isBn: Boolean,
    onPickImage: () -> Unit,
    onSave: () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Surface(
            onClick = onPickImage,
            modifier = Modifier.size(110.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
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
                    Box(modifier = Modifier.size(60.dp)) {
                        NurseIconDrawing(MaterialTheme.colorScheme.primary)
                    }
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        border = BorderStroke(1.5.dp, Color.White)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.75f)
            ),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF1E293B) else Color.LightGray.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isBn) "নার্স আইডি তথ্য ফর্ম পূরণ করুন" else "Fill Nurse ID Information",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name, onValueChange = onNameChange,
                    label = { Text(if (isBn) "পূর্ণ নাম (Full Name)" else "Full Name") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )
                OutlinedTextField(
                    value = designation, onValueChange = onDesigChange,
                    label = { Text(if (isBn) "পদবী (যেমন: সিনিয়র স্টাফ নার্স)" else "Designation") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )
                OutlinedTextField(
                    value = hospital, onValueChange = onHospitalChange,
                    label = { Text(if (isBn) "হাসপাতাল / প্রতিষ্ঠানের নাম" else "Hospital Name") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )
                OutlinedTextField(
                    value = department, onValueChange = onDeptChange,
                    label = { Text(if (isBn) "ডিপার্টমেন্ট / ওয়ার্ড (যেমন: ICU, OT)" else "Department / Ward") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )
                OutlinedTextField(
                    value = employeeId, onValueChange = onIdChange,
                    label = { Text(if (isBn) "স্টাফ আইডি / বিএনএমসি রেজিস্ট্রেশন নং" else "Employee / Reg ID") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isBn) "প্রোফাইল সেভ করুন" else "Save ID Profile", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
