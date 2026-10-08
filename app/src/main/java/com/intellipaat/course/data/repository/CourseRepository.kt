package com.intellipaat.course.data.repository

import com.intellipaat.course.data.local.CourseDao
import com.intellipaat.course.data.local.toDomain
import com.intellipaat.course.data.local.toEntity
import com.intellipaat.course.data.remote.FakeCourseApi
import com.intellipaat.course.domain.model.Course
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room is the single source of truth: the UI only ever observes the database.
 * The API is only used by [refresh] to write fresh data into Room.
 */
class CourseRepository(
    private val api: FakeCourseApi,
    private val dao: CourseDao,
) {

    fun observeCourses(): Flow<List<Course>> =
        dao.observeCourses().map { courses -> courses.map { it.toDomain() } }

    fun observeCourse(courseId: Int): Flow<Course?> =
        dao.observeCourse(courseId).map { it?.toDomain() }

    /** Fetches courses and saves them into Room. Returns how many courses were fetched. */
    suspend fun refresh(): Result<Int> = try {
        val courses = api.fetchCourses()
        dao.saveCourses(
            courses = courses.map { it.toEntity() },
            lessons = courses.flatMap { course -> course.lessons.map { it.toEntity(course.id) } },
        )
        Result.success(courses.size)
    } catch (e: CancellationException) {
        // Never swallow cancellation, or the coroutine keeps running after its scope is gone.
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markLessonCompleted(lessonId: Int) {
        dao.updateLessonCompleted(lessonId, isCompleted = true)
    }
}
