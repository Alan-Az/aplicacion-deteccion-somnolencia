package com.example.aplicaciondesomnolencia

import com.example.aplicaciondesomnolencia.biometrics.BiometricEngine
import com.example.aplicaciondesomnolencia.biometrics.DriverState
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import org.junit.Assert.assertEquals
import org.junit.Test

class BiometricEngineTest {

    private fun createDummyFaceLandmarks(eyeHeightRatio: Float): List<NormalizedLandmark> {
        val landmarks = ArrayList<NormalizedLandmark>()
        // Initialize 468 dummy landmarks with 0.0f
        for (i in 0..468) {
            landmarks.add(NormalizedLandmark.create(0.0f, 0.0f, 0.0f))
        }

        // Left Eye Indices: 362, 385, 387, 263, 373, 380
        // p1=362, p2=385, p3=387, p4=263, p5=373, p6=380
        landmarks[362] = NormalizedLandmark.create(0.0f, 0.0f, 0.0f)
        landmarks[385] = NormalizedLandmark.create(2.0f, eyeHeightRatio, 0.0f)
        landmarks[387] = NormalizedLandmark.create(4.0f, eyeHeightRatio, 0.0f)
        landmarks[263] = NormalizedLandmark.create(6.0f, 0.0f, 0.0f)
        landmarks[373] = NormalizedLandmark.create(4.0f, -eyeHeightRatio, 0.0f)
        landmarks[380] = NormalizedLandmark.create(2.0f, -eyeHeightRatio, 0.0f)

        // Right Eye Indices: 33, 160, 158, 133, 153, 144
        // p1=33, p2=160, p3=158, p4=133, p5=153, p6=144
        landmarks[33] = NormalizedLandmark.create(0.0f, 0.0f, 0.0f)
        landmarks[160] = NormalizedLandmark.create(2.0f, eyeHeightRatio, 0.0f)
        landmarks[158] = NormalizedLandmark.create(4.0f, eyeHeightRatio, 0.0f)
        landmarks[133] = NormalizedLandmark.create(6.0f, 0.0f, 0.0f)
        landmarks[153] = NormalizedLandmark.create(4.0f, -eyeHeightRatio, 0.0f)
        landmarks[144] = NormalizedLandmark.create(2.0f, -eyeHeightRatio, 0.0f)

        return landmarks
    }

    @Test
    fun testNoFaceDetectedState() {
        val engine = BiometricEngine()
        val result = engine.processLandmarks(null)

        assertEquals(DriverState.NO_FACE_DETECTED, result.driverState)
        assertEquals(0.0f, result.rawEar, 0.0001f)
        assertEquals(0, result.consecutiveFatigueFrames)
        assertEquals(0.0f, result.riskProgress, 0.0001f)
    }

    @Test
    fun testOpenEyesAttentiveState() {
        val engine = BiometricEngine(limiteEar = 0.15f, limiteFrames = 15)
        // With eyeHeightRatio = 2.0, EAR = (4+4)/12 = 0.6667 > 0.15
        val face = createDummyFaceLandmarks(eyeHeightRatio = 2.0f)

        val result = engine.processLandmarks(face)

        assertEquals(DriverState.ATTENTIVE, result.driverState)
        assertEquals(0, result.consecutiveFatigueFrames)
        assertEquals(0.0f, result.riskProgress, 0.0001f)
    }

    @Test
    fun testClosedEyesTransitionToFatigue() {
        val limiteFrames = 5
        val engine = BiometricEngine(limiteEar = 0.15f, limiteFrames = limiteFrames)
        // With eyeHeightRatio = 0.2f, EAR = (0.4+0.4)/12 = 0.0667 < 0.15
        val faceDrowsy = createDummyFaceLandmarks(eyeHeightRatio = 0.2f)

        for (frame in 1 until limiteFrames) {
            val res = engine.processLandmarks(faceDrowsy)
            assertEquals(DriverState.ATTENTIVE, res.driverState)
            assertEquals(frame, res.consecutiveFatigueFrames)
            assertEquals(frame.toFloat() / limiteFrames, res.riskProgress, 0.001f)
        }

        // Frame threshold reached
        val resThreshold = engine.processLandmarks(faceDrowsy)
        assertEquals(DriverState.FATIGUE_DETECTED, resThreshold.driverState)
        assertEquals(limiteFrames, resThreshold.consecutiveFatigueFrames)
        assertEquals(1.0f, resThreshold.riskProgress, 0.001f)
    }

    @Test
    fun testEmaFilterSmoothing() {
        val engine = BiometricEngine(alphaPrevious = 0.7f, alphaCurrent = 0.3f)
        val face1 = createDummyFaceLandmarks(eyeHeightRatio = 2.0f) // EAR = 0.6667
        val face2 = createDummyFaceLandmarks(eyeHeightRatio = 0.2f) // EAR = 0.0667

        val res1 = engine.processLandmarks(face1)
        val ear1 = res1.rawEar // First frame: smoothed == raw
        assertEquals(ear1, res1.smoothedEar, 0.001f)

        val res2 = engine.processLandmarks(face2)
        val ear2 = res2.rawEar
        val expectedSmoothed = (ear1 * 0.7f) + (ear2 * 0.3f)

        assertEquals(expectedSmoothed, res2.smoothedEar, 0.001f)
    }

    @Test
    fun testRecoveryResetsConsecutiveFrames() {
        val engine = BiometricEngine(limiteEar = 0.15f, limiteFrames = 10)
        val faceDrowsy = createDummyFaceLandmarks(eyeHeightRatio = 0.2f)
        val faceAlert = createDummyFaceLandmarks(eyeHeightRatio = 2.0f)

        // Process 3 drowsy frames
        engine.processLandmarks(faceDrowsy)
        engine.processLandmarks(faceDrowsy)
        val res3 = engine.processLandmarks(faceDrowsy)
        assertEquals(3, res3.consecutiveFatigueFrames)

        // Process 1 attentive frame
        val resAlert = engine.processLandmarks(faceAlert)
        // Wait for smoothed value to cross above threshold or process enough open eye frames
        // Let's process enough open eye frames so EMA exceeds 0.15f
        var resCurrent = resAlert
        while (resCurrent.smoothedEar < 0.15f) {
            resCurrent = engine.processLandmarks(faceAlert)
        }

        assertEquals(0, resCurrent.consecutiveFatigueFrames)
        assertEquals(DriverState.ATTENTIVE, resCurrent.driverState)
    }
}
