package com.bangkit.batikloka.ui.auth.register

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
class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {
    private val _registerResult = MutableLiveData<Result<Any>?>(null)
    val registerResult: LiveData<Result<Any>?> = _registerResult

    private var currentJob: Job? = null

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        when {
            name.isEmpty() && email.isEmpty() && password.isEmpty() && confirmPassword.isEmpty() -> {
                _registerResult.value = Result.Error(
                    message = context.getString(R.string.error_all_fields_empty),
                    error = context.getString(R.string.error_all_fields_empty)
                )
                return
            }

            name.isEmpty() -> {
                _registerResult.value = Result.Error(
                    message = context.getString(R.string.error_name_required),
                    error = context.getString(R.string.error_name_required)
                )
                return
            }

            email.isEmpty() -> {
                _registerResult.value = Result.Error(
                    message = context.getString(R.string.error_email_required),
                    error = context.getString(R.string.error_email_required)
                )
                return
            }

            password.isEmpty() -> {
                _registerResult.value = Result.Error(
                    message = context.getString(R.string.error_password_required),
                    error = context.getString(R.string.error_password_required)
                )
                return
            }

            password.length < 8 -> {
                _registerResult.value = Result.Error(
                    message = context.getString(R.string.error_password_too_short),
                    error = context.getString(R.string.error_password_too_short)
                )
                return
            }

            password != confirmPassword -> {
                _registerResult.value = Result.Error(
                    message = context.getString(R.string.error_passwords_not_match),
                    error = context.getString(R.string.error_passwords_not_match)
                )
                return
            }
        }

        viewModelScope.launch {
            _registerResult.value = Result.Loading
            try {
                when (val result =
                    authRepository.register(name, email, password, confirmPassword)) {
                    is Result.Success -> {
                        _registerResult.value = Result.Success(
                            context.getString(R.string.success_register)
                        )
                    }

                    is Result.Error -> {
                        val errorMessage = when {
                            result.message.contains("User exists", ignoreCase = true) ->
                                Result.Error(
                                    message = context.getString(R.string.error_user_exists),
                                    error = context.getString(R.string.error_user_exists)
                                )

                            result.message.contains("invalid", ignoreCase = true) ->
                                Result.Error(
                                    message = context.getString(R.string.error_invalid_credentials),
                                    error = context.getString(R.string.error_invalid_credentials)
                                )

                            else -> result
                        }
                        _registerResult.value = errorMessage
                    }

                    is Result.Loading -> {}
                    else -> {
                        _registerResult.value = Result.Error(
                            message = context.getString(R.string.error_unexpected),
                            error = context.getString(R.string.error_unexpected)
                        )
                    }
                }
            } catch (e: Exception) {
                _registerResult.value = Result.Error(
                    message = context.getString(R.string.error_register_failed),
                    error = e.localizedMessage ?: context.getString(R.string.error_unexpected)
                )
            }
        }
    }

    fun cancelCurrentOperation() {
        currentJob?.cancel()
        _registerResult.value = null
    }

    fun resetState() {
        _registerResult.value = null
    }
}