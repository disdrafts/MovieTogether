package com.example.movietogether.presentation.movie

import com.example.movietogether.data.remote.dto.CollectionDto
import com.example.movietogether.data.remote.dto.MovieDto
import com.example.movietogether.data.remote.dto.RoomDto

data class MovieDetailsArgs(
    val kinopoiskId: Long,
    val title: String,
    val overview: String?,
    val year: Int?,
    val rating: Double?,
    val posterUrl: String?
)

sealed class MovieDetailsEvent {
    data object Idle : MovieDetailsEvent()
    data class ShowCollections(val collections: List<CollectionDto>, val movieId: Long) : MovieDetailsEvent()
    data class RoomCreated(val room: RoomDto) : MovieDetailsEvent()
    data class Error(val message: String) : MovieDetailsEvent()
    data class Info(val message: String) : MovieDetailsEvent()
}

data class MovieDetailsUiState(
    val args: MovieDetailsArgs? = null,
    val localMovie: MovieDto? = null,
    val hasTrailer: Boolean = false,
    val isLoadingTrailer: Boolean = false,
    val event: MovieDetailsEvent = MovieDetailsEvent.Idle
)