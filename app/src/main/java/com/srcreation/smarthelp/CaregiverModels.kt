package com.srcreation.smarthelp
import androidx.annotation.Keep
@Keep enum class FamilyRole { PATIENT, CAREGIVER }
@Keep data class PairingInfo(val pairingCode: String = "", val patientId: String = "", val patientName: String = "", val caregiverId: String = "", val caregiverName: String = "", val status: String = "", val createdAt: Long = 0L)
@Keep data class CaregiverMedicineLog(val id: String = "", val patientId: String = "", val patientName: String = "", val medicineId: Int = 0, val medicineName: String = "", val dosage: String = "", val timeCategory: String = "", val status: String = "", val timestamp: Long = 0L)
@Keep data class CaregiverVitalsLog(val id: String = "", val patientId: String = "", val patientName: String = "", val type: String = "", val title: String = "", val valueText: String = "", val statusTag: String = "", val timestamp: Long = 0L)
@Keep data class CaregiverEmergencyLog(val id: String = "", val patientId: String = "", val patientName: String = "", val alertType: String = "", val location: String = "", val message: String = "", val timestamp: Long = 0L)
