package com.bangkit.batikloka.ui.auth.emailverification

import android.util.Patterns
import androidx.lifecycle.ViewModel

class EmailVerificationViewModel : ViewModel() {

    fun validateEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                false
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                false
            }

            else -> true
        }
    }

    fun sendVerificationEmail(email: String): String {
        return "OTP has been sent to your email!"
    }
}