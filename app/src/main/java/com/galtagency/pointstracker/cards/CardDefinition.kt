package com.galtagency.pointstracker.cards

import java.util.regex.Pattern

enum class ValueType { POINTS, DOLLARS }

sealed class CardDefinition(
    val id: String,
    val displayName: String,
    val packageName: String,
    val valueType: ValueType,
    val defaultThreshold: Int,
    protected val notificationPattern: Pattern
) {
    abstract fun parseNotification(text: String): Int

    fun formatValue(value: Int): String = when (valueType) {
        ValueType.POINTS -> {
            java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(value)
        }
        ValueType.DOLLARS -> {
            val dollars = value / 100
            val cents = value % 100
            "$${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(dollars)}.${"%02d".format(cents)}"
        }
    }

    fun valueLabel(): String = when (valueType) {
        ValueType.POINTS -> "points"
        ValueType.DOLLARS -> ""
    }

    data object Robinhood : CardDefinition(
        id = "robinhood",
        displayName = "Robinhood Gold",
        packageName = "com.robinhood.money",
        valueType = ValueType.POINTS,
        defaultThreshold = 10000,
        notificationPattern = Pattern.compile("\\((\\+|-)(\\d+) points\\)", Pattern.CASE_INSENSITIVE)
    ) {
        override fun parseNotification(text: String): Int {
            val textWithoutCommas = text.replace(",", "")
            val matcher = notificationPattern.matcher(textWithoutCommas)
            if (!matcher.find()) return 0

            val signText = matcher.group(1)
            val sign = if (signText == "-") -1 else 1
            return sign * (matcher.group(2)?.toIntOrNull() ?: 0)
        }
    }

    data object Chase : CardDefinition(
        id = "chase",
        displayName = "Chase",
        packageName = "com.chase.sig.android",
        valueType = ValueType.DOLLARS,
        defaultThreshold = 50000,
        notificationPattern = Pattern.compile("\\$(\\d+(?:,\\d{3})*\\.\\d{2})")
    ) {
        override fun parseNotification(text: String): Int {
            val matcher = notificationPattern.matcher(text)
            if (!matcher.find()) return 0

            val amountStr = matcher.group(1)?.replace(",", "") ?: return 0
            val dollars = amountStr.toDoubleOrNull() ?: return 0
            return (dollars * 100).toInt()
        }
    }

    companion object {
        fun all(): List<CardDefinition> = listOf(Robinhood, Chase)
        fun fromPackageName(pkg: String): CardDefinition? = all().find { it.packageName == pkg }
        fun fromId(id: String): CardDefinition? = all().find { it.id == id }
    }
}
