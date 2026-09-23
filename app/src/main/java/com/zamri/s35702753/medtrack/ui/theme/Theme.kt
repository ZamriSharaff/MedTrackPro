package com.zamri.s35702753.medtrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,

    secondary = PurpleGrey40,
    onSecondary = Color.White,

    tertiary = Pink40,
    onTertiary = Color.White,

    background = Color(0xFFB3E5FC),
    onBackground = Color.Black,

    surface = Color.White,
    onSurface = Color.Black,

    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF3F3A43),

    primaryContainer = Color(0xFFD0BCFF),
    onPrimaryContainer = Color(0xFF21005D),

    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),

    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Color.Black,

    secondary = PurpleGrey80,
    onSecondary = Color.Black,

    tertiary = Pink80,
    onTertiary = Color.Black,

    background = Color(0xFF101418),
    onBackground = Color.White,

    surface = Color(0xFF101418),
    onSurface = Color.White,

    surfaceVariant = Color(0xFF45464D),
    onSurfaceVariant = Color(0xFFE5E1E9),

    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),

    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun MedTrackPro_Assignment3Theme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}