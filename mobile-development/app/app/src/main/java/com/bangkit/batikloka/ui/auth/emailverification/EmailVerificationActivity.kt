package com.bangkit.batikloka.ui.auth.emailverification

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.auth.codeverification.VerificationActivity
import com.bangkit.batikloka.ui.viewmodel.AppViewModelFactory

class EmailVerificationActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var btnVerifyEmail: Button
    private lateinit var viewModel: EmailVerificationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        viewModel = ViewModelProvider(
            this,
            AppViewModelFactory()
        )[EmailVerificationViewModel::class.java]

        etEmail = findViewById(R.id.etEmail)
        btnVerifyEmail = findViewById(R.id.btnVerifyEmail)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnVerifyEmail.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (viewModel.validateEmail(email)) {
                sendVerificationEmail(email)
            }
        }
    }

    private fun sendVerificationEmail(email: String) {
        showCustomAlertDialog(viewModel.sendVerificationEmail(email))
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
            startActivity(Intent(this, VerificationActivity::class.java).apply {
                putExtra("action", "forgot_password")
                putExtra("email", etEmail.text.toString().trim())
            })
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