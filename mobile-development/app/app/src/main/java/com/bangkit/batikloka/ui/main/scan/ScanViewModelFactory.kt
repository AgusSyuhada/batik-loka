package com.bangkit.batikloka.ui.main.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bangkit.batikloka.data.repository.PredictRepository
import com.bangkit.batikloka.data.repository.ScanHistoryRepository
import com.bangkit.batikloka.utils.PreferencesManager

class ScanViewModelFactory(
    private val predictRepository: PredictRepository,
    private val scanHistoryRepository: ScanHistoryRepository,
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ScanResultViewModel(
            predictRepository,
            scanHistoryRepository,
            preferencesManager
        ) as T
    }
}