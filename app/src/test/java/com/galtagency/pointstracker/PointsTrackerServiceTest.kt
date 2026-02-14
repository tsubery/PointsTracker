package com.galtagency.pointstracker

import com.galtagency.pointstracker.cards.CardDefinition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PointsTrackerServiceTest {

    @Test
    fun `service routes robinhood package to Robinhood card`() {
        val card = CardDefinition.fromPackageName("com.robinhood.money")
        assertEquals(CardDefinition.Robinhood, card)
    }

    @Test
    fun `service routes chase package to Chase card`() {
        val card = CardDefinition.fromPackageName("com.chase.sig.android")
        assertEquals(CardDefinition.Chase, card)
    }

    @Test
    fun `service ignores unknown packages`() {
        val card = CardDefinition.fromPackageName("com.unknown.app")
        assertNull(card)
    }

    @Test
    fun `robinhood notification parsed correctly via card definition`() {
        val card = CardDefinition.fromPackageName("com.robinhood.money")!!
        assertEquals(63, card.parseNotification("$20.89 (+63 Points)"))
    }

    @Test
    fun `chase notification parsed correctly via card definition`() {
        val card = CardDefinition.fromPackageName("com.chase.sig.android")!!
        assertEquals(2089, card.parseNotification("You made a \$20.89 transaction"))
    }
}
