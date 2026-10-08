package com.intellipaat.course.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.intellipaat.course.ui.dashboard.DashboardRoute
import com.intellipaat.course.ui.details.CourseDetailsRoute
import com.intellipaat.course.ui.login.LoginRoute

private object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val COURSE_ID = "courseId"
    const val DETAILS = "details/{$COURSE_ID}"

    fun details(courseId: Int) = "details/$courseId"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginRoute(
                onLoggedIn = {
                    navController.navigate(Routes.DASHBOARD) {
                        // Remove login from the back stack, so Back on the dashboard exits the app.
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardRoute(
                onCourseClick = { courseId ->
                    navController.navigate(Routes.details(courseId)) { launchSingleTop = true }
                },
            )
        }

        composable(
            route = Routes.DETAILS,
            arguments = listOf(navArgument(Routes.COURSE_ID) { type = NavType.IntType }),
        ) { backStackEntry ->
            val courseId = requireNotNull(backStackEntry.arguments).getInt(Routes.COURSE_ID)
            CourseDetailsRoute(
                courseId = courseId,
                // Ignores a second tap while the screen is already leaving, so Back can't pop twice.
                onBack = dropUnlessResumed { navController.popBackStack() },
            )
        }
    }
}
