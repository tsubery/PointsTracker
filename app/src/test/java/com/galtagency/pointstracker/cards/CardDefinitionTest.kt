package com.galtagency.pointstracker.cards

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CardDefinitionTest {

    // --- Robinhood parsing tests (moved from PointsTrackerServiceTest) ---

    @Test
    fun `Robinhood parseNotification extracts correct points value`() {
        assertEquals(63, CardDefinition.Robinhood.parseNotification("$20.89 (+63 Points)"))
    }

    @Test
    fun `Robinhood parseNotification returns 0 for text without points`() {
        assertEquals(0, CardDefinition.Robinhood.parseNotification("Welcome to our service. Here's a tip."))
    }

    @Test
    fun `Robinhood parseNotification returns 0 for empty string`() {
        assertEquals(0, CardDefinition.Robinhood.parseNotification(""))
    }

    @Test
    fun `Robinhood parseNotification handles numbers with other text`() {
        assertEquals(75, CardDefinition.Robinhood.parseNotification("Your daily reward is (+75 points)."))
    }

    @Test
    fun `Robinhood parseNotification handles large number text`() {
        assertEquals(7000005, CardDefinition.Robinhood.parseNotification("Your daily reward is (+7,000005 points)."))
    }

    @Test
    fun `Robinhood parseNotification handles single-digit points`() {
        assertEquals(5, CardDefinition.Robinhood.parseNotification("You got (+5 points)."))
    }

    @Test
    fun `Robinhood parseNotification handles single-digit negative points`() {
        assertEquals(-5, CardDefinition.Robinhood.parseNotification("You got (-5 points)."))
    }

    @Test
    fun `Robinhood parseNotification returns 0 if only number without points`() {
        assertEquals(0, CardDefinition.Robinhood.parseNotification("Your value is 100."))
    }

    @Test
    fun `Robinhood parseNotification is case insensitive`() {
        assertEquals(50, CardDefinition.Robinhood.parseNotification("(+50 POINTS)"))
        assertEquals(50, CardDefinition.Robinhood.parseNotification("(+50 Points)"))
        assertEquals(50, CardDefinition.Robinhood.parseNotification("(+50 points)"))
        assertEquals(50, CardDefinition.Robinhood.parseNotification("(+50 PoInTs)"))
    }

    @Test
    fun `Robinhood parseNotification extracts first match`() {
        assertEquals(100, CardDefinition.Robinhood.parseNotification("(+100 points) and also (+200 points)"))
    }

    @Test
    fun `Robinhood parseNotification handles large positive numbers`() {
        assertEquals(999999, CardDefinition.Robinhood.parseNotification("(+999,999 points)"))
    }

    @Test
    fun `Robinhood parseNotification handles large negative numbers`() {
        assertEquals(-5000000, CardDefinition.Robinhood.parseNotification("(-5,000,000 points)"))
    }

    @Test
    fun `Robinhood parseNotification returns 0 for missing sign`() {
        assertEquals(0, CardDefinition.Robinhood.parseNotification("(100 points)"))
    }

    @Test
    fun `Robinhood parseNotification returns 0 for missing parentheses`() {
        assertEquals(0, CardDefinition.Robinhood.parseNotification("+100 points"))
    }

    @Test
    fun `Robinhood parseNotification handles dollar amount prefix`() {
        assertEquals(150, CardDefinition.Robinhood.parseNotification("\$150.45 (+150 points)"))
    }

    @Test
    fun `Robinhood parseNotification handles commas in large numbers`() {
        assertEquals(1000, CardDefinition.Robinhood.parseNotification("(+1,000 points)"))
    }

    @Test
    fun `Robinhood parseNotification handles multiple commas`() {
        assertEquals(1000000, CardDefinition.Robinhood.parseNotification("(+1,000,000 points)"))
    }

    @Test
    fun `Robinhood parseNotification returns 0 for whitespace`() {
        assertEquals(0, CardDefinition.Robinhood.parseNotification("   "))
        assertEquals(0, CardDefinition.Robinhood.parseNotification("\n"))
        assertEquals(0, CardDefinition.Robinhood.parseNotification("\t"))
    }

    @Test
    fun `Robinhood parseNotification handles zero points`() {
        assertEquals(0, CardDefinition.Robinhood.parseNotification("(+0 points)"))
    }

    @Test
    fun `Robinhood parseNotification handles negative zero`() {
        assertEquals(0, CardDefinition.Robinhood.parseNotification("(-0 points)"))
    }

    // --- Chase parsing tests ---

    @Test
    fun `Chase parseNotification extracts dollar amount as cents`() {
        assertEquals(2089, CardDefinition.Chase.parseNotification("You made a \$20.89 transaction"))
    }

    @Test
    fun `Chase parseNotification handles large dollar amounts`() {
        assertEquals(15045, CardDefinition.Chase.parseNotification("Transaction: \$150.45 at Store"))
    }

    @Test
    fun `Chase parseNotification handles exact dollar amounts`() {
        assertEquals(1000, CardDefinition.Chase.parseNotification("You spent \$10.00"))
    }

    @Test
    fun `Chase parseNotification returns 0 for no dollar amount`() {
        assertEquals(0, CardDefinition.Chase.parseNotification("Welcome to Chase"))
    }

    @Test
    fun `Chase parseNotification returns 0 for empty string`() {
        assertEquals(0, CardDefinition.Chase.parseNotification(""))
    }

    @Test
    fun `Chase parseNotification handles amounts with commas`() {
        assertEquals(123456, CardDefinition.Chase.parseNotification("You spent \$1,234.56"))
    }

    @Test
    fun `Chase parseNotification handles large amounts with commas`() {
        assertEquals(1234567, CardDefinition.Chase.parseNotification("Transaction: \$12,345.67"))
    }

    @Test
    fun `Chase parseNotification extracts first match`() {
        assertEquals(1099, CardDefinition.Chase.parseNotification("\$10.99 at store, total \$25.00"))
    }

    @Test
    fun `Chase parseNotification handles small amounts`() {
        assertEquals(99, CardDefinition.Chase.parseNotification("Charge: \$0.99"))
    }

    // --- Companion/lookup tests ---

    @Test
    fun `fromPackageName returns Robinhood for correct package`() {
        assertEquals(CardDefinition.Robinhood, CardDefinition.fromPackageName("com.robinhood.money"))
    }

    @Test
    fun `fromPackageName returns Chase for correct package`() {
        assertEquals(CardDefinition.Chase, CardDefinition.fromPackageName("com.chase.sig.android"))
    }

    @Test
    fun `fromPackageName returns null for unknown package`() {
        assertNull(CardDefinition.fromPackageName("com.unknown.app"))
    }

    @Test
    fun `fromId returns correct cards`() {
        assertEquals(CardDefinition.Robinhood, CardDefinition.fromId("robinhood"))
        assertEquals(CardDefinition.Chase, CardDefinition.fromId("chase"))
        assertNull(CardDefinition.fromId("unknown"))
    }

    @Test
    fun `all returns all card definitions`() {
        val all = CardDefinition.all()
        assertEquals(2, all.size)
        assert(all.contains(CardDefinition.Robinhood))
        assert(all.contains(CardDefinition.Chase))
    }

    // --- Value formatting tests ---

    @Test
    fun `Robinhood formatValue formats points with commas`() {
        assertEquals("10,000", CardDefinition.Robinhood.formatValue(10000))
        assertEquals("0", CardDefinition.Robinhood.formatValue(0))
        assertEquals("1,000,000", CardDefinition.Robinhood.formatValue(1000000))
    }

    @Test
    fun `Chase formatValue formats cents as dollars`() {
        assertEquals("$100.00", CardDefinition.Chase.formatValue(10000))
        assertEquals("$0.00", CardDefinition.Chase.formatValue(0))
        assertEquals("$12.34", CardDefinition.Chase.formatValue(1234))
        assertEquals("$1,234.56", CardDefinition.Chase.formatValue(123456))
    }
}
