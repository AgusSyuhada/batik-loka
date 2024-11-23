package com.bangkit.batikloka.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.batikloka.R

class VerificationActivity : AppCompatActivity() {
    private lateinit var etVerfication: EditText
    private lateinit var btnVerifyAccount: Button
    private lateinit var ivShowOtp: ImageView // Jika Anda ingin menambahkan fitur untuk menampilkan OTP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        // Inisialisasi View
        etVerfication = findViewById(R.id.etVerification)
        btnVerifyAccount = findViewById(R.id.btnVerifyAccount)
//        ivShowOtp = findViewById(R.id.ivShowOtp)

        // Setup Listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnVerifyAccount.setOnClickListener {
            val otp = etVerfication.text.toString().trim()

            // Validasi input
            if (otp.isEmpty()) {
                etVerfication.error = "OTP cannot be empty"
            } else {
                // Implementasi logika konfirmasi OTP
                confirmOtp(otp)
            }
        }
    }

    private fun confirmOtp(otp: String) {
        // Implementasi logika konfirmasi OTP
        // Misalnya, panggil API untuk memverifikasi OTP
        Toast.makeText(this, "OTP confirmed: $otp", Toast.LENGTH_SHORT).show()
        // Pindah ke MainActivity atau halaman lain setelah konfirmasi berhasil
         startActivity(Intent(this, StartProfileActivity::class.java))
        // finish()
    }
}