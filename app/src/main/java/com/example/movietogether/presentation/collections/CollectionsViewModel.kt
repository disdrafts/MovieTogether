package com.example.movietogether.presentation.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movietogether.data.Session
import com.example.movietogether.data.remote.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val repository: CollectionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CollectionsUiState>(CollectionsUiState.Loading)
    val uiState: StateFlow<CollectionsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CollectionsUiState.Loading
            try {
                val list = repository.getUserCollections(Session.userId)
                _uiState.value = CollectionsUiState.Success(list)
            } catch (e: Exception) {
                _uiState.value = CollectionsUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun createCollection(name: String) {
        val n = name.trim()
        if (n.isEmpty()) return

        viewModelScope.launch {
            try {
                repository.createCollection(n, Session.userId)
                load()
            } catch (e: Exception) {
                _uiState.value = CollectionsUiState.Error(e.message ?: "Error")
            }
        }
    }
}