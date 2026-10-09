package com.srcreation.smarthelp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.srcreation.smarthelp.db.MealEntity
import com.srcreation.smarthelp.db.MealFoodItemEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import com.srcreation.smarthelp.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FoodScannerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                FoodScannerScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScannerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isAppInDarkTheme()
    val isBn = remember { Locale.getDefault().language == "bn" }
    val scope = rememberCoroutineScope()
    val repo = remember { DietRepository(context) }
    val foodService = remember { GeminiFoodRecognitionService(context) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var detectedFoods by remember { mutableStateOf<List<DetectedFood>>(emptyList()) }
    var mealType by remember { mutableStateOf("Lunch") }
    var analysisDone by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            selectedImageUri = it
            isLoading = true
            scope.launch {
                detectedFoods = foodService.recognizeFood(context, it, isBn)
                isLoading = false
                analysisDone = true
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { bmp ->
            try {
                val tempFile = File(context.cacheDir, "food_${System.currentTimeMillis()}.jpg")
                FileOutputStream(tempFile).use { out ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                val uri = Uri.fromFile(tempFile)
                selectedImageUri = uri
                isLoading = true
                scope.launch {
                    detectedFoods = foodService.recognizeFood(context, uri, isBn)
                    isLoading = false
                    analysisDone = true
                }
            } catch (_: Throwable) {
                Toast.makeText(context, "Error capturing photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (_: Throwable) {
                Toast.makeText(context, if (isBn) "ক্যামেরা ওপেন করা যায়নি" else "Could not open camera", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, if (isBn) "ক্যামেরা পারমিশন প্রয়োজন" else "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val totalCalories = detectedFoods.sumOf { it.calories }
    val totalProtein = detectedFoods.sumOf { it.protein }
    val totalCarbs = detectedFoods.sumOf { it.carbs }
    val totalFat = detectedFoods.sumOf { it.fat }
    val totalFiber = detectedFoods.sumOf { it.fiber }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, shadowElevation = 0.dp) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isBn) "স্মার্ট ফুড স্ক্যানার" else "Smart Food Scanner",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Color.White else Color(0xFF1A237E)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = if (isDark) Color.White else Color(0xFF1A237E))
                        }
                    },
                    actions = { LanguageToggleButton() },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        bottomBar = { AppBottomBar(context = context, currentTab = -1, isNurseMode = true, backgroundBrush = AppGradientBrush) }
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
                    if (!analysisDone && !isLoading) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.9f),
                            border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color(0xFF22C55E), modifier = Modifier.size(56.dp))
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = if (isBn) "খাবারের ছবি তুলুন বা সিলেক্ট করুন" else "Capture or Select Food Photo",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = if (isDark) Color.White else Color(0xFF1A237E)
                                )
                                Text(
                                    text = if (isBn) "এআই তাৎক্ষণিকভাবে ক্যালরি ও পুষ্টি উপাদান অনুমান করবে" else "AI will instantly estimate calories & nutrients",
                                    fontSize = 12.sp,
                                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(20.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            try {
                                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                                    cameraLauncher.launch(null)
                                                } else {
                                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                                }
                                            } catch (_: Throwable) {
                                                Toast.makeText(context, if (isBn) "ক্যামেরা ওপেন করা যায়নি" else "Could not open camera", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(52.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (isBn) "ক্যামেরা" else "Camera", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        },
                                        modifier = Modifier.weight(1f).height(52.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(if (isBn) "গ্যালারি" else "Gallery", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else if (isLoading) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(250.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.9f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF22C55E))
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    text = if (isBn) "এআই খাবার রিকগনিশন ও পুষ্টি বিশ্লেষণ করছে..." else "AI analyzing food image & nutrition...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isDark) Color.White else Color(0xFF1A237E),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Confirmation Screen
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.9f),
                            border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = if (isBn) "✅ এটি কি সঠিক? (Is this correct?)" else "✅ Is this correct?",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = if (isDark) Color.White else Color(0xFF1A237E)
                                )
                                Text(
                                    text = if (isBn) "খাবার নিশ্চিত করার আগে মিল টাইপ চেক করুন" else "Review detected food and select meal type",
                                    fontSize = 12.sp,
                                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                                )
                                Spacer(Modifier.height(14.dp))

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("Breakfast", "Lunch", "Snack", "Dinner").forEach { m ->
                                        FilterChip(
                                            selected = mealType == m,
                                            onClick = { mealType = m },
                                            label = { Text(m, fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(14.dp))

                                detectedFoods.forEach { food ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(food.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                                                Text("${food.estimatedPortionDescription} • P: %.0fg | C: %.0fg | F: %.0fg".format(food.protein, food.carbs, food.fat), fontSize = 11.sp, color = Color.Gray)
                                            }
                                            Text(
                                                "~%.0f kcal".format(food.calories),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp,
                                                color = Color(0xFFEF4444)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(14.dp))

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF22C55E).copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(if (isBn) "প্রাক্কলিত মোট ক্যালরি:" else "Estimated Total:", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                        Text("~%.0f kcal".format(totalCalories), fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF22C55E))
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                OutlinedButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        context.startActivity(Intent(context, ManualFoodEntryActivity::class.java))
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF22C55E))
                                ) {
                                    Icon(Icons.Default.Add, null, tint = Color(0xFF22C55E))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (isBn) "ম্যানুয়ালি খাবার খুঁজুন ও যোগ করুন" else "Search & Add Food Manually", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                                }

                                Spacer(Modifier.height(12.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            analysisDone = false
                                            selectedImageUri = null
                                        },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                    ) {
                                        Text(if (isBn) "পুনরায় স্ক্যান" else "Retake", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            scope.launch {
                                                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                                                val meal = MealEntity(
                                                    profileId = 1,
                                                    date = dateStr,
                                                    time = timeStr,
                                                    mealType = mealType,
                                                    totalCalories = totalCalories,
                                                    totalProtein = totalProtein,
                                                    totalCarbs = totalCarbs,
                                                    totalFat = totalFat,
                                                    totalFiber = totalFiber,
                                                    imageUri = selectedImageUri?.toString()
                                                )
                                                val mealItems = detectedFoods.map { f ->
                                                    MealFoodItemEntity(
                                                        mealId = 0,
                                                        foodName = f.name,
                                                        quantity = f.estimatedQuantity,
                                                        unit = f.unit,
                                                        estimatedCalories = f.calories,
                                                        protein = f.protein,
                                                        carbs = f.carbs,
                                                        fat = f.fat,
                                                        fiber = f.fiber,
                                                        confidence = f.confidence
                                                    )
                                                }
                                                repo.saveMealWithItems(meal, mealItems)
                                                Toast.makeText(context, if (isBn) "মিল সফলভাবে সেভ হয়েছে! 🟢" else "Meal saved successfully! 🟢", Toast.LENGTH_SHORT).show()
                                                onBack()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                                    ) {
                                        Text(if (isBn) "সংরক্ষণ করুন (Confirm)" else "Confirm & Save", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
