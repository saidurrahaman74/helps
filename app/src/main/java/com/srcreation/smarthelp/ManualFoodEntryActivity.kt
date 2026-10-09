package com.srcreation.smarthelp

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.db.DietFoodEntity
import com.srcreation.smarthelp.db.MealEntity
import com.srcreation.smarthelp.db.MealFoodItemEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import com.srcreation.smarthelp.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ManualFoodEntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                ManualFoodEntryScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualFoodEntryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isAppInDarkTheme()
    val isBn = remember { Locale.getDefault().language == "bn" }
    val scope = rememberCoroutineScope()
    val repo = remember { DietRepository(context) }

    var searchQuery by remember { mutableStateOf("") }
    var allFoods by remember { mutableStateOf<List<DietFoodEntity>>(emptyList()) }
    var selectedFood by remember { mutableStateOf<DietFoodEntity?>(null) }
    var quantityStr by remember { mutableStateOf("1") }
    var mealType by remember { mutableStateOf("Lunch") }

    LaunchedEffect(Unit) {
        repo.seedInitialFoodsIfNeeded()
        allFoods = repo.searchFoods("")
    }

    LaunchedEffect(searchQuery) {
        allFoods = repo.searchFoods(searchQuery)
    }

    val qty = quantityStr.toDoubleOrNull() ?: 1.0
    val scale = qty / 1.0
    val calories = (selectedFood?.caloriesPer100g ?: 0.0) * scale * ((selectedFood?.servingSize ?: 100.0) / 100.0)
    val protein = (selectedFood?.proteinPer100g ?: 0.0) * scale * ((selectedFood?.servingSize ?: 100.0) / 100.0)
    val carbs = (selectedFood?.carbsPer100g ?: 0.0) * scale * ((selectedFood?.servingSize ?: 100.0) / 100.0)
    val fat = (selectedFood?.fatPer100g ?: 0.0) * scale * ((selectedFood?.servingSize ?: 100.0) / 100.0)
    val fiber = (selectedFood?.fiberPer100g ?: 0.0) * scale * ((selectedFood?.servingSize ?: 100.0) / 100.0)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, shadowElevation = 0.dp) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isBn) "ম্যানুয়াল খাবার এন্ট্রি" else "Manual Food Entry",
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
                    if (selectedFood == null) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text(if (isBn) "খাবার খুঁজুন (যেমন: ভাত, মাছ, মুরগি)..." else "Search food (e.g. Rice, Fish, Chicken)...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF22C55E)) },
                            singleLine = true
                        )

                        Spacer(Modifier.height(8.dp))

                        allFoods.forEach { food ->
                            Surface(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedFood = food
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = if (isDark) Color(0xFF1E293B) else Color.White,
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(if (isBn) food.nameBangla else food.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                                        Text("${food.category} • ~${food.caloriesPer100g} kcal / 100g", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Icon(Icons.Default.AddCircle, null, tint = Color(0xFF22C55E))
                                }
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.9f),
                            border = BorderStroke(1.2.dp, Color(0xFF22C55E).copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = if (isBn) selectedFood!!.nameBangla else selectedFood!!.name,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = if (isDark) Color.White else Color(0xFF1A237E)
                                )
                                Text("Category: ${selectedFood!!.category}", fontSize = 12.sp, color = Color.Gray)
                                Spacer(Modifier.height(16.dp))

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

                                Spacer(Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = quantityStr,
                                    onValueChange = { quantityStr = it },
                                    label = { Text(if (isBn) "পরিমাণ (${selectedFood!!.servingUnit})" else "Quantity (${selectedFood!!.servingUnit})") },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(14.dp),
                                    singleLine = true
                                )

                                Spacer(Modifier.height(16.dp))

                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF22C55E).copy(alpha = 0.15f)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("~%.0f kcal".format(calories), fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF22C55E))
                                        Spacer(Modifier.height(4.dp))
                                        Text("Protein: %.1fg | Carbs: %.1fg | Fat: %.1fg | Fiber: %.1fg".format(protein, carbs, fat, fiber), fontSize = 11.sp, color = if (isDark) Color.White else Color.DarkGray)
                                    }
                                }

                                Spacer(Modifier.height(20.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { selectedFood = null },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                    ) {
                                        Text(if (isBn) "অন্য খাবার" else "Change Food")
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
                                                    totalCalories = calories,
                                                    totalProtein = protein,
                                                    totalCarbs = carbs,
                                                    totalFat = fat,
                                                    totalFiber = fiber
                                                )
                                                val item = MealFoodItemEntity(
                                                    mealId = 0,
                                                    foodId = selectedFood!!.id,
                                                    foodName = selectedFood!!.name,
                                                    quantity = qty,
                                                    unit = selectedFood!!.servingUnit,
                                                    estimatedCalories = calories,
                                                    protein = protein,
                                                    carbs = carbs,
                                                    fat = fat,
                                                    fiber = fiber,
                                                    confidence = 1.0f
                                                )
                                                repo.saveMealWithItems(meal, listOf(item))
                                                Toast.makeText(context, if (isBn) "খাবার সফলভাবে সেভ হয়েছে! 🟢" else "Food saved successfully! 🟢", Toast.LENGTH_SHORT).show()
                                                onBack()
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                                    ) {
                                        Text(if (isBn) "সেভ করুন" else "Save Meal", fontWeight = FontWeight.Bold)
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
