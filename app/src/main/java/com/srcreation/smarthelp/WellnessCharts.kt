package com.srcreation.smarthelp

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AdvancedLineChart(
    dataPoints: List<Float>,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth().height(250.dp),
    lineColor: Color = Color(0xFF1A237E),
    targetRange: ClosedFloatingPointRange<Float>? = null
) {
    if (dataPoints.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val isDark = isSystemInDarkTheme()
    val actualLineColor = if (isDark && lineColor == Color(0xFF1A237E)) Color(0xFF38BDF8) else lineColor
    
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(dataPoints) {
        animatedProgress.animateTo(1f, tween(1500, easing = FastOutSlowInEasing))
    }

    Card(
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f)),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(label, fontWeight = FontWeight.Black, fontSize = 16.sp, color = if (isDark) Color.White else actualLineColor)
            Spacer(modifier = Modifier.height(20.dp))

            Canvas(
                modifier = modifier
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)
                            val index = (offset.x / stepX).toInt().coerceIn(0, dataPoints.size - 1)
                            selectedIndex = index
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val maxVal = (dataPoints.maxOrNull() ?: 100f) * 1.2f
                val minVal = (dataPoints.minOrNull() ?: 0f) * 0.8f
                val range = (maxVal - minVal).coerceAtLeast(1f)

                // 1. Draw Target Range Background
                targetRange?.let {
                    val topY = height - ((it.endInclusive - minVal) / range) * height
                    val bottomY = height - ((it.start - minVal) / range) * height
                    drawRect(
                        color = actualLineColor.copy(alpha = 0.08f),
                        topLeft = Offset(0f, topY.toFloat()),
                        size = Size(width, (bottomY - topY).toFloat().coerceAtLeast(0f))
                    )
                }

                // 2. Grid Lines
                for (i in 0..3) {
                    val y = height - (i * (height / 3))
                    drawLine(if (isDark) Color.White.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f), Offset(0f, y), Offset(width, y), 1.dp.toPx())
                }

                // 3. Draw the Path (Line)
                val points = dataPoints.mapIndexed { i, valAt ->
                    val x = i * (width / (dataPoints.size - 1).coerceAtLeast(1))
                    val y = height - ((valAt - minVal) / range) * height * animatedProgress.value
                    Offset(x, y)
                }

                if (points.isNotEmpty()) {
                    val strokePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val p0 = points[i - 1]
                            val p1 = points[i]
                            cubicTo(
                                (p0.x + p1.x) / 2, p0.y,
                                (p0.x + p1.x) / 2, p1.y,
                                p1.x, p1.y
                            )
                        }
                    }

                    // Gradient Fill
                    val fillPath = android.graphics.Path(strokePath.asAndroidPath()).asComposePath().apply {
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(actualLineColor.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )

                    drawPath(
                        path = strokePath,
                        color = actualLineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                // 4. Interaction Points
                points.forEachIndexed { index, point ->
                    val isSelected = index == selectedIndex
                    drawCircle(
                        color = if (isSelected) actualLineColor else (if (isDark) Color.White else Color.White),
                        radius = (if (isSelected) 6.dp else 4.dp).toPx(),
                        center = point,
                        style = if (isSelected) Fill else Stroke(2.dp.toPx())
                    )
                    
                    if (isSelected) {
                        val tooltipText = dataPoints[index].toString()
                        val textLayout = textMeasurer.measure(
                            text = AnnotatedString(tooltipText),
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else actualLineColor)
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            color = if (isDark) Color.White else actualLineColor,
                            topLeft = Offset(point.x - (textLayout.size.width / 2), point.y - 60f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HealthActivityBars(
    data: List<Float>,
    modifier: Modifier = Modifier.fillMaxWidth().height(150.dp)
) {
    val barColor = Color(0xFF22C55E)
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        animatedProgress.animateTo(1f, tween(1000, easing = LinearOutSlowInEasing))
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val maxVal = data.maxOrNull() ?: 100f
        val barWidth = (width / data.size.coerceAtLeast(1)) * 0.7f
        val spacing = (width / data.size.coerceAtLeast(1)) * 0.3f

        data.forEachIndexed { i, value ->
            val barHeight = (value / maxVal) * height * animatedProgress.value
            val x = i * (barWidth + spacing) + spacing / 2
            
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(barColor, barColor.copy(alpha = 0.6f))),
                topLeft = Offset(x, height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8.dp.toPx())
            )
        }
    }
}
