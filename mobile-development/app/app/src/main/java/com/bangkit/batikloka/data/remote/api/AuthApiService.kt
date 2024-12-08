package com.bangkit.batikloka.data.remote.api

import com.bangkit.batikloka.data.remote.response.ChangeAvatarResponse
import com.bangkit.batikloka.data.remote.response.ChangeNameRequest
import com.bangkit.batikloka.data.remote.response.ChangeNameResponse
import com.bangkit.batikloka.data.remote.response.ForgetPasswordRequest
import com.bangkit.batikloka.data.remote.response.ForgetPasswordResponse
import com.bangkit.batikloka.data.remote.response.LoginRequest
import com.bangkit.batikloka.data.remote.response.LoginResponse
import com.bangkit.batikloka.data.remote.response.ProfileResponse
import com.bangkit.batikloka.data.remote.response.RegisterRequest
import com.bangkit.batikloka.data.remote.response.RegisterResponse
import com.bangkit.batikloka.data.remote.response.ResetPasswordRequest
import com.bangkit.batikloka.data.remote.response.ResetPasswordResponse
import com.bangkit.batikloka.data.remote.response.VerifyOtpRequest
import com.bangkit.batikloka.data.remote.response.VerifyOtpResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface AuthApiService {
    @POST("verify-otp")
    suspend fun verifyOtp(
        @Body verifyOtpRequest: VerifyOtpRequest
    ): VerifyOtpResponse

    @POST("login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): LoginResponse

    @POST("register")
    suspend fun register(
        @Body registerRequest: RegisterRequest
    ): RegisterResponse

    @POST("forget-password")
    suspend fun forgetPassword(
        @Body forgetPasswordRequest: ForgetPasswordRequest
    ): ForgetPasswordResponse

    @POST("reset-password")
    suspend fun resetPassword(
        @Body resetPasswordRequest: ResetPasswordRequest
    ): ResetPasswordResponse

    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): ProfileResponse

    @Multipart
    @POST("change-avatar")
    suspend fun changeAvatar(
        @Header("Authorization") token: String,
        @Part avatar: MultipartBody.Part
    ): ChangeAvatarResponse

    @PUT("change-name")
    suspend fun changeName(
        @Header("Authorization") token: String,
        @Body requestBody: ChangeNameRequest
    ): ChangeNameResponse
}
