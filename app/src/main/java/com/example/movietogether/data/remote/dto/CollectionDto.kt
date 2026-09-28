package com.example.movietogether.data.remote.dto

data class CollectionDto(
    val id: Long,
    val name: String,
    val userId: Long,
    val movies: List<MovieDto> = emptyList()
)