package com.bangkit.batikloka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.ui.auth.codeverification.VerificationViewModel
import com.bangkit.batikloka.ui.auth.createnewpassword.CreateNewPasswordViewModel
import com.bangkit.batikloka.ui.auth.emailverification.EmailVerificationViewModel
import com.bangkit.batikloka.ui.auth.login.LoginViewModel
import com.bangkit.batikloka.ui.auth.register.RegisterViewModel
import com.bangkit.batikloka.ui.auth.startprofile.StartProfileViewModel
import com.bangkit.batikloka.ui.tour.TourViewModel
import com.bangkit.batikloka.utils.PreferencesManager

@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(
    private val preferencesManager: PreferencesManager? = null,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(VerificationViewModel::class.java) -> {
                VerificationViewModel(preferencesManager!!) as T
            }

            modelClass.isAssignableFrom(CreateNewPasswordViewModel::class.java) -> {
                CreateNewPasswordViewModel() as T
            }

            modelClass.isAssignableFrom(EmailVerificationViewModel::class.java) -> {
                EmailVerificationViewModel() as T
            }

            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(preferencesManager!!) as T
            }

            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(preferencesManager!!) as T
            }

            modelClass.isAssignableFrom(StartProfileViewModel::class.java) -> {
                StartProfileViewModel() as T
            }

            modelClass.isAssignableFrom(TourViewModel::class.java) -> {
                TourViewModel() as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}