package com.bangkit.batikloka.ui.auth.createnewpassword

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.auth.login.LoginActivity
import com.bangkit.batikloka.ui.viewmodel.AppViewModelFactory
import com.bangkit.batikloka.utils.PreferencesManager

class CreateNewPasswordActivity : AppCompatActivity() {
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmNewPassword: EditText
    private lateinit var btnCreateNewPassword: Button
    private lateinit var ivShowNewPassword: ImageView
    private lateinit var ivShowConfirmNewPassword: ImageView
    private var isNewPasswordVisible = false
    private var isConfirmNewPasswordVisible = false
    private lateinit var viewModel: CreateNewPasswordViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_password)

        viewModel = ViewModelProvider(
            this,
            AppViewModelFactory(this, preferencesManager)
        )[CreateNewPasswordViewModel::class.java]

        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)
        btnCreateNewPassword = findViewById(R.id.btnCreateNewPassword)
        ivShowNewPassword = findViewById(R.id.ivShowNewPassword)
        ivShowConfirmNewPassword = findViewById(R.id.ivShowConfirmNewPassword)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        ivShowNewPassword.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            if (isNewPasswordVisible) {
                etNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowNewPassword.setImageResource(R.drawable.ic_visibility)
            } else {
                etNewPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowNewPassword.setImageResource(R.drawable.ic_visibility_off)
            }
            etNewPassword.setSelection(etNewPassword.text.length)
        }

        ivShowConfirmNewPassword.setOnClickListener {
            isConfirmNewPasswordVisible = !isConfirmNewPasswordVisible
            if (isConfirmNewPasswordVisible) {
                etConfirmNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivShowConfirmNewPassword.setImageResource(R.drawable.ic_visibility)
            } else {
                etConfirmNewPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivShowConfirmNewPassword.setImageResource(R.drawable.ic_visibility_off)
            }
            etConfirmNewPassword.setSelection(etConfirmNewPassword.text.length)
        }

        btnCreateNewPassword.setOnClickListener {
            val newPassword = etNewPassword.text.toString().trim()
            val confirmNewPassword = etConfirmNewPassword.text.toString().trim()

            if (viewModel.validateNewPassword(newPassword, confirmNewPassword)) {
                saveNewPassword(newPassword)
            }
        }
    }

    private fun saveNewPassword(newPassword: String) {
        showCustomAlertDialog(viewModel.saveNewPassword(newPassword))
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
            startActivity(Intent(this, LoginActivity::class.java))
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