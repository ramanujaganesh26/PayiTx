package com.example.payit.ui.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Unified 24dp vector iconography for Payit Auth, GPay Home, Payments, Bills & Rewards.
 */
object PayitIcons {

    // ---- Navigation & Utility ----

    val Search: ImageVector by lazy {
        buildIcon("Search") {
            strokePath {
                circle(10.5f, 10.5f, 6.5f)
                moveTo(15.2f, 15.2f)
                lineTo(20.5f, 20.5f)
            }
        }
    }

    val ArrowBack: ImageVector by lazy {
        buildIcon("ArrowBack") {
            strokePath {
                moveTo(19f, 12f)
                lineTo(5f, 12f)
                moveTo(11f, 6f)
                lineTo(5f, 12f)
                lineTo(11f, 18f)
            }
        }
    }

    val Close: ImageVector by lazy {
        buildIcon("Close") {
            strokePath {
                moveTo(6f, 6f)
                lineTo(18f, 18f)
                moveTo(18f, 6f)
                lineTo(6f, 18f)
            }
        }
    }

    val ChevronRight: ImageVector by lazy {
        buildIcon("ChevronRight") {
            strokePath {
                moveTo(9f, 6f)
                lineTo(15f, 12f)
                lineTo(9f, 18f)
            }
        }
    }

    val ChevronDown: ImageVector by lazy {
        buildIcon("ChevronDown") {
            strokePath {
                moveTo(6f, 9f)
                lineTo(12f, 15f)
                lineTo(18f, 9f)
            }
        }
    }

    val ChevronUp: ImageVector by lazy {
        buildIcon("ChevronUp") {
            strokePath {
                moveTo(6f, 15f)
                lineTo(12f, 9f)
                lineTo(18f, 15f)
            }
        }
    }

    val Copy: ImageVector by lazy {
        buildIcon("Copy") {
            strokePath {
                roundedRect(9f, 9f, 20f, 20f, 2f)
                moveTo(6f, 15f)
                lineTo(5f, 15f)
                curveTo(3.9f, 15f, 3f, 14.1f, 3f, 13f)
                lineTo(3f, 5f)
                curveTo(3f, 3.9f, 3.9f, 3f, 5f, 3f)
                lineTo(13f, 3f)
                curveTo(14.1f, 3f, 15f, 3.9f, 15f, 5f)
                lineTo(15f, 6f)
            }
        }
    }

    val Share: ImageVector by lazy {
        buildIcon("Share") {
            strokePath {
                circle(18f, 5f, 2.5f)
                circle(6f, 12f, 2.5f)
                circle(18f, 19f, 2.5f)
                moveTo(8.3f, 10.8f)
                lineTo(15.7f, 6.2f)
                moveTo(8.3f, 13.2f)
                lineTo(15.7f, 17.8f)
            }
        }
    }

    val Check: ImageVector by lazy {
        buildIcon("Check") {
            strokePath(lineWidth = 2.3f) {
                moveTo(4.5f, 12.5f)
                lineTo(9.5f, 17.5f)
                lineTo(19.5f, 7f)
            }
        }
    }

    val VerifiedShield: ImageVector by lazy {
        buildIcon("VerifiedShield") {
            fillPath {
                moveTo(12f, 2f)
                lineTo(4f, 5.5f)
                lineTo(4f, 11.2f)
                curveTo(4f, 16.3f, 7.4f, 20.9f, 12f, 22.2f)
                curveTo(16.6f, 20.9f, 20f, 16.3f, 20f, 11.2f)
                lineTo(20f, 5.5f)
                close()
                moveTo(10.5f, 15.8f)
                lineTo(7.2f, 12.5f)
                lineTo(8.5f, 11.2f)
                lineTo(10.5f, 13.2f)
                lineTo(15.6f, 8.1f)
                lineTo(16.9f, 9.4f)
                close()
            }
        }
    }

    val Lock: ImageVector by lazy {
        buildIcon("Lock") {
            strokePath {
                roundedRect(5f, 11f, 19f, 21f, 2.5f)
                moveTo(8f, 11f)
                lineTo(8f, 7.5f)
                curveTo(8f, 5.1f, 9.8f, 3.3f, 12f, 3.3f)
                curveTo(14.2f, 3.3f, 16f, 5.1f, 16f, 7.5f)
                lineTo(16f, 11f)
                moveTo(12f, 15f)
                lineTo(12f, 17f)
            }
        }
    }

    val Backspace: ImageVector by lazy {
        buildIcon("Backspace") {
            strokePath {
                moveTo(9f, 5f)
                lineTo(20f, 5f)
                curveTo(21.1f, 5f, 22f, 5.9f, 22f, 7f)
                lineTo(22f, 17f)
                curveTo(22f, 18.1f, 21.1f, 19f, 20f, 19f)
                lineTo(9f, 19f)
                lineTo(3f, 12f)
                close()
                moveTo(12.5f, 9.5f)
                lineTo(17.5f, 14.5f)
                moveTo(17.5f, 9.5f)
                lineTo(12.5f, 14.5f)
            }
        }
    }

    val FlashOn: ImageVector by lazy {
        buildIcon("FlashOn") {
            strokePath {
                moveTo(13f, 2.5f)
                lineTo(5.5f, 13f)
                lineTo(11.5f, 13f)
                lineTo(10.5f, 21.5f)
                lineTo(18.5f, 10.5f)
                lineTo(12.5f, 10.5f)
                close()
            }
        }
    }

    val Gallery: ImageVector by lazy {
        buildIcon("Gallery") {
            strokePath {
                roundedRect(3.5f, 4.5f, 20.5f, 19.5f, 2.5f)
                circle(9f, 9.5f, 1.5f)
                moveTo(4.5f, 17.5f)
                lineTo(9.5f, 12.8f)
                lineTo(13.5f, 16.5f)
                lineTo(16f, 14f)
                lineTo(20f, 18f)
            }
        }
    }

    val Sparkles: ImageVector by lazy {
        buildIcon("Sparkles") {
            strokePath {
                moveTo(11f, 3f)
                lineTo(12.7f, 8.3f)
                lineTo(18f, 10f)
                lineTo(12.7f, 11.7f)
                lineTo(11f, 17f)
                lineTo(9.3f, 11.7f)
                lineTo(4f, 10f)
                lineTo(9.3f, 8.3f)
                close()
                moveTo(18.5f, 14.5f)
                lineTo(19.3f, 17.2f)
                lineTo(22f, 18f)
                lineTo(19.3f, 18.8f)
                lineTo(18.5f, 21.5f)
                lineTo(17.7f, 18.8f)
                lineTo(15f, 18f)
                lineTo(17.7f, 17.2f)
                close()
            }
        }
    }

    val Send: ImageVector by lazy {
        buildIcon("Send") {
            strokePath {
                moveTo(21f, 3.5f)
                lineTo(10.5f, 14f)
                moveTo(21f, 3.5f)
                lineTo(14.5f, 21f)
                lineTo(10.5f, 14f)
                lineTo(3.5f, 10f)
                close()
            }
        }
    }

    val Call: ImageVector by lazy {
        buildIcon("Call") {
            strokePath {
                moveTo(6.2f, 4f)
                lineTo(9.3f, 4f)
                lineTo(10.6f, 8.2f)
                lineTo(8.5f, 9.8f)
                curveTo(9.7f, 12.2f, 11.8f, 14.3f, 14.2f, 15.5f)
                lineTo(15.8f, 13.4f)
                lineTo(20f, 14.7f)
                lineTo(20f, 17.8f)
                curveTo(20f, 19.1f, 18.9f, 20.1f, 17.6f, 20f)
                curveTo(10.3f, 19.5f, 4.5f, 13.7f, 4f, 6.4f)
                curveTo(3.9f, 5.1f, 4.9f, 4f, 6.2f, 4f)
                close()
            }
        }
    }

    val SunMoon: ImageVector by lazy {
        buildIcon("SunMoon") {
            strokePath {
                circle(12f, 12f, 4.5f)
                moveTo(12f, 2.5f)
                lineTo(12f, 4.5f)
                moveTo(12f, 19.5f)
                lineTo(12f, 21.5f)
                moveTo(2.5f, 12f)
                lineTo(4.5f, 12f)
                moveTo(19.5f, 12f)
                lineTo(21.5f, 12f)
                moveTo(5.3f, 5.3f)
                lineTo(6.7f, 6.7f)
                moveTo(17.3f, 17.3f)
                lineTo(18.7f, 18.7f)
                moveTo(18.7f, 5.3f)
                lineTo(17.3f, 6.7f)
                moveTo(6.7f, 17.3f)
                lineTo(5.3f, 18.7f)
            }
        }
    }

    val Home: ImageVector by lazy {
        buildIcon("Home") {
            strokePath {
                moveTo(3.5f, 10.5f)
                lineTo(12f, 3.8f)
                lineTo(20.5f, 10.5f)
                moveTo(5.5f, 9.2f)
                lineTo(5.5f, 19.2f)
                curveTo(5.5f, 19.9f, 6.1f, 20.5f, 6.8f, 20.5f)
                lineTo(17.2f, 20.5f)
                curveTo(17.9f, 20.5f, 18.5f, 19.9f, 18.5f, 19.2f)
                lineTo(18.5f, 9.2f)
                moveTo(9.8f, 20.5f)
                lineTo(9.8f, 14.5f)
                lineTo(14.2f, 14.5f)
                lineTo(14.2f, 20.5f)
            }
        }
    }

    val Eye: ImageVector by lazy {
        buildIcon("Eye") {
            strokePath {
                moveTo(2.5f, 12f)
                curveTo(4.8f, 7.6f, 8.2f, 5.8f, 12f, 5.8f)
                curveTo(15.8f, 5.8f, 19.2f, 7.6f, 21.5f, 12f)
                curveTo(19.2f, 16.4f, 15.8f, 18.2f, 12f, 18.2f)
                curveTo(8.2f, 18.2f, 4.8f, 16.4f, 2.5f, 12f)
                close()
                circle(12f, 12f, 2.8f)
            }
        }
    }

    val EyeOff: ImageVector by lazy {
        buildIcon("EyeOff") {
            strokePath {
                moveTo(3f, 3f)
                lineTo(21f, 21f)
                moveTo(10.2f, 6f)
                curveTo(10.8f, 5.9f, 11.4f, 5.8f, 12f, 5.8f)
                curveTo(15.8f, 5.8f, 19.2f, 7.6f, 21.5f, 12f)
                curveTo(20.6f, 13.7f, 19.5f, 15.1f, 18.2f, 16.1f)
                moveTo(14.6f, 17.8f)
                curveTo(13.8f, 18.1f, 12.9f, 18.2f, 12f, 18.2f)
                curveTo(8.2f, 18.2f, 4.8f, 16.4f, 2.5f, 12f)
                curveTo(3.5f, 10.1f, 4.8f, 8.6f, 6.4f, 7.5f)
            }
        }
    }

    val Person: ImageVector by lazy {
        buildIcon("Person") {
            strokePath {
                circle(12f, 8f, 3.8f)
                moveTo(4.5f, 20f)
                curveTo(4.5f, 16.2f, 7.6f, 14.2f, 12f, 14.2f)
                curveTo(16.4f, 14.2f, 19.5f, 16.2f, 19.5f, 20f)
            }
        }
    }

    val Mail: ImageVector by lazy {
        buildIcon("Mail") {
            strokePath {
                roundedRect(3f, 5.2f, 21f, 18.8f, 2.2f)
                moveTo(3.8f, 6.4f)
                lineTo(12f, 12.8f)
                lineTo(20.2f, 6.4f)
            }
        }
    }

    val SmsBubble: ImageVector by lazy {
        buildIcon("SmsBubble") {
            strokePath {
                moveTo(4f, 5.5f)
                curveTo(4f, 4.4f, 4.9f, 3.5f, 6f, 3.5f)
                lineTo(18f, 3.5f)
                curveTo(19.1f, 3.5f, 20f, 4.4f, 20f, 5.5f)
                lineTo(20f, 14.5f)
                curveTo(20f, 15.6f, 19.1f, 16.5f, 18f, 16.5f)
                lineTo(8.5f, 16.5f)
                lineTo(4f, 20.5f)
                close()
                moveTo(8f, 9f)
                lineTo(16f, 9f)
                moveTo(8f, 12.2f)
                lineTo(13.2f, 12.2f)
            }
        }
    }

    val HelpCircle: ImageVector by lazy {
        buildIcon("HelpCircle") {
            strokePath {
                circle(12f, 12f, 9.2f)
                moveTo(9.4f, 9.4f)
                curveTo(9.7f, 7.9f, 11f, 7f, 12.3f, 7.2f)
                curveTo(13.7f, 7.4f, 14.7f, 8.5f, 14.6f, 9.8f)
                curveTo(14.5f, 11.2f, 12.6f, 11.8f, 12f, 13f)
                lineTo(12f, 14f)
                moveTo(12f, 17f)
                lineTo(12f, 17.1f)
            }
        }
    }

    val LogOut: ImageVector by lazy {
        buildIcon("LogOut") {
            strokePath {
                moveTo(10f, 4.5f)
                lineTo(5.8f, 4.5f)
                curveTo(4.7f, 4.5f, 3.8f, 5.4f, 3.8f, 6.5f)
                lineTo(3.8f, 17.5f)
                curveTo(3.8f, 18.6f, 4.7f, 19.5f, 5.8f, 19.5f)
                lineTo(10f, 19.5f)
                moveTo(15.5f, 7.8f)
                lineTo(20f, 12f)
                lineTo(15.5f, 16.2f)
                moveTo(9.5f, 12f)
                lineTo(19.5f, 12f)
            }
        }
    }

    // ---- GPay 8-Grid Primary Actions ----

    val QrScanner: ImageVector by lazy {
        buildIcon("QrScanner") {
            strokePath(lineWidth = 2f) {
                moveTo(3.5f, 8f)
                lineTo(3.5f, 5.5f)
                curveTo(3.5f, 4.4f, 4.4f, 3.5f, 5.5f, 3.5f)
                lineTo(8f, 3.5f)

                moveTo(16f, 3.5f)
                lineTo(18.5f, 3.5f)
                curveTo(19.6f, 3.5f, 20.5f, 4.4f, 20.5f, 5.5f)
                lineTo(20.5f, 8f)

                moveTo(20.5f, 16f)
                lineTo(20.5f, 18.5f)
                curveTo(20.5f, 19.6f, 19.6f, 20.5f, 18.5f, 20.5f)
                lineTo(16f, 20.5f)

                moveTo(8f, 20.5f)
                lineTo(5.5f, 20.5f)
                curveTo(4.4f, 20.5f, 3.5f, 19.6f, 3.5f, 18.5f)
                lineTo(3.5f, 16f)

                moveTo(5.5f, 12f)
                lineTo(18.5f, 12f)
                roundedRect(7f, 7f, 10.5f, 10.5f, 0.7f)
                roundedRect(13.5f, 7f, 17f, 10.5f, 0.7f)
                roundedRect(7f, 13.5f, 10.5f, 17f, 0.7f)
            }
        }
    }

    val PayContacts: ImageVector by lazy {
        buildIcon("PayContacts") {
            strokePath {
                roundedRect(4.5f, 3.5f, 19.5f, 20.5f, 2.5f)
                circle(12f, 9.5f, 2.6f)
                moveTo(8f, 17.5f)
                curveTo(8f, 14.8f, 9.8f, 13.8f, 12f, 13.8f)
                curveTo(14.2f, 13.8f, 16f, 14.8f, 16f, 17.5f)
                moveTo(2.5f, 8f)
                lineTo(5.5f, 8f)
                moveTo(2.5f, 12f)
                lineTo(5.5f, 12f)
                moveTo(2.5f, 16f)
                lineTo(5.5f, 16f)
            }
        }
    }

    val PayPhone: ImageVector by lazy {
        buildIcon("PayPhone") {
            strokePath {
                roundedRect(6.5f, 2.8f, 17.5f, 21.2f, 2.5f)
                moveTo(10.5f, 5.5f)
                lineTo(13.5f, 5.5f)
                moveTo(11f, 18.2f)
                lineTo(13f, 18.2f)
                moveTo(10f, 9.5f)
                lineTo(14f, 9.5f)
                moveTo(10f, 11.5f)
                lineTo(14f, 11.5f)
                moveTo(12.2f, 9.5f)
                curveTo(13.3f, 9.5f, 13.6f, 12.2f, 11.2f, 13.2f)
                lineTo(13.8f, 15.5f)
            }
        }
    }

    val BankTransfer: ImageVector by lazy {
        buildIcon("BankTransfer") {
            strokePath {
                moveTo(3f, 9f)
                lineTo(12f, 3.8f)
                lineTo(21f, 9f)
                close()
                moveTo(5.5f, 9f)
                lineTo(5.5f, 17f)
                moveTo(10f, 9f)
                lineTo(10f, 17f)
                moveTo(14f, 9f)
                lineTo(14f, 17f)
                moveTo(18.5f, 9f)
                lineTo(18.5f, 17f)
                moveTo(3f, 17.5f)
                lineTo(21f, 17.5f)
                moveTo(2.5f, 20.5f)
                lineTo(21.5f, 20.5f)
            }
        }
    }

    val UpiAt: ImageVector by lazy {
        buildIcon("UpiAt") {
            strokePath {
                circle(12f, 12f, 3.5f)
                moveTo(15.5f, 10f)
                lineTo(15.5f, 14f)
                curveTo(15.5f, 15.5f, 17.5f, 16f, 19f, 14.5f)
                curveTo(20.6f, 12.8f, 20.5f, 9.5f, 19f, 6.8f)
                curveTo(17.1f, 3.7f, 13.2f, 2.6f, 9.5f, 3.6f)
                curveTo(5.5f, 4.7f, 3f, 8.6f, 3.5f, 12.8f)
                curveTo(4f, 17.3f, 7.8f, 20.6f, 12.4f, 20.5f)
                curveTo(14.5f, 20.5f, 16.4f, 19.7f, 17.8f, 18.5f)
            }
        }
    }

    val SelfTransfer: ImageVector by lazy {
        buildIcon("SelfTransfer") {
            strokePath {
                moveTo(4f, 8.5f)
                lineTo(19f, 8.5f)
                moveTo(15.2f, 4.8f)
                lineTo(19f, 8.5f)
                lineTo(15.2f, 12.2f)

                moveTo(20f, 15.5f)
                lineTo(5f, 15.5f)
                moveTo(8.8f, 11.8f)
                lineTo(5f, 15.5f)
                lineTo(8.8f, 19.2f)
            }
        }
    }

    val PayBills: ImageVector by lazy {
        buildIcon("PayBills") {
            strokePath {
                moveTo(5.5f, 3.5f)
                lineTo(18.5f, 3.5f)
                lineTo(18.5f, 20.5f)
                lineTo(16f, 18.7f)
                lineTo(13.5f, 20.5f)
                lineTo(11f, 18.7f)
                lineTo(8.5f, 20.5f)
                lineTo(5.5f, 18.7f)
                close()
                moveTo(9f, 8.5f)
                lineTo(15f, 8.5f)
                moveTo(9f, 12f)
                lineTo(15f, 12f)
                moveTo(9f, 15.5f)
                lineTo(12.5f, 15.5f)
            }
        }
    }

    val MobileRecharge: ImageVector by lazy {
        buildIcon("MobileRecharge") {
            strokePath {
                roundedRect(6.5f, 2.8f, 17.5f, 21.2f, 2.4f)
                moveTo(12.6f, 7.5f)
                lineTo(9.8f, 12.5f)
                lineTo(12.5f, 12.5f)
                lineTo(11.4f, 16.8f)
                lineTo(14.5f, 11.5f)
                lineTo(11.8f, 11.5f)
                close()
            }
        }
    }

    // ---- Bills & Finance Icons ----

    val Electricity: ImageVector by lazy {
        buildIcon("Electricity") {
            strokePath {
                moveTo(9f, 15.5f)
                curveTo(6.8f, 14.3f, 5.8f, 11.9f, 6.2f, 9.4f)
                curveTo(6.7f, 6.4f, 9.2f, 4.2f, 12f, 4.2f)
                curveTo(14.8f, 4.2f, 17.3f, 6.4f, 17.8f, 9.4f)
                curveTo(18.2f, 11.9f, 17.2f, 14.3f, 15f, 15.5f)
                lineTo(15f, 18f)
                lineTo(9f, 18f)
                close()
                moveTo(9.5f, 20.8f)
                lineTo(14.5f, 20.8f)
                moveTo(12f, 8.5f)
                lineTo(12f, 13f)
            }
        }
    }

    val DthTv: ImageVector by lazy {
        buildIcon("DthTv") {
            strokePath {
                roundedRect(3.2f, 6.5f, 20.8f, 18.2f, 2.2f)
                moveTo(8.5f, 3.2f)
                lineTo(12f, 6.5f)
                lineTo(15.5f, 3.2f)
                moveTo(8f, 20.8f)
                lineTo(16f, 20.8f)
            }
        }
    }

    val Fastag: ImageVector by lazy {
        buildIcon("Fastag") {
            strokePath {
                moveTo(5f, 12.5f)
                lineTo(6.8f, 7.2f)
                curveTo(7.1f, 6.3f, 8f, 5.8f, 9f, 5.8f)
                lineTo(15f, 5.8f)
                curveTo(16f, 5.8f, 16.9f, 6.3f, 17.2f, 7.2f)
                lineTo(19f, 12.5f)
                roundedRect(3.8f, 12.5f, 20.2f, 17.8f, 1.5f)
                moveTo(6.5f, 17.8f)
                lineTo(6.5f, 20f)
                moveTo(17.5f, 17.8f)
                lineTo(17.5f, 20f)
                circle(7.2f, 15.1f, 0.9f)
                circle(16.8f, 15.1f, 0.9f)
            }
        }
    }

    val CreditCard: ImageVector by lazy {
        buildIcon("CreditCard") {
            strokePath {
                roundedRect(2.8f, 5.2f, 21.2f, 18.8f, 2.4f)
                moveTo(2.8f, 9.8f)
                lineTo(21.2f, 9.8f)
                moveTo(6.5f, 14.8f)
                lineTo(10.5f, 14.8f)
            }
        }
    }

    val Broadband: ImageVector by lazy {
        buildIcon("Broadband") {
            strokePath {
                circle(12f, 18.5f, 1.2f)
                moveTo(8.5f, 15f)
                curveTo(10.4f, 13.2f, 13.6f, 13.2f, 15.5f, 15f)
                moveTo(5.6f, 11.8f)
                curveTo(9.1f, 8.5f, 14.9f, 8.5f, 18.4f, 11.8f)
                moveTo(3f, 8.5f)
                curveTo(8f, 4f, 16f, 4f, 21f, 8.5f)
            }
        }
    }

    val PipedGas: ImageVector by lazy {
        buildIcon("PipedGas") {
            strokePath {
                moveTo(12f, 3f)
                curveTo(12f, 3f, 18f, 9.2f, 18f, 14f)
                curveTo(18f, 17.6f, 15.3f, 20.5f, 12f, 20.5f)
                curveTo(8.7f, 20.5f, 6f, 17.6f, 6f, 14f)
                curveTo(6f, 11f, 8.5f, 7.6f, 10f, 5.8f)
                lineTo(12f, 3f)
                close()
                moveTo(12f, 13f)
                curveTo(13.4f, 14.5f, 13.8f, 16.8f, 12f, 17.8f)
                curveTo(10.2f, 16.8f, 10.6f, 14.5f, 12f, 13f)
                close()
            }
        }
    }

    val WaterDrop: ImageVector by lazy {
        buildIcon("WaterDrop") {
            strokePath {
                moveTo(12f, 3.2f)
                curveTo(12f, 3.2f, 18.2f, 10.2f, 18.2f, 14.5f)
                curveTo(18.2f, 18f, 15.4f, 20.8f, 12f, 20.8f)
                curveTo(8.6f, 20.8f, 5.8f, 18f, 5.8f, 14.5f)
                curveTo(5.8f, 10.2f, 12f, 3.2f, 12f, 3.2f)
                close()
            }
        }
    }

    val History: ImageVector by lazy {
        buildIcon("History") {
            strokePath {
                moveTo(4.2f, 8.2f)
                curveTo(5.7f, 5.2f, 8.7f, 3.5f, 12.2f, 3.5f)
                curveTo(16.9f, 3.5f, 20.5f, 7.3f, 20.5f, 12f)
                curveTo(20.5f, 16.7f, 16.7f, 20.5f, 12f, 20.5f)
                curveTo(7.8f, 20.5f, 4.4f, 17.5f, 3.7f, 13.5f)
                moveTo(3.5f, 4.5f)
                lineTo(3.8f, 8.8f)
                lineTo(8.1f, 8.4f)
                moveTo(12f, 7.5f)
                lineTo(12f, 12.3f)
                lineTo(15.2f, 14.2f)
            }
        }
    }

    val BalanceBank: ImageVector by lazy {
        buildIcon("BalanceBank") {
            strokePath {
                roundedRect(3.2f, 5f, 20.8f, 19f, 2.5f)
                moveTo(14.5f, 10.5f)
                lineTo(20.8f, 10.5f)
                lineTo(20.8f, 14.5f)
                lineTo(14.5f, 14.5f)
                curveTo(13.4f, 14.5f, 12.5f, 13.6f, 12.5f, 12.5f)
                curveTo(12.5f, 11.4f, 13.4f, 10.5f, 14.5f, 10.5f)
                close()
                circle(15.5f, 12.5f, 0.7f)
            }
        }
    }

    val SpeedometerCibil: ImageVector by lazy {
        buildIcon("SpeedometerCibil") {
            strokePath {
                moveTo(4f, 16.5f)
                curveTo(3.5f, 10.5f, 7.3f, 5.2f, 12f, 5.2f)
                curveTo(16.7f, 5.2f, 20.5f, 10.5f, 20f, 16.5f)
                moveTo(12f, 15.5f)
                lineTo(16.2f, 10.2f)
                circle(12f, 16f, 1.4f)
            }
        }
    }

    val TrophyReward: ImageVector by lazy {
        buildIcon("TrophyReward") {
            strokePath {
                moveTo(7.5f, 4f)
                lineTo(16.5f, 4f)
                lineTo(16f, 10.5f)
                curveTo(15.8f, 13f, 14.1f, 14.5f, 12f, 14.5f)
                curveTo(9.9f, 14.5f, 8.2f, 13f, 8f, 10.5f)
                close()
                moveTo(7.6f, 5.5f)
                lineTo(4.5f, 5.5f)
                curveTo(4.5f, 8.8f, 5.8f, 10.5f, 8f, 10.8f)
                moveTo(16.4f, 5.5f)
                lineTo(19.5f, 5.5f)
                curveTo(19.5f, 8.8f, 18.2f, 10.5f, 16f, 10.8f)
                moveTo(12f, 14.5f)
                lineTo(12f, 19f)
                moveTo(8f, 19.5f)
                lineTo(16f, 19.5f)
            }
        }
    }

    val OfferTag: ImageVector by lazy {
        buildIcon("OfferTag") {
            strokePath {
                moveTo(3.8f, 11.8f)
                lineTo(3.8f, 4.5f)
                curveTo(3.8f, 4.1f, 4.1f, 3.8f, 4.5f, 3.8f)
                lineTo(11.8f, 3.8f)
                lineTo(20.4f, 12.4f)
                curveTo(21f, 13f, 21f, 14f, 20.4f, 14.6f)
                lineTo(14.6f, 20.4f)
                curveTo(14f, 21f, 13f, 21f, 12.4f, 20.4f)
                close()
                circle(8f, 8f, 1.1f)
            }
        }
    }

    val GiftReferral: ImageVector by lazy {
        buildIcon("GiftReferral") {
            strokePath {
                roundedRect(3.5f, 8f, 20.5f, 11.5f, 1f)
                moveTo(5f, 11.5f)
                lineTo(5f, 20f)
                lineTo(19f, 20f)
                lineTo(19f, 11.5f)
                moveTo(12f, 8f)
                lineTo(12f, 20f)
                moveTo(12f, 8f)
                curveTo(10.2f, 8f, 7.5f, 6.8f, 7.5f, 5f)
                curveTo(7.5f, 3.7f, 9f, 3.2f, 10.3f, 4.2f)
                curveTo(11.3f, 5f, 12f, 6.8f, 12f, 8f)
                close()
                moveTo(12f, 8f)
                curveTo(13.8f, 8f, 16.5f, 6.8f, 16.5f, 5f)
                curveTo(16.5f, 3.7f, 15f, 3.2f, 13.7f, 4.2f)
                curveTo(12.7f, 5f, 12f, 6.8f, 12f, 8f)
                close()
            }
        }
    }

    val UpiLiteBolt: ImageVector by lazy {
        buildIcon("UpiLiteBolt") {
            strokePath {
                moveTo(13.2f, 3f)
                lineTo(5.8f, 13.2f)
                lineTo(11.6f, 13.2f)
                lineTo(10.6f, 21f)
                lineTo(18.2f, 10.8f)
                lineTo(12.4f, 10.8f)
                close()
            }
        }
    }

    val QrCode: ImageVector by lazy {
        buildIcon("QrCode") {
            strokePath {
                roundedRect(3.5f, 3.5f, 9.5f, 9.5f, 1f)
                roundedRect(14.5f, 3.5f, 20.5f, 9.5f, 1f)
                roundedRect(3.5f, 14.5f, 9.5f, 20.5f, 1f)
                moveTo(14.5f, 14.5f)
                lineTo(17f, 14.5f)
                lineTo(17f, 17f)
                moveTo(20.5f, 14.5f)
                lineTo(20.5f, 20.5f)
                lineTo(14.5f, 20.5f)
                moveTo(6.5f, 6.5f)
                lineTo(6.5f, 6.6f)
                moveTo(17.5f, 6.5f)
                lineTo(17.5f, 6.6f)
                moveTo(6.5f, 17.5f)
                lineTo(6.5f, 17.6f)
            }
        }
    }

    // ---- Internal Vector Helpers ----

    private inline fun buildIcon(
        name: String,
        block: ImageVector.Builder.() -> Unit
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply(block).build()

    private inline fun ImageVector.Builder.strokePath(
        lineWidth: Float = 1.85f,
        crossinline block: PathBuilder.() -> Unit
    ) {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = lineWidth,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            block()
        }
    }

    private inline fun ImageVector.Builder.fillPath(
        crossinline block: PathBuilder.() -> Unit
    ) {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.EvenOdd
        ) {
            block()
        }
    }

    private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
        val c = 0.55228475f * r
        moveTo(cx, cy - r)
        curveTo(cx + c, cy - r, cx + r, cy - c, cx + r, cy)
        curveTo(cx + r, cy + c, cx + c, cy + r, cx, cy + r)
        curveTo(cx - c, cy + r, cx - r, cy + c, cx - r, cy)
        curveTo(cx - r, cy - c, cx - c, cy - r, cx, cy - r)
        close()
    }

    private fun PathBuilder.roundedRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        r: Float
    ) {
        val cr = r.coerceAtMost((right - left) / 2f).coerceAtMost((bottom - top) / 2f)
        val c = 0.55228475f * cr
        moveTo(left + cr, top)
        lineTo(right - cr, top)
        curveTo(right - cr + c, top, right, top + cr - c, right, top + cr)
        lineTo(right, bottom - cr)
        curveTo(right, bottom - cr + c, right - cr + c, bottom, right - cr, bottom)
        lineTo(left + cr, bottom)
        curveTo(left + cr - c, bottom, left, bottom - cr + c, left, bottom - cr)
        lineTo(left, top + cr)
        curveTo(left, top + cr - c, left + cr - c, top, left + cr, top)
        close()
    }
}
