package com.example.movietogether.data.remote.dto

data class RoomDto(
    val roomId: String,
    val movieId: Long,
    val hostUserId: Long,
    val hostUsername: String
)