package com.example.movietogether.data.remote.dto

data class KinopoiskMovieDto (
    val id: Long,
    val title: String,
    val overview: String?,
    val year: Int?,
    val rating: Double?,
    val posterUrl: String?
)