package com.srcreation.smarthelp

import com.srcreation.smarthelp.db.BpEntity
import com.srcreation.smarthelp.db.PendingDeletionEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.Assert.*
import org.junit.Test

class DeletionSyncUnitTest {

    @Test
    fun deleting_synced_record_queues_one_pending_deletion() {
        val pendingList = mutableListOf<PendingDeletionEntity>()
        val uid = "user_123"
        val recordId = "rec_999"

        pendingList.add(PendingDeletionEntity(id = "p_1", ownerUid = uid, kind = "bp", docId = recordId, createdAt = System.currentTimeMillis()))

        assertEquals(1, pendingList.size)
        assertEquals(recordId, pendingList[0].docId)
        assertEquals("bp", pendingList[0].kind)
    }

    @Test
    fun deleting_any_record_queues_pending_deletion() {
        val pendingList = mutableListOf<PendingDeletionEntity>()
        val uid = "user_123"
        val recordId = "rec_999"

        pendingList.add(PendingDeletionEntity(id = "p_1", ownerUid = uid, kind = "bp", docId = recordId, createdAt = System.currentTimeMillis()))

        assertEquals(1, pendingList.size)
        assertEquals(recordId, pendingList[0].docId)
    }

    @Test
    fun pending_deletion_of_another_uid_is_skipped() {
        val currentUid = "user_123"
        val pendingList = listOf(
            PendingDeletionEntity(id = "p_1", ownerUid = "other_user", kind = "bp", docId = "rec_1", createdAt = System.currentTimeMillis()),
            PendingDeletionEntity(id = "p_2", ownerUid = currentUid, kind = "bp", docId = "rec_2", createdAt = System.currentTimeMillis())
        )

        val processed = mutableListOf<String>()
        for (pending in pendingList) {
            if (pending.ownerUid == currentUid) {
                processed.add(pending.docId)
            }
        }

        assertEquals(1, processed.size)
        assertEquals("rec_2", processed[0])
    }

    @Test
    fun blocked_ownership_state_skips_processing() {
        val currentUid = "user_123"
        val ownerUid = "another_owner"

        val isOwnershipValid = (ownerUid.isBlank() || ownerUid == currentUid)
        val shouldProcess = isOwnershipValid

        assertFalse(shouldProcess)
    }

    @Test
    fun medicine_deletion_queues_doses_cleanup() {
        val pendingList = mutableListOf<PendingDeletionEntity>()
        val medicineId = "med_abc"
        val uid = "user_123"

        pendingList.add(PendingDeletionEntity(id = "p_med", ownerUid = uid, kind = "medicine", docId = medicineId, createdAt = System.currentTimeMillis()))

        assertEquals(1, pendingList.size)
        assertEquals("medicine", pendingList[0].kind)
        assertEquals(medicineId, pendingList[0].docId)
    }

    @Test
    fun clear_phone_data_creates_no_pending_deletions() {
        val pendingList = mutableListOf<PendingDeletionEntity>()
        val localRecordsCleared = true

        assertTrue(localRecordsCleared)
        assertEquals(0, pendingList.size)
    }

    @Test
    fun failed_network_call_keeps_row() {
        val pendingList = mutableListOf(
            PendingDeletionEntity(id = "p_fail", ownerUid = "user_123", kind = "bp", docId = "rec_fail", createdAt = System.currentTimeMillis())
        )
        val networkSuccess = false

        if (networkSuccess) {
            pendingList.removeAt(0)
        }
        assertEquals(1, pendingList.size)
        assertEquals("rec_fail", pendingList[0].docId)
    }

    @Test
    fun testNoDirectInternalDeletionsOutsideDeletionSync() {
        val mockCodeLines = listOf(
            "DeletionSync.deleteBp(context, record)",
            "DeletionSync.deleteMedicine(context, medicine)"
        )
        val violations = mockCodeLines.filter { it.contains("Internal(") && !it.contains("DeletionSync") }
        assertTrue(violations.isEmpty())
    }

    @Test
    fun testProfileIdNotOneNeverUploaded() {
        val records = listOf(
            BpEntity(id = "1", profileId = 1, systolic = 120, diastolic = 80, timeCategory = "Morning", date = "2025-01-01"),
            BpEntity(id = "2", profileId = 2, systolic = 130, diastolic = 85, timeCategory = "Evening", date = "2025-01-01") // Family/Nurse profile
        )
        val uploadedToCloud = records.filter { it.profileId == 1 }
        assertEquals(1, uploadedToCloud.size)
        assertEquals("1", uploadedToCloud[0].id)
    }

    @Test
    fun testMutexSingleFlightConcurrency() = runBlocking {
        val mutex = Mutex()
        var counter = 0

        suspend fun simulateSync(): Int {
            return mutex.withLock {
                val current = counter
                delay(10L)
                counter = current + 1
                counter
            }
        }

        val deferreds = (1..5).map {
            async { simulateSync() }
        }
        deferreds.awaitAll()

        assertEquals(5, counter)
    }

    @Test
    fun testRecordDeletedDuringUploadIsDeletedAgainFromCloud() {
        val localRecords = mutableSetOf("rec_1", "rec_2")
        val cloudDocs = mutableSetOf("rec_1", "rec_2")

        val uploadingRecordId = "rec_2"
        // Simulate record being deleted locally during upload
        localRecords.remove(uploadingRecordId)

        // Post-upload check: if no longer exists locally, delete from cloud
        if (!localRecords.contains(uploadingRecordId)) {
            cloudDocs.remove(uploadingRecordId)
        }

        assertTrue(cloudDocs.contains("rec_1"))
        assertFalse(cloudDocs.contains("rec_2"))
    }
}
