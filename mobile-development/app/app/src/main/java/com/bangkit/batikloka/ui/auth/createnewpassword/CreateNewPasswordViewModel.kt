package com.bangkit.batikloka.ui.auth.createnewpassword

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
class CreateNewPasswordViewModel(
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {
    private val _resetPasswordResult = MutableLiveData<Result<Any>?>(null)
    val resetPasswordResult: LiveData<Result<Any>?> = _resetPasswordResult

    private var currentJob: Job? = null

    fun resetPassword(email: String, otp: String, newPassword: String, confirmPassword: String) {
        if (email.isEmpty() && otp.isEmpty() && newPassword.isEmpty() && confirmPassword.isEmpty()) {
            _resetPasswordResult.value = Result.Error(
                message = context.getString(R.string.error_all_fields_empty),
                error = context.getString(R.string.error_all_fields_empty)
            )
            return
        }

        if (otp.isEmpty() || otp.length != 6) {
            _resetPasswordResult.value = Result.Error(
                message = context.getString(R.string.error_invalid_verification_code),
                error = context.getString(R.string.error_invalid_verification_code)
            )
            return
        }

        if (newPassword.isEmpty() || newPassword.length < 8) {
            _resetPasswordResult.value = Result.Error(
                message = context.getString(R.string.error_password_too_short),
                error = context.getString(R.string.error_password_too_short)
            )
            return
        }

        if (newPassword != confirmPassword) {
            _resetPasswordResult.value = Result.Error(
                message = context.getString(R.string.error_passwords_not_match),
                error = context.getString(R.string.error_passwords_not_match)
            )
            return
        }

        viewModelScope.launch {
            _resetPasswordResult.value = Result.Loading
            try {
                when (val result = authRepository.resetPassword(email, otp, newPassword)) {
                    is Result.Success -> {
                        _resetPasswordResult.value = Result.Success(
                            context.getString(R.string.success_password_updated)
                        )
                    }

                    is Result.Error -> {
                        _resetPasswordResult.value = result
                    }

                    is Result.Loading -> {}
                    else -> {
                        _resetPasswordResult.value = Result.Error(
                            message = context.getString(R.string.error_unexpected),
                            error = context.getString(R.string.error_unexpected)
                        )
                    }
                }
            } catch (e: Exception) {
                _resetPasswordResult.value = Result.Error(
                    message = context.getString(R.string.error_reset_password_failed),
                    error = e.localizedMessage ?: context.getString(R.string.error_unexpected)
                )
            }
        }
    }

    fun cancelCurrentOperation() {
        currentJob?.cancel()
        _resetPasswordResult.value = null
    }

    fun resetState() {
        _resetPasswordResult.value = null
    }
}