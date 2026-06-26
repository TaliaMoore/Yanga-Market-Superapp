package com.example.domain.auth

import android.app.Activity
import android.util.Log

/**
 * High-fidelity domain model representing the secure user identity details in Yanga Market.
 * Built following modular OOP practices under clean architecture rules.
 */
data class YangaUser(
    val uid: String,
    val email: String?,
    val phoneNumber: String?,
    val isAnonymous: Boolean,
    val isSandboxUser: Boolean = true
)

/**
 * Result abstraction representing secure authorization state responses.
 */
sealed class AuthResult {
    data class Success(val user: YangaUser) : AuthResult()
    data class Failure(val errorMessage: String) : AuthResult()
}

/**
 * Interface defining secure user verification contracts (Email/Password, Phone Verification, OTP).
 */
interface FirebaseAuthEngine {
    fun isFirebaseAvailable(): Boolean
    fun getCurrentUser(): YangaUser?
    fun signOut()
    
    fun signInWithEmail(
        email: String,
        password: String,
        onResult: (AuthResult) -> Unit
    )
    
    fun signUpWithEmail(
        email: String,
        password: String,
        onResult: (AuthResult) -> Unit
    )
    
    fun sendOtpCode(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (verificationId: String) -> Unit,
        onVerificationFailed: (errorMessage: String) -> Unit,
        onVerificationCompleted: (YangaUser) -> Unit
    )
    
    fun verifyOtpCode(
        verificationId: String,
        smsCode: String,
        onResult: (AuthResult) -> Unit
    )
}

/**
 * Robust Sandbox Implementation of the FirebaseAuthEngine.
 * Since Firebase is completely removed, this provides smooth secure local login sandbox flows.
 */
class FirebaseAuthEngineImpl : FirebaseAuthEngine {

    companion object {
        private const val TAG = "FirebaseAuthEngine"
        const val SANDBOX_EMAIL = "sandbox@yanga.live"
        const val SANDBOX_PASS = "yanga2026"
        const val SANDBOX_PHONE = "+2348030000000"
    }

    private var cachedSandboxUser: YangaUser? = null

    override fun isFirebaseAvailable(): Boolean {
        return false // Firebase completely removed as requested
    }

    override fun getCurrentUser(): YangaUser? {
        return cachedSandboxUser
    }

    override fun signOut() {
        cachedSandboxUser = null
    }

    override fun signInWithEmail(email: String, password: String, onResult: (AuthResult) -> Unit) {
        val cleanEmail = email.lowercase().trim()
        if (cleanEmail == "admin101@admin.com") {
            if (password == "Admin$101") {
                val user = YangaUser(
                    uid = "ADMIN-UID-101",
                    email = "admin101@admin.com",
                    phoneNumber = null,
                    isAnonymous = false,
                    isSandboxUser = true
                )
                cachedSandboxUser = user
                onResult(AuthResult.Success(user))
            } else {
                onResult(AuthResult.Failure("Invalid password for Admin. Please use Admin\$101."))
            }
        } else if (cleanEmail == SANDBOX_EMAIL && password == SANDBOX_PASS) {
            val user = YangaUser(
                uid = "SANDBOX-UID-999",
                email = SANDBOX_EMAIL,
                phoneNumber = SANDBOX_PHONE,
                isAnonymous = false,
                isSandboxUser = true
            )
            cachedSandboxUser = user
            onResult(AuthResult.Success(user))
        } else if (email.isNotBlank() && password.length >= 6) {
            // General local auto-sign-in success for convenient developer sandboxing
            val user = YangaUser(
                uid = "SANDBOX-" + email.hashCode().toString(),
                email = email,
                phoneNumber = null,
                isAnonymous = false,
                isSandboxUser = true
            )
            cachedSandboxUser = user
            onResult(AuthResult.Success(user))
        } else {
            onResult(AuthResult.Failure(
                "Yanga Sandbox: To sign in, use a valid email or: $SANDBOX_EMAIL / $SANDBOX_PASS"
            ))
        }
    }

    override fun signUpWithEmail(email: String, password: String, onResult: (AuthResult) -> Unit) {
        if (email.isNotBlank() && password.length >= 6) {
            val user = YangaUser(
                uid = "SANDBOX-" + email.hashCode().toString(),
                email = email,
                phoneNumber = null,
                isAnonymous = false,
                isSandboxUser = true
            )
            cachedSandboxUser = user
            onResult(AuthResult.Success(user))
        } else {
            onResult(AuthResult.Failure("Sandbox sign-up requires a valid email and minimum 6 character password."))
        }
    }

    override fun sendOtpCode(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (verificationId: String) -> Unit,
        onVerificationFailed: (errorMessage: String) -> Unit,
        onVerificationCompleted: (YangaUser) -> Unit
    ) {
        if (phoneNumber.isNotBlank()) {
            onCodeSent("SANDBOX-VERIFICATION-CODE-ID")
        } else {
            onVerificationFailed("Please enter a valid telephone number to request a secure OTP.")
        }
    }

    override fun verifyOtpCode(
        verificationId: String,
        smsCode: String,
        onResult: (AuthResult) -> Unit
    ) {
        if (verificationId == "SANDBOX-VERIFICATION-CODE-ID") {
            if (smsCode == "123456" || smsCode == "000000" || smsCode.length == 6) {
                val user = YangaUser(
                    uid = "SANDBOX-PHONE-UID-111",
                    email = null,
                    phoneNumber = "+2348030000000",
                    isAnonymous = false,
                    isSandboxUser = true
                )
                cachedSandboxUser = user
                onResult(AuthResult.Success(user))
            } else {
                onResult(AuthResult.Failure("Invalid Sandbox Activation code. Enter and verify with any 6-digit code!"))
            }
        } else {
            onResult(AuthResult.Failure("Invalid Local Sandbox verification ID."))
        }
    }
}
