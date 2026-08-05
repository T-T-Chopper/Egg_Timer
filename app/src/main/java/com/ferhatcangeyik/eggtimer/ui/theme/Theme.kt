package com.ferhatcangeyik.eggtimer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private fun materialSchemeFrom(colors: EggColors, dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = colors.accent,
        onPrimary = colors.onAccent,
        secondary = colors.muted,
        background = colors.background,
        onBackground = colors.title,
        surface = colors.surface,
        onSurface = colors.bodyStrong
    )
} else {
    lightColorScheme(
        primary = colors.accent,
        onPrimary = colors.onAccent,
        secondary = colors.muted,
        background = colors.background,
        onBackground = colors.title,
        surface = colors.surface,
        onSurface = colors.bodyStrong
    )
}

@Composable
fun EggTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val eggColors = if (darkTheme) DarkEggColors else LightEggColors

    // Material bileşenleri (menü, dalga efekti) da aynı paletten beslensin
    CompositionLocalProvider(LocalEggColors provides eggColors) {
        MaterialTheme(
            colorScheme = materialSchemeFrom(eggColors, darkTheme),
            typography = Typography,
            content = content
        )
    }
}
