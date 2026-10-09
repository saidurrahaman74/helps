package com.srcreation.smarthelp

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class PairingRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val authRepo = AuthRepository()

    suspend fun createPairingCode(): String {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("Pairing", "Failed to generate pairing code: User not logged in")
            throw IllegalStateException("User not logged in")
        }

        val role = authRepo.getRole()
        if (!role.equals("patient", ignoreCase = true)) {
            Log.e("Pairing", "Failed to generate pairing code: Only patients can generate pairing codes (Role: $role)")
            throw IllegalStateException("Only patients can generate pairing codes")
        }

        val patientId = currentUser.uid
        val code = (100000..999999).random().toString()
        val now = System.currentTimeMillis()
        val expiresAt = now + 10 * 60 * 1000L // 10 minutes

        val codeMap = hashMapOf(
            "patientId" to patientId,
            "expiresAt" to expiresAt,
            "createdAt" to now
        )

        try {
            firestore.collection("pairingCodes").document(code).set(codeMap).await()
            Log.d("Pairing", "Pairing code generated successfully: $code")
            return code
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Pairing", "Failed to generate pairing code", e)
            throw e
        }
    }

    suspend fun linkWithCode(code: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("Pairing", "Failed to link patient: User not logged in")
            throw Exception("User not logged in")
        }
        val caregiverId = currentUser.uid
        val cleanCode = code.trim()

        try {
            val docSnap = firestore.collection("pairingCodes").document(cleanCode).get().await()
            if (!docSnap.exists()) {
                Log.e("Pairing", "Invalid code: $cleanCode")
                throw Exception("Invalid code")
            }

            val expiresAt = docSnap.getLong("expiresAt") ?: 0L
            if (expiresAt < System.currentTimeMillis()) {
                Log.e("Pairing", "Code expired: $cleanCode")
                try {
                    firestore.collection("pairingCodes").document(cleanCode).delete().await()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {}
                throw Exception("Code expired")
            }

            val patientId = docSnap.getString("patientId")
            if (patientId.isNullOrBlank()) {
                Log.e("Pairing", "Patient ID missing in pairing code: $cleanCode")
                throw Exception("Invalid code")
            }

            val linkId = "${caregiverId}_${patientId}"
            val linkMap = hashMapOf(
                "patientId" to patientId,
                "caregiverId" to caregiverId,
                "status" to "active",
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("links").document(linkId).set(linkMap).await()
            Log.d("Pairing", "Link created successfully: $linkId")

            try {
                firestore.collection("pairingCodes").document(cleanCode).delete().await()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {}

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Pairing", "Failed to link patient with code: ${e.localizedMessage}", e)
            throw e
        }
    }

    suspend fun linkPatientWithCode(code: String): Result<String> {
        return try {
            linkWithCode(code)
            // Extract patientId for Result success
            val cleanCode = code.trim()
            // Since doc is deleted, we can return success or patientId if needed.
            Result.success("Success")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Pairing", "linkPatientWithCode failed: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
}
