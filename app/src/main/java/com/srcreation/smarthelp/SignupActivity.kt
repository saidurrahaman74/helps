package com.srcreation.smarthelp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.srcreation.smarthelp.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val authRepo = AuthRepository()
    private var isBangla = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Language Toggle Listener
        binding.toggleLanguage.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isBangla = (checkedId == R.id.btnLangBn)
                updateLanguageUi()
            }
        }
        binding.toggleLanguage.check(R.id.btnLangEn)

        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                val msg = if (isBangla) "অনুগ্রহ করে সব তথ্য দিন" else "Please fill in all fields"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass.length < 6) {
                val msg = if (isBangla) "পাসওয়ার্ড অন্তত ৬ অক্ষরের হতে হবে" else "Password must be at least 6 characters"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)
            lifecycleScope.launch {
                val result = authRepo.signUp(email, pass, name, "user")
                setLoading(false)
                if (result.isSuccess) {
                    com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        authRepo.updateFcmToken(token)
                    }
                    val msg = if (isBangla) "অ্যাকাউন্ট সফলভাবে তৈরি হয়েছে!" else "Account created successfully!"
                    Toast.makeText(this@SignupActivity, msg, Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@SignupActivity, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
                    val exception = result.exceptionOrNull()
                    val isCollision = exception is com.google.firebase.auth.FirebaseAuthUserCollisionException ||
                            exception?.message?.contains("already exists", ignoreCase = true) == true ||
                            exception?.message?.contains("already in use", ignoreCase = true) == true

                    if (isCollision) {
                        val title = if (isBangla) "রেজিস্ট্রেশন বার্তা" else "Registration Notice"
                        val message = if (isBangla)
                            "যদি এই ইমেইলটি ব্যবহার করা সম্ভব হয়, তবে ইনবক্স চেক করুন অথবা লগইন করার চেষ্টা করুন।"
                        else
                            "If this email can be used, check your inbox or try logging in."

                        androidx.appcompat.app.AlertDialog.Builder(this@SignupActivity)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton(if (isBangla) "লগইন স্ক্রিনে যান" else "Try Logging In") { _, _ ->
                                finish()
                            }
                            .setNeutralButton(if (isBangla) "পাসওয়ার্ড রিসেট" else "Reset Password") { _, _ ->
                                setLoading(true)
                                lifecycleScope.launch {
                                    authRepo.sendPasswordResetEmail(email)
                                    setLoading(false)
                                    Toast.makeText(
                                        this@SignupActivity,
                                        if (isBangla) "যদি অ্যাকাউন্টটি নিবন্ধিত থাকে তবে রিসেট ইমেইল পাঠানো হয়েছে।" else "If the account exists, a reset email has been sent.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    finish()
                                }
                            }
                            .setNegativeButton(if (isBangla) "বাতিল" else "Cancel", null)
                            .show()
                    } else {
                        val msg = if (isBangla) "সাইন আপ ব্যর্থ হয়েছে: " else "Signup Failed: "
                        Toast.makeText(this@SignupActivity, msg + exception?.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.tvToLogin.setOnClickListener {
            finish()
        }
    }

    private fun updateLanguageUi() {
        if (isBangla) {
            binding.tvTitle.text = "অ্যাকাউন্ট তৈরি করুন"
            binding.tvSubtitle.text = "শুরু করতে সাইন আপ করুন"
            binding.tilName.hint = "সম্পূর্ণ নাম"
            binding.tilEmail.hint = "ইমেইল ঠিকানা"
            binding.tilPassword.hint = "পাসওয়ার্ড"
            binding.btnSignup.text = "সাইন আপ"
            binding.tvToLogin.text = "ইতিমধ্যে একটি অ্যাকাউন্ট আছে? লগইন করুন"
        } else {
            binding.tvTitle.text = "Create Account"
            binding.tvSubtitle.text = "Sign up to get started"
            binding.tilName.hint = "Full Name"
            binding.tilEmail.hint = "Email Address"
            binding.tilPassword.hint = "Password"
            binding.btnSignup.text = "Sign Up"
            binding.tvToLogin.text = "Already have an account? Login"
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSignup.isEnabled = !loading
    }
}
