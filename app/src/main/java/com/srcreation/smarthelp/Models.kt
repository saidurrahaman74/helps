package com.srcreation.smarthelp

import android.net.Uri
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import java.util.*

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White.copy(alpha = 0.15f),
    borderColor: Color = Color.White.copy(alpha = 0.3f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.shadow(0.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(content = content)
    }
}

@Composable
fun QuickModeSwitcher(currentMode: String, onModeClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ModeIcon({ PatientIconDrawing(Color(0xFFEF4444)) }, "Patient", currentMode == "patient", { onModeClick("patient") })
        ModeIcon({ NurseIconDrawing(Color(0xFF0284C7)) }, "Nurse", currentMode == "nurse", { onModeClick("nurse") })
        ModeIcon({ WomenCareIconDrawing(Color(0xFFEC4899)) }, "Women", currentMode == "women", { onModeClick("women") })
    }
}

@Composable
fun ModeIcon(icon: @Composable () -> Unit, label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = modifier.size(60.dp),
            shape = RoundedCornerShape(18.dp),
            color = if (isSelected) (if (isDark) Color(0xFF0F172A) else Color.White) else (if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.3f)),
            border = if (isSelected) BorderStroke(2.dp, if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else null
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                icon()
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) (if (isDark) Color.White else Color.Black) else (if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray))
    }
}

@Composable
fun ScreenEntranceAnimation(content: @Composable () -> Unit) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible.value = true }
    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 10 }
    ) {
        content()
    }
}

@Composable
fun AppLogo(modifier: Modifier = Modifier, size: Dp = 64.dp, iconSize: Dp = 40.dp, cornerRadius: Dp = 20.dp, showBackground: Boolean = true) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showBackground) Modifier
                    .background(
                        brush = Brush.linearGradient(colors = listOf(Color(0xFF1A237E), Color(0xFF3949AB))),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(cornerRadius))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(iconSize)) {
            val w = this.size.width
            val h = this.size.height
            val color = if (showBackground) Color.White else (if (isDark) Color.White else Color(0xFF1A237E))

            // Dynamic Heartbeat Pulse Line
            val path = Path().apply {
                moveTo(0f, h * 0.5f)
                lineTo(w * 0.2f, h * 0.5f)
                lineTo(w * 0.35f, h * 0.2f)
                lineTo(w * 0.5f, h * 0.8f)
                lineTo(w * 0.65f, h * 0.4f)
                lineTo(w * 0.8f, h * 0.5f)
                lineTo(w, h * 0.5f)
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Heart outline (Subtle background of the icon)
            val heartPath = Path().apply {
                moveTo(w * 0.5f, h * 0.9f)
                cubicTo(w * 0.1f, h * 0.7f, w * -0.1f, h * 0.3f, w * 0.5f, h * 0.2f)
                cubicTo(w * 1.1f, h * 0.3f, w * 0.9f, h * 0.7f, w * 0.5f, h * 0.9f)
            }
            drawPath(
                path = heartPath,
                color = color.copy(alpha = 0.15f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@Composable
fun PatientIconDrawing(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawCircle(color, radius = w * 0.3f, center = Offset(w * 0.5f, h * 0.4f))
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.9f)
            quadraticBezierTo(w * 0.5f, h * 0.6f, w * 0.8f, h * 0.9f)
        }
        drawPath(path, color, style = Stroke(w * 0.15f, cap = StrokeCap.Round))
    }
}

@Composable
fun NurseIconDrawing(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Nursing Cap Shape
        val capPath = Path().apply {
            moveTo(w * 0.15f, h * 0.65f)
            lineTo(w * 0.15f, h * 0.4f)
            cubicTo(w * 0.15f, h * 0.2f, w * 0.85f, h * 0.2f, w * 0.85f, h * 0.4f)
            lineTo(w * 0.85f, h * 0.65f)
            close()
        }
        drawPath(capPath, color)
        
        // Small White Cross on Cap
        val crossSize = w * 0.15f
        val cx = w * 0.5f
        val cy = h * 0.42f
        drawLine(Color.White, Offset(cx - crossSize/2, cy), Offset(cx + crossSize/2, cy), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(Color.White, Offset(cx, cy - crossSize/2), Offset(cx, cy + crossSize/2), strokeWidth = 3f, cap = StrokeCap.Round)

        // Pulse line below cap
        val pulsePath = Path().apply {
            moveTo(w * 0.1f, h * 0.8f)
            lineTo(w * 0.3f, h * 0.8f)
            lineTo(w * 0.4f, h * 0.6f)
            lineTo(w * 0.6f, h * 0.95f)
            lineTo(w * 0.7f, h * 0.8f)
            lineTo(w * 0.9f, h * 0.8f)
        }
        drawPath(pulsePath, color, style = Stroke(width = 3f, cap = StrokeCap.Round))
    }
}

@Composable
fun WomenCareIconDrawing(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        
        // Stylized Lotus/Care symbol
        drawCircle(color.copy(0.2f), radius = w * 0.45f)
        
        val heartPath = Path().apply {
            moveTo(w * 0.5f, h * 0.85f)
            cubicTo(w * 0.1f, h * 0.6f, w * 0.1f, h * 0.2f, w * 0.5f, h * 0.3f)
            cubicTo(w * 0.9f, h * 0.2f, w * 0.9f, h * 0.6f, w * 0.5f, h * 0.85f)
        }
        drawPath(heartPath, color)
        
        // Inner detail
        drawCircle(Color.White, radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.45f))
    }
}

@Composable
fun BpMachineIconDrawing(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Monitor Body (Rounded rectangle)
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.7f, h * 0.6f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Screen inside monitor
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.25f, h * 0.25f),
            size = Size(w * 0.5f, h * 0.25f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )

        // Pulse wave line on screen
        val wavePath = Path().apply {
            moveTo(w * 0.3f, h * 0.37f)
            lineTo(w * 0.4f, h * 0.37f)
            lineTo(w * 0.45f, h * 0.28f)
            lineTo(w * 0.5f, h * 0.45f)
            lineTo(w * 0.55f, h * 0.37f)
            lineTo(w * 0.7f, h * 0.37f)
        }
        drawPath(wavePath, color, style = Stroke(width = 2.5f, cap = StrokeCap.Round))

        // Stethoscope chest piece & tube
        val tubePath = Path().apply {
            moveTo(w * 0.5f, h * 0.75f)
            quadraticBezierTo(w * 0.5f, h * 0.9f, w * 0.7f, h * 0.9f)
        }
        drawPath(tubePath, color, style = Stroke(width = 3f, cap = StrokeCap.Round))

        drawCircle(color, radius = w * 0.12f, center = Offset(w * 0.7f, h * 0.85f))
        drawCircle(Color.White, radius = w * 0.05f, center = Offset(w * 0.7f, h * 0.85f))
    }
}

@Composable
fun WalkingPersonIconDrawing(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Head
        drawCircle(color, radius = w * 0.18f, center = Offset(w * 0.5f, h * 0.22f))

        // Body (torso line)
        drawLine(color, start = Offset(w * 0.5f, h * 0.38f), end = Offset(w * 0.45f, h * 0.65f), strokeWidth = 3.5f, cap = StrokeCap.Round)

        // Arms (swinging)
        drawLine(color, start = Offset(w * 0.48f, h * 0.45f), end = Offset(w * 0.25f, h * 0.55f), strokeWidth = 3f, cap = StrokeCap.Round)
        drawLine(color, start = Offset(w * 0.48f, h * 0.45f), end = Offset(w * 0.75f, h * 0.50f), strokeWidth = 3f, cap = StrokeCap.Round)

        // Legs (walking stride)
        drawLine(color, start = Offset(w * 0.45f, h * 0.65f), end = Offset(w * 0.25f, h * 0.88f), strokeWidth = 3.5f, cap = StrokeCap.Round)
        drawLine(color, start = Offset(w * 0.45f, h * 0.65f), end = Offset(w * 0.75f, h * 0.85f), strokeWidth = 3.5f, cap = StrokeCap.Round)
    }
}

@Composable
fun InfoCard(data: InfoCardData, onClick: () -> Unit) {
    val cardColor = remember(data.colorHex) { colorFromHex(data.colorHex) }
    val cardIcon = getIconByName(data.iconName)
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")
    val rotateX by animateFloatAsState(if (isPressed) -12f else 0f, label = "rotateX")
    val rotateY by animateFloatAsState(if (isPressed) 8f else 0f, label = "rotateY")
    val animTranslationZ by animateFloatAsState(if (isPressed) 30f else 0f, label = "z")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationX = rotateX
                rotationY = rotateY
                this.shadowElevation = animTranslationZ
                cameraDistance = 15f * density
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            ),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.5.dp, Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.1f), cardColor.copy(alpha = 0.2f))
        ))
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDark) listOf(cardColor.copy(alpha = 0.15f), Color(0xFF020617).copy(alpha = 0.8f))
                    else listOf(Color.White.copy(alpha = 0.9f), cardColor.copy(alpha = 0.05f))
                )
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = cardColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, cardColor.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (data.id == "bp_01") {
                            Box(modifier = Modifier.size(30.dp)) {
                                BpMachineIconDrawing(cardColor)
                            }
                        } else if (data.id == "step_01") {
                            Box(modifier = Modifier.size(30.dp)) {
                                WalkingPersonIconDrawing(cardColor)
                            }
                        } else {
                            Icon(imageVector = cardIcon, contentDescription = null, tint = cardColor, modifier = Modifier.size(26.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = data.title, fontWeight = FontWeight.Black, fontSize = 13.sp, color = if (isDark) Color.White else Color(0xFF1A237E), textAlign = TextAlign.Center, maxLines = 2, lineHeight = 16.sp, overflow = TextOverflow.Ellipsis)
                Text(text = data.desc, fontSize = 9.sp, color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 11.sp, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

data class SmartInsight(val title: String, val message: String, val icon: ImageVector, val color: Color)

@Composable
fun PremiumIconButton(icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    Surface(
        onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onClick() },
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

data class HealthTip(val title: String, val description: String, val icon: ImageVector, val color: Color)
data class InfoCardData(val id: String, val title: String, val desc: String, val iconName: String, val colorHex: String)
data class DetailItem(val id: String, val categoryId: String, val subCategory: String, val title: String, val content: String, val phone: String, val location: String)

data class HospitalData(
    val id: String = "",
    val name: String = "",
    val banglaName: String = "",
    val type: String = "Hospital",
    val category: String = "",
    val beds: String = "",
    val location: String = "",
    val address: String = "",
    val emergencyHotline: String = "",
    val emergencyMobile: String = "",
    val ambulance: String = "",
    val icuHotline: String = "",
    val infoDesk: String = "",
    val landlines: List<String> = emptyList(),
    val opdItems: List<OpdInfo> = emptyList(),
    val departments: List<String> = emptyList(),
    val facilities: List<String> = emptyList(),
    val floorGuide: List<Pair<String, String>> = emptyList(),
    val doctorSections: List<DoctorSection> = emptyList()
)

data class OpdInfo(val name: String, val level: String, val services: String)
data class DoctorSection(val department: String, val doctors: List<String>)
data class NotificationItem(val title: String, val message: String, val timestamp: Long)

object AppConstants { const val ADMIN_PASSWORD = "SR" }

@Composable
fun InfoDetailGridCard(item: DetailItem, accentColor: Color, icon: ImageVector) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.15f),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                
                if (item.phone.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.phone}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.8f), CircleShape),
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = accentColor, modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            Column {
                val isDark = isSystemInDarkTheme()
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isDark) Color.White else Color(0xFF1A237E),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.content,
                    fontSize = 11.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF1A237E),
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                
                if (item.location.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(item.location)}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            },
                        color = accentColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = accentColor, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Map", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getIconByName(name: String): ImageVector {
    return when (name) {
        "Favorite" -> Icons.Default.Favorite
        "Hospital", "Hospitals", "CMH" -> Icons.Default.LocalHospital
        "School" -> Icons.Default.School
        "Devices" -> Icons.Default.Devices
        "Cloud" -> Icons.Default.Cloud
        "Finance" -> Icons.Default.AccountBalanceWallet
        "Settings" -> Icons.Default.Settings
        "Notifications" -> Icons.Default.Notifications
        "Newspaper" -> Icons.Default.Newspaper
        "Medical" -> Icons.Default.MedicalInformation
        "FirstAid" -> Icons.Default.MedicalServices
        "BMI" -> Icons.Default.Calculate
        "Tools" -> Icons.Default.Build
        "BarChart", "ShowChart" -> Icons.Default.BarChart
        "Diet Chart" -> Icons.Default.Restaurant
        "Government" -> Icons.Default.AccountBalance
        "Military" -> Icons.Default.MilitaryTech
        "Private" -> Icons.Default.Business
        "Specialized" -> Icons.Default.Stars
        "Clinics" -> Icons.Default.LocalPharmacy
        "Diagnostic" -> Icons.Default.Biotech
        "Ambulance" -> Icons.Default.LocalShipping
        "Blood Bank" -> Icons.Default.Bloodtype
        "Nursing Home" -> Icons.Default.Home
        "Dental Care" -> Icons.Default.MedicalServices
        "Emergency", "Emergency Medicine" -> Icons.Default.Emergency
        "Wounds" -> Icons.Default.Healing
        "Burns" -> Icons.Default.Whatshot
        "CPR Guide" -> Icons.Default.MonitorHeart
        "Fractures" -> Icons.Default.PersonalInjury
        "Poisoning" -> Icons.Default.Science
        "Choking" -> Icons.Default.Air
        "Electric Shock" -> Icons.Default.Bolt
        "Snake Bite" -> Icons.Default.BugReport
        "Heat Stroke", "Heat Exhaustion" -> Icons.Default.WbSunny
        "Fainting" -> Icons.Default.Snooze
        "Vaccines" -> Icons.Default.Vaccines
        "LocalDining", "Restaurant" -> Icons.Default.LocalDining
        "MedicalServices" -> Icons.Default.MedicalServices
        "DocumentScanner" -> Icons.Default.DocumentScanner
        "Lightbulb", "TipsAndUpdates" -> Icons.Default.Lightbulb
        "Bloodtype" -> Icons.Default.Bloodtype
        "FamilyRestroom" -> Icons.Default.FamilyRestroom
        "Skin Care" -> Icons.Default.Face
        "Child Health" -> Icons.Default.ChildCare
        "Pregnancy" -> Icons.Default.PregnantWoman
        "Vitamins", "Medication" -> Icons.Default.Medication
        "Yoga" -> Icons.Default.SelfImprovement
        "Ideal Weight" -> Icons.Default.MonitorWeight
        "Calories" -> Icons.Default.FoodBank
        "History" -> Icons.Default.History
        "Weight Goal" -> Icons.Default.Flag
        "Diet Advice" -> Icons.AutoMirrored.Filled.FactCheck
        "Water Intake" -> Icons.Default.WaterDrop
        "Progress Chart" -> Icons.AutoMirrored.Filled.ShowChart
        "Pro Tips" -> Icons.Default.TipsAndUpdates
        "BMI Info" -> Icons.Default.Info
        "Underweight Chart", "Overweight Chart" -> Icons.Default.StackedBarChart
        "Obesity Guide" -> Icons.Default.Analytics
        "Child Growth" -> Icons.Default.ChildFriendly
        "Height-Weight Table" -> Icons.Default.TableChart
        "Body Fat %" -> Icons.Default.Percent
        "BP Tracker" -> Icons.Default.MonitorHeart
        "Heart Rate" -> Icons.Default.Favorite
        "Insulin tracker", "Insulin Calculator" -> Icons.Default.Medication
        "DirectionsWalk", "Step Counter", "Steps" -> Icons.AutoMirrored.Filled.DirectionsWalk
        "Period Tracker" -> Icons.Default.CalendarMonth
        "Reminders" -> Icons.Default.NotificationsActive
        "Air", "Oxygen Level", "SpO2", "SpO2 Tracker" -> Icons.Default.Air
        "Thermostat", "Temperature Tracker", "Temp Tracker" -> Icons.Default.Thermostat
        "Medicine Reminder", "Medicine Rem" -> ImageVector.vectorResource(id = R.drawable.ic_medicine_reminder)
        "Doc Appoint", "Doctor Appointment" -> Icons.Default.EventAvailable
        "Nearby Pharma" -> Icons.Default.AddLocationAlt
        "Vaccine" -> Icons.Default.Vaccines
        "Medical Vault" -> Icons.Default.Inventory
        "Unified Report" -> Icons.AutoMirrored.Filled.Assignment
        "SmartToy", "AI" -> Icons.Default.SmartToy
        "Fluid Calculator" -> Icons.Default.WaterDrop
        else -> Icons.Default.Favorite
    }
}

fun colorFromHex(hex: String): Color {
    return try { Color(hex.toColorInt()) } catch (_: Exception) { Color(0xFF1A237E) }
}

@Composable
fun isAppInDarkTheme(): Boolean {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val prefManager = remember { PreferenceManager(context) }
    return when (prefManager.getThemeMode()) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> systemDark
    }
}

@Composable
fun getAppGradient(): Brush {
    val isDark = isAppInDarkTheme()
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    
    val colors = if (isDark) {
        listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF020617))
    } else {
        when (hour) {
            in 6..11 -> listOf(Color(0xFFFFF7ED), Color(0xFFF0F9FF), Color(0xFFE0F2FE))
            in 12..16 -> listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE), Color(0xFFF1F5F9))
            in 17..20 -> listOf(Color(0xFFFEF2F2), Color(0xFFF5F3FF), Color(0xFFEDE9FE))
            else -> listOf(Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color(0xFFE2E8F0))
        }
    }
    return Brush.verticalGradient(colors = colors)
}

fun getGreetingText(): Int {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> R.string.good_morning
        in 12..16 -> R.string.good_afternoon
        in 17..20 -> R.string.good_evening
        else -> R.string.good_night
    }
}

val AppGradientBrush: Brush @Composable get() = getAppGradient()

@Composable
fun AdvancedTrendChart(
    values: List<Float>,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth().height(250.dp),
    lineColor: Color = Color(0xFFFF5252)
) {
    val haptic = LocalHapticFeedback.current
    var selectedIndex by remember { mutableIntStateOf(-1) }
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(values) {
        animatedProgress.animateTo(1f, tween(1200, easing = EaseInOutQuart))
    }

    val isDark = isSystemInDarkTheme()

    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF0F172A).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.7f)
        ),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(label, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(6.dp), shape = CircleShape, color = Color(0xFF10B981)) {}
                        Text("Safe", fontSize = 9.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        Surface(modifier = Modifier.size(6.dp), shape = CircleShape, color = Color(0xFFF59E0B)) {}
                        Text("Warning", fontSize = 9.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                        Surface(modifier = Modifier.size(6.dp), shape = CircleShape, color = Color(0xFFEF4444)) {}
                        Text("Danger", fontSize = 9.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }
                if (selectedIndex != -1) {
                    Text("${values[selectedIndex].toInt()}", fontWeight = FontWeight.Black, color = lineColor, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val stepX = size.width / (values.size - 1).coerceAtLeast(1)
                        val index = (offset.x / stepX).toInt().coerceIn(0, values.size - 1)
                        if (selectedIndex != index) {
                            selectedIndex = index
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                }) {
                    if (values.isEmpty()) return@Canvas
                    val width = size.width
                    val height = size.height

                    // 🔴 Visual Health Spectrum Bands (Red / Yellow / Green Danger Zones)
                    // 1. Red Danger Zone (Top 30%)
                    drawRect(color = Color(0xFFEF4444).copy(alpha = 0.08f), size = Size(width, height * 0.3f))
                    // 2. Yellow Warning Zone (Middle 30%)
                    drawRect(color = Color(0xFFF59E0B).copy(alpha = 0.08f), topLeft = Offset(0f, height * 0.3f), size = Size(width, height * 0.3f))
                    // 3. Green Optimal Zone (Bottom 40%)
                    drawRect(color = Color(0xFF10B981).copy(alpha = 0.08f), topLeft = Offset(0f, height * 0.6f), size = Size(width, height * 0.4f))

                    // Dashed Danger Threshold Lines
                    drawLine(
                        color = Color(0xFFEF4444).copy(alpha = 0.3f),
                        start = Offset(0f, height * 0.3f),
                        end = Offset(width, height * 0.3f),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                    drawLine(
                        color = Color(0xFFF59E0B).copy(alpha = 0.3f),
                        start = Offset(0f, height * 0.6f),
                        end = Offset(width, height * 0.6f),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )

                    val maxVal = (values.maxOrNull() ?: 100f).coerceAtLeast(1f)
                    val minVal = values.minOrNull() ?: 0f
                    val range = (maxVal - minVal).coerceAtLeast(1f)
                    val stepX = width / (values.size - 1).coerceAtLeast(1)

                    val points = values.mapIndexed { i, v ->
                        Offset(i * stepX, height - (((v - minVal) / range) * height * 0.8f) * animatedProgress.value - (height * 0.1f))
                    }

                    val path = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            val p0 = points[i - 1]; val p1 = points[i]
                            cubicTo(p0.x + (p1.x - p0.x) / 2, p0.y, p0.x + (p1.x - p0.x) / 2, p1.y, p1.x, p1.y)
                        }
                    }

                    drawPath(path, brush = Brush.verticalGradient(listOf(lineColor.copy(0.35f), Color.Transparent)), style = Fill)
                    drawPath(path, color = lineColor, style = Stroke(3.5.dp.toPx(), cap = StrokeCap.Round))
                    
                    points.forEachIndexed { i, p ->
                        val isSelected = i == selectedIndex
                        drawCircle(
                            if (isSelected) Color.White else lineColor,
                            radius = if (isSelected) 8.dp.toPx() else 4.dp.toPx(),
                            center = p
                        )
                        if (isSelected) {
                            drawCircle(lineColor, radius = 8.dp.toPx(), center = p, style = Stroke(3.dp.toPx()))
                            
                            // Interactive Tooltip Line
                            drawLine(
                                color = lineColor.copy(alpha = 0.3f),
                                start = Offset(p.x, 0f),
                                end = Offset(p.x, height),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdvancedLiquidTank(
    progress: Float,
    current: Int,
    target: Int,
    modifier: Modifier = Modifier.size(240.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid")
    
    val waveOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart), label = "wave1"
    )
    val waveOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart), label = "wave2"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "progress"
    )

    Box(
        modifier = modifier
            .shadow(24.dp, CircleShape, spotColor = Color(0xFF0284C7))
            .background(Color.White, CircleShape)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().clip(CircleShape)) {
            val w = size.width
            val h = size.height
            val fillHeight = h * (1f - animatedProgress)
            
            // Background Wave (Lighter)
            val path2 = Path().apply {
                moveTo(-w, fillHeight)
                for (i in -1..2) {
                    val x = i * w + (waveOffset2 * w)
                    cubicTo(
                        x + w / 4f, fillHeight + 15f,
                        x + w * 3 / 4f, fillHeight - 15f,
                        x + w, fillHeight
                    )
                }
                lineTo(w, h); lineTo(-w, h); close()
            }
            drawPath(path2, color = Color(0xFF4FC3F7).copy(alpha = 0.4f))

            // Foreground Wave (Primary)
            val path1 = Path().apply {
                moveTo(-w, fillHeight)
                for (i in -1..2) {
                    val x = i * w + (waveOffset1 * w)
                    cubicTo(
                        x + w / 4f, fillHeight - 20f,
                        x + w * 3 / 4f, fillHeight + 20f,
                        x + w, fillHeight
                    )
                }
                lineTo(w, h); lineTo(-w, h); close()
            }
            drawPath(
                path = path1,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0284C7), Color(0xFF01579B))
                )
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = TextStyle(
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    color = if (progress > 0.5f) Color.White else Color(0xFF1A237E),
                    shadow = Shadow(Color.Black.copy(0.3f), Offset(2f, 2f), 4f)
                )
            )
            Text(
                text = "$current / $target ml",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (progress > 0.6f) Color.White.copy(alpha = 0.8f) else Color.Gray
            )
        }
    }
}

@Composable
fun LiquidWaveAnimation(
    modifier: Modifier = Modifier.size(150.dp),
    progress: Float = 0.5f,
    color: Color = Color(0xFF2196F3)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart), label = "offset"
    )

    Box(modifier = modifier.clip(CircleShape).background(color.copy(0.1f)).border(2.dp, color.copy(0.2f), CircleShape)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width; val height = size.height
            val path = Path()
            val waveHeight = 10.dp.toPx()
            val fillHeight = height * (1f - progress)
            
            path.moveTo(0f, fillHeight)
            for (x in 0..width.toInt()) {
                val phase = waveOffset * 2 * Math.PI
                val y = fillHeight + Math.sin((x / width) * 2 * Math.PI + phase).toFloat() * waveHeight
                path.lineTo(x.toFloat(), y)
            }
            path.lineTo(width, height); path.lineTo(0f, height); path.close()
            drawPath(path, brush = Brush.verticalGradient(listOf(color.copy(0.8f), color.copy(0.4f))))
        }
        Text("${(progress * 100).toInt()}%", modifier = Modifier.align(Alignment.Center), fontWeight = FontWeight.Black, fontSize = 20.sp, color = if (progress > 0.5f) Color.White else color)
    }
}
