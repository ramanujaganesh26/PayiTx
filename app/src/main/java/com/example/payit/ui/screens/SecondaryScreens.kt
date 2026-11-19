package com.example.payit.ui.screens

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.payit.model.BankAccount
import com.example.payit.model.BillCategory
import com.example.payit.model.BillItem
import com.example.payit.model.EntryMode
import com.example.payit.model.PayitAppState
import com.example.payit.model.PayitDestination
import com.example.payit.model.RechargePlan
import com.example.payit.model.RewardsTab
import com.example.payit.model.TransactionItem
import com.example.payit.ui.designsystem.GPayAvatarCircle
import com.example.payit.ui.designsystem.GPayManageMoneyRow
import com.example.payit.ui.designsystem.GPayQrCodeView
import com.example.payit.ui.designsystem.GPayTopAppBar
import com.example.payit.ui.designsystem.PayitIcons
import com.example.payit.ui.designsystem.PayitRadius
import com.example.payit.ui.designsystem.PayitSpacing
import com.example.payit.ui.designsystem.formatInr
import com.example.payit.ui.theme.PayitTheme
import com.example.payit.ui.theme.ThemeMode

private class PendingSecondaryPayment(
    val title: String,
    val subtitle: String,
    val amount: Double,
    val account: BankAccount,
    val execute: () -> TransactionItem
)

// ============================================================================
// 1. TRANSACTION HISTORY SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    state: PayitAppState,
    onBack: () -> Unit,
    onNavigate: (PayitDestination) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var selectedTx by remember { mutableStateOf<TransactionItem?>(null) }

    val filters = listOf("All", "Sent", "Received", "Bills", "UPI Lite")

    val filtered = remember(query, selectedFilter, state.transactions.size) {
        val q = query.trim().lowercase()
        state.transactions.filter { tx ->
            val matchesFilter = when (selectedFilter) {
                "All" -> true
                "Sent" -> !tx.isCredit && tx.category != "Bills"
                "Received" -> tx.isCredit
                else -> tx.category == selectedFilter
            }
            val matchesQuery = q.isBlank() ||
                tx.title.lowercase().contains(q) ||
                tx.subtitle.lowercase().contains(q) ||
                tx.upiRefId.contains(q)
            matchesFilter && matchesQuery
        }
    }

    val totalInflow = state.transactions.filter { it.isCredit }.sumOf { it.amount }
    val totalOutflow = state.transactions.filter { !it.isCredit }.sumOf { it.amount }
    val grouped = remember(filtered) { filtered.groupBy { it.dateGroup } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
    ) {
        GPayTopAppBar(
            title = "Transaction history",
            subtitle = "${state.transactions.size} transactions",
            onBack = onBack
        )

        Surface(
            shape = RoundedCornerShape(PayitRadius.pill),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.divider),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PayitSpacing.lg)
                .height(46.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = PayitIcons.Search,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search name, note, or UPI Ref ID",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                        cursorBrush = SolidColor(colors.primaryAccent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = PayitIcons.Close,
                            contentDescription = "Clear",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = PayitSpacing.lg, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primaryContainer,
                        selectedLabelColor = colors.onPrimaryContainer
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(PayitRadius.large),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.divider),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PayitSpacing.lg, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Money received",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+${formatInr(totalInflow)}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.verifiedGreen
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(42.dp)
                                .background(colors.divider)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Money spent",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatInr(totalOutflow),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }

            if (grouped.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = PayitIcons.History,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No transactions match your filter",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )
                    }
                }
            } else {
                grouped.forEach { (groupName, txList) ->
                    item(key = "header_$groupName") {
                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textSecondary,
                            modifier = Modifier.padding(
                                start = PayitSpacing.lg,
                                end = PayitSpacing.lg,
                                top = 16.dp,
                                bottom = 6.dp
                            )
                        )
                    }
                    items(txList, key = { it.id }) { tx ->
                        TransactionRowItem(
                            tx = tx,
                            onClick = { selectedTx = tx }
                        )
                    }
                }
            }
        }
    }

    selectedTx?.let { tx ->
        ModalBottomSheet(
            onDismissRequest = { selectedTx = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayitSpacing.xl)
                    .padding(bottom = 28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GPayAvatarCircle(
                        initials = tx.initials,
                        colorIndex = tx.avatarColorIndex,
                        size = 48.dp,
                        isVerified = true
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tx.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary
                        )
                        Text(
                            text = tx.timestamp,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(PayitRadius.pill),
                        color = colors.verifiedGreenContainer
                    ) {
                        Text(
                            text = tx.status,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (colors.isDark) colors.verifiedGreen else Color(0xFF0A4D1F),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (tx.isCredit) "+${formatInr(tx.amount, true)}" else formatInr(tx.amount, true),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = if (tx.isCredit) colors.verifiedGreen else colors.textPrimary
                )
                Text(
                    text = tx.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(PayitRadius.medium),
                    color = colors.surfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KeyValueRow("UPI ID", tx.upiId)
                        KeyValueRow("Bank / Wallet", tx.bankUsed)
                        KeyValueRow("Category", tx.category)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("UPI Ref ID", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            Row(
                                modifier = Modifier.clickable {
                                    clipboard.setText(AnnotatedString(tx.upiRefId))
                                    onShowMessage("UPI Ref ID copied: ${tx.upiRefId}")
                                },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = tx.upiRefId,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.primary
                                )
                                Icon(
                                    imageVector = PayitIcons.Copy,
                                    contentDescription = "Copy",
                                    tint = colors.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                val matchingContact = state.contacts.firstOrNull {
                    it.upiId.equals(tx.upiId, ignoreCase = true) || it.name.equals(tx.title, ignoreCase = true)
                }
                if (matchingContact != null) {
                    Button(
                        onClick = {
                            selectedTx = null
                            onNavigate(
                                PayitDestination.SendMoney(
                                    contactId = matchingContact.id,
                                    initialAmount = tx.amount.toInt().toString(),
                                    initialNote = tx.subtitle
                                )
                            )
                        },
                        shape = RoundedCornerShape(PayitRadius.pill),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryAccent,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Pay ${matchingContact.name} again")
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRowItem(
    tx: TransactionItem,
    onClick: () -> Unit
) {
    val colors = PayitTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = PayitSpacing.lg, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GPayAvatarCircle(
            initials = tx.initials,
            colorIndex = tx.avatarColorIndex,
            size = 44.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${tx.timestamp} • ${tx.bankUsed}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (tx.isCredit) "+${formatInr(tx.amount)}" else formatInr(tx.amount),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (tx.isCredit) colors.verifiedGreen else colors.textPrimary
        )
    }
}

// ============================================================================
// 2. CHECK BALANCE SCREEN (Bank Selection -> NPCI UPI PIN -> Balance Reveal)
// ============================================================================

@Composable
fun CheckBalanceScreen(
    state: PayitAppState,
    onBack: () -> Unit,
    onNavigate: (PayitDestination) -> Unit
) {
    val colors = PayitTheme.colors
    var verifyingAccount by remember { mutableStateOf<BankAccount?>(null) }
    var revealedAccount by remember { mutableStateOf<BankAccount?>(null) }
    var pinInput by remember { mutableStateOf("") }

    verifyingAccount?.let { targetAccount ->
        NpciPinPadStage(
            account = targetAccount,
            recipientName = "Balance Check",
            amount = 0.0,
            pinInput = pinInput,
            onDigit = { if (pinInput.length < 4) pinInput += it },
            onBackspace = { if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1) },
            onSubmit = {
                if (pinInput.length == 4) {
                    revealedAccount = targetAccount
                    verifyingAccount = null
                    pinInput = ""
                }
            },
            onCancel = {
                verifyingAccount = null
                pinInput = ""
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
    ) {
        GPayTopAppBar(
            title = "Check bank balance",
            subtitle = "Select an account to view balance",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = PayitSpacing.lg, vertical = PayitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            revealedAccount?.let { revealed ->
                item {
                    val liveAccount = state.bankAccounts.firstOrNull { it.id == revealed.id } ?: revealed
                    Surface(
                        shape = RoundedCornerShape(PayitRadius.xl),
                        color = colors.primaryContainer.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, colors.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${liveAccount.bankName} ${liveAccount.maskedNumber}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.onPrimaryContainer
                                )
                                Surface(
                                    shape = RoundedCornerShape(PayitRadius.pill),
                                    color = colors.verifiedGreenContainer
                                ) {
                                    Text(
                                        text = "Fetched via UPI",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (colors.isDark) colors.verifiedGreen else Color(0xFF0A4D1F),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (liveAccount.isCreditCard) "Available Credit Limit" else "Available Account Balance",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = formatInr(liveAccount.balance, showDecimals = true),
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "IFSC: ${liveAccount.ifsc} • ${liveAccount.accountType}",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(PayitRadius.large),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.divider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "UPI Lite Wallet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "On-device balance • Instant",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        Text(
                            text = formatInr(state.upiLiteBalance, showDecimals = true),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.verifiedGreen
                        )
                    }
                }
            }

            items(state.bankAccounts, key = { it.id }) { account ->
                Surface(
                    onClick = {
                        pinInput = ""
                        verifyingAccount = account
                    },
                    shape = RoundedCornerShape(PayitRadius.large),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.divider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(account.accentColorHex).copy(alpha = 0.14f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (account.isCreditCard) PayitIcons.CreditCard else PayitIcons.BalanceBank,
                                    contentDescription = null,
                                    tint = Color(account.accentColorHex),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${account.bankName} ${account.maskedNumber}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = account.accountType,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(PayitRadius.pill),
                            color = colors.primaryContainer
                        ) {
                            Text(
                                text = "Check",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { onNavigate(PayitDestination.TransactionHistory) },
                    shape = RoundedCornerShape(PayitRadius.pill),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("View full transaction statement")
                }
            }
        }
    }
}

// ============================================================================
// 3. CARDS & BANKS SCREEN (with Link New Bank / RuPay Card Modal)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsAndBanksScreen(
    state: PayitAppState,
    onBack: () -> Unit,
    onNavigate: (PayitDestination) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    var showLinkBankSheet by remember { mutableStateOf(false) }
    var pendingPayment by remember { mutableStateOf<PendingSecondaryPayment?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
    ) {
        GPayTopAppBar(
            title = "Payment methods",
            subtitle = "Bank accounts, RuPay UPI & UPI Lite",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = PayitSpacing.lg, vertical = PayitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(PayitRadius.xl),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.divider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "UPI Lite",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Instant lightning wallet ≤ ₹500",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            Text(
                                text = formatInr(state.upiLiteBalance),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { (state.upiLiteBalance / state.upiLiteMaxLimit).toFloat().coerceIn(0f, 1f) },
                            color = colors.rewardGold,
                            trackColor = colors.surfaceContainerHigh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    val primary = state.primaryAccount
                                    if (state.upiLiteBalance >= state.upiLiteMaxLimit) {
                                        onShowMessage("UPI Lite wallet is already at ₹2,000 limit")
                                    } else if (primary.balance < 500.0) {
                                        onShowMessage("Insufficient balance in ${primary.bankName}")
                                    } else {
                                        pendingPayment = PendingSecondaryPayment(
                                            title = "UPI Lite Top-Up",
                                            subtitle = "On-device wallet • ${primary.bankName} ${primary.maskedNumber}",
                                            amount = 500.0,
                                            account = primary,
                                            execute = { state.topUpUpiLite(500.0, primary) }
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(PayitRadius.pill),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primaryAccent,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Add ₹500")
                            }
                            OutlinedButton(
                                onClick = {
                                    val primary = state.primaryAccount
                                    if (state.upiLiteBalance >= state.upiLiteMaxLimit) {
                                        onShowMessage("UPI Lite wallet is already at ₹2,000 limit")
                                    } else if (primary.balance < 200.0) {
                                        onShowMessage("Insufficient balance in ${primary.bankName}")
                                    } else {
                                        pendingPayment = PendingSecondaryPayment(
                                            title = "UPI Lite Top-Up",
                                            subtitle = "On-device wallet • ${primary.bankName} ${primary.maskedNumber}",
                                            amount = 200.0,
                                            account = primary,
                                            execute = { state.topUpUpiLite(200.0, primary) }
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(PayitRadius.pill),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Add ₹200")
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Linked Bank Accounts & Cards",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                    Surface(
                        onClick = { showLinkBankSheet = true },
                        shape = RoundedCornerShape(PayitRadius.pill),
                        color = colors.primaryContainer
                    ) {
                        Text(
                            text = "+ Link new",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            items(state.bankAccounts, key = { it.id }) { account ->
                Surface(
                    shape = RoundedCornerShape(PayitRadius.large),
                    color = colors.surface,
                    border = BorderStroke(
                        width = if (account.isPrimary) 1.5.dp else 1.dp,
                        color = if (account.isPrimary) colors.primary else colors.divider
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(account.accentColorHex),
                                modifier = Modifier.size(width = 52.dp, height = 36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (account.isCreditCard) "RuPay" else account.bankName.take(4).uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${account.bankName} ${account.maskedNumber}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "${account.accountType} • IFSC ${account.ifsc}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            if (account.isPrimary) {
                                Surface(
                                    shape = RoundedCornerShape(PayitRadius.pill),
                                    color = colors.verifiedGreenContainer
                                ) {
                                    Text(
                                        text = "Primary",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (colors.isDark) colors.verifiedGreen else Color(0xFF0A4D1F),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = colors.divider.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (account.isCreditCard) {
                                    "Avail. limit: ${formatInr(account.balance)}"
                                } else {
                                    "Balance: ${formatInr(account.balance)}"
                                },
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!account.isPrimary) {
                                    Surface(
                                        onClick = {
                                            state.setPrimaryAccount(account.id)
                                            onShowMessage("${account.bankName} set as primary account")
                                        },
                                        shape = RoundedCornerShape(PayitRadius.pill),
                                        color = colors.primaryContainer
                                    ) {
                                        Text(
                                            text = "Set primary",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                                Surface(
                                    onClick = { onNavigate(PayitDestination.CheckBalance) },
                                    shape = RoundedCornerShape(PayitRadius.pill),
                                    color = colors.surfaceContainer
                                ) {
                                    Text(
                                        text = "Balance",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.primary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLinkBankSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLinkBankSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            LinkNewBankSheetContent(
                onLink = { bankName, last4, ifsc, isCreditCard ->
                    state.addBankAccount(bankName, last4, ifsc, isCreditCard)
                    showLinkBankSheet = false
                    onShowMessage("Linked $bankName (•••• $last4) to Payit UPI")
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
private fun LinkNewBankSheetContent(
    onLink: (bankName: String, last4: String, ifsc: String, isCreditCard: Boolean) -> Unit
) {
    val colors = PayitTheme.colors
    var bankName by remember { mutableStateOf("Axis Bank") }
    var last4 by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf("UTIB0000112") }
    var isCreditCard by remember { mutableStateOf(false) }

    val quickBanks = listOf("Axis Bank", "Kotak Mahindra", "Yes Bank", "IndusInd Bank")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PayitSpacing.xl)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Link New Bank / RuPay Card",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickBanks.forEach { preset ->
                FilterChip(
                    selected = bankName == preset,
                    onClick = { bankName = preset },
                    label = { Text(preset) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !isCreditCard,
                onClick = { isCreditCard = false },
                label = { Text("Savings Account") }
            )
            FilterChip(
                selected = isCreditCard,
                onClick = { isCreditCard = true },
                label = { Text("RuPay Credit Card") }
            )
        }
        OutlinedTextField(
            value = bankName,
            onValueChange = { bankName = it },
            label = { Text("Bank / Issuer name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = last4,
            onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 4) last4 = it },
            label = { Text("Last 4 digits") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ifsc,
            onValueChange = { if (it.length <= 11) ifsc = it.uppercase() },
            label = { Text("IFSC code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onLink(bankName, last4, ifsc, isCreditCard) },
            enabled = bankName.isNotBlank() && last4.length == 4,
            shape = RoundedCornerShape(PayitRadius.pill),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryAccent,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Link to Payit UPI")
        }
    }
}

// ============================================================================
// 4. PROFILE & SETTINGS SCREEN (My QR + Theme Switcher + Reset & Sign Out)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: PayitAppState,
    onBack: () -> Unit,
    onNavigate: (PayitDestination) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    var showHelpSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
    ) {
        GPayTopAppBar(
            title = "Profile & QR",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = PayitSpacing.lg, vertical = PayitSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(PayitRadius.xl),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.divider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GPayAvatarCircle(
                                initials = state.userInitials,
                                colorIndex = 0,
                                size = 54.dp,
                                isVerified = true
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = state.userName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = state.userPhone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(PayitRadius.pill),
                                color = colors.rewardGoldContainer
                            ) {
                                Text(
                                    text = "₹${state.totalCashbackEarned} won",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (colors.isDark) colors.rewardGold else Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        GPayQrCodeView(
                            data = "upi://pay?pa=${state.userHandle}&pn=${state.userName}",
                            size = 196.dp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "UPI ID: ${state.userHandle}",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(state.userHandle))
                                    onShowMessage("UPI ID copied: ${state.userHandle}")
                                },
                                shape = RoundedCornerShape(PayitRadius.pill),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = PayitIcons.Copy,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy UPI ID")
                            }
                            Button(
                                onClick = {
                                    clipboard.setText(AnnotatedString("Pay ${state.userName} on Payit: ${state.userHandle}"))
                                    onShowMessage("QR payment details copied to share")
                                },
                                shape = RoundedCornerShape(PayitRadius.pill),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primaryAccent,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = PayitIcons.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share QR")
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(PayitRadius.large),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.divider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = PayitIcons.SunMoon,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Appearance",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
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
                                ThemeMode.entries.forEach { mode ->
                                    val selected = state.themeMode == mode
                                    Surface(
                                        onClick = { state.themeMode = mode },
                                        shape = RoundedCornerShape(PayitRadius.pill),
                                        color = if (selected) colors.surface else Color.Transparent,
                                        shadowElevation = if (selected) 2.dp else 0.dp,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 9.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = mode.label,
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
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(PayitRadius.large),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.divider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = PayitSpacing.lg, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = colors.surfaceContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = PayitIcons.Lock,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Biometric & Screen Lock",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Protect Payit with fingerprint or PIN",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                            Switch(
                                checked = state.biometricLockEnabled,
                                onCheckedChange = {
                                    state.biometricLockEnabled = it
                                    onShowMessage(if (it) "App lock enabled" else "App lock disabled")
                                }
                            )
                        }
                        HorizontalDivider(
                            color = colors.divider.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = PayitSpacing.lg)
                        )
                        GPayManageMoneyRow(
                            icon = PayitIcons.CreditCard,
                            title = "Pay with bank & cards",
                            subtitle = "${state.bankAccounts.size} linked accounts",
                            onClick = { onNavigate(PayitDestination.CardsAndBanks) }
                        )
                        HorizontalDivider(
                            color = colors.divider.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = PayitSpacing.lg)
                        )
                        GPayManageMoneyRow(
                            icon = PayitIcons.TrophyReward,
                            title = "Rewards & cashback",
                            subtitle = "${state.unscratchedRewardsCount} scratch cards waiting",
                            onClick = { onNavigate(PayitDestination.Rewards(RewardsTab.SCRATCH_CARDS)) }
                        )
                        HorizontalDivider(
                            color = colors.divider.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = PayitSpacing.lg)
                        )
                        GPayManageMoneyRow(
                            icon = PayitIcons.VerifiedShield,
                            title = "Help & UPI dispute support",
                            subtitle = "24x7 NPCI Bharat BillPay & UPI help",
                            onClick = { showHelpSheet = true }
                        )
                    }
                }
            }

            // Full-Stack Persistence Controls: Reset Demo Data & Sign Out
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            state.resetDemoData()
                            onShowMessage("Demo balances & transactions reset")
                        },
                        shape = RoundedCornerShape(PayitRadius.pill),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Reset Demo Data")
                    }

                    Button(
                        onClick = {
                            state.signOut()
                            onShowMessage("Signed out of Payit")
                        },
                        shape = RoundedCornerShape(PayitRadius.pill),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.errorContainer,
                            contentColor = colors.error
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = PayitIcons.LogOut,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign Out")
                    }
                }
            }
        }
    }

    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayitSpacing.xl)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Payit 24x7 Support",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Every UPI transaction is protected by multi-layer NPCI encryption. If a transfer stays pending for more than 48 hours, funds are automatically refunded to your source bank.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        showHelpSheet = false
                        onNavigate(PayitDestination.TransactionHistory)
                    },
                    shape = RoundedCornerShape(PayitRadius.pill),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select a transaction to get help")
                }
            }
        }
    }
}

// ============================================================================
// 5. BILLS & RECHARGES SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsAndRechargesScreen(
    state: PayitAppState,
    initialCategory: BillCategory?,
    onBack: () -> Unit,
    onNavigate: (PayitDestination) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }
    var activeBill by remember { mutableStateOf<BillItem?>(null) }
    var pendingPayment by remember { mutableStateOf<PendingSecondaryPayment?>(null) }

    val filteredBills = remember(selectedCategory, state.bills.size, state.bills.count { it.isPaid }) {
        if (selectedCategory == null) state.bills.toList()
        else state.bills.filter { it.iconType == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
    ) {
        GPayTopAppBar(
            title = "Bills & recharges",
            subtitle = "Bharat BillPay • Zero convenience fee",
            onBack = onBack
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = PayitSpacing.lg, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primaryContainer,
                    selectedLabelColor = colors.onPrimaryContainer
                )
            )
            BillCategory.entries.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.label.replace("\n", " ")) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.primaryContainer,
                        selectedLabelColor = colors.onPrimaryContainer
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = PayitSpacing.lg, vertical = PayitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    onClick = { onNavigate(PayitDestination.MobileRecharge) },
                    shape = RoundedCornerShape(PayitRadius.large),
                    color = colors.primaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, colors.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = PayitIcons.MobileRecharge,
                            contentDescription = null,
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Prepaid Mobile Recharge",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.onPrimaryContainer
                            )
                            Text(
                                text = "Browse 5G unlimited & data packs",
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

            if (filteredBills.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(PayitRadius.large),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.divider),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No due bills in this category",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Switch to 'All' to view your linked BESCOM, Airtel, Tata Play, and FASTag bills.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredBills, key = { it.id }) { bill ->
                    Surface(
                        shape = RoundedCornerShape(PayitRadius.large),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.divider),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (bill.isPaid) colors.verifiedGreenContainer else colors.surfaceContainer,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = iconForBillCategory(bill.iconType),
                                        contentDescription = null,
                                        tint = if (bill.isPaid) colors.verifiedGreen else colors.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bill.billerName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "${bill.consumerNumber} • ${bill.dueDateText}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (bill.isOverdue) colors.error else colors.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (bill.isPaid) {
                                Surface(
                                    shape = RoundedCornerShape(PayitRadius.pill),
                                    color = colors.verifiedGreenContainer
                                ) {
                                    Text(
                                        text = "Paid",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (colors.isDark) colors.verifiedGreen else Color(0xFF0A4D1F),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { activeBill = bill },
                                    shape = RoundedCornerShape(PayitRadius.pill),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.primaryAccent,
                                        contentColor = Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Text(formatInr(bill.amount))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    activeBill?.let { bill ->
        ModalBottomSheet(
            onDismissRequest = { activeBill = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayitSpacing.xl)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Pay ${bill.billerName}",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
                Text(
                    text = "${bill.category} • ${bill.consumerNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = formatInr(bill.amount, showDecimals = true),
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Paying with ${state.primaryAccount.bankName} ${state.primaryAccount.maskedNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = {
                        val payer = state.primaryAccount
                        if (payer.balance >= bill.amount) {
                            activeBill = null
                            pendingPayment = PendingSecondaryPayment(
                                title = bill.billerName,
                                subtitle = "${bill.category} • Consumer #${bill.consumerNumber}",
                                amount = bill.amount,
                                account = payer,
                                execute = { state.payBill(bill, payer) }
                            )
                        } else {
                            onShowMessage("Insufficient balance in ${payer.bankName}")
                        }
                    },
                    shape = RoundedCornerShape(PayitRadius.pill),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Confirm & pay ${formatInr(bill.amount)}")
                }
            }
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

// ============================================================================
// 6. MOBILE RECHARGE SCREEN
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileRechargeScreen(
    state: PayitAppState,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    var phoneInput by remember { mutableStateOf("9820451720") }
    var selectedOperator by remember { mutableStateOf("Jio 5G") }
    var selectedPlanCategory by remember { mutableStateOf("Popular") }
    var selectedPlan by remember { mutableStateOf<RechargePlan?>(null) }
    var pendingPayment by remember { mutableStateOf<PendingSecondaryPayment?>(null) }

    val operators = listOf("Jio 5G", "Airtel", "Vi")
    val planCategories = listOf("All", "Popular", "Data Add-on", "Annual")
    val visiblePlans = remember(selectedPlanCategory) {
        if (selectedPlanCategory == "All") state.rechargePlans
        else state.rechargePlans.filter { it.category == selectedPlanCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
    ) {
        GPayTopAppBar(
            title = "Mobile recharge",
            subtitle = "Instant prepaid plan activation",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = PayitSpacing.lg, vertical = PayitSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 10) {
                            phoneInput = input
                        }
                    },
                    label = { Text("10-digit mobile number") },
                    prefix = { Text("+91 ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickNumbers = listOf(
                        "Self" to "9820451720",
                        "Priya" to "9876543210",
                        "Rohan" to "9811277834"
                    )
                    quickNumbers.forEach { (label, num) ->
                        Surface(
                            onClick = { phoneInput = num },
                            shape = RoundedCornerShape(PayitRadius.pill),
                            color = if (phoneInput == num) colors.primaryContainer else colors.surfaceContainer,
                            border = BorderStroke(1.dp, colors.divider)
                        ) {
                            Text(
                                text = "$label • $num",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (phoneInput == num) colors.onPrimaryContainer else colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    operators.forEach { op ->
                        val selected = selectedOperator == op
                        FilterChip(
                            selected = selected,
                            onClick = { selectedOperator = op },
                            label = { Text(op) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primaryContainer,
                                selectedLabelColor = colors.onPrimaryContainer
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    planCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedPlanCategory == cat,
                            onClick = { selectedPlanCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }

            items(visiblePlans, key = { it.id }) { plan ->
                Surface(
                    onClick = {
                        if (phoneInput.length == 10) {
                            selectedPlan = plan
                        } else {
                            onShowMessage("Enter a valid 10-digit mobile number")
                        }
                    },
                    shape = RoundedCornerShape(PayitRadius.large),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.divider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₹${plan.price}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!plan.tag.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(PayitRadius.pill),
                                        color = colors.rewardGoldContainer
                                    ) {
                                        Text(
                                            text = plan.tag,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (colors.isDark) colors.rewardGold else Color(0xFF92400E),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${plan.dataText} • ${plan.validityText}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = plan.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }

    selectedPlan?.let { plan ->
        ModalBottomSheet(
            onDismissRequest = { selectedPlan = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayitSpacing.xl)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "$selectedOperator Prepaid Recharge",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
                Text(
                    text = "+91 $phoneInput • ${plan.validityText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "₹${plan.price}",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                Text(
                    text = "${plan.dataText} • ${plan.description}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val payer = state.primaryAccount
                        val amt = plan.price.toDouble()
                        if (payer.balance >= amt) {
                            selectedPlan = null
                            pendingPayment = PendingSecondaryPayment(
                                title = "$selectedOperator Prepaid",
                                subtitle = "+91 $phoneInput • ${plan.dataText}, ${plan.validityText}",
                                amount = amt,
                                account = payer,
                                execute = { state.payRecharge(phoneInput, selectedOperator, plan, payer) }
                            )
                        } else {
                            onShowMessage("Insufficient balance in ${payer.bankName}")
                        }
                    },
                    shape = RoundedCornerShape(PayitRadius.pill),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Pay ₹${plan.price} & recharge")
                }
            }
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

// ============================================================================
// 7. PHONE NUMBER OR UPI ID ENTRY SCREEN
// ============================================================================

@Composable
fun PhoneOrUpiEntryScreen(
    state: PayitAppState,
    mode: EntryMode,
    onBack: () -> Unit,
    onNavigate: (PayitDestination) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    var input by remember(mode) { mutableStateOf("") }
    val isPhoneMode = mode == EntryMode.PHONE

    val isValid = if (isPhoneMode) {
        input.trim().length == 10 && input.trim().all { it.isDigit() }
    } else {
        val t = input.trim()
        t.contains("@") && t.substringBefore("@").isNotBlank() && t.substringAfter("@").length >= 2
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
            .navigationBarsPadding()
    ) {
        GPayTopAppBar(
            title = if (isPhoneMode) "Pay phone number" else "Pay UPI ID or number",
            subtitle = "Verified NPCI instant lookup",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PayitSpacing.lg, vertical = PayitSpacing.md)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { next ->
                    if (isPhoneMode) {
                        if (next.all { it.isDigit() } && next.length <= 10) input = next
                    } else {
                        input = next
                    }
                },
                label = {
                    Text(if (isPhoneMode) "Enter 10-digit mobile number" else "Enter UPI ID (e.g. name@okicici)")
                },
                prefix = if (isPhoneMode) {
                    { Text("+91 ") }
                } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isPhoneMode) KeyboardType.Phone else KeyboardType.Email
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    if (!isValid) {
                        onShowMessage(
                            if (isPhoneMode) "Please enter a 10-digit mobile number" else "Enter a valid UPI ID like name@bank"
                        )
                        return@Button
                    }
                    val trimmed = input.trim()
                    val target = if (isPhoneMode) {
                        state.findOrCreateContact(
                            name = "+91 $trimmed",
                            upiId = "$trimmed@okpayit",
                            phone = "+91 $trimmed"
                        )
                    } else {
                        state.findOrCreateContact(
                            name = trimmed.substringBefore("@"),
                            upiId = trimmed
                        )
                    }
                    onNavigate(PayitDestination.SendMoney(target.id))
                },
                enabled = isValid,
                shape = RoundedCornerShape(PayitRadius.pill),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryAccent,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Verify & Continue")
            }
        }

        Text(
            text = "Recent & Frequent",
            style = MaterialTheme.typography.labelLarge,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = PayitSpacing.lg, vertical = 6.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.contacts.filter { !it.isBusiness }.take(6), key = { it.id }) { person ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(PayitDestination.SendMoney(person.id)) }
                        .padding(horizontal = PayitSpacing.lg, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GPayAvatarCircle(
                        initials = person.initials,
                        colorIndex = person.avatarColorIndex,
                        size = 44.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (isPhoneMode) person.phone else person.upiId,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                    Icon(
                        imageVector = PayitIcons.ChevronRight,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// 8. BANK TRANSFER SCREEN
// ============================================================================

@Composable
fun BankTransferScreen(
    state: PayitAppState,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    var accountNumber by remember { mutableStateOf("") }
    var confirmAccount by remember { mutableStateOf("") }
    var ifsc by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var pendingPayment by remember { mutableStateOf<PendingSecondaryPayment?>(null) }
    var transferSucceeded by remember { mutableStateOf(false) }

    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val accountsMatch = accountNumber.length >= 9 && accountNumber == confirmAccount
    val isFormValid = accountsMatch && ifsc.length >= 8 && recipientName.isNotBlank() && parsedAmount > 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
            .navigationBarsPadding()
    ) {
        GPayTopAppBar(
            title = "Bank transfer",
            subtitle = "IMPS / NEFT instant account transfer",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = PayitSpacing.lg, vertical = PayitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 18) accountNumber = it },
                    label = { Text("Account number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = confirmAccount,
                    onValueChange = { if (it.all { ch -> ch.isDigit() } && it.length <= 18) confirmAccount = it },
                    label = { Text("Re-enter account number") },
                    isError = confirmAccount.isNotEmpty() && confirmAccount != accountNumber,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = ifsc,
                    onValueChange = { if (it.length <= 11) ifsc = it.uppercase() },
                    label = { Text("IFSC code (e.g. HDFC0000240)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                androidx.compose.animation.AnimatedVisibility(
                    visible = ifsc.length >= 4,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "Detected branch: ${ifsc.take(4)} Bank • Instant IMPS enabled",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.verifiedGreen,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it },
                    label = { Text("Recipient name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { next ->
                        if (next.all { it.isDigit() || it == '.' } && next.count { it == '.' } <= 1) {
                            amountText = next
                        }
                    },
                    label = { Text("Amount (₹)") },
                    prefix = { Text("₹ ") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Debiting from ${state.primaryAccount.bankName} ${state.primaryAccount.maskedNumber} (Bal: ${formatInr(state.primaryAccount.balance)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val payer = state.primaryAccount
                        if (payer.balance < parsedAmount) {
                            onShowMessage("Insufficient balance in ${payer.bankName}")
                        } else {
                            transferSucceeded = false
                            val masked = "•••• ${accountNumber.trim().takeLast(4)}"
                            pendingPayment = PendingSecondaryPayment(
                                title = recipientName.trim(),
                                subtitle = "A/C $masked • ${ifsc.trim().uppercase()}",
                                amount = parsedAmount,
                                account = payer,
                                execute = {
                                    transferSucceeded = true
                                    state.recordBankTransfer(
                                        beneficiaryName = recipientName.trim(),
                                        accountNumber = accountNumber.trim(),
                                        ifsc = ifsc.trim(),
                                        amount = parsedAmount,
                                        note = note,
                                        fromAccount = payer
                                    )
                                }
                            )
                        }
                    },
                    enabled = isFormValid,
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
                        text = if (parsedAmount > 0.0) "Transfer ${formatInr(parsedAmount)}" else "Confirm & Pay"
                    )
                }
            }
        }
    }

    pendingPayment?.let { req ->
        UniversalPaymentFlowDialog(
            title = req.title,
            subtitle = req.subtitle,
            amount = req.amount,
            account = req.account,
            onExecutePayment = req.execute,
            onFinish = {
                pendingPayment = null
                if (transferSucceeded) onBack()
            },
            onShowMessage = onShowMessage
        )
    }
}

// ============================================================================
// 9. SELF TRANSFER SCREEN
// ============================================================================

@Composable
fun SelfTransferScreen(
    state: PayitAppState,
    onBack: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    val bankList = state.bankAccounts.filter { !it.isCreditCard }.ifEmpty { state.bankAccounts }
    var fromIndex by remember { mutableIntStateOf(0) }
    var toIndex by remember { mutableIntStateOf(if (bankList.size > 1) 1 else 0) }
    var amountText by remember { mutableStateOf("") }
    var pendingPayment by remember { mutableStateOf<PendingSecondaryPayment?>(null) }
    var transferSucceeded by remember { mutableStateOf(false) }

    val fromAccount = bankList.getOrElse(fromIndex) { state.primaryAccount }
    val toAccount = bankList.getOrElse(toIndex) { state.primaryAccount }
    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
            .navigationBarsPadding()
    ) {
        GPayTopAppBar(
            title = "Self transfer",
            subtitle = "Move money between your linked accounts",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PayitSpacing.lg, vertical = PayitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SelfAccountCard(
                label = "FROM",
                account = fromAccount,
                onCycleAccount = {
                    if (bankList.size > 1) {
                        val next = (fromIndex + 1) % bankList.size
                        fromIndex = next
                        if (next == toIndex) toIndex = (next + 1) % bankList.size
                    }
                }
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    onClick = {
                        val tmp = fromIndex
                        fromIndex = toIndex
                        toIndex = tmp
                    },
                    shape = CircleShape,
                    color = colors.primaryContainer,
                    border = BorderStroke(1.dp, colors.divider),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = PayitIcons.SelfTransfer,
                            contentDescription = "Swap accounts",
                            tint = colors.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            SelfAccountCard(
                label = "TO",
                account = toAccount,
                onCycleAccount = {
                    if (bankList.size > 1) {
                        val next = (toIndex + 1) % bankList.size
                        toIndex = next
                        if (next == fromIndex) fromIndex = (next + 1) % bankList.size
                    }
                }
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { next ->
                    if (next.all { it.isDigit() || it == '.' } && next.count { it == '.' } <= 1) {
                        amountText = next
                    }
                },
                label = { Text("Transfer amount (₹)") },
                prefix = { Text("₹ ") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("1000", "5000", "10000").forEach { preset ->
                    Surface(
                        onClick = { amountText = preset },
                        shape = RoundedCornerShape(PayitRadius.pill),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.divider)
                    ) {
                        Text(
                            text = "₹$preset",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.primary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (fromAccount.id == toAccount.id) {
                        onShowMessage("Select two different accounts")
                    } else if (fromAccount.balance < parsedAmount) {
                        onShowMessage("Insufficient balance in ${fromAccount.bankName}")
                    } else {
                        transferSucceeded = false
                        pendingPayment = PendingSecondaryPayment(
                            title = "${toAccount.bankName} ${toAccount.maskedNumber}",
                            subtitle = "Self transfer from ${fromAccount.bankName} ${fromAccount.maskedNumber}",
                            amount = parsedAmount,
                            account = fromAccount,
                            execute = {
                                transferSucceeded = true
                                state.recordSelfTransfer(fromAccount, toAccount, parsedAmount)
                            }
                        )
                    }
                },
                enabled = parsedAmount > 0.0 && fromAccount.id != toAccount.id,
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
                    text = if (parsedAmount > 0.0) "Transfer ${formatInr(parsedAmount)}" else "Transfer now"
                )
            }
        }
    }

    pendingPayment?.let { req ->
        UniversalPaymentFlowDialog(
            title = req.title,
            subtitle = req.subtitle,
            amount = req.amount,
            account = req.account,
            onExecutePayment = req.execute,
            onFinish = {
                pendingPayment = null
                if (transferSucceeded) onBack()
            },
            onShowMessage = onShowMessage
        )
    }
}

@Composable
private fun SelfAccountCard(
    label: String,
    account: BankAccount,
    onCycleAccount: () -> Unit
) {
    val colors = PayitTheme.colors
    Surface(
        onClick = onCycleAccount,
        shape = RoundedCornerShape(PayitRadius.large),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.divider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(account.accentColorHex).copy(alpha = 0.14f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = PayitIcons.BalanceBank,
                        contentDescription = null,
                        tint = Color(account.accentColorHex),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.primary
                )
                Text(
                    text = "${account.bankName} ${account.maskedNumber}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary
                )
                Text(
                    text = "Balance: ${formatInr(account.balance, showDecimals = true)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            Text(
                text = "Change",
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary
            )
        }
    }
}

// ============================================================================
// 10. CIBIL SCORE SCREEN
// ============================================================================

@Composable
fun CibilScoreScreen(
    state: PayitAppState,
    onBack: () -> Unit
) {
    val colors = PayitTheme.colors
    val score = state.cibilScore
    val progress = ((score - 300f) / 600f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
    ) {
        GPayTopAppBar(
            title = "CIBIL score",
            subtitle = "Powered by TransUnion CIBIL • Free monthly refresh",
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = PayitSpacing.lg, vertical = PayitSpacing.md),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(PayitRadius.xl),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.divider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(196.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val trackColor = colors.surfaceContainerHigh
                            val arcColor = colors.verifiedGreen
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val stroke = 16.dp.toPx()
                                val arcSize = Size(size.width - stroke, size.height - stroke)
                                val topLeft = Offset(stroke / 2f, stroke / 2f)

                                drawArc(
                                    color = trackColor,
                                    startAngle = 150f,
                                    sweepAngle = 240f,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                                drawArc(
                                    brush = Brush.horizontalGradient(
                                        listOf(colors.primaryAccent, arcColor)
                                    ),
                                    startAngle = 150f,
                                    sweepAngle = 240f * progress,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$score",
                                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(PayitRadius.pill),
                                    color = colors.verifiedGreenContainer
                                ) {
                                    Text(
                                        text = "Excellent",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (colors.isDark) colors.verifiedGreen else Color(0xFF0A4D1F),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "You are in the top 15% of credit profiles in India. Keep utilization below 30% to maintain your rating.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Score Factors",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary
                )
            }

            val factors = listOf(
                Triple("On-time payments", "100% (38/38 on time)", "High impact • Excellent"),
                Triple("Credit card utilization", "14% of ₹2,50,000 limit", "High impact • Great"),
                Triple("Credit age", "4 years, 8 months", "Medium impact • Good"),
                Triple("Active credit accounts", "3 accounts (0 overdue)", "Low impact • Healthy")
            )
            items(factors) { (title, metric, impact) ->
                Surface(
                    shape = RoundedCornerShape(PayitRadius.large),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.divider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = impact,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        Text(
                            text = metric,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.verifiedGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyValueRow(label: String, value: String) {
    val colors = PayitTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
