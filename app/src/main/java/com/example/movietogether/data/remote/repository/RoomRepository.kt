package com.example.movietogether.data.remote.repository

import com.example.movietogether.data.remote.api.RoomApi
import com.example.movietogether.data.remote.dto.CreateRoomRequest
import com.example.movietogether.data.remote.dto.RoomDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepository @Inject constructor(
    private val api: RoomApi
) {
    suspend fun createRoom(
        movieId: Long,
        hostUserId: Long,
        hostUsername: String
    ): RoomDto {
        return api.createRoom(
            CreateRoomRequest(
                movieId = movieId,
                hostUserId = hostUserId,
                hostUsername = hostUsername
            )
        )
    }

    suspend fun getRoom(roomId: String): RoomDto {
        return api.getRoom(roomId)
    }
}