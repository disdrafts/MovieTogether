package com.example.movietogether.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.movietogether.data.local.dao.MovieDao
import com.example.movietogether.data.local.entity.CachedMovieEntity

@Database(
    entities = [CachedMovieEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}