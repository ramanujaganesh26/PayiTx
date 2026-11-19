package com.example.payit.model

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.payit.ui.designsystem.formatPhoneNumberDisplay
import com.example.payit.ui.theme.ThemeMode
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

enum class SearchFilter(val label: String) {
    ALL("All"),
    PEOPLE("People"),
    BUSINESSES("Businesses"),
    BILLS("Bills")
}

enum class EntryMode {
    PHONE,
    UPI_ID
}

enum class RewardsTab(val label: String) {
    SCRATCH_CARDS("Scratch cards"),
    OFFERS("Offers")
}

enum class BillCategory(val label: String) {
    MOBILE_RECHARGE("Mobile\nrecharge"),
    ELECTRICITY("Electricity"),
    DTH("DTH /\nCable TV"),
    FASTAG("FASTag\nrecharge"),
    CREDIT_CARD("Credit card\nbill"),
    BROADBAND("Broadband\n/ Landline"),
    PIPED_GAS("Piped gas"),
    WATER("Water")
}

sealed interface PayitDestination {
    data object Home : PayitDestination
    data class SearchOverlay(val initialFilter: SearchFilter = SearchFilter.ALL) : PayitDestination
    data object QrScanner : PayitDestination
    data class PersonChat(val contactId: String) : PayitDestination
    data class SendMoney(
        val contactId: String,
        val initialAmount: String = "",
        val initialNote: String = ""
    ) : PayitDestination
    data class PhoneOrUpiEntry(val mode: EntryMode) : PayitDestination
    data object BankTransfer : PayitDestination
    data object SelfTransfer : PayitDestination
    data class BillsAndRecharges(val initialCategory: BillCategory? = null) : PayitDestination
    data object MobileRecharge : PayitDestination
    data class Rewards(val initialTab: RewardsTab = RewardsTab.SCRATCH_CARDS) : PayitDestination
    data object TransactionHistory : PayitDestination
    data object CheckBalance : PayitDestination
    data object CardsAndBanks : PayitDestination
    data object CibilScore : PayitDestination
    data object Profile : PayitDestination
}

data class BankAccount(
    val id: String,
    val bankName: String,
    val maskedNumber: String,
    val accountType: String,
    val ifsc: String,
    val balance: Double,
    val isPrimary: Boolean,
    val upiPin: String,
    val accentColorHex: Long,
    val isCreditCard: Boolean = false
)

data class ChatEvent(
    val id: String,
    val amount: Double?,
    val note: String,
    val timestamp: String,
    val isOutgoing: Boolean,
    val isPayment: Boolean,
    val upiRefId: String = "",
    val bankName: String = "",
    val isRequest: Boolean = false
)

data class ContactPerson(
    val id: String,
    val name: String,
    val bankingName: String,
    val phone: String,
    val upiId: String,
    val initials: String,
    val avatarColorIndex: Int,
    val isBusiness: Boolean = false,
    val category: String = "",
    val cashbackTag: String? = null,
    val messages: List<ChatEvent> = emptyList()
)

data class BillItem(
    val id: String,
    val billerName: String,
    val category: String,
    val consumerNumber: String,
    val amount: Double,
    val dueDateText: String,
    val isOverdue: Boolean,
    val iconType: BillCategory,
    val colorIndex: Int,
    val isPaid: Boolean = false
)

data class TransactionItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val upiId: String,
    val amount: Double,
    val timestamp: String,
    val dateGroup: String,
    val isCredit: Boolean,
    val status: String,
    val category: String,
    val upiRefId: String,
    val bankUsed: String,
    val avatarColorIndex: Int,
    val initials: String
)

data class RewardCard(
    val id: String,
    val title: String,
    val subtitle: String,
    val cashbackAmount: Int,
    val isScratched: Boolean,
    val expiryText: String,
    val accentColorIndex: Int
)

data class MerchantOffer(
    val id: String,
    val merchantName: String,
    val title: String,
    val code: String,
    val description: String,
    val badgeText: String,
    val colorIndex: Int
)

data class RechargePlan(
    val id: String,
    val price: Int,
    val dataText: String,
    val validityText: String,
    val description: String,
    val tag: String?,
    val category: String
)

@Stable
class PayitAppState(context: Context? = null) {

    private val prefs = context?.applicationContext?.getSharedPreferences(
        "payit_fullstack_prefs",
        Context.MODE_PRIVATE
    )

    var isAuthenticated by mutableStateOf(false)
        private set

    var userName by mutableStateOf("Aarav Sharma")
        private set

    var userHandle by mutableStateOf("aarav.sharma@okpayit")
        private set

    var userPhone by mutableStateOf("+91 98204 51720")
        private set

    val userInitials: String
        get() {
            val parts = userName.trim().split(" ").filter { it.isNotBlank() }
            return when {
                parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                parts.isNotEmpty() -> parts[0].take(2).uppercase()
                else -> "AS"
            }
        }

    private var _themeMode by mutableStateOf(ThemeMode.SYSTEM)
    var themeMode: ThemeMode
        get() = _themeMode
        set(value) {
            _themeMode = value
            saveState()
        }

    private var _biometricLockEnabled by mutableStateOf(true)
    var biometricLockEnabled: Boolean
        get() = _biometricLockEnabled
        set(value) {
            _biometricLockEnabled = value
            saveState()
        }

    var upiLiteBalance by mutableDoubleStateOf(1450.0)
        private set

    val upiLiteMaxLimit: Double = 2000.0

    var cibilScore by mutableIntStateOf(784)
        private set

    var totalCashbackEarned by mutableIntStateOf(1284)
        private set

    val bankAccounts = mutableStateListOf<BankAccount>()
    val contacts = mutableStateListOf<ContactPerson>()
    val bills = mutableStateListOf<BillItem>()
    val transactions = mutableStateListOf<TransactionItem>()
    val rewards = mutableStateListOf<RewardCard>()

    val offers = listOf(
        MerchantOffer(
            id = "off_1",
            merchantName = "Zomato",
            title = "Flat ₹50 cashback on orders above ₹249",
            code = "GPAYZOM50",
            description = "Pay using Payit UPI or RuPay Credit Card on UPI twice this month.",
            badgeText = "₹50 back",
            colorIndex = 9
        ),
        MerchantOffer(
            id = "off_2",
            merchantName = "Starbucks",
            title = "5% instant cashback on UPI Lite payments",
            code = "LITECOFFEE",
            description = "Scan any Starbucks store QR and pay securely with UPI Lite.",
            badgeText = "5% back",
            colorIndex = 8
        ),
        MerchantOffer(
            id = "off_3",
            merchantName = "Swiggy Instamart",
            title = "Up to ₹100 cashback on grocery orders",
            code = "INSTAPAY",
            description = "Minimum cart value ₹499. Valid once per week.",
            badgeText = "10% back",
            colorIndex = 10
        ),
        MerchantOffer(
            id = "off_4",
            merchantName = "BESCOM & Bills",
            title = "Assured scratch card up to ₹150 on utility bills",
            code = "BILLPAY150",
            description = "Pay electricity, gas, or broadband bill above ₹500.",
            badgeText = "Up to ₹150",
            colorIndex = 15
        )
    )

    val rechargePlans = listOf(
        RechargePlan("p1", 299, "1.5 GB/day", "28 Days", "Unlimited calls + 100 SMS/day + 5G Welcome", "Popular", "Popular"),
        RechargePlan("p2", 349, "2 GB/day", "28 Days", "Unlimited True 5G Data + Calls + Prime Trial", "Best Seller", "Popular"),
        RechargePlan("p3", 666, "1.5 GB/day", "77 Days", "Unlimited calls + 100 SMS/day + Free Roaming", "Value", "Popular"),
        RechargePlan("p4", 859, "2 GB/day", "84 Days", "Unlimited 5G + JioCinema / Wynk benefits", "Top Pick", "Popular"),
        RechargePlan("p5", 19, "1 GB", "1 Day", "High-speed data booster add-on", null, "Data Add-on"),
        RechargePlan("p6", 49, "Unlimited", "1 Day", "Unlimited 4G/5G Cricket Pack (FUP 20GB)", "Cricket", "Data Add-on"),
        RechargePlan("p7", 2999, "2.5 GB/day", "365 Days", "Annual Super Saver + Unlimited 5G + OTT", "Save 24%", "Annual")
    )

    val primaryAccount: BankAccount
        get() = bankAccounts.firstOrNull { it.isPrimary } ?: bankAccounts.first()

    val unscratchedRewardsCount: Int
        get() = rewards.count { !it.isScratched }

    init {
        seedDefaultData()
        restoreState()
    }

    fun completeAuthentication(
        name: String,
        dialCode: String,
        phoneDigits: String,
        isSignUp: Boolean
    ) {
        val cleanPhone = formatPhoneNumberDisplay(phoneDigits)
        userPhone = "$dialCode $cleanPhone"
        if (isSignUp && name.trim().isNotBlank()) {
            val trimmed = name.trim()
            userName = trimmed
            val slug = trimmed.lowercase().replace(Regex("[^a-z0-9]+"), ".").trim('.').ifBlank { "user" }
            userHandle = "$slug@okpayit"
        }
        isAuthenticated = true
        saveState()
    }

    fun signOut() {
        isAuthenticated = false
        saveState()
    }

    fun resetDemoData() {
        prefs?.edit()?.clear()?.apply()
        val keepAuth = isAuthenticated
        val keepTheme = _themeMode
        seedDefaultData()
        isAuthenticated = keepAuth
        _themeMode = keepTheme
        userName = "Aarav Sharma"
        userHandle = "aarav.sharma@okpayit"
        userPhone = "+91 98204 51720"
        saveState()
    }

    fun getContactById(id: String): ContactPerson? = contacts.firstOrNull { it.id == id }

    fun setPrimaryAccount(accountId: String) {
        for (i in bankAccounts.indices) {
            val acc = bankAccounts[i]
            bankAccounts[i] = acc.copy(isPrimary = acc.id == accountId)
        }
        saveState()
    }

    fun addBankAccount(
        bankName: String,
        last4Digits: String,
        ifsc: String,
        isCreditCard: Boolean
    ): BankAccount {
        val cleanDigits = last4Digits.filter { it.isDigit() }.takeLast(4).padStart(4, '8')
        val created = BankAccount(
            id = "bank_${System.currentTimeMillis()}",
            bankName = bankName.trim(),
            maskedNumber = "•••• $cleanDigits",
            accountType = if (isCreditCard) "RuPay Credit on UPI" else "Savings Account",
            ifsc = ifsc.trim().uppercase().ifBlank { "PYIT0000101" },
            balance = if (isCreditCard) 60000.0 else 25000.0,
            isPrimary = false,
            upiPin = "1234",
            accentColorHex = if (isCreditCard) 0xFF7C3AED else 0xFF0F766E,
            isCreditCard = isCreditCard
        )
        bankAccounts.add(created)
        saveState()
        return created
    }

    fun findOrCreateContact(name: String, upiId: String, phone: String = ""): ContactPerson {
        val existing = contacts.firstOrNull {
            it.upiId.equals(upiId, ignoreCase = true) ||
                (phone.isNotBlank() && it.phone.replace(" ", "") == phone.replace(" ", ""))
        }
        if (existing != null) return existing

        val cleanName = name.ifBlank {
            upiId.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
        }
        val parts = cleanName.trim().split(" ").filter { it.isNotBlank() }
        val initials = when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            parts.isNotEmpty() -> parts[0].take(2).uppercase()
            else -> "UP"
        }
        val created = ContactPerson(
            id = "c_${System.currentTimeMillis()}",
            name = cleanName,
            bankingName = cleanName.uppercase(),
            phone = phone.ifBlank { "UPI Contact" },
            upiId = upiId,
            initials = initials,
            avatarColorIndex = abs(cleanName.hashCode()) % 8
        )
        contacts.add(0, created)
        saveState()
        return created
    }

    fun recordPayment(
        recipient: ContactPerson,
        amount: Double,
        note: String,
        account: BankAccount,
        useUpiLite: Boolean = false
    ): TransactionItem {
        val refId = generateUpiRefId()
        val paymentSourceLabel = if (useUpiLite) {
            upiLiteBalance = (upiLiteBalance - amount).coerceAtLeast(0.0)
            "UPI Lite"
        } else {
            deductAccountBalance(account.id, amount)
            "${account.bankName} ${account.maskedNumber}"
        }

        val cleanNote = note.trim().ifBlank { "Paid via Payit" }
        val newEvent = ChatEvent(
            id = "ev_${System.currentTimeMillis()}",
            amount = amount,
            note = cleanNote,
            timestamp = "Just now",
            isOutgoing = true,
            isPayment = true,
            upiRefId = refId,
            bankName = paymentSourceLabel
        )
        appendChatEvent(recipient.id, newEvent)

        val tx = TransactionItem(
            id = "tx_${System.currentTimeMillis()}",
            title = recipient.name,
            subtitle = cleanNote,
            upiId = recipient.upiId,
            amount = amount,
            timestamp = "Just now",
            dateGroup = "Today",
            isCredit = false,
            status = "Completed",
            category = if (useUpiLite) "UPI Lite" else "Sent",
            upiRefId = refId,
            bankUsed = paymentSourceLabel,
            avatarColorIndex = recipient.avatarColorIndex,
            initials = recipient.initials
        )
        transactions.add(0, tx)

        if (amount >= 100.0) {
            val bonusCashback = ((amount * 0.03).toInt()).coerceIn(12, 180)
            rewards.add(
                0,
                RewardCard(
                    id = "rew_${System.currentTimeMillis()}",
                    title = "Payit Instant Reward",
                    subtitle = "Unlocked for paying ${recipient.name}",
                    cashbackAmount = bonusCashback,
                    isScratched = false,
                    expiryText = "Expires in 14 days",
                    accentColorIndex = recipient.avatarColorIndex
                )
            )
        }
        saveState()
        return tx
    }

    fun recordRequest(recipient: ContactPerson, amount: Double, note: String) {
        val cleanNote = note.trim().ifBlank { "Payment request" }
        val event = ChatEvent(
            id = "req_${System.currentTimeMillis()}",
            amount = amount,
            note = cleanNote,
            timestamp = "Just now",
            isOutgoing = true,
            isPayment = false,
            isRequest = true
        )
        appendChatEvent(recipient.id, event)
        saveState()
    }

    fun sendChatMessage(recipient: ContactPerson, text: String) {
        if (text.isBlank()) return
        val event = ChatEvent(
            id = "msg_${System.currentTimeMillis()}",
            amount = null,
            note = text.trim(),
            timestamp = "Just now",
            isOutgoing = true,
            isPayment = false
        )
        appendChatEvent(recipient.id, event)
        saveState()
    }

    fun recordSelfTransfer(
        fromAccount: BankAccount,
        toAccount: BankAccount,
        amount: Double
    ): TransactionItem {
        val refId = generateUpiRefId()
        deductAccountBalance(fromAccount.id, amount)
        creditAccountBalance(toAccount.id, amount)

        val tx = TransactionItem(
            id = "tx_self_${System.currentTimeMillis()}",
            title = "Self transfer • ${toAccount.bankName}",
            subtitle = "From ${fromAccount.bankName} ${fromAccount.maskedNumber}",
            upiId = userHandle,
            amount = amount,
            timestamp = "Just now",
            dateGroup = "Today",
            isCredit = false,
            status = "Completed",
            category = "Sent",
            upiRefId = refId,
            bankUsed = "${fromAccount.bankName} ${fromAccount.maskedNumber}",
            avatarColorIndex = 0,
            initials = "ST"
        )
        transactions.add(0, tx)
        saveState()
        return tx
    }

    fun recordBankTransfer(
        beneficiaryName: String,
        accountNumber: String,
        ifsc: String,
        amount: Double,
        note: String,
        fromAccount: BankAccount
    ): TransactionItem {
        val refId = generateUpiRefId()
        deductAccountBalance(fromAccount.id, amount)
        val maskedTarget = if (accountNumber.length >= 4) "•••• ${accountNumber.takeLast(4)}" else accountNumber
        val parts = beneficiaryName.trim().split(" ").filter { it.isNotBlank() }
        val initials = if (parts.size >= 2) {
            "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        } else {
            beneficiaryName.take(2).uppercase().ifBlank { "BT" }
        }
        val tx = TransactionItem(
            id = "tx_bank_${System.currentTimeMillis()}",
            title = beneficiaryName,
            subtitle = "${note.ifBlank { "Bank transfer" }} ($maskedTarget • ${ifsc.uppercase()})",
            upiId = "$accountNumber@${ifsc.lowercase()}",
            amount = amount,
            timestamp = "Just now",
            dateGroup = "Today",
            isCredit = false,
            status = "Completed",
            category = "Sent",
            upiRefId = refId,
            bankUsed = "${fromAccount.bankName} ${fromAccount.maskedNumber}",
            avatarColorIndex = 5,
            initials = initials
        )
        transactions.add(0, tx)
        saveState()
        return tx
    }

    fun payBill(bill: BillItem, account: BankAccount): TransactionItem {
        val refId = generateUpiRefId()
        deductAccountBalance(account.id, bill.amount)
        val idx = bills.indexOfFirst { it.id == bill.id }
        if (idx >= 0) {
            bills[idx] = bill.copy(isPaid = true, dueDateText = "Paid today", isOverdue = false)
        }
        val tx = TransactionItem(
            id = "tx_bill_${System.currentTimeMillis()}",
            title = bill.billerName,
            subtitle = "${bill.category} • ${bill.consumerNumber}",
            upiId = "${bill.billerName.lowercase().replace(" ", "")}@bharatbill",
            amount = bill.amount,
            timestamp = "Just now",
            dateGroup = "Today",
            isCredit = false,
            status = "Completed",
            category = "Bills",
            upiRefId = refId,
            bankUsed = "${account.bankName} ${account.maskedNumber}",
            avatarColorIndex = bill.colorIndex,
            initials = bill.billerName.take(2).uppercase()
        )
        transactions.add(0, tx)
        saveState()
        return tx
    }

    fun payRecharge(
        phone: String,
        operatorName: String,
        plan: RechargePlan,
        account: BankAccount
    ): TransactionItem {
        val refId = generateUpiRefId()
        val amount = plan.price.toDouble()
        deductAccountBalance(account.id, amount)
        val tx = TransactionItem(
            id = "tx_rech_${System.currentTimeMillis()}",
            title = "$operatorName Prepaid",
            subtitle = "$phone • ₹${plan.price} (${plan.dataText}, ${plan.validityText})",
            upiId = "${operatorName.lowercase().replace(" ", "")}@okbiz",
            amount = amount,
            timestamp = "Just now",
            dateGroup = "Today",
            isCredit = false,
            status = "Completed",
            category = "Bills",
            upiRefId = refId,
            bankUsed = "${account.bankName} ${account.maskedNumber}",
            avatarColorIndex = 14,
            initials = operatorName.take(2).uppercase()
        )
        transactions.add(0, tx)
        saveState()
        return tx
    }

    fun scratchCard(cardId: String): RewardCard? {
        val idx = rewards.indexOfFirst { it.id == cardId }
        if (idx < 0) return null
        val card = rewards[idx]
        if (card.isScratched) return card

        val primary = primaryAccount
        val updated = card.copy(
            isScratched = true,
            subtitle = "Credited to ${primary.bankName} ${primary.maskedNumber}",
            expiryText = "Earned just now"
        )
        rewards[idx] = updated
        totalCashbackEarned += card.cashbackAmount
        creditAccountBalance(primary.id, card.cashbackAmount.toDouble())

        transactions.add(
            0,
            TransactionItem(
                id = "tx_rew_${System.currentTimeMillis()}",
                title = "Payit Cashback Reward",
                subtitle = card.title,
                upiId = "rewards@okpayit",
                amount = card.cashbackAmount.toDouble(),
                timestamp = "Just now",
                dateGroup = "Today",
                isCredit = true,
                status = "Completed",
                category = "Received",
                upiRefId = generateUpiRefId(),
                bankUsed = "${primary.bankName} ${primary.maskedNumber}",
                avatarColorIndex = 2,
                initials = "PR"
            )
        )
        saveState()
        return updated
    }

    fun topUpUpiLite(amount: Double, fromAccount: BankAccount): TransactionItem {
        val allowedAmount = amount.coerceAtMost((upiLiteMaxLimit - upiLiteBalance).coerceAtLeast(0.0))
        val effectiveAmount = if (allowedAmount > 0) allowedAmount else amount
        deductAccountBalance(fromAccount.id, effectiveAmount)
        upiLiteBalance += effectiveAmount

        val tx = TransactionItem(
            id = "tx_lite_${System.currentTimeMillis()}",
            title = "UPI Lite Wallet Top-up",
            subtitle = "Added from ${fromAccount.bankName} ${fromAccount.maskedNumber}",
            upiId = "upilite@okpayit",
            amount = effectiveAmount,
            timestamp = "Just now",
            dateGroup = "Today",
            isCredit = false,
            status = "Completed",
            category = "UPI Lite",
            upiRefId = generateUpiRefId(),
            bankUsed = "${fromAccount.bankName} ${fromAccount.maskedNumber}",
            avatarColorIndex = 0,
            initials = "UL"
        )
        transactions.add(0, tx)
        saveState()
        return tx
    }

    private fun appendChatEvent(contactId: String, event: ChatEvent) {
        val idx = contacts.indexOfFirst { it.id == contactId }
        if (idx >= 0) {
            val current = contacts[idx]
            contacts[idx] = current.copy(messages = current.messages + event)
        }
    }

    private fun deductAccountBalance(accountId: String, amount: Double) {
        val idx = bankAccounts.indexOfFirst { it.id == accountId }
        if (idx >= 0) {
            val acc = bankAccounts[idx]
            bankAccounts[idx] = acc.copy(balance = (acc.balance - amount).coerceAtLeast(0.0))
        }
    }

    private fun creditAccountBalance(accountId: String, amount: Double) {
        val idx = bankAccounts.indexOfFirst { it.id == accountId }
        if (idx >= 0) {
            val acc = bankAccounts[idx]
            bankAccounts[idx] = acc.copy(balance = acc.balance + amount)
        }
    }

    private fun generateUpiRefId(): String {
        val suffix = (System.currentTimeMillis() % 100_000_000L).toString().padStart(8, '0')
        return "4322$suffix"
    }

    private fun saveState() {
        val store = prefs ?: return
        runCatching {
            val root = JSONObject().apply {
                put("isAuthenticated", isAuthenticated)
                put("userName", userName)
                put("userHandle", userHandle)
                put("userPhone", userPhone)
                put("themeMode", _themeMode.name)
                put("biometricLockEnabled", _biometricLockEnabled)
                put("upiLiteBalance", upiLiteBalance)
                put("totalCashbackEarned", totalCashbackEarned)

                put("bankAccounts", JSONArray().apply {
                    bankAccounts.forEach { acc ->
                        put(JSONObject().apply {
                            put("id", acc.id)
                            put("bankName", acc.bankName)
                            put("maskedNumber", acc.maskedNumber)
                            put("accountType", acc.accountType)
                            put("ifsc", acc.ifsc)
                            put("balance", acc.balance)
                            put("isPrimary", acc.isPrimary)
                            put("upiPin", acc.upiPin)
                            put("accentColorHex", acc.accentColorHex)
                            put("isCreditCard", acc.isCreditCard)
                        })
                    }
                })

                put("paidBillIds", JSONArray().apply {
                    bills.filter { it.isPaid }.forEach { put(it.id) }
                })

                put("rewards", JSONArray().apply {
                    rewards.forEach { rew ->
                        put(JSONObject().apply {
                            put("id", rew.id)
                            put("title", rew.title)
                            put("subtitle", rew.subtitle)
                            put("cashbackAmount", rew.cashbackAmount)
                            put("isScratched", rew.isScratched)
                            put("expiryText", rew.expiryText)
                            put("accentColorIndex", rew.accentColorIndex)
                        })
                    }
                })

                put("transactions", JSONArray().apply {
                    transactions.forEach { tx ->
                        put(JSONObject().apply {
                            put("id", tx.id)
                            put("title", tx.title)
                            put("subtitle", tx.subtitle)
                            put("upiId", tx.upiId)
                            put("amount", tx.amount)
                            put("timestamp", tx.timestamp)
                            put("dateGroup", tx.dateGroup)
                            put("isCredit", tx.isCredit)
                            put("status", tx.status)
                            put("category", tx.category)
                            put("upiRefId", tx.upiRefId)
                            put("bankUsed", tx.bankUsed)
                            put("avatarColorIndex", tx.avatarColorIndex)
                            put("initials", tx.initials)
                        })
                    }
                })
            }
            store.edit().putString("state_json", root.toString()).apply()
        }
    }

    private fun restoreState() {
        val raw = prefs?.getString("state_json", null) ?: return
        runCatching {
            val root = JSONObject(raw)
            isAuthenticated = root.optBoolean("isAuthenticated", false)
            userName = root.optString("userName", userName)
            userHandle = root.optString("userHandle", userHandle)
            userPhone = root.optString("userPhone", userPhone)
            _themeMode = runCatching {
                ThemeMode.valueOf(root.optString("themeMode", ThemeMode.SYSTEM.name))
            }.getOrDefault(ThemeMode.SYSTEM)
            _biometricLockEnabled = root.optBoolean("biometricLockEnabled", true)
            upiLiteBalance = root.optDouble("upiLiteBalance", upiLiteBalance)
            totalCashbackEarned = root.optInt("totalCashbackEarned", totalCashbackEarned)

            root.optJSONArray("bankAccounts")?.let { arr ->
                if (arr.length() > 0) {
                    bankAccounts.clear()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        bankAccounts.add(
                            BankAccount(
                                id = obj.getString("id"),
                                bankName = obj.getString("bankName"),
                                maskedNumber = obj.getString("maskedNumber"),
                                accountType = obj.getString("accountType"),
                                ifsc = obj.getString("ifsc"),
                                balance = obj.getDouble("balance"),
                                isPrimary = obj.getBoolean("isPrimary"),
                                upiPin = obj.optString("upiPin", "1234"),
                                accentColorHex = obj.getLong("accentColorHex"),
                                isCreditCard = obj.optBoolean("isCreditCard", false)
                            )
                        )
                    }
                }
            }

            root.optJSONArray("paidBillIds")?.let { arr ->
                val paidIds = mutableSetOf<String>()
                for (i in 0 until arr.length()) paidIds.add(arr.getString(i))
                for (i in bills.indices) {
                    val b = bills[i]
                    if (b.id in paidIds) {
                        bills[i] = b.copy(isPaid = true, dueDateText = "Paid today", isOverdue = false)
                    }
                }
            }

            root.optJSONArray("rewards")?.let { arr ->
                if (arr.length() > 0) {
                    rewards.clear()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        rewards.add(
                            RewardCard(
                                id = obj.getString("id"),
                                title = obj.getString("title"),
                                subtitle = obj.getString("subtitle"),
                                cashbackAmount = obj.getInt("cashbackAmount"),
                                isScratched = obj.getBoolean("isScratched"),
                                expiryText = obj.getString("expiryText"),
                                accentColorIndex = obj.getInt("accentColorIndex")
                            )
                        )
                    }
                }
            }

            root.optJSONArray("transactions")?.let { arr ->
                if (arr.length() > 0) {
                    transactions.clear()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        transactions.add(
                            TransactionItem(
                                id = obj.getString("id"),
                                title = obj.getString("title"),
                                subtitle = obj.getString("subtitle"),
                                upiId = obj.getString("upiId"),
                                amount = obj.getDouble("amount"),
                                timestamp = obj.getString("timestamp"),
                                dateGroup = obj.getString("dateGroup"),
                                isCredit = obj.getBoolean("isCredit"),
                                status = obj.getString("status"),
                                category = obj.getString("category"),
                                upiRefId = obj.getString("upiRefId"),
                                bankUsed = obj.getString("bankUsed"),
                                avatarColorIndex = obj.getInt("avatarColorIndex"),
                                initials = obj.getString("initials")
                            )
                        )
                    }
                }
            }
        }
    }

    private fun seedDefaultData() {
        upiLiteBalance = 1450.0
        cibilScore = 784
        totalCashbackEarned = 1284

        bankAccounts.clear()
        bankAccounts.addAll(
            listOf(
                BankAccount(
                    id = "bank_hdfc",
                    bankName = "HDFC Bank",
                    maskedNumber = "•••• 4821",
                    accountType = "Savings Account",
                    ifsc = "HDFC0000240",
                    balance = 142850.00,
                    isPrimary = true,
                    upiPin = "1234",
                    accentColorHex = 0xFF004C8F
                ),
                BankAccount(
                    id = "bank_icici_cc",
                    bankName = "ICICI RuPay Credit",
                    maskedNumber = "•••• 9012",
                    accountType = "RuPay Credit on UPI",
                    ifsc = "ICIC0000018",
                    balance = 85000.00,
                    isPrimary = false,
                    upiPin = "1234",
                    accentColorHex = 0xFFB45309,
                    isCreditCard = true
                ),
                BankAccount(
                    id = "bank_sbi",
                    bankName = "State Bank of India",
                    maskedNumber = "•••• 3319",
                    accountType = "Savings Account",
                    ifsc = "SBIN0004120",
                    balance = 38420.50,
                    isPrimary = false,
                    upiPin = "1234",
                    accentColorHex = 0xFF0B57D0
                )
            )
        )

        contacts.clear()
        contacts.addAll(
            listOf(
                ContactPerson(
                    id = "c_priya",
                    name = "Priya Nair",
                    bankingName = "PRIYA K NAIR",
                    phone = "+91 98765 43210",
                    upiId = "priya.nair@okhdfcbank",
                    initials = "PN",
                    avatarColorIndex = 0,
                    messages = listOf(
                        ChatEvent("m1", 1250.0, "Dinner at Burma Burma", "Yesterday, 9:18 PM", isOutgoing = true, isPayment = true, upiRefId = "432198471201", bankName = "HDFC Bank •••• 4821"),
                        ChatEvent("m2", null, "Paid my half, let me know when you book cabs!", "Yesterday, 9:20 PM", isOutgoing = false, isPayment = false),
                        ChatEvent("m3", 625.0, "Cab split to Indiranagar", "Today, 10:12 AM", isOutgoing = false, isPayment = true, upiRefId = "432200148892", bankName = "HDFC Bank •••• 4821")
                    )
                ),
                ContactPerson("c_rohan", "Rohan Verma", "ROHAN VERMA", "+91 98112 77834", "rohan.verma@okicici", "RV", 1, messages = listOf(
                    ChatEvent("r1", 2400.0, "Weekend badminton court booking", "Nov 12, 6:40 PM", isOutgoing = true, isPayment = true, upiRefId = "431877120091", bankName = "HDFC Bank •••• 4821")
                )),
                ContactPerson("c_ananya", "Ananya Iyer", "ANANYA S IYER", "+91 99451 20811", "ananya.iyer@oksbi", "AI", 2, messages = listOf(
                    ChatEvent("a1", 850.0, "Coffee & croissants", "Nov 11, 11:05 AM", isOutgoing = false, isPayment = true, upiRefId = "431711245601", bankName = "State Bank of India •••• 3319")
                )),
                ContactPerson("c_vikram", "Vikram Mehta", "VIKRAM RAJ MEHTA", "+91 97690 11223", "vikram.m@okaxis", "VM", 3, messages = listOf(
                    ChatEvent("v1", 4500.0, "Concert tickets x2", "Nov 09, 4:20 PM", isOutgoing = true, isPayment = true, upiRefId = "431502194420", bankName = "HDFC Bank •••• 4821")
                )),
                ContactPerson("c_kabir", "Kabir Malhotra", "KABIR MALHOTRA", "+91 98201 33490", "kabir.m@okhdfcbank", "KM", 4),
                ContactPerson("c_sneha", "Sneha Kulkarni", "SNEHA P KULKARNI", "+91 98230 98211", "sneha.k@oksbi", "SK", 5, messages = listOf(
                    ChatEvent("s1", 320.0, "Metro card top-up", "Nov 08, 8:50 AM", isOutgoing = true, isPayment = true, upiRefId = "431400219844", bankName = "UPI Lite")
                )),
                ContactPerson("c_arjun", "Arjun Kapoor", "ARJUN KAPUR", "+91 98104 55612", "arjun.kapoor@okicici", "AK", 6),
                ContactPerson("c_meera", "Meera Reddy", "MEERA DEVI REDDY", "+91 98490 71205", "meera.reddy@okhdfcbank", "MR", 7),
                ContactPerson("c_devansh", "Devansh Joshi", "DEVANSH JOSHI", "+91 99208 44123", "devansh.j@okaxis", "DJ", 0),
                ContactPerson("c_nisha", "Nisha Gupta", "NISHA GUPTA", "+91 98991 60542", "nisha.gupta@okicici", "NG", 2),
                ContactPerson("c_aditya", "Aditya Rao", "ADITYA V RAO", "+91 98802 19304", "aditya.rao@oksbi", "AR", 3),
                ContactPerson("c_tara", "Tara Sen", "TARA SEN", "+91 98301 77412", "tara.sen@okhdfcbank", "TS", 4),

                ContactPerson("b_starbucks", "Starbucks", "TATA STARBUCKS PVT LTD", "Merchant", "starbucks.india@okbizaxis", "SB", 8, isBusiness = true, category = "Food & Coffee", cashbackTag = "5% back", messages = listOf(
                    ChatEvent("sb1", 340.0, "Iced Caramel Macchiato", "Today, 9:05 AM", isOutgoing = true, isPayment = true, upiRefId = "432200102938", bankName = "UPI Lite")
                )),
                ContactPerson("b_zomato", "Zomato", "ZOMATO LIMITED", "Merchant", "zomato.pay@okbizicici", "ZM", 9, isBusiness = true, category = "Dining & Delivery", cashbackTag = "₹50 off", messages = listOf(
                    ChatEvent("zm1", 648.0, "Order #49102 • Truffle Bowl", "Nov 13, 8:42 PM", isOutgoing = true, isPayment = true, upiRefId = "431988120312", bankName = "ICICI RuPay Credit •••• 9012")
                )),
                ContactPerson("b_swiggy", "Swiggy", "BUNDL TECHNOLOGIES PVT LTD", "Merchant", "swiggy.instamart@okbizaxis", "SW", 10, isBusiness = true, category = "Instamart & Food", cashbackTag = "10% back", messages = listOf(
                    ChatEvent("sw1", 412.0, "Swiggy Instamart Groceries", "Nov 12, 11:20 AM", isOutgoing = true, isPayment = true, upiRefId = "431871230411", bankName = "HDFC Bank •••• 4821")
                )),
                ContactPerson("b_uber", "Uber", "UBER INDIA SYSTEMS PVT LTD", "Merchant", "uber.rides@okbizhdfc", "UB", 11, isBusiness = true, category = "Mobility", cashbackTag = null, messages = listOf(
                    ChatEvent("ub1", 285.0, "Airport Express Ride", "Nov 10, 6:15 AM", isOutgoing = true, isPayment = true, upiRefId = "431654001289", bankName = "UPI Lite")
                )),
                ContactPerson("b_flipkart", "Flipkart", "FLIPKART INTERNET PVT LTD", "Merchant", "flipkart.upi@okbizaxis", "FK", 12, isBusiness = true, category = "Shopping", cashbackTag = "₹100 off"),
                ContactPerson("b_cred", "CRED", "DREAMPLUG TECHNOLOGIES", "Merchant", "cred.club@okbizicici", "CR", 13, isBusiness = true, category = "Credit Cards", cashbackTag = "₹25 back"),
                ContactPerson("b_jiomart", "JioMart", "RELIANCE RETAIL LIMITED", "Merchant", "jiomart@okbizsbi", "JM", 14, isBusiness = true, category = "Supermarket"),
                ContactPerson("b_bescom", "BESCOM", "BANGALORE ELECTRICITY SUPPLY CO", "Utility Biller", "bescom.bharatbill@okbizsbi", "BE", 15, isBusiness = true, category = "Electricity Utility")
            )
        )

        bills.clear()
        bills.addAll(
            listOf(
                BillItem("bill_bescom", "BESCOM Electricity", "Electricity", "CA-889204112", 1840.0, "Due in 3 days", false, BillCategory.ELECTRICITY, 15),
                BillItem("bill_airtel", "Airtel Postpaid", "Mobile Postpaid", "9820451720", 799.0, "Due tomorrow", true, BillCategory.MOBILE_RECHARGE, 9),
                BillItem("bill_tataplay", "Tata Play DTH", "DTH / Cable TV", "SUB-10498271", 420.0, "Due Nov 22", false, BillCategory.DTH, 14),
                BillItem("bill_act", "ACT Fibernet", "Broadband", "ACT-4412098", 1179.0, "Due Nov 25", false, BillCategory.BROADBAND, 12),
                BillItem("bill_gas", "Mahanagar Gas", "Piped gas", "MGL-3391042", 645.0, "Due Nov 28", false, BillCategory.PIPED_GAS, 10),
                BillItem("bill_fastag", "ICICI FASTag", "FASTag", "KA01MG9482", 500.0, "Low balance", false, BillCategory.FASTAG, 11)
            )
        )

        transactions.clear()
        transactions.addAll(
            listOf(
                TransactionItem("tx_1", "Priya Nair", "Cab split to Indiranagar", "priya.nair@okhdfcbank", 625.0, "Today, 10:12 AM", "Today", true, "Completed", "Received", "432200148892", "HDFC Bank •••• 4821", 0, "PN"),
                TransactionItem("tx_2", "Starbucks India", "Iced Caramel Macchiato", "starbucks.india@okbizaxis", 340.0, "Today, 9:05 AM", "Today", false, "Completed", "UPI Lite", "432200102938", "UPI Lite", 8, "SB"),
                TransactionItem("tx_3", "Priya Nair", "Dinner at Burma Burma", "priya.nair@okhdfcbank", 1250.0, "Yesterday, 9:18 PM", "Yesterday", false, "Completed", "Sent", "432198471201", "HDFC Bank •••• 4821", 0, "PN"),
                TransactionItem("tx_4", "Zomato", "Order #49102 • Truffle Bowl", "zomato.pay@okbizicici", 648.0, "Nov 13, 8:42 PM", "This Week", false, "Completed", "Sent", "431988120312", "ICICI RuPay Credit •••• 9012", 9, "ZM"),
                TransactionItem("tx_5", "Swiggy Instamart", "Groceries & fresh produce", "swiggy.instamart@okbizaxis", 412.0, "Nov 12, 11:20 AM", "This Week", false, "Completed", "Sent", "431871230411", "HDFC Bank •••• 4821", 10, "SW"),
                TransactionItem("tx_6", "Rohan Verma", "Badminton court booking", "rohan.verma@okicici", 2400.0, "Nov 12, 6:40 PM", "This Week", false, "Completed", "Sent", "431877120091", "HDFC Bank •••• 4821", 1, "RV"),
                TransactionItem("tx_7", "Ananya Iyer", "Coffee & croissants", "ananya.iyer@oksbi", 850.0, "Nov 11, 11:05 AM", "This Week", true, "Completed", "Received", "431711245601", "State Bank of India •••• 3319", 2, "AI"),
                TransactionItem("tx_8", "Apple Cloud Services", "AutoPay • iCloud+ 200GB", "apple.autopay@okbizhdfc", 219.0, "Nov 09, 7:00 AM", "Earlier this month", false, "Completed", "Bills", "431500219001", "HDFC Bank •••• 4821", 11, "AC")
            )
        )

        rewards.clear()
        rewards.addAll(
            listOf(
                RewardCard("rew_1", "Super Pay Reward", "Unlocked for paying Priya Nair", 48, false, "Expires Nov 30", 0),
                RewardCard("rew_2", "UPI Lite Flash Bonus", "Unlocked on Starbucks order", 25, false, "Expires Nov 27", 8),
                RewardCard("rew_3", "RuPay UPI Cashback", "Credited to HDFC Bank •••• 4821", 115, true, "Earned Nov 12", 2),
                RewardCard("rew_4", "Electricity Bill Bonus", "Credited to HDFC Bank •••• 4821", 42, true, "Earned Nov 05", 1),
                RewardCard("rew_5", "Weekly Streak Reward", "Credited to HDFC Bank •••• 4821", 24, true, "Earned Nov 01", 3)
            )
        )
    }
}

@Composable
fun rememberPayitAppState(): PayitAppState {
    val context = LocalContext.current
    return remember(context) { PayitAppState(context) }
}
