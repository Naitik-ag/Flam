package com.example.flam.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.flam.R

private val LightColors = lightColorScheme(
    primary = Color(0xFF4A6CF7),
    onPrimary = Color.White,

    secondary = Color(0xFF5C5F6A),
    onSecondary = Color.White,

    background = Color(0xFFF3F5F9),
    onBackground = Color(0xFF1B1C1E),

    surface = Color.White,
    onSurface = Color(0xFF1B1C1E),

    surfaceVariant = Color(0xFFE6E8EF),
    onSurfaceVariant = Color(0xFF2D2F33),

    error = Color(0xFFD32F2F),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF728AFF),
    onPrimary = Color(0xFF0C1020),

    secondary = Color(0xFF9296A5),
    onSecondary = Color(0xFF121212),

    background = Color(0xFF0E0F12),
    onBackground = Color(0xFFE1E3E8),

    surface = Color(0xFF1A1B20),
    onSurface = Color(0xFFE1E3E8),

    surfaceVariant = Color(0xFF2A2C33),
    onSurfaceVariant = Color(0xFFD8DADE),

    error = Color(0xFFFF5A5F),
    onError = Color(0xFF1A0A0A)
)

@Composable
fun FLAMTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = FlamTypography,
        shapes = FlamShapes,
        content = content
    )
}
