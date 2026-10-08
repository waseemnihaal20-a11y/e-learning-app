package com.intellipaat.course.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Transaction
    @Query("SELECT * FROM courses ORDER BY id")
    fun observeCourses(): Flow<List<CourseWithLessons>>

    @Transaction
    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun observeCourse(courseId: Int): Flow<CourseWithLessons?>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY id")
    fun observeLessons(courseId: Int): Flow<List<LessonEntity>>

    // Upsert updates existing rows in place. REPLACE would delete and re-insert
    // the course, and the CASCADE foreign key would wipe its lessons.
    @Upsert
    suspend fun upsertCourses(courses: List<CourseEntity>)

    // IGNORE keeps existing lessons untouched, so a refresh never resets
    // a lesson the user already completed.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Transaction
    suspend fun saveCourses(courses: List<CourseEntity>, lessons: List<LessonEntity>) {
        upsertCourses(courses)
        insertLessons(lessons)
    }

    @Query("UPDATE lessons SET isCompleted = :isCompleted WHERE id = :lessonId")
    suspend fun updateLessonCompleted(lessonId: Int, isCompleted: Boolean)

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun courseCount(): Int
}
