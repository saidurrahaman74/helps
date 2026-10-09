package com.srcreation.smarthelp.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DeepNightColorScheme = darkColorScheme(
    primary = NightPrimary,
    onPrimary = NightDark,
    primaryContainer = NightBorder,
    onPrimaryContainer = Color.White,
    
    secondary = NightSecondary,
    onSecondary = Color.White,
    
    tertiary = NightAccent,
    onTertiary = Color.White,
    
    background = NightDark,
    onBackground = Color.White,
    
    surface = NightSurface,
    onSurface = Color.White,
    surfaceVariant = NightBorder,
    onSurfaceVariant = Color(0xFF94A3B8),
    
    outline = NightBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A237E),
    onPrimary = Color.White,
    secondary = Color(0xFF0284C7),
    tertiary = Color(0xFFEC4899),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B)
)

@Composable
fun isAppInDarkTheme(): Boolean {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val prefs = remember { context.getSharedPreferences("smart_help_prefs", Context.MODE_PRIVATE) }
    val mode = prefs.getInt("app_theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    return when (mode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> systemDark
    }
}

val AppTitleColor: Color
    @Composable get() = if (isAppInDarkTheme()) Color.White else Color(0xFF1A237E)

val AppSubtextColor: Color
    @Composable get() = if (isAppInDarkTheme()) Color.White.copy(alpha = 0.7f) else Color.Gray

val AppBodyTextColor: Color
    @Composable get() = if (isAppInDarkTheme()) Color.White.copy(alpha = 0.9f) else Color(0xFF1C1B1F)

val AppCardContainerColor: Color
    @Composable get() = if (isAppInDarkTheme()) Color(0xFF0F172A) else Color.White.copy(alpha = 0.85f)

val AppCardBorderColor: Color
    @Composable get() = if (isAppInDarkTheme()) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color(0xFF1A237E).copy(alpha = 0.2f)

val AppIconTint: Color
    @Composable get() = if (isAppInDarkTheme()) Color.White else Color(0xFF1A237E)

@Composable
fun appOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = if (isAppInDarkTheme()) Color.White else Color.Black,
    unfocusedTextColor = if (isAppInDarkTheme()) Color.White.copy(alpha = 0.9f) else Color.Black,
    focusedLabelColor = if (isAppInDarkTheme()) Color(0xFF38BDF8) else Color(0xFF1A237E),
    unfocusedLabelColor = if (isAppInDarkTheme()) Color.White.copy(alpha = 0.7f) else Color.Gray,
    focusedBorderColor = if (isAppInDarkTheme()) Color(0xFF38BDF8) else Color(0xFF1A237E),
    unfocusedBorderColor = if (isAppInDarkTheme()) Color.White.copy(alpha = 0.3f) else Color.LightGray,
    cursorColor = if (isAppInDarkTheme()) Color(0xFF38BDF8) else Color(0xFF1A237E)
)

@Composable
fun HelpsTheme(
    darkTheme: Boolean = isAppInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DeepNightColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
