package com.bangkit.batikloka.data.remote.response

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
)

data class RegisterResponse(
    val message: String,
    val error: String,
    val name: String,
)

data class VerifyOtpResponse(
    val message: String,
    val error: String,
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class LoginResponse(
    val token: String,
    val message: String,
    val error: String,
)

data class ForgetPasswordRequest(
    val email: String,
)

data class ForgetPasswordResponse(
    val message: String,
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String,
)

data class ResetPasswordResponse(
    val message: String,
)

data class ProfileResponse(
    val name: String,
    val email: String,
)

data class ChangeAvatarResponse(
    val message: String,
    val avatarUrl: String
)

data class ChangeNameRequest(
    val newName: String,
)

data class ChangeNameResponse(
    val message: String,
)

