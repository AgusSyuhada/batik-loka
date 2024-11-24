package com.bangkit.batikloka.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.batikloka.R

class EmailVerificationActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var btnVerifyEmail: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_verification)

        // Inisialisasi View
        etEmail = findViewById(R.id.etEmail)
        btnVerifyEmail = findViewById(R.id.btnVerifyEmail)

        // Setup Click Listener
        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnVerifyEmail.setOnClickListener {
            val email = etEmail.text.toString().trim()

            // Validasi input
            if (validateEmail(email)) {
                // Kirim email verifikasi (misalnya, panggil API)
                sendVerificationEmail(email)
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                etEmail.error = "Email cannot be empty"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etEmail.error = "Invalid email format"
                false
            }
            else -> true
        }
    }

    private fun sendVerificationEmail(email: String) {
        // Tampilkan dialog bahwa OTP telah dikirim
        showCustomAlertDialog("OTP has been sent to your email!")
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
            startActivity(Intent(this, VerificationActivity::class.java).apply {
                putExtra("action", "forgot_password") // Menandakan bahwa ini adalah permintaan reset password
                putExtra("email", etEmail.text.toString().trim()) // Kirim email ke activity berikutnya
            })
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
}