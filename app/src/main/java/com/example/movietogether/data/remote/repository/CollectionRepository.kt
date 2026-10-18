package com.example.movietogether.data.remote.repository

import com.example.movietogether.data.remote.api.CollectionApi
import com.example.movietogether.data.remote.dto.AddMovieRequest
import com.example.movietogether.data.remote.dto.CollectionDto
import com.example.movietogether.data.remote.dto.CreateCollectionRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepository @Inject constructor(
    private val api: CollectionApi
) {
    suspend fun getUserCollections(userId: Long): List<CollectionDto> {
        return api.getUserCollections(userId)
    }

    suspend fun createCollection(name: String, userId: Long): CollectionDto {
        return api.createCollection(CreateCollectionRequest(name, userId))
    }

    suspend fun getCollection(id: Long): CollectionDto {
        return api.getCollection(id)
    }

    suspend fun addMovieToCollection(collectionId: Long, movieId: Long): CollectionDto {
        return api.addMovieToCollection(
            collectionId = collectionId,
            request = AddMovieRequest(movieId = movieId)
        )
    }
}