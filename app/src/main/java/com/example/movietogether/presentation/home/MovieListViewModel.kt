package com.example.movietogether.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movietogether.data.remote.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieListViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MovieListUiState>(MovieListUiState.Loading)
    val uiState: StateFlow<MovieListUiState> = _uiState.asStateFlow()

    fun load(type: String) {
        viewModelScope.launch {
            _uiState.value = MovieListUiState.Loading
            try {
                val movies = when (type) {
                    MovieListFragment.TYPE_TOP250 -> repository.getTop250()
                    else -> repository.getPopularMovies()
                }
                _uiState.value = MovieListUiState.Success(movies, type)
            } catch (e: Exception) {
                _uiState.value = MovieListUiState.Error(e.message ?: "Error")
            }
        }
    }
}