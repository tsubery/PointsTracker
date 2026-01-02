package com.galtagency.pointstracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import java.util.regex.Pattern

class PointsTrackerService : NotificationListenerService() {

    private val pointsPattern = Pattern.compile("\\(\\+(\\d+) points\\)", Pattern.CASE_INSENSITIVE)
    private val threshold = 100000 // Configurable threshold

    override fun onCreate() {
        super.onCreate()
    }
    // debugging notifications

    /*
        override fun onListenerConnected() {
            super.onListenerConnected()
            val activeNotifications = this.activeNotifications
            if (activeNotifications != null) {
                for (sbn in activeNotifications) {
                    processNotification(sbn)
                }
            }
        }*/

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        processNotification(sbn)
    }

    fun processNotification(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.robinhood.money") {
            return
        }
        val notificationText = sbn.notification.extras.getString(Notification.EXTRA_TEXT, "")
        val points = parsePointsFromNotification(notificationText)
        val currentPoints = PointsRepository.addPoints(points)
        val threshold = PointsRepository.getThreshold()
        if (currentPoints >= threshold) {
            showThresholdNotification(currentPoints)
            PointsRepository.resetPoints()
        }
    }

    internal fun parsePointsFromNotification(text: String): Int {
        val matcher = pointsPattern.matcher(text)
        if (matcher.find()) {
            return matcher.group(1).toInt()
        }
        return 0
    }

    private fun showThresholdNotification(totalPoints: Int) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "points_tracker_channel"

        val channel =
            NotificationChannel(channelId, "Points Tracker", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val notification =
            NotificationCompat.Builder(this, channelId).setContentTitle("Points Threshold Reached!")
                .setContentText("You've accumulated $totalPoints points.")
                .setSmallIcon(android.R.drawable.ic_dialog_info).build()

        notificationManager.notify(1, notification)
    }
}
