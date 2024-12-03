package com.bangkit.batikloka.ui.auth.createnewpassword

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.bangkit.batikloka.data.local.database.AppDatabase
import com.bangkit.batikloka.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("StaticFieldLeak")
class CreateNewPasswordViewModel(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val database: AppDatabase,
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

    suspend fun saveNewPassword(newPassword: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val email = preferencesManager.getResetPasswordEmail()
                if (email != null) {
                    val hashedPassword = hashPassword(newPassword)
                    database.userDao().updatePassword(email, hashedPassword)
                    preferencesManager.clearResetPasswordData()
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("CreateNewPasswordVM", "Error saving new password", e)
                false
            }
        }
    }

    private fun hashPassword(password: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}