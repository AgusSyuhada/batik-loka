package com.bangkit.batikloka.ui.auth.codeverification

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.auth.createnewpassword.CreateNewPasswordActivity
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.auth.register.RegisterActivity
import com.bangkit.batikloka.ui.auth.startprofile.StartProfileActivity
import com.bangkit.batikloka.ui.viewmodel.AppViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager

class VerificationActivity : AppCompatActivity() {
    private lateinit var etVerification: EditText
    private lateinit var btnVerifyAccount: Button
    private lateinit var viewModel: VerificationViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        preferencesManager = PreferencesManager(this)
        viewModel = ViewModelProvider(
            this,
            AppViewModelFactory(this, preferencesManager)
        )[VerificationViewModel::class.java]

        checkRegistrationValidity()

        etVerification = findViewById(R.id.etVerification)
        btnVerifyAccount = findViewById(R.id.btnVerifyAccount)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnVerifyAccount.setOnClickListener {
            val otp = etVerification.text.toString().trim()

            if (!viewModel.validateOtp(otp)) {
                if (otp.isEmpty()) {
                    etVerification.error = getString(R.string.error_otp_empty)
                } else {
                    etVerification.error = getString(R.string.error_otp_length)
                }
            } else {
                confirmOtp(otp)
            }
        }
    }

    private fun checkRegistrationValidity() {
        val registrationStep = preferencesManager.getRegistrationStep()
        val action = intent.getStringExtra("action")

        when (action) {
            "register" -> {
                if (registrationStep == null || registrationStep != "email_registered") {
                    Toast.makeText(this, "Invalid registration process", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, RegisterActivity::class.java))
                    finish()
                    return
                }
            }

            "forgot_password" -> {
                if (!preferencesManager.isResetPassword()) {
                    Toast.makeText(this, "Invalid password reset process", Toast.LENGTH_SHORT)
                        .show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    return
                }
            }
        }
    }

    private fun confirmOtp(otp: String) {
        val action = intent.getStringExtra("action")
        if (action == "forgot_password") {
            preferencesManager.setResetPasswordStatus(false)
        } else {
            preferencesManager.saveRegistrationStep("otp_verified")
        }

        val confirmationMessage = viewModel.confirmOtp(otp)
        showCustomAlertDialog(confirmationMessage)
    }

    private fun showCustomAlertDialog(title: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_checkmark, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.dialog_title)
        titleTextView.text = title

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
            dialog.setCanceledOnTouchOutside(true)
        }

        dialog.setOnDismissListener {
            val action = intent.getStringExtra("action")
            val intent = when (action) {
                "register" -> Intent(this, StartProfileActivity::class.java)
                "forgot_password" -> Intent(this, CreateNewPasswordActivity::class.java)
                else -> Intent(this, StartProfileActivity::class.java) // Default
            }
            startActivity(intent)
            finish()
        }

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 3000)
    }
}
