package com.galtagency.pointstrackerimport

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// A singleton object to ensure there's only one instance of the repository
object PointsRepository {
    private lateinit var sharedPreferences: SharedPreferences
    private val _points = MutableStateFlow(0)
    val points = _points.asStateFlow()

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("points_tracker", Context.MODE_PRIVATE)
        _points.value = sharedPreferences.getInt("total_points", 0)

        // Listen for changes in SharedPreferences to keep the flow updated
        sharedPreferences.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "total_points") {
                _points.value = sharedPreferences.getInt("total_points", 0)
            }
        }
    }

    fun getPoints(): Int {
        return sharedPreferences.getInt("total_points", 0)
    }

    fun addPoints(newPoints: Int): Int {
        val currentPoints = sharedPreferences.getInt("total_points", 0)
        val newTotal = currentPoints + newPoints

        sharedPreferences.edit {
            putInt("total_points", newTotal)
        }
        return newTotal;
    }
    fun resetPoints() {
        sharedPreferences.edit {
            putInt("total_points",0)
        }
    }
}
