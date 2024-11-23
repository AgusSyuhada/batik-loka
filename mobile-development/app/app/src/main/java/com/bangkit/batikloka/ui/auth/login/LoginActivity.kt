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
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.auth.ForgotPasswordActivity
import com.bangkit.batikloka.ui.home.MainActivity
import com.bangkit.batikloka.ui.auth.register.RegisterActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnLoginGoogle: Button
    private lateinit var tvRegister: TextView
    private lateinit var ivShowPassword: ImageView
    private var isPasswordVisible = false
    private lateinit var tvForgotPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi View
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle)
        tvRegister = findViewById(R.id.tvLogin)
        ivShowPassword = findViewById(R.id.ivShowPassword)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)

        // Setup Listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        ivShowPassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowPassword.setImageResource(R.drawable.ic_visibility) // Ganti dengan ikon mata terbuka
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowPassword.setImageResource(R.drawable.ic_visibility_off) // Ganti dengan ikon mata tertutup
            }
            etPassword.setSelection(etPassword.text.length) // Memindahkan kursor ke akhir
        }

        // Login Button
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validasi input
            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

        // Google Login
        btnLoginGoogle.setOnClickListener {
            // Implementasi login dengan Google
            performGoogleLogin()
        }

        // Register Navigation
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Forgot Password
        tvForgotPassword.setOnClickListener {
            // Implementasi lupa password
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                etEmail.error = "Email cannot be empty"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etEmail.error = "Invalid email format"
                false
            }
            password.isEmpty() -> {
                etPassword.error = "Password cannot be empty"
                false
            }
            else -> true
        }
    }

    private fun performLogin(email: String, password: String) {
        // Implementasi login
        // Misalnya, panggil API atau validasi lokal
        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
        // Pindah ke activity berikutnya setelah login
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun performGoogleLogin() {
        // Implementasi login dengan Google
        Toast.makeText(this, "Google login clicked", Toast.LENGTH_SHORT).show()
        // Tambahkan logika untuk login dengan Google
    }
}