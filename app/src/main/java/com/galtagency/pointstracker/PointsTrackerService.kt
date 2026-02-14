package com.galtagency.pointstracker

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.galtagency.pointstracker.cards.CardDefinition

class PointsTrackerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val card = CardDefinition.fromPackageName(sbn.packageName) ?: return
        val notificationText = sbn.notification.extras.getString(Notification.EXTRA_TEXT, "")
        val value = card.parseNotification(notificationText)
        if (value != 0) {
            PointsRepository.addCardValue(card.id, value)
        }
    }
}
