package com.srcreation.smarthelp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth

class PatientInstructionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser == null) {
            val loginIntent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(loginIntent)
            finish()
            return
        }

        val targetIntent = Intent(this, PatientLinkHubActivity::class.java).apply {
            intent.extras?.let { putExtras(it) }
            putExtra("TAB", "INSTRUCTIONS")
        }
        startActivity(targetIntent)
        finish()
    }
}
