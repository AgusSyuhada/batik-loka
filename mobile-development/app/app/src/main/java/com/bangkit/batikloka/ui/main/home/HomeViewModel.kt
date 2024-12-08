package com.bangkit.batikloka.ui.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.batikloka.data.remote.response.NewsItem
import com.bangkit.batikloka.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val newsRepository: NewsRepository
) : ViewModel() {
    private val _newsResult = MutableStateFlow<Result<List<NewsItem>>?>(null)
    val newsResult: StateFlow<Result<List<NewsItem>>?> = _newsResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun fetchNews() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = newsRepository.getNews()
            _newsResult.value = result
            _isLoading.value = false
        }
    }
}