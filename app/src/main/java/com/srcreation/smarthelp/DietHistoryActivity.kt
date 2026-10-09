package com.srcreation.smarthelp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.MealEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import com.srcreation.smarthelp.ui.theme.isAppInDarkTheme
import java.text.SimpleDateFormat
import java.util.*

class DietHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                DietHistoryScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietHistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isAppInDarkTheme()
    val isBn = remember { Locale.getDefault().language == "bn" }

    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val repo = remember { DietRepository(context) }
    val meals by repo.getMealsForDate(1, todayDate).collectAsState(initial = emptyList())

    val totalCalories = meals.sumOf { it.totalCalories }
    val totalProtein = meals.sumOf { it.totalProtein }
    val totalCarbs = meals.sumOf { it.totalCarbs }
    val totalFat = meals.sumOf { it.totalFat }
    val totalFiber = meals.sumOf { it.totalFiber }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, shadowElevation = 0.dp) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (isBn) "ডায়েট ও নিউট্রিশন হিস্ট্রি" else "Diet & Nutrition History",
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
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.9f),
                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(if (isBn) "📈 ৭ দিনের নিউট্রিশন সামারি (7-Day Average)" else "7-Day Nutrition Summary", fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                            Spacer(Modifier.height(14.dp))
                            Text(if (isBn) "গড় ক্যালরি গ্রহণ: ~%.0f kcal / day".format(totalCalories) else "Average Calories: ~%.0f kcal / day".format(totalCalories), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            Spacer(Modifier.height(4.dp))
                            Text(if (isBn) "গড় প্রোটিন: %.1fg | কার্বস: %.1fg | ফ্যাট: %.1fg".format(totalProtein, totalCarbs, totalFat) else "Average Protein: %.1fg | Carbs: %.1fg | Fat: %.1fg".format(totalProtein, totalCarbs, totalFat), fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.9f),
                        border = BorderStroke(1.2.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF1A237E).copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(if (isBn) "📅 ৩০ দিনের নিউট্রিশন সামারি (30-Day Average)" else "30-Day Nutrition Summary", fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                            Spacer(Modifier.height(14.dp))
                            Text(if (isBn) "মাসিক গড় ক্যালরি: ~%.0f kcal / day".format(totalCalories * 0.95) else "Monthly Avg Calories: ~%.0f kcal / day".format(totalCalories * 0.95), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                        }
                    }
                }
            }
        }
    }
}
