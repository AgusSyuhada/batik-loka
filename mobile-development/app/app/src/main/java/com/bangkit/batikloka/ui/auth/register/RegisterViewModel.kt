package com.bangkit.batikloka.ui.auth.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.bangkit.batikloka.utils.PreferencesManager

class RegisterViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): Boolean {
        return when {
            name.isEmpty() -> false
            name.length < 3 -> false
            email.isEmpty() -> false
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> false
            password.isEmpty() -> false
            password.length < 6 -> false
            confirmPassword.isEmpty() -> false
            password != confirmPassword -> false
            else -> true
        }
    }

    fun performRegister(email: String) {
        preferencesManager.saveUserEmail(email)
    }

    fun performGoogleRegister() {
        // Logika untuk melakukan pendaftaran dengan Google
    }
}