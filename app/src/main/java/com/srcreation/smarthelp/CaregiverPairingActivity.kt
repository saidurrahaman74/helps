package com.srcreation.smarthelp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity

class CaregiverPairingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "Family Sync feature has been removed.", Toast.LENGTH_SHORT).show()
        finish()
    }
}
