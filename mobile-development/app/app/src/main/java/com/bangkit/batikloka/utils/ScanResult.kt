package com.bangkit.batikloka.utils

sealed class ScanResult<out T> {
    data class Success<T>(val data: T) : ScanResult<T>()
    data class Error(
        val message: String
    ) : ScanResult<Nothing>()

    object Loading : ScanResult<Nothing>()
}

inline fun <T> ScanResult<T>.onSuccess(action: (T) -> Unit): ScanResult<T> {
    if (this is ScanResult.Success) action(data)
    return this
}

inline fun <T> ScanResult<T>.onError(action: (String) -> Unit): ScanResult<T> {
    if (this is ScanResult.Error) action(message)
    return this
}

inline fun <T> ScanResult<T>.onLoading(action: () -> Unit): ScanResult<T> {
    if (this is ScanResult.Loading) action()
    return this
}