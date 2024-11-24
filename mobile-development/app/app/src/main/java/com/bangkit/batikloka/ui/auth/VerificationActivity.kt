package com.bangkit.batikloka.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.auth.register.StartProfileActivity

class VerificationActivity : AppCompatActivity() {
    private lateinit var etVerification: EditText
    private lateinit var btnVerifyAccount: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        // Inisialisasi View
        etVerification = findViewById(R.id.etVerification)
        btnVerifyAccount = findViewById(R.id.btnVerifyAccount)

        // Setup Listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnVerifyAccount.setOnClickListener {
            val otp = etVerification.text.toString().trim()

            // Validasi input
            if (otp.isEmpty()) {
                etVerification.error = "OTP cannot be empty"
            } else if (otp.length != 6) { // Memastikan OTP tepat 6 karakter
                etVerification.error = "OTP must be 6 digits"
            } else {
                // Implementasi logika konfirmasi OTP
                confirmOtp(otp)
            }
        }
    }

    private fun confirmOtp(otp: String) {
        // Tampilkan dialog konfirmasi
        showCustomAlertDialog("OTP confirmed successfully!")
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
            val action = intent.getStringExtra("action")
            Log.d("VerificationActivity", "Action received: $action") // Log untuk debugging
            val intent = when (action) {
                "register" -> Intent(this, StartProfileActivity::class.java)
                "forgot_password" -> Intent(this, CreateNewPasswordActivity::class.java)
                else -> Intent(this, StartProfileActivity::class.java) // Default
            }
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
}