package com.intellipaat.course.ui.dashboard

import com.intellipaat.course.domain.model.Course

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object Empty : DashboardUiState
    data class Success(val courses: List<Course>) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}
