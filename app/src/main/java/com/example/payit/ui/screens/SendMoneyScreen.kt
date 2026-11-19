package com.example.payit.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.payit.model.BankAccount
import com.example.payit.model.ContactPerson
import com.example.payit.model.PayitAppState
import com.example.payit.model.PayitDestination
import com.example.payit.model.RewardsTab
import com.example.payit.model.TransactionItem
import com.example.payit.ui.designsystem.GPayAvatarCircle
import com.example.payit.ui.designsystem.GPayNumericKeypad
import com.example.payit.ui.designsystem.PayitIcons
import com.example.payit.ui.designsystem.PayitRadius
import com.example.payit.ui.designsystem.PayitSpacing
import com.example.payit.ui.designsystem.formatInr
import com.example.payit.ui.theme.NpciDarkHeader
import com.example.payit.ui.theme.PayitTheme
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private enum class PaymentStage {
    AMOUNT_ENTRY,
    NPCI_PIN_PAD,
    PROCESSING,
    SUCCESS_RECEIPT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendMoneyScreen(
    state: PayitAppState,
    contactId: String,
    initialAmount: String = "",
    initialNote: String = "",
    onBack: () -> Unit,
    onNavigate: (PayitDestination) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    val recipient = state.getContactById(contactId)

    if (recipient == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .statusBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Recipient not found", color = colors.textPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Back") }
        }
        return
    }

    var stage by remember { mutableStateOf(PaymentStage.AMOUNT_ENTRY) }
    var amountInput by remember(initialAmount) { mutableStateOf(initialAmount) }
    var noteInput by remember(initialNote) { mutableStateOf(initialNote) }
    var useUpiLite by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf(state.primaryAccount) }
    var showMethodSheet by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var completedTx by remember { mutableStateOf<TransactionItem?>(null) }

    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isUpiLiteEligible = parsedAmount in 1.0..500.0 && state.upiLiteBalance >= parsedAmount

    LaunchedEffect(stage) {
        if (stage == PaymentStage.PROCESSING) {
            delay(650L)
            completedTx = state.recordPayment(
                recipient = recipient,
                amount = parsedAmount,
                note = noteInput,
                account = selectedAccount,
                useUpiLite = useUpiLite && isUpiLiteEligible
            )
            stage = PaymentStage.SUCCESS_RECEIPT
        }
    }

    AnimatedContent(
        targetState = stage,
        transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(200)) },
        label = "sendMoneyStage"
    ) { currentStage ->
        when (currentStage) {
            PaymentStage.AMOUNT_ENTRY -> {
                AmountEntryStage(
                    recipient = recipient,
                    amountInput = amountInput,
                    parsedAmount = parsedAmount,
                    noteInput = noteInput,
                    useUpiLite = useUpiLite,
                    upiLiteBalance = state.upiLiteBalance,
                    selectedAccount = selectedAccount,
                    onAmountChange = { amountInput = it },
                    onNoteChange = { noteInput = it },
                    onOpenMethodSheet = { showMethodSheet = true },
                    onBack = onBack,
                    onProceed = {
                        if (parsedAmount <= 0.0) {
                            onShowMessage("Enter an amount greater than ₹0")
                            return@AmountEntryStage
                        }
                        if (parsedAmount > 100000.0) {
                            onShowMessage("UPI per-transaction limit is ₹1,00,000")
                            return@AmountEntryStage
                        }
                        if (useUpiLite) {
                            if (parsedAmount > 500.0) {
                                onShowMessage("UPI Lite supports payments up to ₹500. Switch to a bank account.")
                                return@AmountEntryStage
                            }
                            if (state.upiLiteBalance < parsedAmount) {
                                onShowMessage("Insufficient UPI Lite balance")
                                return@AmountEntryStage
                            }
                        } else {
                            if (selectedAccount.balance < parsedAmount) {
                                onShowMessage("Insufficient balance in ${selectedAccount.bankName}")
                                return@AmountEntryStage
                            }
                        }
                        pinInput = ""
                        stage = PaymentStage.NPCI_PIN_PAD
                    }
                )
            }

            PaymentStage.NPCI_PIN_PAD -> {
                val pinAccount = if (useUpiLite) {
                    selectedAccount.copy(bankName = "UPI Lite", maskedNumber = "On-Device Wallet")
                } else {
                    selectedAccount
                }
                NpciPinPadStage(
                    account = pinAccount,
                    recipientName = recipient.bankingName,
                    amount = parsedAmount,
                    pinInput = pinInput,
                    onDigit = { digit ->
                        if (pinInput.length < 4) {
                            pinInput += digit
                        }
                    },
                    onBackspace = {
                        if (pinInput.isNotEmpty()) {
                            pinInput = pinInput.dropLast(1)
                        }
                    },
                    onSubmit = {
                        if (pinInput.length == 4) {
                            stage = PaymentStage.PROCESSING
                        }
                    },
                    onCancel = { stage = PaymentStage.AMOUNT_ENTRY }
                )
            }

            PaymentStage.PROCESSING -> {
                ProcessingPaymentStage(
                    amount = parsedAmount,
                    recipientName = recipient.name,
                    isUpiLite = useUpiLite
                )
            }

            PaymentStage.SUCCESS_RECEIPT -> {
                PaymentSuccessStage(
                    recipient = recipient,
                    amount = parsedAmount,
                    transaction = completedTx,
                    unlockedReward = parsedAmount >= 100.0,
                    onViewReward = { onNavigate(PayitDestination.Rewards(RewardsTab.SCRATCH_CARDS)) },
                    onDone = onBack,
                    onShowMessage = onShowMessage
                )
            }
        }
    }

    if (showMethodSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMethodSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            PaymentMethodSelectorSheet(
                accounts = state.bankAccounts,
                upiLiteBalance = state.upiLiteBalance,
                parsedAmount = parsedAmount,
                selectedUseUpiLite = useUpiLite,
                selectedAccountId = selectedAccount.id,
                onSelectUpiLite = {
                    useUpiLite = true
                    showMethodSheet = false
                },
                onSelectAccount = { acc ->
                    useUpiLite = false
                    selectedAccount = acc
                    showMethodSheet = false
                }
            )
        }
    }
}

@Composable
private fun AmountEntryStage(
    recipient: ContactPerson,
    amountInput: String,
    parsedAmount: Double,
    noteInput: String,
    useUpiLite: Boolean,
    upiLiteBalance: Double,
    selectedAccount: BankAccount,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onOpenMethodSheet: () -> Unit,
    onBack: () -> Unit,
    onProceed: () -> Unit
) {
    val colors = PayitTheme.colors

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
                .padding(horizontal = PayitSpacing.sm, vertical = PayitSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = PayitIcons.Close,
                    contentDescription = "Close",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(PayitRadius.pill),
                color = colors.surfaceContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = PayitIcons.Lock,
                        contentDescription = null,
                        tint = colors.verifiedGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "UPI Secured",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.width(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = PayitSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GPayAvatarCircle(
                initials = recipient.initials,
                colorIndex = recipient.avatarColorIndex,
                size = 56.dp,
                isVerified = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Paying ${recipient.name}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = PayitIcons.VerifiedShield,
                    contentDescription = null,
                    tint = colors.verifiedGreen,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Banking name: ${recipient.bankingName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = recipient.upiId,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "₹",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 32.sp
                    ),
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = amountInput.ifEmpty { "0" },
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = if (amountInput.length > 6) 40.sp else 52.sp
                    ),
                    color = if (amountInput.isEmpty()) colors.textMuted else colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(100, 500, 2000).forEach { add ->
                    Surface(
                        onClick = {
                            val nextVal = (parsedAmount + add).toInt()
                            onAmountChange(nextVal.toString())
                        },
                        shape = RoundedCornerShape(PayitRadius.pill),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.divider)
                    ) {
                        Text(
                            text = "+₹$add",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(PayitRadius.medium),
                color = colors.surfaceContainer,
                border = BorderStroke(1.dp, colors.divider),
                modifier = Modifier.width(220.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (noteInput.isEmpty()) {
                        Text(
                            text = "Add a note",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted
                        )
                    }
                    BasicTextField(
                        value = noteInput,
                        onValueChange = { if (it.length <= 40) onNoteChange(it) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(colors.primaryAccent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1-Tap Quick Note Category Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
            ) {
                listOf("Coffee", "Food & Dining", "Cab split", "Groceries", "Rent").forEach { tag ->
                    val selected = noteInput.equals(tag, ignoreCase = true)
                    Surface(
                        onClick = { onNoteChange(if (selected) "" else tag) },
                        shape = RoundedCornerShape(PayitRadius.pill),
                        color = if (selected) colors.primaryContainer else colors.surfaceContainer.copy(alpha = 0.7f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selected) colors.primary else colors.divider
                        )
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = if (selected) colors.onPrimaryContainer else colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.divider),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayitSpacing.lg, vertical = 14.dp)
            ) {
                Surface(
                    onClick = onOpenMethodSheet,
                    shape = RoundedCornerShape(PayitRadius.medium),
                    color = colors.surfaceContainer,
                    border = BorderStroke(1.dp, colors.divider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (useUpiLite) colors.rewardGoldContainer else colors.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (useUpiLite) PayitIcons.UpiLiteBolt else PayitIcons.BalanceBank,
                                    contentDescription = null,
                                    tint = if (useUpiLite) colors.rewardGold else colors.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (useUpiLite) "UPI Lite (PIN Verified ≤ ₹500)" else "${selectedAccount.bankName} ${selectedAccount.maskedNumber}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = if (useUpiLite) {
                                    "Wallet balance: ${formatInr(upiLiteBalance, showDecimals = true)}"
                                } else {
                                    "${selectedAccount.accountType} • Bal: ${formatInr(selectedAccount.balance)}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        Icon(
                            imageVector = PayitIcons.ChevronDown,
                            contentDescription = "Switch account",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                GPayNumericKeypad(
                    allowDecimal = true,
                    onDigitClick = { key ->
                        if (key == ".") {
                            if (!amountInput.contains(".")) {
                                onAmountChange(if (amountInput.isEmpty()) "0." else "$amountInput.")
                            }
                        } else {
                            val parts = amountInput.split(".")
                            if (parts.size == 2 && parts[1].length >= 2) return@GPayNumericKeypad
                            if (amountInput.length >= 8) return@GPayNumericKeypad
                            val next = if (amountInput == "0") key else amountInput + key
                            onAmountChange(next)
                        }
                    },
                    onBackspaceClick = {
                        if (amountInput.isNotEmpty()) {
                            onAmountChange(amountInput.dropLast(1))
                        }
                    },
                    onSubmitClick = onProceed,
                    submitEnabled = parsedAmount > 0.0
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodSelectorSheet(
    accounts: List<BankAccount>,
    upiLiteBalance: Double,
    parsedAmount: Double,
    selectedUseUpiLite: Boolean,
    selectedAccountId: String,
    onSelectUpiLite: () -> Unit,
    onSelectAccount: (BankAccount) -> Unit
) {
    val colors = PayitTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PayitSpacing.xl)
            .padding(bottom = 28.dp)
    ) {
        Text(
            text = "Choose account to pay with",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(14.dp))

        val liteEligible = parsedAmount <= 500.0
        Surface(
            onClick = { if (liteEligible) onSelectUpiLite() },
            shape = RoundedCornerShape(PayitRadius.medium),
            color = if (selectedUseUpiLite) colors.primaryContainer.copy(alpha = 0.45f) else colors.surfaceContainer,
            border = BorderStroke(
                width = if (selectedUseUpiLite) 1.5.dp else 1.dp,
                color = if (selectedUseUpiLite) colors.primary else colors.divider
            ),
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (liteEligible) 1f else 0.55f)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = colors.rewardGoldContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = PayitIcons.UpiLiteBolt,
                            contentDescription = null,
                            tint = colors.rewardGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "UPI Lite (PIN Verified ≤ ₹500)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = if (liteEligible) {
                            "Balance: ${formatInr(upiLiteBalance, showDecimals = true)}"
                        } else {
                            "Available for payments up to ₹500"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                if (selectedUseUpiLite) {
                    Icon(
                        imageVector = PayitIcons.Check,
                        contentDescription = "Selected",
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        accounts.forEach { acc ->
            val isSelected = !selectedUseUpiLite && acc.id == selectedAccountId
            Surface(
                onClick = { onSelectAccount(acc) },
                shape = RoundedCornerShape(PayitRadius.medium),
                color = if (isSelected) colors.primaryContainer.copy(alpha = 0.45f) else colors.surfaceContainer,
                border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) colors.primary else colors.divider
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(acc.accentColorHex).copy(alpha = 0.14f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (acc.isCreditCard) PayitIcons.CreditCard else PayitIcons.BalanceBank,
                                contentDescription = null,
                                tint = Color(acc.accentColorHex),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${acc.bankName} ${acc.maskedNumber}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${acc.accountType} • ${formatInr(acc.balance)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = PayitIcons.Check,
                            contentDescription = "Selected",
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Authentic NPCI UPI PIN Pad Stage.
 */
@Composable
fun NpciPinPadStage(
    account: BankAccount,
    recipientName: String,
    amount: Double,
    pinInput: String,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = PayitTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
    ) {
        Surface(
            color = NpciDarkHeader,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = PayitSpacing.lg, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = account.bankName.uppercase(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = account.maskedNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "BHIM UPI",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        IconButton(onClick = onCancel) {
                            Icon(
                                imageVector = PayitIcons.Close,
                                contentDescription = "Cancel PIN entry",
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.16f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "To: $recipientName",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatInr(amount, showDecimals = true),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = PayitSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "ENTER 4-DIGIT UPI PIN",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                repeat(4) { index ->
                    val filled = index < pinInput.length
                    Box(
                        modifier = Modifier
                            .size(width = 48.dp, height = 52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceContainer)
                            .border(
                                width = if (filled) 2.dp else 1.dp,
                                color = if (filled) colors.primaryAccent else colors.divider,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (filled) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(colors.textPrimary, CircleShape)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Demo UPI PIN: enter any 4 digits",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted
            )
        }

        Surface(
            color = colors.surface,
            border = BorderStroke(1.dp, colors.divider),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            GPayNumericKeypad(
                allowDecimal = false,
                onDigitClick = onDigit,
                onBackspaceClick = onBackspace,
                onSubmitClick = onSubmit,
                submitEnabled = pinInput.length == 4,
                modifier = Modifier.padding(horizontal = PayitSpacing.lg, vertical = 14.dp)
            )
        }
    }
}

@Composable
private fun ProcessingPaymentStage(
    amount: Double,
    recipientName: String,
    isUpiLite: Boolean
) {
    val colors = PayitTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = colors.primaryAccent,
            strokeWidth = 4.dp,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (isUpiLite) "Paying via UPI Lite..." else "Paying $recipientName...",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = formatInr(amount, showDecimals = true),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.primaryAccent
        )
    }
}

@Composable
private fun PaymentSuccessStage(
    recipient: ContactPerson,
    amount: Double,
    transaction: TransactionItem?,
    unlockedReward: Boolean,
    onViewReward: () -> Unit,
    onDone: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = PayitSpacing.xl, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.45f))

        GPaySuccessTickHero()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = formatInr(amount, showDecimals = true),
            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Paid to ${recipient.name}",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = PayitIcons.VerifiedShield,
                contentDescription = null,
                tint = colors.verifiedGreen,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = recipient.bankingName,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Sculpted Digital Receipt Card with Top Accent Header Strip & Status Badge
        DigitalReceiptSummaryCard(
            timestamp = transaction?.timestamp ?: "Just now",
            bankUsed = transaction?.bankUsed ?: "HDFC Bank •••• 4821",
            upiRefId = transaction?.upiRefId ?: "432200192841",
            note = transaction?.subtitle.orEmpty()
        )

        if (unlockedReward) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                onClick = onViewReward,
                shape = RoundedCornerShape(PayitRadius.large),
                color = colors.rewardGoldContainer,
                border = BorderStroke(1.dp, colors.rewardGold.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = PayitIcons.Sparkles,
                        contentDescription = null,
                        tint = colors.rewardGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reward unlocked!",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Tap to scratch & win instant cashback",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                    Icon(
                        imageVector = PayitIcons.ChevronRight,
                        contentDescription = null,
                        tint = colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val receiptText = "Payit Receipt: Paid ${formatInr(amount, true)} to ${recipient.bankingName} (${recipient.upiId}). Ref: ${transaction?.upiRefId ?: "432200192841"}"
                    clipboard.setText(AnnotatedString(receiptText))
                    onShowMessage("Receipt copied to clipboard for sharing")
                },
                shape = RoundedCornerShape(PayitRadius.pill),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = PayitIcons.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share receipt")
            }

            Button(
                onClick = onDone,
                shape = RoundedCornerShape(PayitRadius.pill),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryAccent,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Text(
                    text = "Done",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

/**
 * Choreographed 5-layer Canvas Payment Successful Tick Hero with sonar ripples, rotating 12-lobe seal,
 * emerald-teal gradient medallion, progressive checkmark stroke, and radiating confetti stars.
 */
@Composable
fun GPaySuccessTickHero(modifier: Modifier = Modifier) {
    val colors = PayitTheme.colors

    val badgeScale = remember { Animatable(0.2f) }
    val checkDrawProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        badgeScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 280f)
        )
    }

    LaunchedEffect(Unit) {
        delay(160L)
        checkDrawProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
        )
    }

    val infinite = rememberInfiniteTransition(label = "tickHeroInfinite")
    val ring1Progress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1"
    )
    val ring2Progress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(900)
        ),
        label = "ring2"
    )
    val rosetteRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rosetteRotation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(188.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val center = Offset(cx, cy)
                val minDim = size.minDimension
                val scale = badgeScale.value.coerceAtLeast(0.05f)
                val coreR = minDim * 0.255f * scale

                val emeraldDeep = Color(0xFF059669)
                val emeraldMid = Color(0xFF10B981)
                val emeraldBright = Color(0xFF34D399)

                // Layer 1: Ambient radial glow + dual staggered sonar ripple rings
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            emeraldMid.copy(alpha = 0.24f),
                            emeraldBright.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = minDim * 0.48f
                    ),
                    radius = minDim * 0.48f,
                    center = center
                )

                listOf(ring1Progress, ring2Progress).forEach { prog ->
                    val rippleR = (minDim * 0.255f) + (minDim * 0.22f) * prog
                    val rippleAlpha = (1f - prog) * 0.36f
                    drawCircle(
                        color = emeraldMid.copy(alpha = rippleAlpha),
                        radius = rippleR,
                        center = center,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Layer 2: Rotating 12-point geometric verification rosette/seal
                val lobes = 12
                val step = (2.0 * PI / lobes).toFloat()
                val rOuter = coreR * 1.24f
                val rValley = coreR * 1.07f
                val sealPath = Path().apply {
                    for (i in 0 until lobes) {
                        val a0 = i * step
                        val aMid = a0 + step * 0.5f
                        val a1 = (i + 1) * step
                        val x0 = cx + rValley * cos(a0.toDouble()).toFloat()
                        val y0 = cy + rValley * sin(a0.toDouble()).toFloat()
                        val xPeak = cx + rOuter * cos(aMid.toDouble()).toFloat()
                        val yPeak = cy + rOuter * sin(aMid.toDouble()).toFloat()
                        val x1 = cx + rValley * cos(a1.toDouble()).toFloat()
                        val y1 = cy + rValley * sin(a1.toDouble()).toFloat()
                        if (i == 0) moveTo(x0, y0)
                        quadraticTo(xPeak, yPeak, x1, y1)
                    }
                    close()
                }
                rotate(degrees = rosetteRotation, pivot = center) {
                    drawPath(
                        path = sealPath,
                        color = emeraldMid.copy(alpha = 0.22f)
                    )
                    drawPath(
                        path = sealPath,
                        color = emeraldBright.copy(alpha = 0.45f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                // Layer 3: Rich Emerald-Teal Gradient Medallion with Specular Rim
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(emeraldDeep, emeraldMid, emeraldBright),
                        start = Offset(cx - coreR, cy - coreR),
                        end = Offset(cx + coreR, cy + coreR)
                    ),
                    radius = coreR,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.36f),
                    radius = coreR * 0.89f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
                val specR = coreR * 0.78f
                drawArc(
                    color = Color.White.copy(alpha = 0.45f),
                    startAngle = -140f,
                    sweepAngle = 92f,
                    useCenter = false,
                    topLeft = Offset(cx - specR, cy - specR),
                    size = Size(specR * 2f, specR * 2f),
                    style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                )

                // Layer 4: Progressive Self-Drawing Checkmark Stroke
                val prog = checkDrawProgress.value.coerceIn(0f, 1f)
                if (prog > 0f) {
                    val p0 = Offset(cx - coreR * 0.35f, cy + coreR * 0.02f)
                    val p1 = Offset(cx - coreR * 0.09f, cy + coreR * 0.27f)
                    val p2 = Offset(cx + coreR * 0.39f, cy - coreR * 0.23f)
                    val checkPath = Path().apply {
                        moveTo(p0.x, p0.y)
                        if (prog <= 0.38f) {
                            val t = prog / 0.38f
                            lineTo(
                                p0.x + (p1.x - p0.x) * t,
                                p0.y + (p1.y - p0.y) * t
                            )
                        } else {
                            val t = (prog - 0.38f) / 0.62f
                            lineTo(p1.x, p1.y)
                            lineTo(
                                p1.x + (p2.x - p1.x) * t,
                                p1.y + (p2.y - p1.y) * t
                            )
                        }
                    }
                    drawPath(
                        path = checkPath,
                        color = Color.White,
                        style = Stroke(
                            width = 5.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Layer 5: 16 Radiating Confetti Diamonds, Stars & Dots
                val palette = listOf(
                    Color(0xFFFBBF24),
                    Color(0xFF34D399),
                    Color(0xFF60A5FA),
                    Color(0xFFF472B6),
                    Color(0xFFA78BFA)
                )
                for (i in 0 until 16) {
                    val angle = i * (2.0 * PI / 16.0) + 0.16
                    val distFactor = 1.46f + (i % 3) * 0.18f
                    val dist = coreR * distFactor
                    val px = cx + dist * cos(angle).toFloat()
                    val py = cy + dist * sin(angle).toFloat()
                    val pColor = palette[i % palette.size].copy(alpha = scale.coerceIn(0f, 1f))
                    val pSize = (3.4f + (i % 2) * 1.4f).dp.toPx() * scale

                    when (i % 3) {
                        0 -> {
                            // 4-point sparkle star
                            val star = Path().apply {
                                moveTo(px, py - pSize * 1.3f)
                                lineTo(px + pSize * 0.38f, py - pSize * 0.38f)
                                lineTo(px + pSize * 1.3f, py)
                                lineTo(px + pSize * 0.38f, py + pSize * 0.38f)
                                lineTo(px, py + pSize * 1.3f)
                                lineTo(px - pSize * 0.38f, py + pSize * 0.38f)
                                lineTo(px - pSize * 1.3f, py)
                                lineTo(px - pSize * 0.38f, py - pSize * 0.38f)
                                close()
                            }
                            drawPath(path = star, color = pColor)
                        }
                        1 -> {
                            // Confetti diamond
                            val diamond = Path().apply {
                                moveTo(px, py - pSize)
                                lineTo(px + pSize * 0.85f, py)
                                lineTo(px, py + pSize)
                                lineTo(px - pSize * 0.85f, py)
                                close()
                            }
                            drawPath(path = diamond, color = pColor)
                        }
                        else -> {
                            drawCircle(color = pColor, radius = pSize * 0.72f, center = Offset(px, py))
                        }
                    }
                }
            }
        }

        // Verified Settlement Pill
        Surface(
            shape = RoundedCornerShape(PayitRadius.pill),
            color = colors.verifiedGreenContainer,
            border = BorderStroke(1.dp, colors.verifiedGreen.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = PayitIcons.VerifiedShield,
                    contentDescription = null,
                    tint = colors.verifiedGreen,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "BHIM UPI VERIFIED • INSTANT TRANSFER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (colors.isDark) colors.verifiedGreen else Color(0xFF0A4D1F)
                )
            }
        }
    }
}

@Composable
private fun DigitalReceiptSummaryCard(
    timestamp: String,
    bankUsed: String,
    upiRefId: String,
    note: String
) {
    val colors = PayitTheme.colors
    Surface(
        shape = RoundedCornerShape(PayitRadius.large),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.divider),
        shadowElevation = if (colors.isDark) 0.dp else 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(colors.primaryAccent, colors.verifiedGreen)
                        )
                    )
            )
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UPI Digital Receipt",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(PayitRadius.pill),
                        color = colors.verifiedGreenContainer
                    ) {
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (colors.isDark) colors.verifiedGreen else Color(0xFF0A4D1F),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }
                HorizontalDivider(color = colors.divider.copy(alpha = 0.65f))
                ReceiptDetailLine("Date & time", timestamp)
                ReceiptDetailLine("Paid via", bankUsed)
                ReceiptDetailLine("UPI transaction ID", upiRefId)
                if (note.isNotBlank()) {
                    ReceiptDetailLine("Note", note)
                }
            }
        }
    }
}

/**
 * Reusable full-screen UPI PIN -> Processing -> Celebratory Payment Tick & Receipt dialog
 * used by Bills, Mobile Recharges, Bank Transfers, Self Transfers, and UPI Lite Top-Ups.
 */
@Composable
fun UniversalPaymentFlowDialog(
    title: String,
    subtitle: String,
    amount: Double,
    account: BankAccount,
    onExecutePayment: () -> TransactionItem,
    onFinish: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    var stage by remember { mutableStateOf(PaymentStage.NPCI_PIN_PAD) }
    var pinInput by remember { mutableStateOf("") }
    var completedTx by remember { mutableStateOf<TransactionItem?>(null) }

    LaunchedEffect(stage) {
        if (stage == PaymentStage.PROCESSING) {
            delay(600L)
            completedTx = onExecutePayment()
            stage = PaymentStage.SUCCESS_RECEIPT
        }
    }

    Dialog(
        onDismissRequest = {
            if (stage == PaymentStage.NPCI_PIN_PAD) {
                onFinish()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = stage != PaymentStage.PROCESSING,
            dismissOnClickOutside = false
        )
    ) {
        AnimatedContent(
            targetState = stage,
            transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(200)) },
            label = "universalPayStage"
        ) { currentStage ->
            when (currentStage) {
                PaymentStage.NPCI_PIN_PAD -> {
                    NpciPinPadStage(
                        account = account,
                        recipientName = title.uppercase(),
                        amount = amount,
                        pinInput = pinInput,
                        onDigit = { if (pinInput.length < 4) pinInput += it },
                        onBackspace = { if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1) },
                        onSubmit = {
                            if (pinInput.length == 4) {
                                stage = PaymentStage.PROCESSING
                            }
                        },
                        onCancel = onFinish
                    )
                }

                PaymentStage.PROCESSING -> {
                    ProcessingPaymentStage(
                        amount = amount,
                        recipientName = title,
                        isUpiLite = false
                    )
                }

                PaymentStage.SUCCESS_RECEIPT, PaymentStage.AMOUNT_ENTRY -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.background)
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(horizontal = PayitSpacing.xl, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.weight(0.45f))

                        GPaySuccessTickHero()

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = formatInr(amount, showDecimals = true),
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Paid to $title",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        if (subtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        DigitalReceiptSummaryCard(
                            timestamp = completedTx?.timestamp ?: "Just now",
                            bankUsed = completedTx?.bankUsed ?: "${account.bankName} ${account.maskedNumber}",
                            upiRefId = completedTx?.upiRefId ?: "432200192841",
                            note = completedTx?.subtitle.orEmpty()
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val receiptText = "Payit Receipt: Paid ${formatInr(amount, true)} to $title. Ref: ${completedTx?.upiRefId ?: "432200192841"}"
                                    clipboard.setText(AnnotatedString(receiptText))
                                    onShowMessage("Receipt copied to clipboard for sharing")
                                },
                                shape = RoundedCornerShape(PayitRadius.pill),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Icon(
                                    imageVector = PayitIcons.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share receipt")
                            }

                            Button(
                                onClick = onFinish,
                                shape = RoundedCornerShape(PayitRadius.pill),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primaryAccent,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = "Done",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptDetailLine(label: String, value: String) {
    val colors = PayitTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
