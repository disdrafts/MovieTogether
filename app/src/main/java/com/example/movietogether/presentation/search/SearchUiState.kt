package com.example.movietogether.presentation.search

import com.example.movietogether.data.remote.dto.KinopoiskMovieDto

sealed class SearchUiState {
    data object Loading : SearchUiState()
    data class Success(val movies: List<KinopoiskMovieDto>, val isEmptyQuery: Boolean) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}