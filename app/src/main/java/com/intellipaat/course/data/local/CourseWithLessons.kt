package com.intellipaat.course.data.local

import androidx.room.Embedded
import androidx.room.Relation

/** One course row plus all of its lesson rows, loaded together by Room. */
data class CourseWithLessons(
    @Embedded val course: CourseEntity,
    @Relation(parentColumn = "id", entityColumn = "courseId")
    val lessons: List<LessonEntity>,
)
