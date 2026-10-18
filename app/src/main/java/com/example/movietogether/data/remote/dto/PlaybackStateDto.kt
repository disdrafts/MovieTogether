// data/remote/dto/PlaybackStateDto.kt
package com.example.movietogether.data.remote.dto

data class PlaybackStateDto(
    val roomId: String,
    val currentTime: Double,
    val isPlaying: Boolean,
    val username: String? = null
)