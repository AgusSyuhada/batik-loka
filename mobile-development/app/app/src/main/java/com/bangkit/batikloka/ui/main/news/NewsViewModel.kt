package com.bangkit.batikloka.ui.main.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangkit.batikloka.data.remote.response.NewsItem
import com.bangkit.batikloka.data.repository.NewsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsViewModel(
    private val newsRepository: NewsRepository
) : ViewModel() {
    private val _newsState = MutableStateFlow<List<NewsItem>>(emptyList())
    val newsState: StateFlow<List<NewsItem>> = _newsState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchNews() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            newsRepository.getNews()
                .onSuccess { newsList ->
                    _newsState.value = newsList
                    _isLoading.value = false
                }
                .onFailure { error ->
                    _error.value = error.message
                    _isLoading.value = false
                }
        }
    }
}