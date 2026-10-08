package com.intellipaat.course.ui.login

import com.intellipaat.course.data.repository.FakeAuthRepository
import com.intellipaat.course.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel() = LoginViewModel(FakeAuthRepository())

    @Test
    fun `no errors are shown before Login is tapped`() {
        val viewModel = createViewModel()

        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("123")

        assertNull(viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.passwordError)
    }

    @Test
    fun `invalid email is rejected`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEmailChange("not-an-email")
        viewModel.onPasswordChange("secret123")

        viewModel.onLoginClick()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Enter a valid email", state.emailError)
        assertNull(state.passwordError)
        assertFalse(state.isLoading)
        assertFalse(state.isLoggedIn)
    }

    @Test
    fun `short password is rejected`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("12345")

        viewModel.onLoginClick()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.emailError)
        assertEquals("Password must be at least 6 characters", state.passwordError)
        assertFalse(state.isLoggedIn)
    }

    @Test
    fun `valid input shows loading, then logs in`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("secret123")

        viewModel.onLoginClick()
        assertTrue(viewModel.uiState.value.isLoading)

        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isLoggedIn)
        assertNull(state.errorMessage)
    }

    @Test
    fun `demo wrong password shows the error state`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange(FakeAuthRepository.DEMO_WRONG_PASSWORD)

        viewModel.onLoginClick()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoggedIn)
        assertNotNull(state.errorMessage)
        assertEquals("Invalid email or password", state.errorMessage)
    }
}
