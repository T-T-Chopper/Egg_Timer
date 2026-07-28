package com.example.eggtimer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EggOrange,
    secondary = EggBrownLight,
    tertiary = EggSelected
)

private val LightColorScheme = lightColorScheme(
    primary = EggOrange,
    secondary = EggBrownLight,
    tertiary = EggBrown,
    background = EggCream,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = EggBrownDarkest,
    onSurface = EggBrownDarkest
)

@Composable
fun EggTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
