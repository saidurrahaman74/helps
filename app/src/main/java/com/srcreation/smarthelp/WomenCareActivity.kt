package com.srcreation.smarthelp

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import kotlinx.coroutines.launch
import java.util.Locale

class WomenCareActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                WomenCareScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WomenCareScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isBn = remember { Locale.getDefault().language == "bn" }
    val isDark = isSystemInDarkTheme()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
    
    val pinkBackground = Brush.verticalGradient(
        colors = if (isDark) listOf(Color(0xFF020617), Color(0xFF1E1B4B), Color(0xFF020617))
        else listOf(Color(0xFFFFF1F2), Color(0xFFFFE4E6), Color(0xFFFFF1F2))
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = if (isDark) Color(0xFF0F172A) else Color.White,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Surface(modifier = Modifier.size(64.dp), shape = CircleShape, color = Color(0xFFF43F5E).copy(alpha = 0.15f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PregnantWoman, contentDescription = null, tint = Color(0xFFF43F5E), modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = if (isBn) "উইমেন কেয়ার হাব" else "Women Care Hub", fontWeight = FontWeight.Black, fontSize = 18.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                    
                    // Gyne ID Tag Badge
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color(0xFFF43F5E).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "🏷️ Gyne ID",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF43F5E),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))

                // Gyne ID Profile Button inside Drawer
                NavigationDrawerItem(
                    label = { Text(text = if (isBn) "গাইনী আইডি প্রোফাইল (Gyne ID Profile)" else "Gyne ID Profile", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color.White else Color.Black) },
                    selected = false,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch { drawerState.close() }
                        context.startActivity(Intent(context, GyneProfileActivity::class.java))
                    },
                    icon = { Icon(imageVector = Icons.Default.Badge, contentDescription = "Gyne ID Profile", tint = Color(0xFFF43F5E)) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                QuickModeSwitcher(
                    currentMode = "women",
                    onModeClick = { mode ->
                        scope.launch { drawerState.close() }
                        val intent = when(mode) {
                            "patient" -> Intent(context, HomeActivity::class.java)
                            "nurse" -> Intent(context, NurseHubActivity::class.java)
                            else -> null
                        }
                        intent?.let { context.startActivity(it.apply { flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT }) }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, shadowElevation = 0.dp) {
                    CenterAlignedTopAppBar(
                        title = { 
                            Text(
                                text = stringResource(id = R.string.women_care), 
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black, 
                                color = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                scope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Open Drawer", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                context.startActivity(Intent(context, NotificationActivity::class.java))
                            }) {
                                Icon(Icons.Default.NotificationsNone, "Notifications", tint = MaterialTheme.colorScheme.primary)
                            }
                            LanguageToggleButton()
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            },
            bottomBar = { AppBottomBar(context = context, currentTab = 3, isNurseMode = false, isWomenMode = true, backgroundBrush = pinkBackground) }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().background(pinkBackground).padding(innerPadding)) {
                ScreenEntranceAnimation {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Text(
                                text = stringResource(id = R.string.women_care_tools_title),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Start
                            )
                        }

                        item {
                            InfoCard(
                                data = InfoCardData("period_01", stringResource(id = R.string.period_tracker), stringResource(id = R.string.period_tracker_desc), "Period Tracker", "#EC4899"),
                                onClick = { context.startActivity(Intent(context, PeriodTrackerActivity::class.java)) }
                            )
                        }

                        item {
                            InfoCard(
                                data = InfoCardData("preg_01", stringResource(id = R.string.pregnancy_care), stringResource(id = R.string.pregnancy_care_desc), "Pregnancy", "#F43F5E"),
                                onClick = { context.startActivity(Intent(context, PregnancyActivity::class.java)) }
                            )
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1B4B).copy(alpha = 0.8f) else Color.White.copy(alpha = 0.9f)),
                                border = BorderStroke(1.5.dp, Color(0xFFEC4899).copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Icon(Icons.Default.Favorite, null, tint = Color(0xFFEC4899), modifier = Modifier.size(24.dp))
                                        Text(
                                            text = if (isBn) "🌸 নারী স্বাস্থ্য পরামর্শ ও টিপস" else "🌸 Women's Health & Wellness Tip",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = if (isDark) Color.White else Color(0xFF1A237E)
                                        )
                                    }
                                    Text(
                                        text = if (isBn)
                                            "মাসিকের সময় পর্যাপ্ত আয়রনযুক্ত খাবার (যেমন: পালং শাক, ডালিম, কলিজা) এবং পর্যাপ্ত পানি পান করুন। নিয়মিত হালকা ইয়োগা বা স্ট্রেচিং ক্র্যাম্প কমাতে সাহায্য করে।"
                                        else
                                            "During menstruation, stay hydrated and consume iron-rich foods (spinach, dates, pomegranate). Light stretching or yoga helps reduce menstrual cramps effectively.",
                                            fontSize = 13.sp,
                                            color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.DarkGray,
                                            lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}
