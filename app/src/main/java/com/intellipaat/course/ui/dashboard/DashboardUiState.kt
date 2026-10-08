package com.intellipaat.course.ui.dashboard

import com.intellipaat.course.domain.model.Course

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Empty : DashboardUiState

    /** [message] is a one-time, non-blocking message (Snackbar), e.g. a failed refresh. */
    data class Success(val courses: List<Course>, val message: String? = null) : DashboardUiState

    data class Error(val message: String) : DashboardUiState
}
