package com.galtagency.pointstracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.galtagency.pointstracker.cards.CardDefinition

private const val TAG = "LaunchTrampoline"
const val EXTRA_CARD_ID = "card_id"

class LaunchTrampolineActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cardId = intent.getStringExtra(EXTRA_CARD_ID)
        val card = cardId?.let { CardDefinition.fromId(it) }

        if (card != null && launchCardApp(this, card)) {
            PointsRepository.initialize(applicationContext)
            PointsRepository.resetCard(card.id)
            Log.v(TAG, "${card.displayName} value reset.")
        }
        finish()
    }

    private fun launchCardApp(context: Context, card: CardDefinition): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(card.packageName)

        if (launchIntent == null) {
            Log.e(TAG, "Cannot launch ${card.packageName} - app not installed")
            return false
        }

        Log.v(TAG, "Launching ${card.packageName}")
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        try {
            context.startActivity(launchIntent)
            Log.v(TAG, "Successfully launched ${card.packageName}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch ${card.packageName}", e)
            return false
        }
    }
}
