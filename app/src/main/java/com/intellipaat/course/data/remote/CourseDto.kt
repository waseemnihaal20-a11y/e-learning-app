package com.intellipaat.course.data.remote

/** Shape of the course data as the "server" sends it. */
data class CourseDto(
    val id: Int,
    val title: String,
    val instructor: String,
    val lessons: List<LessonDto>,
)

data class LessonDto(
    val id: Int,
    val title: String,
    val isCompleted: Boolean,
)
