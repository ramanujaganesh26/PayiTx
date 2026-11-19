package com.example.payit.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payit.ui.designsystem.AuthSegmentedToggle
import com.example.payit.ui.designsystem.CountryDialCode
import com.example.payit.ui.designsystem.GPayNumericKeypad
import com.example.payit.ui.designsystem.GPayTopAppBar
import com.example.payit.ui.designsystem.OtpSixDigitRow
import com.example.payit.ui.designsystem.PayitIcons
import com.example.payit.ui.designsystem.PayitRadius
import com.example.payit.ui.designsystem.PayitSpacing
import com.example.payit.ui.designsystem.PhoneInputCard
import com.example.payit.ui.designsystem.SupportedCountryCodes
import com.example.payit.ui.designsystem.formatPhoneNumberDisplay
import com.example.payit.ui.theme.PayitTheme
import com.example.payit.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

enum class AuthTab(val label: String) {
    SIGN_IN("Sign In"),
    SIGN_UP("Sign Up")
}

private enum class AuthStage {
    CREDENTIALS,
    OTP_VERIFY,
    VERIFIED_SESSION
}

private fun generateSixDigitOtp(): String {
    val value = Random.nextInt(100_000, 999_999)
    val str = value.toString()
    return if (str == "000000") "482910" else str
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onAuthenticated: (fullName: String, dialCode: String, phoneDigits: String, isSignUp: Boolean) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val colors = PayitTheme.colors

    var activeTab by remember { mutableStateOf(AuthTab.SIGN_IN) }
    var stage by remember { mutableStateOf(AuthStage.CREDENTIALS) }

    var selectedCountry by remember { mutableStateOf(SupportedCountryCodes.first()) }
    var phoneDigits by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var acceptedTerms by remember { mutableStateOf(false) }

    var isSendingOtp by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("482910") }
    var otpInput by remember { mutableStateOf("") }
    var otpError by remember { mutableStateOf<String?>(null) }
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var otpChannelLabel by remember { mutableStateOf("SMS") }
    var resendSecondsLeft by remember { mutableIntStateOf(30) }
    var timerResetCycle by remember { mutableIntStateOf(0) }
    var showSmsBanner by remember { mutableStateOf(false) }

    var showCountrySheet by remember { mutableStateOf(false) }
    var showHelpSheet by remember { mutableStateOf(false) }

    // Back press during OTP verification returns to phone/credentials screen
    BackHandler(enabled = stage == AuthStage.OTP_VERIFY) {
        if (!isVerifyingOtp) {
            otpInput = ""
            otpError = null
            showSmsBanner = false
            stage = AuthStage.CREDENTIALS
        }
    }

    // Handle simulated OTP dispatch transition
    LaunchedEffect(isSendingOtp) {
        if (isSendingOtp) {
            delay(600L)
            generatedOtp = generateSixDigitOtp()
            otpInput = ""
            otpError = null
            otpChannelLabel = "SMS"
            resendSecondsLeft = 30
            timerResetCycle++
            isSendingOtp = false
            stage = AuthStage.OTP_VERIFY
            delay(280L)
            showSmsBanner = true
        }
    }

    // Handle 30-second resend countdown on OTP stage
    LaunchedEffect(stage, timerResetCycle) {
        if (stage == AuthStage.OTP_VERIFY) {
            while (resendSecondsLeft > 0) {
                delay(1000L)
                resendSecondsLeft--
            }
        }
    }

    // Handle simulated OTP verification transition
    LaunchedEffect(isVerifyingOtp) {
        if (isVerifyingOtp) {
            delay(550L)
            isVerifyingOtp = false
            if (otpInput == "000000") {
                otpError = "Invalid OTP code. Enter $generatedOtp or tap Auto-fill."
            } else {
                otpError = null
                showSmsBanner = false
                stage = AuthStage.VERIFIED_SESSION
            }
        }
    }

    fun triggerSendOtp() {
        val requiredDigits = selectedCountry.expectedDigits
        if (activeTab == AuthTab.SIGN_UP) {
            if (fullName.trim().length < 2) {
                onShowMessage("Please enter your full name")
                return
            }
            if (email.isNotBlank() && (!email.contains("@") || !email.contains("."))) {
                onShowMessage("Enter a valid email address or leave it blank")
                return
            }
            if (phoneDigits.length != requiredDigits) {
                onShowMessage("Enter a valid $requiredDigits-digit mobile number")
                return
            }
            if (!acceptedTerms) {
                onShowMessage("Please agree to the Terms of Service & Privacy Policy")
                return
            }
        } else {
            if (phoneDigits.length != requiredDigits) {
                onShowMessage("Enter a valid $requiredDigits-digit mobile number")
                return
            }
        }
        isSendingOtp = true
    }

    fun triggerVerifyOtp() {
        if (otpInput.length == 6 && !isVerifyingOtp) {
            otpError = null
            isVerifyingOtp = true
        }
    }

    AnimatedContent(
        targetState = stage,
        transitionSpec = {
            fadeIn(tween(260)) togetherWith fadeOut(tween(200))
        },
        label = "authStageTransition"
    ) { currentStage ->
        when (currentStage) {
            AuthStage.CREDENTIALS -> {
                CredentialsStage(
                    activeTab = activeTab,
                    onTabSelected = { tab ->
                        activeTab = tab
                    },
                    selectedCountry = selectedCountry,
                    phoneDigits = phoneDigits,
                    fullName = fullName,
                    email = email,
                    acceptedTerms = acceptedTerms,
                    isSendingOtp = isSendingOtp,
                    themeMode = themeMode,
                    onPhoneChange = { digits ->
                        phoneDigits = digits.filter { it.isDigit() }.take(selectedCountry.expectedDigits)
                    },
                    onFullNameChange = { fullName = it },
                    onEmailChange = { email = it },
                    onAcceptedTermsChange = { acceptedTerms = it },
                    onOpenCountryPicker = { showCountrySheet = true },
                    onOpenHelp = { showHelpSheet = true },
                    onToggleTheme = onToggleTheme,
                    onSendOtp = ::triggerSendOtp
                )
            }

            AuthStage.OTP_VERIFY -> {
                OtpVerifyStage(
                    country = selectedCountry,
                    phoneDigits = phoneDigits,
                    generatedOtp = generatedOtp,
                    otpInput = otpInput,
                    otpError = otpError,
                    isVerifying = isVerifyingOtp,
                    resendSecondsLeft = resendSecondsLeft,
                    showSmsBanner = showSmsBanner,
                    otpChannelLabel = otpChannelLabel,
                    onDigit = { digit ->
                        if (!isVerifyingOtp && otpInput.length < 6) {
                            otpError = null
                            val next = otpInput + digit
                            otpInput = next
                            if (next.length == 6) {
                                isVerifyingOtp = true
                            }
                        }
                    },
                    onBackspace = {
                        if (!isVerifyingOtp && otpInput.isNotEmpty()) {
                            otpError = null
                            otpInput = otpInput.dropLast(1)
                        }
                    },
                    onClear = {
                        if (!isVerifyingOtp) {
                            otpError = null
                            otpInput = ""
                        }
                    },
                    onAutoFillOtp = {
                        if (!isVerifyingOtp) {
                            otpError = null
                            otpInput = generatedOtp
                            isVerifyingOtp = true
                        }
                    },
                    onResend = { channel ->
                        generatedOtp = generateSixDigitOtp()
                        otpInput = ""
                        otpError = null
                        otpChannelLabel = channel
                        resendSecondsLeft = 30
                        timerResetCycle++
                        showSmsBanner = true
                        onShowMessage("New 6-digit OTP sent via $channel")
                    },
                    onVerifyClick = ::triggerVerifyOtp,
                    onEditPhone = {
                        if (!isVerifyingOtp) {
                            otpInput = ""
                            otpError = null
                            showSmsBanner = false
                            stage = AuthStage.CREDENTIALS
                        }
                    },
                    onToggleTheme = onToggleTheme
                )
            }

            AuthStage.VERIFIED_SESSION -> {
                VerifiedSessionStage(
                    authTab = activeTab,
                    fullName = fullName.trim().ifBlank { "Aarav Sharma" },
                    email = email.trim(),
                    country = selectedCountry,
                    phoneDigits = phoneDigits,
                    otpChannelLabel = otpChannelLabel,
                    onContinue = {
                        onAuthenticated(
                            fullName.trim(),
                            selectedCountry.dialCode,
                            phoneDigits,
                            activeTab == AuthTab.SIGN_UP
                        )
                    }
                )
            }
        }
    }

    // Country Dial Code Selector Sheet
    if (showCountrySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCountrySheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            CountryPickerSheetContent(
                selectedCountry = selectedCountry,
                onSelectCountry = { country ->
                    selectedCountry = country
                    phoneDigits = phoneDigits.take(country.expectedDigits)
                    showCountrySheet = false
                }
            )
        }
    }

    // Help & OTP Troubleshooting Sheet
    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colors.surface
        ) {
            HelpSignInSheetContent(
                onUseDemoNumber = {
                    selectedCountry = SupportedCountryCodes.first()
                    phoneDigits = "9820451720"
                    showHelpSheet = false
                    onShowMessage("Filled demo number: +91 98204 51720")
                },
                onClose = { showHelpSheet = false }
            )
        }
    }
}

@Composable
private fun AuthTopHeader(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    val colors = PayitTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = PayitSpacing.lg, vertical = PayitSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Brand Wordmark + Monogram
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.primaryAccent,
                shadowElevation = 2.dp,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "P",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )
                }
            }
            Column {
                Text(
                    text = "Payit",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
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
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "256-bit OTP Secured",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }
        }

        // Light/Dark Theme Switcher Pill
        Surface(
            onClick = onToggleTheme,
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
                    imageVector = PayitIcons.SunMoon,
                    contentDescription = "Switch theme",
                    tint = colors.primary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = themeMode.label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textPrimary
                )
            }
        }
    }
}

@Composable
private fun CredentialsStage(
    activeTab: AuthTab,
    onTabSelected: (AuthTab) -> Unit,
    selectedCountry: CountryDialCode,
    phoneDigits: String,
    fullName: String,
    email: String,
    acceptedTerms: Boolean,
    isSendingOtp: Boolean,
    themeMode: ThemeMode,
    onPhoneChange: (String) -> Unit,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onAcceptedTermsChange: (Boolean) -> Unit,
    onOpenCountryPicker: () -> Unit,
    onOpenHelp: () -> Unit,
    onToggleTheme: () -> Unit,
    onSendOtp: () -> Unit
) {
    val colors = PayitTheme.colors
    val gradientTop = if (colors.isDark) Color(0xFF13223F) else Color(0xFFE7F0FE)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Subtle Fintech Hero Backdrop Wash
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(listOf(gradientTop, colors.background))
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val ringColor = colors.primaryAccent.copy(alpha = if (colors.isDark) 0.14f else 0.08f)
                drawCircle(
                    color = ringColor,
                    radius = size.width * 0.44f,
                    center = Offset(size.width * 0.92f, size.height * 0.16f)
                )
                drawCircle(
                    color = ringColor,
                    radius = size.width * 0.30f,
                    center = Offset(size.width * 0.92f, size.height * 0.16f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            AuthTopHeader(
                themeMode = themeMode,
                onToggleTheme = onToggleTheme
            )

            // Sculpted Fintech Hero Showcase Card
            PayitUpiPassCard(
                modifier = Modifier.padding(horizontal = PayitSpacing.lg, vertical = 4.dp)
            )

            // Segmented Sign In / Sign Up Switcher
            Box(modifier = Modifier.padding(horizontal = PayitSpacing.lg, vertical = PayitSpacing.sm)) {
                AuthSegmentedToggle(
                    options = AuthTab.entries.map { it.label },
                    selectedIndex = activeTab.ordinal,
                    onSelect = { idx -> onTabSelected(AuthTab.entries[idx]) }
                )
            }

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                },
                modifier = Modifier.weight(1f),
                label = "authTabContent"
            ) { tab ->
                if (tab == AuthTab.SIGN_IN) {
                    SignInBody(
                        selectedCountry = selectedCountry,
                        phoneDigits = phoneDigits,
                        isSendingOtp = isSendingOtp,
                        onPhoneChange = onPhoneChange,
                        onOpenCountryPicker = onOpenCountryPicker,
                        onOpenHelp = onOpenHelp,
                        onSendOtp = onSendOtp
                    )
                } else {
                    SignUpBody(
                        selectedCountry = selectedCountry,
                        phoneDigits = phoneDigits,
                        fullName = fullName,
                        email = email,
                        acceptedTerms = acceptedTerms,
                        isSendingOtp = isSendingOtp,
                        onPhoneChange = onPhoneChange,
                        onFullNameChange = onFullNameChange,
                        onEmailChange = onEmailChange,
                        onAcceptedTermsChange = onAcceptedTermsChange,
                        onOpenCountryPicker = onOpenCountryPicker,
                        onOpenHelp = onOpenHelp,
                        onSendOtp = onSendOtp
                    )
                }
            }
        }
    }
}

@Composable
private fun SignInBody(
    selectedCountry: CountryDialCode,
    phoneDigits: String,
    isSendingOtp: Boolean,
    onPhoneChange: (String) -> Unit,
    onOpenCountryPicker: () -> Unit,
    onOpenHelp: () -> Unit,
    onSendOtp: () -> Unit
) {
    val colors = PayitTheme.colors
    val isPhoneValid = phoneDigits.length == selectedCountry.expectedDigits

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PayitSpacing.lg)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.divider),
                shadowElevation = if (colors.isDark) 0.dp else 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Welcome back",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sign in with your mobile number for instant 6-digit OTP verification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PhoneInputCard(
                        country = selectedCountry,
                        phoneDigits = phoneDigits,
                        onCountryClick = onOpenCountryPicker,
                        onPhoneChange = onPhoneChange,
                        readOnlyForKeypad = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Demo Number Quick-Fill + Digit Counter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { onPhoneChange("9820451720".take(selectedCountry.expectedDigits)) },
                            shape = RoundedCornerShape(PayitRadius.pill),
                            color = colors.primaryContainer.copy(alpha = 0.65f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = PayitIcons.Sparkles,
                                    contentDescription = null,
                                    tint = colors.onPrimaryContainer,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Try demo: 98204 51720",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.onPrimaryContainer
                                )
                            }
                        }

                        Text(
                            text = "${phoneDigits.length}/${selectedCountry.expectedDigits} digits",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPhoneValid) colors.verifiedGreen else colors.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onSendOtp,
                        enabled = isPhoneValid && !isSendingOtp,
                        shape = RoundedCornerShape(PayitRadius.pill),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryAccent,
                            contentColor = Color.White,
                            disabledContainerColor = colors.surfaceContainerHigh,
                            disabledContentColor = colors.textMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isSendingOtp) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sending 6-digit OTP...",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        } else {
                            Text(
                                text = "Get OTP",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenHelp)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = PayitIcons.HelpCircle,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Need help signing in?",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = colors.primary
                )
            }
        }

        // Tactile Numeric Keypad Surface
        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.divider),
            shadowElevation = 6.dp
        ) {
            GPayNumericKeypad(
                onDigitClick = { digit ->
                    if (!isSendingOtp && phoneDigits.length < selectedCountry.expectedDigits) {
                        onPhoneChange(phoneDigits + digit)
                    }
                },
                onBackspaceClick = {
                    if (!isSendingOtp && phoneDigits.isNotEmpty()) {
                        onPhoneChange(phoneDigits.dropLast(1))
                    }
                },
                onClearClick = {
                    if (!isSendingOtp) onPhoneChange("")
                },
                modifier = Modifier.padding(horizontal = PayitSpacing.lg, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun SignUpBody(
    selectedCountry: CountryDialCode,
    phoneDigits: String,
    fullName: String,
    email: String,
    acceptedTerms: Boolean,
    isSendingOtp: Boolean,
    onPhoneChange: (String) -> Unit,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onAcceptedTermsChange: (Boolean) -> Unit,
    onOpenCountryPicker: () -> Unit,
    onOpenHelp: () -> Unit,
    onSendOtp: () -> Unit
) {
    val colors = PayitTheme.colors
    val isPhoneValid = phoneDigits.length == selectedCountry.expectedDigits
    val isReady = fullName.trim().length >= 2 && isPhoneValid && acceptedTerms

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primaryAccent,
        unfocusedBorderColor = colors.divider,
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PayitSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Column {
            Text(
                text = "Create your account",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Set up your Payit profile and verify your mobile number with an instant OTP.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }

        // Full Name
        OutlinedTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = { Text("Full Name") },
            placeholder = { Text("e.g. Aarav Sharma") },
            leadingIcon = {
                Icon(
                    imageVector = PayitIcons.Person,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(PayitRadius.medium),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Optional Email
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email Address (Optional)") },
            placeholder = { Text("aarav@example.com") },
            leadingIcon = {
                Icon(
                    imageVector = PayitIcons.Mail,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(PayitRadius.medium),
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Mobile Number Input Card (editable directly via soft keyboard)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mobile Number (OTP required)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                    color = colors.textPrimary
                )
                Text(
                    text = "${phoneDigits.length}/${selectedCountry.expectedDigits}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPhoneValid) colors.verifiedGreen else colors.textMuted
                )
            }
            PhoneInputCard(
                country = selectedCountry,
                phoneDigits = phoneDigits,
                onCountryClick = onOpenCountryPicker,
                onPhoneChange = onPhoneChange,
                readOnlyForKeypad = false,
                onDone = onSendOtp
            )
        }

        // Terms & Privacy Checkbox Card
        Surface(
            onClick = { onAcceptedTermsChange(!acceptedTerms) },
            shape = RoundedCornerShape(PayitRadius.medium),
            color = colors.surfaceContainer,
            border = BorderStroke(
                width = 1.dp,
                color = if (acceptedTerms) colors.primary.copy(alpha = 0.5f) else colors.divider
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = onAcceptedTermsChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.primaryAccent,
                        checkmarkColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "I agree to Payit's Terms of Service & Privacy Policy and consent to SMS/WhatsApp OTP verification.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }

        // Create Account & Send OTP CTA
        Button(
            onClick = onSendOtp,
            enabled = !isSendingOtp,
            shape = RoundedCornerShape(PayitRadius.pill),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isReady) colors.primaryAccent else colors.primaryAccent.copy(alpha = 0.7f),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isSendingOtp) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Sending 6-digit OTP...",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            } else {
                Text(
                    text = "Create Account & Send OTP",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        // Footer Help Link
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenHelp)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = PayitIcons.HelpCircle,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Need help signing up?",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = colors.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun OtpVerifyStage(
    country: CountryDialCode,
    phoneDigits: String,
    generatedOtp: String,
    otpInput: String,
    otpError: String?,
    isVerifying: Boolean,
    resendSecondsLeft: Int,
    showSmsBanner: Boolean,
    otpChannelLabel: String,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onAutoFillOtp: () -> Unit,
    onResend: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onEditPhone: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val colors = PayitTheme.colors
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(otpError) {
        if (otpError != null) {
            val sequence = listOf(-14f, 14f, -10f, 10f, -5f, 5f, 0f)
            sequence.forEach { target ->
                shakeOffset.animateTo(target, tween(45))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
    ) {
        GPayTopAppBar(
            title = "OTP Verification",
            subtitle = "Edit phone number",
            onBack = onEditPhone,
            trailingContent = {
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = PayitIcons.SunMoon,
                        contentDescription = "Switch theme",
                        tint = colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        // Simulated Incoming SMS / WhatsApp OTP Notification Banner
        AnimatedVisibility(
            visible = showSmsBanner,
            enter = slideInVertically(tween(300)) { -it / 2 } + expandVertically() + fadeIn(),
            exit = slideOutVertically() + shrinkVertically() + fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(PayitRadius.large),
                color = colors.primaryContainer,
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f)),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PayitSpacing.lg, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colors.primaryAccent,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = PayitIcons.SmsBubble,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$otpChannelLabel from PAYIT • Just now",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.onPrimaryContainer
                        )
                        Text(
                            text = "Your verification OTP is $generatedOtp",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = colors.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = onAutoFillOtp,
                        shape = RoundedCornerShape(PayitRadius.pill),
                        color = colors.primaryAccent
                    ) {
                        Text(
                            text = "Auto-fill",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // Main OTP Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PayitSpacing.lg),
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.divider),
                shadowElevation = if (colors.isDark) 0.dp else 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(colors.primaryContainer.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(colors.primaryAccent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = PayitIcons.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enter 6-digit code",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Sent via $otpChannelLabel to ${country.dialCode} ${formatPhoneNumberDisplay(phoneDigits)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Surface(
                            onClick = onEditPhone,
                            shape = RoundedCornerShape(PayitRadius.pill),
                            color = colors.surfaceContainer,
                            border = BorderStroke(1.dp, colors.divider)
                        ) {
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Box(
                        modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    ) {
                        OtpSixDigitRow(
                            otpCode = otpInput,
                            isError = otpError != null
                        )
                    }

                    AnimatedVisibility(visible = otpError != null) {
                        Text(
                            text = otpError.orEmpty(),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = colors.error,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Countdown & Resend Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (resendSecondsLeft > 0) {
                            Text(
                                text = "Resend in 00:${resendSecondsLeft.toString().padStart(2, '0')}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = colors.textSecondary
                            )
                        } else {
                            Text(
                                text = "Didn't receive?",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                onClick = { onResend("SMS") },
                                enabled = resendSecondsLeft == 0,
                                shape = RoundedCornerShape(PayitRadius.pill),
                                color = if (resendSecondsLeft == 0) colors.primaryContainer else colors.surfaceContainer
                            ) {
                                Text(
                                    text = "Resend SMS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (resendSecondsLeft == 0) colors.onPrimaryContainer else colors.textMuted,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                onClick = { onResend("WhatsApp") },
                                shape = RoundedCornerShape(PayitRadius.pill),
                                color = colors.verifiedGreenContainer
                            ) {
                                Text(
                                    text = "WhatsApp",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (colors.isDark) colors.verifiedGreen else Color(0xFF0A4D1F),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onVerifyClick,
                        enabled = otpInput.length == 6 && !isVerifying,
                        shape = RoundedCornerShape(PayitRadius.pill),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryAccent,
                            contentColor = Color.White,
                            disabledContainerColor = colors.surfaceContainerHigh,
                            disabledContentColor = colors.textMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Verifying OTP...",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        } else {
                            Text(
                                text = "Verify OTP",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        }

        // Tactile Numeric Keypad for 6-digit OTP Entry
        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.divider),
            shadowElevation = 6.dp
        ) {
            GPayNumericKeypad(
                onDigitClick = onDigit,
                onBackspaceClick = onBackspace,
                onClearClick = onClear,
                modifier = Modifier.padding(horizontal = PayitSpacing.lg, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun VerifiedSessionStage(
    authTab: AuthTab,
    fullName: String,
    email: String,
    country: CountryDialCode,
    phoneDigits: String,
    otpChannelLabel: String,
    onContinue: () -> Unit
) {
    val colors = PayitTheme.colors
    val checkScale = remember { Animatable(0.45f) }

    LaunchedEffect(Unit) {
        checkScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
        )
        delay(950L)
        onContinue()
    }

    val pulseTransition = rememberInfiniteTransition(label = "verifiedRipple")
    val ringScale by pulseTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringScale"
    )
    val ringAlpha by pulseTransition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = PayitSpacing.xl, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // Animated Concentric Emerald Checkmark
        Box(
            modifier = Modifier.size(136.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(ringScale)
                    .alpha(ringAlpha)
                    .background(colors.verifiedGreen, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(checkScale.value)
                    .background(colors.verifiedGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = PayitIcons.Check,
                    contentDescription = "Phone verified",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Phone Verified!",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (authTab == AuthTab.SIGN_UP) {
                "Your Payit account has been created and secured with OTP."
            } else {
                "Welcome back! Your mobile session is authenticated."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Authenticated Session Summary Card
        Surface(
            shape = RoundedCornerShape(PayitRadius.xl),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.divider),
            shadowElevation = if (colors.isDark) 0.dp else 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (authTab == AuthTab.SIGN_UP) "New Account Created" else "Signed In Session",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(PayitRadius.pill),
                        color = colors.verifiedGreenContainer
                    ) {
                        Text(
                            text = "OTP Verified",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (colors.isDark) colors.verifiedGreen else Color(0xFF0A4D1F),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = colors.divider.copy(alpha = 0.7f))

                if (authTab == AuthTab.SIGN_UP) {
                    SessionDetailRow("Account holder", fullName)
                    if (email.isNotBlank()) {
                        SessionDetailRow("Email", email)
                    }
                }
                SessionDetailRow(
                    "Mobile number",
                    "${country.dialCode} ${formatPhoneNumberDisplay(phoneDigits)}"
                )
                SessionDetailRow("Verification channel", "$otpChannelLabel 6-digit OTP")
                SessionDetailRow("Security", "Device & SIM bound • Active")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            shape = RoundedCornerShape(PayitRadius.pill),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryAccent,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Continue to Payit Home",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun SessionDetailRow(label: String, value: String) {
    val colors = PayitTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CountryPickerSheetContent(
    selectedCountry: CountryDialCode,
    onSelectCountry: (CountryDialCode) -> Unit
) {
    val colors = PayitTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PayitSpacing.xl)
            .padding(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Select country code",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        SupportedCountryCodes.forEach { country ->
            val selected = country.isoCode == selectedCountry.isoCode
            Surface(
                onClick = { onSelectCountry(country) },
                shape = RoundedCornerShape(PayitRadius.medium),
                color = if (selected) colors.primaryContainer.copy(alpha = 0.55f) else colors.surfaceContainer,
                border = BorderStroke(
                    width = if (selected) 1.5.dp else 1.dp,
                    color = if (selected) colors.primary else colors.divider
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = country.isoCode,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.primary,
                        modifier = Modifier.width(38.dp)
                    )
                    Text(
                        text = country.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = country.dialCode,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpSignInSheetContent(
    onUseDemoNumber: () -> Unit,
    onClose: () -> Unit
) {
    val colors = PayitTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PayitSpacing.xl)
            .padding(bottom = 28.dp)
    ) {
        Text(
            text = "Phone & OTP Verification Help",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "• Enter your 10-digit mobile number and tap Get OTP.\n" +
                "• A simulated SMS notification with your 6-digit OTP appears automatically so you can tap Auto-fill or enter it on the keypad.\n" +
                "• To test an invalid OTP state, enter 000000.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(18.dp))
        Button(
            onClick = onUseDemoNumber,
            shape = RoundedCornerShape(PayitRadius.pill),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryAccent,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Use Demo Number (+91 98204 51720)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onClose,
            shape = RoundedCornerShape(PayitRadius.pill),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Text("Close")
        }
    }
}

@Composable
private fun PayitUpiPassCard(modifier: Modifier = Modifier) {
    val colors = PayitTheme.colors
    val cardGradient = if (colors.isDark) {
        Brush.linearGradient(listOf(Color(0xFF15274D), Color(0xFF0E1B38), Color(0xFF1A365D)))
    } else {
        Brush.linearGradient(listOf(Color(0xFF0B57D0), Color(0xFF1A73E8), Color(0xFF2563EB)))
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(cardGradient)
                .padding(horizontal = 16.dp, vertical = 13.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Gold EMV Chip Graphic
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFBBF24),
                            border = BorderStroke(0.8.dp, Color(0xFFFEF3C7)),
                            modifier = Modifier.size(width = 30.dp, height = 22.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val lineColor = Color(0xFF92400E).copy(alpha = 0.55f)
                                drawLine(
                                    color = lineColor,
                                    start = Offset(0f, size.height * 0.5f),
                                    end = Offset(size.width, size.height * 0.5f),
                                    strokeWidth = 1.dp.toPx()
                                )
                                drawLine(
                                    color = lineColor,
                                    start = Offset(size.width * 0.5f, 0f),
                                    end = Offset(size.width * 0.5f, size.height),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }
                        Text(
                            text = "INSTANT UPI • ZERO FEES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.9.sp
                            ),
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                    Icon(
                        imageVector = PayitIcons.VerifiedShield,
                        contentDescription = null,
                        tint = Color(0xFF6DD58C),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("UPI Lite", "RuPay Credit", "BharatQR").forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(PayitRadius.pill),
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
