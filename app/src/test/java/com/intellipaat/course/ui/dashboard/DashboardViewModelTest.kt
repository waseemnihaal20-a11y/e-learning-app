package com.intellipaat.course.ui.dashboard

import com.intellipaat.course.testutil.FakeCourseRepository
import com.intellipaat.course.testutil.MainDispatcherRule
import com.intellipaat.course.testutil.testCourse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleCourses = listOf(testCourse(1, 20, 13), testCourse(2, 16, 6))

    /** Records every state the screen would see. */
    private fun TestScope.collectStates(viewModel: DashboardViewModel): List<DashboardUiState> {
        val states = mutableListOf<DashboardUiState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.toList(states)
        }
        return states
    }

    @Test
    fun `first load shows Loading and never Empty before the fetch finishes`() = runTest {
        val repository = FakeCourseRepository().apply { coursesFromServer = sampleCourses }
        val viewModel = DashboardViewModel(repository)
        val states = collectStates(viewModel)

        assertEquals(DashboardUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()

        assertEquals(listOf(DashboardUiState.Loading, DashboardUiState.Success(sampleCourses)), states)
        assertFalse(states.any { it is DashboardUiState.Empty })
    }

    @Test
    fun `Empty only after a successful fetch of zero courses`() = runTest {
        val viewModel = DashboardViewModel(FakeCourseRepository())
        val states = collectStates(viewModel)

        advanceUntilIdle()

        assertEquals(listOf(DashboardUiState.Loading, DashboardUiState.Empty), states)
    }

    @Test
    fun `failed fetch with no cached data shows Error`() = runTest {
        val repository = FakeCourseRepository().apply { shouldFail = true }
        val viewModel = DashboardViewModel(repository)
        collectStates(viewModel)

        advanceUntilIdle()

        assertEquals(DashboardUiState.Error("No internet connection"), viewModel.uiState.value)
    }

    @Test
    fun `failed fetch with cached data shows Success with a message, never Error`() = runTest {
        val repository = FakeCourseRepository(cachedCourses = sampleCourses).apply { shouldFail = true }
        val viewModel = DashboardViewModel(repository)
        val states = collectStates(viewModel)

        advanceUntilIdle()

        // Loading is only stateIn's initial value, before Room's first emission.
        assertEquals(
            listOf(
                DashboardUiState.Loading,
                DashboardUiState.Success(sampleCourses),
                DashboardUiState.Success(sampleCourses, message = "No internet connection"),
            ),
            states,
        )
    }

    @Test
    fun `onMessageShown clears the message and keeps the list`() = runTest {
        val repository = FakeCourseRepository(cachedCourses = sampleCourses).apply { shouldFail = true }
        val viewModel = DashboardViewModel(repository)
        collectStates(viewModel)
        advanceUntilIdle()

        viewModel.onMessageShown()

        assertEquals(DashboardUiState.Success(sampleCourses, message = null), viewModel.uiState.value)
    }

    @Test
    fun `retry after a failure shows Loading, then Success without the stale message`() = runTest {
        val repository = FakeCourseRepository().apply { shouldFail = true }
        val viewModel = DashboardViewModel(repository)
        collectStates(viewModel)
        advanceUntilIdle()
        assertEquals(DashboardUiState.Error("No internet connection"), viewModel.uiState.value)

        repository.shouldFail = false
        repository.coursesFromServer = sampleCourses
        viewModel.refresh()

        assertEquals(DashboardUiState.Loading, viewModel.uiState.value)
        advanceUntilIdle()
        assertEquals(DashboardUiState.Success(sampleCourses, message = null), viewModel.uiState.value)
    }
}
