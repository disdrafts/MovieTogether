package com.example.movietogether.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movietogether.data.Session
import com.example.movietogether.data.local.AvatarStorage
import com.example.movietogether.data.remote.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {

    val username: String get() = Session.username
    val email: String get() = Session.email

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadAvatar(storage: AvatarStorage) {
        _uiState.value = ProfileUiState.AvatarUpdated(storage.getPath())
    }

    fun saveAvatar(storage: AvatarStorage, uri: Uri) {
        try {
            val path = storage.saveFromUri(uri)
            _uiState.value = ProfileUiState.AvatarUpdated(path)
        } catch (e: Exception) {
            _uiState.value = ProfileUiState.Error(e.message ?: "Cannot save avatar")
        }
    }

    fun joinRoom(roomId: String) {
        val code = roomId.trim()
        if (code.isEmpty()) {
            _uiState.value = ProfileUiState.Error("Enter room code")
            return
        }

        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val room = roomRepository.getRoom(code)
                _uiState.value = ProfileUiState.RoomFound(room)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Room not found")
            }
        }
    }

    fun logout() {
        Session.clear()
    }

    fun resetState() {
        _uiState.value = ProfileUiState.Idle
    }
}