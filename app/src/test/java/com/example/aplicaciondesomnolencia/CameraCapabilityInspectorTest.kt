package com.example.aplicaciondesomnolencia

import com.example.aplicaciondesomnolencia.camera.CameraCapabilityInspector
import com.example.aplicaciondesomnolencia.camera.CameraStatusVerdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CameraCapabilityInspectorTest {

    @Test
    fun testFallbackSpecsCreation() {
        val specs = CameraCapabilityInspector.createFallbackSpecs(
            isFrontCamera = true,
            width = 1280,
            height = 720,
            fps = 30,
            hasFlashUnit = false,
            isAeSupported = true
        )

        assertEquals("Frontal", specs.lensFacingName)
        assertTrue(specs.isFrontCamera)
        assertFalse(specs.hasFlashUnit)
        assertTrue(specs.isExposureCompensationSupported)
        assertEquals(30, specs.maxFps)
        assertEquals(1280, specs.resolutionWidth)
        assertEquals(720, specs.resolutionHeight)
        assertEquals("720p (1280 x 720)", specs.resolutionLabel)
        assertEquals(CameraStatusVerdict.LIMITED_LOW_LIGHT, specs.statusVerdict)
    }

    @Test
    fun testEvaluateStatusOptimal() {
        val verdict = CameraCapabilityInspector.evaluateStatus(
            width = 1920,
            height = 1080,
            maxFps = 60,
            hasFlashUnit = true,
            isAeSupported = true
        )
        assertEquals(CameraStatusVerdict.OPTIMAL, verdict)
    }

    @Test
    fun testEvaluateStatusLimitedLowLight() {
        val verdict = CameraCapabilityInspector.evaluateStatus(
            width = 1280,
            height = 720,
            maxFps = 30,
            hasFlashUnit = false,
            isAeSupported = true
        )
        assertEquals(CameraStatusVerdict.LIMITED_LOW_LIGHT, verdict)
    }

    @Test
    fun testEvaluateStatusInsufficientLowResolution() {
        val verdict = CameraCapabilityInspector.evaluateStatus(
            width = 320,
            height = 240,
            maxFps = 30,
            hasFlashUnit = true,
            isAeSupported = true
        )
        assertEquals(CameraStatusVerdict.INSUFFICIENT, verdict)
    }

    @Test
    fun testEvaluateStatusInsufficientLowFps() {
        val verdict = CameraCapabilityInspector.evaluateStatus(
            width = 1280,
            height = 720,
            maxFps = 10,
            hasFlashUnit = true,
            isAeSupported = true
        )
        assertEquals(CameraStatusVerdict.INSUFFICIENT, verdict)
    }

    @Test
    fun testEvaluateStatusCompatible() {
        val verdict = CameraCapabilityInspector.evaluateStatus(
            width = 640,
            height = 480,
            maxFps = 20,
            hasFlashUnit = true,
            isAeSupported = false
        )
        assertEquals(CameraStatusVerdict.COMPATIBLE, verdict)
    }

    @Test
    fun testFormatResolutionLabel() {
        assertEquals("1080p (1920 x 1080)", CameraCapabilityInspector.formatResolutionLabel(1920, 1080))
        assertEquals("720p (1280 x 720)", CameraCapabilityInspector.formatResolutionLabel(1280, 720))
        assertEquals("480p (640 x 480)", CameraCapabilityInspector.formatResolutionLabel(640, 480))
        assertEquals("240p (320 x 240)", CameraCapabilityInspector.formatResolutionLabel(320, 240))
    }
}
