package com.example.movietogether.presentation.profile

import com.example.movietogether.data.remote.dto.RoomDto

sealed class ProfileUiState {
    data object Idle : ProfileUiState()
    data object Loading : ProfileUiState()
    data class RoomFound(val room: RoomDto) : ProfileUiState()
    data class AvatarUpdated(val path: String?) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}