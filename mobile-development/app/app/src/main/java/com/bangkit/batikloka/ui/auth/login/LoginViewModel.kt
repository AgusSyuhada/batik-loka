package com.bangkit.batikloka.ui.auth.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.batikloka.data.local.database.AppDatabase
import com.bangkit.batikloka.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class LoginViewModel(
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

    fun loginUser(
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

                if (!emailExists) {
                    onError("Email tidak terdaftar")
                    return@launch
                }

                val hashedPassword = hashPassword(password)

                val user = withContext(Dispatchers.IO) {
                    database.userDao().getUserByEmail(email)
                }

                if (user == null) {
                    onError("Pengguna tidak ditemukan")
                    return@launch
                }

                if (user.password != hashedPassword) {
                    onError("Password salah")
                    return@launch
                }

                preferencesManager.saveUserEmail(email)
                preferencesManager.setUserLoggedIn(true)
                onSuccess()
            } catch (e: Exception) {
                onError("Terjadi kesalahan: ${e.message}")
            }
        }
    }

    fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> false
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> false
            password.isEmpty() -> false
            password.length < 6 -> false
            else -> true
        }
    }

    fun isUserLoggedIn(): Boolean {
        return preferencesManager.isUserLoggedIn()
    }
}