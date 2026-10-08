package com.intellipaat.course.domain.model

data class Lesson(
    val id: Int,
    val courseId: Int,
    val title: String,
    val isCompleted: Boolean,
)
