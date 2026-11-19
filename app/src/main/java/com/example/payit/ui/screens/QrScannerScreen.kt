package com.example.payit.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payit.model.PayitAppState
import com.example.payit.model.PayitDestination
import com.example.payit.ui.designsystem.GPayQrCodeView
import com.example.payit.ui.designsystem.PayitIcons
import com.example.payit.ui.designsystem.PayitRadius
import com.example.payit.ui.designsystem.PayitSpacing
import com.example.payit.ui.theme.GPayBlue
import com.example.payit.ui.theme.GPayGreen
import com.example.payit.ui.theme.GPayRed
import com.example.payit.ui.theme.GPayYellow
import com.example.payit.ui.theme.PayitTheme
import com.example.payit.ui.theme.ScannerBackdrop
import kotlinx.coroutines.delay

private data class DemoQrTarget(
    val label: String,
    val contactId: String,
    val amount: String,
    val note: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    state: PayitAppState,
    onBack: () -> Unit,
    onNavigate: (PayitDestination) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    var torchOn by remember { mutableStateOf(false) }
    var showMyQrSheet by remember { mutableStateOf(false) }
    var recognizingTarget by remember { mutableStateOf<DemoQrTarget?>(null) }

    val demoTargets = remember {
        listOf(
            DemoQrTarget(
                label = "Scan Starbucks (₹340)",
                contactId = "b_starbucks",
                amount = "340",
                note = "Starbucks QR Payment"
            ),
            DemoQrTarget(
                label = "Scan Priya Nair",
                contactId = "c_priya",
                amount = "",
                note = ""
            ),
            DemoQrTarget(
                label = "Scan Electricity Bill (₹1,840)",
                contactId = "b_bescom",
                amount = "1840",
                note = "BESCOM Electricity Bill"
            )
        )
    }

    LaunchedEffect(recognizingTarget) {
        val target = recognizingTarget ?: return@LaunchedEffect
        delay(480L)
        recognizingTarget = null
        onNavigate(
            PayitDestination.SendMoney(
                contactId = target.contactId,
                initialAmount = target.amount,
                initialNote = target.note
            )
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserPos"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScannerBackdrop)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(
                brush = Brush.radialGradient(
                    colors = if (torchOn) {
                        listOf(
                            Color(0xFF334155).copy(alpha = 0.72f),
                            ScannerBackdrop
                        )
                    } else {
                        listOf(
                            Color(0xFF172033).copy(alpha = 0.55f),
                            ScannerBackdrop
                        )
                    },
                    center = Offset(w / 2f, h * 0.42f),
                    radius = w * 0.75f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayitSpacing.sm, vertical = PayitSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = PayitIcons.Close,
                        contentDescription = "Close scanner",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Scan any QR code",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { torchOn = !torchOn },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (torchOn) Color.White else Color.White.copy(alpha = 0.14f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = PayitIcons.FlashOn,
                                    contentDescription = "Toggle flashlight",
                                    tint = if (torchOn) ScannerBackdrop else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { recognizingTarget = demoTargets.first() },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.14f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = PayitIcons.Gallery,
                                    contentDescription = "Upload QR from gallery",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.6f))

            Box(
                modifier = Modifier.size(258.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cornerLen = 36.dp.toPx()
                    val strokeW = 4.5.dp.toPx()
                    val r = 20.dp.toPx()

                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.05f),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(r, r)
                    )

                    drawCornerBracket(GPayBlue, Offset(0f, 0f), cornerLen, r, strokeW, flipX = false, flipY = false)
                    drawCornerBracket(GPayRed, Offset(w, 0f), cornerLen, r, strokeW, flipX = true, flipY = false)
                    drawCornerBracket(GPayYellow, Offset(0f, h), cornerLen, r, strokeW, flipX = false, flipY = true)
                    drawCornerBracket(GPayGreen, Offset(w, h), cornerLen, r, strokeW, flipX = true, flipY = true)

                    val laserY = h * laserProgress
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                GPayBlue.copy(alpha = 0.9f),
                                Color(0xFF60A5FA),
                                GPayBlue.copy(alpha = 0.9f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(16.dp.toPx(), laserY),
                        end = Offset(w - 16.dp.toPx(), laserY),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = recognizingTarget != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(PayitRadius.pill),
                        color = colors.verifiedGreen,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = PayitIcons.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "QR recognized • Opening Pay",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Point your camera at any BharatQR, PhonePe, Paytm, or Google Pay QR",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(modifier = Modifier.weight(0.7f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TRY INSTANT DEMO SCAN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = PayitSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    demoTargets.forEach { target ->
                        Surface(
                            onClick = { recognizingTarget = target },
                            shape = RoundedCornerShape(PayitRadius.pill),
                            color = Color.White.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = PayitIcons.QrScanner,
                                    contentDescription = null,
                                    tint = Color(0xFF93C5FD),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = target.label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                onClick = { showMyQrSheet = true },
                shape = RoundedCornerShape(PayitRadius.pill),
                color = Color.White,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = PayitIcons.QrCode,
                        contentDescription = null,
                        tint = GPayBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Show my QR code",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF1F1F1F)
                    )
                }
            }
        }
    }

    if (showMyQrSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMyQrSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayitSpacing.xl)
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.userName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary
                )
                Text(
                    text = "${state.primaryAccount.bankName} ${state.primaryAccount.maskedNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(18.dp))
                GPayQrCodeView(
                    data = "upi://pay?pa=${state.userHandle}&pn=${state.userName}",
                    size = 212.dp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "UPI ID: ${state.userHandle}",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        clipboard.setText(AnnotatedString(state.userHandle))
                        showMyQrSheet = false
                        onShowMessage("UPI ID copied: ${state.userHandle}")
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
                    Icon(
                        imageVector = PayitIcons.Copy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy UPI ID")
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCornerBracket(
    color: Color,
    corner: Offset,
    armLen: Float,
    radius: Float,
    strokeWidth: Float,
    flipX: Boolean,
    flipY: Boolean
) {
    val sx = if (flipX) -1f else 1f
    val sy = if (flipY) -1f else 1f
    val path = Path().apply {
        moveTo(corner.x, corner.y + sy * armLen)
        lineTo(corner.x, corner.y + sy * radius)
        quadraticTo(corner.x, corner.y, corner.x + sx * radius, corner.y)
        lineTo(corner.x + sx * armLen, corner.y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}
