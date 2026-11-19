package com.example.payit.ui.theme

import androidx.compose.ui.graphics.Color

// Light Mode Tokens — GPay + Neo-Bank Fintech
val LightPrimaryRoyalBlue = Color(0xFF0B57D0)
val LightInteractiveBlue = Color(0xFF1A73E8)
val LightPrimaryContainer = Color(0xFFD3E3FD)
val LightOnPrimaryContainer = Color(0xFF041E49)

val LightVerifiedEmerald = Color(0xFF146C2E)
val LightEmeraldContainer = Color(0xFFC4EED0)

val LightRewardGold = Color(0xFFD97706)
val LightRewardGoldContainer = Color(0xFFFEF3C7)

val LightErrorCoral = Color(0xFFB3261E)
val LightErrorContainer = Color(0xFFF9DEDC)

val LightBackgroundAlabaster = Color(0xFFF8FAFD)
val LightSurfacePure = Color(0xFFFFFFFF)
val LightSurfaceContainer = Color(0xFFF0F4F9)
val LightSurfaceContainerHigh = Color(0xFFE9EEF6)

val LightTextPrimary = Color(0xFF1F1F1F)
val LightTextSecondary = Color(0xFF444746)
val LightTextMuted = Color(0xFF747775)
val LightDivider = Color(0xFFE1E6EF)

// Dark Mode Tokens — Deep Obsidian Neo-Bank Surface
val DarkPrimarySkyBlue = Color(0xFFA8C7FA)
val DarkInteractiveBlue = Color(0xFF4C8DF6)
val DarkPrimaryContainer = Color(0xFF0842A0)
val DarkOnPrimaryContainer = Color(0xFFD3E3FD)

val DarkVerifiedEmerald = Color(0xFF6DD58C)
val DarkEmeraldContainer = Color(0xFF0F5223)

val DarkRewardGold = Color(0xFFFBBF24)
val DarkRewardGoldContainer = Color(0xFF452B03)

val DarkErrorCoral = Color(0xFFF2B8B5)
val DarkErrorContainer = Color(0xFF601410)

val DarkBackgroundObsidian = Color(0xFF0E1117)
val DarkSurfaceCharcoal = Color(0xFF13171F)
val DarkSurfaceContainer = Color(0xFF1E232E)
val DarkSurfaceContainerHigh = Color(0xFF282E3B)

val DarkTextPrimary = Color(0xFFE3E3E3)
val DarkTextSecondary = Color(0xFFC4C7C5)
val DarkTextMuted = Color(0xFF8E918F)
val DarkDivider = Color(0xFF2B313E)

// Brand & Hero Accents
val GPayBlue = Color(0xFF1A73E8)
val GPayGreen = Color(0xFF1E8E3E)
val GPayYellow = Color(0xFFF9AB00)
val GPayRed = Color(0xFFD93025)
val NpciDarkHeader = Color(0xFF131B2E)
val ScannerBackdrop = Color(0xFF090C12)

// Curated Avatar & Merchant Brand Palettes (background + foreground)
data class AvatarColorPair(
    val container: Color,
    val content: Color
)

val AvatarPalette: List<AvatarColorPair> = listOf(
    AvatarColorPair(Color(0xFFD3E3FD), Color(0xFF041E49)), // 0: Google Blue Tint
    AvatarColorPair(Color(0xFFC4EED0), Color(0xFF072711)), // 1: Emerald Mint
    AvatarColorPair(Color(0xFFFFE0B2), Color(0xFF4E2600)), // 2: Warm Amber
    AvatarColorPair(Color(0xFFEADDFF), Color(0xFF21005D)), // 3: Royal Lavender
    AvatarColorPair(Color(0xFFFFD8E4), Color(0xFF31111D)), // 4: Rose Berry
    AvatarColorPair(Color(0xFFC2E7FF), Color(0xFF001D35)), // 5: Sky Cyan
    AvatarColorPair(Color(0xFFFFDAD6), Color(0xFF410002)), // 6: Terracotta
    AvatarColorPair(Color(0xFFD7E8CD), Color(0xFF12200D)), // 7: Sage Olive
    AvatarColorPair(Color(0xFF00704A), Color(0xFFFFFFFF)), // 8: Starbucks Forest
    AvatarColorPair(Color(0xFFE23744), Color(0xFFFFFFFF)), // 9: Zomato Crimson
    AvatarColorPair(Color(0xFFFC8019), Color(0xFFFFFFFF)), // 10: Swiggy Tangerine
    AvatarColorPair(Color(0xFF1A1A2E), Color(0xFFFFFFFF)), // 11: Uber Midnight
    AvatarColorPair(Color(0xFF2874F0), Color(0xFFFFFFFF)), // 12: Flipkart Cobalt
    AvatarColorPair(Color(0xFF1F2937), Color(0xFFFBBF24)), // 13: CRED Charcoal & Gold
    AvatarColorPair(Color(0xFF5F259F), Color(0xFFFFFFFF)), // 14: Jio / Telecom Violet
    AvatarColorPair(Color(0xFF0F766E), Color(0xFFFFFFFF))  // 15: Utility Teal
)

fun avatarPairAt(index: Int): AvatarColorPair {
    if (AvatarPalette.isEmpty()) return AvatarColorPair(LightPrimaryContainer, LightOnPrimaryContainer)
    val safeIndex = ((index % AvatarPalette.size) + AvatarPalette.size) % AvatarPalette.size
    return AvatarPalette[safeIndex]
}
