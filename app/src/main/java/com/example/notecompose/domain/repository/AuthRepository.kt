package com.example.notecompose.domain.repository

import com.google.firebase.auth.AuthResult

interface AuthRepository {
    suspend fun signUp(email: String, password: String): AuthResult
    suspend fun login(email: String, password: String): AuthResult
    fun logout()
    fun isUserLoggedIn(): Boolean
    suspend fun sendPasswordResetEmail(email: String)
}
