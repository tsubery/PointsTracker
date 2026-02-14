package com.galtagency.pointstracker.cards

import android.content.pm.PackageManager

class CardDetector(private val packageManager: PackageManager) {
    fun detectInstalledCards(): List<CardDefinition> {
        return CardDefinition.all().filter { card ->
            try {
                packageManager.getPackageInfo(card.packageName, 0)
                true
            } catch (_: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
}
