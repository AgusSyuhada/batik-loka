package com.bangkit.batikloka.ui.auth.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.bangkit.batikloka.utils.PreferencesManager

class LoginViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                false
            }

            password.isEmpty() -> {
                false
            }

            else -> true
        }
    }

    fun performLogin(email: String) {
        // Simpan status login di SharedPreferences
        preferencesManager.saveUserEmail(email)
    }

    fun performGoogleLogin() {
        // Logika untuk login dengan Google
    }

    fun isUserLoggedIn(): Boolean {
        return preferencesManager.isUserLoggedIn()
    }
}