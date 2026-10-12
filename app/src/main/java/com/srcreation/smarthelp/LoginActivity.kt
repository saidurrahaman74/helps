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
    private var isBangla = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Language Toggle Listener
        binding.toggleLanguage.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isBangla = (checkedId == R.id.btnLangBn)
                updateLanguageUi()
            }
        }
        binding.toggleLanguage.check(R.id.btnLangEn)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
        
        // Auto-skip login if already logged in
        if (authRepo.isUserLoggedIn()) {
            setLoading(true)
            BackgroundCheckScheduler.scheduleWorkers(this)
            checkRoleAndNavigate()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                val msg = if (isBangla) "অনুগ্রহ করে সব তথ্য দিন" else "Please fill in all fields"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)
            lifecycleScope.launch {
                val result = authRepo.login(email, pass)
                if (result.isSuccess) {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        authRepo.updateFcmToken(token)
                    }
                    BackgroundCheckScheduler.scheduleWorkers(this@LoginActivity)
                    checkRoleAndNavigate()
                } else {
                    setLoading(false)
                    val failMsg = if (isBangla) "লগইন ব্যর্থ হয়েছে: " else "Login Failed: "
                    Toast.makeText(this@LoginActivity, failMsg + result.exceptionOrNull()?.localizedMessage, Toast.LENGTH_LONG).show()
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

    private fun updateLanguageUi() {
        if (isBangla) {
            binding.tvTitle.text = "স্বাগতম"
            binding.tvSubtitle.text = "আপনার হেলথ কমান্ড সেন্টারে লগইন করুন"
            binding.tilEmail.hint = "ইমেইল ঠিকানা"
            binding.tilPassword.hint = "পাসওয়ার্ড"
            binding.tvForgotPassword.text = "পাসওয়ার্ড ভুলে গেছেন?"
            binding.btnLogin.text = "লগইন করুন"
            binding.tvToSignup.text = "অ্যাকাউন্ট নেই? সাইন আপ করুন"
        } else {
            binding.tvTitle.text = "Welcome Back"
            binding.tvSubtitle.text = "Sign in to your health command center"
            binding.tilEmail.hint = "Email Address"
            binding.tilPassword.hint = "Password"
            binding.tvForgotPassword.text = "Forgot Password?"
            binding.btnLogin.text = "Secure Login"
            binding.tvToSignup.text = "Don't have an account? Sign Up"
        }
    }

    private fun showResetPasswordDialog(initialEmail: String) {
        val input = TextInputEditText(this).apply {
            hint = if (isBangla) "ইমেইল ঠিকানা" else "Email Address"
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setText(initialEmail)
        }
        val container = FrameLayout(this).apply {
            val padding = (20 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding / 2, padding, 0)
            addView(input)
        }

        val title = if (isBangla) "পাসওয়ার্ড রিকভারি" else "Reset Password"
        val message = if (isBangla) "পাসওয়ার্ড রিসেট লিঙ্ক পেতে আপনার নিবন্ধিত ইমেইল ঠিকানা লিখুন:" else "Enter your registered email address to receive a password reset link:"
        val posBtn = if (isBangla) "রিসেট লিঙ্ক পাঠান" else "Send Reset Link"
        val negBtn = if (isBangla) "বাতিল" else "Cancel"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setView(container)
            .setPositiveButton(posBtn) { dialog, _ ->
                val email = input.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(this, if (isBangla) "দয়া করে আপনার ইমেইল লিখুন" else "Please enter your email address", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                setLoading(true)
                lifecycleScope.launch {
                    val result = authRepo.sendPasswordResetEmail(email)
                    setLoading(false)
                    if (result.isSuccess) {
                        Toast.makeText(this@LoginActivity, if (isBangla) "পাসওয়ার্ড রিসেট ইমেইল পাঠানো হয়েছে! ইনবক্স চেক করুন।" else "Password reset email sent! Please check your inbox.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@LoginActivity, (if (isBangla) "রিসেট ব্যর্থ হয়েছে: " else "Reset Failed: ") + result.exceptionOrNull()?.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(negBtn) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun checkRoleAndNavigate() {
        lifecycleScope.launch {
            val role = try {
                authRepo.getRole()
            } catch (e:Exception) {
                Log.e("Caregiver", "Error reading role in LoginActivity", e)
                null
            }

            val targetActivity = if (!role.isNullOrBlank() && role.equals("caregiver", ignoreCase = true)) {
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
