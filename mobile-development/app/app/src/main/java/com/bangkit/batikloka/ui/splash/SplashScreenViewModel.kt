package com.bangkit.batikloka.ui.splash

import androidx.lifecycle.ViewModel
import com.bangkit.batikloka.utils.PreferencesManager

class SplashScreenViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    fun isUserLoggedIn(): Boolean {
        return preferencesManager.isUserLoggedIn()
    }

    fun isUserRegistered(): Boolean {
        return preferencesManager.isUserRegistered()
    }
}