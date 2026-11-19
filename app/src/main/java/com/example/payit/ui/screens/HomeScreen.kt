package com.example.payit.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payit.model.BankAccount
import com.example.payit.model.BillCategory
import com.example.payit.model.BillItem
import com.example.payit.model.ContactPerson
import com.example.payit.model.EntryMode
import com.example.payit.model.PayitAppState
import com.example.payit.model.PayitDestination
import com.example.payit.model.RewardsTab
import com.example.payit.model.SearchFilter
import com.example.payit.model.TransactionItem
import com.example.payit.ui.designsystem.GPayAvatarCircle
import com.example.payit.ui.designsystem.GPayAvatarGridItem
import com.example.payit.ui.designsystem.GPayBottomFloatingDock
import com.example.payit.ui.designsystem.GPayFloatingSearchBar
import com.example.payit.ui.designsystem.GPayManageMoneyRow
import com.example.payit.ui.designsystem.GPayQuickActionTile
import com.example.payit.ui.designsystem.GPaySectionHeader
import com.example.payit.ui.designsystem.PayitIcons
import com.example.payit.ui.designsystem.PayitRadius
import com.example.payit.ui.designsystem.PayitSpacing
import com.example.payit.ui.designsystem.formatInr
import com.example.payit.ui.theme.PayitTheme
import com.example.payit.ui.theme.ThemeMode
import kotlinx.coroutines.launch

private class PendingHomePayment(
    val title: String,
    val subtitle: String,
    val amount: Double,
    val account: BankAccount,
    val execute: () -> TransactionItem
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: PayitAppState,
    onNavigate: (PayitDestination) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    var peopleExpanded by remember { mutableStateOf(false) }
    var showBalancePeek by remember { mutableStateOf(false) }
    var showUpiLiteSheet by remember { mutableStateOf(false) }
    var showReferralSheet by remember { mutableStateOf(false) }
    var quickPayBill by remember { mutableStateOf<BillItem?>(null) }
    var pendingPayment by remember { mutableStateOf<PendingHomePayment?>(null) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val people = state.contacts.filter { !it.isBusiness }
    val businesses = state.contacts.filter { it.isBusiness }
    val nextDueBill = state.bills.firstOrNull { !it.isPaid }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 112.dp)
        ) {
            // 1. Top Search Bar + Illustrated Skyline Hero Canvas
            item {
                GPayHeroHeader(
                    state = state,
                    onSearchClick = { onNavigate(PayitDestination.SearchOverlay(SearchFilter.ALL)) },
                    onAvatarClick = { onNavigate(PayitDestination.Profile) },
                    onScanQrClick = { onNavigate(PayitDestination.QrScanner) },
                    onToggleTheme = {
                        val nextMode = when (state.themeMode) {
                            ThemeMode.SYSTEM -> if (colors.isDark) ThemeMode.LIGHT else ThemeMode.DARK
                            ThemeMode.LIGHT -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.LIGHT
                        }
                        state.themeMode = nextMode
                        onShowMessage("Switched to ${nextMode.label} theme")
                    }
                )
            }

            // 2. Interactive Neo-Bank Wallet & Quick-Balance Peek Card
            item {
                WalletPeekCard(
                    state = state,
                    showBalance = showBalancePeek,
                    onToggleBalance = { showBalancePeek = !showBalancePeek },
                    onUpiLiteClick = { showUpiLiteSheet = true },
                    onCopyUpiClick = {
                        clipboard.setText(AnnotatedString(state.userHandle))
                        onShowMessage("UPI ID copied: ${state.userHandle}")
                    },
                    modifier = Modifier.padding(horizontal = PayitSpacing.lg)
                )
            }

            // 3. Sculpted 8-Grid Quick Actions Card
            item {
                Spacer(modifier = Modifier.height(PayitSpacing.lg))
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.divider),
                    shadowElevation = if (colors.isDark) 0.dp else 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PayitSpacing.lg)
                ) {
                    Column(modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp)) {
                        Text(
                            text = "Quick Money Transfers & Bills",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        GPayPrimaryActionGrid(
                            onScanQr = { onNavigate(PayitDestination.QrScanner) },
                            onPayContacts = { onNavigate(PayitDestination.SearchOverlay(SearchFilter.PEOPLE)) },
                            onPayPhone = { onNavigate(PayitDestination.PhoneOrUpiEntry(EntryMode.PHONE)) },
                            onBankTransfer = { onNavigate(PayitDestination.BankTransfer) },
                            onPayUpiId = { onNavigate(PayitDestination.PhoneOrUpiEntry(EntryMode.UPI_ID)) },
                            onSelfTransfer = { onNavigate(PayitDestination.SelfTransfer) },
                            onPayBills = { onNavigate(PayitDestination.BillsAndRecharges()) },
                            onMobileRecharge = { onNavigate(PayitDestination.MobileRecharge) }
                        )
                    }
                }
            }

            // 4. People Grid
            item {
                Spacer(modifier = Modifier.height(PayitSpacing.xxl))
                GPaySectionHeader(title = "People")
                Spacer(modifier = Modifier.height(PayitSpacing.md))

                val firstRow = people.take(4)
                val secondRow = people.drop(4).take(3)
                val extraRow = people.drop(7).take(5)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PayitSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(PayitSpacing.md)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PayitSpacing.sm)
                    ) {
                        firstRow.forEach { person ->
                            GPayAvatarGridItem(
                                name = person.name.substringBefore(" "),
                                initials = person.initials,
                                colorIndex = person.avatarColorIndex,
                                hasActivityRing = person.messages.isNotEmpty(),
                                onClick = { onNavigate(PayitDestination.PersonChat(person.id)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PayitSpacing.sm)
                    ) {
                        secondRow.forEach { person ->
                            GPayAvatarGridItem(
                                name = person.name.substringBefore(" "),
                                initials = person.initials,
                                colorIndex = person.avatarColorIndex,
                                hasActivityRing = person.messages.isNotEmpty(),
                                onClick = { onNavigate(PayitDestination.PersonChat(person.id)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (!peopleExpanded && extraRow.isNotEmpty()) {
                            MoreLessToggleItem(
                                expanded = false,
                                onClick = { peopleExpanded = true },
                                modifier = Modifier.weight(1f)
                            )
                        } else if (peopleExpanded && extraRow.isNotEmpty()) {
                            val fourthInSecondRow = extraRow.first()
                            GPayAvatarGridItem(
                                name = fourthInSecondRow.name.substringBefore(" "),
                                initials = fourthInSecondRow.initials,
                                colorIndex = fourthInSecondRow.avatarColorIndex,
                                hasActivityRing = fourthInSecondRow.messages.isNotEmpty(),
                                onClick = { onNavigate(PayitDestination.PersonChat(fourthInSecondRow.id)) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    AnimatedVisibility(
                        visible = peopleExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        val remaining = extraRow.drop(1)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(PayitSpacing.sm)
                        ) {
                            remaining.take(3).forEach { person ->
                                GPayAvatarGridItem(
                                    name = person.name.substringBefore(" "),
                                    initials = person.initials,
                                    colorIndex = person.avatarColorIndex,
                                    hasActivityRing = person.messages.isNotEmpty(),
                                    onClick = { onNavigate(PayitDestination.PersonChat(person.id)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat((3 - remaining.take(3).size).coerceAtLeast(0)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            MoreLessToggleItem(
                                expanded = true,
                                onClick = { peopleExpanded = false },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 5. Businesses Grid + Featured Merchant Spotlight Carousel
            item {
                Spacer(modifier = Modifier.height(PayitSpacing.section))
                GPaySectionHeader(
                    title = "Businesses",
                    actionLabel = "Explore",
                    onActionClick = { onNavigate(PayitDestination.SearchOverlay(SearchFilter.BUSINESSES)) }
                )
                Spacer(modifier = Modifier.height(PayitSpacing.md))

                FeaturedMerchantSpotlightRow(
                    onMerchantClick = { contactId ->
                        onNavigate(PayitDestination.PersonChat(contactId))
                    }
                )
                Spacer(modifier = Modifier.height(PayitSpacing.lg))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PayitSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(PayitSpacing.md)
                ) {
                    businesses.chunked(4).take(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(PayitSpacing.sm)
                        ) {
                            rowItems.forEach { merchant ->
                                GPayAvatarGridItem(
                                    name = merchant.name,
                                    initials = merchant.initials,
                                    colorIndex = merchant.avatarColorIndex,
                                    badgeText = merchant.cashbackTag,
                                    isVerified = true,
                                    onClick = { onNavigate(PayitDestination.PersonChat(merchant.id)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(4 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

        // 6. Bills & Recharges Section
        item {
            Spacer(modifier = Modifier.height(PayitSpacing.section))
            GPaySectionHeader(
                title = "Bills & recharges",
                actionLabel = "See all",
                onActionClick = { onNavigate(PayitDestination.BillsAndRecharges()) }
            )
            Spacer(modifier = Modifier.height(PayitSpacing.md))

            if (nextDueBill != null) {
                DueBillReminderCard(
                    bill = nextDueBill,
                    onPayClick = { quickPayBill = nextDueBill },
                    modifier = Modifier.padding(horizontal = PayitSpacing.lg)
                )
                Spacer(modifier = Modifier.height(PayitSpacing.lg))
            }

            BillCategoryGrid(
                onCategoryClick = { category ->
                    if (category == BillCategory.MOBILE_RECHARGE) {
                        onNavigate(PayitDestination.MobileRecharge)
                    } else {
                        onNavigate(PayitDestination.BillsAndRecharges(category))
                    }
                }
            )
        }

        // 7. Offers & Rewards Section
        item {
            Spacer(modifier = Modifier.height(PayitSpacing.section))
            GPaySectionHeader(title = "Offers & rewards")
            Spacer(modifier = Modifier.height(PayitSpacing.md))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayitSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(PayitSpacing.md)
            ) {
                PromotionFeatureCircle(
                    title = "Rewards",
                    icon = PayitIcons.TrophyReward,
                    badgeText = if (state.unscratchedRewardsCount > 0) "${state.unscratchedRewardsCount} new" else null,
                    containerColor = colors.rewardGoldContainer,
                    iconTint = colors.rewardGold,
                    onClick = { onNavigate(PayitDestination.Rewards(RewardsTab.SCRATCH_CARDS)) },
                    modifier = Modifier.weight(1f)
                )
                PromotionFeatureCircle(
                    title = "Offers",
                    icon = PayitIcons.OfferTag,
                    badgeText = "${state.offers.size} live",
                    containerColor = colors.primaryContainer,
                    iconTint = colors.onPrimaryContainer,
                    onClick = { onNavigate(PayitDestination.Rewards(RewardsTab.OFFERS)) },
                    modifier = Modifier.weight(1f)
                )
                PromotionFeatureCircle(
                    title = "Referrals",
                    icon = PayitIcons.GiftReferral,
                    badgeText = "₹201",
                    containerColor = colors.verifiedGreenContainer,
                    iconTint = colors.verifiedGreen,
                    onClick = { showReferralSheet = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 8. Promotional RuPay Credit Card on UPI Banner
        item {
            Spacer(modifier = Modifier.height(PayitSpacing.section))
            RuPayPromoBannerCard(
                onClick = { onNavigate(PayitDestination.CardsAndBanks) },
                modifier = Modifier.padding(horizontal = PayitSpacing.lg)
            )
        }

        // 9. Manage Your Money Section
        item {
            Spacer(modifier = Modifier.height(PayitSpacing.section))
            GPaySectionHeader(title = "Manage your money")
            Spacer(modifier = Modifier.height(PayitSpacing.md))

            Surface(
                shape = RoundedCornerShape(PayitRadius.large),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.divider),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayitSpacing.lg)
            ) {
                Column {
                    GPayManageMoneyRow(
                        icon = PayitIcons.SpeedometerCibil,
                        title = "Check your CIBIL score for free",
                        subtitle = "Updated monthly • Zero credit impact",
                        statusBadge = "${state.cibilScore} • Free",
                        statusBadgeHighlight = true,
                        onClick = { onNavigate(PayitDestination.CibilScore) }
                    )
                    HorizontalDivider(
                        color = colors.divider.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = PayitSpacing.lg)
                    )
                    GPayManageMoneyRow(
                        icon = PayitIcons.History,
                        title = "See transaction history",
                        subtitle = "${state.transactions.size} recent payments & credits",
                        onClick = { onNavigate(PayitDestination.TransactionHistory) }
                    )
                    HorizontalDivider(
                        color = colors.divider.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = PayitSpacing.lg)
                    )
                    GPayManageMoneyRow(
                        icon = PayitIcons.BalanceBank,
                        title = "Check bank balance",
                        subtitle = "${state.primaryAccount.bankName} ${state.primaryAccount.maskedNumber}",
                        onClick = { onNavigate(PayitDestination.CheckBalance) }
                    )
                    HorizontalDivider(
                        color = colors.divider.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = PayitSpacing.lg)
                    )
                    GPayManageMoneyRow(
                        icon = PayitIcons.CreditCard,
                        title = "Manage cards & bank accounts",
                        subtitle = "${state.bankAccounts.size} linked accounts & RuPay UPI",
                        onClick = { onNavigate(PayitDestination.CardsAndBanks) }
                    )
                }
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }

        // Floating Bottom Scan & Navigation Dock
        GPayBottomFloatingDock(
            unscratchedRewardsCount = state.unscratchedRewardsCount,
            onHomeClick = { scope.launch { listState.animateScrollToItem(0) } },
            onScanQrClick = { onNavigate(PayitDestination.QrScanner) },
            onRewardsClick = { onNavigate(PayitDestination.Rewards(RewardsTab.SCRATCH_CARDS)) },
            onHistoryClick = { onNavigate(PayitDestination.TransactionHistory) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showUpiLiteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showUpiLiteSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            UpiLiteTopUpSheetContent(
                state = state,
                onTopUp = { amount ->
                    val primary = state.primaryAccount
                    if (state.upiLiteBalance >= state.upiLiteMaxLimit) {
                        onShowMessage("UPI Lite wallet is already at ₹2,000 limit")
                    } else if (primary.balance >= amount) {
                        showUpiLiteSheet = false
                        pendingPayment = PendingHomePayment(
                            title = "UPI Lite Top-Up",
                            subtitle = "On-device wallet • ${primary.bankName} ${primary.maskedNumber}",
                            amount = amount,
                            account = primary,
                            execute = { state.topUpUpiLite(amount, primary) }
                        )
                    } else {
                        onShowMessage("Insufficient balance in ${primary.bankName}")
                    }
                },
                onManageClick = {
                    showUpiLiteSheet = false
                    onNavigate(PayitDestination.CardsAndBanks)
                }
            )
        }
    }

    if (showReferralSheet) {
        ModalBottomSheet(
            onDismissRequest = { showReferralSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            ReferralSheetContent(
                onCopyCode = {
                    clipboard.setText(AnnotatedString("PAYIT201"))
                    showReferralSheet = false
                    onShowMessage("Referral code PAYIT201 copied")
                },
                onClose = { showReferralSheet = false }
            )
        }
    }

    quickPayBill?.let { bill ->
        ModalBottomSheet(
            onDismissRequest = { quickPayBill = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            QuickBillConfirmSheet(
                bill = bill,
                account = state.primaryAccount,
                onConfirmPay = {
                    if (state.primaryAccount.balance >= bill.amount) {
                        val payer = state.primaryAccount
                        quickPayBill = null
                        pendingPayment = PendingHomePayment(
                            title = bill.billerName,
                            subtitle = "${bill.category} • Consumer #${bill.consumerNumber}",
                            amount = bill.amount,
                            account = payer,
                            execute = { state.payBill(bill, payer) }
                        )
                    } else {
                        onShowMessage("Insufficient balance in ${state.primaryAccount.bankName}")
                    }
                },
                onViewAllBills = {
                    quickPayBill = null
                    onNavigate(PayitDestination.BillsAndRecharges(bill.iconType))
                }
            )
        }
    }

    pendingPayment?.let { req ->
        UniversalPaymentFlowDialog(
            title = req.title,
            subtitle = req.subtitle,
            amount = req.amount,
            account = req.account,
            onExecutePayment = req.execute,
            onFinish = { pendingPayment = null },
            onShowMessage = onShowMessage
        )
    }
}

@Composable
private fun GPayHeroHeader(
    state: PayitAppState,
    onSearchClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onScanQrClick: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val colors = PayitTheme.colors
    val gradientStart = if (colors.isDark) Color(0xFF13223F) else Color(0xFFE7F0FE)
    val gradientEnd = colors.background

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(gradientStart, gradientEnd)
                )
            )
    ) {
        // Multi-layered Horizon / Skyline Vector Illustration
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(242.dp)
        ) {
            val w = size.width
            val h = size.height
            val circleColor = colors.primaryAccent.copy(alpha = if (colors.isDark) 0.16f else 0.10f)
            val secondaryCircleColor = colors.verifiedGreen.copy(alpha = if (colors.isDark) 0.11f else 0.07f)
            val hillBackColor = colors.primaryAccent.copy(alpha = if (colors.isDark) 0.10f else 0.06f)
            val hillFrontColor = colors.primaryContainer.copy(alpha = if (colors.isDark) 0.22f else 0.36f)

            // Sun / Moon orb
            drawCircle(
                color = circleColor,
                radius = w * 0.36f,
                center = Offset(w * 0.86f, h * 0.24f)
            )
            drawCircle(
                color = circleColor,
                radius = w * 0.24f,
                center = Offset(w * 0.86f, h * 0.24f),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = secondaryCircleColor,
                radius = w * 0.22f,
                center = Offset(w * 0.12f, h * 0.65f)
            )

            // Rolling hills horizon
            val backHill = Path().apply {
                moveTo(0f, h * 0.82f)
                quadraticTo(w * 0.28f, h * 0.64f, w * 0.56f, h * 0.78f)
                quadraticTo(w * 0.80f, h * 0.90f, w, h * 0.70f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = backHill, color = hillBackColor)

            val frontHill = Path().apply {
                moveTo(0f, h * 0.90f)
                quadraticTo(w * 0.35f, h * 0.80f, w * 0.68f, h * 0.88f)
                quadraticTo(w * 0.88f, h * 0.94f, w, h * 0.84f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = frontHill, color = hillFrontColor)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = PayitSpacing.sm, bottom = PayitSpacing.md)
        ) {
            GPayFloatingSearchBar(
                userInitials = state.userInitials,
                onSearchClick = onSearchClick,
                onAvatarClick = onAvatarClick,
                showThemeQuickToggle = true,
                onThemeToggleClick = onToggleTheme
            )

            Spacer(modifier = Modifier.height(PayitSpacing.lg))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayitSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(PayitRadius.pill),
                        color = colors.primaryContainer.copy(alpha = 0.85f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = PayitIcons.VerifiedShield,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "PAYIT • BHIM UPI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.6.sp
                                ),
                                color = colors.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Tap, scan & pay\nanyone instantly",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 29.sp
                        ),
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        onClick = onScanQrClick,
                        shape = RoundedCornerShape(PayitRadius.pill),
                        color = colors.primaryAccent,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = PayitIcons.QrScanner,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Scan any QR",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    onClick = onScanQrClick,
                    shape = RoundedCornerShape(PayitRadius.large),
                    color = colors.surface.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, colors.divider),
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(width = 110.dp, height = 112.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = colors.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = PayitIcons.QrCode,
                                    contentDescription = "QR Pay",
                                    tint = colors.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Text(
                            text = "UPI Instant",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Zero fees",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.verifiedGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletPeekCard(
    state: PayitAppState,
    showBalance: Boolean,
    onToggleBalance: () -> Unit,
    onUpiLiteClick: () -> Unit,
    onCopyUpiClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PayitTheme.colors
    val primary = state.primaryAccount

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.divider),
        shadowElevation = if (colors.isDark) 0.dp else 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${primary.bankName} ${primary.maskedNumber}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    AnimatedContent(
                        targetState = showBalance,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "balancePeek"
                    ) { visible ->
                        Text(
                            text = if (visible) {
                                formatInr(primary.balance, showDecimals = true)
                            } else {
                                "₹ ••••••"
                            },
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                    }
                }

                Surface(
                    onClick = onToggleBalance,
                    shape = RoundedCornerShape(PayitRadius.pill),
                    color = colors.primaryContainer.copy(alpha = 0.75f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (showBalance) PayitIcons.EyeOff else PayitIcons.Eye,
                            contentDescription = if (showBalance) "Hide balance" else "Show balance",
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (showBalance) "Hide" else "Show balance",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = colors.divider.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = onUpiLiteClick,
                    shape = RoundedCornerShape(PayitRadius.pill),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.divider)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = PayitIcons.UpiLiteBolt,
                            contentDescription = null,
                            tint = colors.rewardGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "UPI Lite: ${formatInr(state.upiLiteBalance)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Top up",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.primary
                        )
                    }
                }

                Surface(
                    onClick = onCopyUpiClick,
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
                            text = state.userHandle,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary
                        )
                        Icon(
                            imageVector = PayitIcons.Copy,
                            contentDescription = "Copy UPI ID",
                            tint = colors.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedMerchantSpotlightRow(
    onMerchantClick: (String) -> Unit
) {
    val colors = PayitTheme.colors
    val spotlights = listOf(
        Triple("b_starbucks", "Starbucks", "5% Cashback on UPI Lite") to Color(0xFF00704A),
        Triple("b_zomato", "Zomato", "Flat ₹50 Off above ₹249") to Color(0xFFE23744),
        Triple("b_swiggy", "Swiggy", "10% Back on Instamart") to Color(0xFFFC8019)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = PayitSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        spotlights.forEach { (info, brandColor) ->
            val (contactId, name, offerText) = info
            Surface(
                onClick = { onMerchantClick(contactId) },
                shape = RoundedCornerShape(PayitRadius.large),
                color = colors.surface,
                border = BorderStroke(1.dp, brandColor.copy(alpha = 0.28f)),
                shadowElevation = if (colors.isDark) 0.dp else 1.dp,
                modifier = Modifier.width(196.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = brandColor,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = name.take(2).uppercase(),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = offerText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (colors.isDark) colors.rewardGold else brandColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GPayPrimaryActionGrid(
    onScanQr: () -> Unit,
    onPayContacts: () -> Unit,
    onPayPhone: () -> Unit,
    onBankTransfer: () -> Unit,
    onPayUpiId: () -> Unit,
    onSelfTransfer: () -> Unit,
    onPayBills: () -> Unit,
    onMobileRecharge: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PayitSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(PayitSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PayitSpacing.xs)
        ) {
            GPayQuickActionTile(
                label = "Scan any\nQR code",
                icon = PayitIcons.QrScanner,
                highlighted = true,
                onClick = onScanQr,
                modifier = Modifier.weight(1f)
            )
            GPayQuickActionTile(
                label = "Pay\ncontacts",
                icon = PayitIcons.PayContacts,
                onClick = onPayContacts,
                modifier = Modifier.weight(1f)
            )
            GPayQuickActionTile(
                label = "Pay phone\nnumber",
                icon = PayitIcons.PayPhone,
                onClick = onPayPhone,
                modifier = Modifier.weight(1f)
            )
            GPayQuickActionTile(
                label = "Bank\ntransfer",
                icon = PayitIcons.BankTransfer,
                onClick = onBankTransfer,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PayitSpacing.xs)
        ) {
            GPayQuickActionTile(
                label = "Pay UPI ID\nor number",
                icon = PayitIcons.UpiAt,
                onClick = onPayUpiId,
                modifier = Modifier.weight(1f)
            )
            GPayQuickActionTile(
                label = "Self\ntransfer",
                icon = PayitIcons.SelfTransfer,
                onClick = onSelfTransfer,
                modifier = Modifier.weight(1f)
            )
            GPayQuickActionTile(
                label = "Pay\nbills",
                icon = PayitIcons.PayBills,
                onClick = onPayBills,
                modifier = Modifier.weight(1f)
            )
            GPayQuickActionTile(
                label = "Mobile\nrecharge",
                icon = PayitIcons.MobileRecharge,
                onClick = onMobileRecharge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MoreLessToggleItem(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PayitTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(PayitRadius.medium))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = PayitSpacing.xs, horizontal = PayitSpacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = colors.surfaceContainer,
            border = BorderStroke(1.dp, colors.divider),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (expanded) PayitIcons.ChevronUp else PayitIcons.ChevronDown,
                    contentDescription = if (expanded) "Show less" else "Show more",
                    tint = colors.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (expanded) "Less" else "More",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DueBillReminderCard(
    bill: BillItem,
    onPayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PayitTheme.colors
    Surface(
        shape = RoundedCornerShape(PayitRadius.large),
        color = colors.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (bill.isOverdue) colors.error.copy(alpha = 0.4f) else colors.divider
        ),
        shadowElevation = if (colors.isDark) 0.dp else 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PayitSpacing.lg, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (bill.isOverdue) colors.errorContainer else colors.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = iconForBillCategory(bill.iconType),
                        contentDescription = null,
                        tint = if (bill.isOverdue) colors.error else colors.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.billerName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${formatInr(bill.amount)} • ${bill.dueDateText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (bill.isOverdue) colors.error else colors.textSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onPayClick,
                shape = RoundedCornerShape(PayitRadius.pill),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryAccent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Text(
                    text = "Pay",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun BillCategoryGrid(
    onCategoryClick: (BillCategory) -> Unit
) {
    val categories = BillCategory.entries
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PayitSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(PayitSpacing.md)
    ) {
        categories.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PayitSpacing.xs)
            ) {
                row.forEach { cat ->
                    GPayQuickActionTile(
                        label = cat.label,
                        icon = iconForBillCategory(cat),
                        onClick = { onCategoryClick(cat) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

fun iconForBillCategory(category: BillCategory): ImageVector = when (category) {
    BillCategory.MOBILE_RECHARGE -> PayitIcons.MobileRecharge
    BillCategory.ELECTRICITY -> PayitIcons.Electricity
    BillCategory.DTH -> PayitIcons.DthTv
    BillCategory.FASTAG -> PayitIcons.Fastag
    BillCategory.CREDIT_CARD -> PayitIcons.CreditCard
    BillCategory.BROADBAND -> PayitIcons.Broadband
    BillCategory.PIPED_GAS -> PayitIcons.PipedGas
    BillCategory.WATER -> PayitIcons.WaterDrop
}

@Composable
private fun PromotionFeatureCircle(
    title: String,
    icon: ImageVector,
    badgeText: String?,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PayitTheme.colors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(PayitRadius.large),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.divider),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = containerColor,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = colors.textPrimary,
                maxLines = 1
            )
            if (!badgeText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.primary
                )
            }
        }
    }
}

@Composable
private fun RuPayPromoBannerCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PayitTheme.colors
    val cardGradient = if (colors.isDark) {
        Brush.linearGradient(listOf(Color(0xFF172542), Color(0xFF1E293B)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFECF3FE), Color(0xFFF5F8FF)))
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(PayitRadius.large),
        color = Color.Transparent,
        border = BorderStroke(1.dp, colors.primaryContainer),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .background(cardGradient)
                .padding(PayitSpacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colors.primaryAccent,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = PayitIcons.CreditCard,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "RuPay Credit Cards on UPI",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Link & earn 2% cashback on merchant scans",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(PayitRadius.pill),
                color = colors.surface
            ) {
                Text(
                    text = "Manage",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun UpiLiteTopUpSheetContent(
    state: PayitAppState,
    onTopUp: (Double) -> Unit,
    onManageClick: () -> Unit
) {
    val colors = PayitTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PayitSpacing.xl)
            .padding(bottom = 28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = colors.rewardGoldContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = PayitIcons.UpiLiteBolt,
                        contentDescription = null,
                        tint = colors.rewardGold,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "UPI Lite On-Device Wallet",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
                Text(
                    text = "Instant on-device payments up to ₹500",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Surface(
            shape = RoundedCornerShape(PayitRadius.medium),
            color = colors.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Text(
                        text = formatInr(state.upiLiteBalance, showDecimals = true),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                }
                Text(
                    text = "Max ${formatInr(state.upiLiteMaxLimit)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Add money from ${state.primaryAccount.bankName} ${state.primaryAccount.maskedNumber}",
            style = MaterialTheme.typography.labelLarge,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(200.0, 500.0, 1000.0).forEach { amt ->
                OutlinedButton(
                    onClick = { onTopUp(amt) },
                    shape = RoundedCornerShape(PayitRadius.pill),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "+${formatInr(amt)}")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onManageClick,
            shape = RoundedCornerShape(PayitRadius.pill),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = "Manage Wallet & Banks")
        }
    }
}

@Composable
private fun ReferralSheetContent(
    onCopyCode: () -> Unit,
    onClose: () -> Unit
) {
    val colors = PayitTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PayitSpacing.xl)
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = colors.verifiedGreenContainer,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = PayitIcons.GiftReferral,
                    contentDescription = null,
                    tint = colors.verifiedGreen,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Invite friends, earn ₹201 each",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "When your friend makes their first UPI payment on Payit, you get ₹201 and they get ₹21 directly in their bank.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        Surface(
            shape = RoundedCornerShape(PayitRadius.medium),
            color = colors.surfaceContainer,
            border = BorderStroke(1.dp, colors.divider),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "YOUR REFERRAL CODE",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                    Text(
                        text = "PAYIT201",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = colors.primary
                    )
                }
                Button(
                    onClick = onCopyCode,
                    shape = RoundedCornerShape(PayitRadius.pill),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent)
                ) {
                    Text("Copy code")
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onClose,
            shape = RoundedCornerShape(PayitRadius.pill),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }
    }
}

@Composable
private fun QuickBillConfirmSheet(
    bill: BillItem,
    account: BankAccount,
    onConfirmPay: () -> Unit,
    onViewAllBills: () -> Unit
) {
    val colors = PayitTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PayitSpacing.xl)
            .padding(bottom = 28.dp)
    ) {
        Text(
            text = "Quick Bill Pay",
            style = MaterialTheme.typography.labelLarge,
            color = colors.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = bill.billerName,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary
        )
        Text(
            text = "${bill.category} • Consumer #${bill.consumerNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(18.dp))
        Surface(
            shape = RoundedCornerShape(PayitRadius.medium),
            color = colors.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bill Amount",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Text(
                        text = formatInr(bill.amount, showDecimals = true),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(PayitRadius.pill),
                    color = colors.surface
                ) {
                    Text(
                        text = bill.dueDateText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (bill.isOverdue) colors.error else colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Paying from ${account.bankName} ${account.maskedNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onConfirmPay,
            shape = RoundedCornerShape(PayitRadius.pill),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryAccent,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Pay ${formatInr(bill.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onViewAllBills,
            shape = RoundedCornerShape(PayitRadius.pill),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Text("View all billers")
        }
    }
}

/**
 * Full-screen Search & Quick Pay Overlay triggered from GPay search bar, "Pay contacts", or "Explore".
 */
@Composable
fun SearchOverlayScreen(
    state: PayitAppState,
    initialFilter: SearchFilter,
    onBack: () -> Unit,
    onNavigate: (PayitDestination) -> Unit
) {
    val colors = PayitTheme.colors
    var query by remember { mutableStateOf("") }
    var selectedFilter by remember(initialFilter) { mutableStateOf(initialFilter) }

    val filteredContacts = remember(query, selectedFilter, state.contacts.size) {
        val q = query.trim().lowercase()
        state.contacts.filter { contact ->
            val matchesFilter = when (selectedFilter) {
                SearchFilter.ALL -> true
                SearchFilter.PEOPLE -> !contact.isBusiness
                SearchFilter.BUSINESSES -> contact.isBusiness
                SearchFilter.BILLS -> false
            }
            val matchesQuery = q.isBlank() ||
                contact.name.lowercase().contains(q) ||
                contact.upiId.lowercase().contains(q) ||
                contact.phone.lowercase().contains(q)
            matchesFilter && matchesQuery
        }
    }

    val filteredBills = remember(query, selectedFilter, state.bills.size) {
        if (selectedFilter == SearchFilter.PEOPLE || selectedFilter == SearchFilter.BUSINESSES) {
            emptyList()
        } else {
            val q = query.trim().lowercase()
            state.bills.filter { bill ->
                q.isBlank() ||
                    bill.billerName.lowercase().contains(q) ||
                    bill.category.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PayitSpacing.md, vertical = PayitSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(PayitRadius.pill),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.divider),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = PayitIcons.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = "Pay name, phone, UPI ID, or biller",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary),
                            cursorBrush = SolidColor(colors.primaryAccent),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector = PayitIcons.Close,
                                contentDescription = "Clear",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = PayitSpacing.lg, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchFilter.entries.forEach { filter ->
                val selected = selectedFilter == filter
                FilterChip(
                    selected = selected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primaryContainer,
                        selectedLabelColor = colors.onPrimaryContainer
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = PayitSpacing.sm)
        ) {
            val trimmed = query.trim()
            if (trimmed.contains("@") && trimmed.length >= 5) {
                item {
                    Surface(
                        onClick = {
                            val target = state.findOrCreateContact(
                                name = trimmed.substringBefore("@"),
                                upiId = trimmed
                            )
                            onNavigate(PayitDestination.SendMoney(target.id))
                        },
                        color = colors.primaryContainer.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(PayitRadius.medium),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PayitSpacing.lg, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = PayitIcons.UpiAt,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pay UPI ID \"$trimmed\"",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.onPrimaryContainer
                                )
                                Text(
                                    text = "Tap to verify & enter amount",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            Icon(
                                imageVector = PayitIcons.ChevronRight,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer
                            )
                        }
                    }
                }
            } else if (trimmed.all { it.isDigit() } && trimmed.length == 10) {
                item {
                    Surface(
                        onClick = {
                            val target = state.findOrCreateContact(
                                name = "+91 $trimmed",
                                upiId = "$trimmed@okpayit",
                                phone = "+91 $trimmed"
                            )
                            onNavigate(PayitDestination.SendMoney(target.id))
                        },
                        color = colors.primaryContainer.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(PayitRadius.medium),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PayitSpacing.lg, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = PayitIcons.PayPhone,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pay +91 $trimmed",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.onPrimaryContainer
                                )
                                Text(
                                    text = "Send via UPI phone number",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            Icon(
                                imageVector = PayitIcons.ChevronRight,
                                contentDescription = null,
                                tint = colors.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            if (filteredContacts.isNotEmpty()) {
                item {
                    Text(
                        text = if (selectedFilter == SearchFilter.BUSINESSES) "Merchants & Businesses" else "Contacts & Businesses",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = PayitSpacing.lg, vertical = 8.dp)
                    )
                }
                items(filteredContacts, key = { it.id }) { contact ->
                    SearchContactRow(
                        contact = contact,
                        onClick = { onNavigate(PayitDestination.PersonChat(contact.id)) }
                    )
                }
            }

            if (filteredBills.isNotEmpty()) {
                item {
                    Text(
                        text = "Billers",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = PayitSpacing.lg, vertical = 10.dp)
                    )
                }
                items(filteredBills, key = { it.id }) { bill ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(PayitDestination.BillsAndRecharges(bill.iconType)) }
                            .padding(horizontal = PayitSpacing.lg, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = colors.surfaceContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = iconForBillCategory(bill.iconType),
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bill.billerName,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${bill.category} • ${bill.consumerNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        Text(
                            text = if (bill.isPaid) "Paid" else formatInr(bill.amount),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (bill.isPaid) colors.verifiedGreen else colors.textPrimary
                        )
                    }
                }
            }

            if (filteredContacts.isEmpty() && filteredBills.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = PayitIcons.Search,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No matching contacts or billers",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enter a 10-digit mobile number or UPI ID (e.g. name@okbank) to pay directly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchContactRow(
    contact: ContactPerson,
    onClick: () -> Unit
) {
    val colors = PayitTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = PayitSpacing.lg, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GPayAvatarCircle(
            initials = contact.initials,
            colorIndex = contact.avatarColorIndex,
            size = 46.dp,
            isVerified = contact.isBusiness
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )
            Text(
                text = if (contact.isBusiness) contact.upiId else contact.phone,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        if (!contact.cashbackTag.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(PayitRadius.pill),
                color = colors.rewardGoldContainer
            ) {
                Text(
                    text = contact.cashbackTag,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (colors.isDark) colors.rewardGold else Color(0xFF92400E),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
