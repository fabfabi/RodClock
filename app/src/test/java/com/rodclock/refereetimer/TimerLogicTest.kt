package com.rodclock.refereetimer

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimerLogicTest {
    @Test
    fun formatTime_shouldRenderMinutesAndSeconds() {
        assertEquals("00:00", TimerLogic.formatTime(0))
        assertEquals("00:05", TimerLogic.formatTime(5))
        assertEquals("01:05", TimerLogic.formatTime(65))
        assertEquals("01:30", TimerLogic.formatTime(90))
    }

    @Test
    fun elapsedSeconds_shouldFollowTheClock() {
        assertEquals(0, TimerLogic.elapsedSeconds(5_000L, 5_000L))
        assertEquals(0, TimerLogic.elapsedSeconds(5_000L, 5_999L))
        assertEquals(1, TimerLogic.elapsedSeconds(5_000L, 6_000L))
        assertEquals(90, TimerLogic.elapsedSeconds(5_000L, 95_400L))
    }

    @Test
    fun delayToNextSecond_shouldAlignToFullSeconds() {
        assertEquals(1000L, TimerLogic.delayToNextSecondMs(5_000L, 5_000L))
        assertEquals(700L, TimerLogic.delayToNextSecondMs(5_000L, 5_300L))
        assertEquals(1L, TimerLogic.delayToNextSecondMs(5_000L, 7_999L))
    }

    @Test
    fun backgroundColors_shouldChangeAtExpectedThresholds() {
        assertEquals("#A7F3D0", TimerLogic.backgroundColorFor(0))
        assertEquals("#A7F3D0", TimerLogic.backgroundColorFor(4))
        assertEquals("#A7F3D0", TimerLogic.backgroundColorFor(9))
        assertEquals("#FACC15", TimerLogic.backgroundColorFor(10))
        assertEquals("#FACC15", TimerLogic.backgroundColorFor(11))
        assertEquals("#FACC15", TimerLogic.backgroundColorFor(14))
        assertEquals("#EF4444", TimerLogic.backgroundColorFor(15))
        assertEquals("#EF4444", TimerLogic.backgroundColorFor(29))
        assertEquals("#3B82F6", TimerLogic.backgroundColorFor(30))
        assertEquals("#3B82F6", TimerLogic.backgroundColorFor(89))
        assertEquals("#FFFFFF", TimerLogic.backgroundColorFor(90))
    }

    @Test
    fun vibrationSignals_shouldMatchTheRefereeSchedule() {
        assertEquals(false, TimerLogic.shouldTriggerShortSignal(5))
        assertEquals(true, TimerLogic.shouldTriggerShortSignal(8))
        assertEquals(false, TimerLogic.shouldTriggerShortSignal(10))
        assertEquals(true, TimerLogic.shouldTriggerLongSignal(10))
        assertEquals(true, TimerLogic.shouldTriggerShortSignal(29))
        assertEquals(true, TimerLogic.shouldTriggerLongSignal(90))
        assertEquals(false, TimerLogic.shouldTriggerLongSignal(11))
    }

    @Test
    fun vibrationPattern_shouldMatchSignalType() {
        assertArrayEquals(longArrayOf(0L, 10L), TimerLogic.vibrationPatternFor(8))
        assertArrayEquals(longArrayOf(0L, 10L), TimerLogic.vibrationPatternFor(89))
        assertArrayEquals(longArrayOf(0L, 450L), TimerLogic.vibrationPatternFor(10))
        assertArrayEquals(longArrayOf(0L, 200L, 100L, 200L), TimerLogic.vibrationPatternFor(15))
        assertArrayEquals(longArrayOf(0L, 450L), TimerLogic.vibrationPatternFor(30))
        assertArrayEquals(longArrayOf(0L, 450L), TimerLogic.vibrationPatternFor(90))
        assertNull(TimerLogic.vibrationPatternFor(11))
    }

    @Test
    fun resetPattern_shouldBeThreeShortPulses() {
        assertArrayEquals(longArrayOf(0L, 10L, 60L, 10L, 60L, 10L), TimerLogic.RESET_PATTERN)
    }
}
