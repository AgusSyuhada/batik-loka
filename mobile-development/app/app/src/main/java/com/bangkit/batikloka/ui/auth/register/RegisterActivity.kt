package com.bangkit.batikloka.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.auth.codeverification.VerificationActivity
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.viewmodel.AppViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager

class RegisterActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var ivShowPassword: ImageView
    private lateinit var ivShowConfirmPassword: ImageView
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnRegisterGoogle: Button
    private lateinit var tvLogin: TextView
    private lateinit var viewModel: RegisterViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        preferencesManager = PreferencesManager(this)
        viewModel = ViewModelProvider(
            this,
            AppViewModelFactory(preferencesManager)
        )[RegisterViewModel::class.java]

        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        ivShowPassword = findViewById(R.id.ivShowPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        ivShowConfirmPassword = findViewById(R.id.ivShowConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnRegisterGoogle = findViewById(R.id.btnRegisterGoogle)
        tvLogin = findViewById(R.id.tvLogin)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        ivShowPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowPassword.setImageResource(R.drawable.ic_visibility)
            } else {
                etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowPassword.setImageResource(R.drawable.ic_visibility_off)
            }
            etPassword.setSelection(etPassword.text.length)
        }

        ivShowConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            if (isConfirmPasswordVisible) {
                etConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowConfirmPassword.setImageResource(R.drawable.ic_visibility)
            } else {
                etConfirmPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowConfirmPassword.setImageResource(R.drawable.ic_visibility_off)
            }
            etConfirmPassword.setSelection(etConfirmPassword.text.length)
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (viewModel.validateInput(name, email, password, confirmPassword)) {
                performRegister(name, email, password)
            } else {
                showValidationErrors(name, email, password, confirmPassword)
            }
        }

        btnRegisterGoogle.setOnClickListener {
            viewModel.performGoogleRegister()
            Toast.makeText(this, getString(R.string.login_google_clicked), Toast.LENGTH_SHORT)
                .show()
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showValidationErrors(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
    ) {
        if (name.isEmpty()) {
            etName.error = getString(R.string.error_name_empty)
        } else if (name.length < 3) {
            etName.error = getString(R.string.error_name_length)
        }

        if (email.isEmpty()) {
            etEmail.error = getString(R.string.error_email_empty)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = getString(R.string.error_email_invalid)
        }

        if (password.isEmpty()) {
            etPassword.error = getString(R.string.error_password_empty)
        } else if (password.length < 6) {
            etPassword.error = getString(R.string.error_password_length)
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = getString(R.string.error_confirm_password_empty)
        } else if (password != confirmPassword) {
            etConfirmPassword.error = getString(R.string.error_password_mismatch)
        }
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
            val intent = Intent(this, VerificationActivity::class.java)
            intent.putExtra("action", "register")
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

    private fun performRegister(name: String, email: String, password: String) {
        showCustomAlertDialog(getString(R.string.registration_successful))

        preferencesManager.saveUserEmail(email)
    }
}