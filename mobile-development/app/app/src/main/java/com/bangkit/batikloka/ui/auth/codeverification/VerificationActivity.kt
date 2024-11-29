package com.bangkit.batikloka.ui.auth.codeverification

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.auth.createnewpassword.CreateNewPasswordActivity
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
            AppViewModelFactory(preferencesManager)
        )[VerificationViewModel::class.java]

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

    private fun confirmOtp(otp: String) {
        val confirmationMessage = viewModel.confirmOtp(otp)
        showCustomAlertDialog(confirmationMessage)

        val preferencesManager = PreferencesManager(this)
        preferencesManager.setUserRegistered()
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
            Log.d("VerificationActivity", "Action received: $action")
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