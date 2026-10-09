package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.srcreation.smarthelp.db.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class UnifiedReportViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val pref = PreferenceManager(application)
    private val activeProfileId = pref.getActiveProfile()
    
    val bpRecords = db.bpDao().getAllRecords(activeProfileId)
    val insulinRecords = db.insulinDao().getAllRecords(activeProfileId)
    val stepRecords = db.stepDao().getAllRecords(activeProfileId)
    val medicineRecords = db.medicineDao().getAllMedicines(activeProfileId)
    val bmiRecords = db.bmiDao().getAllBmiRecords(activeProfileId)
    val medicalId = db.medicalIdDao().getMedicalId(activeProfileId)
    val periodLogs = db.periodDao().getAllLogs(activeProfileId)

    // Combined status for report generation
    val isReady: Flow<Boolean> = combine(
        bpRecords, insulinRecords, stepRecords, medicineRecords, bmiRecords
    ) { bp, insulin, steps, med, bmi ->
        bp.isNotEmpty() || insulin.isNotEmpty() || steps.isNotEmpty() || med.isNotEmpty() || bmi.isNotEmpty()
    }
}
