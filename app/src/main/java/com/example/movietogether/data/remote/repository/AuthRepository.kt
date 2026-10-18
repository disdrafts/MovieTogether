package com.example.movietogether.data.remote.repository

import com.example.movietogether.data.remote.api.AuthApi
import com.example.movietogether.data.remote.dto.AuthResponse
import com.example.movietogether.data.remote.dto.LoginRequest
import com.example.movietogether.data.remote.dto.RegisterRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi
) {
    suspend fun register(username: String, email: String, password: String): AuthResponse {
        return api.register(RegisterRequest(username, email, password))
    }

    suspend fun login(email: String, password: String): AuthResponse {
        return api.login(LoginRequest(email, password))
    }
}