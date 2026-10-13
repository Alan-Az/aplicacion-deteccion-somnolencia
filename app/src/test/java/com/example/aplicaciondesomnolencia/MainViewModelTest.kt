package com.example.aplicaciondesomnolencia

import android.app.Application
import android.net.Uri
import com.example.aplicaciondesomnolencia.biometrics.BiometricEngine
import com.example.aplicaciondesomnolencia.camera.CameraCapabilityInspector
import com.example.aplicaciondesomnolencia.viewmodel.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class MainViewModelTest {

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        val application = mock(Application::class.java)
        viewModel = MainViewModel(application)
    }

    @Test
    fun testDefaultSettings() {
        assertEquals(BiometricEngine.DEFAULT_LIMITE_EAR, viewModel.limiteEar.value, 0.001f)
        assertEquals(BiometricEngine.DEFAULT_LIMITE_FRAMES, viewModel.limiteFrames.value)
        assertTrue(viewModel.isAudioAlertEnabled.value)
        assertTrue(viewModel.isHapticAlertEnabled.value)
        assertTrue(viewModel.isFrontCamera.value)
        assertFalse(viewModel.isTorchEnabled.value)
        assertNull(viewModel.cameraSpecs.value)
        assertNull(viewModel.customAudioUri.value)
    }

    @Test
    fun testSetLimiteEar() {
        viewModel.setLimiteEar(0.20f)
        assertEquals(0.20f, viewModel.limiteEar.value, 0.001f)
        assertEquals(0.20f, viewModel.biometricEngine.limiteEar, 0.001f)
    }

    @Test
    fun testSetLimiteEarCoercion() {
        viewModel.setLimiteEar(0.01f)
        assertEquals(0.08f, viewModel.limiteEar.value, 0.001f)

        viewModel.setLimiteEar(0.50f)
        assertEquals(0.30f, viewModel.limiteEar.value, 0.001f)
    }

    @Test
    fun testSetLimiteFrames() {
        viewModel.setLimiteFrames(25)
        assertEquals(25, viewModel.limiteFrames.value)
        assertEquals(25, viewModel.biometricEngine.limiteFrames)
    }

    @Test
    fun testSetLimiteFramesCoercion() {
        viewModel.setLimiteFrames(2)
        assertEquals(5, viewModel.limiteFrames.value)

        viewModel.setLimiteFrames(100)
        assertEquals(45, viewModel.limiteFrames.value)
    }

    @Test
    fun testAlertToggles() {
        viewModel.setAudioAlertEnabled(false)
        assertFalse(viewModel.isAudioAlertEnabled.value)

        viewModel.setHapticAlertEnabled(false)
        assertFalse(viewModel.isHapticAlertEnabled.value)
    }

    @Test
    fun testToggleCameraLens() {
        assertTrue(viewModel.isFrontCamera.value)
        viewModel.toggleCameraLens()
        assertFalse(viewModel.isFrontCamera.value)
        viewModel.toggleCameraLens()
        assertTrue(viewModel.isFrontCamera.value)
    }

    @Test
    fun testTorchToggles() {
        assertFalse(viewModel.isTorchEnabled.value)
        viewModel.toggleTorch()
        assertTrue(viewModel.isTorchEnabled.value)
        viewModel.toggleTorch()
        assertFalse(viewModel.isTorchEnabled.value)

        viewModel.setTorchEnabled(true)
        assertTrue(viewModel.isTorchEnabled.value)
    }

    @Test
    fun testUpdateCameraSpecs() {
        val dummySpecs = CameraCapabilityInspector.createFallbackSpecs(isFrontCamera = true)
        viewModel.updateCameraSpecs(dummySpecs)
        assertEquals(dummySpecs, viewModel.cameraSpecs.value)
    }

    @Test
    fun testSetCustomAudioUri() {
        val mockUri = mock(Uri::class.java)
        viewModel.setCustomAudioUri(mockUri)
        assertEquals(mockUri, viewModel.customAudioUri.value)

        viewModel.setCustomAudioUri(null)
        assertNull(viewModel.customAudioUri.value)
    }

    @Test
    fun testResetDefaults() {
        viewModel.setLimiteEar(0.25f)
        viewModel.setLimiteFrames(35)
        viewModel.setAudioAlertEnabled(false)
        viewModel.setHapticAlertEnabled(false)
        viewModel.toggleCameraLens()
        viewModel.setTorchEnabled(true)
        val mockUri = mock(Uri::class.java)
        viewModel.setCustomAudioUri(mockUri)

        viewModel.resetDefaults()

        assertEquals(BiometricEngine.DEFAULT_LIMITE_EAR, viewModel.limiteEar.value, 0.001f)
        assertEquals(BiometricEngine.DEFAULT_LIMITE_FRAMES, viewModel.limiteFrames.value)
        assertTrue(viewModel.isAudioAlertEnabled.value)
        assertTrue(viewModel.isHapticAlertEnabled.value)
        assertTrue(viewModel.isFrontCamera.value)
        assertFalse(viewModel.isTorchEnabled.value)
        assertNull(viewModel.customAudioUri.value)
    }
}
