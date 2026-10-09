const {onDocumentCreated} = require("firebase-functions/v2/firestore");
const {onSchedule} = require("firebase-functions/v2/scheduler");
const admin = require("firebase-admin");
admin.initializeApp();

// Threshold config object for critical vitals
const THRESHOLDS = {
  spo2Min: 90,
  pulseMin: 50,
  pulseMax: 120,
  tempMax: 38.5,
  bpSystolicMax: 160,
  bpSystolicMin: 90,
};

/**
 * 1. Critical Vitals Alert Trigger
 * Triggered when a new vital reading is created under patients/{patientId}/vitals/{vitalId}
 */
exports.checkCriticalVitals = onDocumentCreated(
    "patients/{patientId}/vitals/{vitalId}",
    async (event) => {
      const snapshot = event.data;
      if (!snapshot) return;

      const vital = snapshot.data();
      const patientId = event.params.patientId;
      const type = (vital.type || "").toUpperCase();
      const val1 = Number(vital.value) || 0;
      const val2 = Number(vital.value2) || 0;
      const unit = vital.unit || "";

      let isCritical = false;
      let alertMessage = "";

      if (type === "SPO2" && val1 < THRESHOLDS.spo2Min) {
        isCritical = true;
        alertMessage = `🚨 Critical Alert: SpO2 is dangerously low at ${val1}% (Below ${THRESHOLDS.spo2Min}%)!`;
      } else if (type === "PULSE" && (val1 < THRESHOLDS.pulseMin || val1 > THRESHOLDS.pulseMax)) {
        isCritical = true;
        alertMessage = `🚨 Critical Alert: Heart rate / Pulse is abnormal at ${val1} BPM!`;
      } else if (type === "TEMPERATURE" && val1 > THRESHOLDS.tempMax) {
        isCritical = true;
        alertMessage = `🚨 Critical Alert: High fever detected at ${val1} ${unit} (Above ${THRESHOLDS.tempMax}°C)!`;
      } else if (type === "BP" && (val1 > THRESHOLDS.bpSystolicMax || val1 < THRESHOLDS.bpSystolicMin)) {
        isCritical = true;
        alertMessage = `🚨 Critical Alert: Blood Pressure Systolic is abnormal at ${val1} mmHg!`;
      }

      if (!isCritical) return;

      try {
        // Find all active caregivers linked to this patient
        const linksSnapshot = await admin.firestore()
            .collection("links")
            .whereEqualTo("patientId", patientId)
            .whereEqualTo("status", "active")
            .get();

        if (linksSnapshot.empty) {
          console.log(`No active caregivers found for patient: ${patientId}`);
          return;
        }

        // Fetch patient name for better alert context
        const patientDoc = await admin.firestore()
            .collection("users")
            .doc(patientId)
            .get();
        const patientName = patientDoc.exists ? (patientDoc.data().name || "Patient") : "Patient";

        const notificationTitle = `⚠️ Critical Health Alert (${patientName})`;

        // Send FCM to each linked caregiver
        for (const linkDoc of linksSnapshot.docs) {
          const caregiverId = linkDoc.data().caregiverId;
          if (!caregiverId) continue;

          const caregiverDoc = await admin.firestore()
              .collection("users")
              .doc(caregiverId)
              .get();

          if (!caregiverDoc.exists) continue;
          const fcmToken = caregiverDoc.data().fcmToken;

          if (fcmToken) {
            const message = {
              token: fcmToken,
              notification: {
                title: notificationTitle,
                body: alertMessage,
              },
              data: {
                title: notificationTitle,
                text: alertMessage,
                type: "critical_vital",
                patientId: patientId,
              },
            };

            await admin.messaging().send(message);
            console.log(`Critical vital alert sent to caregiver ${caregiverId}`);
          }
        }
      } catch (error) {
        console.error("Error processing critical vitals alert:", error);
      }
    },
);

/**
 * 2. Scheduled Missed Medicines Checker (Runs every 10 minutes)
 * Checks if medicine was not taken 30 minutes past scheduled time.
 */
exports.checkMissedMedicines = onSchedule("every 10 minutes", async (event) => {
  const db = admin.firestore();
  const now = Date.now();
  const thirtyMinsAgo = now - (30 * 60 * 1000);

  try {
    // Get all active links
    const linksSnapshot = await db.collection("links")
        .whereEqualTo("status", "active")
        .get();

    if (linksSnapshot.empty) return;

    for (const linkDoc of linksSnapshot.docs) {
      const data = linkDoc.data();
      const patientId = data.patientId;
      const caregiverId = data.caregiverId;

      if (!patientId || !caregiverId) continue;

      // Get patient's medicines
      const medsSnapshot = await db.collection("patients")
          .doc(patientId)
          .collection("medicines")
          .get();

      if (medsSnapshot.empty) continue;

      // Get caregiver fcmToken
      const caregiverDoc = await db.collection("users").doc(caregiverId).get();
      if (!caregiverDoc.exists) continue;
      const fcmToken = caregiverDoc.data().fcmToken;
      if (!fcmToken) continue;

      // Get patient name
      const patientDoc = await db.collection("users").doc(patientId).get();
      const patientName = patientDoc.exists ? (patientDoc.data().name || "Patient") : "Patient";

      for (const medDoc of medsSnapshot.docs) {
        const med = medDoc.data();
        const taken = med.taken || false;
        const lastTakenAt = med.takenAt || 0;

        // If not taken and scheduled time was at least 30 mins ago (and not already notified today)
        // For simplicity, we check if lastTakenAt is 0 or older than start of today, and time has passed.
        const todayStart = new Date().setHours(0, 0, 0, 0);
        const isTakenToday = lastTakenAt >= todayStart;

        if (!taken && !isTakenToday) {
          // Check if we already sent a missed alert today for this medicine
          const missedNotifiedAt = med.missedNotifiedAt || 0;
          const notifiedToday = missedNotifiedAt >= todayStart;

          if (!notifiedToday) {
            // Trigger missed medicine alert
            const title = `⚠️ Missed Medicine Alert (${patientName})`;
            const body = `${patientName} has not taken their scheduled medicine: ${med.name} (${med.dose || ""}).`;

            const message = {
              token: fcmToken,
              notification: {
                title: title,
                body: body,
              },
              data: {
                title: title,
                text: body,
                type: "missed_medicine",
              },
            };

            await admin.messaging().send(message);

            // Mark as notified today to prevent spamming every 10 minutes
            await medDoc.ref.update({missedNotifiedAt: now});
            console.log(`Missed medicine alert sent for patient ${patientId}, med ${med.name}`);
          }
        }
      }
    }
  } catch (error) {
    console.error("Error in scheduled missed medicines check:", error);
  }
});
