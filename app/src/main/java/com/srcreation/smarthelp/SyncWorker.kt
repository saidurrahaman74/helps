package com.srcreation.smarthelp

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = SyncRepository(applicationContext)
        val authRepo = AuthRepository()
        
        if (!authRepo.isUserLoggedIn()) {
            return Result.success()
        }
        val uid = authRepo.uid()
        val ownerUid = LocalDataOwnership.getOwnerUid(applicationContext)
        
        if ((ownerUid.isNullOrBlank() && LocalDataOwnership.hasUserHealthData(applicationContext)) || 
            (!ownerUid.isNullOrBlank() && !uid.isNullOrBlank() && ownerUid != uid)) {
            return Result.success()
        }

        val success = try {
            repository.syncAllUnsynced()
        } catch (_: Exception) {
            false
        }

        return if (success) Result.success() else Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "SyncWorker_OneTime",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }

        fun schedulePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "SyncWorker_Periodic",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
        }
    }
}
