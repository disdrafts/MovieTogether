package com.example.movietogether.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movietogether.data.Session
import com.example.movietogether.data.remote.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RegisterUiState {
    data object Idle : RegisterUiState()
    data object Loading : RegisterUiState()
    data object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(username: String, email: String, password: String) {
        val u = username.trim()
        val e = email.trim()
        val p = password.trim()

        if (u.isEmpty() || e.isEmpty() || p.isEmpty()) {
            _uiState.value = RegisterUiState.Error("Fill all fields")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            _uiState.value = RegisterUiState.Error("Invalid email")
            return
        }
        if (p.length < 4) {
            _uiState.value = RegisterUiState.Error("Password too short")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                val user = authRepository.register(u, e, p)
                Session.save(user.id, user.username, user.email)
                _uiState.value = RegisterUiState.Success
            } catch (ex: Exception) {
                _uiState.value = RegisterUiState.Error(ex.message ?: "Register failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}