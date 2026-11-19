package com.example.payit

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.payit.model.PayitAppState
import com.example.payit.model.PayitDestination
import com.example.payit.model.rememberPayitAppState
import com.example.payit.ui.auth.AuthScreen
import com.example.payit.ui.designsystem.PayitRadius
import com.example.payit.ui.screens.BankTransferScreen
import com.example.payit.ui.screens.BillsAndRechargesScreen
import com.example.payit.ui.screens.CardsAndBanksScreen
import com.example.payit.ui.screens.CheckBalanceScreen
import com.example.payit.ui.screens.CibilScoreScreen
import com.example.payit.ui.screens.HomeScreen
import com.example.payit.ui.screens.MobileRechargeScreen
import com.example.payit.ui.screens.PersonChatScreen
import com.example.payit.ui.screens.PhoneOrUpiEntryScreen
import com.example.payit.ui.screens.ProfileScreen
import com.example.payit.ui.screens.QrScannerScreen
import com.example.payit.ui.screens.RewardsScreen
import com.example.payit.ui.screens.SearchOverlayScreen
import com.example.payit.ui.screens.SelfTransferScreen
import com.example.payit.ui.screens.SendMoneyScreen
import com.example.payit.ui.screens.TransactionHistoryScreen
import com.example.payit.ui.theme.PayitTheme
import com.example.payit.ui.theme.ThemeMode
import com.example.payit.ui.theme.isPayitDarkTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appState = rememberPayitAppState()
            val isDark = isPayitDarkTheme(appState.themeMode)
            val backStack = remember { mutableStateListOf<PayitDestination>(PayitDestination.Home) }
            var isForwardTransition by remember { mutableStateOf(true) }
            val currentDestination = backStack.lastOrNull() ?: PayitDestination.Home

            LaunchedEffect(isDark, appState.isAuthenticated, currentDestination) {
                val useDarkBars = isDark || (appState.isAuthenticated && currentDestination is PayitDestination.QrScanner)
                enableEdgeToEdge(
                    statusBarStyle = if (useDarkBars) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    },
                    navigationBarStyle = if (useDarkBars) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    }
                )
            }

            PayitTheme(themeMode = appState.themeMode) {
                PayitAppRoot(
                    appState = appState,
                    isDark = isDark,
                    backStack = backStack,
                    isForwardTransition = isForwardTransition,
                    onSetForward = { isForwardTransition = it }
                )
            }
        }
    }
}

@Composable
private fun PayitAppRoot(
    appState: PayitAppState,
    isDark: Boolean,
    backStack: MutableList<PayitDestination>,
    isForwardTransition: Boolean,
    onSetForward: (Boolean) -> Unit
) {
    val colors = PayitTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val showMessage: (String) -> Unit = { message ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    val navigateTo: (PayitDestination) -> Unit = { destination ->
        val top = backStack.lastOrNull()
        if (top != destination) {
            onSetForward(true)
            if (destination is PayitDestination.SendMoney &&
                (top is PayitDestination.QrScanner || top is PayitDestination.PhoneOrUpiEntry)
            ) {
                backStack.removeAt(backStack.lastIndex)
                val chatDest = PayitDestination.PersonChat(destination.contactId)
                if (backStack.lastOrNull() != chatDest) {
                    backStack.add(chatDest)
                }
            }
            if (top is PayitDestination.SendMoney && destination is PayitDestination.Rewards) {
                backStack.removeAt(backStack.lastIndex)
            }
            backStack.add(destination)
        }
    }

    val navigateBack: () -> Unit = {
        if (backStack.size > 1) {
            onSetForward(false)
            backStack.removeAt(backStack.lastIndex)
        }
    }

    BackHandler(enabled = appState.isAuthenticated && backStack.size > 1) {
        navigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AnimatedContent(
            targetState = appState.isAuthenticated,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(220)) },
            label = "authToHomeRoot"
        ) { authenticated ->
            if (!authenticated) {
                AuthScreen(
                    themeMode = appState.themeMode,
                    onToggleTheme = {
                        val next = when (appState.themeMode) {
                            ThemeMode.SYSTEM -> if (isDark) ThemeMode.LIGHT else ThemeMode.DARK
                            ThemeMode.LIGHT -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.LIGHT
                        }
                        appState.themeMode = next
                        showMessage("Switched to ${next.label} theme")
                    },
                    onAuthenticated = { fullName, dialCode, phoneDigits, isSignUp ->
                        backStack.clear()
                        backStack.add(PayitDestination.Home)
                        appState.completeAuthentication(
                            name = fullName,
                            dialCode = dialCode,
                            phoneDigits = phoneDigits,
                            isSignUp = isSignUp
                        )
                    },
                    onShowMessage = showMessage
                )
            } else {
                val currentDestination = backStack.lastOrNull() ?: PayitDestination.Home
                AnimatedContent(
                    targetState = currentDestination,
                    transitionSpec = {
                        if (isForwardTransition) {
                            (slideInHorizontally(tween(260)) { it / 6 } + fadeIn(tween(220)))
                                .togetherWith(slideOutHorizontally(tween(240)) { -it / 10 } + fadeOut(tween(180)))
                        } else {
                            (slideInHorizontally(tween(260)) { -it / 8 } + fadeIn(tween(220)))
                                .togetherWith(slideOutHorizontally(tween(240)) { it / 6 } + fadeOut(tween(180)))
                        }
                    },
                    label = "payitNavigation"
                ) { destination ->
                    when (destination) {
                        is PayitDestination.Home -> HomeScreen(
                            state = appState,
                            onNavigate = navigateTo,
                            onShowMessage = showMessage
                        )

                        is PayitDestination.SearchOverlay -> SearchOverlayScreen(
                            state = appState,
                            initialFilter = destination.initialFilter,
                            onBack = navigateBack,
                            onNavigate = navigateTo
                        )

                        is PayitDestination.QrScanner -> QrScannerScreen(
                            state = appState,
                            onBack = navigateBack,
                            onNavigate = navigateTo,
                            onShowMessage = showMessage
                        )

                        is PayitDestination.PersonChat -> PersonChatScreen(
                            state = appState,
                            contactId = destination.contactId,
                            onBack = navigateBack,
                            onNavigate = navigateTo,
                            onShowMessage = showMessage
                        )

                        is PayitDestination.SendMoney -> SendMoneyScreen(
                            state = appState,
                            contactId = destination.contactId,
                            initialAmount = destination.initialAmount,
                            initialNote = destination.initialNote,
                            onBack = navigateBack,
                            onNavigate = navigateTo,
                            onShowMessage = showMessage
                        )

                        is PayitDestination.PhoneOrUpiEntry -> PhoneOrUpiEntryScreen(
                            state = appState,
                            mode = destination.mode,
                            onBack = navigateBack,
                            onNavigate = navigateTo,
                            onShowMessage = showMessage
                        )

                        is PayitDestination.BankTransfer -> BankTransferScreen(
                            state = appState,
                            onBack = navigateBack,
                            onShowMessage = showMessage
                        )

                        is PayitDestination.SelfTransfer -> SelfTransferScreen(
                            state = appState,
                            onBack = navigateBack,
                            onShowMessage = showMessage
                        )

                        is PayitDestination.BillsAndRecharges -> BillsAndRechargesScreen(
                            state = appState,
                            initialCategory = destination.initialCategory,
                            onBack = navigateBack,
                            onNavigate = navigateTo,
                            onShowMessage = showMessage
                        )

                        is PayitDestination.MobileRecharge -> MobileRechargeScreen(
                            state = appState,
                            onBack = navigateBack,
                            onShowMessage = showMessage
                        )

                        is PayitDestination.Rewards -> RewardsScreen(
                            state = appState,
                            initialTab = destination.initialTab,
                            onBack = navigateBack,
                            onNavigate = navigateTo,
                            onShowMessage = showMessage
                        )

                        is PayitDestination.TransactionHistory -> TransactionHistoryScreen(
                            state = appState,
                            onBack = navigateBack,
                            onNavigate = navigateTo,
                            onShowMessage = showMessage
                        )

                        is PayitDestination.CheckBalance -> CheckBalanceScreen(
                            state = appState,
                            onBack = navigateBack,
                            onNavigate = navigateTo
                        )

                        is PayitDestination.CardsAndBanks -> CardsAndBanksScreen(
                            state = appState,
                            onBack = navigateBack,
                            onNavigate = navigateTo,
                            onShowMessage = showMessage
                        )

                        is PayitDestination.CibilScore -> CibilScoreScreen(
                            state = appState,
                            onBack = navigateBack
                        )

                        is PayitDestination.Profile -> ProfileScreen(
                            state = appState,
                            onBack = navigateBack,
                            onNavigate = navigateTo,
                            onShowMessage = showMessage
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(PayitRadius.medium),
                containerColor = if (colors.isDark) {
                    colors.surfaceContainerHigh
                } else {
                    MaterialTheme.colorScheme.inverseSurface
                },
                contentColor = if (colors.isDark) {
                    colors.textPrimary
                } else {
                    MaterialTheme.colorScheme.inverseOnSurface
                }
            )
        }
    }
}
