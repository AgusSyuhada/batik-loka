package com.bangkit.batikloka.ui.main.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.batikloka.data.remote.response.NewsItem
import com.bangkit.batikloka.data.repository.NewsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val newsRepository: NewsRepository
) : ViewModel() {
    private val _newsState = MutableStateFlow<List<NewsItem>>(emptyList())
    val newsState: StateFlow<List<NewsItem>> = _newsState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            newsRepository.getLocalNews().collect { localNews ->
                _newsState.value = localNews
            }
        }
    }

    fun fetchNews() {
        viewModelScope.launch {
            delay(500)
            _isLoading.value = true
            _isRefreshing.value = true
            _error.value = null

            newsRepository.getNews()
                .onSuccess { newsList ->
                    _newsState.value = newsList
                    _isRefreshing.value = false
                    _isLoading.value = false
                }
                .onFailure { error ->
                    _error.value = error.message
                    _isLoading.value = false
                    _isRefreshing.value = false
                }
        }
    }
}