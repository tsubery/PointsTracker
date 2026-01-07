package com.galtagency.pointstracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val TAG = "NotificationWorker"

    override suspend fun doWork(): Result {
        Log.v(TAG, "doWork started")

        val points = PointsRepository.getPoints()
        val threshold = PointsRepository.getThreshold()
        Log.v(TAG, "Current points: $points, Threshold: $threshold")

        if (points >= threshold) {
            Log.v(TAG, "Points ($points) >= threshold ($threshold). Showing notification.")
            showThresholdNotification(points)
        } else {
            Log.v(TAG, "Points ($points) < threshold ($threshold). No notification will be shown.")
        }

        Log.v(TAG, "doWork finished")
        return Result.success()
    }

    private fun showThresholdNotification(points: Int) {
        showNotification(
            "Points Threshold Reached!",
            "You've accumulated $points points. Please claim your rewards using the app."
        )
    }

    fun showNotification(title: String, text: String) {
        Log.v(TAG, "showNotification: title='$title', text='$text'")
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "points_tracker_channel"

        val channel =
            NotificationChannel(channelId, "Points Tracker", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)
        Log.v(TAG, "Notification channel created.")

        val intent = Intent(applicationContext, LaunchTrampolineActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        Log.v(TAG, "Notification built. Notifying...")
        notificationManager.notify(1, notification)
        Log.v(TAG, "Notification sent.")
    }
}
