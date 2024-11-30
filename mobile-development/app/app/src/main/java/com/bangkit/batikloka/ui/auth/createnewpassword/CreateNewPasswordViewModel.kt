package com.bangkit.batikloka.ui.auth.createnewpassword

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import com.bangkit.batikloka.R

@SuppressLint("StaticFieldLeak")
class CreateNewPasswordViewModel(
    private val context: Context,
) : ViewModel() {

    fun validateNewPassword(newPassword: String, confirmNewPassword: String): Boolean {
        return when {
            newPassword.isEmpty() -> {
                false
            }

            newPassword.length < 6 -> {
                false
            }

            confirmNewPassword.isEmpty() -> {
                false
            }

            newPassword != confirmNewPassword -> {
                false
            }

            else -> true
        }
    }

    fun saveNewPassword(newPassword: String): String {
        return context.getString(R.string.new_password_created_successfully)
    }
}