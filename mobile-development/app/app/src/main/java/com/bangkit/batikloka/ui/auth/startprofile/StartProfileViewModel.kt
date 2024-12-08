//package com.bangkit.batikloka.ui.auth.startprofile
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.net.Uri
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.bangkit.batikloka.data.local.database.AppDatabase
//import com.bangkit.batikloka.utils.PreferencesManager
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.ByteArrayOutputStream
//import java.io.File
//import java.io.FileInputStream
//
//class StartProfileViewModel(
//    private val preferencesManager: PreferencesManager,
//    private val database: AppDatabase,
//) : ViewModel() {
//
//    fun logImageSourceSelection(source: String) {
//        Log.d("StartProfileViewModel", "$source selected")
//    }
//
//    fun saveProfilePicture(imageUri: Uri) {
//        viewModelScope.launch {
//            try {
//                val email = preferencesManager.getUserEmail() ?: return@launch
//
//                val bitmap = withContext(Dispatchers.IO) {
//                    val file = File(imageUri.path ?: return@withContext null)
//                    val inputStream = FileInputStream(file)
//                    BitmapFactory.decodeStream(inputStream)
//                }
//
//                bitmap?.let {
//                    val byteArray = bitmapToByteArray(it)
//
//                    withContext(Dispatchers.IO) {
//                        database.userDao().updateProfilePicture(email, byteArray)
//                    }
//
//                    preferencesManager.saveProfilePictureUri(imageUri.toString())
//                }
//            } catch (e: Exception) {
//                Log.e("StartProfileViewModel", "Error saving profile picture", e)
//            }
//        }
//    }
//
//    suspend fun retrieveProfilePicture(): Bitmap? {
//        val email = preferencesManager.getUserEmail() ?: return null
//
//        return withContext(Dispatchers.IO) {
//            val byteArray = database.userDao().getProfilePicture(email)
//            byteArray?.let { byteArrayToBitmap(it) }
//        }
//    }
//
//
//    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
//        val outputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
//        return outputStream.toByteArray()
//    }
//
//    private fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
//        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
//    }
//}