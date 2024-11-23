package com.bangkit.batikloka.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bangkit.batikloka.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Tampilkan dialog setelah beberapa detik
        showCustomAlertDialog("Welcome to BatikLoka")
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

        dialog.show()

        // Menambahkan delay sebelum dialog ditutup
        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss() // Menutup dialog setelah delay
            }
        }, 3000) // Delay selama 3000 ms (3 detik)
    }
}