package com.example.movietogether.presentation.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movietogether.data.remote.repository.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionDetailsViewModel @Inject constructor(
    private val repository: CollectionRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<CollectionDetailsUiState>(CollectionDetailsUiState.Loading)
    val uiState: StateFlow<CollectionDetailsUiState> = _uiState.asStateFlow()

    fun load(collectionId: Long, fallbackName: String = "") {
        viewModelScope.launch {
            _uiState.value = CollectionDetailsUiState.Loading
            try {
                val collection = repository.getCollection(collectionId)
                _uiState.value = CollectionDetailsUiState.Success(
                    name = collection.name.ifBlank { fallbackName },
                    movies = collection.movies
                )
            } catch (e: Exception) {
                _uiState.value = CollectionDetailsUiState.Error(e.message ?: "Error")
            }
        }
    }
}