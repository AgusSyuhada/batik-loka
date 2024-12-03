package com.bangkit.batikloka.ui.auth.emailverification

import android.annotation.SuppressLint
import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.bangkit.batikloka.data.local.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("StaticFieldLeak")
class EmailVerificationViewModel(
    private val context: Context,
    private val database: AppDatabase,
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

    suspend fun isEmailExists(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            database.userDao().getUserByEmail(email) != null
        }
    }

    fun sendVerificationEmail(email: String): String {
        return "OTP has been sent to your email!"
    }
}