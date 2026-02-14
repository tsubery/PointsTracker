package com.galtagency.pointstracker

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.galtagency.pointstracker.cards.CardDefinition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PointsRepository {

    private var sharedPreferences: SharedPreferences? = null

    private const val PREFS_NAME = "points_tracker"

    private fun valueKey(cardId: String) = "${cardId}_value"
    private fun thresholdKey(cardId: String) = "${cardId}_threshold"

    private val _values = mutableMapOf<String, MutableStateFlow<Int>>()
    private val _thresholds = mutableMapOf<String, MutableStateFlow<Int>>()

    fun initialize(context: Context) {
        synchronized(this) {
            if (sharedPreferences == null) {
                sharedPreferences = context.applicationContext.getSharedPreferences(
                    PREFS_NAME, Context.MODE_PRIVATE
                )
                loadInitialValues()
                registerListener()
            }
        }
    }

    private fun loadInitialValues() {
        val prefs = getPrefs()
        for (card in CardDefinition.all()) {
            val value = prefs.getInt(valueKey(card.id), 0)
            val threshold = prefs.getInt(thresholdKey(card.id), card.defaultThreshold)
            _values[card.id] = MutableStateFlow(value)
            _thresholds[card.id] = MutableStateFlow(threshold)
        }
    }

    private fun registerListener() {
        getPrefs().registerOnSharedPreferenceChangeListener { prefs, key ->
            if (key == null) return@registerOnSharedPreferenceChangeListener
            for (card in CardDefinition.all()) {
                when (key) {
                    valueKey(card.id) -> {
                        _values[card.id]?.value = prefs.getInt(key, 0)
                    }
                    thresholdKey(card.id) -> {
                        _thresholds[card.id]?.value = prefs.getInt(key, card.defaultThreshold)
                    }
                }
            }
        }
    }

    private fun getPrefs(): SharedPreferences {
        return sharedPreferences
            ?: throw IllegalStateException("PointsRepository must be initialized")
    }

    fun getCardValue(cardId: String): StateFlow<Int> {
        return (_values[cardId] ?: MutableStateFlow(0)).asStateFlow()
    }

    fun getCardValueInt(cardId: String): Int {
        return _values[cardId]?.value ?: 0
    }

    fun addCardValue(cardId: String, amount: Int): Int {
        synchronized(this) {
            val newTotal = getCardValueInt(cardId) + amount
            setCardValue(cardId, newTotal)
            return newTotal
        }
    }

    fun setCardValue(cardId: String, value: Int) {
        synchronized(this) {
            _values[cardId]?.let { it.value = value }
            getPrefs().edit {
                putInt(valueKey(cardId), value)
            }
        }
    }

    fun resetCard(cardId: String) {
        setCardValue(cardId, 0)
    }

    fun getCardThreshold(cardId: String): StateFlow<Int> {
        return (_thresholds[cardId]
            ?: MutableStateFlow(CardDefinition.fromId(cardId)?.defaultThreshold ?: 0)).asStateFlow()
    }

    fun getCardThresholdInt(cardId: String): Int {
        return _thresholds[cardId]?.value
            ?: CardDefinition.fromId(cardId)?.defaultThreshold ?: 0
    }

    fun setCardThreshold(cardId: String, threshold: Int) {
        synchronized(this) {
            _thresholds[cardId]?.let { it.value = threshold }
            getPrefs().edit {
                putInt(thresholdKey(cardId), threshold)
            }
        }
    }
}
