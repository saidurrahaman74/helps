package com.srcreation.smarthelp

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.flow.firstOrNull
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.MedicalIdEntity
import com.srcreation.smarthelp.ui.theme.HelpsTheme
import kotlinx.coroutines.flow.first

class MedicalQrActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelpsTheme {
                MedicalQrScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalQrScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val db = AppDatabase.getDatabase(context)
    val pref = PreferenceManager(context)
    val isDark = isSystemInDarkTheme()
    
    var medicalId by remember { mutableStateOf<MedicalIdEntity?>(null) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val activeId = pref.getActiveProfile()
        val data = db.medicalIdDao().getMedicalId(activeId).first()
        val contacts = db.medicalIdDao().getContactList(activeId)
        
        medicalId = data
        data?.let {
            val qrContent = """
                MEDICAL ID
                Name: ${it.fullName}
                Blood: ${it.bloodGroup}
                Allergies: ${it.allergies}
                Emergency: ${contacts.firstOrNull()?.firstOrNull()?.phone ?: "N/A"}
            """.trimIndent()
            qrBitmap = generateQrCode(qrContent)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Emergency QR ID", fontSize = 18.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E)) },
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
        },
        bottomBar = { AppBottomBar(context = context, currentTab = 1, backgroundBrush = AppGradientBrush) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(brush = AppGradientBrush).padding(innerPadding)) {
            ScreenEntranceAnimation {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                    } else if (medicalId == null) {
                        Text("Please complete your Medical ID first.", color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
                    } else {
                        // Premium ID Card Design
                        Card(
                            modifier = Modifier.fillMaxWidth().height(450.dp),
                            shape = RoundedCornerShape(32.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF0F172A) else Color.White),
                            border = BorderStroke(1.5.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF1A237E).copy(alpha = 0.15f)),
                            elevation = CardDefaults.cardElevation(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Header
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AppLogo(size = 40.dp)
                                    Spacer(Modifier.width(12.dp))
                                    Text("SMART HELP MEDICAL ID", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                                }
                                
                                Spacer(Modifier.height(24.dp))
                                
                                // User Info
                                Text(medicalId!!.fullName, fontWeight = FontWeight.Black, fontSize = 22.sp, color = if (isDark) Color.White else Color.Black)
                                Surface(
                                    color = Color(0xFFEF4444),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text(
                                        "BLOOD GROUP: ${medicalId!!.bloodGroup}",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                // QR Code Area
                                Surface(
                                    modifier = Modifier.size(180.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                                ) {
                                    qrBitmap?.let {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = "Medical QR Code",
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.weight(1f))
                                
                                Text(
                                    "SCAN FOR EMERGENCY INFO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Text(
                            "Tip: Save this card or set as Lock Screen wallpaper for emergency rescuers.",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

fun generateQrCode(content: String): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
        }
    }
    return bitmap
}
