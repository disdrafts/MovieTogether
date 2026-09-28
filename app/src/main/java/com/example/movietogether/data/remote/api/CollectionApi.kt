package com.example.movietogether.data.remote.api

import com.example.movietogether.data.remote.dto.AddMovieRequest
import com.example.movietogether.data.remote.dto.CollectionDto
import com.example.movietogether.data.remote.dto.CreateCollectionRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CollectionApi {
    @GET("api/collections/user/{userId}")
    suspend fun getUserCollections(@Path("userId") userId: Long): List<CollectionDto>

    @POST("api/collections")
    suspend fun createCollection(@Body request: CreateCollectionRequest): CollectionDto

    @GET("api/collections/{id}")
    suspend fun getCollection(@Path("id") id: Long): CollectionDto

    @POST("api/collections/{collectionId}/movies")
    suspend fun addMovieToCollection(
        @Path("collectionId") collectionId: Long,
        @Body request: AddMovieRequest
    ): CollectionDto
}