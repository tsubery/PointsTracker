package com.galtagency.pointstracker

import android.app.Notification
import android.app.NotificationChannel
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import java.util.regex.Pattern


class PointsTrackerService : NotificationListenerService() {

    private val pointsPattern = Pattern.compile("\\(\\+(\\d+) points\\)", Pattern.CASE_INSENSITIVE)
    private var notificationId = 1

    override fun onCreate() {
        super.onCreate()
        PointsRepository.initialize(applicationContext)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.robinhood.money") {
            return
        }
        val notificationText = sbn.notification.extras.getString(Notification.EXTRA_TEXT, "")
        val matcher = pointsPattern.matcher(notificationText)
        matcher.find()
        val points = matcher.group(1)?.toIntOrNull() ?: 0
        PointsRepository.addPoints(points)
    }

    fun showNotification(title: String, text: String) {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "points_tracker_channel"

        val channel = NotificationChannel(
            channelId, "Points Tracker", android.app.NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val notification =
            NotificationCompat.Builder(this, channelId).setContentTitle(title).setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info).build()

        notificationManager.notify(notificationId++, notification)
    }

    fun parsePointsFromNotification(notificationText: String): Int {
        val matcher = pointsPattern.matcher(notificationText)
        if (matcher.find()) {
            return matcher.group(1)?.toIntOrNull() ?: 0
        } else {
            return 0
        }
    }

}
