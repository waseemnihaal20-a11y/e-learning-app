package com.intellipaat.course.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intellipaat.course.domain.model.Course
import com.intellipaat.course.ui.AppViewModelProvider

@Composable
fun DashboardRoute(
    onCourseClick: (Int) -> Unit,
    viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.dashboardFactory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        state = state,
        onCourseClick = onCourseClick,
        onRetry = viewModel::refresh,
        onMessageShown = viewModel::onMessageShown,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onCourseClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onMessageShown: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Show a non-blocking message once (e.g. a failed refresh while cached courses are shown).
    val message = (state as? DashboardUiState.Success)?.message
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Courses") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (state) {
                DashboardUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                DashboardUiState.Empty -> Text("No courses available yet.", Modifier.align(Alignment.Center))

                is DashboardUiState.Error -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(state.message)
                    Button(onClick = onRetry) { Text("Retry") }
                }

                is DashboardUiState.Success -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.courses, key = { it.id }) { course ->
                        CourseCard(course = course, onClick = { onCourseClick(course.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseCard(course: Course, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(course.title, style = MaterialTheme.typography.titleMedium)
            Text("by ${course.instructor}", style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(
                progress = { course.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${course.progressPercent}% · ${course.lessonCount} lessons")
                Button(onClick = onClick) { Text("Continue") }
            }
        }
    }
}
