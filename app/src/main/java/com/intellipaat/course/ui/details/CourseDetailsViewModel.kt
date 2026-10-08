package com.intellipaat.course.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intellipaat.course.data.repository.CourseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CourseDetailsViewModel(
    courseId: Int,
    private val repository: CourseRepository,
) : ViewModel() {

    val uiState: StateFlow<CourseDetailsUiState> = repository.observeCourse(courseId)
        .map { course ->
            if (course == null) CourseDetailsUiState.NotFound else CourseDetailsUiState.Success(course)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CourseDetailsUiState.Loading)

    /** Writes to Room only. The Room Flow then updates this screen and the dashboard. */
    fun markLessonCompleted(lessonId: Int) {
        viewModelScope.launch {
            repository.markLessonCompleted(lessonId)
        }
    }
}
