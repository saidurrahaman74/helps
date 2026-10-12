package com.srcreation.smarthelp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.srcreation.smarthelp.db.MedicalRecordEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import java.io.File
import java.io.FileOutputStream
import android.view.WindowManager

class MedicalVaultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        ActivityGateHelper.checkGateAndRender(this) {
            setContent {
                HelpsTheme {
                    MedicalVaultScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalVaultScreen(viewModel: MedicalVaultViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRecordImages by remember { mutableStateOf<List<String>?>(null) }
    var playingAudioPath by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Health Vault & Voice Notes", fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E), fontWeight = FontWeight.Black) },
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
        },
        bottomBar = { AppBottomBar(context = context, currentTab = 1, backgroundBrush = AppGradientBrush) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showAddDialog = true
            }, containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), contentColor = if (isDark) Color.Black else Color.White, shape = CircleShape) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Record")
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val records by viewModel.allRecords.collectAsState(initial = null)
            
            if (records == null) {
                VaultSkeleton(isDark)
            } else if (records!!.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudOff, null, modifier = Modifier.size(64.dp), tint = if (isDark) Color.White.copy(alpha = 0.5f) else Color.LightGray)
                        Spacer(Modifier.height(16.dp))
                        Text("No medical records found.", color = if (isDark) Color.White else Color.Gray)
                        Text("Tap + to scan documents & record voice notes.", fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(records!!) { record ->
                        MedicalRecordCard(
                            record = record,
                            isDark = isDark,
                            onView = { selectedRecordImages = record.imageUris.split(",") },
                            onDelete = { viewModel.deleteRecord(record) },
                            onPlayAudio = { audioPath ->
                                playingAudioPath = audioPath
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddRecordDialog(
                isDark = isDark,
                onDismiss = { showAddDialog = false },
                onAdd = { title, type, paths, note ->
                    viewModel.addRecord(title, type, paths, note)
                    showAddDialog = false
                }
            )
        }

        selectedRecordImages?.let { images ->
            ImageViewerDialog(images = images, onDismiss = { selectedRecordImages = null })
        }

        playingAudioPath?.let { audioPath ->
            AudioPlayerDialog(audioPath = audioPath, isDark = isDark, onDismiss = { playingAudioPath = null })
        }
    }
}

@Composable
fun VaultSkeleton(isDark: Boolean) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        items(6) {
            Card(
                modifier = Modifier.fillMaxWidth().height(180.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp).shimmerLoadingAnimation())
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SkeletonItem(width = 100.dp, height = 14.dp)
                        SkeletonItem(width = 60.dp, height = 10.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun MedicalRecordCard(
    record: MedicalRecordEntity,
    isDark: Boolean,
    onView: () -> Unit,
    onDelete: () -> Unit,
    onPlayAudio: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val images = record.imageUris.split(",")
    val hasAudio = record.note.startsWith("[AUDIO:")
    val audioPath = if (hasAudio) {
        record.note.substringAfter("[AUDIO:").substringBefore("]")
    } else null

    Card(
        modifier = Modifier.fillMaxWidth().clickable {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onView()
        },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.4f))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                val firstImage = images.firstOrNull()?.ifBlank { null }
                if (firstImage != null) {
                    AsyncImage(
                        model = firstImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background((if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasAudio) Icons.Default.Mic else Icons.Default.Description,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                if (images.size > 1) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape
                    ) {
                        Text("+${images.size - 1}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Surface(
                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart),
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(record.type, color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete()
                    },
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(if (isDark) Color(0xFF0F172A).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red, modifier = Modifier.size(14.dp))
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(record.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color.White else Color(0xFF1A237E), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(record.date, fontSize = 10.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                
                if (audioPath != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { onPlayAudio(audioPath) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Play Audio", tint = if (isDark) Color.Black else Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ভয়েস নোট শুনুন (Voice Note)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.Black else Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordDialog(isDark: Boolean, onDismiss: () -> Unit, onAdd: (String, String, List<String>, String) -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Prescription") }
    var note by remember { mutableStateOf("") }
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    // Voice Note State
    var isRecording by remember { mutableStateOf(false) }
    var recordedAudioFile by remember { mutableStateOf<File?>(null) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Audio permission is required for voice notes", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        selectedUris = uris
    }

    DisposableEffect(Unit) {
        onDispose {
            recorder?.release()
            recorder = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan / Add Document", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Record Title (e.g., Napa Prescription)") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
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
                
                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.Black)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Prescription", "Lab Report", "Radio & Imaging", "Vaccine Card", "Other").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                type = t
                            },
                            label = { Text(t, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = if (isDark) Color(0xFF1E293B) else Color.Transparent,
                                labelColor = if (isDark) Color.White else Color.Black,
                                selectedContainerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                                selectedLabelColor = if (isDark) Color.Black else Color.White
                            )
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().height(140.dp).background(if (isDark) Color(0xFF1E293B) else Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUris.isNotEmpty()) {
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            selectedUris.forEach { uri ->
                                AsyncImage(model = uri, contentDescription = null, modifier = Modifier.size(140.dp).padding(4.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, tint = if (isDark) Color(0xFF38BDF8) else Color.Gray)
                            Text("Tap to select image(s)", fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                        }
                    }
                }

                // Voice Note Recording Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎙️ ডাক্তারের পরামর্শ ভয়েস রেকর্ড করুন (Voice Note)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (!isRecording && recordedAudioFile == null) {
                            Button(
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        return@Button
                                    }
                                    try {
                                        val file = File(context.filesDir, "audio_note_${System.currentTimeMillis()}.m4a")
                                        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            MediaRecorder(context)
                                        } else {
                                            @Suppress("DEPRECATION")
                                            MediaRecorder()
                                        }.apply {
                                            setAudioSource(MediaRecorder.AudioSource.MIC)
                                            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                            setOutputFile(file.absolutePath)
                                            prepare()
                                            start()
                                        }
                                        recordedAudioFile = file
                                        isRecording = true
                                        Toast.makeText(context, "ভয়েস রেকর্ড শুরু হয়েছে...", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, "রেকর্ড শুরু করা যায়নি", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("রেকর্ড শুরু করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (isRecording) {
                            Button(
                                onClick = {
                                    try {
                                        recorder?.stop()
                                        recorder?.release()
                                        recorder = null
                                        isRecording = false
                                        Toast.makeText(context, "রেকর্ডিং সংরক্ষিত হয়েছে! 🟢", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(context, "স্টপ করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                                        isRecording = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("রেকর্ডিং বন্ধ করুন (Stop)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF22C55E))
                                Text("Voice Note Ready", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                TextButton(onClick = {
                                    recordedAudioFile?.delete()
                                    recordedAudioFile = null
                                }) {
                                    Text("Reset", color = Color.Red, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val uris = selectedUris.map { uri ->
                        saveImageToInternal(context, uri) ?: uri.toString()
                    }
                    val finalNote = if (recordedAudioFile != null) {
                        "[AUDIO:${recordedAudioFile!!.absolutePath}] $note"
                    } else note
                    onAdd(title.ifBlank { "Medical Record" }, type, uris, finalNote)
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
            ) {
                Text(stringResource(id = R.string.save), color = if (isDark) Color.Black else Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel), color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
            }
        },
        containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun AudioPlayerDialog(audioPath: String, isDark: Boolean, onDismiss: () -> Unit) {
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(audioPath) {
        val player = MediaPlayer().apply {
            setDataSource(audioPath)
            prepare()
            setOnCompletionListener { isPlaying = false }
        }
        mediaPlayer = player
        onDispose {
            player.release()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Voice Note Player", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                IconButton(
                    onClick = {
                        mediaPlayer?.let { player ->
                            if (player.isPlaying) {
                                player.pause()
                                isPlaying = false
                            } else {
                                player.start()
                                isPlaying = true
                            }
                        }
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isDark) Color.Black else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন (Close)", color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
            }
        },
        containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ImageViewerDialog(images: List<String>, onDismiss: () -> Unit) {
    val pagerState = rememberPagerState { images.size }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = images[page],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).statusBarsPadding()
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
            
            // Page Indicator
            if (images.size > 1) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape
                ) {
                    Text("${pagerState.currentPage + 1} / ${images.size}", color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                }
            }
        }
    }
}

private fun saveImageToInternal(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "vault_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
