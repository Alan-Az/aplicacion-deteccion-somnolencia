package com.example.aplicaciondesomnolencia

import com.example.aplicaciondesomnolencia.biometrics.EarCalculator
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import org.junit.Assert.assertEquals
import org.junit.Test

class EarCalculatorTest {

    @Test
    fun testEuclideanDistance2D() {
        val p1 = NormalizedLandmark.create(0.0f, 0.0f, 0.0f)
        val p2 = NormalizedLandmark.create(3.0f, 4.0f, 0.0f)

        val dist = EarCalculator.euclideanDistance(p1, p2, use3D = false)
        assertEquals(5.0f, dist, 0.0001f)
    }

    @Test
    fun testEuclideanDistance3D() {
        val p1 = NormalizedLandmark.create(0.0f, 0.0f, 0.0f)
        val p2 = NormalizedLandmark.create(1.0f, 2.0f, 2.0f)

        val dist = EarCalculator.euclideanDistance(p1, p2, use3D = true)
        assertEquals(3.0f, dist, 0.0001f)
    }

    @Test
    fun testCalculateSingleEyeEarOpenEye() {
        // Eye shape (p1..p6):
        // p1 = horizontal left (0, 0)
        // p2 = top left (2, 2)
        // p3 = top right (4, 2)
        // p4 = horizontal right (6, 0)
        // p5 = bottom right (4, -2)
        // p6 = bottom left (2, -2)
        //
        // ||p2 - p6|| = |2 - (-2)| = 4
        // ||p3 - p5|| = |2 - (-2)| = 4
        // ||p1 - p4|| = |0 - 6| = 6
        //
        // EAR = (4 + 4) / (2 * 6) = 8 / 12 = 0.6667
        val p1 = NormalizedLandmark.create(0.0f, 0.0f, 0.0f)
        val p2 = NormalizedLandmark.create(2.0f, 2.0f, 0.0f)
        val p3 = NormalizedLandmark.create(4.0f, 2.0f, 0.0f)
        val p4 = NormalizedLandmark.create(6.0f, 0.0f, 0.0f)
        val p5 = NormalizedLandmark.create(4.0f, -2.0f, 0.0f)
        val p6 = NormalizedLandmark.create(2.0f, -2.0f, 0.0f)

        val landmarks = listOf(p1, p2, p3, p4, p5, p6)
        val ear = EarCalculator.calculateSingleEyeEar(landmarks)

        assertEquals(8.0f / 12.0f, ear, 0.001f)
    }

    @Test
    fun testCalculateSingleEyeEarClosedEye() {
        // Closed eye: vertical distances near 0
        val p1 = NormalizedLandmark.create(0.0f, 0.0f, 0.0f)
        val p2 = NormalizedLandmark.create(2.0f, 0.1f, 0.0f)
        val p3 = NormalizedLandmark.create(4.0f, 0.1f, 0.0f)
        val p4 = NormalizedLandmark.create(6.0f, 0.0f, 0.0f)
        val p5 = NormalizedLandmark.create(4.0f, -0.1f, 0.0f)
        val p6 = NormalizedLandmark.create(2.0f, -0.1f, 0.0f)

        val landmarks = listOf(p1, p2, p3, p4, p5, p6)
        val ear = EarCalculator.calculateSingleEyeEar(landmarks)

        assertEquals(0.4f / 12.0f, ear, 0.001f)
    }
}
