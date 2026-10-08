package com.intellipaat.course.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intellipaat.course.data.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** What happened to the last fetch. Private: the screen only sees [DashboardUiState]. */
private sealed interface RefreshState {
    data object Running : RefreshState
    data class Succeeded(val fetchedCount: Int) : RefreshState
    data class Failed(val message: String) : RefreshState
}

class DashboardViewModel(
    private val repository: CourseRepository,
) : ViewModel() {

    private val refreshState = MutableStateFlow<RefreshState>(RefreshState.Running)

    // Kept apart from refreshState so dismissing the Snackbar doesn't change the real status.
    private val userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeCourses(),
        refreshState,
        userMessage,
    ) { courses, refresh, message ->
        when {
            // Cached data always wins: a failed refresh never hides the list.
            courses.isNotEmpty() -> DashboardUiState.Success(courses, message)
            refresh is RefreshState.Running -> DashboardUiState.Loading
            refresh is RefreshState.Failed -> DashboardUiState.Error(refresh.message)
            refresh is RefreshState.Succeeded && refresh.fetchedCount == 0 -> DashboardUiState.Empty
            // Fetched some courses, but Room hasn't emitted them yet.
            else -> DashboardUiState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        refreshState.value = RefreshState.Running
        userMessage.value = null
        viewModelScope.launch {
            repository.refresh()
                .onSuccess { count -> refreshState.value = RefreshState.Succeeded(count) }
                .onFailure { error ->
                    val message = error.message ?: "Couldn't load courses"
                    refreshState.value = RefreshState.Failed(message)
                    userMessage.value = message
                }
        }
    }

    fun onMessageShown() {
        userMessage.value = null
    }
}
