package com.srcreation.smarthelp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.srcreation.smarthelp.db.AppDatabase
import com.srcreation.smarthelp.db.NotificationEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application, savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.notificationDao()
    private val firestore = FirebaseFirestore.getInstance()
    
    // Get profileId from intent
    private val profileId = savedStateHandle.get<Int>("PROFILE_ID") ?: 1

    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications(profileId)

    init {
        syncGlobalNotifications()
    }

    private fun syncGlobalNotifications() {
        viewModelScope.launch {
            try {
                firestore.collection("admin_notifications").get().addOnSuccessListener { snapshot ->
                    if (snapshot != null) {
                        viewModelScope.launch {
                            val localNotifications = allNotifications.first()
                            snapshot.documents.forEach { doc ->
                                val remoteId = doc.id
                                val title = doc.getString("title") ?: "Announcement"
                                val message = doc.getString("message") ?: ""
                                
                                // Only add if not already in local DB
                                if (localNotifications.none { it.remoteId == remoteId }) {
                                    dao.insertNotification(
                                        NotificationEntity(
                                            profileId = profileId,
                                            remoteId = remoteId,
                                            title = title,
                                            message = message
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteNotification(notification: NotificationEntity) {
        viewModelScope.launch {
            dao.deleteNotification(notification)
        }
    }

    fun deleteMultiple(ids: List<Int>) {
        viewModelScope.launch {
            dao.deleteMultiple(profileId, ids)
        }
    }

    fun addNotification(title: String, message: String) {
        viewModelScope.launch {
            dao.insertNotification(NotificationEntity(profileId = profileId, title = title, message = message))
        }
    }
}
