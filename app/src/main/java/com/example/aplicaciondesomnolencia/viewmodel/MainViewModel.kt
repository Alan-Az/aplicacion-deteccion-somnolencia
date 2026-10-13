package com.example.aplicaciondesomnolencia.viewmodel

import android.app.Application
import android.net.Uri
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import com.example.aplicaciondesomnolencia.alerts.AudioAlertManager
import com.example.aplicaciondesomnolencia.alerts.HapticAlertManager
import com.example.aplicaciondesomnolencia.biometrics.BiometricEngine
import com.example.aplicaciondesomnolencia.biometrics.DriverState
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val biometricEngine = BiometricEngine()
    val audioAlertManager = AudioAlertManager(application)
    val hapticAlertManager = HapticAlertManager(application)

    private val _driverState = MutableStateFlow(DriverState.NO_FACE_DETECTED)
    val driverState: StateFlow<DriverState> = _driverState.asStateFlow()

    private val _earScore = MutableStateFlow(0.0f)
    val earScore: StateFlow<Float> = _earScore.asStateFlow()

    private val _rawEar = MutableStateFlow(0.0f)
    val rawEar: StateFlow<Float> = _rawEar.asStateFlow()

    private val _fps = MutableStateFlow(0)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    private val _consecutiveFatigueFrames = MutableStateFlow(0)
    val consecutiveFatigueFrames: StateFlow<Int> = _consecutiveFatigueFrames.asStateFlow()

    private val _limiteEar = MutableStateFlow(BiometricEngine.DEFAULT_LIMITE_EAR)
    val limiteEar: StateFlow<Float> = _limiteEar.asStateFlow()

    private val _limiteFrames = MutableStateFlow(BiometricEngine.DEFAULT_LIMITE_FRAMES)
    val limiteFrames: StateFlow<Int> = _limiteFrames.asStateFlow()

    private val _riskProgress = MutableStateFlow(0.0f)
    val riskProgress: StateFlow<Float> = _riskProgress.asStateFlow()

    private val _isAudioAlertEnabled = MutableStateFlow(value = true)
    val isAudioAlertEnabled: StateFlow<Boolean> = _isAudioAlertEnabled.asStateFlow()

    private val _isHapticAlertEnabled = MutableStateFlow(value = true)
    val isHapticAlertEnabled: StateFlow<Boolean> = _isHapticAlertEnabled.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(value = true)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _isTorchEnabled = MutableStateFlow(value = false)
    val isTorchEnabled: StateFlow<Boolean> = _isTorchEnabled.asStateFlow()

    private val _cameraSpecs = MutableStateFlow<com.example.aplicaciondesomnolencia.camera.CameraSpecs?>(null)
    val cameraSpecs: StateFlow<com.example.aplicaciondesomnolencia.camera.CameraSpecs?> = _cameraSpecs.asStateFlow()

    private val _customAudioUri = MutableStateFlow<Uri?>(null)
    val customAudioUri: StateFlow<Uri?> = _customAudioUri.asStateFlow()

    private var frameCounter = 0
    private var lastFpsTimestamp = SystemClock.uptimeMillis()

    fun processFaceLandmarks(faceLandmarks: List<NormalizedLandmark>?) {
        updateFps()

        val result = biometricEngine.processLandmarks(faceLandmarks)

        _driverState.value = result.driverState
        _rawEar.value = result.rawEar
        _earScore.value = result.smoothedEar
        _consecutiveFatigueFrames.value = result.consecutiveFatigueFrames
        _riskProgress.value = result.riskProgress

        handleAlertState(result.driverState)
    }

    private fun handleAlertState(state: DriverState) {
        if (state == DriverState.FATIGUE_DETECTED) {
            if (_isAudioAlertEnabled.value) {
                audioAlertManager.startAlert()
            }
            if (_isHapticAlertEnabled.value) {
                hapticAlertManager.startVibration()
            }
        } else {
            audioAlertManager.stopAlert()
            hapticAlertManager.stopVibration()
        }
    }

    private fun updateFps() {
        frameCounter++
        val now = SystemClock.uptimeMillis()
        val elapsed = now - lastFpsTimestamp
        if (elapsed >= 1000) {
            val currentFps = (frameCounter * 1000f / elapsed.toFloat()).roundToInt()
            _fps.value = currentFps
            frameCounter = 0
            lastFpsTimestamp = now
        }
    }

    fun setLimiteEar(limit: Float) {
        val coerced = limit.coerceIn(0.08f, 0.30f)
        biometricEngine.limiteEar = coerced
        _limiteEar.value = coerced
    }

    fun setLimiteFrames(limit: Int) {
        val coerced = limit.coerceIn(5, 45)
        biometricEngine.limiteFrames = coerced
        _limiteFrames.value = coerced
    }

    fun setAudioAlertEnabled(enabled: Boolean) {
        _isAudioAlertEnabled.value = enabled
        if (!enabled) {
            audioAlertManager.stopAlert()
        } else if (_driverState.value == DriverState.FATIGUE_DETECTED) {
            audioAlertManager.startAlert()
        }
    }

    fun setHapticAlertEnabled(enabled: Boolean) {
        _isHapticAlertEnabled.value = enabled
        if (!enabled) {
            hapticAlertManager.stopVibration()
        } else if (_driverState.value == DriverState.FATIGUE_DETECTED) {
            hapticAlertManager.startVibration()
        }
    }

    fun toggleCameraLens() {
        _isFrontCamera.value = !_isFrontCamera.value
    }

    fun setFrontCamera(isFront: Boolean) {
        _isFrontCamera.value = isFront
    }

    fun toggleTorch() {
        _isTorchEnabled.value = !_isTorchEnabled.value
    }

    fun setTorchEnabled(enabled: Boolean) {
        _isTorchEnabled.value = enabled
    }

    fun updateCameraSpecs(specs: com.example.aplicaciondesomnolencia.camera.CameraSpecs) {
        _cameraSpecs.value = specs
    }

    fun setCustomAudioUri(uri: Uri?) {
        _customAudioUri.value = uri
        audioAlertManager.setCustomAudioUri(uri)
    }

    fun resetDefaults() {
        setLimiteEar(BiometricEngine.DEFAULT_LIMITE_EAR)
        setLimiteFrames(BiometricEngine.DEFAULT_LIMITE_FRAMES)
        setAudioAlertEnabled(true)
        setHapticAlertEnabled(true)
        setFrontCamera(true)
        setTorchEnabled(false)
        setCustomAudioUri(null)
    }

    fun resetState() {
        biometricEngine.reset()
        _driverState.value = DriverState.NO_FACE_DETECTED
        _earScore.value = 0.0f
        _rawEar.value = 0.0f
        _consecutiveFatigueFrames.value = 0
        _riskProgress.value = 0.0f
        audioAlertManager.stopAlert()
        hapticAlertManager.stopVibration()
    }

    override fun onCleared() {
        audioAlertManager.release()
        hapticAlertManager.release()
    }
}
