package com.example.movietogether.data.remote.repository

import com.example.movietogether.data.remote.RetrofitClient
import com.example.movietogether.data.remote.dto.KinopoiskMovieDto
import com.example.movietogether.data.remote.dto.MovieDto

class MovieRepository {
    private val api = RetrofitClient.movieApi

    suspend fun getPopularMovies(page: Int = 1): List<KinopoiskMovieDto> {
        return api.getPopular(page)
    }

    suspend fun getTop250(page: Int = 1): List<KinopoiskMovieDto> {
        return api.getTop250(page)
    }

    suspend fun searchMovies(query: String, page: Int = 1): List<KinopoiskMovieDto> {
        return api.search(query, page)
    }

    suspend fun getMyMovies(): List<MovieDto> {
        return api.getMyMovies()
    }

    suspend fun getMovieById(id: Long): MovieDto {
        return api.getMovieById(id)
    }

    suspend fun getByKinopoiskId(kinopoiskId: Long): MovieDto? {
        return try {
            api.getByKinopoiskId(kinopoiskId)
        } catch (e: Exception) {
            null
        }
    }
}