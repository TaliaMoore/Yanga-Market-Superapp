package com.example.data.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class PassportStatus {
    UNLINKED,
    VERIFYING,
    LINKED
}

data class YangaPassportProfile(
    val passportId: String,       // format: YGP-XXXX-NG (decentralized identity format)
    val name: String,
    val email: String,
    val biometricEnabled: Boolean,
    val issueDate: String,
    val signatureToken: String    // Encrypted token for secure API signing
)

data class PassportVaultItem(
    val id: String,
    val title: String,
    val value: String,            // Encrypted/masked value for secure credential backup
    val category: String,         // "Wallet Private key", "Medical Card ID", "Access Voucher"
    val timestamp: Long
)

data class AppointmentNotification(
    val notificationId: String,
    val bookingId: String,
    val title: String,            // e.g. "St. Nicholas Premium Hospital Care"
    val description: String,      // e.g. "Specialty Appointment: Cardiology"
    val appointmentTime: String,  // e.g. "tomorrow at 10:00"
    val isRead: Boolean = false,
    val severity: String = "HIGH" // "HIGH", "MEDIUM"
)

class PassportManager {
    private val _status = MutableStateFlow(PassportStatus.UNLINKED)
    val status: StateFlow<PassportStatus> = _status.asStateFlow()

    private val _profile = MutableStateFlow<YangaPassportProfile?>(null)
    val profile: StateFlow<YangaPassportProfile?> = _profile.asStateFlow()

    private val _vaultItems = MutableStateFlow<List<PassportVaultItem>>(
        listOf(
            PassportVaultItem(
                id = "v-1",
                title = "Yanga Wallet Cryptographic Seed Recovery Backup",
                value = "yanga-wallet-seed-5f21a8b9... [ENCRYPTED MD5]",
                category = "Wallet Key Backup",
                timestamp = System.currentTimeMillis()
            )
        )
    )
    val vaultItems: StateFlow<List<PassportVaultItem>> = _vaultItems.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppointmentNotification>>(emptyList())
    val notifications: StateFlow<List<AppointmentNotification>> = _notifications.asStateFlow()

    suspend fun enrollYangaPassport(name: String, email: String, enableBiometrics: Boolean) {
        _status.value = PassportStatus.VERIFYING
        delay(2000) // Simulate secure biometric generation & public key enrollment with Yanga Network servers

        val randomIdSuffix = (1000..9999).random()
        val generatedPassportNo = "YGP-$randomIdSuffix-NG"
        val secureToken = UUID.randomUUID().toString().take(16).uppercase()

        _profile.value = YangaPassportProfile(
            passportId = generatedPassportNo,
            name = name,
            email = email,
            biometricEnabled = enableBiometrics,
            issueDate = "2026-06-19",
            signatureToken = secureToken
        )
        _status.value = PassportStatus.LINKED

        // Seed initial notifications based on new registration
        _notifications.value = listOf(
            AppointmentNotification(
                notificationId = "notif-wel",
                bookingId = "system",
                title = "Passport Setup Completed successfully! 🔑🎫",
                description = "Your biometric digital signature is now linked. You can backup database files, key pairs, and receive health alerts.",
                appointmentTime = "Just Now",
                severity = "HIGH"
            )
        )
    }

    fun unlinkPassport() {
        _status.value = PassportStatus.UNLINKED
        _profile.value = null
        _notifications.value = emptyList()
    }

    fun addVaultItem(title: String, secretValue: String, category: String) {
        val newItem = PassportVaultItem(
            id = "v-${System.currentTimeMillis()}",
            title = title,
            value = secretValue,
            category = category,
            timestamp = System.currentTimeMillis()
        )
        _vaultItems.value = _vaultItems.value + newItem
    }

    fun deleteVaultItem(id: String) {
        _vaultItems.value = _vaultItems.value.filter { it.id != id }
    }

    fun addAppointmentNotification(bookingId: String, title: String, description: String, time: String) {
        val newNotif = AppointmentNotification(
            notificationId = "notif-${System.currentTimeMillis()}",
            bookingId = bookingId,
            title = "Upcoming: $title",
            description = description,
            appointmentTime = time,
            severity = "HIGH"
        )
        _notifications.value = listOf(newNotif) + _notifications.value
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.notificationId == id) it.copy(isRead = true) else it
        }
    }

    fun dismissNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.notificationId != id }
    }
}
