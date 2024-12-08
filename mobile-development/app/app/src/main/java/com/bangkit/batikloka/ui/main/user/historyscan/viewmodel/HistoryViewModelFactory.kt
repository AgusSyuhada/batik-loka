package com.bangkit.batikloka.ui.main.user.historyscan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.data.repository.ScanHistoryRepository
import com.bangkit.batikloka.ui.main.user.historyscan.HistoryScanViewModel
import com.bangkit.batikloka.ui.main.user.historyscan.detail.ScanHistoryDetailViewModel

class HistoryViewModelFactory(
    private val scanHistoryRepository: ScanHistoryRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HistoryScanViewModel::class.java) -> {
                HistoryScanViewModel(scanHistoryRepository) as T
            }

            modelClass.isAssignableFrom(ScanHistoryDetailViewModel::class.java) -> {
                ScanHistoryDetailViewModel(scanHistoryRepository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}