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
import androidx.lifecycle.lifecycleScope
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.local.database.AppDatabase
import com.bangkit.batikloka.ui.auth.codeverification.VerificationActivity
import com.bangkit.batikloka.ui.viewmodel.AppViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager
import kotlinx.coroutines.launch

class EmailVerificationActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var btnVerifyEmail: Button
    private lateinit var viewModel: EmailVerificationViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        preferencesManager = PreferencesManager(this)
        database = AppDatabase.getDatabase(this)

        viewModel = ViewModelProvider(
            this,
            AppViewModelFactory(this, preferencesManager, database)
        )[EmailVerificationViewModel::class.java]

        etEmail = findViewById(R.id.etEmail)
        btnVerifyEmail = findViewById(R.id.btnVerifyEmail)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnVerifyEmail.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (viewModel.validateEmail(email)) {
                lifecycleScope.launch {
                    val isEmailExists = viewModel.isEmailExists(email)
                    if (isEmailExists) {
                        sendVerificationEmail(email)
                    } else {
                        etEmail.error = "Email not registered"
                    }
                }
            } else {
                etEmail.error = "Invalid email format"
            }
        }
    }

    private fun sendVerificationEmail(email: String) {
        preferencesManager.setResetPasswordStatus(true, email)
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