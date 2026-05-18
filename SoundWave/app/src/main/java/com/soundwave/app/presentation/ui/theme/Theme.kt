package com.soundwave.app.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Brand Colors ─────────────────────────────────────────────────────────────

val SoundWaveGreen = Color(0xFF1ED760)
val SoundWaveGreenDark = Color(0xFF169C45)
val SoundWaveBlack = Color(0xFF000000)
val SoundWaveBlackSurface = Color(0xFF0A0A0A)
val SoundWaveBlackCard = Color(0xFF121212)
val SoundWaveBlackElevated = Color(0xFF1A1A1A)
val SoundWaveDarkGray = Color(0xFF282828)
val SoundWaveMidGray = Color(0xFF404040)
val SoundWaveLightGray = Color(0xFFB3B3B3)
val SoundWaveWhite = Color(0xFFFFFFFF)
val SoundWaveWhiteAlpha80 = Color(0xCCFFFFFF)
val SoundWaveWhiteAlpha60 = Color(0x99FFFFFF)
val SoundWaveWhiteAlpha30 = Color(0x4DFFFFFF)
val SoundWaveWhiteAlpha10 = Color(0x1AFFFFFF)

// Glassmorphism tokens
val GlassWhite = Color(0x26FFFFFF)      // 15% white
val GlassBorder = Color(0x33FFFFFF)     // 20% white
val GlassBlack = Color(0x80000000)      // 50% black
val GlassBackgroundDark = Color(0xCC000000) // 80% black

// ─── Dark (AMOLED) Color Scheme ───────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary = SoundWaveGreen,
    onPrimary = SoundWaveBlack,
    primaryContainer = Color(0xFF003314),
    onPrimaryContainer = Color(0xFF9EFFC0),
    secondary = SoundWaveLightGray,
    onSecondary = SoundWaveBlack,
    secondaryContainer = SoundWaveDarkGray,
    onSecondaryContainer = SoundWaveWhite,
    tertiary = Color(0xFF536EE0),
    onTertiary = SoundWaveWhite,
    tertiaryContainer = Color(0xFF001B6E),
    onTertiaryContainer = Color(0xFFDDE1FF),
    error = Color(0xFFCF6679),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = SoundWaveBlack,
    onBackground = SoundWaveWhite,
    surface = SoundWaveBlackCard,
    onSurface = SoundWaveWhite,
    surfaceVariant = SoundWaveDarkGray,
    onSurfaceVariant = SoundWaveLightGray,
    surfaceTint = SoundWaveGreen,
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = SoundWaveGreenDark,
    outline = SoundWaveMidGray,
    outlineVariant = Color(0xFF49454F),
    scrim = SoundWaveBlack
)

// ─── Light Color Scheme ───────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary = SoundWaveGreenDark,
    onPrimary = SoundWaveWhite,
    primaryContainer = Color(0xFFC7FFD9),
    onPrimaryContainer = Color(0xFF00210D),
    secondary = Color(0xFF526350),
    onSecondary = SoundWaveWhite,
    secondaryContainer = Color(0xFFD5E8CF),
    onSecondaryContainer = Color(0xFF101F10),
    background = Color(0xFFFCFDF6),
    onBackground = Color(0xFF1A1C19),
    surface = Color(0xFFFCFDF6),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFDEE4D7),
    onSurfaceVariant = Color(0xFF424940)
)

// ─── AMOLED Color Scheme ──────────────────────────────────────────────────────

private val AmoledColorScheme = DarkColorScheme.copy(
    background = SoundWaveBlack,
    surface = SoundWaveBlack,
    surfaceVariant = SoundWaveBlackSurface
)

// ─── Typography ───────────────────────────────────────────────────────────────

val SoundWaveTypography = Typography(
    displayLarge = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = androidx.compose.ui.unit.sp(57)
    ),
    displayMedium = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = androidx.compose.ui.unit.sp(45)
    ),
    headlineLarge = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        fontSize = androidx.compose.ui.unit.sp(32)
    ),
    headlineMedium = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        fontSize = androidx.compose.ui.unit.sp(28)
    ),
    titleLarge = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        fontSize = androidx.compose.ui.unit.sp(22)
    ),
    titleMedium = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = androidx.compose.ui.unit.sp(16)
    ),
    titleSmall = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = androidx.compose.ui.unit.sp(14)
    ),
    bodyLarge = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = androidx.compose.ui.unit.sp(16)
    ),
    bodyMedium = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = androidx.compose.ui.unit.sp(14)
    ),
    bodySmall = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal,
        fontSize = androidx.compose.ui.unit.sp(12)
    ),
    labelLarge = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = androidx.compose.ui.unit.sp(14)
    ),
    labelMedium = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = androidx.compose.ui.unit.sp(12)
    ),
    labelSmall = androidx.compose.ui.text.TextStyle(
        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
        fontSize = androidx.compose.ui.unit.sp(11)
    )
)

// ─── Custom Theme Extensions ──────────────────────────────────────────────────

data class SoundWaveExtendedColors(
    val playerBackground: Color,
    val miniPlayerBackground: Color,
    val cardGlass: Color,
    val cardBorder: Color,
    val waveform: Color,
    val favoriteActive: Color,
    val downloadActive: Color,
    val lyricsHighlight: Color,
    val lyricsInactive: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    SoundWaveExtendedColors(
        playerBackground = SoundWaveBlack,
        miniPlayerBackground = GlassBlack,
        cardGlass = GlassWhite,
        cardBorder = GlassBorder,
        waveform = SoundWaveGreen,
        favoriteActive = Color(0xFFFF6B6B),
        downloadActive = SoundWaveGreen,
        lyricsHighlight = SoundWaveWhite,
        lyricsInactive = SoundWaveWhiteAlpha30
    )
}

// ─── Main Theme Composable ────────────────────────────────────────────────────

@Composable
fun SoundWaveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    amoledMode: Boolean = false,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            when {
                amoledMode -> dynamicDarkColorScheme(context).copy(
                    background = SoundWaveBlack,
                    surface = SoundWaveBlack
                )
                darkTheme -> dynamicDarkColorScheme(context)
                else -> dynamicLightColorScheme(context)
            }
        }
        amoledMode -> AmoledColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColors = SoundWaveExtendedColors(
        playerBackground = if (darkTheme || amoledMode) SoundWaveBlack else Color.White,
        miniPlayerBackground = if (darkTheme || amoledMode) GlassBlack else Color(0xE6FFFFFF),
        cardGlass = if (darkTheme || amoledMode) GlassWhite else Color(0x26000000),
        cardBorder = if (darkTheme || amoledMode) GlassBorder else Color(0x33000000),
        waveform = colorScheme.primary,
        favoriteActive = Color(0xFFFF6B6B),
        downloadActive = colorScheme.primary,
        lyricsHighlight = colorScheme.onBackground,
        lyricsInactive = colorScheme.onBackground.copy(alpha = 0.3f)
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme && !amoledMode
            insetsController.isAppearanceLightNavigationBars = !darkTheme && !amoledMode
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SoundWaveTypography,
            content = content
        )
    }
}

val MaterialTheme.extendedColors: SoundWaveExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
