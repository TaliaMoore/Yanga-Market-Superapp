package com.example.data.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class OAuthStatus {
    UNAUTHORIZED,
    AUTHORIZING,
    AUTHORIZED
}

data class GoogleUserProfile(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String?,
    val scopes: List<String>
)

data class GoogleContact(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val profileColor: Long = 0xFF7E22CE // Purple theme default
)

class OAuthManager {
    private val _status = MutableStateFlow(OAuthStatus.UNAUTHORIZED)
    val status: StateFlow<OAuthStatus> = _status.asStateFlow()

    private val _userProfile = MutableStateFlow<GoogleUserProfile?>(null)
    val userProfile: StateFlow<GoogleUserProfile?> = _userProfile.asStateFlow()

    private val _synchronizedContacts = MutableStateFlow<List<GoogleContact>>(emptyList())
    val synchronizedContacts: StateFlow<List<GoogleContact>> = _synchronizedContacts.asStateFlow()

    private val defaultContacts = listOf(
        GoogleContact("c1", "Tunde Lekki", "tunde@lekki.com", "+234 803 111 2222", 0xFF6B21A8),
        GoogleContact("c2", "Chioma Chowdeck", "chioma@chowdeck.com", "+234 812 333 4444", 0xFF0F766E),
        GoogleContact("c3", "Musa Yanga Dispatch", "musa@yanga.live", "+234 905 555 6666", 0xFFB45309),
        GoogleContact("c4", "Funmi Lagos Groceries", "funmi@yanga.live", "+234 809 777 8888", 0xFFBE123C)
    )

    suspend fun startAuthorizationFlow(userEmail: String, userName: String) {
        _status.value = OAuthStatus.AUTHORIZING
        delay(1500) // Simulate OAuth 2.0 network redirect & secure handshake
        
        _userProfile.value = GoogleUserProfile(
            id = "G-10928374...",
            name = userName,
            email = userEmail,
            avatarUrl = null,
            scopes = listOf(
                "openid",
                "https://www.googleapis.com/auth/userinfo.profile",
                "https://www.googleapis.com/auth/userinfo.email",
                "https://www.googleapis.com/auth/contacts.readonly"
            )
        )
        
        // Sync contacts
        _synchronizedContacts.value = defaultContacts
        _status.value = OAuthStatus.AUTHORIZED
    }

    fun revokeAuthorization() {
        _status.value = OAuthStatus.UNAUTHORIZED
        _userProfile.value = null
        _synchronizedContacts.value = emptyList()
    }
}
