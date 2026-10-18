package com.example.movietogether.data.remote.dto

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)