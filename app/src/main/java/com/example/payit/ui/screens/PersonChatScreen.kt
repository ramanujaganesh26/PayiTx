package com.example.payit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payit.model.ChatEvent
import com.example.payit.model.ContactPerson
import com.example.payit.model.PayitAppState
import com.example.payit.model.PayitDestination
import com.example.payit.ui.designsystem.GPayAvatarCircle
import com.example.payit.ui.designsystem.PayitIcons
import com.example.payit.ui.designsystem.PayitRadius
import com.example.payit.ui.designsystem.PayitSpacing
import com.example.payit.ui.designsystem.formatInr
import com.example.payit.ui.theme.PayitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonChatScreen(
    state: PayitAppState,
    contactId: String,
    onBack: () -> Unit,
    onNavigate: (PayitDestination) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    val contact = state.getContactById(contactId)

    if (contact == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .statusBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Contact not found",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Go back") }
        }
        return
    }

    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    val listState = rememberLazyListState()
    var messageInput by remember { mutableStateOf("") }
    var selectedReceiptEvent by remember { mutableStateOf<ChatEvent?>(null) }
    var showRequestSheet by remember { mutableStateOf(false) }
    var showContactInfoSheet by remember { mutableStateOf(false) }

    LaunchedEffect(contact.messages.size) {
        if (contact.messages.isNotEmpty()) {
            listState.animateScrollToItem(contact.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
    ) {
        Surface(
            color = colors.surface,
            tonalElevation = 1.dp,
            shadowElevation = if (colors.isDark) 0.dp else 2.dp
        ) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = PayitSpacing.sm, vertical = PayitSpacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = PayitIcons.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showContactInfoSheet = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GPayAvatarCircle(
                        initials = contact.initials,
                        colorIndex = contact.avatarColorIndex,
                        size = 42.dp,
                        isVerified = contact.isBusiness
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = PayitIcons.VerifiedShield,
                                contentDescription = "Verified banking name",
                                tint = colors.verifiedGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = contact.bankingName,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        if (contact.isBusiness) {
                            showContactInfoSheet = true
                        } else {
                            onShowMessage("Calling ${contact.name} (${contact.phone})")
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (contact.isBusiness) PayitIcons.QrCode else PayitIcons.Call,
                        contentDescription = if (contact.isBusiness) "Merchant details" else "Call",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = PayitSpacing.lg, vertical = PayitSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                VerifiedContactBanner(contact = contact)
            }

            if (contact.messages.isEmpty()) {
                item {
                    EmptyChatPromptCard(
                        contact = contact,
                        onQuickPay = { amount ->
                            onNavigate(PayitDestination.SendMoney(contact.id, initialAmount = amount))
                        }
                    )
                }
            } else {
                items(contact.messages, key = { it.id }) { event ->
                    when {
                        event.isPayment && event.amount != null -> {
                            PaymentBubbleCard(
                                event = event,
                                recipientName = contact.name,
                                onClick = { selectedReceiptEvent = event }
                            )
                        }
                        event.isRequest && event.amount != null -> {
                            RequestBubbleCard(
                                event = event,
                                recipientName = contact.name
                            )
                        }
                        else -> {
                            TextMessageBubble(event = event)
                        }
                    }
                }
            }
        }

        Surface(
            color = colors.surface,
            shadowElevation = 6.dp,
            border = BorderStroke(0.5.dp, colors.divider)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = PayitSpacing.md, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onNavigate(PayitDestination.SendMoney(contact.id)) },
                        shape = RoundedCornerShape(PayitRadius.pill),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryAccent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = "Pay",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    if (!contact.isBusiness) {
                        Surface(
                            onClick = { showRequestSheet = true },
                            shape = RoundedCornerShape(PayitRadius.pill),
                            color = colors.primaryContainer,
                            modifier = Modifier.height(44.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Request",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(PayitRadius.pill),
                        color = colors.surfaceContainer,
                        border = BorderStroke(1.dp, colors.divider),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 14.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (messageInput.isEmpty()) {
                                    Text(
                                        text = "Message...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.textMuted
                                    )
                                }
                                BasicTextField(
                                    value = messageInput,
                                    onValueChange = { messageInput = it },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                                    cursorBrush = SolidColor(colors.primaryAccent),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (messageInput.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        state.sendChatMessage(contact, messageInput)
                                        messageInput = ""
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = PayitIcons.Send,
                                        contentDescription = "Send message",
                                        tint = colors.primaryAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedReceiptEvent?.let { event ->
        ModalBottomSheet(
            onDismissRequest = { selectedReceiptEvent = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            ChatPaymentReceiptSheet(
                event = event,
                contact = contact,
                onCopyRef = {
                    clipboard.setText(AnnotatedString(event.upiRefId))
                    onShowMessage("UPI Ref ID copied: ${event.upiRefId}")
                },
                onPayAgain = {
                    selectedReceiptEvent = null
                    onNavigate(
                        PayitDestination.SendMoney(
                            contactId = contact.id,
                            initialAmount = event.amount?.toInt()?.toString().orEmpty(),
                            initialNote = event.note
                        )
                    )
                }
            )
        }
    }

    if (showRequestSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRequestSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            RequestMoneySheetContent(
                recipient = contact,
                onSubmitRequest = { amount, note ->
                    state.recordRequest(contact, amount, note)
                    showRequestSheet = false
                    onShowMessage("Requested ${formatInr(amount)} from ${contact.name}")
                }
            )
        }
    }

    if (showContactInfoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showContactInfoSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            ContactDetailSheet(
                contact = contact,
                onCopyUpi = {
                    clipboard.setText(AnnotatedString(contact.upiId))
                    showContactInfoSheet = false
                    onShowMessage("Copied ${contact.upiId}")
                }
            )
        }
    }
}

@Composable
private fun VerifiedContactBanner(contact: ContactPerson) {
    val colors = PayitTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GPayAvatarCircle(
            initials = contact.initials,
            colorIndex = contact.avatarColorIndex,
            size = 64.dp,
            isVerified = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = contact.name,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = contact.upiId,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(PayitRadius.pill),
            color = colors.verifiedGreenContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
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
                    text = "Banking name: ${contact.bankingName}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (colors.isDark) colors.verifiedGreen else Color(0xFF0A4D1F)
                )
            }
        }
    }
}

@Composable
private fun EmptyChatPromptCard(
    contact: ContactPerson,
    onQuickPay: (String) -> Unit
) {
    val colors = PayitTheme.colors
    Surface(
        shape = RoundedCornerShape(PayitRadius.large),
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.divider),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Start paying ${contact.name}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fast, zero-fee UPI transfers backed by NPCI security.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("100", "500", "1000").forEach { amt ->
                    Surface(
                        onClick = { onQuickPay(amt) },
                        shape = RoundedCornerShape(PayitRadius.pill),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.divider)
                    ) {
                        Text(
                            text = "Pay ₹$amt",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.primary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentBubbleCard(
    event: ChatEvent,
    recipientName: String,
    onClick: () -> Unit
) {
    val colors = PayitTheme.colors
    val align = if (event.isOutgoing) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = align
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(PayitRadius.large),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.divider),
            shadowElevation = if (colors.isDark) 0.dp else 1.dp,
            modifier = Modifier.widthIn(min = 210.dp, max = 264.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (event.isOutgoing) "Payment to $recipientName" else "Payment from $recipientName",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatInr(event.amount ?: 0.0),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp
                    ),
                    color = colors.textPrimary
                )
                if (event.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = colors.divider.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(colors.verifiedGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PayitIcons.Check,
                                contentDescription = "Paid",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Text(
                            text = if (event.isOutgoing) "Paid • ${event.timestamp}" else "Received • ${event.timestamp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                    Icon(
                        imageVector = PayitIcons.ChevronRight,
                        contentDescription = "View receipt",
                        tint = colors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestBubbleCard(
    event: ChatEvent,
    recipientName: String
) {
    val colors = PayitTheme.colors
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            shape = RoundedCornerShape(PayitRadius.large),
            color = colors.primaryContainer.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f)),
            modifier = Modifier.widthIn(min = 210.dp, max = 264.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Requested from $recipientName",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatInr(event.amount ?: 0.0),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary
                )
                if (event.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pending • ${event.timestamp}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.primary
                )
            }
        }
    }
}

@Composable
private fun TextMessageBubble(event: ChatEvent) {
    val colors = PayitTheme.colors
    val isOutgoing = event.isOutgoing
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isOutgoing) colors.primaryContainer else colors.surfaceContainer,
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = event.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOutgoing) colors.onPrimaryContainer else colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = event.timestamp,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = colors.textMuted,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun ChatPaymentReceiptSheet(
    event: ChatEvent,
    contact: ContactPerson,
    onCopyRef: () -> Unit,
    onPayAgain: () -> Unit
) {
    val colors = PayitTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PayitSpacing.xl)
            .padding(bottom = 28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(colors.verifiedGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PayitIcons.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = if (event.isOutgoing) "Paid to ${contact.name}" else "Received from ${contact.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary
                )
                Text(
                    text = event.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = formatInr(event.amount ?: 0.0, showDecimals = true),
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary
        )
        if (event.note.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Note: ${event.note}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }

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
                ReceiptDetailRow("Banking name", contact.bankingName)
                ReceiptDetailRow("UPI ID", contact.upiId)
                ReceiptDetailRow("Payment mode", event.bankName.ifBlank { "HDFC Bank •••• 4821" })
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("UPI Ref ID", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    Row(
                        modifier = Modifier.clickable(onClick = onCopyRef),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = event.upiRefId.ifBlank { "432200148892" },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.primary
                        )
                        Icon(
                            imageVector = PayitIcons.Copy,
                            contentDescription = "Copy Ref ID",
                            tint = colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onPayAgain,
            shape = RoundedCornerShape(PayitRadius.pill),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryAccent,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Pay again")
        }
    }
}

@Composable
private fun ReceiptDetailRow(label: String, value: String) {
    val colors = PayitTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = colors.textPrimary
        )
    }
}

@Composable
private fun RequestMoneySheetContent(
    recipient: ContactPerson,
    onSubmitRequest: (Double, String) -> Unit
) {
    val colors = PayitTheme.colors
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PayitSpacing.xl)
            .padding(bottom = 28.dp)
    ) {
        Text(
            text = "Request money from ${recipient.name}",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary
        )
        Text(
            text = recipient.upiId,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = amountText,
            onValueChange = { input ->
                if (input.all { it.isDigit() || it == '.' } && input.count { it == '.' } <= 1) {
                    amountText = input
                }
            },
            label = { Text("Amount (₹)") },
            prefix = { Text("₹ ") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Note (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = { onSubmitRequest(parsedAmount, noteText) },
            enabled = parsedAmount > 0.0,
            shape = RoundedCornerShape(PayitRadius.pill),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryAccent,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = if (parsedAmount > 0.0) "Request ${formatInr(parsedAmount)}" else "Enter amount"
            )
        }
    }
}

@Composable
private fun ContactDetailSheet(
    contact: ContactPerson,
    onCopyUpi: () -> Unit
) {
    val colors = PayitTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PayitSpacing.xl)
            .padding(bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GPayAvatarCircle(
            initials = contact.initials,
            colorIndex = contact.avatarColorIndex,
            size = 64.dp,
            isVerified = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = contact.name,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary
        )
        Text(
            text = "Banking name: ${contact.bankingName}",
            style = MaterialTheme.typography.bodySmall,
            color = colors.verifiedGreen
        )
        Spacer(modifier = Modifier.height(14.dp))
        Surface(
            shape = RoundedCornerShape(PayitRadius.medium),
            color = colors.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReceiptDetailRow("UPI ID", contact.upiId)
                ReceiptDetailRow("Phone / Type", contact.phone)
                if (contact.category.isNotBlank()) {
                    ReceiptDetailRow("Category", contact.category)
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onCopyUpi,
            shape = RoundedCornerShape(PayitRadius.pill),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryContainer,
                contentColor = colors.onPrimaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Copy UPI ID")
        }
    }
}
