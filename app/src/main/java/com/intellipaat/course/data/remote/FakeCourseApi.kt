package com.intellipaat.course.data.remote

import kotlinx.coroutines.delay
import java.io.IOException

class FakeCourseApi(
    private val isOnline: () -> Boolean,
    private val delayMillis: Long = 1_000,
) {

    suspend fun fetchCourses(): List<CourseDto> {
        // Delay first so the loading state is visible even when the request fails.
        delay(delayMillis)
        if (!isOnline()) throw IOException("No internet connection")
        return sampleCourses
    }

    private val sampleCourses = listOf(
        course(1, "Python Programming", "John Smith", totalLessons = 20, completedLessons = 13),
        course(2, "Generative AI", "Sarah Williams", totalLessons = 16, completedLessons = 6),
        course(3, "Full Stack Development", "David Brown", totalLessons = 28, completedLessons = 7),
    )

    private fun course(
        id: Int,
        title: String,
        instructor: String,
        totalLessons: Int,
        completedLessons: Int,
    ) = CourseDto(
        id = id,
        title = title,
        instructor = instructor,
        lessons = (1..totalLessons).map { index ->
            LessonDto(
                // Stable IDs: the same lesson always gets the same ID on every fetch.
                id = id * 1000 + index,
                title = "Lesson $index",
                isCompleted = index <= completedLessons,
            )
        },
    )
}
