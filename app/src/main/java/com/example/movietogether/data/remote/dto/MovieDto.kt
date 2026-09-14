package com.example.movietogether.data.remote.dto

data class MovieDto (
    val id: Long,
    val kinopoiskId: Long?,
    val title: String,
    val description: String?,
    val year: Int?,
    val rating: Double?,
    val posterUrl: String?,
    val videoUrl: String?,
    val durationSeconds: Int?
)