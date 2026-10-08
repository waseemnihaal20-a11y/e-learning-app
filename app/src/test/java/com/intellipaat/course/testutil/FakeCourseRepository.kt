package com.intellipaat.course.testutil

import com.intellipaat.course.data.repository.CourseRepository
import com.intellipaat.course.domain.model.Course
import com.intellipaat.course.domain.model.Lesson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.io.IOException

/**
 * In-memory CourseRepository. [courses] plays the role of Room; a successful
 * refresh "saves" [coursesFromServer] into it, like the real repository does.
 */
class FakeCourseRepository(
    cachedCourses: List<Course> = emptyList(),
) : CourseRepository {

    private val courses = MutableStateFlow(cachedCourses)

    var shouldFail = false
    var coursesFromServer: List<Course> = emptyList()

    override fun observeCourses(): Flow<List<Course>> = courses

    override fun observeCourse(courseId: Int): Flow<Course?> =
        courses.map { list -> list.find { it.id == courseId } }

    override suspend fun refresh(): Result<Int> {
        delay(1_000)
        if (shouldFail) return Result.failure(IOException("No internet connection"))
        courses.value = coursesFromServer
        return Result.success(coursesFromServer.size)
    }

    override suspend fun markLessonCompleted(lessonId: Int) {
        courses.update { list ->
            list.map { course ->
                course.copy(lessons = course.lessons.map { if (it.id == lessonId) it.copy(isCompleted = true) else it })
            }
        }
    }
}

fun testCourse(id: Int, totalLessons: Int, completedLessons: Int) = Course(
    id = id,
    title = "Course $id",
    instructor = "Instructor $id",
    lessons = (1..totalLessons).map { index ->
        Lesson(id = id * 1000 + index, courseId = id, title = "Lesson $index", isCompleted = index <= completedLessons)
    },
)
