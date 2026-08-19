package com.vocalrange.analyzer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrimaryColor = Color(0xFF3F6BFF)
private val SecondaryColor = Color(0xFF00C2A8)
private val ErrorColor = Color(0xFFE05353)

private val DarkColors = darkColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    error = ErrorColor,
    background = Color(0xFF10131A),
    surface = Color(0xFF191D26)
)

private val LightColors = lightColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    error = ErrorColor,
    background = Color(0xFFF7F8FC),
    surface = Color.White
)

@Composable
fun VoiceRangeAnalyzerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
