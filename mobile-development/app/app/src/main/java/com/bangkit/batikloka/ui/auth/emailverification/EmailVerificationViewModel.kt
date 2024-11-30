package com.bangkit.batikloka.ui.auth.emailverification

import android.annotation.SuppressLint
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel

@SuppressLint("StaticFieldLeak")
class EmailVerificationViewModel(
    private val context: Context,
) : ViewModel() {
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