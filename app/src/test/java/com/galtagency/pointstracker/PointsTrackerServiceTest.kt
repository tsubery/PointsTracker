package com.galtagency.pointstracker

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern


class PointsTrackerServiceTest {

    private lateinit var service: PointsTrackerService

    @Before
    fun setUp() {
        // Create an instance of the service to test its methods
        service = PointsTrackerService()
    }

    @Test
    fun `parsePointsFromNotification should extract correct points value`() {
        val notificationText = "$20.89 (+63 Points)"
        val expectedPoints = 63
        val actualPoints = service.parsePointsFromNotification(notificationText)
        assertEquals(expectedPoints, actualPoints)
    }

    @Test
    fun `parsePointsFromNotification should return 0 for text without points`() {
        val notificationText = "Welcome to our service. Here's a tip."
        val expectedPoints = 0
        val actualPoints = service.parsePointsFromNotification(notificationText)
        assertEquals(expectedPoints, actualPoints)
    }

    @Test
    fun `parsePointsFromNotification should return 0 for empty string`() {
        val notificationText = ""
        val expectedPoints = 0
        val actualPoints = service.parsePointsFromNotification(notificationText)
        assertEquals(expectedPoints, actualPoints)
    }

    @Test
    fun `parsePointsFromNotification should handle numbers with other text`() {
        val notificationText = "Your daily reward is (+75 points)."
        val expectedPoints = 75
        val actualPoints = service.parsePointsFromNotification(notificationText)
        assertEquals(expectedPoints, actualPoints)
    }

    @Test
    fun `parsePointsFromNotification should handle single-digit points`() {
        val notificationText = "You got (+5 points)."
        val expectedPoints = 5
        val actualPoints = service.parsePointsFromNotification(notificationText)
        assertEquals(expectedPoints, actualPoints)
    }

    @Test
    fun `parsePointsFromNotification should return 0 if only number is present without 'points'`() {
        val notificationText = "Your value is 100."
        val expectedPoints = 0
        val actualPoints = service.parsePointsFromNotification(notificationText)
        assertEquals(expectedPoints, actualPoints)
    }
}
