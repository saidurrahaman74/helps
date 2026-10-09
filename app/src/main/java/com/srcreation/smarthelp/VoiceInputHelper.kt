package com.srcreation.smarthelp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Locale

/**
 * Voice Input Helper for Smart Help App.
 * Converts spoken Bengali or English speech into text & health values.
 */
object VoiceInputHelper {

    /**
     * Convert Bengali numerals (০-৯) to English digits (0-9)
     */
    fun convertBnToEnDigits(input: String): String {
        return input.replace('০', '0')
            .replace('১', '1')
            .replace('২', '2')
            .replace('৩', '3')
            .replace('৪', '4')
            .replace('৫', '5')
            .replace('৬', '6')
            .replace('৭', '7')
            .replace('৮', '8')
            .replace('৯', '9')
    }

    /**
     * Extracts all integers from spoken speech (e.g. "১২০ বাই ৮০" -> [120, 80])
     */
    fun parseSpokenNumbers(text: String): List<Int> {
        val converted = convertBnToEnDigits(text)
        val regex = Regex("\\d+")
        return regex.findAll(converted).mapNotNull { it.value.toIntOrNull() }.toList()
    }

    /**
     * Extracts first decimal/double number from spoken speech (e.g. "৭.৫" -> 7.5)
     */
    fun parseSpokenDouble(text: String): Double? {
        val converted = convertBnToEnDigits(text).replace(',', '.')
        val regex = Regex("\\d+(\\.\\d+)?")
        val match = regex.find(converted)?.value
        return match?.toDoubleOrNull()
    }
}

/**
 * Reusable Voice Input Microphone Button
 */
@Composable
fun VoiceInputIconButton(
    promptText: String = "কথা বলুন... / Speak now...",
    iconSize: Dp = 20.dp,
    tintColor: Color = MaterialTheme.colorScheme.primary,
    onResult: (spokenText: String, numbers: List<Int>, decimalVal: Double?) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenResults = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenResults?.firstOrNull() ?: ""
            if (spokenText.isNotBlank()) {
                val numbers = VoiceInputHelper.parseSpokenNumbers(spokenText)
                val decimal = VoiceInputHelper.parseSpokenDouble(spokenText)
                onResult(spokenText, numbers, decimal)
                Toast.makeText(context, "🎤 \"$spokenText\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, promptText)
            }
            try {
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Voice input unavailable on this device", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        Surface(
            modifier = Modifier.size(iconSize + 14.dp),
            shape = CircleShape,
            color = tintColor.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, tintColor.copy(alpha = 0.25f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input Assistant",
                    tint = tintColor,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
