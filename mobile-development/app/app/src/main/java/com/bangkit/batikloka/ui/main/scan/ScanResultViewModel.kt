package com.bangkit.batikloka.ui.main.scan

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.batikloka.data.remote.response.PredictResponse
import com.bangkit.batikloka.data.repository.PredictRepository
import com.bangkit.batikloka.data.repository.ScanHistoryRepository
import com.bangkit.batikloka.utils.PreferencesManager
import com.bangkit.batikloka.utils.ScanResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class ScanResultViewModel(
    private val predictRepository: PredictRepository,
    private val scanHistoryRepository: ScanHistoryRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    private val _predictResult = MutableLiveData<ScanResult<PredictResponse>?>(null)
    val predictResult: LiveData<ScanResult<PredictResponse>?> = _predictResult

    private var currentJob: Job? = null

    fun saveScanHistory(predictResponse: PredictResponse, imageFile: File) {
        viewModelScope.launch {
            try {
                scanHistoryRepository.saveScanHistory(predictResponse, imageFile)
            } catch (e: Exception) {
                Log.e("ScanResultViewModel", "Error saving scan history", e)
            }
        }
    }

    fun predictBatik(imageFile: File) {
        viewModelScope.launch {
            _predictResult.value = ScanResult.Loading
            _predictResult.value = predictRepository.predictBatik(imageFile)
        }
    }

    fun cancelCurrentOperation() {
        currentJob?.cancel()
        _predictResult.value = null
    }
}