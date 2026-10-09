package com.srcreation.smarthelp

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.srcreation.smarthelp.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        
        // Auto-skip login if already logged in
        if (authRepo.isUserLoggedIn()) {
            setLoading(true)
            checkRoleAndNavigate()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)
            lifecycleScope.launch {
                val result = authRepo.login(email, pass)
                if (result.isSuccess) {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        authRepo.updateFcmToken(token)
                    }
                    checkRoleAndNavigate()
                } else {
                    setLoading(false)
                    Toast.makeText(this@LoginActivity, "Login Failed: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            val emailInput = binding.etEmail.text.toString().trim()
            showResetPasswordDialog(emailInput)
        }

        binding.tvToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun showResetPasswordDialog(initialEmail: String) {
        val input = TextInputEditText(this).apply {
            hint = "Email Address"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setText(initialEmail)
        }
        val container = FrameLayout(this).apply {
            val padding = (20 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding / 2, padding, 0)
            addView(input)
        }

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("Enter your registered email address to receive a password reset link:")
            .setView(container)
            .setPositiveButton("Send Reset Link") { dialog, _ ->
                val email = input.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                setLoading(true)
                lifecycleScope.launch {
                    val result = authRepo.sendPasswordResetEmail(email)
                    setLoading(false)
                    if (result.isSuccess) {
                        Toast.makeText(this@LoginActivity, "Password reset email sent! Please check your inbox.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@LoginActivity, "Reset Failed: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun checkRoleAndNavigate() {
        lifecycleScope.launch {
            val role = try {
                authRepo.getRole()
            } catch (e: Exception) {
                Log.e("Caregiver", "Error reading role in LoginActivity", e)
                null
            }

            if (role.isNullOrBlank()) {
                Log.e("Caregiver", "Role reading failed or is null in LoginActivity")
                FirebaseAuth.getInstance().signOut()
                setLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    "Could not load your account, please log in again",
                    Toast.LENGTH_LONG
                ).show()
                return@launch
            }

            val targetActivity = if (role.equals("caregiver", ignoreCase = true)) {
                CaregiverDashboardActivity::class.java
            } else {
                MainActivity::class.java
            }
            val intent = Intent(this@LoginActivity, targetActivity).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
    }
}
