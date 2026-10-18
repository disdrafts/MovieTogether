package com.example.movietogether.presentation.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movietogether.data.Session
import com.example.movietogether.data.remote.dto.KinopoiskMovieDto
import com.example.movietogether.data.remote.dto.MovieDto
import com.example.movietogether.data.remote.repository.CollectionRepository
import com.example.movietogether.data.remote.repository.MovieRepository
import com.example.movietogether.data.remote.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val collectionRepository: CollectionRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MovieDetailsUiState())
    val uiState: StateFlow<MovieDetailsUiState> = _uiState.asStateFlow()

    fun initFromKinopoisk(movie: KinopoiskMovieDto) {
        setArgs(
            MovieDetailsArgs(
                kinopoiskId = movie.id,
                title = movie.title,
                overview = movie.overview,
                year = movie.year,
                rating = movie.rating,
                posterUrl = movie.posterUrl
            )
        )
    }

    fun initFromLocal(movie: MovieDto) {
        setArgs(
            MovieDetailsArgs(
                kinopoiskId = movie.kinopoiskId ?: movie.id,
                title = movie.title,
                overview = movie.description,
                year = movie.year,
                rating = movie.rating,
                posterUrl = movie.posterUrl
            )
        )
    }

    fun initFromArgs(
        kinopoiskId: Long,
        title: String,
        overview: String?,
        year: Int?,
        rating: Double?,
        posterUrl: String?
    ) {
        setArgs(
            MovieDetailsArgs(
                kinopoiskId = kinopoiskId,
                title = title,
                overview = overview,
                year = year,
                rating = rating,
                posterUrl = posterUrl
            )
        )
    }

    private fun setArgs(args: MovieDetailsArgs) {
        _uiState.update { it.copy(args = args) }
        loadLocalMovie(args.kinopoiskId)
    }

    private fun loadLocalMovie(kinopoiskId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTrailer = true) }
            try {
                val local = movieRepository.getByKinopoiskId(kinopoiskId)
                _uiState.update {
                    it.copy(
                        localMovie = local,
                        hasTrailer = !local?.videoUrl.isNullOrBlank(),
                        isLoadingTrailer = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        localMovie = null,
                        hasTrailer = false,
                        isLoadingTrailer = false
                    )
                }
            }
        }
    }

    fun onAddToListClick() {
        val localId = _uiState.value.localMovie?.id
        if (localId == null) {
            _uiState.update {
                it.copy(event = MovieDetailsEvent.Error("Movie is not on server"))
            }
            return
        }

        viewModelScope.launch {
            try {
                val collections = collectionRepository.getUserCollections(Session.userId)
                if (collections.isEmpty()) {
                    _uiState.update {
                        it.copy(event = MovieDetailsEvent.Error("Create a collection first"))
                    }
                } else {
                    _uiState.update {
                        it.copy(event = MovieDetailsEvent.ShowCollections(collections, localId))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(event = MovieDetailsEvent.Error(e.message ?: "Error"))
                }
            }
        }
    }

    fun addToCollection(collectionId: Long, movieId: Long) {
        viewModelScope.launch {
            try {
                collectionRepository.addMovieToCollection(collectionId, movieId)
                _uiState.update {
                    it.copy(event = MovieDetailsEvent.Info("Added to list"))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(event = MovieDetailsEvent.Error(e.message ?: "Error"))
                }
            }
        }
    }

    fun createRoom() {
        val localId = _uiState.value.localMovie?.id
        if (localId == null) {
            _uiState.update {
                it.copy(event = MovieDetailsEvent.Error("Movie is not on server"))
            }
            return
        }

        viewModelScope.launch {
            try {
                val room = roomRepository.createRoom(
                    movieId = localId,
                    hostUserId = Session.userId,
                    hostUsername = Session.username
                )
                _uiState.update {
                    it.copy(event = MovieDetailsEvent.RoomCreated(room))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(event = MovieDetailsEvent.Error(e.message ?: "Error"))
                }
            }
        }
    }

    fun consumeEvent() {
        _uiState.update { it.copy(event = MovieDetailsEvent.Idle) }
    }
}