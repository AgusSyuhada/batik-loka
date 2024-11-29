package com.bangkit.batikloka.ui.auth.createnewpassword

import androidx.lifecycle.ViewModel

class CreateNewPasswordViewModel : ViewModel() {

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
        return "New password created successfully!"
    }
}