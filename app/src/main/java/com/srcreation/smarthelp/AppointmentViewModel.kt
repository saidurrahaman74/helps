package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.AppointmentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AppointmentViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.appointmentDao()
    private val pref = PreferenceManager(application)

    val allAppointments: Flow<List<AppointmentEntity>> = dao.getAllAppointments(pref.getActiveProfile())

    fun addAppointment(
        doctorName: String, 
        specialty: String, 
        chamber: String, 
        date: String, 
        time: String,
        note: String? = null
    ) {
        viewModelScope.launch {
            dao.insertAppointment(
                AppointmentEntity(
                    profileId = pref.getActiveProfile(),
                    doctorName = doctorName,
                    specialty = specialty,
                    chamberName = chamber,
                    appointmentDate = date,
                    appointmentTime = time,
                    note = note
                )
            )

            // 🌐 Caregiver Sync
            CaregiverSyncRepository.syncVitals(
                context = getApplication(),
                type = "APPOINTMENT",
                title = "ডাক্তারের অ্যাপয়েন্টমেন্ট ($doctorName)",
                valueText = "$date at $time ($chamber)",
                statusTag = "Scheduled"
            )
        }
    }

    fun updateAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            dao.updateAppointment(appointment)
        }
    }

    fun deleteAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            dao.deleteAppointment(appointment)
        }
    }
}
