package com.intellipaat.course.data.repository

import com.intellipaat.course.data.local.CourseDao
import com.intellipaat.course.data.local.CourseEntity
import com.intellipaat.course.data.local.CourseWithLessons
import com.intellipaat.course.data.local.LessonEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory CourseDao for JVM tests. It copies the conflict rules of the real DAO:
 * courses are upserted, lessons that already exist are ignored.
 */
class FakeCourseDao : CourseDao {

    private val courses = MutableStateFlow<Map<Int, CourseEntity>>(emptyMap())
    private val lessons = MutableStateFlow<Map<Int, LessonEntity>>(emptyMap())

    var saveCoursesCalls = 0
        private set

    override fun observeCourses(): Flow<List<CourseWithLessons>> =
        combine(courses, lessons) { courseMap, lessonMap ->
            courseMap.values.sortedBy { it.id }.map { course ->
                CourseWithLessons(course, lessonMap.values.filter { it.courseId == course.id })
            }
        }

    override fun observeCourse(courseId: Int): Flow<CourseWithLessons?> =
        observeCourses().map { list -> list.find { it.course.id == courseId } }

    override fun observeLessons(courseId: Int): Flow<List<LessonEntity>> =
        lessons.map { lessonMap -> lessonMap.values.filter { it.courseId == courseId }.sortedBy { it.id } }

    override suspend fun upsertCourses(courses: List<CourseEntity>) {
        this.courses.update { current -> current + courses.associateBy { it.id } }
    }

    override suspend fun insertLessons(lessons: List<LessonEntity>) {
        this.lessons.update { current ->
            current + lessons.filter { it.id !in current }.associateBy { it.id }
        }
    }

    override suspend fun saveCourses(courses: List<CourseEntity>, lessons: List<LessonEntity>) {
        saveCoursesCalls++
        super.saveCourses(courses, lessons)
    }

    override suspend fun updateLessonCompleted(lessonId: Int, isCompleted: Boolean) {
        lessons.update { current ->
            val lesson = current[lessonId] ?: return@update current
            current + (lessonId to lesson.copy(isCompleted = isCompleted))
        }
    }

    override suspend fun courseCount(): Int = courses.value.size
}
