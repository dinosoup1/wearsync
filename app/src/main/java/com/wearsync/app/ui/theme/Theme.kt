package com.wearsync.app.ui.theme

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

private val Blue40 = Color(0xFF1B6EF3)
private val BlueGrey40 = Color(0xFF4A6FA5)
private val Teal40 = Color(0xFF00897B)
private val Blue80 = Color(0xFF9ECAFF)
private val BlueGrey80 = Color(0xFFB8CCE4)
private val Teal80 = Color(0xFF80CBC4)

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    secondary = BlueGrey80,
    tertiary = Teal80,
    surface = Color(0xFF111318),
    background = Color(0xFF111318),
    onPrimary = Color(0xFF003063),
    onSecondary = Color(0xFF1F3355),
    onTertiary = Color(0xFF003731),
    surfaceVariant = Color(0xFF1D2025),
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = BlueGrey40,
    tertiary = Teal40,
    surface = Color(0xFFF8F9FF),
    background = Color(0xFFF8F9FF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    surfaceVariant = Color(0xFFEEF1F8),
)

@Composable
fun WearSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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
        content = content
    )
}
