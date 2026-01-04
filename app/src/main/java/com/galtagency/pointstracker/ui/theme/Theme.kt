package com.galtagency.pointstracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Gold80,
    onPrimary = Color(0xFF1A1612),
    primaryContainer = GoldAmber40,
    onPrimaryContainer = GoldCream80,

    secondary = GoldAmber80,
    onSecondary = Color(0xFF1A1612),
    secondaryContainer = GoldBronze40,
    onSecondaryContainer = GoldCream80,

    tertiary = GoldCream80,
    onTertiary = Color(0xFF1A1612),
    tertiaryContainer = Gold40,
    onTertiaryContainer = GoldCream80,

    background = DarkGoldBg,
    onBackground = GoldCream80,

    surface = Color(0xFF1F1B16),
    onSurface = GoldCream80,
    surfaceVariant = Color(0xFF2A2520),
    onSurfaceVariant = GoldAmber80,

    outline = GoldAmber40,
    outlineVariant = GoldBronze40,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val LightColorScheme = lightColorScheme(
    primary = Gold40,
    onPrimary = Color.White,
    primaryContainer = GoldCream80,
    onPrimaryContainer = DeepGold,

    secondary = GoldAmber40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF0C2),
    onSecondaryContainer = GoldBronze40,

    tertiary = GoldBronze40,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE5B4),
    onTertiaryContainer = DeepGold,

    background = LightGoldBg,
    onBackground = Color(0xFF1F1B16),

    surface = Color(0xFFFFFBF7),
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = Color(0xFFFFF4E0),
    onSurfaceVariant = GoldBronze40,

    outline = GoldAmber40,
    outlineVariant = Color(0xFFE5D5B3),

    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun PointsTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color disabled to always use gold palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}