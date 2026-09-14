package com.example.movietogether.data.remote.api

import com.example.movietogether.data.remote.dto.KinopoiskMovieDto
import com.example.movietogether.data.remote.dto.MovieDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApi {

    @GET("api/movies")
    suspend fun getMyMovies(): List<MovieDto>

    @GET("api/movies/{id}")
    suspend fun getMovieById(@Path("id") id: Long): MovieDto

    @GET("api/movies/by-kinopoisk/{kinopoiskId}")
    suspend fun getByKinopoiskId(@Path("kinopoiskId") kinopoiskId: Long): MovieDto

    @GET("api/kinopoisk/popular")
    suspend fun getPopular(@Query("page") page: Int = 1): List<KinopoiskMovieDto>

    @GET("api/kinopoisk/top250")
    suspend fun getTop250(@Query("page") page: Int = 1): List<KinopoiskMovieDto>

    @GET("api/kinopoisk/search")
    suspend fun search(@Query("query") query: String, @Query("page") page: Int = 1): List<KinopoiskMovieDto>
}