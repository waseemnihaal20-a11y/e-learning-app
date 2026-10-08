package com.intellipaat.course.data.local

import com.intellipaat.course.data.remote.CourseDto
import com.intellipaat.course.data.remote.LessonDto
import com.intellipaat.course.domain.model.Course
import com.intellipaat.course.domain.model.Lesson

fun LessonEntity.toDomain(): Lesson = Lesson(
    id = id,
    courseId = courseId,
    title = title,
    isCompleted = isCompleted,
)

fun CourseWithLessons.toDomain(): Course = Course(
    id = course.id,
    title = course.title,
    instructor = course.instructor,
    lessons = lessons.sortedBy { it.id }.map { it.toDomain() },
)

fun Lesson.toEntity(): LessonEntity = LessonEntity(
    id = id,
    courseId = courseId,
    title = title,
    isCompleted = isCompleted,
)

fun Course.toEntity(): CourseEntity = CourseEntity(
    id = id,
    title = title,
    instructor = instructor,
)

fun CourseDto.toEntity(): CourseEntity = CourseEntity(
    id = id,
    title = title,
    instructor = instructor,
)

fun LessonDto.toEntity(courseId: Int): LessonEntity = LessonEntity(
    id = id,
    courseId = courseId,
    title = title,
    isCompleted = isCompleted,
)
