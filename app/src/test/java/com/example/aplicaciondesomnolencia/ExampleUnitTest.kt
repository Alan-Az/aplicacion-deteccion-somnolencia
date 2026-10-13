package com.example.aplicaciondesomnolencia

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        org.junit.Assert.assertEquals(4, 2 + 2)
    }

    @Test
    fun generateRawAlarmSound() {
        val rawDir = File("src/main/res/raw")
        if (!rawDir.exists()) {
            rawDir.mkdirs()
        }
        val sampleRate = 22050
        val durationSec = 2.0
        val numSamples = (sampleRate * durationSec).toInt()
        val freq1 = 880.0
        val freq2 = 1400.0

        val pcmData = ByteArray(numSamples * 2)
        var idx = 0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freq = if (((t * 8).toInt() % 2) == 0) freq1 else freq2
            val angle = 2.0 * Math.PI * i * freq / sampleRate
            val angle2 = 2.0 * Math.PI * i * (freq * 1.5) / sampleRate
            val valDouble = (Math.sin(angle) * 0.7 + Math.sin(angle2) * 0.3) * 32767
            val sampleVal = valDouble.toInt().coerceIn(-32768, 32767).toShort()

            val bb = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
            bb.putShort(sampleVal)
            val bytes = bb.array()

            pcmData[idx++] = bytes[0]
            pcmData[idx++] = bytes[1]
        }

        val totalDataLen = pcmData.size + 36
        val byteRate = sampleRate * 2

        val wavFile = File(rawDir, "alarm_sound.mp3")
        FileOutputStream(wavFile).use { out ->
            out.write("RIFF".toByteArray())
            out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(totalDataLen).array())
            out.write("WAVE".toByteArray())
            out.write("fmt ".toByteArray())
            out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(16).array())
            out.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(1.toShort()).array())
            out.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(1.toShort()).array())
            out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(sampleRate).array())
            out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(byteRate).array())
            out.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(2.toShort()).array())
            out.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(16.toShort()).array())
            out.write("data".toByteArray())
            out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(pcmData.size).array())
            out.write(pcmData)
        }
        assertTrue(wavFile.exists() && wavFile.length() > 0)
    }
}