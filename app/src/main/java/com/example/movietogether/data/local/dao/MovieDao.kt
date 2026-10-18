package com.example.movietogether.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.movietogether.data.local.entity.CachedMovieEntity

@Dao
interface MovieDao {

    @Query("SELECT * FROM cached_movies WHERE listType = :type")
    suspend fun getByType(type: String): List<CachedMovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<CachedMovieEntity>)

    @Query("DELETE FROM cached_movies WHERE listType = :type")
    suspend fun clearType(type: String)
}