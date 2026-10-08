package com.intellipaat.course.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.intellipaat.course.AppContainer
import com.intellipaat.course.LearningDashboardApp
import com.intellipaat.course.ui.dashboard.DashboardViewModel
import com.intellipaat.course.ui.details.CourseDetailsViewModel
import com.intellipaat.course.ui.login.LoginViewModel

/** Builds every ViewModel from the app-wide [AppContainer]. */
object AppViewModelProvider {

    val loginFactory = viewModelFactory {
        initializer { LoginViewModel(container().authRepository) }
    }

    val dashboardFactory = viewModelFactory {
        initializer { DashboardViewModel(container().courseRepository) }
    }

    fun courseDetailsFactory(courseId: Int) = viewModelFactory {
        initializer { CourseDetailsViewModel(courseId, container().courseRepository) }
    }

    private fun CreationExtras.container(): AppContainer =
        (this[APPLICATION_KEY] as LearningDashboardApp).container
}
