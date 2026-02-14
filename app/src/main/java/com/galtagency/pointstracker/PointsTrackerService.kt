package com.galtagency.pointstracker

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.galtagency.pointstracker.cards.CardDefinition

class PointsTrackerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val card = CardDefinition.fromPackageName(sbn.packageName) ?: return
        val notificationText = extractNotificationText(sbn.notification)
        val value = card.parseNotification(notificationText)
        if (value != 0) {
            PointsRepository.addCardValue(card.id, value)
        }
    }

    private fun extractNotificationText(notification: Notification): String {
        val extras = notification.extras ?: return ""
        val parts = mutableListOf<String>()

        extras.getString(Notification.EXTRA_TITLE)?.let { if (it.isNotBlank()) parts.add(it) }
        extras.getString(Notification.EXTRA_TEXT)?.let { if (it.isNotBlank()) parts.add(it) }
        extras.getString(Notification.EXTRA_BIG_TEXT)?.let { if (it.isNotBlank()) parts.add(it) }
        extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.mapNotNull { it?.toString()?.takeIf(String::isNotBlank) }
            ?.let(parts::addAll)

        return parts.joinToString(" ")
    }
}
