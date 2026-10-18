package com.example.movietogether.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_movies")
data class CachedMovieEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val overview: String?,
    val year: Int?,
    val rating: Double?,
    val posterUrl: String?,
    val listType: String
)