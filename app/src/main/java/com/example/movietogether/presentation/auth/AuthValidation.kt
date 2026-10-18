package com.example.movietogether.presentation.auth

import android.util.Patterns

object AuthValidation {
    fun isEmailValid(email: String): Boolean =
        Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isPasswordValid(password: String): Boolean =
        password.length >= 6
}