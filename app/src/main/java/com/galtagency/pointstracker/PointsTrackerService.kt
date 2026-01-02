package com.galtagency.pointstracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import java.util.regex.Pattern

class PointsTrackerService : NotificationListenerService() {

    private val pointsPattern = Pattern.compile("\\(\\+(\\d+) points\\)", Pattern.CASE_INSENSITIVE)

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
        val text = sbn.notification.extras.getString(Notification.EXTRA_TEXT, "")
        val packageName = sbn.packageName

        if (packageName == "com.robinhood.money") {
            processNotificationText(packageName, text)
        } else {
            //processNotificationText(packageName, "packageName: $packageName is not supported")
        }
    }

    fun processNotificationText(packageName: String, text: String) {
        val newPoints = parsePointsFromNotification(text)
        val currentPoints = PointsRepository.addPoints(newPoints)
        processNotificationText(
            "Points Tracker",
            "New Points: $newPoints, Total Points: $currentPoints"
        )
        val threshold = PointsRepository.getThreshold()
        if (currentPoints >= threshold) {
            showThresholdNotification(currentPoints)
            PointsRepository.resetPoints()
        }
    }

    internal fun parsePointsFromNotification(text: String): Int {
        val matcher = pointsPattern.matcher(text)
        val found = matcher.find()
        val pointsString = matcher.group(1)
        if (found && pointsString != null) {
            return pointsString.toInt()
        }
        return 0
    }

    private fun showThresholdNotification(totalPoints: Int) {
        publishNotification(
            "Points Threshold Reached!", "You've accumulated $totalPoints points."
        )
    }

    private fun publishNotification(title: String, text: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "points_tracker_channel"

        val channel = NotificationChannel(
            channelId, "Points Tracker", NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val notification =
            NotificationCompat.Builder(this, channelId).setContentTitle(title).setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info).build()

        notificationManager.notify(1, notification)
    }
}
