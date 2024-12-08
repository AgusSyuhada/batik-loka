package com.bangkit.batikloka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.ui.splash.SplashScreenViewModel
import com.bangkit.batikloka.ui.tour.TourViewModel
import com.bangkit.batikloka.utils.PreferencesManager

@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SplashScreenViewModel::class.java) -> {
                SplashScreenViewModel(preferencesManager) as T
            }

            modelClass.isAssignableFrom(TourViewModel::class.java) -> {
                TourViewModel(preferencesManager) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}