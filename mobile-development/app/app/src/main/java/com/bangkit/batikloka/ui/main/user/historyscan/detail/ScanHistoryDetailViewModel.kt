package com.bangkit.batikloka.ui.main.user.historyscan.detail

import androidx.lifecycle.ViewModel
import com.bangkit.batikloka.data.local.entity.ScanHistoryEntity
import com.bangkit.batikloka.data.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.Flow

class ScanHistoryDetailViewModel(
    private val scanHistoryRepository: ScanHistoryRepository
) : ViewModel() {
    fun getScanHistoryById(id: Int): Flow<ScanHistoryEntity?> =
        scanHistoryRepository.getScanHistoryById(id)
}