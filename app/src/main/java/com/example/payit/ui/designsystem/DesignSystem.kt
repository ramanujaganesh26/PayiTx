package com.example.payit.ui.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payit.ui.theme.PayitTheme
import com.example.payit.ui.theme.avatarPairAt
import kotlin.math.abs
import kotlin.math.roundToLong

object PayitSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val section = 28.dp
}

object PayitRadius {
    val small = 10.dp
    val medium = 16.dp
    val large = 22.dp
    val xl = 28.dp
    val pill = 100.dp
}

data class CountryDialCode(
    val name: String,
    val isoCode: String,
    val dialCode: String,
    val expectedDigits: Int = 10
)

val SupportedCountryCodes = listOf(
    CountryDialCode("India", "IN", "+91", 10),
    CountryDialCode("United States", "US", "+1", 10),
    CountryDialCode("United Kingdom", "UK", "+44", 10),
    CountryDialCode("United Arab Emirates", "UAE", "+971", 9),
    CountryDialCode("Singapore", "SG", "+65", 8)
)

fun formatPhoneNumberDisplay(rawDigits: String): String {
    val digits = rawDigits.filter { it.isDigit() }
    return if (digits.length > 5) {
        "${digits.substring(0, 5)} ${digits.substring(5)}"
    } else {
        digits
    }
}

/**
 * Formats an amount in Indian Rupees using authentic Indian comma grouping (e.g. ₹1,42,850 or ₹1,840.50).
 */
fun formatInr(amount: Double, showDecimals: Boolean = false): String {
    val negative = amount < 0
    val absAmount = abs(amount)
    val prefix = if (negative) "-₹" else "₹"

    if (showDecimals || (absAmount % 1.0) > 0.004) {
        val totalPaise = (absAmount * 100.0).roundToLong()
        val rupees = totalPaise / 100
        val paise = (totalPaise % 100).toInt()
        val formattedRupees = formatIndianInteger(rupees)
        val paiseStr = paise.toString().padStart(2, '0')
        return "$prefix$formattedRupees.$paiseStr"
    }
    return "$prefix${formatIndianInteger(absAmount.roundToLong())}"
}

private fun formatIndianInteger(number: Long): String {
    val str = number.toString()
    if (str.length <= 3) return str
    val lastThree = str.substring(str.length - 3)
    val rest = str.substring(0, str.length - 3)
    val sb = StringBuilder()
    var count = 0
    for (i in rest.length - 1 downTo 0) {
        sb.append(rest[i])
        count++
        if (count == 2 && i != 0) {
            sb.append(',')
            count = 0
        }
    }
    return sb.reverse().toString() + "," + lastThree
}

/**
 * Shared GPay-style Top App Bar with edge-to-edge status bar inset handling.
 */
@Composable
fun GPayTopAppBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = PayitTheme.colors.background,
    contentColor: Color = PayitTheme.colors.textPrimary,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Surface(
        color = containerColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = PayitSpacing.sm, vertical = PayitSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = PayitIcons.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = PayitSpacing.xs)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = PayitTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

/**
 * Iconic GPay Floating Search Bar with leading Search icon and right-aligned Profile Avatar + mini QR badge.
 */
@Composable
fun GPayFloatingSearchBar(
    placeholder: String = "Pay friends, merchants, or bills",
    userInitials: String,
    onSearchClick: () -> Unit,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
    showThemeQuickToggle: Boolean = false,
    onThemeToggleClick: (() -> Unit)? = null
) {
    val colors = PayitTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PayitSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PayitSpacing.md)
    ) {
        Surface(
            onClick = onSearchClick,
            shape = RoundedCornerShape(PayitRadius.pill),
            color = colors.surface,
            shadowElevation = if (colors.isDark) 2.dp else 4.dp,
            tonalElevation = 1.dp,
            border = BorderStroke(
                width = 1.dp,
                color = if (colors.isDark) colors.divider else colors.divider.copy(alpha = 0.7f)
            ),
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PayitSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PayitSpacing.md)
            ) {
                Icon(
                    imageVector = PayitIcons.Search,
                    contentDescription = "Search",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (showThemeQuickToggle && onThemeToggleClick != null) {
                    Surface(
                        onClick = onThemeToggleClick,
                        shape = CircleShape,
                        color = colors.surfaceContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = PayitIcons.SunMoon,
                                contentDescription = "Switch theme",
                                tint = colors.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onAvatarClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userInitials,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = if (colors.isDark) Color(0xFF041E49) else Color.White
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-1).dp, y = (-1).dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(colors.surface)
                    .border(1.dp, colors.divider, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PayitIcons.QrCode,
                    contentDescription = "My QR code",
                    tint = colors.primary,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}

/**
 * GPay 4-column quick action tile with tonal squircle icon container and 2-line centered label.
 */
@Composable
fun GPayQuickActionTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    containerColor: Color? = null,
    iconTint: Color? = null
) {
    val colors = PayitTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "tileScale"
    )

    val resolvedContainer = containerColor ?: if (highlighted) {
        colors.primaryAccent
    } else {
        colors.surfaceContainer
    }
    val resolvedTint = iconTint ?: if (highlighted) {
        Color.White
    } else {
        colors.primary
    }

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(PayitRadius.medium))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = PayitSpacing.xs, horizontal = PayitSpacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (highlighted && containerColor == null) Color.Transparent else resolvedContainer,
            shadowElevation = if (highlighted) 4.dp else 0.dp,
            border = if (highlighted) null else BorderStroke(
                width = 0.85.dp,
                color = colors.divider.copy(alpha = 0.75f)
            ),
            modifier = Modifier.size(56.dp)
        ) {
            val boxModifier = if (highlighted && containerColor == null) {
                val gradientColors = if (colors.isDark) {
                    listOf(Color(0xFF1A73E8), Color(0xFF4C8DF6))
                } else {
                    listOf(Color(0xFF0B57D0), Color(0xFF1A73E8))
                }
                Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(gradientColors))
            } else {
                Modifier.fillMaxSize()
            }
            Box(
                modifier = boxModifier,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = resolvedTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                lineHeight = 15.sp,
                fontSize = 11.5.sp
            ),
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Reusable circular contact or merchant avatar.
 */
@Composable
fun GPayAvatarCircle(
    initials: String,
    colorIndex: Int,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    isVerified: Boolean = false,
    hasActivityRing: Boolean = false
) {
    val pair = avatarPairAt(colorIndex)
    val colors = PayitTheme.colors
    val ringBrush = Brush.linearGradient(listOf(colors.primaryAccent, colors.verifiedGreen))

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val innerModifier = if (hasActivityRing) {
            Modifier
                .fillMaxSize()
                .border(2.dp, ringBrush, CircleShape)
                .padding(3.5.dp)
        } else {
            Modifier.fillMaxSize()
        }
        Box(
            modifier = innerModifier
                .clip(CircleShape)
                .background(pair.container)
                .border(1.dp, colors.surface.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = if (size >= 52.dp) {
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                } else {
                    MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                },
                color = pair.content
            )
        }
        if (isVerified) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(colors.surface)
                    .padding(1.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PayitIcons.VerifiedShield,
                    contentDescription = "Verified",
                    tint = colors.verifiedGreen,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * GPay People / Businesses circular avatar grid cell with optional cashback/status pill.
 */
@Composable
fun GPayAvatarGridItem(
    name: String,
    initials: String,
    colorIndex: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    isVerified: Boolean = false,
    hasActivityRing: Boolean = false
) {
    val colors = PayitTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "avatarScale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(PayitRadius.medium))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = PayitSpacing.xs, horizontal = PayitSpacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.BottomCenter) {
            GPayAvatarCircle(
                initials = initials,
                colorIndex = colorIndex,
                size = 56.dp,
                isVerified = isVerified && badgeText == null,
                hasActivityRing = hasActivityRing
            )
            if (!badgeText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(PayitRadius.pill),
                    color = colors.rewardGoldContainer,
                    border = BorderStroke(1.dp, colors.surface),
                    modifier = Modifier.offset(y = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (colors.isDark) colors.rewardGold else Color(0xFF92400E),
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(if (badgeText != null) 8.dp else 6.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 15.sp
            ),
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Clean section header with optional subtitle and trailing action pill.
 */
@Composable
fun GPaySectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    val colors = PayitTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PayitSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 19.sp
                ),
                color = colors.textPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
        if (!actionLabel.isNullOrBlank() && onActionClick != null) {
            Surface(
                onClick = onActionClick,
                shape = RoundedCornerShape(PayitRadius.pill),
                color = colors.primaryContainer.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.onPrimaryContainer
                    )
                    Icon(
                        imageVector = PayitIcons.ChevronRight,
                        contentDescription = null,
                        tint = colors.onPrimaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * GPay "Manage your money" footer row with icon, title, subtitle, optional status pill, and chevron.
 */
@Composable
fun GPayManageMoneyRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    statusBadge: String? = null,
    statusBadgeHighlight: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PayitTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = PayitSpacing.lg, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = colors.surfaceContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = colors.textPrimary
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }
        if (!statusBadge.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(PayitRadius.pill),
                color = if (statusBadgeHighlight) colors.verifiedGreenContainer else colors.surfaceContainerHigh
            ) {
                Text(
                    text = statusBadge,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (statusBadgeHighlight) {
                        if (colors.isDark) colors.verifiedGreen else Color(0xFF0A4D1F)
                    } else {
                        colors.textSecondary
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            imageVector = PayitIcons.ChevronRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * Custom Canvas QR Code renderer generating a crisp deterministic QR matrix with finder squares and Payit center badge.
 */
@Composable
fun GPayQrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 196.dp
) {
    val matrixSize = 25
    val grid = remember(data) {
        buildDeterministicQrMatrix(data, matrixSize)
    }
    val colors = PayitTheme.colors

    Surface(
        shape = RoundedCornerShape(PayitRadius.large),
        color = Color.White,
        border = BorderStroke(1.dp, colors.divider),
        shadowElevation = 2.dp,
        modifier = modifier.size(size)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
            ) {
                val cellSize = this.size.width / matrixSize
                val moduleColor = Color(0xFF111827)

                for (row in 0 until matrixSize) {
                    for (col in 0 until matrixSize) {
                        if (grid[row][col]) {
                            drawRoundRect(
                                color = moduleColor,
                                topLeft = Offset(col * cellSize, row * cellSize),
                                size = Size(cellSize * 0.92f, cellSize * 0.92f),
                                cornerRadius = CornerRadius(cellSize * 0.22f, cellSize * 0.22f)
                            )
                        }
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 3.dp,
                border = BorderStroke(2.dp, Color(0xFF1A73E8)),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "P",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        ),
                        color = Color(0xFF0B57D0)
                    )
                }
            }
        }
    }
}

private fun buildDeterministicQrMatrix(seedText: String, size: Int): Array<BooleanArray> {
    val grid = Array(size) { BooleanArray(size) }

    fun placeFinder(startR: Int, startC: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isOuter = r == 0 || r == 6 || c == 0 || c == 6
                val isInner = r in 2..4 && c in 2..4
                grid[startR + r][startC + c] = isOuter || isInner
            }
        }
    }

    placeFinder(0, 0)
    placeFinder(0, size - 7)
    placeFinder(size - 7, 0)

    for (i in 8 until size - 8) {
        grid[6][i] = i % 2 == 0
        grid[i][6] = i % 2 == 0
    }

    val alignStart = size - 9
    for (r in 0 until 5) {
        for (c in 0 until 5) {
            val isEdge = r == 0 || r == 4 || c == 0 || c == 4
            val isCenter = r == 2 && c == 2
            grid[alignStart + r][alignStart + c] = isEdge || isCenter
        }
    }

    var hash = seedText.hashCode().toLong() * 2654435761L + 0x9E3779B9L
    for (r in 0 until size) {
        for (c in 0 until size) {
            val inTopLeft = r < 8 && c < 8
            val inTopRight = r < 8 && c >= size - 8
            val inBottomLeft = r >= size - 8 && c < 8
            val inTiming = r == 6 || c == 6
            val inCenterLogo = r in (size / 2 - 2)..(size / 2 + 2) && c in (size / 2 - 2)..(size / 2 + 2)
            val inAlignment = r in alignStart until (alignStart + 5) && c in alignStart until (alignStart + 5)

            if (!inTopLeft && !inTopRight && !inBottomLeft && !inTiming && !inCenterLogo && !inAlignment) {
                hash = hash xor (hash shl 13)
                hash = hash xor (hash ushr 7)
                hash = hash xor (hash shl 17)
                grid[r][c] = (hash and 0x3L) != 0L
            }
        }
    }
    return grid
}

/**
 * Animated segmented pill toggle for switching between Sign In and Sign Up.
 */
@Composable
fun AuthSegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PayitTheme.colors
    Surface(
        shape = RoundedCornerShape(PayitRadius.pill),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.divider),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val containerColor by animateColorAsState(
                    targetValue = if (selected) colors.surface else Color.Transparent,
                    animationSpec = tween(220),
                    label = "segColor"
                )
                Surface(
                    onClick = { onSelect(index) },
                    shape = RoundedCornerShape(PayitRadius.pill),
                    color = containerColor,
                    shadowElevation = if (selected) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                            ),
                            color = if (selected) colors.primary else colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fintech Phone Number Input Card with Country Code selector pill, live validation badge,
 * clear action, and optional direct keyboard editing.
 */
@Composable
fun PhoneInputCard(
    country: CountryDialCode,
    phoneDigits: String,
    onCountryClick: () -> Unit,
    onPhoneChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnlyForKeypad: Boolean = false,
    onDone: (() -> Unit)? = null
) {
    val colors = PayitTheme.colors
    val isValid = phoneDigits.length == country.expectedDigits
    val borderColor by animateColorAsState(
        targetValue = when {
            isValid -> colors.verifiedGreen
            phoneDigits.isNotEmpty() -> colors.primaryAccent
            else -> colors.divider
        },
        animationSpec = tween(200),
        label = "phoneBorder"
    )

    Surface(
        shape = RoundedCornerShape(PayitRadius.large),
        color = colors.surface,
        border = BorderStroke(if (phoneDigits.isNotEmpty()) 1.5.dp else 1.dp, borderColor),
        shadowElevation = if (colors.isDark) 0.dp else 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onCountryClick,
                shape = RoundedCornerShape(PayitRadius.medium),
                color = colors.surfaceContainer,
                border = BorderStroke(1.dp, colors.divider)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = country.isoCode,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary
                    )
                    Text(
                        text = country.dialCode,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                    Icon(
                        imageVector = PayitIcons.ChevronDown,
                        contentDescription = "Choose country code",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (readOnlyForKeypad) {
                    val formatted = formatPhoneNumberDisplay(phoneDigits)
                    Text(
                        text = formatted.ifEmpty { "Enter ${country.expectedDigits}-digit mobile number" },
                        style = if (formatted.isEmpty()) {
                            MaterialTheme.typography.bodyMedium
                        } else {
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.8.sp
                            )
                        },
                        color = if (formatted.isEmpty()) colors.textMuted else colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    if (phoneDigits.isEmpty()) {
                        Text(
                            text = "Enter ${country.expectedDigits}-digit number",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                    }
                    BasicTextField(
                        value = phoneDigits,
                        onValueChange = { next ->
                            val filtered = next.filter { it.isDigit() }.take(country.expectedDigits)
                            onPhoneChange(filtered)
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            letterSpacing = 0.8.sp
                        ),
                        cursorBrush = SolidColor(colors.primaryAccent),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onDone?.invoke() }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (isValid) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(colors.verifiedGreenContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PayitIcons.Check,
                        contentDescription = "Valid phone number",
                        tint = colors.verifiedGreen,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            if (phoneDigits.isNotEmpty()) {
                IconButton(
                    onClick = { onPhoneChange("") },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = PayitIcons.Close,
                        contentDescription = "Clear number",
                        tint = colors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 6-digit OTP verification cell row with active focus indicator, spring fill pop, and error highlight.
 */
@Composable
fun OtpSixDigitRow(
    otpCode: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = PayitTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        repeat(6) { index ->
            val char = otpCode.getOrNull(index)?.toString().orEmpty()
            val isFocused = index == otpCode.length && !isError
            val isFilled = char.isNotEmpty()
            val scale by animateFloatAsState(
                targetValue = if (isFilled) 1.03f else 1f,
                animationSpec = spring(stiffness = 500f),
                label = "otpCellScale"
            )
            val borderColor by animateColorAsState(
                targetValue = when {
                    isError -> colors.error
                    isFocused -> colors.primaryAccent
                    isFilled -> colors.primary
                    else -> colors.divider
                },
                animationSpec = tween(180),
                label = "otpCellBorder"
            )
            val containerColor by animateColorAsState(
                targetValue = when {
                    isError -> colors.errorContainer.copy(alpha = 0.4f)
                    isFilled -> colors.primaryContainer.copy(alpha = 0.45f)
                    else -> colors.surface
                },
                animationSpec = tween(180),
                label = "otpCellContainer"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .scale(scale)
                    .background(containerColor, RoundedCornerShape(14.dp))
                    .border(
                        width = if (isFocused || isError || isFilled) 1.75.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isFilled) {
                    Text(
                        text = char,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = if (isError) colors.error else colors.textPrimary
                    )
                } else if (isFocused) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(22.dp)
                            .background(colors.primaryAccent, RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

/**
 * Tactile 3x4 numeric keypad shared by Auth (phone/OTP) and GPay Payments (amount/NPCI PIN).
 */
@Composable
fun GPayNumericKeypad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    modifier: Modifier = Modifier,
    allowDecimal: Boolean = true,
    onClearClick: (() -> Unit)? = null,
    onSubmitClick: (() -> Unit)? = null,
    submitEnabled: Boolean = true,
    keyContainerColor: Color = PayitTheme.colors.surfaceContainer,
    keyContentColor: Color = PayitTheme.colors.textPrimary
) {
    val colors = PayitTheme.colors
    val bottomLeftKey = when {
        onClearClick != null -> "CLEAR"
        allowDecimal -> "."
        else -> ""
    }
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(bottomLeftKey, "0", "BACKSPACE")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    when (key) {
                        "" -> {
                            Spacer(modifier = Modifier.weight(1f).height(52.dp))
                        }
                        "CLEAR" -> {
                            KeypadButton(
                                modifier = Modifier.weight(1f),
                                containerColor = Color.Transparent,
                                onClick = { onClearClick?.invoke() }
                            ) {
                                Text(
                                    text = "Clear",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                    color = colors.textSecondary
                                )
                            }
                        }
                        "BACKSPACE" -> {
                            KeypadButton(
                                modifier = Modifier.weight(1f),
                                containerColor = Color.Transparent,
                                onClick = onBackspaceClick
                            ) {
                                Icon(
                                    imageVector = PayitIcons.Backspace,
                                    contentDescription = "Backspace",
                                    tint = keyContentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        else -> {
                            KeypadButton(
                                modifier = Modifier.weight(1f),
                                containerColor = keyContainerColor,
                                onClick = { onDigitClick(key) }
                            ) {
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 23.sp
                                    ),
                                    color = keyContentColor
                                )
                            }
                        }
                    }
                }
            }
        }
        if (onSubmitClick != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                onClick = { if (submitEnabled) onSubmitClick() },
                enabled = submitEnabled,
                shape = RoundedCornerShape(PayitRadius.pill),
                color = if (submitEnabled) colors.primaryAccent else colors.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = PayitIcons.Check,
                        contentDescription = "Confirm",
                        tint = if (submitEnabled) Color.White else colors.textMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    onClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(PayitRadius.medium),
        color = containerColor,
        modifier = modifier.height(52.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

/**
 * Floating bottom pill dock for one-handed thumb navigation across Home, Scan QR, Rewards & History.
 */
@Composable
fun GPayBottomFloatingDock(
    unscratchedRewardsCount: Int,
    onHomeClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onRewardsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PayitTheme.colors
    val scanGradient = if (colors.isDark) {
        Brush.linearGradient(listOf(Color(0xFF1A73E8), Color(0xFF4C8DF6)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF0B57D0), Color(0xFF1A73E8)))
    }

    Surface(
        shape = RoundedCornerShape(PayitRadius.pill),
        color = colors.surface.copy(alpha = if (colors.isDark) 0.96f else 0.98f),
        border = BorderStroke(1.dp, colors.divider),
        shadowElevation = 10.dp,
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = PayitSpacing.lg, vertical = PayitSpacing.md)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DockNavItem(
                label = "Home",
                icon = PayitIcons.Home,
                selected = true,
                onClick = onHomeClick
            )

            Surface(
                onClick = onScanQrClick,
                shape = RoundedCornerShape(PayitRadius.pill),
                color = Color.Transparent,
                shadowElevation = 4.dp,
                modifier = Modifier.height(44.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(scanGradient)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(
                        imageVector = PayitIcons.QrScanner,
                        contentDescription = "Scan QR",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Scan QR",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
            }

            DockNavItem(
                label = "Rewards",
                icon = PayitIcons.TrophyReward,
                selected = false,
                showBadgeDot = unscratchedRewardsCount > 0,
                onClick = onRewardsClick
            )

            DockNavItem(
                label = "History",
                icon = PayitIcons.History,
                selected = false,
                onClick = onHistoryClick
            )
        }
    }
}

@Composable
private fun DockNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    showBadgeDot: Boolean = false,
    onClick: () -> Unit
) {
    val colors = PayitTheme.colors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(PayitRadius.pill),
        color = if (selected) colors.primaryContainer.copy(alpha = 0.7f) else Color.Transparent
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) colors.onPrimaryContainer else colors.textSecondary,
                    modifier = Modifier.size(19.dp)
                )
                if (showBadgeDot) {
                    Box(
                        modifier = Modifier
                            .offset(x = 3.dp, y = (-2).dp)
                            .size(7.dp)
                            .background(colors.rewardGold, CircleShape)
                            .border(1.dp, colors.surface, CircleShape)
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (selected) colors.onPrimaryContainer else colors.textSecondary
            )
        }
    }
}
