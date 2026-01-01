package com.galtagency.pointstracker

import android.app.Application
import com.galtagency.pointstrackerimport.PointsRepository


class PointsTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the repository when the app starts
        PointsRepository.initialize(this)
    }
}