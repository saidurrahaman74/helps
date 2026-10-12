package com.srcreation.smarthelp

import android.content.Context
import androidx.room.withTransaction
import com.srcreation.smarthelp.db.*
import java.util.UUID

object DeletionSync {
    private fun getUid(context: Context): String {
        return AuthRepository().uid() ?: PreferenceManager(context).getPatientUid() ?: "local_user"
    }

    suspend fun deleteBp(context: Context, record: BpEntity) {
        val db = AppDatabase.getDatabase(context)
        val uid = getUid(context)
        db.withTransaction {
            db.bpDao().deleteBpInternal(record)
            val pending = PendingDeletionEntity(
                id = UUID.randomUUID().toString(),
                ownerUid = uid,
                kind = "bp",
                docId = record.id,
                createdAt = System.currentTimeMillis()
            )
            db.pendingDeletionDao().insert(pending)
        }
        SyncWorker.enqueue(context)
    }

    suspend fun deletePulse(context: Context, record: PulseEntity) {
        val db = AppDatabase.getDatabase(context)
        val uid = getUid(context)
        db.withTransaction {
            db.pulseDao().deletePulseInternal(record)
            val pending = PendingDeletionEntity(
                id = UUID.randomUUID().toString(),
                ownerUid = uid,
                kind = "pulse",
                docId = record.id,
                createdAt = System.currentTimeMillis()
            )
            db.pendingDeletionDao().insert(pending)
        }
        SyncWorker.enqueue(context)
    }

    suspend fun deleteSpO2(context: Context, record: SpO2Entity) {
        val db = AppDatabase.getDatabase(context)
        val uid = getUid(context)
        db.withTransaction {
            db.spo2Dao().deleteSpO2Internal(record)
            val pending = PendingDeletionEntity(
                id = UUID.randomUUID().toString(),
                ownerUid = uid,
                kind = "spo2",
                docId = record.id,
                createdAt = System.currentTimeMillis()
            )
            db.pendingDeletionDao().insert(pending)
        }
        SyncWorker.enqueue(context)
    }

    suspend fun deleteInsulin(context: Context, record: InsulinEntity) {
        val db = AppDatabase.getDatabase(context)
        val uid = getUid(context)
        db.withTransaction {
            db.insulinDao().deleteInsulinInternal(record)
            val pending = PendingDeletionEntity(
                id = UUID.randomUUID().toString(),
                ownerUid = uid,
                kind = "insulin",
                docId = record.id,
                createdAt = System.currentTimeMillis()
            )
            db.pendingDeletionDao().insert(pending)
        }
        SyncWorker.enqueue(context)
    }

    suspend fun deleteTemp(context: Context, record: TempEntity) {
        val db = AppDatabase.getDatabase(context)
        val uid = getUid(context)
        db.withTransaction {
            db.tempDao().deleteTempInternal(record)
            val pending = PendingDeletionEntity(
                id = UUID.randomUUID().toString(),
                ownerUid = uid,
                kind = "temp",
                docId = record.id,
                createdAt = System.currentTimeMillis()
            )
            db.pendingDeletionDao().insert(pending)
        }
        SyncWorker.enqueue(context)
    }

    suspend fun deleteMedicine(context: Context, medicine: MedicineEntity) {
        val db = AppDatabase.getDatabase(context)
        val uid = getUid(context)
        db.withTransaction {
            db.medicineDao().deleteMedicineInternal(medicine)
            db.medicineDoseDao().deleteDosesByMedicineIdInternal(medicine.id)
            val pending = PendingDeletionEntity(
                id = UUID.randomUUID().toString(),
                ownerUid = uid,
                kind = "medicine",
                docId = medicine.id,
                createdAt = System.currentTimeMillis()
            )
            db.pendingDeletionDao().insert(pending)
        }
        SyncWorker.enqueue(context)
    }
}
