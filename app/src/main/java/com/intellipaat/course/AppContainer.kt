package com.intellipaat.course

import android.content.Context
import com.intellipaat.course.data.local.AppDatabase
import com.intellipaat.course.data.remote.ConnectivityChecker
import com.intellipaat.course.data.remote.FakeCourseApi
import com.intellipaat.course.data.repository.AuthRepository
import com.intellipaat.course.data.repository.CourseRepository
import com.intellipaat.course.data.repository.CourseRepositoryImpl
import com.intellipaat.course.data.repository.FakeAuthRepository

/** Manual dependency injection: builds every long-lived object once, for the whole app. */
class AppContainer(context: Context) {

    private val database = AppDatabase.create(context.applicationContext)

    private val connectivityChecker = ConnectivityChecker(context)

    private val courseApi = FakeCourseApi(isOnline = connectivityChecker::isOnline)

    val courseRepository: CourseRepository = CourseRepositoryImpl(courseApi, database.courseDao())

    val authRepository: AuthRepository = FakeAuthRepository()
}
