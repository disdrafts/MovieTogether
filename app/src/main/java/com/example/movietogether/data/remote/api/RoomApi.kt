package com.example.movietogether.data.remote.api

import com.example.movietogether.data.remote.dto.CreateRoomRequest
import com.example.movietogether.data.remote.dto.RoomDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RoomApi {

    @POST("api/rooms")
    suspend fun createRoom(@Body request: CreateRoomRequest): RoomDto

    @GET("api/rooms/{roomId}")
    suspend fun getRoom(@Path("roomId") roomId: String): RoomDto
}