package com.srcreation.smarthelp

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.srcreation.smarthelp.db.AppDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LinkedPatientInfo(
    val patientId: String = "",
    val patientName: String = "Patient",
    val pairingCode: String = ""
)

class PreferenceManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("smart_help_prefs", Context.MODE_PRIVATE)

    fun getInstallId(): String {
        val installPrefs = context.getSharedPreferences("smart_help_install_prefs", Context.MODE_PRIVATE)
        var id = installPrefs.getString("install_id", null)
        if (id.isNullOrBlank()) {
            id = java.util.UUID.randomUUID().toString()
            installPrefs.edit().putString("install_id", id).apply()
        }
        return id!!
    }

    fun getLocalDataOwnerUid(): String? = sharedPreferences.getString("local_data_owner_uid", null)
    fun getLocalDataOwnerUidInternal(): String? = sharedPreferences.getString("local_data_owner_uid", null)
    internal fun setLocalDataOwnerUidInternal(uid: String) = sharedPreferences.edit().putString("local_data_owner_uid", uid).apply()

    fun setUnitSystem(isMetric: Boolean) {
        sharedPreferences.edit().putBoolean("is_metric", isMetric).apply()
    }

    fun isMetric(): Boolean {
        return sharedPreferences.getBoolean("is_metric", true)
    }

    fun setThemeMode(mode: Int) {
        sharedPreferences.edit().putInt("app_theme_mode", mode).apply()
    }

    fun getThemeMode(): Int {
        return sharedPreferences.getInt("app_theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    fun setAppLock(pin: String?) {
        sharedPreferences.edit().putString("app_pin", pin).apply()
    }

    fun getAppLock(): String? {
        return sharedPreferences.getString("app_pin", null)
    }

    fun setSecurityQuestion(question: String, answer: String) {
        sharedPreferences.edit()
            .putString("security_question", question)
            .putString("security_answer", answer)
            .apply()
    }

    fun getSecurityQuestion(): String? = sharedPreferences.getString("security_question", null)
    fun getSecurityAnswer(): String? = sharedPreferences.getString("security_answer", null)

    companion object {
        private val _activeProfileFlow = MutableStateFlow(1)
    }

    val activeProfileFlow: Flow<Int> = _activeProfileFlow.asStateFlow()

    fun setActiveProfile(id: Int) {
        sharedPreferences.edit().putInt("active_profile_id", id).apply()
        _activeProfileFlow.value = id
    }

    fun getActiveProfile(): Int {
        return sharedPreferences.getInt("active_profile_id", 1)
    }

    fun setBiometricLock(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("biometric_lock", enabled).apply()
    }

    fun isBiometricLockEnabled(): Boolean {
        return sharedPreferences.getBoolean("biometric_lock", false)
    }

    fun isDisclaimerAccepted(): Boolean {
        return sharedPreferences.getBoolean("disclaimer_accepted", false)
    }

    fun setDisclaimerAccepted(accepted: Boolean) {
        sharedPreferences.edit().putBoolean("disclaimer_accepted", accepted).apply()
    }

    fun setNurseId(name: String, hospital: String, department: String, designation: String, employeeId: String, imageUri: String? = null) {
        sharedPreferences.edit()
            .putString("nurse_name", name)
            .putString("nurse_hospital", hospital)
            .putString("nurse_department", department)
            .putString("nurse_designation", designation)
            .putString("nurse_employee_id", employeeId)
            .putString("nurse_image_uri", imageUri)
            .apply()
    }

    fun getNurseName(): String = sharedPreferences.getString("nurse_name", "") ?: ""
    fun getNurseHospital(): String = sharedPreferences.getString("nurse_hospital", "") ?: ""
    fun getNurseDepartment(): String = sharedPreferences.getString("nurse_department", "") ?: ""
    fun getNurseDesignation(): String = sharedPreferences.getString("nurse_designation", "") ?: ""
    fun getNurseEmployeeId(): String = sharedPreferences.getString("nurse_employee_id", "") ?: ""
    fun getNurseImageUri(): String? = sharedPreferences.getString("nurse_image_uri", null)
    
    fun setNurseImageUri(uri: String?) {
        sharedPreferences.edit().putString("nurse_image_uri", uri).apply()
    }

    // Medicine Reminder Settings
    fun setMedVolume(volume: Float) {
        sharedPreferences.edit().putFloat("med_volume", volume).apply()
    }
    fun getMedVolume(): Float = sharedPreferences.getFloat("med_volume", 1.0f)

    fun setMedVibration(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("med_vibration", enabled).apply()
    }
    fun isMedVibrationEnabled(): Boolean = sharedPreferences.getBoolean("med_vibration", true)

    fun setMedAlarmTone(uri: String?) {
        sharedPreferences.edit().putString("med_alarm_tone", uri).apply()
    }
    fun getMedAlarmTone(): String? = sharedPreferences.getString("med_alarm_tone", null)

    fun setMedAlarmBackground(color: String) {
        sharedPreferences.edit().putString("med_alarm_bg", color).apply()
    }
    fun getMedAlarmBackground(): String = sharedPreferences.getString("med_alarm_bg", "#1A237E") ?: "#1A237E"

    fun setVoiceReminderEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("med_voice_reminder", enabled).apply()
    }
    fun isVoiceReminderEnabled(): Boolean = sharedPreferences.getBoolean("med_voice_reminder", true)

    // Family Connectivity Preferences
    fun setFamilyRole(role: String) {
        sharedPreferences.edit().putString("family_role", role).apply()
    }
    fun getFamilyRole(): String = sharedPreferences.getString("family_role", "PATIENT") ?: "PATIENT"

    fun setPairingCode(code: String) {
        sharedPreferences.edit().putString("pairing_code", code).apply()
    }
    fun getPairingCode(): String = sharedPreferences.getString("pairing_code", "") ?: ""

    fun setLinkedPatientId(patientId: String) {
        sharedPreferences.edit().putString("linked_patient_id", patientId).apply()
    }
    fun getLinkedPatientId(): String = sharedPreferences.getString("linked_patient_id", "") ?: ""

    fun setPatientName(name: String) {
        sharedPreferences.edit().putString("patient_name", name).apply()
    }
    fun getPatientName(): String = sharedPreferences.getString("patient_name", "Patient") ?: "Patient"

    fun setPatientPhone(phone: String) {
        sharedPreferences.edit().putString("patient_phone", phone).apply()
    }
    fun getPatientPhone(): String = sharedPreferences.getString("patient_phone", "") ?: ""

    fun setPatientAge(age: String) {
        sharedPreferences.edit().putString("patient_age", age).apply()
    }
    fun getPatientAge(): String = sharedPreferences.getString("patient_age", "") ?: ""

    fun setPatientUid(uid: String) {
        sharedPreferences.edit().putString("patient_uid", uid).apply()
    }
    fun getPatientUid(): String = sharedPreferences.getString("patient_uid", "") ?: ""

    fun setCustomGeminiApiKey(key: String?) {
        sharedPreferences.edit().putString("custom_gemini_api_key", key).apply()
    }
    fun getCustomGeminiApiKey(): String? {
        val key = sharedPreferences.getString("custom_gemini_api_key", null)
        return if (!key.isNullOrBlank()) key else null
    }

    fun setDonorDeviceId(id: String) {
        sharedPreferences.edit().putString("donor_device_id", id).apply()
    }
    fun getDonorDeviceId(): String {
        return sharedPreferences.getString("donor_device_id", "") ?: ""
    }

    fun addLocallyDeletedDonorId(id: String) {
        val set = getLocallyDeletedDonorIds().toMutableSet()
        set.add(id)
        sharedPreferences.edit().putStringSet("locally_deleted_donors", set).apply()
    }
    fun getLocallyDeletedDonorIds(): Set<String> {
        return sharedPreferences.getStringSet("locally_deleted_donors", emptySet()) ?: emptySet()
    }

    fun addLinkedPatient(patientId: String, patientName: String, code: String) {
        val list = getLinkedPatientsList().toMutableList()
        list.removeAll { it.patientId == patientId || (code.isNotEmpty() && it.pairingCode == code) }
        val cleanName = patientName.substringBefore("(").trim().ifEmpty { "Patient" }
        list.add(LinkedPatientInfo(patientId, cleanName, code))
        val json = Gson().toJson(list)
        sharedPreferences.edit()
            .putString("linked_patients_list_json", json)
            .putString("linked_patient_id", patientId)
            .putString("patient_name", cleanName)
            .apply()
    }

    fun getLinkedPatientsList(): List<LinkedPatientInfo> {
        val json = sharedPreferences.getString("linked_patients_list_json", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<LinkedPatientInfo>>() {}.type
            Gson().fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun removeLinkedPatient(patientId: String) {
        val list = getLinkedPatientsList().toMutableList()
        list.removeAll { it.patientId == patientId || it.pairingCode == patientId }
        val json = Gson().toJson(list)
        val nextActive = list.firstOrNull()
        val editor = sharedPreferences.edit()
            .putString("linked_patients_list_json", json)
            .putString("linked_patient_id", nextActive?.patientId ?: "")
            .putString("patient_name", nextActive?.patientName ?: "Patient")
        if (nextActive == null) {
            editor.putString("family_role", "PATIENT")
            editor.putString("pairing_code", "")
        }
        editor.apply()
    }

    fun setStepGoal(goal: Int) {
        sharedPreferences.edit().putInt("step_goal", goal).apply()
    }
    fun getStepGoal(): Int = sharedPreferences.getInt("step_goal", 6000)

    fun setStepBaselineDate(profileId: Int, date: String) {
        sharedPreferences.edit().putString("step_baseline_date_$profileId", date).apply()
    }
    fun getStepBaselineDate(profileId: Int): String = sharedPreferences.getString("step_baseline_date_$profileId", "") ?: ""

    fun setStepBaseline(profileId: Int, baseline: Int) {
        sharedPreferences.edit().putInt("step_baseline_$profileId", baseline).apply()
    }
    fun getStepBaseline(profileId: Int): Int = sharedPreferences.getInt("step_baseline_$profileId", -1)

    fun setSelectedSosCountry(country: String) {
        sharedPreferences.edit().putString("selected_sos_country", country).apply()
    }
    fun getSelectedSosCountry(): String {
        return sharedPreferences.getString("selected_sos_country", "") ?: ""
    }

    fun clearAllData(context: Context, onComplete: () -> Unit = {}) {
        sharedPreferences.edit().clear().apply()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppDatabase.getDatabase(context).clearAllTables()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    private fun getUidPrefix(): String {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
        return "u_${uid}_"
    }

    fun isAlertChecksEnabled(): Boolean {
        return sharedPreferences.getBoolean("${getUidPrefix()}alert_checks_enabled", true)
    }

    fun setAlertChecksEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("${getUidPrefix()}alert_checks_enabled", enabled).apply()
    }

    fun getLastCheckedTimestamp(patientId: String): Long {
        return sharedPreferences.getLong("${getUidPrefix()}last_checked_ts_$patientId", 0L)
    }

    fun setLastCheckedTimestamp(patientId: String, timestamp: Long) {
        sharedPreferences.edit().putLong("${getUidPrefix()}last_checked_ts_$patientId", timestamp).apply()
    }

    fun isAlreadyNotified(eventId: String): Boolean {
        return sharedPreferences.getBoolean("${getUidPrefix()}notified_$eventId", false)
    }

    fun markNotified(eventId: String) {
        sharedPreferences.edit().putBoolean("${getUidPrefix()}notified_$eventId", true).apply()
    }
}
