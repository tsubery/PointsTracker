package com.galtagency.pointstracker.cards

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CardDetectorTest {

    @Test
    fun `detectInstalledCards returns empty when no cards installed`() {
        val packageManager = mockk<PackageManager>()
        every { packageManager.getPackageInfo(any<String>(), any<Int>()) } throws
            PackageManager.NameNotFoundException()

        val detector = CardDetector(packageManager)
        assertTrue(detector.detectInstalledCards().isEmpty())
    }

    @Test
    fun `detectInstalledCards returns only installed cards`() {
        val packageManager = mockk<PackageManager>()
        every { packageManager.getPackageInfo("com.robinhood.money", any<Int>()) } returns PackageInfo()
        every { packageManager.getPackageInfo("com.chase.sig.android", any<Int>()) } throws
            PackageManager.NameNotFoundException()

        val detector = CardDetector(packageManager)
        val installed = detector.detectInstalledCards()
        assertEquals(1, installed.size)
        assertEquals(CardDefinition.Robinhood, installed[0])
    }

    @Test
    fun `detectInstalledCards returns all cards when all installed`() {
        val packageManager = mockk<PackageManager>()
        every { packageManager.getPackageInfo(any<String>(), any<Int>()) } returns PackageInfo()

        val detector = CardDetector(packageManager)
        val installed = detector.detectInstalledCards()
        assertEquals(2, installed.size)
    }
}
