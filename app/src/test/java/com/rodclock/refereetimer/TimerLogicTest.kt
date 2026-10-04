package com.rodclock.refereetimer

import org.junit.Assert.assertEquals
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
    fun backgroundColors_shouldChangeAtExpectedThresholds() {
        assertEquals("#A7F3D0", TimerLogic.backgroundColorFor(0))
        assertEquals("#A7F3D0", TimerLogic.backgroundColorFor(9))
        assertEquals("#FACC15", TimerLogic.backgroundColorFor(10))
        assertEquals("#FACC15", TimerLogic.backgroundColorFor(11))
        assertEquals("#EF4444", TimerLogic.backgroundColorFor(15))
        assertEquals("#3B82F6", TimerLogic.backgroundColorFor(30))
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
}
