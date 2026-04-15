package com.vocallock.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Brand palette ─────────────────────────────────────────────

object VLColor {
    val MidnightSlate = Color(0xFF0D1117)
    val Onyx = Color(0xFF161B22)
    val Surface = Color(0xFF1C2128)
    val SurfaceVariant = Color(0xFF22272E)
    val SurfaceHigh = Color(0xFF2D333B)

    // Trust — greens and teals
    val TrustGreen = Color(0xFF1D9E75)
    val TrustGreenDim = Color(0xFF0F6E56)
    val TrustGreenSoft = Color(0xFF9FE1CB)
    val TrustGreenBg = Color(0xFF04342C)

    // Action — cyan
    val NeonCyan = Color(0xFF00E5FF)
    val CyanDim = Color(0xFF00ACC1)

    // Secondary — cerulean
    val CeruleanBlue = Color(0xFF378ADD)
    val CeruleanDim = Color(0xFF185FA5)

    // Lockdown — crimson
    val CrimsonRed = Color(0xFFD32F2F)
    val RubyRed = Color(0xFFB71C1C)
    val CrimsonSoft = Color(0xFFEF9A9A)
    val CrimsonBg = Color(0xFF1A0A0A)

    // Nudge — amber
    val NudgeAmber = Color(0xFFFFC107)
    val NudgeAmberDim = Color(0xFFFF8F00)
    val NudgeAmberSoft = Color(0xFFFFECB3)

    // Text
    val TextPrimary = Color(0xFFE6EDF3)
    val TextSecondary = Color(0xFF8B949E)
    val TextMuted = Color(0xFF484F58)
    val TextDisabled = Color(0xFF30363D)

    // Borders
    val Border = Color(0xFF30363D)
    val BorderEmphasis = Color(0xFF484F58)
}

// ── Dark colour scheme ────────────────────────────────────────

private val DarkScheme = darkColorScheme(
    primary = VLColor.TrustGreen,
    onPrimary = Color(0xFF003824),
    primaryContainer = VLColor.TrustGreenDim,
    secondary = VLColor.CeruleanBlue,
    onSecondary = Color(0xFF002855),
    tertiary = VLColor.NeonCyan,
    background = VLColor.MidnightSlate,
    onBackground = VLColor.TextPrimary,
    surface = VLColor.Onyx,
    onSurface = VLColor.TextPrimary,
    surfaceVariant = VLColor.SurfaceVariant,
    onSurfaceVariant = VLColor.TextSecondary,
    outline = VLColor.Border,
    outlineVariant = VLColor.BorderEmphasis,
    error = VLColor.CrimsonRed,
    onError = Color.White,
)

// ── Typography ────────────────────────────────────────────────

val VLType = Typography(
    displayLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.W400,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.W500, lineHeight = 32.sp),
    headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.W500, lineHeight = 28.sp),
    titleLarge = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.W500,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.W500, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.W400, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W400, lineHeight = 22.sp),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.W400, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.W500, lineHeight = 20.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.W500, lineHeight = 16.sp),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.W400, lineHeight = 16.sp),
)

// ── Base Material Shapes ──────────────────────────────────────

val MaterialBaseShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

// ── Entry point ───────────────────────────────────────────────

@Composable
fun VocalLockTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = VLType,
        shapes = MaterialBaseShapes,
        content = content,
    )
}