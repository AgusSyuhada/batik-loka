package com.bangkit.batikloka.ui.auth.login

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
class LoginViewModel(
    val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {
    private val _loginResult = MutableLiveData<Result<Any>?>(null)
    val loginResult: LiveData<Result<Any>?> = _loginResult

    private var currentJob: Job? = null

    fun login(email: String, password: String) {
        if (email.isEmpty() && password.isEmpty()) {
            _loginResult.value = Result.Error(
                message = context.getString(R.string.error_all_fields_empty),
                error = context.getString(R.string.error_all_fields_empty)
            )
            return
        }

        if (email.isEmpty()) {
            _loginResult.value = Result.Error(
                message = context.getString(R.string.error_email_required),
                error = context.getString(R.string.error_email_required)
            )
            return
        }

        if (password.isEmpty()) {
            _loginResult.value = Result.Error(
                message = context.getString(R.string.error_password_required),
                error = context.getString(R.string.error_password_required)
            )
            return
        }

        viewModelScope.launch {
            _loginResult.value = Result.Loading
            try {
                when (val result = authRepository.login(email, password)) {
                    is Result.Success -> {
                        _loginResult.value = Result.Success(
                            context.getString(R.string.success_login)
                        )
                    }

                    is Result.Error -> {
                        val errorMessage = when {
                            result.message.contains("not found", ignoreCase = true) ->
                                Result.Error(
                                    message = context.getString(R.string.error_user_not_found),
                                    error = context.getString(R.string.error_user_not_found)
                                )

                            result.message.contains("invalid", ignoreCase = true) ->
                                Result.Error(
                                    message = context.getString(R.string.error_invalid_credentials),
                                    error = context.getString(R.string.error_invalid_credentials)
                                )

                            else -> result
                        }
                        _loginResult.value = errorMessage
                    }

                    is Result.Loading -> {}
                    else -> {
                        _loginResult.value = Result.Error(
                            message = context.getString(R.string.error_unexpected),
                            error = context.getString(R.string.error_unexpected)
                        )
                    }
                }
            } catch (e: Exception) {
                _loginResult.value = Result.Error(
                    message = context.getString(R.string.error_login_failed),
                    error = e.localizedMessage ?: context.getString(R.string.error_unexpected)
                )
            }
        }
    }

    fun cancelCurrentOperation() {
        currentJob?.cancel()
        _loginResult.value = null
    }

    fun resetState() {
        _loginResult.value = null
    }
}