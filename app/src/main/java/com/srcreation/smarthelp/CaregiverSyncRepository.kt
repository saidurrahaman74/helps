package com.srcreation.smarthelp

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.UUID

object CaregiverSyncRepository {

    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private const val FIREBASE_API_KEY = "AIzaSyBaps61pKaq6jERhCAeYcRGGZQg7lAyuZQ"

    private fun ensureAuth(context: Context, onReady: () -> Unit) {
        try {
            db.enableNetwork()
        } catch (_: Exception) {}

        if (auth.currentUser != null) {
            onReady()
        } else {
            auth.signInAnonymously()
                .addOnCompleteListener { onReady() }
        }
    }

    fun forceOnline(context: Context, onComplete: (Boolean) -> Unit = {}) {
        try {
            db.enableNetwork()
        } catch (_: Exception) {}

        val currentUser = auth.currentUser
        if (currentUser != null) {
            onComplete(true)
        } else {
            auth.signInAnonymously()
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }
    }

    fun getPatientUserId(context: Context, callback: (String) -> Unit) {
        val prefs = PreferenceManager(context)
        val existingUid = prefs.getPatientUid()
        if (existingUid.isNotEmpty()) {
            callback(existingUid)
            return
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            prefs.setPatientUid(currentUser.uid)
            callback(currentUser.uid)
        } else {
            auth.signInAnonymously()
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: UUID.randomUUID().toString()
                    prefs.setPatientUid(uid)
                    callback(uid)
                }
                .addOnFailureListener {
                    val fallbackUid = UUID.randomUUID().toString()
                    prefs.setPatientUid(fallbackUid)
                    callback(fallbackUid)
                }
        }
    }

    fun generatePairingCode(context: Context, patientName: String, callback: (String) -> Unit) {
        val code = (100000..999999).random().toString()
        val prefs = PreferenceManager(context)
        prefs.setPairingCode(code)
        prefs.setPatientName(patientName)
        prefs.setFamilyRole("PATIENT")

        callback(code)

        ensureAuth(context) {
            getPatientUserId(context) { uid ->
                val pairingData = hashMapOf(
                    "pairingCode" to code,
                    "patientId" to uid,
                    "patientName" to patientName,
                    "caregiverId" to "",
                    "caregiverName" to "",
                    "status" to "WAITING",
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("pairings").document(code)
                    .set(pairingData, SetOptions.merge())
            }
        }
    }

    fun resetPairingCode(context: Context, patientName: String, callback: (String) -> Unit) {
        generatePairingCode(context, patientName, callback)
    }

    fun unpairDevice(context: Context) {
        val prefs = PreferenceManager(context)
        val code = prefs.getPairingCode()
        if (code.isNotEmpty()) {
            try {
                db.collection("pairings").document(code)
                    .set(mapOf("status" to "UNPAIRED"), SetOptions.merge())
            } catch (_: Exception) {}
        }
        prefs.setPairingCode("")
        prefs.setLinkedPatientId("")
        prefs.setFamilyRole("PATIENT")
    }

    fun connectWithPairingCode(
        context: Context,
        inputCode: String,
        caregiverName: String,
        customPatientName: String = "",
        onResult: (Boolean, String) -> Unit
    ) {
        val cleanCode = inputCode.trim()
        if (cleanCode.length != 6) {
            onResult(false, "সঠিক ৬-ডিজিট কোড দিন (Enter valid 6-digit code)")
            return
        }

        val prefs = PreferenceManager(context)
        val finalPatientName = customPatientName.ifBlank { "Patient" }

        prefs.setFamilyRole("CAREGIVER")
        prefs.setPairingCode(cleanCode)
        prefs.addLinkedPatient(cleanCode, finalPatientName, cleanCode)
        onResult(true, "Successfully connected!")

        ensureAuth(context) {
            val caregiverUid = auth.currentUser?.uid ?: UUID.randomUUID().toString()
            val updateData = hashMapOf(
                "caregiverId" to caregiverUid,
                "caregiverName" to caregiverName,
                "status" to "CONNECTED",
                "pairingCode" to cleanCode
            )
            db.collection("pairings").document(cleanCode)
                .set(updateData, SetOptions.merge())
        }
    }

    fun syncMedicineTaken(
        context: Context,
        medicineName: String,
        dosage: String,
        timeCategory: String,
        status: String
    ) {
        val prefs = PreferenceManager(context)
        val code = prefs.getPairingCode()
        val uid = prefs.getPatientUid()
        val patientId = code.ifEmpty { uid }
        val patientName = prefs.getPatientName()

        if (patientId.isEmpty()) return

        val logId = UUID.randomUUID().toString()
        val logData = hashMapOf(
            "id" to logId,
            "patientId" to patientId,
            "pairingCode" to code,
            "patientUid" to uid,
            "patientName" to patientName,
            "medicineName" to medicineName,
            "dosage" to dosage,
            "timeCategory" to timeCategory,
            "status" to status,
            "timestamp" to System.currentTimeMillis()
        )

        ensureAuth(context) {
            try {
                db.collection("medicine_logs").document(logId).set(logData, SetOptions.merge())
            } catch (_: Exception) {}
        }
    }

    fun syncVitals(
        context: Context,
        type: String,
        title: String,
        valueText: String,
        statusTag: String
    ) {
        val prefs = PreferenceManager(context)
        val code = prefs.getPairingCode()
        val uid = prefs.getPatientUid()
        val patientId = code.ifEmpty { uid }
        val patientName = prefs.getPatientName()

        if (patientId.isEmpty()) return

        val logId = UUID.randomUUID().toString()
        val logData = hashMapOf(
            "id" to logId,
            "patientId" to patientId,
            "pairingCode" to code,
            "patientUid" to uid,
            "patientName" to patientName,
            "type" to type,
            "title" to title,
            "valueText" to valueText,
            "statusTag" to statusTag,
            "timestamp" to System.currentTimeMillis()
        )

        val vitalObj = CaregiverVitalsLog(
            id = logId,
            patientId = patientId,
            patientName = patientName,
            type = type,
            title = title,
            valueText = valueText,
            statusTag = statusTag,
            timestamp = System.currentTimeMillis()
        )

        FirestoreRestApi.postVital(vitalObj)

        ensureAuth(context) {
            try {
                db.collection("vitals").document(logId).set(logData, SetOptions.merge())
            } catch (_: Exception) {}
        }
    }

    fun syncMedicalId(
        context: Context,
        fullName: String,
        age: String,
        bloodGroup: String,
        weight: String,
        emergencyPhone: String
    ) {
        val prefs = PreferenceManager(context)
        val code = prefs.getPairingCode()
        val uid = prefs.getPatientUid()
        val patientId = code.ifEmpty { uid }
        if (patientId.isEmpty()) return

        val data = hashMapOf(
            "patientId" to patientId,
            "pairingCode" to code,
            "fullName" to fullName,
            "age" to age,
            "bloodGroup" to bloodGroup,
            "weight" to weight,
            "emergencyPhone" to emergencyPhone,
            "timestamp" to System.currentTimeMillis()
        )

        ensureAuth(context) {
            try {
                db.collection("medical_ids").document(patientId).set(data, SetOptions.merge())
            } catch (_: Exception) {}
        }
    }

    fun getMedicalIdFlow(patientId: String): Flow<Map<String, Any>?> = callbackFlow {
        if (patientId.isEmpty()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = db.collection("medical_ids").document(patientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.data)
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    fun sendCaregiverNudge(
        context: Context,
        patientId: String,
        message: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val alertId = UUID.randomUUID().toString()
        val alertData = hashMapOf(
            "id" to alertId,
            "patientId" to patientId,
            "pairingCode" to patientId,
            "patientName" to "Caregiver",
            "senderName" to "Caregiver",
            "senderRole" to "CAREGIVER",
            "alertType" to "NUDGE",
            "location" to "",
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        FirestoreRestApi.postNudge(patientId, message)

        ensureAuth(context) {
            try {
                db.collection("emergency_alerts").document(alertId).set(alertData, SetOptions.merge())
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(true) }
            } catch (_: Exception) {
                onComplete(true)
            }
        }
    }

    fun getNudgesFlow(patientId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        if (patientId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("emergency_alerts")
            .whereEqualTo("patientId", patientId)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val pId = data["patientId"] as? String ?: ""
                    val pCode = data["pairingCode"] as? String ?: ""
                    val pType = data["alertType"] as? String ?: ""
                    if ((pId == patientId || pCode == patientId) && pType == "NUDGE") {
                        data
                    } else null
                }?.sortedByDescending { (it["timestamp"] as? Number)?.toLong() ?: 0L } ?: emptyList()
                if (list.isNotEmpty()) trySend(list)
            }

        val restJob = launch {
            while (true) {
                FirestoreRestApi.fetchNudgesRest(patientId) { restList ->
                    if (restList.isNotEmpty()) {
                        trySend(restList)
                    }
                }
                delay(3000)
            }
        }

        awaitClose {
            listener.remove()
            restJob.cancel()
        }
    }

    fun getMedicineLogsFlow(patientId: String): Flow<List<CaregiverMedicineLog>> = callbackFlow {
        if (patientId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("medicine_logs")
            .whereEqualTo("patientId", patientId)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    CaregiverMedicineLog(
                        id = data["id"] as? String ?: doc.id,
                        patientId = data["patientId"] as? String ?: "",
                        patientName = data["patientName"] as? String ?: "",
                        medicineName = data["medicineName"] as? String ?: "",
                        dosage = data["dosage"] as? String ?: "",
                        timeCategory = data["timeCategory"] as? String ?: "",
                        status = data["status"] as? String ?: "",
                        timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
                    )
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                if (list.isNotEmpty()) trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getVitalsLogsFlow(patientId: String): Flow<List<CaregiverVitalsLog>> = callbackFlow {
        if (patientId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("vitals")
            .whereEqualTo("patientId", patientId)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    CaregiverVitalsLog(
                        id = data["id"] as? String ?: doc.id,
                        patientId = data["patientId"] as? String ?: "",
                        patientName = data["patientName"] as? String ?: "",
                        type = data["type"] as? String ?: "",
                        title = data["title"] as? String ?: "",
                        valueText = data["valueText"] as? String ?: "",
                        statusTag = data["statusTag"] as? String ?: "",
                        timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
                    )
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                if (list.isNotEmpty()) trySend(list)
            }

        val restJob = launch {
            while (true) {
                FirestoreRestApi.fetchVitalsRest(patientId) { restList ->
                    if (restList.isNotEmpty()) {
                        trySend(restList)
                    }
                }
                delay(4000)
            }
        }

        awaitClose {
            listener.remove()
            restJob.cancel()
        }
    }

    fun getEmergencyAlertsFlow(patientId: String): Flow<List<CaregiverEmergencyLog>> = callbackFlow {
        if (patientId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = db.collection("emergency_alerts")
            .whereEqualTo("patientId", patientId)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    CaregiverEmergencyLog(
                        id = data["id"] as? String ?: doc.id,
                        patientId = data["patientId"] as? String ?: "",
                        patientName = data["patientName"] as? String ?: "",
                        alertType = data["alertType"] as? String ?: "",
                        location = data["location"] as? String ?: "",
                        message = data["message"] as? String ?: "",
                        timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
                    )
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getPairingStatusFlow(pairingCode: String): Flow<PairingInfo?> = callbackFlow {
        if (pairingCode.isEmpty()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val listener = db.collection("pairings").document(pairingCode)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(PairingInfo::class.java))
                } else {
                    trySend(null)
                }
            }
        awaitClose { listener.remove() }
    }

    fun syncEmergencyAlert(
        context: Context,
        alertType: String,
        location: String,
        message: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val prefs = PreferenceManager(context)
        val code = prefs.getPairingCode()
        val uid = prefs.getPatientUid()
        val patientId = code.ifEmpty { uid }
        val patientName = prefs.getPatientName()

        if (patientId.isEmpty()) {
            onComplete(false)
            return
        }

        val alertId = UUID.randomUUID().toString()
        val data = hashMapOf(
            "id" to alertId,
            "patientId" to patientId,
            "pairingCode" to code,
            "patientName" to patientName,
            "alertType" to alertType,
            "location" to location,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        ensureAuth(context) {
            try {
                db.collection("emergency_alerts").document(alertId).set(data, SetOptions.merge())
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(true) }
            } catch (_: Exception) {
                onComplete(true)
            }
        }
    }

    fun fetchPatientDataNow(
        patientId: String,
        onResult: (List<CaregiverVitalsLog>, List<CaregiverMedicineLog>) -> Unit,
        onError: (String) -> Unit = {}
    ) {
        if (patientId.isEmpty()) {
            onResult(emptyList(), emptyList())
            return
        }

        FirestoreRestApi.fetchVitalsRest(patientId) { restVitals ->
            if (restVitals.isNotEmpty()) {
                onResult(restVitals, emptyList())
            }
        }

        try {
            db.collection("vitals").whereEqualTo("patientId", patientId)
                .limit(50)
                .get()
                .addOnSuccessListener { vitalsSnap ->
                    val vitals = vitalsSnap.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        CaregiverVitalsLog(
                            id = data["id"] as? String ?: doc.id,
                            patientId = data["patientId"] as? String ?: "",
                            patientName = data["patientName"] as? String ?: "",
                            type = data["type"] as? String ?: "",
                            title = data["title"] as? String ?: "",
                            valueText = data["valueText"] as? String ?: "",
                            statusTag = data["statusTag"] as? String ?: "",
                            timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
                        )
                    }.sortedByDescending { it.timestamp }

                    db.collection("medicine_logs").whereEqualTo("patientId", patientId)
                        .limit(50)
                        .get()
                        .addOnSuccessListener { medSnap ->
                            val meds = medSnap.documents.mapNotNull { doc ->
                                val data = doc.data ?: return@mapNotNull null
                                CaregiverMedicineLog(
                                    id = data["id"] as? String ?: doc.id,
                                    patientId = data["patientId"] as? String ?: "",
                                    patientName = data["patientName"] as? String ?: "",
                                    medicineName = data["medicineName"] as? String ?: "",
                                    dosage = data["dosage"] as? String ?: "",
                                    timeCategory = data["timeCategory"] as? String ?: "",
                                    status = data["status"] as? String ?: "",
                                    timestamp = (data["timestamp"] as? Number)?.toLong() ?: 0L
                                )
                            }.sortedByDescending { it.timestamp }

                            onResult(vitals, meds)
                        }
                        .addOnFailureListener { onResult(vitals, emptyList()) }
                }
                .addOnFailureListener { onResult(emptyList(), emptyList()) }
        } catch (_: Exception) {
            onResult(emptyList(), emptyList())
        }
    }

    private object FirestoreRestApi {
        private val client = OkHttpClient.Builder().build()
        private const val BASE_URL = "https://firestore.googleapis.com/v1/projects/helps-app-1371/databases/(default)/documents"

        fun postVital(log: CaregiverVitalsLog) {
            val json = JSONObject().apply {
                put("fields", JSONObject().apply {
                    put("id", JSONObject().put("stringValue", log.id))
                    put("patientId", JSONObject().put("stringValue", log.patientId))
                    put("pairingCode", JSONObject().put("stringValue", log.patientId))
                    put("patientName", JSONObject().put("stringValue", log.patientName))
                    put("type", JSONObject().put("stringValue", log.type))
                    put("title", JSONObject().put("stringValue", log.title))
                    put("valueText", JSONObject().put("stringValue", log.valueText))
                    put("statusTag", JSONObject().put("stringValue", log.statusTag))
                    put("timestamp", JSONObject().put("integerValue", log.timestamp.toString()))
                })
            }
            val request = Request.Builder()
                .url("$BASE_URL/vitals?key=$FIREBASE_API_KEY")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) { response.close() }
            })
        }

        fun postNudge(patientId: String, message: String) {
            val json = JSONObject().apply {
                put("fields", JSONObject().apply {
                    put("id", JSONObject().put("stringValue", UUID.randomUUID().toString()))
                    put("patientId", JSONObject().put("stringValue", patientId))
                    put("pairingCode", JSONObject().put("stringValue", patientId))
                    put("patientName", JSONObject().put("stringValue", "Caregiver"))
                    put("senderName", JSONObject().put("stringValue", "Caregiver"))
                    put("senderRole", JSONObject().put("stringValue", "CAREGIVER"))
                    put("alertType", JSONObject().put("stringValue", "NUDGE"))
                    put("message", JSONObject().put("stringValue", message))
                    put("timestamp", JSONObject().put("integerValue", System.currentTimeMillis().toString()))
                })
            }
            val request = Request.Builder()
                .url("$BASE_URL/emergency_alerts?key=$FIREBASE_API_KEY")
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {}
                override fun onResponse(call: Call, response: Response) { response.close() }
            })
        }

        fun fetchVitalsRest(patientId: String, onResult: (List<CaregiverVitalsLog>) -> Unit) {
            val request = Request.Builder()
                .url("$BASE_URL/vitals?key=$FIREBASE_API_KEY&pageSize=100")
                .get()
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onResult(emptyList())
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { resp ->
                        if (!resp.isSuccessful) {
                            onResult(emptyList())
                            return
                        }
                        val bodyStr = resp.body?.string() ?: ""
                        val list = parseVitalsRestJson(bodyStr, patientId)
                        onResult(list)
                    }
                }
            })
        }

        fun fetchNudgesRest(patientId: String, onResult: (List<Map<String, Any>>) -> Unit) {
            val request = Request.Builder()
                .url("$BASE_URL/emergency_alerts?key=$FIREBASE_API_KEY&pageSize=100")
                .get()
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    onResult(emptyList())
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use { resp ->
                        if (!resp.isSuccessful) {
                            onResult(emptyList())
                            return
                        }
                        val bodyStr = resp.body?.string() ?: ""
                        val list = parseNudgesRestJson(bodyStr, patientId)
                        onResult(list)
                    }
                }
            })
        }

        private fun parseVitalsRestJson(jsonStr: String, targetPatientId: String): List<CaregiverVitalsLog> {
            val result = mutableListOf<CaregiverVitalsLog>()
            try {
                val root = JSONObject(jsonStr)
                val docs = root.optJSONArray("documents") ?: return emptyList()
                for (i in 0 until docs.length()) {
                    val doc = docs.getJSONObject(i)
                    val fields = doc.optJSONObject("fields") ?: continue
                    val pId = fields.optJSONObject("patientId")?.optString("stringValue") ?: ""
                    val pCode = fields.optJSONObject("pairingCode")?.optString("stringValue") ?: ""
                    if (pId == targetPatientId || pCode == targetPatientId) {
                        val id = fields.optJSONObject("id")?.optString("stringValue") ?: ""
                        val pName = fields.optJSONObject("patientName")?.optString("stringValue") ?: ""
                        val type = fields.optJSONObject("type")?.optString("stringValue") ?: ""
                        val title = fields.optJSONObject("title")?.optString("stringValue") ?: ""
                        val valueText = fields.optJSONObject("valueText")?.optString("stringValue") ?: ""
                        val statusTag = fields.optJSONObject("statusTag")?.optString("stringValue") ?: ""
                        val tsStr = fields.optJSONObject("timestamp")?.optString("integerValue")
                            ?: fields.optJSONObject("timestamp")?.optString("stringValue") ?: "0"
                        val timestamp = tsStr.toLongOrNull() ?: 0L

                        result.add(
                            CaregiverVitalsLog(
                                id = id,
                                patientId = pId,
                                patientName = pName,
                                type = type,
                                title = title,
                                valueText = valueText,
                                statusTag = statusTag,
                                timestamp = timestamp
                            )
                        )
                    }
                }
            } catch (_: Exception) {}
            return result.sortedByDescending { it.timestamp }
        }

        private fun parseNudgesRestJson(jsonStr: String, targetPatientId: String): List<Map<String, Any>> {
            val result = mutableListOf<Map<String, Any>>()
            try {
                val root = JSONObject(jsonStr)
                val docs = root.optJSONArray("documents") ?: return emptyList()
                for (i in 0 until docs.length()) {
                    val doc = docs.getJSONObject(i)
                    val fields = doc.optJSONObject("fields") ?: continue
                    val pId = fields.optJSONObject("patientId")?.optString("stringValue") ?: ""
                    val pCode = fields.optJSONObject("pairingCode")?.optString("stringValue") ?: ""
                    val alertType = fields.optJSONObject("alertType")?.optString("stringValue") ?: ""
                    if ((pId == targetPatientId || pCode == targetPatientId) && alertType == "NUDGE") {
                        val msg = fields.optJSONObject("message")?.optString("stringValue") ?: ""
                        val sender = fields.optJSONObject("senderName")?.optString("stringValue")
                            ?: fields.optJSONObject("patientName")?.optString("stringValue") ?: "Caregiver"
                        val tsStr = fields.optJSONObject("timestamp")?.optString("integerValue")
                            ?: fields.optJSONObject("timestamp")?.optString("stringValue") ?: "0"
                        val timestamp = tsStr.toLongOrNull() ?: 0L

                        result.add(
                            mapOf(
                                "message" to msg,
                                "patientName" to sender,
                                "senderName" to sender,
                                "timestamp" to timestamp
                            )
                        )
                    }
                }
            } catch (_: Exception) {}
            return result.sortedByDescending { (it["timestamp"] as? Long) ?: 0L }
        }
    }
}
