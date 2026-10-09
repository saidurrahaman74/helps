package com.srcreation.smarthelp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.srcreation.smarthelp.databinding.ActivityLinkPatientBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LinkPatientActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLinkPatientBinding
    private val pairingRepo = PairingRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityLinkPatientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnLink.setOnClickListener {
            val code = binding.etCode.text.toString().trim()
            binding.tvError.visibility = View.GONE
            binding.tvError.text = ""

            if (code.length != 6) {
                val err = "Please enter a valid 6-digit code"
                binding.tvError.text = err
                binding.tvError.visibility = View.VISIBLE
                Toast.makeText(this, err, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            binding.btnLink.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            lifecycleScope.launch {
                try {
                    pairingRepo.linkWithCode(code)
                    Toast.makeText(this@LinkPatientActivity, "Successfully linked patient! 🎉", Toast.LENGTH_LONG).show()
                    finish()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    val errorMsg = e.localizedMessage ?: "Linking failed"
                    binding.tvError.text = errorMsg
                    binding.tvError.visibility = View.VISIBLE
                    Toast.makeText(this@LinkPatientActivity, errorMsg, Toast.LENGTH_LONG).show()
                } finally {
                    binding.btnLink.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
}
