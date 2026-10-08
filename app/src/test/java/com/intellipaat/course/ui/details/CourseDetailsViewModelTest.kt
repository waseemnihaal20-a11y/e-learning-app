package com.intellipaat.course.ui.details

import com.intellipaat.course.testutil.FakeCourseRepository
import com.intellipaat.course.testutil.MainDispatcherRule
import com.intellipaat.course.testutil.testCourse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CourseDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `unknown course id shows NotFound`() = runTest {
        val viewModel = CourseDetailsViewModel(courseId = 99, repository = FakeCourseRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertEquals(CourseDetailsUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun `marking a lesson completed updates lesson status and progress through the Flow`() = runTest {
        val repository = FakeCourseRepository(cachedCourses = listOf(testCourse(1, 20, 13)))
        val viewModel = CourseDetailsViewModel(courseId = 1, repository = repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.markLessonCompleted(1020)

        val course = (viewModel.uiState.value as CourseDetailsUiState.Success).course
        assertTrue(course.lessons.single { it.id == 1020 }.isCompleted)
        assertEquals(70, course.progressPercent)
    }
}
