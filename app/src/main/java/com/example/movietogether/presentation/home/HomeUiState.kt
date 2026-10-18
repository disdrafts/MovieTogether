package com.example.movietogether.presentation.home

import com.example.movietogether.data.remote.dto.KinopoiskMovieDto

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(
        val popular: List<KinopoiskMovieDto>,
        val top250: List<KinopoiskMovieDto>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}