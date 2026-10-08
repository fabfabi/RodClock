package com.rodclock.refereetimer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.rodclock.refereetimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val seconds = intent?.getIntExtra(RefereeTimerService.EXTRA_SECONDS, 0) ?: 0
            binding.timeText.text = TimerLogic.formatTime(seconds)
            binding.root.setBackgroundColor(Color.parseColor(TimerLogic.backgroundColorFor(seconds)))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // All background colors are light, so system bar icons stay dark.
        WindowCompat.getInsetsController(window, binding.root).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        val basePadding = binding.root.paddingLeft
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                basePadding + bars.left,
                basePadding + bars.top,
                basePadding + bars.right,
                basePadding + bars.bottom
            )
            insets
        }

        binding.root.setOnClickListener { resetTimer() }
        binding.resetButton.setOnClickListener { resetTimer() }

        ContextCompat.registerReceiver(
            this,
            timerReceiver,
            IntentFilter(RefereeTimerService.TIMER_UPDATED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        resetTimer()
    }

    override fun onDestroy() {
        unregisterReceiver(timerReceiver)
        if (isFinishing) stopService(Intent(this, RefereeTimerService::class.java))
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (event?.repeatCount == 0) resetTimer()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun resetTimer() {
        val serviceIntent = Intent(this, RefereeTimerService::class.java).apply {
            action = RefereeTimerService.ACTION_RESET
        }

        startService(serviceIntent)
        triggerResetPattern()
    }

    private fun triggerResetPattern() {
        val pattern = TimerLogic.RESET_PATTERN
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
}
