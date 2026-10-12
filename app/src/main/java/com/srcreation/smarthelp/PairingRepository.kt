package com.srcreation.smarthelp

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class PairingRepository(private val context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun createPairingCode(): String {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("Pairing", "Failed to generate pairing code: User not logged in")
            throw IllegalStateException("User not logged in")
        }

        val patientId = currentUser.uid
        val installId = PreferenceManager(context).getInstallId()
        if (installId.isBlank()) {
            throw IllegalStateException("Install ID cannot be blank")
        }

        val code = (100000..999999).random().toString()
        val now = System.currentTimeMillis()
        val expiresAt = now + 10 * 60 * 1000L // 10 minutes

        val codeMap = hashMapOf(
            "patientId" to patientId,
            "patientInstallId" to installId,
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

            // Same-phone guard 1: Reject if patientId == current caregiverId
            if (patientId == caregiverId) {
                throw Exception("You cannot link your own account to yourself.")
            }

            // Same-phone guard 2: Reject if patientInstallId == this phone's installId
            val patientInstallId = docSnap.getString("patientInstallId") ?: ""
            val caregiverInstallId = PreferenceManager(context).getInstallId()
            if (caregiverInstallId.isBlank()) {
                throw IllegalStateException("Install ID cannot be blank")
            }
            if (patientInstallId.isNotBlank() && patientInstallId == caregiverInstallId) {
                throw Exception("Cannot link accounts on the same physical phone/installation.")
            }

            val linkId = "${caregiverId}_${patientId}"

            // Check existing link status
            val existingLinkDoc = firestore.collection("links").document(linkId).get().await()
            if (existingLinkDoc.exists()) {
                val existingStatus = existingLinkDoc.getString("status") ?: "active"
                if (existingStatus == "active") {
                    throw Exception("You are already linked to this patient")
                } else if (existingStatus == "pending") {
                    throw Exception("Your request is already waiting for approval")
                }
            }

            // Resolve caregiverName
            var caregiverName = ""
            try {
                val caregiverUserDoc = firestore.collection("users").document(caregiverId).get().await()
                caregiverName = caregiverUserDoc.getString("name") ?: ""
            } catch (_: Exception) {}

            if (caregiverName.isBlank()) {
                caregiverName = currentUser.displayName ?: currentUser.email ?: "Caregiver"
            }

            val now = System.currentTimeMillis()
            val linkMap = hashMapOf(
                "patientId" to patientId,
                "caregiverId" to caregiverId,
                "status" to "pending",
                "code" to cleanCode,
                "caregiverName" to caregiverName,
                "patientInstallId" to patientInstallId,
                "caregiverInstallId" to caregiverInstallId,
                "createdAt" to now
            )

            // Create link document with status = "pending" while pairing code still exists
            firestore.collection("links").document(linkId).set(linkMap).await()
            Log.d("Pairing", "Pending link created successfully: $linkId")

            // Delete pairing code after link document is written successfully
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
            Result.success("Success")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Pairing", "linkPatientWithCode failed: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
}
