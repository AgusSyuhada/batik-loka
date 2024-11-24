package com.bangkit.batikloka.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.auth.login.LoginActivity

class CreateNewPasswordActivity : AppCompatActivity() {
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmNewPassword: EditText
    private lateinit var btnCreateNewPassword: Button
    private lateinit var ivShowNewPassword: ImageView
    private lateinit var ivShowConfirmNewPassword: ImageView
    private var isNewPasswordVisible = false
    private var isConfirmNewPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_password)

        // Inisialisasi View
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)
        btnCreateNewPassword = findViewById(R.id.btnCreateNewPassword)
        ivShowNewPassword = findViewById(R.id.ivShowNewPassword)
        ivShowConfirmNewPassword = findViewById(R.id.ivShowConfirmNewPassword)

        // Setup Listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        ivShowNewPassword.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            if (isNewPasswordVisible) {
                etNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowNewPassword.setImageResource(R.drawable.ic_visibility) // Ganti dengan ikon mata terbuka
            } else {
                etNewPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowNewPassword.setImageResource(R.drawable.ic_visibility_off) // Ganti dengan ikon mata tertutup
            }
            etNewPassword.setSelection(etNewPassword.text.length) // Memindahkan kursor ke akhir
        }

        ivShowConfirmNewPassword.setOnClickListener {
            isConfirmNewPasswordVisible = !isConfirmNewPasswordVisible
            if (isConfirmNewPasswordVisible) {
                etConfirmNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowConfirmNewPassword.setImageResource(R.drawable.ic_visibility) // Ganti dengan ikon mata terbuka
            } else {
                etConfirmNewPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowConfirmNewPassword.setImageResource(R.drawable.ic_visibility_off) // Ganti dengan ikon mata tertutup
            }
            etConfirmNewPassword.setSelection(etConfirmNewPassword.text.length) // Memindahkan kursor ke akhir
        }

        btnCreateNewPassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()
            val confirmNewPassword = etConfirmNewPassword.text.toString().trim()

            // Validasi input
            if (validateNewPassword(newPassword, confirmNewPassword)) {
                // Implementasi logika untuk menyimpan password baru
                saveNewPassword(newPassword)
            }
        }
    }

    private fun validateNewPassword(newPassword: String, confirmNewPassword: String): Boolean {
        return when {
            newPassword.isEmpty() -> {
                etNewPassword.error = "New password cannot be empty"
                false
            }
            newPassword.length < 6 -> {
                etNewPassword.error = "Password must be at least 6 characters"
                false
            }
            confirmNewPassword.isEmpty() -> {
                etConfirmNewPassword.error = "Confirm password cannot be empty"
                false
            }
            newPassword != confirmNewPassword -> {
                etConfirmNewPassword.error = "Passwords do not match"
                false
            }
            else -> true
        }
    }

    private fun saveNewPassword(newPassword: String) {
        // Tampilkan dialog bahwa password baru telah berhasil dibuat
        showCustomAlertDialog("New password created successfully!")
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
            // Pindah ke LoginActivity setelah dialog ditutup
            startActivity(Intent(this, LoginActivity::class.java))
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