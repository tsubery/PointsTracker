package com.galtagency.pointstracker

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.regex.Pattern


class PointsTrackerService : NotificationListenerService() {

    private val pointsPattern =
        Pattern.compile("\\((\\+|-)(\\d+) points\\)", Pattern.CASE_INSENSITIVE)
    private var notificationId = 1

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != "com.robinhood.money") {
            return
        }
        val notificationText = sbn.notification.extras.getString(Notification.EXTRA_TEXT, "")
        val points = parsePointsFromNotification(notificationText)
        PointsRepository.addPoints(points)
    }


    fun parsePointsFromNotification(notificationText: String): Int {
        val notificationTextWithoutCommas = notificationText.replace(",", "")
        val matcher = pointsPattern.matcher(notificationTextWithoutCommas)
        if (!matcher.find()) {
            return 0
        }

        val signText = matcher.group(1)
        val sign = (if (signText == "-") -1 else 1)

        return sign * (matcher.group(2)?.toIntOrNull() ?: 0)
    }

}
