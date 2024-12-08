package com.bangkit.batikloka.ui.auth.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.data.repository.AuthRepository
import com.bangkit.batikloka.ui.auth.codeverification.VerificationViewModel
import com.bangkit.batikloka.ui.auth.createnewpassword.CreateNewPasswordViewModel
import com.bangkit.batikloka.ui.auth.emailverification.EmailVerificationViewModel
import com.bangkit.batikloka.ui.auth.login.LoginViewModel
import com.bangkit.batikloka.ui.auth.register.RegisterViewModel

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(VerificationViewModel::class.java) -> {
                VerificationViewModel(authRepository, context) as T
            }

            modelClass.isAssignableFrom(CreateNewPasswordViewModel::class.java) -> {
                CreateNewPasswordViewModel(authRepository, context) as T
            }

            modelClass.isAssignableFrom(EmailVerificationViewModel::class.java) -> {
                EmailVerificationViewModel(authRepository, context) as T
            }

            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(authRepository, context) as T
            }

            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(authRepository, context) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}