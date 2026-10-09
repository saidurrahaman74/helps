package com.srcreation.smarthelp

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class InstructionItem(
    val id: String = "",
    val text: String = "",
    val sentAt: Long = 0L,
    val seen: Boolean = false
)

class InstructionsRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun sendInstruction(patientId: String, text: String): Result<Unit> {
        return try {
            val cleanText = text.trim()
            if (cleanText.isEmpty()) throw Exception("Instruction text cannot be empty")
            if (patientId.isEmpty()) throw Exception("Patient ID is missing")

            val docRef = firestore.collection("patients").document(patientId)
                .collection("instructions").document()

            val data = hashMapOf(
                "text" to cleanText,
                "sentAt" to System.currentTimeMillis(),
                "seen" to false
            )

            docRef.set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getInstructionsFlow(patientId: String): Flow<List<InstructionItem>> = callbackFlow {
        if (patientId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = firestore.collection("patients").document(patientId)
            .collection("instructions")
            .orderBy("sentAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    val text = doc.getString("text") ?: ""
                    val sentAt = doc.getLong("sentAt") ?: 0L
                    val seen = doc.getBoolean("seen") ?: false
                    InstructionItem(doc.id, text, sentAt, seen)
                }
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    fun getUnreadCountFlow(patientId: String): Flow<Int> = callbackFlow {
        if (patientId.isEmpty()) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = firestore.collection("patients").document(patientId)
            .collection("instructions")
            .whereEqualTo("seen", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(0)
                    return@addSnapshotListener
                }
                trySend(snapshot.size())
            }

        awaitClose { listener.remove() }
    }

    suspend fun markAsSeen(patientId: String, instructionId: String) {
        if (patientId.isEmpty() || instructionId.isEmpty()) return
        try {
            firestore.collection("patients").document(patientId)
                .collection("instructions").document(instructionId)
                .update("seen", true)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
