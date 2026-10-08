package com.intellipaat.course.domain.model

data class Course(
    val id: Int,
    val title: String,
    val instructor: String,
    val lessons: List<Lesson>,
) {
    val lessonCount: Int get() = lessons.size
    val progressPercent: Int get() = calculateProgress(lessons)
}

/**
 * Progress is derived from lesson state, never stored.
 * Uses integer division, so it only reaches 100 when every lesson is completed.
 */
fun calculateProgress(lessons: List<Lesson>): Int {
    if (lessons.isEmpty()) return 0
    val completed = lessons.count { it.isCompleted }
    return completed * 100 / lessons.size
}
