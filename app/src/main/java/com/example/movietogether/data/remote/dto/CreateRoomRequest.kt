package com.example.movietogether.data.remote.dto

data class CreateRoomRequest(
    val movieId: Long,
    val hostUserId: Long,
    val hostUsername: String
)