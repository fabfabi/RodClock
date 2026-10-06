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
}
