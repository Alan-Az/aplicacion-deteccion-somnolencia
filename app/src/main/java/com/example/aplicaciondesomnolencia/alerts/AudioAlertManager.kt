package com.example.aplicaciondesomnolencia.alerts

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

/**
 * AudioAlertManager controls alarm sound playback using MediaPlayer.
 *
 * Loops a loud, aggressive alert tone while fatigue is detected.
 * Supports custom audio URI selection from device storage, falling back to
 * bundled raw alarm sound (R.raw.alarm_sound), system alarm ringtone, or synth WAV.
 */
class AudioAlertManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var isPlayingAlert = false

    var customAudioUri: Uri? = null
        private set

    @Synchronized
    fun setCustomAudioUri(uri: Uri?) {
        this.customAudioUri = uri
        if (isPlayingAlert) {
            stopAlert()
            startAlert()
        }
    }

    @Synchronized
    fun startAlert() {
        if (isPlayingAlert) return

        try {
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )

            var loaded = false

            // 1. Try Custom Audio URI from Storage if selected
            val userUri = customAudioUri
            if (userUri != null) {
                try {
                    mp.setDataSource(context, userUri)
                    mp.isLooping = true
                    mp.prepare()
                    loaded = true
                    Log.d(TAG, "Loaded custom alarm audio URI: $userUri")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load custom audio URI: $userUri, falling back to raw resource", e)
                    mp.reset()
                }
            }

            // 2. Try Bundled Raw Alarm Sound (app/src/main/res/raw/alarm_sound.mp3)
            if (!loaded) {
                try {
                    val afd = context.resources.openRawResourceFd(com.example.aplicaciondesomnolencia.R.raw.alarm_sound)
                    if (afd != null) {
                        mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                        afd.close()
                        mp.isLooping = true
                        mp.prepare()
                        loaded = true
                        Log.d(TAG, "Loaded bundled raw alarm sound.")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load bundled R.raw.alarm_sound, falling back to system alarm", e)
                    mp.reset()
                }
            }

            // 3. Fallback to System Alarm/Notification Tone
            if (!loaded) {
                val alarmUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                if (alarmUri != null) {
                    try {
                        mp.setDataSource(context, alarmUri)
                        mp.isLooping = true
                        mp.prepare()
                        loaded = true
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load default system alarm URI, falling back to synth tone", e)
                        mp.reset()
                    }
                }
            }

            // 4. Final Fallback to Generated Synth WAV
            if (!loaded) {
                val synthWavFile = getOrGenerateSynthWav(context)
                mp.setDataSource(synthWavFile.absolutePath)
                mp.isLooping = true
                mp.prepare()
            }

            mp.start()
            mediaPlayer = mp
            isPlayingAlert = true
            Log.d(TAG, "Audio alert started.")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio alert", e)
            stopAlert()
        }
    }

    @Synchronized
    fun stopAlert() {
        val mp = mediaPlayer
        if ((mp == null) && !isPlayingAlert) return

        try {
            mp?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error checking or stopping mediaPlayer state: ${e.message}")
                }
                player.reset()
                player.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio alert", e)
        } finally {
            mediaPlayer = null
            isPlayingAlert = false
            Log.d(TAG, "Audio alert stopped.")
        }
    }

    fun release() {
        stopAlert()
    }

    private fun getOrGenerateSynthWav(context: Context): File {
        val wavFile = File(context.cacheDir, "alert_synth_tone.wav")
        if (wavFile.exists() && wavFile.length() > 0) {
            return wavFile
        }

        val sampleRate = 22050
        val durationSec = 0.5
        val numSamples = (sampleRate * durationSec).toInt()
        val freq = 880.0

        val pcmData = ByteArray(numSamples * 2)
        var idx = 0

        for (i in 0 until numSamples) {
            val angle = 2.0 * PI * i.toDouble() / (sampleRate.toDouble() / freq)
            val sampleVal = (sin(angle) * 32767).toInt().coerceIn(-32768, 32767).toShort()

            val bb = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
            bb.putShort(sampleVal)
            val bytes = bb.array()

            pcmData[idx++] = bytes[0]
            pcmData[idx++] = bytes[1]
        }

        val totalDataLen = pcmData.size + 36
        val byteRate = sampleRate * 2

        try {
            FileOutputStream(wavFile).use { out ->
                // RIFF header
                out.write("RIFF".toByteArray())
                out.write(intToLittleEndian(totalDataLen))
                out.write("WAVE".toByteArray())
                // fmt chunk
                out.write("fmt ".toByteArray())
                out.write(intToLittleEndian(16)) // Subchunk1Size
                out.write(shortToLittleEndian(1.toShort())) // AudioFormat PCM = 1
                out.write(shortToLittleEndian(1.toShort())) // NumChannels Mono = 1
                out.write(intToLittleEndian(sampleRate))
                out.write(intToLittleEndian(byteRate))
                out.write(shortToLittleEndian(2.toShort())) // BlockAlign
                out.write(shortToLittleEndian(16.toShort())) // BitsPerSample
                // data chunk
                out.write("data".toByteArray())
                out.write(intToLittleEndian(pcmData.size))
                out.write(pcmData)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing synth WAV file", e)
        }

        return wavFile
    }

    private fun intToLittleEndian(value: Int): ByteArray {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    }

    private fun shortToLittleEndian(value: Short): ByteArray {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array()
    }

    companion object {
        private const val TAG = "AudioAlertManager"
    }
}
