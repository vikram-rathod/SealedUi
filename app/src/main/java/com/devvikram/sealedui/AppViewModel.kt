package com.devvikram.sealedui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel  : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<String>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            delay(1000)
            _uiState.value = UiState.Success(listOf("Apple", "Banana", "Cherry"))
            delay(3000)
            _uiState.value = UiState.Empty("No items found.")
            delay(3000)
            _uiState.value = UiState.Error(message = "Failed to load items.")

        }
    }


}