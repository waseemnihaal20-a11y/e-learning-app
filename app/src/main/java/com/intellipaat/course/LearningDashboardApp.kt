package com.intellipaat.course

import android.app.Application

class LearningDashboardApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
