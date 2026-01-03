package com.galtagency.pointstracker

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PointsApplication : Application(), Configuration.Provider {

    private val TAG = "PointsApplication"


    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        Log.v(TAG, "onCreate called.")
        scheduleDailyNotificationWorker()
    }

    private fun scheduleDailyNotificationWorker() {
        Log.v(TAG, "Scheduling daily notification worker.")
        // Calculate the time until the desired start time (e.g., 11:30:20)
        val now = Calendar.getInstance()
        val desiredTimeOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)

            if (before(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val initialDelay = desiredTimeOfDay.timeInMillis - now.timeInMillis
        Log.v(TAG, "Initial delay for worker: $initialDelay ms")

        val dailyWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "daily_notification_worker",
            ExistingPeriodicWorkPolicy.REPLACE,
            dailyWorkRequest
        )
        Log.v(TAG, "Daily notification worker enqueued.")
    }
}
