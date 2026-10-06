package com.rodclock.refereetimer

object TimerLogic {
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    fun elapsedSeconds(startMs: Long, nowMs: Long): Int {
        return ((nowMs - startMs) / 1000L).toInt()
    }

    fun delayToNextSecondMs(startMs: Long, nowMs: Long): Long {
        return 1000L - (nowMs - startMs) % 1000L
    }

    fun backgroundColorFor(seconds: Int): String {
        return when {
            seconds >= 90 -> "#FFFFFF"
            seconds >= 30 -> "#3B82F6"
            seconds >= 15 -> "#EF4444"
            seconds >= 10 -> "#FACC15"
            else -> "#A7F3D0"
        }
    }

    fun shouldTriggerLongSignal(seconds: Int): Boolean {
        return seconds in setOf(10, 15, 30, 90)
    }

    fun shouldTriggerShortSignal(seconds: Int): Boolean {
        return seconds in setOf(8, 9, 13, 14, 28, 29, 88, 89)
    }

    /** Waveform timings (off/on in ms) for the signal at [seconds], or null if none. */
    fun vibrationPatternFor(seconds: Int): LongArray? {
        return when {
            seconds == 15 -> longArrayOf(0L, DOUBLE_PULSE_MS, PULSE_GAP_MS, DOUBLE_PULSE_MS)
            shouldTriggerLongSignal(seconds) -> longArrayOf(0L, LONG_PULSE_MS)
            shouldTriggerShortSignal(seconds) -> longArrayOf(0L, SHORT_PULSE_MS)
            else -> null
        }
    }

    const val SHORT_PULSE_MS = 10L
    const val LONG_PULSE_MS = 450L
    const val DOUBLE_PULSE_MS = 200L
    const val PULSE_GAP_MS = 100L
    const val RESET_GAP_MS = 60L

    val RESET_PATTERN = longArrayOf(0L, SHORT_PULSE_MS, RESET_GAP_MS, SHORT_PULSE_MS, RESET_GAP_MS, SHORT_PULSE_MS)
}
