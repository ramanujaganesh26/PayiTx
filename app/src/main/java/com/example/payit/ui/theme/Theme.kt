package com.example.payit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

@Immutable
data class PayitExtendedColors(
    val background: Color,
    val surface: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val primary: Color,
    val primaryAccent: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val verifiedGreen: Color,
    val verifiedGreenContainer: Color,
    val rewardGold: Color,
    val rewardGoldContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val divider: Color,
    val isDark: Boolean
)

private val LightPayitColors = PayitExtendedColors(
    background = LightBackgroundAlabaster,
    surface = LightSurfacePure,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    primary = LightPrimaryRoyalBlue,
    primaryAccent = LightInteractiveBlue,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    verifiedGreen = LightVerifiedEmerald,
    verifiedGreenContainer = LightEmeraldContainer,
    rewardGold = LightRewardGold,
    rewardGoldContainer = LightRewardGoldContainer,
    error = LightErrorCoral,
    errorContainer = LightErrorContainer,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    divider = LightDivider,
    isDark = false
)

private val DarkPayitColors = PayitExtendedColors(
    background = DarkBackgroundObsidian,
    surface = DarkSurfaceCharcoal,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    primary = DarkPrimarySkyBlue,
    primaryAccent = DarkInteractiveBlue,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    verifiedGreen = DarkVerifiedEmerald,
    verifiedGreenContainer = DarkEmeraldContainer,
    rewardGold = DarkRewardGold,
    rewardGoldContainer = DarkRewardGoldContainer,
    error = DarkErrorCoral,
    errorContainer = DarkErrorContainer,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    divider = DarkDivider,
    isDark = true
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimaryRoyalBlue,
    onPrimary = Color.White,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightInteractiveBlue,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceContainerHigh,
    onSecondaryContainer = LightTextPrimary,
    tertiary = LightVerifiedEmerald,
    onTertiary = Color.White,
    tertiaryContainer = LightEmeraldContainer,
    onTertiaryContainer = Color(0xFF072711),
    background = LightBackgroundAlabaster,
    onBackground = LightTextPrimary,
    surface = LightSurfacePure,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceContainer,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerLowest = LightSurfacePure,
    surfaceContainerLow = LightBackgroundAlabaster,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    outline = LightTextMuted,
    outlineVariant = LightDivider,
    error = LightErrorCoral,
    onError = Color.White,
    errorContainer = LightErrorContainer,
    onErrorContainer = Color(0xFF410E0B)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimarySkyBlue,
    onPrimary = Color(0xFF041E49),
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkInteractiveBlue,
    onSecondary = Color(0xFF041E49),
    secondaryContainer = DarkSurfaceContainerHigh,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = DarkVerifiedEmerald,
    onTertiary = Color(0xFF072711),
    tertiaryContainer = DarkEmeraldContainer,
    onTertiaryContainer = Color(0xFFC4EED0),
    background = DarkBackgroundObsidian,
    onBackground = DarkTextPrimary,
    surface = DarkSurfaceCharcoal,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceContainer,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLowest = DarkBackgroundObsidian,
    surfaceContainerLow = DarkSurfaceCharcoal,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    outline = DarkTextMuted,
    outlineVariant = DarkDivider,
    error = DarkErrorCoral,
    onError = Color(0xFF601410),
    errorContainer = DarkErrorContainer,
    onErrorContainer = Color(0xFFF9DEDC)
)

val LocalPayitColors = staticCompositionLocalOf { LightPayitColors }

object PayitTheme {
    val colors: PayitExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPayitColors.current
}

@Composable
fun isPayitDarkTheme(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
fun PayitTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    @Suppress("UNUSED_PARAMETER") dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = isPayitDarkTheme(themeMode)
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkPayitColors else LightPayitColors

    CompositionLocalProvider(LocalPayitColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
