package com.example.movietogether.data

object Session {
    var userId: Long = -1L
    var username: String = ""
    var email: String = ""

    val isLoggedIn: Boolean
        get() = userId > 0

    fun save(id: Long, username: String, email: String) {
        this.userId = id
        this.username = username
        this.email = email
    }

    fun clear() {
        userId = -1L
        username = ""
        email = ""
    }
}