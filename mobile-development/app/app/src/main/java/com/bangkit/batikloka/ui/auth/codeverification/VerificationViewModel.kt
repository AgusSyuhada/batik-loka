package com.bangkit.batikloka.ui.auth.codeverification

import androidx.lifecycle.ViewModel
import com.bangkit.batikloka.utils.PreferencesManager

class VerificationViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {

    fun validateOtp(otp: String): Boolean {
        return otp.isNotEmpty() && otp.length == 6
    }

    fun confirmOtp(otp: String): String {
        preferencesManager.setUserRegistered()
        return "OTP confirmed successfully!"
    }
}