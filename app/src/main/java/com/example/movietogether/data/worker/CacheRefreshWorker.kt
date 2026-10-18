package com.example.movietogether.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.movietogether.data.remote.repository.MovieRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CacheRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val movieRepository: MovieRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            movieRepository.getPopularMovies()
            movieRepository.getTop250()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}