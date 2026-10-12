package com.srcreation.smarthelp

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class PatientCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val prefs = PreferenceManager(context)
        if (!prefs.isAlertChecksEnabled()) {
            return Result.success()
        }

        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid ?: return Result.success()
        val firestore = FirebaseFirestore.getInstance()

        try {
            // 1. Check unread instructions: patients/{uid}/instructions where seen == false
            val instructionsSnap = firestore.collection("patients").document(uid)
                .collection("instructions")
                .whereEqualTo("seen", false)
                .get()
                .await()

            for (doc in instructionsSnap.documents) {
                val instructionId = doc.id
                val text = doc.getString("text") ?: "New instruction"
                val eventId = "instruction_$instructionId"

                if (!prefs.isAlreadyNotified(eventId)) {
                    prefs.markNotified(eventId)
                    NotificationHelper.showInstructionNotification(
                        context = context,
                        notificationId = eventId.hashCode(),
                        patientName = "Caregiver",
                        message = text
                    )
                }
            }

            // 2. Check pending access requests: links where patientId == uid and status == "pending"
            val requestsSnap = firestore.collection("links")
                .whereEqualTo("patientId", uid)
                .whereEqualTo("status", "pending")
                .get()
                .await()

            for (doc in requestsSnap.documents) {
                val linkId = doc.id
                val caregiverName = doc.getString("caregiverName") ?: "Caregiver"
                val eventId = "request_$linkId"

                if (!prefs.isAlreadyNotified(eventId)) {
                    prefs.markNotified(eventId)
                    NotificationHelper.showAccessRequestNotification(
                        context = context,
                        notificationId = eventId.hashCode(),
                        caregiverName = caregiverName
                    )
                }
            }

            return Result.success()
        } catch (e: Exception) {
            Log.w("PatientCheckWorker", "Quietly handling patient check error: ${e.localizedMessage}")
            return Result.success()
        }
    }
}
