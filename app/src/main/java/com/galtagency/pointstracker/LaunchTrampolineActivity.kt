package com.galtagency.pointstracker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

private const val COM_ROBINHOOD_MONEY = "com.robinhood.money"
private const val TAG = "LaunchTrampoline"

class LaunchTrampolineActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (launchRobinhoodApp(this)) {
            PointsRepository.initialize(applicationContext)
            PointsRepository.resetPoints()
            Log.v(TAG, "Points reset.")
        }
        finish()
    }

    private fun launchRobinhoodApp(context: Context): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(COM_ROBINHOOD_MONEY)

        if (launchIntent == null) {
            Log.e(TAG, "Cannot launch $COM_ROBINHOOD_MONEY - app not installed")
            return false
        }

        Log.v(TAG, "Launching $COM_ROBINHOOD_MONEY")
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        try {
            context.startActivity(launchIntent)
            Log.v(TAG, "Successfully launched $COM_ROBINHOOD_MONEY")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch $COM_ROBINHOOD_MONEY", e)
            return false
        }
    }
}
