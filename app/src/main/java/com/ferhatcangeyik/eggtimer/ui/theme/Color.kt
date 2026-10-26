package com.ferhatcangeyik.eggtimer.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Uygulamanın renk paleti.
 *
 * Renkler doğrudan değil rolleriyle kullanılır (zemin, başlık, vurgu…);
 * böylece aynı ekran kodu hem açık hem karanlık temada çalışır.
 */
@Immutable
data class EggColors(
    val background: Color,
    val surface: Color,
    val surfaceSelected: Color,
    val title: Color,
    val body: Color,
    val bodyStrong: Color,
    val muted: Color,
    val accent: Color,
    val accentDeep: Color,
    val onAccent: Color,
    val neutralButton: Color,
    val ringTrack: Color,
    val danger: Color,
    val success: Color,
    val successSurface: Color,
    val shadowTint: Color
)

val LightEggColors = EggColors(
    background = Color(0xFFFEF5E7),
    surface = Color(0xFFFFFFFF),
    surfaceSelected = Color(0xFFFFCC80),
    title = Color(0xFF3E2723),
    body = Color(0xFF4E342E),
    bodyStrong = Color(0xFF5D4037),
    muted = Color(0xFF8D6E63),
    accent = Color(0xFFFFA726),
    accentDeep = Color(0xFFFF5722),
    onAccent = Color.White,
    neutralButton = Color(0xFF8D6E63),
    ringTrack = Color(0xFFFFCC80),
    danger = Color(0xFFE53935),
    success = Color(0xFF4CAF50),
    successSurface = Color(0xFFE8F5E9),
    shadowTint = Color(0xFFE3CBA4)
)

val DarkEggColors = EggColors(
    background = Color(0xFF17120F),
    surface = Color(0xFF2A211C),
    surfaceSelected = Color(0xFF4A3520),
    title = Color(0xFFF6EBDF),
    body = Color(0xFFE6D8CA),
    bodyStrong = Color(0xFFF1E4D6),
    muted = Color(0xFFAD9987),
    accent = Color(0xFFFFA726),
    accentDeep = Color(0xFFFF7043),
    onAccent = Color(0xFF2A1B10),
    neutralButton = Color(0xFF5A483D),
    ringTrack = Color(0xFF4A3520),
    danger = Color(0xFFEF5350),
    success = Color(0xFF66BB6A),
    successSurface = Color(0xFF1F3322),
    shadowTint = Color(0xFF0E0B09)
)

val LocalEggColors = staticCompositionLocalOf { LightEggColors }
