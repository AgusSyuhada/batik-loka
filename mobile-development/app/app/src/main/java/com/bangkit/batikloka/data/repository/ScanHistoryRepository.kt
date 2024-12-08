package com.bangkit.batikloka.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.bangkit.batikloka.data.local.dao.ScanHistoryDao
import com.bangkit.batikloka.data.local.entity.ScanHistoryEntity
import com.bangkit.batikloka.data.remote.response.PredictResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date

class ScanHistoryRepository(
    private val context: Context,
    private val scanHistoryDao: ScanHistoryDao
) {
    suspend fun saveScanHistory(
        predictResponse: PredictResponse,
        imageFile: File
    ): Long = withContext(Dispatchers.IO) {
        try {
            val savedImageFile = saveImageToInternalStorage(imageFile)

            val scanHistory = ScanHistoryEntity(
                label = predictResponse.label,
                description = predictResponse.description,
                probability = predictResponse.predictions.maxOrNull() ?: 0.0,
                imagePath = savedImageFile.absolutePath,
                scanDate = Date()
            )

            scanHistoryDao.insertScanHistory(scanHistory)
        } catch (e: Exception) {
            Log.e("ScanHistoryRepository", "Error saving scan history", e)
            -1
        }
    }

    private fun saveImageToInternalStorage(imageFile: File): File {
        return try {
            val directory = File(context.filesDir, "scan_history")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val newFileName = "batik_${System.currentTimeMillis()}.jpg"
            val newFile = File(directory, newFileName)

            imageFile.copyTo(newFile, overwrite = true)
            newFile
        } catch (e: IOException) {
            Log.e("ScanHistoryRepository", "Error saving image", e)
            imageFile
        }
    }

    suspend fun saveImageFromUri(imageUri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val directory = File(context.filesDir, "scan_history")
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val newFileName = "batik_${System.currentTimeMillis()}.jpg"
            val newFile = File(directory, newFileName)

            context.contentResolver.openInputStream(imageUri)?.use { input ->
                FileOutputStream(newFile).use { output ->
                    input.copyTo(output)
                }
            }

            newFile
        } catch (e: Exception) {
            Log.e("ScanHistoryRepository", "Error saving image from Uri", e)
            null
        }
    }

    fun getAllScanHistory(): Flow<List<ScanHistoryEntity>> =
        scanHistoryDao.getAllScanHistory()

    fun getScanHistoryById(id: Int): Flow<ScanHistoryEntity?> {
        return flow {
            emit(scanHistoryDao.getScanHistoryById(id))
        }.flowOn(Dispatchers.IO)
    }

    suspend fun deleteScanHistory(scanHistory: ScanHistoryEntity) {
        withContext(Dispatchers.IO) {
            try {
                val imageFile = File(scanHistory.imagePath)
                if (imageFile.exists()) {
                    imageFile.delete()
                }

                scanHistoryDao.deleteScanHistory(scanHistory)
            } catch (e: Exception) {
                Log.e("ScanHistoryRepository", "Error deleting scan history", e)
            }
        }
    }

    suspend fun deleteAllScanHistory() {
        withContext(Dispatchers.IO) {
            try {
                val historyList = scanHistoryDao.getAllScanHistory().first()
                historyList.forEach { scanHistory ->
                    val imageFile = File(scanHistory.imagePath)
                    if (imageFile.exists()) {
                        imageFile.delete()
                    }
                }

                scanHistoryDao.deleteAllScanHistory()
            } catch (e: Exception) {
                Log.e("ScanHistoryRepository", "Error deleting all scan history", e)
            }
        }
    }
}