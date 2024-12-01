package com.bangkit.batikloka.ui.auth.codeverification

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import com.bangkit.batikloka.R
import com.bangkit.batikloka.utils.PreferencesManager

@SuppressLint("StaticFieldLeak")
class VerificationViewModel(
    private val preferencesManager: PreferencesManager,
    private val context: Context,
) : ViewModel() {

    fun validateOtp(otp: String): Boolean {
        return otp.isNotEmpty() && otp.length == 6
    }

    fun confirmOtp(otp: String): String {
        preferencesManager.setUserRegistered()
        preferencesManager.saveRegistrationStep("otp_verified")
        return context.getString(R.string.otp_confirmed_successfully)
    }
}