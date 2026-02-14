package com.galtagency.pointstracker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PointsRepositoryInstrumentedTest {

    private lateinit var context: Context

    private fun resetRepository() {
        val field = PointsRepository::class.java.getDeclaredField("sharedPreferences")
        field.isAccessible = true
        field.set(PointsRepository, null)

        // Also clear the internal maps
        val valuesField = PointsRepository::class.java.getDeclaredField("_values")
        valuesField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (valuesField.get(PointsRepository) as MutableMap<*, *>).clear()

        val thresholdsField = PointsRepository::class.java.getDeclaredField("_thresholds")
        thresholdsField.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (thresholdsField.get(PointsRepository) as MutableMap<*, *>).clear()
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("points_tracker", Context.MODE_PRIVATE)
            .edit().clear().commit()
        resetRepository()
        PointsRepository.initialize(context)
    }

    @Test
    fun initializeSetsDefaultValues() {
        assertEquals(0, PointsRepository.getCardValueInt("robinhood"))
        assertEquals(10000, PointsRepository.getCardThresholdInt("robinhood"))
        assertEquals(0, PointsRepository.getCardValueInt("chase"))
        assertEquals(50000, PointsRepository.getCardThresholdInt("chase"))
    }

    @Test
    fun setAndGetCardValue() {
        PointsRepository.setCardValue("robinhood", 500)
        assertEquals(500, PointsRepository.getCardValueInt("robinhood"))
    }

    @Test
    fun addCardValueAccumulates() {
        PointsRepository.setCardValue("robinhood", 100)
        val result = PointsRepository.addCardValue("robinhood", 50)
        assertEquals(150, result)
        assertEquals(150, PointsRepository.getCardValueInt("robinhood"))
    }

    @Test
    fun addCardValueMultipleTimes() {
        PointsRepository.setCardValue("chase", 0)
        PointsRepository.addCardValue("chase", 1000)
        PointsRepository.addCardValue("chase", 2000)
        PointsRepository.addCardValue("chase", 3000)
        assertEquals(6000, PointsRepository.getCardValueInt("chase"))
    }

    @Test
    fun addNegativeCardValue() {
        PointsRepository.setCardValue("robinhood", 100)
        PointsRepository.addCardValue("robinhood", -30)
        assertEquals(70, PointsRepository.getCardValueInt("robinhood"))
    }

    @Test
    fun resetCardSetsToZero() {
        PointsRepository.setCardValue("robinhood", 500)
        PointsRepository.resetCard("robinhood")
        assertEquals(0, PointsRepository.getCardValueInt("robinhood"))
    }

    @Test
    fun setAndGetCardThreshold() {
        PointsRepository.setCardThreshold("robinhood", 5000)
        assertEquals(5000, PointsRepository.getCardThresholdInt("robinhood"))
    }

    @Test
    fun cardsAreIndependent() {
        PointsRepository.setCardValue("robinhood", 100)
        PointsRepository.setCardValue("chase", 200)
        assertEquals(100, PointsRepository.getCardValueInt("robinhood"))
        assertEquals(200, PointsRepository.getCardValueInt("chase"))

        PointsRepository.resetCard("robinhood")
        assertEquals(0, PointsRepository.getCardValueInt("robinhood"))
        assertEquals(200, PointsRepository.getCardValueInt("chase"))
    }

    @Test
    fun cardValueStateFlowUpdates() {
        PointsRepository.setCardValue("robinhood", 0)
        assertEquals(0, PointsRepository.getCardValue("robinhood").value)

        PointsRepository.setCardValue("robinhood", 250)
        assertEquals(250, PointsRepository.getCardValue("robinhood").value)
    }

    @Test
    fun cardThresholdStateFlowUpdates() {
        PointsRepository.setCardThreshold("chase", 50000)
        assertEquals(50000, PointsRepository.getCardThreshold("chase").value)

        PointsRepository.setCardThreshold("chase", 75000)
        assertEquals(75000, PointsRepository.getCardThreshold("chase").value)
    }

    @Test
    fun valuesPersistedAcrossReinitialization() {
        PointsRepository.setCardValue("robinhood", 999)
        PointsRepository.setCardThreshold("robinhood", 2000)
        PointsRepository.setCardValue("chase", 5000)
        PointsRepository.setCardThreshold("chase", 10000)

        resetRepository()
        PointsRepository.initialize(context)

        assertEquals(999, PointsRepository.getCardValueInt("robinhood"))
        assertEquals(2000, PointsRepository.getCardThresholdInt("robinhood"))
        assertEquals(5000, PointsRepository.getCardValueInt("chase"))
        assertEquals(10000, PointsRepository.getCardThresholdInt("chase"))
    }

    @Test
    fun legacyKeysMigratedToRobinhood() {
        // Simulate legacy state: clear everything and write old keys
        context.getSharedPreferences("points_tracker", Context.MODE_PRIVATE)
            .edit().clear()
            .putInt("total_points", 777)
            .putInt("points_threshold", 3000)
            .commit()

        resetRepository()
        PointsRepository.initialize(context)

        assertEquals(777, PointsRepository.getCardValueInt("robinhood"))
        assertEquals(3000, PointsRepository.getCardThresholdInt("robinhood"))

        // Legacy keys should be removed
        val prefs = context.getSharedPreferences("points_tracker", Context.MODE_PRIVATE)
        assertEquals(false, prefs.contains("total_points"))
        assertEquals(false, prefs.contains("points_threshold"))
    }

    @Test
    fun addCardValueReturnsNewTotal() {
        PointsRepository.setCardValue("robinhood", 100)
        val newTotal = PointsRepository.addCardValue("robinhood", 75)
        assertEquals(175, newTotal)
    }

    @Test
    fun setCardValueToZero() {
        PointsRepository.setCardValue("chase", 5000)
        PointsRepository.setCardValue("chase", 0)
        assertEquals(0, PointsRepository.getCardValueInt("chase"))
    }

    @Test
    fun setLargeCardValue() {
        PointsRepository.setCardValue("robinhood", Int.MAX_VALUE)
        assertEquals(Int.MAX_VALUE, PointsRepository.getCardValueInt("robinhood"))
    }
}
