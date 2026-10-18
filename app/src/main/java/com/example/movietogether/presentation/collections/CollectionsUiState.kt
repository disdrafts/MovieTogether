package com.example.movietogether.presentation.collections

import com.example.movietogether.data.remote.dto.CollectionDto
import com.example.movietogether.data.remote.dto.MovieDto

sealed class CollectionsUiState {
    data object Loading : CollectionsUiState()
    data class Success(val collections: List<CollectionDto>) : CollectionsUiState()
    data class Error(val message: String) : CollectionsUiState()
}

sealed class CollectionDetailsUiState {
    data object Loading : CollectionDetailsUiState()
    data class Success(
        val name: String,
        val movies: List<MovieDto>
    ) : CollectionDetailsUiState()
    data class Error(val message: String) : CollectionDetailsUiState()
}