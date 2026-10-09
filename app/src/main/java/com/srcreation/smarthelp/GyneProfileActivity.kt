package com.srcreation.smarthelp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.GyneProfileEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import java.util.*

class GyneProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                GyneProfileScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GyneProfileScreen(viewModel: GyneViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val gyneProfile by viewModel.gyneProfile.collectAsState(initial = null)
    
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    
    var maritalStatus by rememberSaveable { mutableStateOf("") }
    var gravity by rememberSaveable { mutableStateOf("0") }
    var parity by rememberSaveable { mutableStateOf("0") }
    var abortions by rememberSaveable { mutableStateOf("0") }
    var livingChildren by rememberSaveable { mutableStateOf("0") }
    var lmpDate by rememberSaveable { mutableStateOf("") }
    var contraceptive by rememberSaveable { mutableStateOf("") }
    var history by rememberSaveable { mutableStateOf("") }
    var cycleLength by rememberSaveable { mutableStateOf("28") }
    var lastCheckup by rememberSaveable { mutableStateOf("") }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }

    val careIdSaved = stringResource(id = R.string.save)

    LaunchedEffect(gyneProfile) {
        gyneProfile?.let {
            maritalStatus = it.maritalStatus
            gravity = it.gravity
            parity = it.parity
            abortions = it.abortions
            livingChildren = it.livingChildren
            lmpDate = it.lmpDate
            contraceptive = it.contraceptiveHistory
            history = it.gynecologicalHistory
            cycleLength = it.menstrualCycleLength
            lastCheckup = it.lastCheckupDate
            imageUri = it.imageUri
            isEditing = false
        } ?: run {
            isEditing = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.care_id), fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) Color.White else Color(0xFF1A237E))
                    }
                },
                actions = {
                    if (!isEditing && gyneProfile != null) {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isEditing = true
                        }) {
                            Icon(Icons.Default.Edit, "Edit", tint = if (isDark) Color.White else Color(0xFF1A237E))
                        }
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showDeleteDialog = true
                        }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF4444))
                        }
                    }
                    LanguageToggleButton()
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = { AppBottomBar(context = context, currentTab = -1, backgroundBrush = AppGradientBrush) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
                    if (isEditing) {
                        GyneEditForm(
                            maritalStatus, { maritalStatus = it },
                            gravity, { gravity = it },
                            parity, { parity = it },
                            abortions, { abortions = it },
                            livingChildren, { livingChildren = it },
                            lmpDate, { lmpDate = it },
                            contraceptive, { contraceptive = it },
                            history, { history = it },
                            cycleLength, { cycleLength = it },
                            lastCheckup, { lastCheckup = it },
                            imageUri, { imageUri = it },
                            onSave = {
                                viewModel.saveGyneProfile(maritalStatus, gravity, parity, abortions, livingChildren, lmpDate, contraceptive, history, cycleLength, lastCheckup, imageUri)
                                isEditing = false
                                Toast.makeText(context, careIdSaved, Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        GyneView(gyneProfile)
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }
        }

        if (showDeleteDialog) {
            val isBn = Locale.getDefault().language == "bn"
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
                title = { Text(if (isBn) "উইমেন কেয়ার আইডি ডিলিট করুন" else "Delete Women Care ID", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Unspecified) },
                text = { Text(if (isBn) "আপনি কি নিশ্চিত যে আপনার সেভ করা উইমেন কেয়ার / কেয়ার আইডি প্রোফাইল মুছে ফেলতে চান?" else "Are you sure you want to delete your saved Women Care ID profile?", color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.Unspecified) },
                confirmButton = {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.deleteGyneProfile()
                            showDeleteDialog = false
                            isEditing = true
                            val deletedMsg = if (isBn) "উইমেন কেয়ার আইডি মুছে ফেলা হয়েছে!" else "Women Care ID profile deleted!"
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
fun GyneView(data: GyneProfileEntity?) {
    val isDark = isSystemInDarkTheme()
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // ID Card Style
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.8f)),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFFEC4899).copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = Color(0xFFEC4899).copy(alpha = if (isDark) 0.2f else 0.1f),
                        border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.3f))
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
                                Icon(Icons.Default.Female, null, tint = Color(0xFFEC4899), modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(stringResource(id = R.string.obstetric_summary), fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                        Text(stringResource(id = R.string.intl_format), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    GpalItem("G", data?.gravity ?: "0", stringResource(id = R.string.gravida))
                    GpalItem("P", data?.parity ?: "0", stringResource(id = R.string.para))
                    GpalItem("A", data?.abortions ?: "0", stringResource(id = R.string.abortus))
                    GpalItem("L", data?.livingChildren ?: "0", stringResource(id = R.string.living))
                }
            }
        }

        // Details List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DetailRow(Icons.Default.Favorite, stringResource(id = R.string.marital_status), data?.maritalStatus ?: stringResource(id = R.string.status_not_set))
                DetailRow(Icons.Default.CalendarMonth, stringResource(id = R.string.lmp_date), data?.lmpDate ?: stringResource(id = R.string.status_not_set))
                DetailRow(Icons.Default.Loop, stringResource(id = R.string.cycle_length_label), "${data?.menstrualCycleLength ?: "28"} " + stringResource(id = R.string.days_label))
                DetailRow(Icons.Default.Medication, stringResource(id = R.string.contraceptive), data?.contraceptiveHistory ?: stringResource(id = R.string.status_none))
                DetailRow(Icons.Default.History, stringResource(id = R.string.gyne_history), data?.gynecologicalHistory ?: stringResource(id = R.string.status_none))
                DetailRow(Icons.Default.CheckCircle, stringResource(id = R.string.last_checkup), data?.lastCheckupDate ?: stringResource(id = R.string.status_never))
            }
        }
    }
}

@Composable
fun GpalItem(label: String, value: String, full: String) {
    val isDark = isSystemInDarkTheme()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF38BDF8) else Color(0xFFEC4899), fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E), fontSize = 24.sp)
        Text(full, fontSize = 9.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    val isDark = isSystemInDarkTheme()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 14.sp, color = if (isDark) Color.White else Color.Black, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun GyneEditForm(
    marital: String, onMaritalChange: (String) -> Unit,
    gravity: String, onGravityChange: (String) -> Unit,
    parity: String, onParityChange: (String) -> Unit,
    abortions: String, onAbortionChange: (String) -> Unit,
    living: String, onLivingChange: (String) -> Unit,
    lmp: String, onLmpChange: (String) -> Unit,
    contraceptive: String, onContraceptiveChange: (String) -> Unit,
    history: String, onHistoryChange: (String) -> Unit,
    cycle: String, onCycleChange: (String) -> Unit,
    checkup: String, onCheckupChange: (String) -> Unit,
    imageUri: String?, onImageChange: (String?) -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { 
            val permanentPath = ImagePickerHelper.saveImageToInternalStorage(context, it, "care_gyne_id")
            if (permanentPath != null) {
                onImageChange(permanentPath)
                Toast.makeText(context, "ছবি সফলভাবে সংরক্ষণ করা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    fun showDatePicker(onDateSet: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d ->
            onDateSet("$d/${m+1}/$y")
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    val disabledFieldColors = OutlinedTextFieldDefaults.colors(
        disabledTextColor = if (isDark) Color.White else Color.Black,
        disabledBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
        disabledLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
        disabledLeadingIconColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
        disabledContainerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent
    )

    val editFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = if (isDark) Color.White else Color.Black,
        unfocusedTextColor = if (isDark) Color.White else Color.Black,
        focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.5f),
        focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
        unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
        focusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent,
        unfocusedContainerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Avatar Picker
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(
                onClick = { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    launcher.launch("image/*") 
                },
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.5f),
                border = BorderStroke(2.dp, Color(0xFFEC4899))
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
                        Icon(Icons.Default.AddAPhoto, null, tint = Color(0xFFEC4899), modifier = Modifier.size(32.dp))
                    }
                    
                    // Small Edit Overlay
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = Color(0xFFEC4899),
                            border = BorderStroke(1.5.dp, Color.White)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        Text(stringResource(id = R.string.gyne_personal_marital), fontWeight = FontWeight.Black, fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
        OutlinedTextField(value = marital, onValueChange = onMaritalChange, label = { Text(stringResource(id = R.string.marital_status)) }, modifier = Modifier.fillMaxWidth(), colors = editFieldColors)
        
        Text(stringResource(id = R.string.gyne_obstetric_history), fontWeight = FontWeight.Black, fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = gravity, onValueChange = onGravityChange, label = { Text("G") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = editFieldColors)
            OutlinedTextField(value = parity, onValueChange = onParityChange, label = { Text("P") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = editFieldColors)
            OutlinedTextField(value = abortions, onValueChange = onAbortionChange, label = { Text("A") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = editFieldColors)
            OutlinedTextField(value = living, onValueChange = onLivingChange, label = { Text("L") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = editFieldColors)
        }

        Text(stringResource(id = R.string.gyne_menstrual_health), fontWeight = FontWeight.Black, fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
        OutlinedTextField(value = lmp, onValueChange = {}, label = { Text(stringResource(id = R.string.lmp_date_label)) }, modifier = Modifier.fillMaxWidth().clickable {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showDatePicker(onLmpChange)
        }, enabled = false, leadingIcon = { Icon(Icons.Default.CalendarToday, null) }, colors = disabledFieldColors)
        OutlinedTextField(value = cycle, onValueChange = onCycleChange, label = { Text(stringResource(id = R.string.cycle_length_days)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = editFieldColors)
        OutlinedTextField(value = contraceptive, onValueChange = onContraceptiveChange, label = { Text(stringResource(id = R.string.contraceptive_method)) }, modifier = Modifier.fillMaxWidth(), colors = editFieldColors)
        OutlinedTextField(value = history, onValueChange = onHistoryChange, label = { Text(stringResource(id = R.string.gyne_history_input)) }, modifier = Modifier.fillMaxWidth(), minLines = 2, colors = editFieldColors)
        OutlinedTextField(value = checkup, onValueChange = {}, label = { Text(stringResource(id = R.string.last_checkup_date)) }, modifier = Modifier.fillMaxWidth().clickable {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showDatePicker(onCheckupChange)
        }, enabled = false, leadingIcon = { Icon(Icons.Default.CheckCircle, null) }, colors = disabledFieldColors)

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSave()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
        ) {
            Icon(Icons.Default.Save, null, tint = if (isDark) Color.Black else Color.White)
            Spacer(Modifier.width(12.dp))
            Text(stringResource(id = R.string.save_care_id), fontWeight = FontWeight.Bold, color = if (isDark) Color.Black else Color.White)
        }
    }
}
