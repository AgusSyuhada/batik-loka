package com.bangkit.batikloka.ui.main.user.historyscan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.batikloka.data.local.entity.ScanHistoryEntity
import com.bangkit.batikloka.data.repository.ScanHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryScanViewModel(
    private val scanHistoryRepository: ScanHistoryRepository
) : ViewModel() {
    val scanHistory = scanHistoryRepository.getAllScanHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteAllHistory() {
        viewModelScope.launch {
            scanHistoryRepository.deleteAllScanHistory()
        }
    }

    fun deleteSingleHistory(scanHistory: ScanHistoryEntity) {
        viewModelScope.launch {
            scanHistoryRepository.deleteScanHistory(scanHistory)
        }
    }
}