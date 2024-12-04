package com.bangkit.batikloka.ui.auth.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.batikloka.data.local.database.AppDatabase
import com.bangkit.batikloka.data.local.entity.UserEntity
import com.bangkit.batikloka.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class RegisterViewModel(
    private val preferencesManager: PreferencesManager,
    private val database: AppDatabase,
) : ViewModel() {

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun isEmailExists(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            database.userDao().checkUserExists(email) > 0
        }
    }

    fun registerUser(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val emailExists = withContext(Dispatchers.IO) {
                    isEmailExists(email)
                }

                if (emailExists) {
                    onError("Email sudah terdaftar")
                    return@launch
                }

                val hashedPassword = hashPassword(password)

                val newUser = UserEntity(
                    name = name,
                    email = email,
                    password = hashedPassword
                )

                val userId = withContext(Dispatchers.IO) {
                    database.userDao().insertUser(newUser)
                }

                preferencesManager.saveUserEmail(email)
                preferencesManager.saveUserName(name)
                preferencesManager.saveRegistrationStep("email_registered")

                onSuccess()
            } catch (e: Exception) {
                onError("Gagal mendaftar: ${e.message}")
            }
        }
    }

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
}