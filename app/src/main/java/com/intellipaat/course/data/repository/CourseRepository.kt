package com.intellipaat.course.data.repository

import com.intellipaat.course.domain.model.Course
import kotlinx.coroutines.flow.Flow

/**
 * Room is the single source of truth: the UI only ever observes the database.
 * The API is only used by [refresh] to write fresh data into Room.
 */
interface CourseRepository {

    fun observeCourses(): Flow<List<Course>>

    fun observeCourse(courseId: Int): Flow<Course?>

    /** Fetches courses and saves them into Room. Returns how many courses were fetched. */
    suspend fun refresh(): Result<Int>

    suspend fun markLessonCompleted(lessonId: Int)
}
