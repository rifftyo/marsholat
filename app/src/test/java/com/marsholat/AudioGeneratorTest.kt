package com.marsholat

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

class AudioGeneratorTest {

    @Test
    fun generateAdzanAudioFile() {
        val rawDir = File("src/main/res/raw")
        if (!rawDir.exists()) {
            rawDir.mkdirs()
        }
        val outputFile = File(rawDir, "adzan.wav")

        val sampleRate = 44100
        val durationSeconds = 5
        val numSamples = sampleRate * durationSeconds
        val dataSize = numSamples * 2 // 16-bit mono = 2 bytes per sample

        val bos = ByteArrayOutputStream()
        // RIFF header
        bos.write("RIFF".toByteArray())
        bos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(dataSize + 36).array())
        bos.write("WAVE".toByteArray())

        // fmt chunk
        bos.write("fmt ".toByteArray())
        bos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(16).array())
        bos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(1).array()) // PCM
        bos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(1).array()) // Mono
        bos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(sampleRate).array())
        bos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(sampleRate * 2).array())
        bos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(2).array())
        bos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(16).array())

        // data chunk
        bos.write("data".toByteArray())
        bos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(dataSize).array())

        val notes = doubleArrayOf(329.63, 392.00, 440.00, 493.88, 440.00, 329.63)
        val samplesPerNote = numSamples / notes.size

        val sampleBuffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until numSamples) {
            val noteIdx = (i / samplesPerNote).coerceAtMost(notes.size - 1)
            val freq = notes[noteIdx]
            val t = i.toDouble() / sampleRate
            val noteProgress = (i % samplesPerNote).toDouble() / samplesPerNote
            val env = sin(PI * noteProgress)
            val wave = 0.7 * sin(2 * PI * freq * t) + 0.3 * sin(4 * PI * freq * t)
            val sampleVal = (wave * env * 24000).toInt().coerceIn(-32768, 32767).toShort()

            sampleBuffer.clear()
            sampleBuffer.putShort(sampleVal)
            bos.write(sampleBuffer.array())
        }

        FileOutputStream(outputFile).use { fos ->
            fos.write(bos.toByteArray())
        }

        assertTrue("adzan.wav should exist and have content", outputFile.exists() && outputFile.length() > 1000)
    }
}
