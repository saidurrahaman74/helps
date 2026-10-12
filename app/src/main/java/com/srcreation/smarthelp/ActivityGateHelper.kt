package com.srcreation.smarthelp

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ActivityGateHelper {

    fun checkGateAndRender(
        activity: ComponentActivity,
        onShowContent: () -> Unit
    ) {
        val currentUid = FirebaseAuth.getInstance().currentUser?.uid

        // 1. Logged-out access check: if no user is logged in, redirect immediately to LoginActivity
        if (currentUid.isNullOrBlank()) {
            val intent = Intent(activity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            activity.startActivity(intent)
            activity.finish()
            return
        }

        // 2. Evaluate ownership decision off the main thread BEFORE setContent
        activity.lifecycleScope.launch(Dispatchers.IO) {
            val isBlocked = LocalDataOwnership.isOwnershipBlocked(activity)
            withContext(Dispatchers.Main) {
                if (activity.isFinishing || activity.isDestroyed) return@withContext
                if (isBlocked) {
                    val intent = Intent(activity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("TARGET_ACTIVITY", activity::class.java.name)
                    }
                    activity.startActivity(intent)
                    activity.finish()
                } else {
                    // Allowed: render UI content now!
                    onShowContent()
                }
            }
        }
    }
}
