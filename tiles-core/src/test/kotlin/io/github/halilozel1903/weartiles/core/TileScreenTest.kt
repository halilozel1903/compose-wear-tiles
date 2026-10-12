package io.github.halilozel1903.weartiles.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TileScreenTest {

    @Test
    fun screenClasses() {
        assertEquals(ScreenClass.SmallRound, TileScreen.SmallRound.screenClass)
        assertEquals(ScreenClass.LargeRound, TileScreen.LargeRound.screenClass)
        assertEquals(ScreenClass.LargeRound, TileScreen(225, 225).screenClass)
        assertEquals(ScreenClass.SmallRound, TileScreen(224, 240).screenClass)
        assertEquals(ScreenClass.SmallRect, TileScreen(200, 200, isRound = false).screenClass)
        assertEquals(ScreenClass.LargeRect, TileScreen(240, 260, isRound = false).screenClass)
        assertFailsWith<IllegalArgumentException> { TileScreen(0, 200) }
    }

    @Test
    fun largeScreensShowMore() {
        val small = TileScreen.SmallRound.layout
        val large = TileScreen.LargeRound.layout
        assertTrue(large.maxStats > small.maxStats)
        assertTrue(large.maxShortcuts > small.maxShortcuts)
        assertTrue(small.compactNumbers)
        assertFalse(large.compactNumbers)
        assertTrue(large.showSecondaryText)
        assertFalse(small.showSecondaryText)
    }

    @Test
    fun balancedRows() {
        assertEquals(emptyList(), TileLayoutSpec.balancedRows(0, 3))
        assertEquals(listOf(1), TileLayoutSpec.balancedRows(1, 3))
        assertEquals(listOf(3), TileLayoutSpec.balancedRows(3, 3))
        assertEquals(listOf(2, 2), TileLayoutSpec.balancedRows(4, 3))
        assertEquals(listOf(3, 2), TileLayoutSpec.balancedRows(5, 3))
        assertEquals(listOf(3, 3), TileLayoutSpec.balancedRows(6, 3))
        assertEquals(listOf(3, 2, 2), TileLayoutSpec.balancedRows(7, 3))
        assertEquals(listOf(2, 2), TileLayoutSpec.balancedRows(4, 2))
        assertFailsWith<IllegalArgumentException> { TileLayoutSpec.balancedRows(3, 0) }
    }

    @Test
    fun shortcutRowsAreCutAndBalanced() {
        val items = listOf("Run", "Ride", "Walk", "Yoga", "Swim")
        assertEquals(listOf(listOf("Run", "Ride"), listOf("Walk", "Yoga")), TileScreen.SmallRound.layout.shortcutRows(items))
        assertEquals(listOf(listOf("Run", "Ride", "Walk"), listOf("Yoga", "Swim")), TileScreen.LargeRound.layout.shortcutRows(items))
    }

    @Test
    fun titlesAreShortened() {
        assertEquals("Steps", TileScreen.SmallRound.layout.fitTitle("Steps"))
        assertEquals("Morning run…", TileLayoutSpec.ellipsize("Morning run with Alex", 12))
        assertEquals("Weekly distan…", TileScreen.SmallRound.layout.fitTitle("Weekly distance total"))
    }
}
