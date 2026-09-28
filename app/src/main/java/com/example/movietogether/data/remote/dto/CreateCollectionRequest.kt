package com.example.movietogether.data.remote.dto

data class CreateCollectionRequest(
    val name: String,
    val userId: Long
)