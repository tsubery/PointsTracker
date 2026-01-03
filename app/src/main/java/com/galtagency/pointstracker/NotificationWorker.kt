package com.galtagency.pointstracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        PointsRepository.initialize(applicationContext)
        val points = PointsRepository.getPoints()
        val threshold = PointsRepository.getThreshold()

        if (points >= threshold) {
            showThresholdNotification(points)
            PointsRepository.resetPoints()
        }

        return Result.success()
    }

    private fun showThresholdNotification(points: Int) {
        showNotification(
            "Points Threshold Reached!",
            "You've accumulated $points points. Please claim your rewards using the app."
        )
    }

    fun showNotification(title: String, text: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "points_tracker_channel"

        val channel =
            NotificationChannel(channelId, "Points Tracker", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        notificationManager.notify(1, notification)
    }
}
