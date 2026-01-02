package com.galtagency.pointstracker


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object PointsRepository {


    private var sharedPreferences: SharedPreferences? = null

    private const val KEY_POINTS = "total_points"
    private const val KEY_THRESHOLD = "points_threshold"
    private const val DEFAULT_THRESHOLD = 10000

    private val _points = MutableStateFlow(0)
    val points = _points.asStateFlow()

    private val _threshold = MutableStateFlow(DEFAULT_THRESHOLD)
    val threshold = _threshold.asStateFlow()

    // The synchronized block ensures thread-safety during initialization
    fun initialize(context: Context) {
        synchronized(this) {
            if (sharedPreferences == null) {
                sharedPreferences = context.applicationContext.getSharedPreferences(
                    "points_tracker", Context.MODE_PRIVATE
                )
                loadInitialValues()
                registerListener()
            }
        }
    }

    private fun loadInitialValues() {
        val prefs = getPrefs() // Use a helper to get a non-null reference
        _points.value = prefs.getInt(KEY_POINTS, 0)
        _threshold.value = prefs.getInt(KEY_THRESHOLD, DEFAULT_THRESHOLD)
    }

    private fun registerListener() {
        getPrefs().registerOnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_POINTS -> _points.value = getPrefs().getInt(KEY_POINTS, 0)
                KEY_THRESHOLD -> _threshold.value =
                    getPrefs().getInt(KEY_THRESHOLD, DEFAULT_THRESHOLD)
            }
        }
    }

    private fun getPrefs(): SharedPreferences {
        return sharedPreferences
            ?: throw IllegalStateException("PointsRepository must be initialized")
    }

    fun getPoints(): Int {
        return _points.value
    }

    fun addPoints(newPoints: Int): Int {
        val newTotal = getPoints() + newPoints
        setPoints(newTotal)
        return newTotal
    }

    fun resetPoints() {
        setPoints(0)
    }


    fun getThreshold(): Int {
        return _threshold.value
    }

    fun setThreshold(newThreshold: Int) {
        _threshold.value = newThreshold
        getPrefs().edit {
            putInt(KEY_THRESHOLD, newThreshold)
        }
    }

    fun setPoints(newPoints: Int) {
        _points.value = newPoints
        getPrefs().edit {
            putInt(KEY_POINTS, newPoints)
        }
    }
}
