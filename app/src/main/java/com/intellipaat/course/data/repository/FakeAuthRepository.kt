package com.intellipaat.course.data.repository

import kotlinx.coroutines.delay

/**
 * Mocked login. Accepts any valid input, except the demo password [DEMO_WRONG_PASSWORD],
 * which always fails so the error state can be shown.
 *
 * A real implementation would call an auth API and store the returned token
 * encrypted on the device (for example DataStore with a key kept in the Android
 * Keystore). This fake stores nothing.
 */
class FakeAuthRepository(
    private val delayMillis: Long = 1_000,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        delay(delayMillis)
        return if (password == DEMO_WRONG_PASSWORD) {
            Result.failure(IllegalArgumentException("Invalid email or password"))
        } else {
            Result.success(Unit)
        }
    }

    companion object {
        const val DEMO_WRONG_PASSWORD = "wrongpass"
    }
}
