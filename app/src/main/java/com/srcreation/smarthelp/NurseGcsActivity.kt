package com.srcreation.smarthelp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme

class NurseGcsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                GcsCalculatorScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GcsCalculatorScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    var eyeScore by remember { mutableIntStateOf(4) }
    var verbalScore by remember { mutableIntStateOf(5) }
    var motorScore by remember { mutableIntStateOf(6) }

    val totalScore = eyeScore + verbalScore + motorScore

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.tool_gcs_calc),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDark) Color.White else Color(0xFF1A237E)
                        )
                    },
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
        bottomBar = { AppBottomBar(context = context, currentTab = -1, backgroundBrush = AppGradientBrush) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Result Header
                    GcsResultCard(totalScore)

                    // Eye Opening
                    GcsSelectionSection(
                        title = stringResource(id = R.string.gcs_eye_opening),
                        options = listOf(
                            stringResource(id = R.string.gcs_spontaneous) to 4,
                            stringResource(id = R.string.gcs_to_speech) to 3,
                            stringResource(id = R.string.gcs_to_pain) to 2,
                            stringResource(id = R.string.gcs_none) to 1
                        ),
                        selectedScore = eyeScore
                    ) { eyeScore = it }

                    // Verbal Response
                    GcsSelectionSection(
                        title = stringResource(id = R.string.gcs_verbal_response),
                        options = listOf(
                            stringResource(id = R.string.gcs_oriented) to 5,
                            stringResource(id = R.string.gcs_confused) to 4,
                            stringResource(id = R.string.gcs_inappropriate) to 3,
                            stringResource(id = R.string.gcs_incomprehensible) to 2,
                            stringResource(id = R.string.gcs_none) to 1
                        ),
                        selectedScore = verbalScore
                    ) { verbalScore = it }

                    // Motor Response
                    GcsSelectionSection(
                        title = stringResource(id = R.string.gcs_motor_response),
                        options = listOf(
                            stringResource(id = R.string.gcs_obeys_commands) to 6,
                            stringResource(id = R.string.gcs_localizes_pain) to 5,
                            stringResource(id = R.string.gcs_withdraws) to 4,
                            stringResource(id = R.string.gcs_flexion) to 3,
                            stringResource(id = R.string.gcs_extension) to 2,
                            stringResource(id = R.string.gcs_none) to 1
                        ),
                        selectedScore = motorScore
                    ) { motorScore = it }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun GcsResultCard(score: Int) {
    val isDark = isSystemInDarkTheme()
    val status = when {
        score >= 13 -> stringResource(id = R.string.gcs_mild)
        score >= 9 -> stringResource(id = R.string.gcs_moderate)
        else -> stringResource(id = R.string.gcs_severe)
    }
    val color = when {
        score >= 13 -> Color(0xFF22C55E)
        score >= 9 -> Color(0xFFFF9800)
        else -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E293B) else color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, if (isDark) color else color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(id = R.string.gcs_total_score), fontSize = 14.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Bold)
            Text("$score / 15", fontSize = 48.sp, fontWeight = FontWeight.Black, color = color)
            Surface(
                color = color,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun GcsSelectionSection(
    title: String,
    options: List<Pair<String, Int>>,
    selectedScore: Int,
    onSelect: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    Column {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else Color(0xFF1A237E),
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (label, score) ->
                val isSelected = selectedScore == score
                val surfaceBg = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.6f))
                val labelColor = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.9f) else Color.Black)
                val scoreColor = if (isSelected) (if (isDark) Color.Black.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f)) else (if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                val borderColor = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f))

                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelect(score)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = surfaceBg,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            color = labelColor,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        Text(
                            text = "+$score",
                            color = scoreColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
