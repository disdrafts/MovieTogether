package com.example.movietogether.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movietogether.data.remote.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadPopular()
    }

    fun onQueryChanged(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            val q = query.trim()
            if (q.length < 2) {
                loadPopular()
            } else {
                search(q)
            }
        }
    }

    fun onQuerySubmit(query: String) {
        searchJob?.cancel()
        val q = query.trim()
        if (q.length < 2) {
            loadPopular()
        } else {
            search(q)
        }
    }

    private fun loadPopular() {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val movies = repository.getPopularMovies()
                _uiState.value = SearchUiState.Success(movies, isEmptyQuery = true)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Error")
            }
        }
    }

    private fun search(query: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val movies = repository.searchMovies(query)
                _uiState.value = SearchUiState.Success(movies, isEmptyQuery = false)
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Error")
            }
        }
    }
}