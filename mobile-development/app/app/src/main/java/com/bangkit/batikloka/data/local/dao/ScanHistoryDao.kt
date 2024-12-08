package com.bangkit.batikloka.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.bangkit.batikloka.data.local.entity.ScanHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
    @Insert
    suspend fun insertScanHistory(scanHistory: ScanHistoryEntity): Long

    @Query("SELECT * FROM scan_history ORDER BY scanDate DESC")
    fun getAllScanHistory(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE id = :id")
    suspend fun getScanHistoryById(id: Int): ScanHistoryEntity?

    @Delete
    suspend fun deleteScanHistory(scanHistory: ScanHistoryEntity)

    @Query("DELETE FROM scan_history")
    suspend fun deleteAllScanHistory()

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteScanHistoryById(id: Int)
}