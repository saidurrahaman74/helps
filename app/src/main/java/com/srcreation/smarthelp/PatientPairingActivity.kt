package com.srcreation.smarthelp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.srcreation.smarthelp.databinding.ActivityPatientPairingBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class PatientPairingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPatientPairingBinding
    private val pairingRepo = PairingRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in before starting any coroutine
        if (FirebaseAuth.getInstance().currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityPatientPairingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnGenerateCode.setOnClickListener {
            binding.btnGenerateCode.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    val code = pairingRepo.createPairingCode()
                    binding.tvPairingCode.text = code
                    binding.tvPairingCode.visibility = View.VISIBLE
                    binding.tvExpiryNotice.visibility = View.VISIBLE
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val errorMsg = e.localizedMessage ?: "Failed to generate pairing code"
                    Toast.makeText(this@PatientPairingActivity, errorMsg, Toast.LENGTH_LONG).show()
                } finally {
                    binding.btnGenerateCode.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
}
