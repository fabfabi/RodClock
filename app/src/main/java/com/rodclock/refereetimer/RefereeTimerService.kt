package com.rodclock.refereetimer

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class RefereeTimerService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var elapsedSeconds = 0
    private var firedSignals = mutableSetOf<Int>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_RESET -> {
                elapsedSeconds = 0
                firedSignals.clear()
                sendTimeUpdate()
            }
        }

        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(tickRunnable, 1000L)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private val tickRunnable = object : Runnable {
        override fun run() {
            elapsedSeconds += 1

            if (TimerLogic.shouldTriggerLongSignal(elapsedSeconds)) {
                triggerLongVibration()
            }

            if (TimerLogic.shouldTriggerShortSignal(elapsedSeconds)) {
                triggerShortVibration()
            }

            sendTimeUpdate()
            handler.postDelayed(this, 1000L)
        }
    }

    private fun sendTimeUpdate() {
        val intent = Intent(TIMER_UPDATED).apply {
            putExtra(EXTRA_SECONDS, elapsedSeconds)
        }
        sendBroadcast(intent)
    }

    private fun triggerShortVibration() {
        if (!firedSignals.add(elapsedSeconds)) return
        val vibrator = getVibrator()
        vibrator?.vibrate(VibrationEffect.createOneShot(120L, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun triggerLongVibration() {
        if (!firedSignals.add(elapsedSeconds)) return
        val vibrator = getVibrator()
        vibrator?.vibrate(VibrationEffect.createOneShot(450L, VibrationEffect.DEFAULT_AMPLITUDE))
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
    }
}
