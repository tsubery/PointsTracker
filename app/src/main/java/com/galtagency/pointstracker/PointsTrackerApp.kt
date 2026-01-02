package com.galtagency.pointstracker

import android.app.Application


class PointsTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the repository when the app starts
        PointsRepository.initialize(this)
    }
}