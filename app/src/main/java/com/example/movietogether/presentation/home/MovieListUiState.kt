package com.example.movietogether.presentation.home

import com.example.movietogether.data.remote.dto.KinopoiskMovieDto

sealed class MovieListUiState {
    data object Loading : MovieListUiState()
    data class Success(
        val movies: List<KinopoiskMovieDto>,
        val titleType: String
    ) : MovieListUiState()
    data class Error(val message: String) : MovieListUiState()
}