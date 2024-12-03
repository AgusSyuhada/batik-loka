package com.bangkit.batikloka.ui.main.user

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.batikloka.data.local.database.AppDatabase
import com.bangkit.batikloka.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class UserActivityViewModel(
    private val preferencesManager: PreferencesManager,
    private val database: AppDatabase,
) : ViewModel() {

    fun logImageSourceSelection(source: String) {
        Log.d("UserActivityViewModel", "$source selected")
    }

    fun updateProfilePicture(imageUri: Uri) {
        viewModelScope.launch {
            try {
                val email = preferencesManager.getUserEmail() ?: return@launch

                val bitmap = withContext(Dispatchers.IO) {
                    val file = File(imageUri.path ?: return@withContext null)
                    val inputStream = FileInputStream(file)
                    BitmapFactory.decodeStream(inputStream)
                }

                bitmap?.let {
                    val byteArray = bitmapToByteArray(it)

                    withContext(Dispatchers.IO) {
                        database.userDao().updateProfilePicture(email, byteArray)
                    }

                    preferencesManager.saveProfilePictureUri(imageUri.toString())
                }
            } catch (e: Exception) {
                Log.e("UserActivityViewModel", "Error updating profile picture", e)
            }
        }
    }

    suspend fun retrieveProfilePicture(): Bitmap? {
        val email = preferencesManager.getUserEmail() ?: return null

        return withContext(Dispatchers.IO) {
            val byteArray = database.userDao().getProfilePicture(email)
            byteArray?.let { byteArrayToBitmap(it) }
        }
    }

    fun updateUsername(newUsername: String) {
        viewModelScope.launch {
            val email = preferencesManager.getUserEmail() ?: return@launch

            withContext(Dispatchers.IO) {
                database.userDao().updateUsername(email, newUsername)
            }

            preferencesManager.saveUserName(newUsername)
        }
    }

    suspend fun updatePassword(
        email: String,
        currentPassword: String,
        newPassword: String,
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val hashedCurrentPassword = hashPassword(currentPassword)
                val user = database.userDao().getUserByEmail(email)

                if (user?.password == hashedCurrentPassword) {
                    val hashedNewPassword = hashPassword(newPassword)

                    database.userDao().updatePassword(email, hashedNewPassword)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("UserActivityViewModel", "Error updating password", e)
                false
            }
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return outputStream.toByteArray()
    }

    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    private fun hashPassword(password: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}