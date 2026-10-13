package com.example.aplicaciondesomnolencia.alerts

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * HapticAlertManager controls repeating vibration alert patterns across all API levels (API 24+ to 34+).
 */
class HapticAlertManager(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private var isVibrating = false

    @Synchronized
    fun startVibration() {
        if (isVibrating) return

        val vib = vibrator
        if (vib == null || !vib.hasVibrator()) {
            Log.w(TAG, "Device does not support vibration or vibrator service is unavailable.")
            return
        }

        try {
            // Aggressive repeating waveform pattern: [delay, vibrate, pause, vibrate, pause, vibrate]
            val pattern = longArrayOf(0, 500, 150, 500, 150, 1000)
            val repeatIndex = 0 // Repeat continuously from start

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, repeatIndex)
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                @Suppress("DEPRECATION")
                vib.vibrate(effect, audioAttributes)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(pattern, repeatIndex)
            }

            isVibrating = true
            Log.d(TAG, "Haptic alert started.")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration alert", e)
            stopVibration()
        }
    }

    @Synchronized
    fun stopVibration() {
        val vib = vibrator
        if (!isVibrating && vib == null) return

        try {
            vib?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibration alert", e)
        } finally {
            isVibrating = false
            Log.d(TAG, "Haptic alert stopped.")
        }
    }

    fun release() {
        stopVibration()
    }

    companion object {
        private const val TAG = "HapticAlertManager"
    }
}
