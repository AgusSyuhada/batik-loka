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
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.auth.VerificationActivity
import com.bangkit.batikloka.ui.auth.login.LoginActivity

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inisialisasi View
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        ivShowPassword = findViewById(R.id.ivShowPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        ivShowConfirmPassword = findViewById(R.id.ivShowConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnRegisterGoogle = findViewById(R.id.btnRegisterGoogle)
        tvLogin = findViewById(R.id.tvLogin)

        // Setup Listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Show Password
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

        // Show Confirm Password
        ivShowConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            if (isConfirmPasswordVisible) {
                etConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowConfirmPassword.setImageResource(R.drawable.ic_visibility) // Ganti dengan ikon mata terbuka
            } else {
                etConfirmPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowConfirmPassword.setImageResource(R.drawable.ic_visibility_off) // Ganti dengan ikon mata tertutup
            }
            etConfirmPassword.setSelection(etConfirmPassword.text.length) // Memindahkan kursor ke akhir
        }

        // Register Button
        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Validasi input
            if (validateInput(name, email, password, confirmPassword)) {
                performRegister(name, email, password)
            }
        }

        // Google Register
        btnRegisterGoogle.setOnClickListener {
            // Implementasi register dengan Google
            performGoogleRegister()
        }

        // Login Navigation
        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            name.isEmpty() -> {
                etName.error = "Name cannot be empty"
                false
            }
            name.length < 3 -> {
                etName.error = "Name must be at least 3 characters"
                false
            }
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
            password.length < 6 -> {
                etPassword.error = "Password must be at least 6 characters"
                false
            }
            confirmPassword.isEmpty() -> {
                etConfirmPassword.error = "Confirm password cannot be empty"
                false
            }
            password != confirmPassword -> {
                etConfirmPassword.error = "Passwords do not match"
                false
            }
            else -> true
        }
    }

    private fun showCustomAlertDialog(title: String) {
        // Inflate layout kustom
        val dialogView = layoutInflater.inflate(R.layout.dialog_checkmark, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.dialog_title)
        titleTextView.text = title

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true) // Memungkinkan dialog ditutup dengan mengklik di luar
            .create()

        dialog.setOnShowListener {
            // Mengatur latar belakang dialog dengan drawable kustom
            dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)

            // Menangani klik di luar dialog untuk menutup dialog
            dialog.setCanceledOnTouchOutside(true)
        }

        dialog.setOnDismissListener {
            // Pindah ke VerificationActivity setelah dialog ditutup
            val intent = Intent(this, VerificationActivity::class.java)
            intent.putExtra("action", "register") // Menandakan bahwa ini adalah pendaftaran
            startActivity(intent)
            finish()
        }

        dialog.show()

        // Menambahkan delay sebelum dialog ditutup
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss() // Menutup dialog setelah delay
            }
        }, 3000) // Delay selama 3000 ms (3 detik)
    }

    private fun performRegister(name: String, email: String, password: String) {
        // Panggil alert dialog untuk menginformasikan bahwa OTP telah dikirim
        showCustomAlertDialog("OTP has been sent to your email!")
    }

    private fun performGoogleRegister() {
        // Implementasi register dengan Google
        Toast.makeText(this, "Google register clicked", Toast.LENGTH_SHORT).show()
        // Tambahkan logika untuk register dengan Google
    }
}