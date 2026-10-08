package com.intellipaat.course.ui.details

import com.intellipaat.course.domain.model.Course

sealed interface CourseDetailsUiState {
    data object Loading : CourseDetailsUiState
    data class Success(val course: Course) : CourseDetailsUiState
    data object NotFound : CourseDetailsUiState
}
