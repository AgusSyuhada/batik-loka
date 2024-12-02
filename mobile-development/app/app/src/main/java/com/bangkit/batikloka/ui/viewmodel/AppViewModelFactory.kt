package com.bangkit.batikloka.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.ui.auth.codeverification.VerificationViewModel
import com.bangkit.batikloka.ui.auth.createnewpassword.CreateNewPasswordViewModel
import com.bangkit.batikloka.ui.auth.emailverification.EmailVerificationViewModel
import com.bangkit.batikloka.ui.auth.login.LoginViewModel
import com.bangkit.batikloka.ui.auth.register.RegisterViewModel
import com.bangkit.batikloka.ui.auth.startprofile.StartProfileViewModel
import com.bangkit.batikloka.ui.main.user.UserActivityViewModel
import com.bangkit.batikloka.ui.splash.SplashScreenViewModel
import com.bangkit.batikloka.ui.tour.TourViewModel
import com.bangkit.batikloka.utils.PreferencesManager

@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SplashScreenViewModel::class.java) -> {
                SplashScreenViewModel(preferencesManager) as T
            }

            modelClass.isAssignableFrom(VerificationViewModel::class.java) -> {
                VerificationViewModel(preferencesManager, context) as T
            }

            modelClass.isAssignableFrom(CreateNewPasswordViewModel::class.java) -> {
                CreateNewPasswordViewModel(context, preferencesManager) as T
            }

            modelClass.isAssignableFrom(EmailVerificationViewModel::class.java) -> {
                EmailVerificationViewModel(context) as T
            }

            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(preferencesManager) as T
            }

            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(preferencesManager) as T
            }

            modelClass.isAssignableFrom(StartProfileViewModel::class.java) -> {
                StartProfileViewModel() as T
            }

            modelClass.isAssignableFrom(UserActivityViewModel::class.java) -> {
                UserActivityViewModel() as T
            }

            modelClass.isAssignableFrom(TourViewModel::class.java) -> {
                TourViewModel(preferencesManager) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}