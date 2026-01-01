package com.galtagency.pointstracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import java.util.regex.Pattern
import androidx.core.content.edit
import com.galtagency.pointstrackerimport.PointsRepository

class PointsTrackerService : NotificationListenerService() {

    private val pointsPattern = Pattern.compile("(\\d+) points")
    private val threshold = 1000 // Configurable threshold

    override fun onCreate() {
        super.onCreate()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.robinhood.money") {
            return
        }
        val notificationText = sbn.notification.extras.getString(Notification.EXTRA_TEXT, "")
        val matcher = pointsPattern.matcher(notificationText)
        if (!matcher.find()) {
            return
        }
        val points = matcher.group(1)?.toIntOrNull() ?: 0
        val currentPoints = PointsRepository.addPoints(points);
        if (currentPoints >= threshold) {
            showThresholdNotification(currentPoints)
            PointsRepository.resetPoints()
        }
    }

    private fun showThresholdNotification(totalPoints: Int) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "points_tracker_channel"

        val channel =
            NotificationChannel(channelId, "Points Tracker", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Points Threshold Reached!")
            .setContentText("You've accumulated $totalPoints points.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        notificationManager.notify(1, notification)
    }
}
