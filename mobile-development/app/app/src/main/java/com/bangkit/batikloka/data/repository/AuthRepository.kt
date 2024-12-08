package com.bangkit.batikloka.data.repository

import android.util.Log
import com.bangkit.batikloka.data.remote.api.AuthApiService
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
import com.bangkit.batikloka.utils.PreferencesManager
import com.bangkit.batikloka.utils.Result
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.File
import java.io.IOException

class AuthRepository(
    private val apiService: AuthApiService,
    private val preferencesManager: PreferencesManager,
) {
    suspend fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): Result<RegisterResponse> {
        Log.d("AuthRepository", "Register attempt started")
        Log.d("AuthRepository", "Email: $email")

        return try {
            Log.d("AuthRepository", "Preparing register request")
            val registerRequest = RegisterRequest(name, email, password, confirmPassword)

            Log.d("AuthRepository", "Sending register request to API")
            val response = apiService.register(registerRequest)

            Log.d("AuthRepository", "API Response received")
            Log.d("AuthRepository", "Response: $response")

            Log.d("AuthRepository", "Register successful")
            Result.Success(response)
        } catch (e: HttpException) {
            Log.e("AuthRepository", "HttpException occurred", e)

            val errorBody = try {
                e.response()?.errorBody()?.string()
            } catch (ioException: IOException) {
                null
            }

            val errorMessage = try {
                if (errorBody != null) {
                    val errorJson = JSONObject(errorBody)

                    when {
                        errorJson.has("message") -> errorJson.getString("message")
                        errorJson.has("error") -> errorJson.getString("error")
                        else -> "Register failed"
                    }
                } else {
                    "Register failed"
                }
            } catch (jsonException: JSONException) {
                "${e.code()} ${e.message()}"
            }

            Log.e("AuthRepository", "Register failed with error: $errorMessage")
            Result.Error(
                message = errorMessage,
                error = errorMessage
            )
        } catch (e: Exception) {
            Log.e("AuthRepository", "Unexpected error during register", e)
            Result.Error(
                message = e.localizedMessage ?: "Register failed",
                error = e.localizedMessage ?: "Register failed"
            )
        }
    }

    suspend fun login(
        email: String,
        password: String,
    ): Result<LoginResponse> {
        Log.d("AuthRepository", "Login attempt started")
        Log.d("AuthRepository", "Email: $email")

        return try {
            Log.d("AuthRepository", "Preparing login request")
            val loginRequest = LoginRequest(email, password)

            Log.d("AuthRepository", "Sending login request to API")
            val response = apiService.login(loginRequest)

            Log.d("AuthRepository", "API Response received")
            Log.d("AuthRepository", "Response: $response")

            Log.d("AuthRepository", "Saving token to preferences")
            preferencesManager.saveToken(response.token)

            Log.d("AuthRepository", "Login successful")
            Log.d("AuthRepository", "Token: ${response.token}")

            Result.Success(response)
        } catch (e: HttpException) {
            Log.e("AuthRepository", "HttpException occurred", e)

            val errorBody = try {
                e.response()?.errorBody()?.string()
            } catch (ioException: IOException) {
                null
            }

            val errorMessage = try {
                if (errorBody != null) {
                    val errorJson = JSONObject(errorBody)

                    when {
                        errorJson.has("message") -> errorJson.getString("message")
                        errorJson.has("error") -> errorJson.getString("error")
                        else -> "Login failed"
                    }
                } else {
                    "Login failed"
                }
            } catch (jsonException: JSONException) {
                "${e.code()} ${e.message()}"
            }

            Log.e("AuthRepository", "Login failed with error: $errorMessage")
            Result.Error(
                message = errorMessage,
                error = errorMessage
            )
        } catch (e: Exception) {
            Log.e("AuthRepository", "Unexpected error during login", e)
            Result.Error(
                message = e.localizedMessage ?: "Login failed",
                error = e.localizedMessage ?: "Login failed"
            )
        }
    }

    fun logout(): Result<Unit> {
        return try {
            preferencesManager.clearToken()

            Log.d("AuthRepository", "Logout successful")
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Logout failed", e)
            Result.Error(
                message = "Logout Gagal",
                error = e.localizedMessage ?: "Terjadi kesalahan saat logout"
            )
        }
    }

    suspend fun changeAvatar(
        imageFile: File
    ): Result<ChangeAvatarResponse> {
        Log.d("AuthRepository", "Avatar upload attempt started")
        Log.d("AuthRepository", "File: ${imageFile.name}")

        return try {
            val token = preferencesManager.getToken()
                ?: return Result.Error(
                    message = "Pengguna belum login",
                    error = "Unauthorized"
                )

            Log.d("AuthRepository", "Preparing avatar upload request")
            val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val avatarPart = MultipartBody.Part.createFormData(
                "avatar",
                imageFile.name,
                requestFile
            )

            Log.d("AuthRepository", "Sending avatar upload request to API")
            val response = apiService.changeAvatar("Bearer $token", avatarPart)

            Log.d("AuthRepository", "API Response received")
            Log.d("AuthRepository", "Response: $response")

            Log.d("AuthRepository", "Avatar upload successful")
            Result.Success(response)
        } catch (e: HttpException) {
            Log.e("AuthRepository", "HttpException occurred", e)

            val errorBody = try {
                e.response()?.errorBody()?.string()
            } catch (ioException: IOException) {
                null
            }

            val errorMessage = try {
                if (errorBody != null) {
                    val errorJson = JSONObject(errorBody)

                    when {
                        errorJson.has("message") -> errorJson.getString("message")
                        errorJson.has("error") -> errorJson.getString("error")
                        else -> "Upload avatar failed"
                    }
                } else {
                    "Upload avatar failed"
                }
            } catch (jsonException: JSONException) {
                "${e.code()} ${e.message()}"
            }

            Log.e("AuthRepository", "Avatar upload failed with error: $errorMessage")
            Result.Error(
                message = errorMessage,
                error = errorMessage
            )
        } catch (e: Exception) {
            Log.e("AuthRepository", "Unexpected error during avatar upload", e)
            Result.Error(
                message = e.localizedMessage ?: "Upload avatar failed",
                error = e.localizedMessage ?: "Upload avatar failed"
            )
        }
    }

    fun isUserLoggedIn(): Boolean {
        return preferencesManager.getToken() != null
    }

    suspend fun verifyOtp(
        email: String,
        otp: String,
    ): Result<VerifyOtpResponse> {
        Log.d("AuthRepository", "OTP verification attempt started")
        Log.d("AuthRepository", "Email: $email")

        return try {
            Log.d("AuthRepository", "Preparing OTP verification request")
            val request = VerifyOtpRequest(email, otp)

            Log.d("AuthRepository", "Sending OTP verification request to API")
            val response = apiService.verifyOtp(request)

            Log.d("AuthRepository", "API Response received")
            Log.d("AuthRepository", "Response: $response")

            Log.d("AuthRepository", "OTP verification successful")
            Result.Success(response)
        } catch (e: HttpException) {
            Log.e("AuthRepository", "HttpException occurred", e)

            val errorBody = try {
                e.response()?.errorBody()?.string()
            } catch (ioException: IOException) {
                null
            }

            val errorMessage = try {
                if (errorBody != null) {
                    val errorJson = JSONObject(errorBody)

                    when {
                        errorJson.has("message") -> errorJson.getString("message")
                        errorJson.has("error") -> errorJson.getString("error")
                        else -> "Verification failed"
                    }
                } else {
                    "Verification failed"
                }
            } catch (jsonException: JSONException) {
                "${e.code()} ${e.message()}"
            }

            Log.e("AuthRepository", "Verification failed with error: $errorMessage")
            Result.Error(
                message = errorMessage,
                error = errorMessage
            )
        } catch (e: Exception) {
            Log.e("AuthRepository", "Unexpected error during verification", e)
            Result.Error(
                message = e.localizedMessage ?: "Verification failed",
                error = e.localizedMessage ?: "Verification failed"
            )
        }
    }

    suspend fun forgetPassword(
        email: String,
    ): Result<ForgetPasswordResponse> {
        Log.d("AuthRepository", "Forget Password attempt started")
        Log.d("AuthRepository", "Email: $email")

        return try {
            Log.d("AuthRepository", "Preparing forget password request")
            val request = ForgetPasswordRequest(email)

            Log.d("AuthRepository", "Sending forget password request to API")
            val response = apiService.forgetPassword(request)

            Log.d("AuthRepository", "API Response received")
            Log.d("AuthRepository", "Response: $response")

            Log.d("AuthRepository", "Forget password request successful")
            Result.Success(response)
        } catch (e: HttpException) {
            Log.e("AuthRepository", "HttpException occurred during forget password", e)

            val errorBody = try {
                e.response()?.errorBody()?.string()
            } catch (ioException: IOException) {
                null
            }

            val errorMessage = try {
                if (errorBody != null) {
                    val errorJson = JSONObject(errorBody)

                    when {
                        errorJson.has("message") -> errorJson.getString("message")
                        errorJson.has("error") -> errorJson.getString("error")
                        else -> "Forget password failed"
                    }
                } else {
                    "Forget password failed"
                }
            } catch (jsonException: JSONException) {
                "${e.code()} ${e.message()}"
            }

            Log.e("AuthRepository", "Forget password failed with error: $errorMessage")
            Result.Error(
                message = errorMessage,
                error = errorMessage
            )
        } catch (e: Exception) {
            Log.e("AuthRepository", "Unexpected error during forget password process", e)
            Result.Error(
                message = e.localizedMessage ?: "Forget password failed",
                error = e.localizedMessage ?: "Forget password failed"
            )
        }
    }

    suspend fun resetPassword(
        email: String,
        otp: String,
        newPassword: String,
    ): Result<ResetPasswordResponse> {
        Log.d("AuthRepository", "Reset Password attempt started")
        Log.d("AuthRepository", "Email: $email")
        Log.d("AuthRepository", "OTP: $otp")

        return try {
            Log.d("AuthRepository", "Preparing reset password request")
            val request = ResetPasswordRequest(email, otp, newPassword)

            Log.d("AuthRepository", "Sending reset password request to API")
            val response = apiService.resetPassword(request)

            Log.d("AuthRepository", "API Response received")
            Log.d("AuthRepository", "Response: $response")

            Log.d("AuthRepository", "Reset password successful")
            Result.Success(response)
        } catch (e: HttpException) {
            Log.e("AuthRepository", "HttpException occurred during reset password", e)

            val errorBody = try {
                e.response()?.errorBody()?.string()
            } catch (ioException: IOException) {
                null
            }

            val errorMessage = try {
                if (errorBody != null) {
                    val errorJson = JSONObject(errorBody)

                    when {
                        errorJson.has("message") -> errorJson.getString("message")
                        errorJson.has("error") -> errorJson.getString("error")
                        else -> "Reset password failed"
                    }
                } else {
                    "Reset password failed"
                }
            } catch (jsonException: JSONException) {
                "${e.code()} ${e.message()}"
            }

            Log.e("AuthRepository", "Reset password failed with error: $errorMessage")
            Result.Error(
                message = errorMessage,
                error = errorMessage
            )
        } catch (e: Exception) {
            Log.e("AuthRepository", "Unexpected error during reset password process", e)
            Result.Error(
                message = e.localizedMessage ?: "Reset password failed",
                error = e.localizedMessage ?: "Reset password failed"
            )
        }
    }

    suspend fun getProfile(): Result<ProfileResponse> {
        Log.d("AuthRepository", "Get Profile attempt started")

        return try {
            val token = preferencesManager.getToken()
                ?: return Result.Error(
                    message = "Pengguna belum login",
                    error = "Unauthorized"
                )

            Log.d("AuthRepository", "Sending get profile request to API")
            val response = apiService.getProfile("Bearer $token")

            Log.d("AuthRepository", "API Response received")
            Log.d("AuthRepository", "Response: $response")

            Log.d("AuthRepository", "Get profile successful")
            Result.Success(response)
        } catch (e: HttpException) {
            Log.e("AuthRepository", "HttpException occurred", e)

            val errorBody = try {
                e.response()?.errorBody()?.string()
            } catch (ioException: IOException) {
                null
            }

            val errorMessage = try {
                if (errorBody != null) {
                    val errorJson = JSONObject(errorBody)

                    when {
                        errorJson.has("message") -> errorJson.getString("message")
                        errorJson.has("error") -> errorJson.getString("error")
                        else -> "Gagal mendapatkan profil"
                    }
                } else {
                    "Gagal mendapatkan profil"
                }
            } catch (jsonException: JSONException) {
                "${e.code()} ${e.message()}"
            }

            Log.e("AuthRepository", "Get profile failed with error: $errorMessage")
            Result.Error(
                message = errorMessage,
                error = errorMessage
            )
        } catch (e: Exception) {
            Log.e("AuthRepository", "Unexpected error during get profile", e)
            Result.Error(
                message = e.localizedMessage ?: "Gagal mendapatkan profil",
                error = e.localizedMessage ?: "Gagal mendapatkan profil"
            )
        }
    }

    suspend fun changeName(
        newName: String
    ): Result<ChangeNameResponse> {
        Log.d("AuthRepository", "Change Name attempt started")
        Log.d("AuthRepository", "New Name: $newName")

        return try {
            val token = preferencesManager.getToken()
                ?: return Result.Error(
                    message = "Pengguna belum login",
                    error = "Unauthorized"
                )

            Log.d("AuthRepository", "Preparing change name request")
            val request = ChangeNameRequest(newName)

            Log.d("AuthRepository", "Sending change name request to API")
            val response = apiService.changeName("Bearer $token", request)

            Log.d("AuthRepository", "API Response received")
            Log.d("AuthRepository", "Response: $response")

            Log.d("AuthRepository", "Change name successful")
            Result.Success(response)
        } catch (e: HttpException) {
            Log.e("AuthRepository", "HttpException occurred", e)

            val errorBody = try {
                e.response()?.errorBody()?.string()
            } catch (ioException: IOException) {
                null
            }

            val errorMessage = try {
                if (errorBody != null) {
                    val errorJson = JSONObject(errorBody)

                    when {
                        errorJson.has("message") -> errorJson.getString("message")
                        errorJson.has("error") -> errorJson.getString("error")
                        else -> "Gagal mengubah nama"
                    }
                } else {
                    "Gagal mengubah nama"
                }
            } catch (jsonException: JSONException) {
                "${e.code()} ${e.message()}"
            }

            Log.e("AuthRepository", "Change name failed with error: $errorMessage")
            Result.Error(
                message = errorMessage,
                error = errorMessage
            )
        } catch (e: Exception) {
            Log.e("AuthRepository", "Unexpected error during change name", e)
            Result.Error(
                message = e.localizedMessage ?: "Gagal mengubah nama",
                error = e.localizedMessage ?: "Gagal mengubah nama"
            )
        }
    }
}