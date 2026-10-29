package com.srcreation.smarthelp

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class CaregiverCheckWorker(
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

        var permissionLoggedThisRun = false

        try {
            val linksSnapshot = try {
                firestore.collection("links")
                    .whereEqualTo("caregiverId", uid)
                    .whereEqualTo("status", "active")
                    .get()
                    .await()
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: ""
                if ((msg.contains("PERMISSION_DENIED", true) || msg.contains("FAILED_PRECONDITION", true)) && !permissionLoggedThisRun) {
                    permissionLoggedThisRun = true
                    Log.e("AlertCheck", "Firestore permission/precondition error checking links: $msg")
                } else {
                    Log.e("AlertCheck", "Error fetching caregiver links")
                }
                return Result.success()
            }

            if (linksSnapshot.isEmpty) {
                return Result.success()
            }

            val pendingNotifications = mutableListOf<Pair<String, String>>() // Pair(title, message)

            for (linkDoc in linksSnapshot.documents) {
                val patientId = linkDoc.getString("patientId") ?: continue
                if (patientId.isBlank()) continue

                val userDoc = try {
                    firestore.collection("users").document(patientId).get().await()
                } catch (e: Exception) {
                    null
                }
                val patientName = userDoc?.getString("name") ?: "Patient"

                val lastChecked = prefs.getLastCheckedTimestamp(patientId)
                val currentTime = System.currentTimeMillis()

                // First run initialization: if no lastChecked exists (0L), initialize to current time and do not notify about older readings.
                if (lastChecked == 0L) {
                    prefs.setLastCheckedTimestamp(patientId, currentTime)
                    continue
                }

                var maxTsFound = lastChecked

                // ONE query per patient filtered only by timestamp greater than lastChecked, ordered by timestamp, limit 100.
                val vitalsSnap = try {
                    firestore.collection("patients").document(patientId)
                        .collection("vitals")
                        .whereGreaterThan("timestamp", lastChecked)
                        .orderBy("timestamp", Query.Direction.ASCENDING)
                        .limit(100)
                        .get()
                        .await()
                } catch (e: Exception) {
                    val msg = e.localizedMessage ?: ""
                    if ((msg.contains("PERMISSION_DENIED", true) || msg.contains("FAILED_PRECONDITION", true)) && !permissionLoggedThisRun) {
                        permissionLoggedThisRun = true
                        Log.e("AlertCheck", "Firestore permission/precondition error reading vitals: $msg")
                    } else {
                        Log.e("AlertCheck", "Error fetching vitals for patient")
                    }
                    null
                }

                if (vitalsSnap != null) {
                    val maxAllowedTs = currentTime + 5 * 60 * 1000L
                    val twentyFourHoursAgo = currentTime - 24 * 3600 * 1000L

                    for (doc in vitalsSnap.documents) {
                        val type = doc.getString("type") ?: ""
                        val ts = when (val t = doc.get("timestamp")) {
                            is Number -> t.toLong()
                            is String -> t.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                        if (ts > maxAllowedTs) continue
                        if (ts > maxTsFound) maxTsFound = ts

                        val val1 = doc.get("value")
                        val val2 = doc.get("value2")
                        val unit = doc.getString("unit") ?: ""

                        // Classify readings by type on client and evaluate with AlertThresholds
                        val alertResult = when (type) {
                            "BP" -> {
                                val sys = when (val1) { is Number -> val1.toInt(); is String -> val1.toIntOrNull() ?: 0; else -> 0 }
                                val dia = when (val2) { is Number -> val2.toInt(); is String -> val2.toIntOrNull() ?: 0; else -> 0 }
                                AlertThresholds.checkBp(sys, dia)
                            }
                            "PULSE" -> {
                                val bpm = when (val1) { is Number -> val1.toInt(); is String -> val1.toIntOrNull() ?: 0; else -> 0 }
                                AlertThresholds.checkPulse(bpm)
                            }
                            "SPO2" -> {
                                val spo2 = when (val1) { is Number -> val1.toDouble(); is String -> val1.toDoubleOrNull() ?: 0.0; else -> 0.0 }
                                AlertThresholds.checkSpO2(spo2)
                            }
                            "GLUCOSE" -> {
                                val glucose = when (val1) { is Number -> val1.toDouble(); is String -> val1.toDoubleOrNull() ?: 0.0; else -> 0.0 }
                                AlertThresholds.checkGlucose(glucose, unit)
                            }
                            "TEMPERATURE" -> {
                                val temp = when (val1) { is Number -> val1.toDouble(); is String -> val1.toDoubleOrNull() ?: 0.0; else -> 0.0 }
                                AlertThresholds.checkTemperature(temp, unit)
                            }
                            else -> null
                        }

                        if (alertResult != null && !alertResult.isImpossible && alertResult.level == VitalAlertLevel.DANGER) {
                            if (ts >= twentyFourHoursAgo) {
                                val eventId = "danger_${patientId}_${doc.id}_$ts"
                                if (!prefs.isAlreadyNotified(eventId)) {
                                    prefs.markNotified(eventId)
                                    val msg = if (alertResult.detailText.isNotBlank()) {
                                        "${alertResult.message}, ${alertResult.detailText}"
                                    } else {
                                        alertResult.message
                                    }
                                    pendingNotifications.add(
                                        Pair("Health Alert: $type", "Patient $patientName has a critical reading: $msg")
                                    )
                                }
                            }
                        }
                    }
                }

                if (maxTsFound > lastChecked) {
                    prefs.setLastCheckedTimestamp(patientId, maxTsFound)
                }

                // Check missed doses
                try {
                    val timeZoneId = userDoc?.getString("timeZone")?.takeIf { it.isNotBlank() } ?: TimeZone.getDefault().id
                    val tz = TimeZone.getTimeZone(timeZoneId)
                    val calendar = Calendar.getInstance(tz)
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    if (hour in 0..4) {
                        calendar.add(Calendar.DAY_OF_YEAR, -1)
                    }
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { this.timeZone = tz }
                    val todayDate = dateFormat.format(calendar.time)

                    val rawHour = calendar.get(Calendar.HOUR_OF_DAY)
                    val rawMinute = calendar.get(Calendar.MINUTE)
                    val currentTimeInMinutes = if (rawHour in 0..4) (1440 + rawHour * 60 + rawMinute) else (rawHour * 60 + rawMinute)

                    val medsSnap = firestore.collection("patients").document(patientId)
                        .collection("medicines")
                        .get()
                        .await()

                    for (medDoc in medsSnap.documents) {
                        val medId = medDoc.id
                        val medName = medDoc.getString("name") ?: "Medicine"
                        val timeStr = medDoc.getString("time") ?: ""
                        val slotNames = timeStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        val scheduleTimesMap = medDoc.get("scheduleTimes") as? Map<String, String> ?: emptyMap()

                        val doseSnap = firestore.collection("patients").document(patientId)
                            .collection("medicines").document(medId)
                            .collection("doses")
                            .whereEqualTo("date", todayDate)
                            .get()
                            .await()

                        val takenSlots = mutableSetOf<String>()
                        for (dDoc in doseSnap.documents) {
                            val slot = dDoc.getString("slot") ?: ""
                            val taken = dDoc.getBoolean("taken") ?: false
                            if (slot.isNotBlank() && taken) {
                                takenSlots.add(slot)
                            }
                        }

                        for (slotName in slotNames) {
                            val status = MedicineScheduleHelper.calculateDoseStatus(slotName, takenSlots.contains(slotName), currentTimeInMinutes, scheduleTimesMap = scheduleTimesMap)
                            if (status == "Missed") {
                                val missedEventId = "missed_${patientId}_${medId}_${slotName}_$todayDate"
                                if (!prefs.isAlreadyNotified(missedEventId)) {
                                    prefs.markNotified(missedEventId)
                                    pendingNotifications.add(
                                        Pair("Missed Medicine Dose", "Patient $patientName missed $medName ($slotName dose).")
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AlertCheck", "Error checking medicines for patient")
                }
            }

            // Cap notifications of one run at 3 (group the rest into one summary notification)
            val maxNotifications = 3
            if (pendingNotifications.size <= maxNotifications) {
                pendingNotifications.forEachIndexed { index, (title, msg) ->
                    NotificationHelper.showHealthAlertNotification(
                        context = context,
                        notificationId = (title + msg + index).hashCode(),
                        patientName = "Patient",
                        title = title,
                        message = msg
                    )
                }
            } else {
                for (i in 0 until maxNotifications) {
                    val (title, msg) = pendingNotifications[i]
                    NotificationHelper.showHealthAlertNotification(
                        context = context,
                        notificationId = (title + msg + i).hashCode(),
                        patientName = "Patient",
                        title = title,
                        message = msg
                    )
                }
                val extraCount = pendingNotifications.size - maxNotifications
                NotificationHelper.showHealthAlertNotification(
                    context = context,
                    notificationId = "summary_alert".hashCode(),
                    patientName = "Patients",
                    title = "Multiple Health Alerts",
                    message = "$extraCount more alerts, open the app."
                )
            }

            return Result.success()
        } catch (e: Exception) {
            val msg = e.localizedMessage ?: "Unknown error"
            if (msg.contains("PERMISSION_DENIED", true) || msg.contains("FAILED_PRECONDITION", true)) {
                Log.e("AlertCheck", "Firestore permission/precondition error in worker: $msg")
            } else {
                Log.e("AlertCheck", "Background check worker error occurred", e)
            }
            return Result.success()
        }
    }
}
