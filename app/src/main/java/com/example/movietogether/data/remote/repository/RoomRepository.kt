package com.example.movietogether.data.remote.repository

import com.example.movietogether.data.remote.RetrofitClient
import com.example.movietogether.data.remote.dto.CreateRoomRequest
import com.example.movietogether.data.remote.dto.RoomDto

class RoomRepository {

    private val api = RetrofitClient.roomApi

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