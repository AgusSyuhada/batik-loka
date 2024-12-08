package com.bangkit.batikloka.ui.auth.emailverification

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
class EmailVerificationViewModel(
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {
    private val _forgetPasswordResult = MutableLiveData<Result<Any>?>(null)
    val forgetPasswordResult: LiveData<Result<Any>?> = _forgetPasswordResult

    private var currentJob: Job? = null

    fun forgetPassword(email: String) {
        if (email.isEmpty()) {
            _forgetPasswordResult.value = Result.Error(
                message = context.getString(R.string.error_email_required),
                error = context.getString(R.string.error_email_required)
            )
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _forgetPasswordResult.value = Result.Error(
                message = context.getString(R.string.error_invalid_email_format),
                error = context.getString(R.string.error_invalid_email_format)
            )
            return
        }

        viewModelScope.launch {
            _forgetPasswordResult.value = Result.Loading
            try {
                when (val result = authRepository.forgetPassword(email)) {
                    is Result.Success -> {
                        _forgetPasswordResult.value = Result.Success(result.data)
                    }

                    is Result.Error -> {
                        val errorMessage = when {
                            result.message.contains("User not found", ignoreCase = true) ->
                                Result.Error(
                                    message = context.getString(R.string.error_user_not_found),
                                    error = context.getString(R.string.error_user_not_found)
                                )

                            result.message.contains("required", ignoreCase = true) ->
                                Result.Error(
                                    message = context.getString(R.string.error_email_required),
                                    error = context.getString(R.string.error_email_required)
                                )

                            else -> result
                        }
                        _forgetPasswordResult.value = errorMessage
                    }

                    else -> {
                        _forgetPasswordResult.value = Result.Error(
                            message = context.getString(R.string.error_unexpected),
                            error = context.getString(R.string.error_unexpected)
                        )
                    }
                }
            } catch (e: Exception) {
                _forgetPasswordResult.value = Result.Error(
                    message = context.getString(R.string.error_send_failed) + ": ${e.localizedMessage}",
                    error = e.localizedMessage ?: context.getString(R.string.error_unexpected)
                )
            }
        }
    }

    fun cancelCurrentOperation() {
        currentJob?.cancel()
        _forgetPasswordResult.value = null
    }

    fun resetState() {
        _forgetPasswordResult.value = null
    }
}