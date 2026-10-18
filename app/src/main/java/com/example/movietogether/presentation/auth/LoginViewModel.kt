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

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        val e = email.trim()
        val p = password.trim()

        if (e.isEmpty() || p.isEmpty()) {
            _uiState.value = LoginUiState.Error("Fill all fields")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            _uiState.value = LoginUiState.Error("Invalid email")
            return
        }
        if (p.length < 4) {
            _uiState.value = LoginUiState.Error("Password too short")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val user = authRepository.login(e, p)
                Session.save(user.id, user.username, user.email)
                _uiState.value = LoginUiState.Success
            } catch (ex: Exception) {
                _uiState.value = LoginUiState.Error(ex.message ?: "Login failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}