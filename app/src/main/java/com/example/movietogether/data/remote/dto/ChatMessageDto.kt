package com.example.movietogether.data.remote.dto

data class ChatMessageDto(
    val roomId: String,
    val username: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)