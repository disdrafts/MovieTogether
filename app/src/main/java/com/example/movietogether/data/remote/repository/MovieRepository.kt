package com.example.movietogether.data.remote.repository

import com.example.movietogether.data.local.dao.MovieDao
import com.example.movietogether.data.local.entity.CachedMovieEntity
import com.example.movietogether.data.remote.api.MovieApi
import com.example.movietogether.data.remote.dto.KinopoiskMovieDto
import com.example.movietogether.data.remote.dto.MovieDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieRepository @Inject constructor(
    private val api: MovieApi,
    private val movieDao: MovieDao
) {
    suspend fun getPopularMovies(page: Int = 1): List<KinopoiskMovieDto> {
        return loadWithCache("popular") {
            api.getPopular(page)
        }
    }

    suspend fun getTop250(page: Int = 1): List<KinopoiskMovieDto> {
        return loadWithCache("top250") {
            api.getTop250(page)
        }
    }

    private suspend fun loadWithCache(
        type: String,
        network: suspend () -> List<KinopoiskMovieDto>
    ): List<KinopoiskMovieDto> {
        return try {
            val remote = network()
            movieDao.clearType(type)
            movieDao.insertAll(remote.map { it.toEntity(type) })
            remote
        } catch (e: Exception) {
            val cached = movieDao.getByType(type)
            if (cached.isNotEmpty()) {
                cached.map { it.toDto() }
            } else {
                throw e
            }
        }
    }

    suspend fun searchMovies(query: String, page: Int = 1): List<KinopoiskMovieDto> {
        return api.search(query, page)
    }

    suspend fun getMyMovies(): List<MovieDto> = api.getMyMovies()

    suspend fun getMovieById(id: Long): MovieDto = api.getMovieById(id)

    suspend fun getByKinopoiskId(kinopoiskId: Long): MovieDto? {
        return try {
            api.getByKinopoiskId(kinopoiskId)
        } catch (e: Exception) {
            null
        }
    }

    private fun KinopoiskMovieDto.toEntity(type: String) = CachedMovieEntity(
        id = id,
        title = title,
        overview = overview,
        year = year,
        rating = rating,
        posterUrl = posterUrl,
        listType = type
    )

    private fun CachedMovieEntity.toDto() = KinopoiskMovieDto(
        id = id,
        title = title,
        overview = overview,
        year = year,
        rating = rating,
        posterUrl = posterUrl
    )
}