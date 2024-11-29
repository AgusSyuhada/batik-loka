package com.bangkit.batikloka.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.auth.emailverification.EmailVerificationActivity
import com.bangkit.batikloka.ui.auth.register.RegisterActivity
import com.bangkit.batikloka.ui.user.UserActivity
import com.bangkit.batikloka.ui.viewmodel.AppViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnLoginGoogle: Button
    private lateinit var tvRegister: TextView
    private lateinit var ivShowPassword: ImageView
    private var isPasswordVisible = false
    private lateinit var tvForgotPassword: TextView
    private lateinit var viewModel: LoginViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        preferencesManager = PreferencesManager(this)

        viewModel = ViewModelProvider(
            this,
            AppViewModelFactory(preferencesManager)
        )[LoginViewModel::class.java]
        if (viewModel.isUserLoggedIn()) {
            startActivity(Intent(this, UserActivity::class.java))
            finish()
            return
        }

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle)
        tvRegister = findViewById(R.id.tvLogin)
        ivShowPassword = findViewById(R.id.ivShowPassword)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

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

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (viewModel.validateInput(email, password)) {
                performLogin(email)
            } else {
                showValidationErrors(email, password)
            }
        }

        btnLoginGoogle.setOnClickListener {
            viewModel.performGoogleLogin()
            Toast.makeText(this, "Google login clicked", Toast.LENGTH_SHORT).show()
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, EmailVerificationActivity::class.java))
        }
    }

    private fun showValidationErrors(email: String, password: String) {
        if (email.isEmpty()) {
            etEmail.error = getString(R.string.error_email_empty)
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = getString(R.string.error_email_invalid)
        }

        if (password.isEmpty()) {
            etPassword.error = getString(R.string.error_password_empty)
        }
    }

    private fun performLogin(email: String) {
        viewModel.performLogin(email)

        startActivity(Intent(this, UserActivity::class.java))
        finish()
    }
}