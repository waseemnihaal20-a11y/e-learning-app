package com.intellipaat.course.data.repository

import com.intellipaat.course.data.remote.FakeCourseApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class CourseRepositoryTest {

    private var online = true
    private val dao = FakeCourseDao()
    private val repository = CourseRepository(
        api = FakeCourseApi(isOnline = { online }, delayMillis = 0),
        dao = dao,
    )

    // Python Programming seeds lessons 1..13 as completed, so lesson 20 starts pending.
    private val pendingLessonId = 1020

    @Test
    fun `refresh saves all courses in one transaction and returns the count`() = runTest {
        val result = repository.refresh()

        assertEquals(3, result.getOrNull())
        assertEquals(1, dao.saveCoursesCalls)
        assertEquals(listOf(20, 16, 28), repository.observeCourses().first().map { it.lessonCount })
    }

    @Test
    fun `refresh keeps a lesson the user already completed`() = runTest {
        repository.refresh()
        repository.markLessonCompleted(pendingLessonId)

        repository.refresh()

        val python = repository.observeCourse(1).first()!!
        assertTrue(python.lessons.single { it.id == pendingLessonId }.isCompleted)
        assertEquals(70, python.progressPercent) // 14 of 20
    }

    @Test
    fun `refreshing twice creates no duplicate lessons`() = runTest {
        repository.refresh()
        repository.refresh()

        val courses = repository.observeCourses().first()
        val allLessonIds = courses.flatMap { course -> course.lessons.map { it.id } }
        assertEquals(64, allLessonIds.size)
        assertEquals(allLessonIds.size, allLessonIds.toSet().size)
    }

    @Test
    fun `failed refresh leaves cached data untouched`() = runTest {
        repository.refresh()
        repository.markLessonCompleted(pendingLessonId)
        val cached = repository.observeCourses().first()

        online = false
        val result = repository.refresh()

        assertTrue(result.exceptionOrNull() is IOException)
        assertEquals(cached, repository.observeCourses().first())
    }

    @Test
    fun `failed refresh on an empty database returns failure and saves nothing`() = runTest {
        online = false

        val result = repository.refresh()

        assertFalse(result.isSuccess)
        assertTrue(repository.observeCourses().first().isEmpty())
        assertEquals(0, dao.saveCoursesCalls)
    }
}
