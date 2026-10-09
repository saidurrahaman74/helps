package com.srcreation.smarthelp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srcreation.smarthelp.ui.theme.HelpsTheme

class WeightChartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val chartType = intent.getStringExtra("CHART_TYPE") ?: "Normal Weight"
        setContent {
            HelpsTheme {
                WeightChartScreen(chartType, onBack = { finish() })
            }
        }
    }
}

data class WeightData(
    val heightFtIn: String,
    val heightCm: Int,
    val weightKg: String,
    val weightLbs: String,
    val status: String,
    val color: Color,
    val statusRes: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightChartScreen(initialType: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    
    var selectedTabIndex by remember {
        mutableIntStateOf(
            when (initialType.trim().lowercase()) {
                "underweight chart", "underweight", "কম ওজন", "ওজন কম", "কম ওজনের চার্ট" -> 0
                "normal weight", "normal", "স্বাভাবিক", "স্বাভাবিক ওজন", "স্বাভাবিক ওজনের চার্ট" -> 1
                "overweight chart", "overweight", "অতিরিক্ত ওজন", "অতিরিক্ত ওজনের চার্ট" -> 2
                "obesity guide", "obese", "স্থূলতা", "স্থূলতা গাইড" -> 3
                else -> 1
            }
        )
    }

    val tabs = listOf(
        stringResource(R.string.tab_underweight),
        stringResource(R.string.tab_normal),
        stringResource(R.string.tab_overweight),
        stringResource(R.string.tab_obese)
    )

    val chartData = getInternationalWeightData(selectedTabIndex)

    Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Transparent,
                    shadowElevation = 0.dp
                ) {
                    Column {
                        CenterAlignedTopAppBar(
                            title = { Text(stringResource(R.string.weight_chart_title), fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
                            navigationIcon = {
                                IconButton(onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onBack()
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = if (isDark) Color.White else Color(0xFF1A237E))
                                }
                            },
                            actions = { LanguageToggleButton() },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        )
                        
                        // Category Tabs (4 Equal Weighted Pill Chips)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tabs.forEachIndexed { index, title ->
                                val isSelected = selectedTabIndex == index
                                val chipBg = if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.35f))
                                val textColor = if (isSelected) (if (isDark) Color.Black else Color.White) else (if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF0F172A))

                                Surface(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedTabIndex = index
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = chipBg,
                                    border = BorderStroke(1.dp, if (isSelected) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.6f))),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .shadow(if (isSelected) 4.dp else 0.dp, RoundedCornerShape(14.dp))
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp)
                                    ) {
                                        Text(
                                            text = title,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = textColor,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = { AppBottomBar(context = context, currentTab = 1, backgroundBrush = AppGradientBrush) }
        ) { innerPadding ->
            ScreenEntranceAnimation {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Category Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color(0xFF1A237E).copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            val bmiRangeStr = when(selectedTabIndex) {
                                0 -> stringResource(R.string.bmi_under_range)
                                1 -> stringResource(R.string.bmi_normal_range)
                                2 -> stringResource(R.string.bmi_overweight_range)
                                else -> stringResource(R.string.bmi_obese_range)
                            }
                            val dotColor = when(selectedTabIndex) {
                                0 -> Color(0xFF0284C7)
                                1 -> Color(0xFF22C55E)
                                2 -> Color(0xFFFF9800)
                                else -> Color(0xFFEF4444)
                            }
                            
                            Box(modifier = Modifier.size(8.dp).background(dotColor, CircleShape))
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(R.string.chart_who_standard, bmiRangeStr), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                        }
                    }

                    // Header Table
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.chart_header_height), modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                        Text(stringResource(R.string.chart_header_weight), modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, textAlign = TextAlign.End)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(chartData) { data ->
                            WeightRow(data, isDark)
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun WeightRow(data: WeightData, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else data.color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(data.heightFtIn, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black, fontSize = 15.sp)
                Text("(${data.heightCm} ${stringResource(R.string.unit_cm)})", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Medium)
            }

            Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.End) {
                Text(data.weightKg, textAlign = TextAlign.End, fontWeight = FontWeight.Black, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), fontSize = 15.sp)
                Text("(${data.weightLbs})", fontSize = 11.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
                
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = data.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = stringResource(data.statusRes),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = data.color
                    )
                }
            }
        }
    }
}

@Composable
fun getInternationalWeightData(tabIndex: Int): List<WeightData> {
    val normalColor = Color(0xFF22C55E)
    val underweightColor = Color(0xFF0284C7)
    val overweightColor = Color(0xFFFF9800)
    val obeseColor = Color(0xFFEF4444)

    return when (tabIndex) {
        0 -> listOf(
            WeightData("4'10\"", 147, stringResource(R.string.chart_weight_below, "41"), stringResource(R.string.chart_lbs_below, "90"), "Underweight", underweightColor, R.string.underweight),
            WeightData("5'0\"", 152, stringResource(R.string.chart_weight_below, "44"), stringResource(R.string.chart_lbs_below, "97"), "Underweight", underweightColor, R.string.underweight),
            WeightData("5'2\"", 157, stringResource(R.string.chart_weight_below, "47"), stringResource(R.string.chart_lbs_below, "104"), "Underweight", underweightColor, R.string.underweight),
            WeightData("5'4\"", 163, stringResource(R.string.chart_weight_below, "50"), stringResource(R.string.chart_lbs_below, "110"), "Underweight", underweightColor, R.string.underweight),
            WeightData("5'6\"", 168, stringResource(R.string.chart_weight_below, "53"), stringResource(R.string.chart_lbs_below, "117"), "Underweight", underweightColor, R.string.underweight),
            WeightData("5'8\"", 173, stringResource(R.string.chart_weight_below, "56"), stringResource(R.string.chart_lbs_below, "123"), "Underweight", underweightColor, R.string.underweight),
            WeightData("5'10\"", 178, stringResource(R.string.chart_weight_below, "60"), stringResource(R.string.chart_lbs_below, "132"), "Underweight", underweightColor, R.string.underweight),
            WeightData("6'0\"", 183, stringResource(R.string.chart_weight_below, "63"), stringResource(R.string.chart_lbs_below, "139"), "Underweight", underweightColor, R.string.underweight)
        )
        1 -> listOf(
            WeightData("4'10\"", 147, stringResource(R.string.chart_weight_range, "41", "52"), stringResource(R.string.chart_lbs_range, "90", "115"), "Normal", normalColor, R.string.normal),
            WeightData("5'0\"", 152, stringResource(R.string.chart_weight_range, "44", "56"), stringResource(R.string.chart_lbs_range, "97", "123"), "Normal", normalColor, R.string.normal),
            WeightData("5'2\"", 157, stringResource(R.string.chart_weight_range, "47", "60"), stringResource(R.string.chart_lbs_range, "104", "132"), "Normal", normalColor, R.string.normal),
            WeightData("5'4\"", 163, stringResource(R.string.chart_weight_range, "50", "64"), stringResource(R.string.chart_lbs_range, "110", "141"), "Normal", normalColor, R.string.normal),
            WeightData("5'6\"", 168, stringResource(R.string.chart_weight_range, "53", "68"), stringResource(R.string.chart_lbs_range, "117", "150"), "Normal", normalColor, R.string.normal),
            WeightData("5'8\"", 173, stringResource(R.string.chart_weight_range, "56", "72"), stringResource(R.string.chart_lbs_range, "123", "159"), "Normal", normalColor, R.string.normal),
            WeightData("5'10\"", 178, stringResource(R.string.chart_weight_range, "60", "77"), stringResource(R.string.chart_lbs_range, "132", "170"), "Normal", normalColor, R.string.normal),
            WeightData("6'0\"", 183, stringResource(R.string.chart_weight_range, "63", "81"), stringResource(R.string.chart_lbs_range, "139", "179"), "Normal", normalColor, R.string.normal)
        )
        2 -> listOf(
            WeightData("4'10\"", 147, stringResource(R.string.chart_weight_above, "52"), stringResource(R.string.chart_lbs_above, "115"), "Overweight", overweightColor, R.string.overweight),
            WeightData("5'0\"", 152, stringResource(R.string.chart_weight_above, "56"), stringResource(R.string.chart_lbs_above, "123"), "Overweight", overweightColor, R.string.overweight),
            WeightData("5'2\"", 157, stringResource(R.string.chart_weight_above, "60"), stringResource(R.string.chart_lbs_above, "132"), "Overweight", overweightColor, R.string.overweight),
            WeightData("5'4\"", 163, stringResource(R.string.chart_weight_above, "64"), stringResource(R.string.chart_lbs_above, "141"), "Overweight", overweightColor, R.string.overweight),
            WeightData("5'6\"", 168, stringResource(R.string.chart_weight_above, "68"), stringResource(R.string.chart_lbs_above, "150"), "Overweight", overweightColor, R.string.overweight),
            WeightData("5'8\"", 173, stringResource(R.string.chart_weight_above, "72"), stringResource(R.string.chart_lbs_above, "159"), "Overweight", overweightColor, R.string.overweight),
            WeightData("5'10\"", 178, stringResource(R.string.chart_weight_above, "77"), stringResource(R.string.chart_lbs_above, "170"), "Overweight", overweightColor, R.string.overweight),
            WeightData("6'0\"", 183, stringResource(R.string.chart_weight_above, "81"), stringResource(R.string.chart_lbs_above, "179"), "Overweight", overweightColor, R.string.overweight)
        )
        else -> listOf(
            WeightData("4'10\"", 147, stringResource(R.string.chart_weight_above, "63"), stringResource(R.string.chart_lbs_above, "138"), "Obese", obeseColor, R.string.obese),
            WeightData("5'0\"", 152, stringResource(R.string.chart_weight_above, "68"), stringResource(R.string.chart_lbs_above, "150"), "Obese", obeseColor, R.string.obese),
            WeightData("5'2\"", 157, stringResource(R.string.chart_weight_above, "73"), stringResource(R.string.chart_lbs_above, "161"), "Obese", obeseColor, R.string.obese),
            WeightData("5'4\"", 163, stringResource(R.string.chart_weight_above, "77"), stringResource(R.string.chart_lbs_above, "170"), "Obese", obeseColor, R.string.obese),
            WeightData("5'6\"", 168, stringResource(R.string.chart_weight_above, "82"), stringResource(R.string.chart_lbs_above, "181"), "Obese", obeseColor, R.string.obese),
            WeightData("5'8\"", 173, stringResource(R.string.chart_weight_above, "87"), stringResource(R.string.chart_lbs_above, "192"), "Obese", obeseColor, R.string.obese),
            WeightData("5'10\"", 178, stringResource(R.string.chart_weight_above, "92"), stringResource(R.string.chart_lbs_above, "203"), "Obese", obeseColor, R.string.obese),
            WeightData("6'0\"", 183, stringResource(R.string.chart_weight_above, "98"), stringResource(R.string.chart_lbs_above, "216"), "Obese", obeseColor, R.string.obese)
        )
    }
}
