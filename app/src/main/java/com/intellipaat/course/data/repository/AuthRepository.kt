package com.intellipaat.course.data.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
}
