package com.srcreation.smarthelp

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.MealEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import com.srcreation.smarthelp.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SmartDietActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                SmartDietScreen(
                    onBack = { finish() },
                    onManualEntry = { startActivity(Intent(this, ManualFoodEntryActivity::class.java)) },
                    onHistory = { startActivity(Intent(this, DietHistoryActivity::class.java)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartDietScreen(
    onBack: () -> Unit,
    onManualEntry: () -> Unit,
    onHistory: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isAppInDarkTheme()
    val isBn = remember { Locale.getDefault().language == "bn" || AppCompatDelegate.getApplicationLocales().toLanguageTags().contains("bn") }
    val db = remember { AppDatabase.getDatabase(context) }
    val repo = remember { DietRepository(context) }
    val scope = rememberCoroutineScope()
    var showResetDialog by remember { mutableStateOf(false) }

    val mealDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val stepDate = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()) }
    val meals by repo.getMealsForDate(1, mealDate).collectAsState(initial = emptyList())
    val stepRecords by db.stepDao().getAllRecords(1).collectAsState(initial = emptyList())
    val todayRecord = stepRecords.firstOrNull { it.date == stepDate } ?: stepRecords.firstOrNull()
    val todaySteps = todayRecord?.steps ?: 0

    LaunchedEffect(Unit) {
        scope.launch {
            repo.seedInitialFoodsIfNeeded()
        }
    }

    val totalCalories = meals.sumOf { it.totalCalories }
    val totalProtein = meals.sumOf { it.totalProtein }
    val totalCarbs = meals.sumOf { it.totalCarbs }
    val totalFat = meals.sumOf { it.totalFat }
    val totalFiber = meals.sumOf { it.totalFiber }

    val calorieTarget = 2000.0
    val proteinTarget = 90.0
    val carbsTarget = 250.0
    val fatTarget = 65.0
    val fiberTarget = 30.0

    val distanceKm = (todaySteps * 0.00075)
    val activeMinutes = if (todaySteps > 0) (todaySteps / 130) else 0
    val activityCalories = (todaySteps * 0.04).toInt()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, shadowElevation = 0.dp) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isBn) "স্মার্ট ডায়েট ও ক্যালরি ট্র্যাকার" else "Smart Diet & Calorie Tracker",
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
                    actions = {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showResetDialog = true
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset Diet", tint = Color(0xFFEF4444))
                        }
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onHistory()
                        }) {
                            Icon(Icons.Default.History, contentDescription = "History", tint = if (isDark) Color.White else Color(0xFF1A237E))
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
            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text(if (isBn) "আজকের ডায়েট রিসেট করুন" else "Reset Today's Diet", fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color.Black) },
                    text = { Text(if (isBn) "আপনি কি নিশ্চিত যে আজকের সমস্ত লোগ করা খাবার ও ক্যালরি রিসেট করে ০ করতে চান?" else "Are you sure you want to reset today's logged meals and calories to 0?", color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.DarkGray) },
                    confirmButton = {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch {
                                    repo.clearMealsForDate(1, mealDate)
                                    Toast.makeText(context, if (isBn) "🔄 আজকের ডায়েট রিসেট করা হয়েছে!" else "🔄 Today's diet reset to 0!", Toast.LENGTH_SHORT).show()
                                }
                                showResetDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text(if (isBn) "রিসেট (০)" else "Reset to 0", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text(if (isBn) "বাতিল" else "Cancel", color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                        }
                    },
                    containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
                    shape = RoundedCornerShape(24.dp)
                )
            }
            ScreenEntranceAnimation {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onManualEntry()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isBn) "✍️ খাবার যোগ করুন (Add Food)" else "Add Food Manually", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.9f),
                            border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(if (isBn) "🍽️ আজকের পুষ্টি ও ক্যালরি (Today's Nutrition)" else "Today's Nutrition & Calories", fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                                Spacer(Modifier.height(14.dp))

                                NutritionProgressRow(if (isBn) "ক্যালরি (Calories)" else "Calories", totalCalories, calorieTarget, "kcal", Color(0xFFEF4444), isDark)
                                Spacer(Modifier.height(8.dp))
                                NutritionProgressRow(if (isBn) "প্রোটিন (Protein)" else "Protein", totalProtein, proteinTarget, "g", Color(0xFF0284C7), isDark)
                                Spacer(Modifier.height(8.dp))
                                NutritionProgressRow(if (isBn) "কার্বোহাইড্রেট (Carbs)" else "Carbs", totalCarbs, carbsTarget, "g", Color(0xFF22C55E), isDark)
                                Spacer(Modifier.height(8.dp))
                                NutritionProgressRow(if (isBn) "চর্বি (Fat)" else "Fat", totalFat, fatTarget, "g", Color(0xFFFF9800), isDark)
                                Spacer(Modifier.height(8.dp))
                                NutritionProgressRow(if (isBn) "ফাইবার (Fiber)" else "Fiber", totalFiber, fiberTarget, "g", Color(0xFFA855F7), isDark)
                            }
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.9f),
                            border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(if (isBn) "🏃 আজকের শারীরিক কার্যক্রম (Today's Activity)" else "Today's Activity", fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                                Spacer(Modifier.height(14.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    ActivityMetricItem(if (isBn) "স্টেপস" else "Steps", "$todaySteps", Icons.Default.DirectionsWalk, Color(0xFF22C55E), isDark)
                                    ActivityMetricItem(if (isBn) "দূরত্ব" else "Distance", "%.2f km".format(distanceKm), Icons.Default.Straighten, Color(0xFF0284C7), isDark)
                                    ActivityMetricItem(if (isBn) "অ্যাক্টিভ" else "Active", "$activeMinutes min", Icons.Default.Timer, Color(0xFFFF9800), isDark)
                                    ActivityMetricItem(if (isBn) "ক্যালরি বার্ন" else "Burned", "$activityCalories kcal", Icons.Default.LocalFireDepartment, Color(0xFFEF4444), isDark)
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            color = if (isDark) Color(0xFF0F172A) else Color.White,
                            border = BorderStroke(1.5.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.15f)),
                            shadowElevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            modifier = Modifier.size(32.dp),
                                            shape = CircleShape,
                                            color = Color(0xFFEF4444).copy(alpha = 0.15f)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                            }
                                        }
                                        Text(
                                            text = if (isBn) "এনার্জি ড্যাশবোর্ড (Apple Health)" else "Energy Dashboard",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 16.sp,
                                            color = if (isDark) Color.White else Color(0xFF1A237E)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF22C55E).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "LIVE",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF22C55E)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    EnergyRingMetricItem(
                                        title = if (isBn) "খাবার গ্রহণ" else "Intake",
                                        value = "%.0f".format(totalCalories),
                                        unit = "kcal",
                                        color = Color(0xFFEF4444),
                                        isDark = isDark
                                    )
                                    EnergyRingMetricItem(
                                        title = if (isBn) "অ্যাক্টিভিটি বার্ন" else "Burned",
                                        value = "$activityCalories",
                                        unit = "kcal",
                                        color = Color(0xFFFF9800),
                                        isDark = isDark
                                    )
                                    EnergyRingMetricItem(
                                        title = if (isBn) "দৈনিক লক্ষ্য" else "Target",
                                        value = "2000",
                                        unit = "kcal",
                                        color = Color(0xFF38BDF8),
                                        isDark = isDark
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                                Spacer(Modifier.height(12.dp))

                                val netCalories = totalCalories - activityCalories
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (isBn) "নেট ক্যালরি ব্যালেন্স" else "Net Energy Balance",
                                            fontSize = 11.sp,
                                            color = if (isDark) Color.Gray else Color.DarkGray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Target - Intake + Burned",
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Text(
                                        text = "%.0f kcal".format(2000.0 - netCalories),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = if (2000.0 - netCalories >= 0) Color(0xFF22C55E) else Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(if (isBn) "🍛 আজকের খাবার (Today's Meals)" else "Today's Meals", fontWeight = FontWeight.Black, fontSize = 17.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                    }

                    if (meals.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.6f)
                            ) {
                                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text(if (isBn) "আজকে এখনো কোনো খাবার লোগ করা হয়নি" else "No meals logged for today yet", color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(meals) { meal ->
                            MealLogItemCard(meal, isDark, isBn) {
                                scope.launch {
                                    repo.deleteMeal(meal)
                                    Toast.makeText(context, if (isBn) "মিল ডিলিট করা হয়েছে" else "Meal deleted", Toast.LENGTH_SHORT).show()
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
fun NutritionProgressRow(label: String, current: Double, target: Double, unit: String, color: Color, isDark: Boolean) {
    val progress = (current / target).toFloat().coerceIn(0f, 1f)
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF0F172A))
            Text("%.0f / %.0f %s".format(current, target, unit), fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun ActivityMetricItem(label: String, value: String, icon: ImageVector, color: Color, isDark: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = color.copy(alpha = 0.15f)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(value, fontWeight = FontWeight.Black, fontSize = 13.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
        Text(label, fontSize = 10.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MealLogItemCard(meal: MealEntity, isDark: Boolean, isBn: Boolean, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) Color(0xFF1E293B) else Color.White,
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(meal.mealType, fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                Text("${meal.time} • P: %.0fg | C: %.0fg | F: %.0fg".format(meal.totalProtein, meal.totalCarbs, meal.totalFat), fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "~%.0f kcal".format(meal.totalCalories),
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFFEF4444)
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun EnergyRingMetricItem(title: String, value: String, unit: String, color: Color, isDark: Boolean) {
    Column(
        modifier = Modifier
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
        Text(unit, fontSize = 9.sp, color = color.copy(alpha = 0.8f))
    }
}
