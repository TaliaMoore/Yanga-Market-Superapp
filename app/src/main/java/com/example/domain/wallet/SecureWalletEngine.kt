package com.example.domain.wallet

import java.security.MessageDigest
import java.util.UUID

/**
 * Types of operations supported by the Wallet Engine
 */
enum class WalletOpType {
    FUND,     // Adding money
    PAYMENT,  // Spend/charge
    REFUND    // Reclaiming money
}

/**
 * Business Model for a modular, OOP representation of a secure wallet transaction
 */
data class SecureWalletTransaction(
    val id: String,
    val customerId: String,
    val type: WalletOpType,
    val amount: Double,
    val description: String,
    val timestamp: Long,
    val signature: String // Cryptographic validation sign
)

/**
 * Audit result provided by the security engine verifying ledger health
 */
data class WalletAuditReport(
    val isSystemAuthentic: Boolean,
    val totalTransactionsCount: Int,
    val computedBalance: Double,
    val anomalyCount: Int,
    val corruptedTxnIds: List<String>,
    val auditTimestamp: Long = System.currentTimeMillis()
)

/**
 * Core Architect interface defining the secure digital asset ledger interactions.
 */
interface WalletEngine {
    fun calculateBalance(transactions: List<SecureWalletTransaction>, starterBonus: Double = 10000.0): Double
    
    @Throws(IllegalArgumentException::class)
    fun validateDeposit(amount: Double)
    
    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    fun validatePayment(amount: Double, currentBalance: Double)
    
    fun generateSecureSignature(
        type: String,
        amount: Double,
        timestamp: Long,
        salt: String = "YANGAWALLETLOCKEDSECURITYKEY"
    ): String
    
    fun auditLedgerIntegrity(
        transactions: List<SecureWalletTransaction>,
        starterBonus: Double = 10000.0,
        salt: String = "YANGAWALLETLOCKEDSECURITYKEY"
    ): WalletAuditReport
}

/**
 * Thread-safe, secure implementation of the WalletEngine following clean architecture principles.
 */
class SecureWalletEngineImpl : WalletEngine {

    companion object {
        const val MINIMUM_DEPOSIT = 100.0 // Minimum ₦100 deposit
        const val MAXIMUM_DEPOSIT = 1000000.0 // Max ₦1,000,000 per transaction
        const val MAXIMUM_BALANCE = 5000000.0 // Max wallet hold ₦5M
    }

    override fun calculateBalance(transactions: List<SecureWalletTransaction>, starterBonus: Double): Double {
        var runningBalance = starterBonus
        for (tx in transactions) {
            when (tx.type) {
                WalletOpType.FUND -> runningBalance += tx.amount
                WalletOpType.PAYMENT -> runningBalance -= tx.amount
                WalletOpType.REFUND -> runningBalance += tx.amount
            }
        }
        return runningBalance
    }

    override fun validateDeposit(amount: Double) {
        require(amount >= MINIMUM_DEPOSIT) {
            "Deposit amount of ₦${String.format("%,.2f", amount)} is below the minimum allowed deposit of ₦${String.format("%,.2f", MINIMUM_DEPOSIT)}."
        }
        require(amount <= MAXIMUM_DEPOSIT) {
            "Deposit amount of ₦${String.format("%,.2f", amount)} exceeds the single-transaction maximum of ₦${String.format("%,.2f", MAXIMUM_DEPOSIT)}."
        }
    }

    override fun validatePayment(amount: Double, currentBalance: Double) {
        require(amount > 0.0) {
            "Payment amount must be greater than zero."
        }
        if (currentBalance < amount) {
            throw IllegalStateException(
                "Insufficient Funds! Transaction requires ₦${String.format("%,.2f", amount)} but balance is only ₦${String.format("%,.2f", currentBalance)}."
            )
        }
    }

    override fun generateSecureSignature(type: String, amount: Double, timestamp: Long, salt: String): String {
        return try {
            val rawMessage = "$type-$amount-$timestamp-$salt"
            val digestEngine = MessageDigest.getInstance("SHA-256")
            val hashBytes = digestEngine.digest(rawMessage.toByteArray(Charsets.UTF_8))
            hashBytes.fold("") { str, byte -> str + "%02x".format(byte) }.take(24)
        } catch (e: Exception) {
            "SIGNATURE_GEN_SYS_ERR"
        }
    }

    override fun auditLedgerIntegrity(
        transactions: List<SecureWalletTransaction>,
        starterBonus: Double,
        salt: String
    ): WalletAuditReport {
        val corruptedIds = mutableListOf<String>()
        var runningBalance = starterBonus
        var anomalies = 0

        for (tx in transactions) {
            // 1. Verify cryptographic signature match
            val expectedSignature = generateSecureSignature(tx.type.name, tx.amount, tx.timestamp, salt)
            if (tx.signature != expectedSignature) {
                corruptedIds.add(tx.id)
                anomalies++
            }

            // 2. Formulate state changes representing double entry ledger
            when (tx.type) {
                WalletOpType.FUND -> runningBalance += tx.amount
                WalletOpType.PAYMENT -> {
                    if (runningBalance < tx.amount) {
                        // Negative balance anomaly! Illegal override.
                        anomalies++
                    }
                    runningBalance -= tx.amount
                }
                WalletOpType.REFUND -> runningBalance += tx.amount
            }
        }

        return WalletAuditReport(
            isSystemAuthentic = anomalies == 0,
            totalTransactionsCount = transactions.size,
            computedBalance = runningBalance,
            anomalyCount = anomalies,
            corruptedTxnIds = corruptedIds
        )
    }
}
