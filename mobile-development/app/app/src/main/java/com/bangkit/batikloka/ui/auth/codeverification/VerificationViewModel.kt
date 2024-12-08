package com.bangkit.batikloka.ui.auth.codeverification

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.batikloka.R
import com.bangkit.batikloka.data.repository.AuthRepository
import com.bangkit.batikloka.utils.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
class VerificationViewModel(
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {
    private val _verifyOtpResult = MutableLiveData<Result<Any>?>(null)
    val verifyOtpResult: LiveData<Result<Any>?> = _verifyOtpResult

    private var currentJob: Job? = null

    fun verifyOtp(email: String, otp: String) {
        when {
            otp.isEmpty() -> {
                _verifyOtpResult.value = Result.Error(
                    message = context.getString(R.string.error_otp_required),
                    error = context.getString(R.string.error_otp_required)
                )
                return
            }

            otp.length != 6 -> {
                _verifyOtpResult.value = Result.Error(
                    message = context.getString(R.string.error_invalid_otp_length),
                    error = context.getString(R.string.error_invalid_otp_length)
                )
                return
            }
        }

        viewModelScope.launch {
            _verifyOtpResult.value = Result.Loading
            try {
                val result = authRepository.verifyOtp(email, otp)
                _verifyOtpResult.value = result
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("Invalid OTP", ignoreCase = true) == true ->
                        Result.Error(
                            message = context.getString(R.string.error_invalid_otp),
                            error = context.getString(R.string.error_invalid_otp)
                        )

                    else -> Result.Error(
                        message = context.getString(R.string.error_unexpected),
                        error = context.getString(R.string.error_unexpected)
                    )
                }

                _verifyOtpResult.value = errorMessage
            }
        }
    }

    fun cancelCurrentOperation() {
        currentJob?.cancel()
        _verifyOtpResult.value = null
    }

    fun resetState() {
        _verifyOtpResult.value = null
    }
}