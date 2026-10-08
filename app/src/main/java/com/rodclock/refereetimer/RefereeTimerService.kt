package com.rodclock.refereetimer

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class RefereeTimerService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var startRealtimeMs = 0L
    private var elapsedSeconds = 0
    private var firedSignals = mutableSetOf<Int>()
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        acquireWakeLock()

        when (intent?.action) {
            ACTION_RESET -> {
                startRealtimeMs = SystemClock.elapsedRealtime()
                elapsedSeconds = 0
                firedSignals.clear()
                sendTimeUpdate()
            }
        }

        handler.removeCallbacksAndMessages(null)
        scheduleNextTick()
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        wakeLock?.let { if (it.isHeld) it.release() }
        super.onDestroy()
    }

    private val tickRunnable = object : Runnable {
        override fun run() {
            elapsedSeconds = TimerLogic.elapsedSeconds(startRealtimeMs, SystemClock.elapsedRealtime())

            TimerLogic.vibrationPatternFor(elapsedSeconds)?.let { triggerVibration(it) }

            sendTimeUpdate()
            scheduleNextTick()
        }
    }

    private fun scheduleNextTick() {
        val delayMs = TimerLogic.delayToNextSecondMs(startRealtimeMs, SystemClock.elapsedRealtime())
        handler.postDelayed(tickRunnable, delayMs)
    }

    private fun acquireWakeLock() {
        val lock = wakeLock ?: getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RodClock:timer")
            .apply { setReferenceCounted(false) }
            .also { wakeLock = it }
        lock.acquire(WAKE_LOCK_TIMEOUT_MS)
    }

    private fun sendTimeUpdate() {
        val intent = Intent(TIMER_UPDATED).apply {
            setPackage(packageName)
            putExtra(EXTRA_SECONDS, elapsedSeconds)
        }
        sendBroadcast(intent)
    }

    private fun triggerVibration(pattern: LongArray) {
        if (!firedSignals.add(elapsedSeconds)) return
        val vibrator = getVibrator()
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    private fun getVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VibratorManager::class.java)
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }
    }

    companion object {
        const val ACTION_RESET = "com.rodclock.refereetimer.RESET_TIMER"
        const val ACTION_START = "com.rodclock.refereetimer.START_TIMER"
        const val TIMER_UPDATED = "com.rodclock.refereetimer.TIMER_UPDATED"
        const val EXTRA_SECONDS = "extra_seconds"
        private const val WAKE_LOCK_TIMEOUT_MS = 5 * 60 * 1000L
    }
}
