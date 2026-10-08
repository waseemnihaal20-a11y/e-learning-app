package com.intellipaat.course.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intellipaat.course.domain.model.Course
import com.intellipaat.course.domain.model.Lesson
import com.intellipaat.course.ui.AppViewModelProvider

@Composable
fun CourseDetailsRoute(
    courseId: Int,
    onBack: () -> Unit,
    viewModel: CourseDetailsViewModel = viewModel(factory = AppViewModelProvider.courseDetailsFactory(courseId)),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CourseDetailsScreen(
        state = state,
        onBack = onBack,
        onMarkCompleted = viewModel::markLessonCompleted,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailsScreen(
    state: CourseDetailsUiState,
    onBack: () -> Unit,
    onMarkCompleted: (lessonId: Int) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (state) {
                CourseDetailsUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                CourseDetailsUiState.NotFound -> Text("Course not found.", Modifier.align(Alignment.Center))

                is CourseDetailsUiState.Success -> LazyColumn {
                    item { CourseHeader(state.course) }
                    items(state.course.lessons, key = { it.id }) { lesson ->
                        LessonRow(lesson = lesson, onMarkCompleted = { onMarkCompleted(lesson.id) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseHeader(course: Course) {
    val completedCount = course.lessons.count { it.isCompleted }
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(course.title, style = MaterialTheme.typography.headlineSmall)
        Text("${course.progressPercent}% complete · $completedCount of ${course.lessonCount} lessons")
        LinearProgressIndicator(
            progress = { course.progressPercent / 100f },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun LessonRow(lesson: Lesson, onMarkCompleted: () -> Unit) {
    ListItem(
        headlineContent = { Text(lesson.title) },
        supportingContent = {
            if (lesson.isCompleted) {
                Text("✓ Completed", color = MaterialTheme.colorScheme.primary)
            } else {
                Text("Pending")
            }
        },
        trailingContent = {
            if (!lesson.isCompleted) {
                OutlinedButton(onClick = onMarkCompleted) { Text("Mark completed") }
            }
        },
    )
}
