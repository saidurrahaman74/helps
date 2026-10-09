package com.srcreation.smarthelp

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity

@Composable
fun AppLockScreen(
    prefManager: PreferenceManager,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    
    val correctPin = prefManager.getAppLock() ?: ""

    Column(
        modifier = Modifier.fillMaxSize().background(AppGradientBrush),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(24.dp))
        Text(stringResource(id = R.string.enter_pin), fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color(0xFF1A237E))
        
        Spacer(Modifier.height(32.dp))
        
        // PIN Dots
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { index ->
                val isFilled = index < enteredPin.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isError) Color.Red else if (isFilled) (if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E)) else (if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray.copy(0.5f)))
                )
            }
        }
        
        Spacer(Modifier.height(48.dp))
        
        // Custom Keypad
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "BIO", "0", "DEL")
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            keys.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    row.forEach { key ->
                        KeyButton(
                            label = key,
                            isDark = isDark,
                            onClick = {
                                when (key) {
                                    "DEL" -> if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                    "BIO" -> {
                                        if (prefManager.isBiometricLockEnabled()) {
                                            val helper = BiometricHelper(context as FragmentActivity)
                                            helper.showBiometricPrompt(onSuccess = { onUnlocked() }, onError = {})
                                        }
                                    }
                                    else -> {
                                        if (enteredPin.length < 4) {
                                            enteredPin += key
                                            if (enteredPin.length == 4) {
                                                if (enteredPin == correctPin) {
                                                    onUnlocked()
                                                } else {
                                                    isError = true
                                                    enteredPin = ""
                                                }
                                            } else {
                                                isError = false
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
        TextButton(onClick = { showForgotDialog = true }) {
            Text(stringResource(id = R.string.forgot_pin), color = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E), fontWeight = FontWeight.Bold)
        }
    }

    if (showForgotDialog) {
        SecurityResetDialog(
            prefManager = prefManager,
            isDark = isDark,
            onDismiss = { showForgotDialog = false },
            onResetSuccess = { onUnlocked() }
        )
    }
}

@Composable
fun KeyButton(label: String, isDark: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(72.dp),
        shape = CircleShape,
        color = if (isDark) Color(0xFF1E293B) else Color.White.copy(alpha = 0.5f),
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.Transparent)
    ) {
        Box(contentAlignment = Alignment.Center) {
            when (label) {
                "DEL" -> Icon(Icons.AutoMirrored.Filled.Backspace, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                "BIO" -> Icon(Icons.Default.Fingerprint, null, tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E))
                else -> Text(label, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color(0xFF1A237E))
            }
        }
    }
}

@Composable
fun SecurityResetDialog(
    prefManager: PreferenceManager,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onResetSuccess: () -> Unit
) {
    var answer by remember { mutableStateOf("") }
    val question = prefManager.getSecurityQuestion() ?: stringResource(id = R.string.security_not_set)
    val correctAnswer = prefManager.getSecurityAnswer() ?: ""

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) Color(0xFF0F172A) else Color.White,
        title = { Text(stringResource(id = R.string.reset_pin), fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color.Unspecified) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(question, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(alpha = 0.9f) else Color.Unspecified)
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    placeholder = { Text(stringResource(id = R.string.your_answer)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = if (isDark) Color.White else Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.3f) else Color.LightGray,
                        focusedLabelColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                        unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (answer.trim().equals(correctAnswer, ignoreCase = true)) {
                        prefManager.setAppLock(null) // Clear PIN
                        onResetSuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF1A237E),
                    contentColor = if (isDark) Color.Black else Color.White
                )
            ) {
                Text(stringResource(id = R.string.confirm_reset))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel), color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
