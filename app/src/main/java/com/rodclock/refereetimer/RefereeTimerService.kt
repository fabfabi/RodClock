package com.rodclock.refereetimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat

class RefereeTimerService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var startRealtimeMs = 0L
    private var elapsedSeconds = 0
    private var firedSignals = mutableSetOf<Int>()
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startInForeground()
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

            if (TimerLogic.shouldTriggerLongSignal(elapsedSeconds)) {
                triggerLongVibration()
            }

            if (TimerLogic.shouldTriggerShortSignal(elapsedSeconds)) {
                triggerShortVibration()
            }

            sendTimeUpdate()
            scheduleNextTick()
        }
    }

    private fun scheduleNextTick() {
        val delayMs = TimerLogic.delayToNextSecondMs(startRealtimeMs, SystemClock.elapsedRealtime())
        handler.postDelayed(tickRunnable, delayMs)
    }

    private fun startInForeground() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Referee timer", NotificationManager.IMPORTANCE_LOW)
        )

        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("RodClock")
            .setContentText("Referee timer running")
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
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
        private const val CHANNEL_ID = "referee_timer"
        private const val NOTIFICATION_ID = 1
        private const val WAKE_LOCK_TIMEOUT_MS = 4 * 60 * 60 * 1000L
    }
}
