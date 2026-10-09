package com.srcreation.smarthelp

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.srcreation.smarthelp.db.KickLogEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import kotlinx.coroutines.delay
import java.util.*

class PregnancyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                PregnancyScreen(onBack = { finish() })
            }
        }
    }
}

val PregnancyColor = Color(0xFFF43F5E) // Deep Rose/Pink
val TrimesterColor = Color(0xFFFB7185)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PregnancyScreen(viewModel: PregnancyViewModel = viewModel(), onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val isBn = remember { Locale.getDefault().language == "bn" }
    val data by viewModel.automatedPregnancyData.collectAsState(initial = null)
    val kickLogs by viewModel.kickLogs.collectAsState(initial = emptyList())
    
    var showKickCounter by remember { mutableStateOf(false) }
    var showContractionTimer by remember { mutableStateOf(false) }
    var showTrimesterGuide by remember { mutableStateOf(false) }

    val setupGuide = stringResource(id = R.string.pregnancy_setup_guide)
    val movementSaved = stringResource(id = R.string.movement_saved)

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent, shadowElevation = 0.dp) {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(id = R.string.pregnancy_care), fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
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
            }
        },
        bottomBar = { AppBottomBar(context = context, currentTab = -1, isWomenMode = true, backgroundBrush = AppGradientBrush) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (data == null) {
                        item {
                            PregnancySetupCard {
                                Toast.makeText(context, setupGuide, Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        item {
                            PregnancyProgressCard(data!!)
                        }
                        
                        item {
                            WeeklyMilestoneCard(data!!.gestationalWeeks)
                        }

                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ActionToolCard(
                                    title = if (isBn) "কিক কাউন্টার" else "Kick Counter",
                                    icon = Icons.Default.Favorite,
                                    color = PregnancyColor,
                                    modifier = Modifier.weight(1f)
                                ) { showKickCounter = true }
                                
                                ActionToolCard(
                                    title = if (isBn) "কন্ট্রাকশন টাইমার" else "Contraction Timer",
                                    icon = Icons.Default.Timer,
                                    color = Color(0xFF0284C7),
                                    modifier = Modifier.weight(1f)
                                ) { showContractionTimer = true }
                            }
                        }

                        item {
                            ActionToolCard(
                                title = if (isBn) "ট্রাইমেস্টার পুষ্টি ও গাইড" else "Trimester Nutrition & Care Guide",
                                icon = Icons.Default.MedicalServices,
                                color = Color(0xFF22C55E),
                                modifier = Modifier.fillMaxWidth()
                            ) { showTrimesterGuide = true }
                        }
                        
                        item {
                            Text(
                                stringResource(id = R.string.kick_history_title),
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1A237E),
                                fontSize = 16.sp
                            )
                        }
                        
                        items(kickLogs.take(5)) { log ->
                            KickHistoryItem(log)
                        }
                    }
                }
            }
        }
    }

    if (showKickCounter) {
        KickCounterDialog(onDismiss = { showKickCounter = false }) { count, dur ->
            viewModel.addKickLog(count, dur)
            showKickCounter = false
            Toast.makeText(context, movementSaved, Toast.LENGTH_SHORT).show()
        }
    }

    if (showContractionTimer) {
        ContractionTimerDialog(isBn = isBn, isDark = isDark) { showContractionTimer = false }
    }

    if (showTrimesterGuide) {
        TrimesterGuideDialog(isBn = isBn, isDark = isDark) { showTrimesterGuide = false }
    }
}

@Composable
fun PregnancySetupCard(onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth().clickable { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A) else Color.White)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.PregnantWoman, null, tint = PregnancyColor, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(12.dp))
            Text("Set Last Menstrual Period (LMP)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
            Spacer(Modifier.height(8.dp))
            Text("Tap to setup your pregnancy tracking schedule.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun KickHistoryItem(log: KickLogEntity) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else Color.White)
    ) {
        Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.Favorite, null, tint = PregnancyColor, modifier = Modifier.size(20.dp))
                Column {
                    Text("${log.kickCount} Kicks", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDark) Color.White else Color(0xFF0F172A))
                    Text("Duration: ${log.durationMinutes} mins", fontSize = 11.sp, color = Color.Gray)
                }
            }
            Text(log.date, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun KickCounterDialog(onDismiss: () -> Unit, onSave: (Int, Int) -> Unit) {
    var count by remember { mutableIntStateOf(10) }
    var duration by remember { mutableIntStateOf(15) }
    val haptic = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Baby Kick Counter", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Log fetal movement session.", fontSize = 12.sp, color = Color.Gray)
                OutlinedTextField(
                    value = count.toString(),
                    onValueChange = { count = it.toIntOrNull() ?: count },
                    label = { Text("Kick Count") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = duration.toString(),
                    onValueChange = { duration = it.toIntOrNull() ?: duration },
                    label = { Text("Duration (minutes)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onSave(count, duration) },
                colors = ButtonDefaults.buttonColors(containerColor = PregnancyColor)
            ) {
                Text("Save Session", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun PregnancyProgressCard(data: PregnancyData) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(id = R.string.gestational_age), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = "${data.gestationalWeeks}", fontSize = 56.sp, fontWeight = FontWeight.Black, color = PregnancyColor)
                Text(text = " " + stringResource(id = R.string.weeks_label), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PregnancyColor, modifier = Modifier.padding(bottom = 12.dp))
                if (data.gestationalDays > 0) {
                    Text(text = " ${data.gestationalDays}" + stringResource(id = R.string.days_label).take(1), fontSize = 18.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            val progress = (data.gestationalWeeks.toFloat() / 40f).coerceIn(0f, 1f)
            Box(contentAlignment = Alignment.Center) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape),
                    color = PregnancyColor,
                    trackColor = PregnancyColor.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoBadge(stringResource(id = R.string.trimester_label, data.trimester), TrimesterColor)
                InfoBadge(stringResource(id = R.string.days_remaining_label, data.daysRemaining), if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7))
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFFF1F5F9).copy(alpha = 0.5f))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Celebration, null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(id = R.string.edd_display, data.eddDate), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.DarkGray)
            }
        }
    }
}

@Composable
fun InfoBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WeeklyMilestoneCard(weeks: Int) {
    val isDark = isSystemInDarkTheme()
    val fruitRes = when {
        weeks < 5 -> R.string.fruit_seed
        weeks < 9 -> R.string.fruit_blueberry
        weeks < 13 -> R.string.fruit_lemon
        weeks < 17 -> R.string.fruit_avocado
        weeks < 21 -> R.string.fruit_banana
        weeks < 25 -> R.string.fruit_corn
        weeks < 29 -> R.string.fruit_eggplant
        weeks < 33 -> R.string.fruit_pineapple
        weeks < 37 -> R.string.fruit_watermelon
        else -> R.string.fruit_pumpkin
    }
    
    val fruit = stringResource(id = fruitRes)
    val size = when {
        weeks < 5 -> "2 mm"
        weeks < 9 -> "2 cm"
        weeks < 13 -> "5 cm"
        weeks < 17 -> "12 cm"
        weeks < 21 -> "25 cm"
        weeks < 25 -> "30 cm"
        weeks < 29 -> "38 cm"
        weeks < 33 -> "43 cm"
        weeks < 37 -> "48 cm"
        else -> "50 cm"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color(0xFFFFF1F2).copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = if (isDark) Color(0xFF1E293B) else Color.White) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (weeks < 13) "🌱" else "👶", fontSize = 24.sp)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(stringResource(id = R.string.baby_size_title, fruit), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isDark) Color.White else Color(0xFF1A237E))
                Text(stringResource(id = R.string.approx_length_title, size), fontSize = 12.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
            }
        }
    }
}

@Composable
fun ActionToolCard(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = modifier.clickable {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (isDark) Color.White else Color.DarkGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ContractionTimerDialog(isBn: Boolean, isDark: Boolean, onDismiss: () -> Unit) {
    var timerRunning by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(timerRunning) {
        while (timerRunning) {
            delay(1000)
            secondsElapsed++
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBn) "লেবার কন্ট্রাকশন টাইমার" else "Contraction Timer", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isBn) "কন্ট্রাকশন শুরু হলে স্টার্ট চাপুন এবং শেষ হলে স্টপ চাপুন।" else "Tap Start when contraction begins, Stop when it ends.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "${secondsElapsed / 60}m ${secondsElapsed % 60}s",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = PregnancyColor
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); timerRunning = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        enabled = !timerRunning
                    ) {
                        Text(if (isBn) "শুরু (Start)" else "Start")
                    }
                    Button(
                        onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); timerRunning = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        enabled = timerRunning
                    ) {
                        Text(if (isBn) "থামান (Stop)" else "Stop")
                    }
                    OutlinedButton(onClick = { secondsElapsed = 0; timerRunning = false }) {
                        Text(if (isBn) "রিসেট" else "Reset")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(if (isBn) "বন্ধ করুন" else "Close") }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isDark) Color(0xFF0F172A) else Color.White
    )
}

@Composable
fun TrimesterGuideDialog(isBn: Boolean, isDark: Boolean, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isBn) "ট্রাইমেস্টার পুষ্টি ও গাইড" else "Trimester Nutrition Guide", fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E)) },
        text = {
            Column(
                modifier = Modifier.height(300.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isBn) "🌱 প্রথম ট্রাইমেস্টার (১-১৩ সপ্তাহ):" else "🌱 First Trimester (Weeks 1-13):",
                    fontWeight = FontWeight.Bold,
                    color = PregnancyColor,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isBn) "• ফলিক অ্যাসিড ও আয়রন সাপ্লিমেন্ট নিয়মিত নিন।\n• বমি বমি ভাব এড়াতে অল্প অল্প করে বারবার পুষ্টিকর খাবার খান।\n• পর্যাপ্ত পানি ও ডাবের পানি পান করুন।"
                    else "• Take Folic Acid & Iron daily.\n• Eat small, frequent meals to combat morning sickness.\n• Stay well hydrated.",
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Text(
                    text = if (isBn) "🌸 দ্বিতীয় ট্রাইমেস্টার (১৪-২৭ সপ্তাহ):" else "🌸 Second Trimester (Weeks 14-27):",
                    fontWeight = FontWeight.Bold,
                    color = PregnancyColor,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isBn) "• ক্যালসিয়াম ও প্রোটিন সমৃদ্ধ খাবার (ডিম, দুধ, মাছ, মাংস) বাড়ান।\n• নিয়মিত হালকা ওয়াকিং ও পেলভিক এক্সারসাইজ করুন।"
                    else "• Increase calcium & protein intake.\n• Engage in light walking and prenatal yoga.",
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray
                )
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Text(
                    text = if (isBn) "👶 তৃতীয় ট্রাইমেস্টার (২৮-৪০+ সপ্তাহ):" else "👶 Third Trimester (Weeks 28-40+):",
                    fontWeight = FontWeight.Bold,
                    color = PregnancyColor,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isBn) "• ক্যাডেন্স বা কিক কাউন্ট নিয়মিত করুন।\n• হসপিটাল ব্যাগ প্রস্তুত রাখুন এবং লেবার সাইন (Contractions) খেয়াল রাখুন।"
                    else "• Monitor baby kicks daily.\n• Keep hospital bag ready and track labor contractions.",
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.85f) else Color.DarkGray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PregnancyColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isBn) "বুঝেছি (Got It)" else "Got It", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isDark) Color(0xFF0F172A) else Color.White
    )
}
