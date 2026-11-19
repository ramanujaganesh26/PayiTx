package com.example.payit.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.payit.model.MerchantOffer
import com.example.payit.model.PayitAppState
import com.example.payit.model.PayitDestination
import com.example.payit.model.RewardCard
import com.example.payit.model.RewardsTab
import com.example.payit.ui.designsystem.GPayAvatarCircle
import com.example.payit.ui.designsystem.GPayTopAppBar
import com.example.payit.ui.designsystem.PayitIcons
import com.example.payit.ui.designsystem.PayitRadius
import com.example.payit.ui.designsystem.PayitSpacing
import com.example.payit.ui.designsystem.formatInr
import com.example.payit.ui.theme.PayitTheme
import com.example.payit.ui.theme.avatarPairAt
import kotlin.math.hypot

@Composable
fun RewardsScreen(
    state: PayitAppState,
    initialTab: RewardsTab,
    onBack: () -> Unit,
    onNavigate: (PayitDestination) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    var activeTab by remember(initialTab) { mutableStateOf(initialTab) }
    var activeScratchCard by remember { mutableStateOf<RewardCard?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
    ) {
        GPayTopAppBar(
            title = "Rewards & offers",
            subtitle = "Cashback & scratch cards",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = PayitSpacing.lg, vertical = PayitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PayitSpacing.md)
        ) {
            item {
                val heroGradient = if (colors.isDark) {
                    Brush.linearGradient(listOf(Color(0xFF1F2B47), Color(0xFF162033)))
                } else {
                    Brush.linearGradient(listOf(Color(0xFFEAF2FF), Color(0xFFF6F9FF)))
                }
                Surface(
                    shape = RoundedCornerShape(PayitRadius.xl),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, colors.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .background(heroGradient)
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = colors.rewardGoldContainer,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = PayitIcons.TrophyReward,
                                    contentDescription = null,
                                    tint = colors.rewardGold,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = formatInr(state.totalCashbackEarned.toDouble()),
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Total rewards earned in bank",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        if (state.unscratchedRewardsCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(PayitRadius.pill),
                                color = colors.primaryAccent
                            ) {
                                Text(
                                    text = "${state.unscratchedRewardsCount} new",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(PayitRadius.pill),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.divider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RewardsTab.entries.forEach { tab ->
                            val selected = activeTab == tab
                            Surface(
                                onClick = { activeTab = tab },
                                shape = RoundedCornerShape(PayitRadius.pill),
                                color = if (selected) colors.surface else Color.Transparent,
                                shadowElevation = if (selected) 2.dp else 0.dp,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tab.label,
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

            if (activeTab == RewardsTab.SCRATCH_CARDS) {
                items(state.rewards.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PayitSpacing.md)
                    ) {
                        pair.forEach { card ->
                            ScratchRewardGridTile(
                                card = card,
                                onClick = { activeScratchCard = card },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                items(state.offers, key = { it.id }) { offer ->
                    OfferListCard(
                        offer = offer,
                        onCopyCode = {
                            clipboard.setText(AnnotatedString(offer.code))
                            onShowMessage("Offer code ${offer.code} copied")
                        },
                        onRedeem = {
                            val merchant = state.contacts.firstOrNull {
                                it.isBusiness && offer.merchantName.contains(it.name, ignoreCase = true)
                            }
                            if (merchant != null) {
                                onNavigate(PayitDestination.PersonChat(merchant.id))
                            } else {
                                onNavigate(PayitDestination.BillsAndRecharges())
                            }
                        }
                    )
                }
            }
        }
    }

    activeScratchCard?.let { card ->
        ScratchCardModalDialog(
            card = card,
            onScratchComplete = {
                val updated = state.scratchCard(card.id)
                if (updated != null) {
                    activeScratchCard = updated
                    onShowMessage("₹${updated.cashbackAmount} cashback credited to ${state.primaryAccount.bankName}!")
                }
            },
            onDismiss = { activeScratchCard = null }
        )
    }
}

@Composable
private fun ScratchRewardGridTile(
    card: RewardCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PayitTheme.colors
    val accentPair = avatarPairAt(card.accentColorIndex)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(PayitRadius.large),
        color = if (card.isScratched) colors.surface else colors.primaryAccent,
        border = BorderStroke(
            width = 1.dp,
            color = if (card.isScratched) colors.divider else colors.primary
        ),
        shadowElevation = if (card.isScratched) 0.dp else 3.dp,
        modifier = modifier.aspectRatio(0.95f)
    ) {
        if (!card.isScratched) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.14f),
                        radius = size.width * 0.45f,
                        center = Offset(size.width * 0.85f, size.height * 0.18f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.10f),
                        radius = size.width * 0.3f,
                        center = Offset(size.width * 0.15f, size.height * 0.85f)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(PayitRadius.pill),
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = "Tap to scratch",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = PayitIcons.Sparkles,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = card.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = card.expiryText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentPair.container,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = PayitIcons.TrophyReward,
                            contentDescription = null,
                            tint = accentPair.content,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "You won",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "₹${card.cashbackAmount}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = card.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.verifiedGreen,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ScratchCardModalDialog(
    card: RewardCard,
    onScratchComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = PayitTheme.colors
    var scratchProgress by remember(card.id) {
        mutableFloatStateOf(if (card.isScratched) 1f else 0f)
    }
    var revealed by remember(card.id) { mutableStateOf(card.isScratched) }
    val animatedFoilAlpha by animateFloatAsState(
        targetValue = if (revealed) 0f else (1f - scratchProgress * 0.7f),
        animationSpec = tween(320),
        label = "foilAlpha"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(PayitRadius.xl),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.divider),
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    onClick = {
                        if (!revealed) {
                            scratchProgress = 1f
                            revealed = true
                            onScratchComplete()
                        }
                    },
                    shape = RoundedCornerShape(PayitRadius.large),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.divider),
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(PayitRadius.large))
                        .pointerInput(card.id, revealed) {
                            if (!revealed) {
                                detectDragGestures { _, dragAmount ->
                                    val delta = hypot(dragAmount.x, dragAmount.y) / 280f
                                    scratchProgress = (scratchProgress + delta).coerceIn(0f, 1f)
                                    if (scratchProgress >= 0.75f && !revealed) {
                                        scratchProgress = 1f
                                        revealed = true
                                        onScratchComplete()
                                    }
                                }
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = colors.rewardGoldContainer,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = PayitIcons.TrophyReward,
                                        contentDescription = null,
                                        tint = colors.rewardGold,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "₹${card.cashbackAmount}",
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Cashback won!",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.verifiedGreen
                            )
                        }

                        AnimatedContent(
                            targetState = revealed,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "foilOverlay"
                        ) { isRevealed ->
                            if (!isRevealed) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(colors.primaryAccent.copy(alpha = animatedFoilAlpha)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = PayitIcons.Sparkles,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "Swipe or tap to scratch",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        )
                                        if (scratchProgress > 0.05f) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            LinearProgressIndicator(
                                                progress = { scratchProgress },
                                                color = Color.White,
                                                trackColor = Color.White.copy(alpha = 0.25f),
                                                modifier = Modifier
                                                    .width(120.dp)
                                                    .height(5.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (!revealed) {
                            scratchProgress = 1f
                            revealed = true
                            onScratchComplete()
                        } else {
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(PayitRadius.pill),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text(if (revealed) "Awesome, done!" else "Reveal reward")
                }
            }
        }
    }
}

@Composable
private fun OfferListCard(
    offer: MerchantOffer,
    onCopyCode: () -> Unit,
    onRedeem: () -> Unit
) {
    val colors = PayitTheme.colors
    Surface(
        shape = RoundedCornerShape(PayitRadius.large),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.divider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GPayAvatarCircle(
                    initials = offer.merchantName.take(2).uppercase(),
                    colorIndex = offer.colorIndex,
                    size = 46.dp,
                    isVerified = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = offer.merchantName,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.textSecondary
                    )
                    Text(
                        text = offer.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(PayitRadius.pill),
                    color = colors.rewardGoldContainer
                ) {
                    Text(
                        text = offer.badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (colors.isDark) colors.rewardGold else Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = offer.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onCopyCode,
                    shape = RoundedCornerShape(PayitRadius.pill),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.divider)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Code: ${offer.code}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.primary
                        )
                        Icon(
                            imageVector = PayitIcons.Copy,
                            contentDescription = "Copy code",
                            tint = colors.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Button(
                    onClick = onRedeem,
                    shape = RoundedCornerShape(PayitRadius.pill),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryAccent,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Pay now",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}
