package com.devvikram.sealedui
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Empty(val message: String? = null) : UiState<Nothing>()
    data class Error(val throwable: Throwable? = null, val message: String? = null) : UiState<Nothing>()
}