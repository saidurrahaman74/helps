package com.srcreation.smarthelp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.TimeZone

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun signUp(email: String, pass: String, name: String, role: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")

            val userMap = hashMapOf(
                "name" to name,
                "role" to role, // "patient" or "caregiver"
                "timeZone" to TimeZone.getDefault().id,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users").document(userId).set(userMap).await()
            Result.success(Unit)
        } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            Result.failure(Exception("এই ইমেইল দিয়ে ইতিমধ্যেই একটি অ্যাকাউন্ট তৈরি করা রয়েছে। (An account with this email already exists)"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, pass: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            val userId = auth.currentUser?.uid
            if (userId != null) {
                firestore.collection("users").document(userId)
                    .update("timeZone", TimeZone.getDefault().id).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateTimeZoneOnAppStart() {
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .set(mapOf("timeZone" to TimeZone.getDefault().id), com.google.firebase.firestore.SetOptions.merge())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRole(): String? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            if (!snapshot.exists()) return null
            snapshot.getString("role")
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun uid(): String? {
        return auth.currentUser?.uid
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun updateFcmToken(token: String) {
        val userId = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(userId)
                .update("fcmToken", token)
        } catch (_: Exception) {}
    }

    suspend fun deleteAccount(): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("No user is currently logged in"))
        val userId = user.uid
        return try {
            try {
                firestore.collection("users").document(userId).delete().await()
            } catch (_: Exception) {}

            user.delete().await()
            Result.success(Unit)
        } catch (e: com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException) {
            Result.failure(Exception("REQUIRES_RECENT_LOGIN"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccountWithPassword(password: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("No user is currently logged in"))
        val userId = user.uid
        val email = user.email

        return try {
            if (!email.isNullOrEmpty() && password.isNotEmpty()) {
                val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
                user.reauthenticate(credential).await()
            }

            try {
                firestore.collection("users").document(userId).delete().await()
            } catch (_: Exception) {}

            user.delete().await()
            Result.success(Unit)
        } catch (e: com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("WRONG_PASSWORD"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
