package com.bangkit.batikloka.ui.main.user.viewmodel

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
import java.io.File

@SuppressLint("StaticFieldLeak")
class UserActivityViewModel(
    private val authRepository: AuthRepository,
    private val context: Context
) : ViewModel() {

    private val _profileResult = MutableLiveData<Result<Any>?>(null)
    val profileResult: LiveData<Result<Any>?> = _profileResult

    private val _logoutResult = MutableLiveData<Result<Any>?>(null)
    val logoutResult: LiveData<Result<Any>?> = _logoutResult

    private val _changeNameResult = MutableLiveData<Result<Any>?>(null)
    val changeNameResult: LiveData<Result<Any>?> = _changeNameResult

    private val _changePasswordResult = MutableLiveData<Result<Any>?>(null)
    val changePasswordResult: LiveData<Result<Any>?> = _changePasswordResult

    private var currentJob: Job? = null

    private val _avatarResult = MutableLiveData<Result<Any>?>(null)
    val avatarResult: LiveData<Result<Any>?> = _avatarResult

    fun fetchProfile() {
        viewModelScope.launch {
            _profileResult.value = Result.Loading
            try {
                val result = authRepository.getProfile()
                _profileResult.value = result
            } catch (e: Exception) {
                _profileResult.value = Result.Error(
                    message = context.getString(R.string.failed_to_fetch_profile),
                    error = context.getString(R.string.profile_fetch_error)
                )
            }
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            _logoutResult.value = Result.Loading
            try {
                val result = authRepository.logout()
                _logoutResult.value = result
            } catch (e: Exception) {
                _logoutResult.value = Result.Error(
                    message = context.getString(R.string.logout_failed),
                    error = context.getString(R.string.logout_error)
                )
            }
        }
    }

    fun changeName(newName: String) {
        viewModelScope.launch {
            _changeNameResult.value = Result.Loading
            try {
                if (newName.isEmpty()) {
                    _changeNameResult.value = Result.Error(
                        message = context.getString(R.string.name_cannot_be_empty),
                        error = context.getString(R.string.name_is_required)
                    )
                    return@launch
                }

                if (newName.length < 3) {
                    _changeNameResult.value = Result.Error(
                        message = context.getString(R.string.name_must_be_at_least_3_characters),
                        error = context.getString(R.string.name_too_short)
                    )
                    return@launch
                }

                val result = authRepository.changeName(newName)
                _changeNameResult.value = result
            } catch (e: Exception) {
                _changeNameResult.value = Result.Error(
                    message = context.getString(R.string.failed_to_change_name),
                    error = context.getString(R.string.change_name_error)
                )
            }
        }
    }

    fun forgetPassword(email: String) {
        currentJob = viewModelScope.launch {
            _changePasswordResult.value = Result.Loading
            try {
                val result = authRepository.forgetPassword(email)
                _changePasswordResult.value = result
            } catch (e: Exception) {
                _changePasswordResult.value = Result.Error(
                    message = context.getString(R.string.failed_to_verify_email),
                    error = context.getString(R.string.verification_error)
                )
            }
        }
    }

    fun resetPassword(email: String, verificationCode: String, newPassword: String) {
        currentJob = viewModelScope.launch {
            _changePasswordResult.value = Result.Loading
            try {
                val result = authRepository.resetPassword(email, verificationCode, newPassword)
                _changePasswordResult.value = result
            } catch (e: Exception) {
                _changePasswordResult.value = Result.Error(
                    message = context.getString(R.string.failed_to_reset_password),
                    error = context.getString(R.string.reset_password_error)
                )
            }
        }
    }

    fun uploadAvatar(file: File) {
        viewModelScope.launch {
            _avatarResult.value = Result.Loading
            try {
                val result = authRepository.changeAvatar(file)
                _avatarResult.value = result
            } catch (e: Exception) {
                _avatarResult.value = Result.Error(
                    message = context.getString(R.string.failed_to_upload_avatar),
                    error = context.getString(R.string.avatar_upload_error)
                )
            }
        }
    }

    fun cancelCurrentOperation() {
        currentJob?.cancel()
        _changePasswordResult.value = null
    }

    fun resetState() {
        _profileResult.value = null
        _logoutResult.value = null
        _changeNameResult.value = null
        _changePasswordResult.value = null
        _avatarResult.value = null
    }
}