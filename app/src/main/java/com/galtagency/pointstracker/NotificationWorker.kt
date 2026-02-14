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
import com.galtagency.pointstracker.cards.CardDefinition

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val TAG = "NotificationWorker"

    override suspend fun doWork(): Result {
        Log.v(TAG, "doWork started")

        for (card in CardDefinition.all()) {
            val value = PointsRepository.getCardValueInt(card.id)
            val threshold = PointsRepository.getCardThresholdInt(card.id)
            Log.v(TAG, "${card.displayName}: value=$value, threshold=$threshold")

            if (value >= threshold) {
                Log.v(TAG, "${card.displayName} value ($value) >= threshold ($threshold). Showing notification.")
                showThresholdNotification(card, value)
            }
        }

        Log.v(TAG, "doWork finished")
        return Result.success()
    }

    private fun showThresholdNotification(card: CardDefinition, value: Int) {
        val formattedValue = card.formatValue(value)
        val label = card.valueLabel()
        val text = if (label.isNotEmpty()) {
            "You've accumulated $formattedValue $label. Please claim your rewards."
        } else {
            "You've accumulated $formattedValue. Please claim your rewards."
        }
        showNotification(
            card = card,
            title = "${card.displayName} Threshold Reached!",
            text = text
        )
    }

    private fun showNotification(card: CardDefinition, title: String, text: String) {
        Log.v(TAG, "showNotification: title='$title', text='$text'")
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "points_tracker_channel"

        val channel =
            NotificationChannel(channelId, "Points Tracker", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(applicationContext, LaunchTrampolineActivity::class.java).apply {
            putExtra(EXTRA_CARD_ID, card.id)
        }
        val requestCode = card.id.hashCode()
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            requestCode,
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

        val notificationId = card.id.hashCode()
        notificationManager.notify(notificationId, notification)
        Log.v(TAG, "Notification sent for ${card.displayName}.")
    }
}
