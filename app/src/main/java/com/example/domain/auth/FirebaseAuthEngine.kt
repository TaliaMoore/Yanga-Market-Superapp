package com.example.domain.auth

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.FirebaseException
import java.util.concurrent.TimeUnit

/**
 * High-fidelity domain model representing the secure user identity details in Yanga Market.
 * Built following modular OOP practices under clean architecture rules.
 */
data class YangaUser(
    val uid: String,
    val email: String?,
    val phoneNumber: String?,
    val isAnonymous: Boolean,
    val isSandboxUser: Boolean = false
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
 * Real production implementation of the FirebaseAuthEngine with fallback sandbox controls
 * when Firebase Services config (google-services.json) has not yet been linked to the build.
 */
class FirebaseAuthEngineImpl : FirebaseAuthEngine {

    companion object {
        private const val TAG = "FirebaseAuthEngine"
        const val SANDBOX_EMAIL = "sandbox@yanga.live"
        const val SANDBOX_PASS = "yanga2026"
        const val SANDBOX_PHONE = "+2348030000000"
    }

    private var cachedSandboxUser: YangaUser? = null

    // Safe instance helper preventing crash when services are uninitialized
    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseAuth initialization failed: ${e.localizedMessage}")
            null
        }
    }

    override fun isFirebaseAvailable(): Boolean {
        return firebaseAuth != null
    }

    override fun getCurrentUser(): YangaUser? {
        val fbAuth = firebaseAuth
        if (fbAuth != null) {
            val user: FirebaseUser? = fbAuth.currentUser
            if (user != null) {
                return YangaUser(
                    uid = user.uid,
                    email = user.email,
                    phoneNumber = user.phoneNumber,
                    isAnonymous = user.isAnonymous,
                    isSandboxUser = false
                )
            }
        }
        return cachedSandboxUser
    }

    override fun signOut() {
        firebaseAuth?.signOut()
        cachedSandboxUser = null
    }

    override fun signInWithEmail(email: String, password: String, onResult: (AuthResult) -> Unit) {
        val auth = firebaseAuth
        if (auth == null) {
            // High-fidelity Sandbox verification flow
            if (email.lowercase() == SANDBOX_EMAIL && password == SANDBOX_PASS) {
                val user = YangaUser(
                    uid = "SANDBOX-UID-999",
                    email = SANDBOX_EMAIL,
                    phoneNumber = SANDBOX_PHONE,
                    isAnonymous = false,
                    isSandboxUser = true
                )
                cachedSandboxUser = user
                onResult(AuthResult.Success(user))
            } else {
                onResult(AuthResult.Failure(
                    "Default FirebaseApp is not initialized (no google-services.json). " +
                    "To use the local Sandbox secure loop, sign in with: $SANDBOX_EMAIL / $SANDBOX_PASS"
                ))
            }
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        onResult(AuthResult.Success(
                            YangaUser(uid = user.uid, email = user.email, phoneNumber = user.phoneNumber, isAnonymous = user.isAnonymous)
                        ))
                    } else {
                        onResult(AuthResult.Failure("Sign in successful but user details missing."))
                    }
                } else {
                    onResult(AuthResult.Failure(task.exception?.localizedMessage ?: "Unknown Firebase sign-in failure."))
                }
            }
    }

    override fun signUpWithEmail(email: String, password: String, onResult: (AuthResult) -> Unit) {
        val auth = firebaseAuth
        if (auth == null) {
            // Local sandbox sign up
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
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        onResult(AuthResult.Success(
                            YangaUser(uid = user.uid, email = user.email, phoneNumber = user.phoneNumber, isAnonymous = user.isAnonymous)
                        ))
                    } else {
                        onResult(AuthResult.Failure("Account creation succeeded but user details missing."))
                    }
                } else {
                    onResult(AuthResult.Failure(task.exception?.localizedMessage ?: "Unknown Firebase user sign up failure."))
                }
            }
    }

    override fun sendOtpCode(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (verificationId: String) -> Unit,
        onVerificationFailed: (errorMessage: String) -> Unit,
        onVerificationCompleted: (YangaUser) -> Unit
    ) {
        val auth = firebaseAuth
        if (auth == null) {
            // Sandbox OTP response loop
            if (phoneNumber.isNotBlank()) {
                // Instantly reply with a Sandbox ID to proceed to SMS verification entry step
                onCodeSent("SANDBOX-VERIFICATION-CODE-ID")
            } else {
                onVerificationFailed("Please enter a valid telephone number to request a secure OTP.")
            }
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // If phone can be verified automatically (instant SMS fetch)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = task.result?.user
                            if (user != null) {
                                onVerificationCompleted(YangaUser(
                                    uid = user.uid,
                                    email = user.email,
                                    phoneNumber = user.phoneNumber,
                                    isAnonymous = user.isAnonymous
                                ))
                            }
                        } else {
                            onVerificationFailed(task.exception?.localizedMessage ?: "Automatic verification sign-in failed.")
                        }
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onVerificationFailed(e.localizedMessage ?: "Firebase Verification failed.")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun verifyOtpCode(
        verificationId: String,
        smsCode: String,
        onResult: (AuthResult) -> Unit
    ) {
        val auth = firebaseAuth
        if (auth == null) {
            // Local sandbox validation loop
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
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        onResult(AuthResult.Success(YangaUser(
                            uid = user.uid,
                            email = user.email,
                            phoneNumber = user.phoneNumber,
                            isAnonymous = user.isAnonymous
                        )))
                    } else {
                        onResult(AuthResult.Failure("Authentication succeeded but detail retrieval failed."))
                    }
                } else {
                    onResult(AuthResult.Failure(task.exception?.localizedMessage ?: "Invalid verification code entered verification failure."))
                }
            }
    }
}
