package com.srcreation.smarthelp

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object BackgroundCheckScheduler {
    fun scheduleWorkers(context: Context) {
        val prefs = PreferenceManager(context)
        if (!prefs.isAlertChecksEnabled()) {
            cancelWorkers(context)
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val caregiverRequest = PeriodicWorkRequestBuilder<CaregiverCheckWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
            .build()

        val patientRequest = PeriodicWorkRequestBuilder<PatientCheckWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "CaregiverCheckWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            caregiverRequest
        )

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "PatientCheckWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            patientRequest
        )
    }

    fun cancelWorkers(context: Context) {
        try {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork("CaregiverCheckWorker")
            workManager.cancelUniqueWork("PatientCheckWorker")
        } catch (_: Exception) {}
    }
}
