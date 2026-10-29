package com.srcreation.smarthelp

import java.util.Locale
import com.srcreation.smarthelp.ui.theme.isAppInDarkTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PregnantWoman
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.material.icons.filled.Language

import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween

fun Modifier.shimmerLoadingAnimation(): Modifier = composed {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    this.background(brush)
}

@Composable
fun SkeletonItem(
    modifier: Modifier = Modifier,
    width: Dp = 100.dp,
    height: Dp = 20.dp,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(cornerRadius))
            .shimmerLoadingAnimation()
    )
}

@Composable
fun LanguageToggleButton() {
    val currentLocaleTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val isBangla = if (currentLocaleTags.isNotEmpty()) {
        currentLocaleTags.contains("bn")
    } else {
        Locale.getDefault().language == "bn"
    }
    
    IconButton(
        onClick = {
            val newLocale = if (isBangla) "en" else "bn"
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(newLocale)
            AppCompatDelegate.setApplicationLocales(appLocale)
        },
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .padding(end = 4.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language Switcher",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isBangla) "EN" else "বাংলা",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AppBottomBar(
    context: Context, 
    currentTab: Int, 
    isNurseMode: Boolean = false,
    isWomenMode: Boolean = false,
    backgroundBrush: Brush? = null
) {
    val isDark = isAppInDarkTheme()
    val activeColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0F172A) // Bright Cyan in Dark Mode, Deep Navy in Light Mode
    val inactiveColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF1E293B) // Translucent white in dark mode, charcoal in light mode

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            color = Color.Transparent,
            shape = RoundedCornerShape(28.dp),
            border = null,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home (হোম)
                BottomTabItem(
                    icon = Icons.Default.Home,
                    label = stringResource(id = R.string.nav_home),
                    isSelected = currentTab == 0,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    iconSize = 26.dp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                }

                // 2. Settings (সেটিংস)
                BottomTabItem(
                    icon = Icons.Default.Settings,
                    label = stringResource(id = R.string.nav_settings),
                    isSelected = currentTab == 4,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    iconSize = 26.dp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    context.startActivity(Intent(context, SettingsActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        putExtra("FROM_NURSE", isNurseMode)
                    })
                }
            }
        }
    }
}

@Composable
fun BottomTabItem(
    icon: ImageVector? = null,
    customIcon: @Composable ((Color) -> Unit)? = null,
    label: String,
    subLabel: String? = null,
    isSelected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    iconSize: Dp = 20.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isSelected) 1.15f else 1f, label = "scale")
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.88f, label = "alpha")
    val color = if (isSelected) activeColor else inactiveColor

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 2.dp, horizontal = 2.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(iconSize), contentAlignment = Alignment.Center) {
            if (customIcon != null) {
                customIcon(color)
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            color = color,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center
        )
        if (!subLabel.isNullOrBlank()) {
            Text(
                text = subLabel,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFEC4899),
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
fun WellnessScoreCard(score: Int) {
    val isDark = isAppInDarkTheme()
    val statusColor = when {
        score >= 80 -> Color(0xFF22C55E)
        score >= 50 -> Color(0xFF0284C7)
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) statusColor.copy(alpha = 0.2f) else statusColor.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, if (isDark) statusColor.copy(alpha = 0.4f) else statusColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CircularProgressIndicator(
                    progress = { score.toFloat() / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = statusColor,
                    strokeWidth = 8.dp,
                    trackColor = statusColor.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color.White else Color.Black
                    )
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when {
                            score >= 80 -> "GOLD LEVEL"
                            score >= 50 -> "SILVER LEVEL"
                            else -> "BRONZE LEVEL"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if(score >= 80) "Excellent Overall! 🌟" else if(score >= 50) "Doing Great! 💪" else "Stay Active! ⚡",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color.Black
                )
                Text(
                    text = "Track daily to improve your score",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black
                )
            }
        }
    }
}

@Composable
fun DailyChecklistSection(tasks: List<DailyTask>) {
    val completedCount = tasks.count { it.isCompleted }
    val progress = if (tasks.isNotEmpty()) completedCount.toFloat() / tasks.size else 0f
    val isDark = isAppInDarkTheme()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Daily Checklist",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color.White else Color.Black
                )
                Text(
                    text = "Complete your daily tasks",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black
                )
            }
            Text(
                text = "${(progress * 100).toInt()}% Done",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (progress == 1f) Color(0xFF22C55E) else if (isDark) Color.White else Color.Black
            )
        }

        Spacer(Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = if (progress == 1f) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary,
            trackColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            tasks.forEach { task ->
                val activeColor = if (task.isCompleted) Color(0xFF22C55E) else if (isDark) Color(0xFF4FC3F7) else Color(0xFF1A237E)
                Card(
                    modifier = Modifier.width(130.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) {
                            if (task.isCompleted) activeColor.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                        } else {
                            if (task.isCompleted) activeColor.copy(alpha = 0.08f) else Color.White
                        }
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (task.isCompleted) activeColor.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (task.isCompleted) activeColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = task.icon,
                                contentDescription = null,
                                tint = if (task.isCompleted) activeColor else if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black,
                            maxLines = 2,
                            minLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (task.isCompleted) "Completed" else "Pending",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = if (task.isCompleted) activeColor else if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumLinkHubFab(
    title: String,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier.padding(bottom = 8.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (isDark) listOf(Color(0xFF0284C7), Color(0xFF38BDF8))
                        else listOf(Color(0xFF1A237E), Color(0xFF0284C7))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
