package com.example.aplicaciondesomnolencia.camera

import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraInfo

enum class CameraStatusVerdict(
    val badgeTitle: String,
    val description: String
) {
    OPTIMAL(
        badgeTitle = "ÓPTIMO PARA NOCHE Y DÍA",
        description = "Cámara de alto rendimiento con linterna, compensación de exposición automática y alta resolución."
    ),
    COMPATIBLE(
        badgeTitle = "COMPATIBLE",
        description = "Cumple con los requisitos mínimos de resolución y tasa de cuadros para monitoreo diurno."
    ),
    LIMITED_LOW_LIGHT(
        badgeTitle = "ADVERTENCIA: POCA ILUMINACIÓN EN NOCHE",
        description = "La cámara no dispone de linterna/flash para baja iluminación nocturna. Se sugiere luz ambiental suplementaria."
    ),
    INSUFFICIENT(
        badgeTitle = "INSUFICIENTE",
        description = "No alcanza la resolución mínima (480p) o tasa de captura (15 FPS) indispensable para el cálculo confiable de EAR."
    )
}

data class CameraSpecs(
    val lensFacingName: String,
    val isFrontCamera: Boolean,
    val hasFlashUnit: Boolean,
    val isExposureCompensationSupported: Boolean,
    val exposureRangeMin: Int,
    val exposureRangeMax: Int,
    val exposureStep: Float,
    val minZoomRatio: Float,
    val maxZoomRatio: Float,
    val maxFps: Int,
    val resolutionWidth: Int,
    val resolutionHeight: Int,
    val resolutionLabel: String,
    val statusVerdict: CameraStatusVerdict
) {
    val meetsMinResolution: Boolean get() = (resolutionWidth >= 640 && resolutionHeight >= 480) || (resolutionWidth >= 480 && resolutionHeight >= 640)
    val meetsMinFps: Boolean get() = maxFps >= 15
    val meetsMinCameraPresence: Boolean get() = true

    val meetsRecTorch: Boolean get() = hasFlashUnit
    val meetsRecAe: Boolean get() = isExposureCompensationSupported
    val meetsRecResolution: Boolean get() = (resolutionWidth >= 1280 && resolutionHeight >= 720) || (resolutionWidth >= 720 && resolutionHeight >= 1280)
    val meetsRecFps: Boolean get() = maxFps >= 30
}

/**
 * Camera capability inspector and diagnostic engine for evaluating hardware suitability
 * for day and night drowsiness monitoring.
 */
object CameraCapabilityInspector {

    private const val TAG = "CameraCapabilityInspector"

    @OptIn(ExperimentalCamera2Interop::class)
    fun inspect(
        cameraInfo: CameraInfo?,
        isFrontCamera: Boolean = true,
        defaultWidth: Int = 1280,
        defaultHeight: Int = 720,
        defaultFps: Int = 30
    ): CameraSpecs {
        if (cameraInfo == null) {
            return createFallbackSpecs(isFrontCamera, defaultWidth, defaultHeight, defaultFps)
        }

        val hasFlash = try {
            cameraInfo.hasFlashUnit()
        } catch (e: Exception) {
            Log.w(TAG, "Error checking flash unit", e)
            false
        }

        val exposureState = try {
            cameraInfo.exposureState
        } catch (e: Exception) {
            null
        }

        val isAeSupported = exposureState?.isExposureCompensationSupported ?: false
        val range = exposureState?.exposureCompensationRange
        val minExp = range?.lower ?: 0
        val maxExp = range?.upper ?: 0
        val stepRational = exposureState?.exposureCompensationStep
        val stepFloat = if (stepRational != null && stepRational.denominator != 0) {
            stepRational.numerator.toFloat() / stepRational.denominator.toFloat()
        } else 0f

        val zoomState = try {
            cameraInfo.zoomState.value
        } catch (e: Exception) {
            null
        }
        val minZoom = zoomState?.minZoomRatio ?: 1.0f
        val maxZoom = zoomState?.maxZoomRatio ?: 1.0f

        var maxFps = defaultFps
        var width = defaultWidth
        var height = defaultHeight

        try {
            val camera2Info = Camera2CameraInfo.from(cameraInfo)
            val fpsRanges = camera2Info.getCameraCharacteristic(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
            if (fpsRanges != null && fpsRanges.isNotEmpty()) {
                val highestFps = fpsRanges.maxOf { it.upper }
                if (highestFps > 0) {
                    maxFps = highestFps
                }
            }

            val map = camera2Info.getCameraCharacteristic(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            if (map != null) {
                val sizes = map.getOutputSizes(ImageFormat.YUV_420_888)
                    ?: map.getOutputSizes(SurfaceTexture::class.java)
                if (sizes != null && sizes.isNotEmpty()) {
                    val largest = sizes.maxByOrNull { it.width * it.height }
                    if (largest != null) {
                        width = largest.width
                        height = largest.height
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Camera2 characteristics reflection notice", e)
        }

        val resolutionLabel = formatResolutionLabel(width, height)
        val lensName = if (isFrontCamera) "Frontal" else "Trasera"

        val verdict = evaluateStatus(
            width = width,
            height = height,
            maxFps = maxFps,
            hasFlashUnit = hasFlash,
            isAeSupported = isAeSupported
        )

        return CameraSpecs(
            lensFacingName = lensName,
            isFrontCamera = isFrontCamera,
            hasFlashUnit = hasFlash,
            isExposureCompensationSupported = isAeSupported,
            exposureRangeMin = minExp,
            exposureRangeMax = maxExp,
            exposureStep = stepFloat,
            minZoomRatio = minZoom,
            maxZoomRatio = maxZoom,
            maxFps = maxFps,
            resolutionWidth = width,
            resolutionHeight = height,
            resolutionLabel = resolutionLabel,
            statusVerdict = verdict
        )
    }

    fun evaluateStatus(
        width: Int,
        height: Int,
        maxFps: Int,
        hasFlashUnit: Boolean,
        isAeSupported: Boolean
    ): CameraStatusVerdict {
        val minDim = minOf(width, height)
        val maxDim = maxOf(width, height)

        val meetsMinRes = minDim >= 480 || maxDim >= 640
        val meetsMinFps = maxFps >= 15

        if (!meetsMinRes || !meetsMinFps) {
            return CameraStatusVerdict.INSUFFICIENT
        }

        val meetsRecRes = minDim >= 720 || maxDim >= 1280
        val meetsRecFps = maxFps >= 30

        if (!hasFlashUnit) {
            return CameraStatusVerdict.LIMITED_LOW_LIGHT
        }

        if (meetsRecRes && meetsRecFps && isAeSupported) {
            return CameraStatusVerdict.OPTIMAL
        }

        return CameraStatusVerdict.COMPATIBLE
    }

    fun formatResolutionLabel(w: Int, h: Int): String {
        val minDim = minOf(w, h)
        return when {
            minDim >= 1080 -> "1080p ($w x $h)"
            minDim >= 720 -> "720p ($w x $h)"
            minDim >= 480 -> "480p ($w x $h)"
            else -> "${minDim}p ($w x $h)"
        }
    }

    fun createFallbackSpecs(
        isFrontCamera: Boolean = true,
        width: Int = 1280,
        height: Int = 720,
        fps: Int = 30,
        hasFlashUnit: Boolean = false,
        isAeSupported: Boolean = true
    ): CameraSpecs {
        val verdict = evaluateStatus(width, height, fps, hasFlashUnit, isAeSupported)
        return CameraSpecs(
            lensFacingName = if (isFrontCamera) "Frontal" else "Trasera",
            isFrontCamera = isFrontCamera,
            hasFlashUnit = hasFlashUnit,
            isExposureCompensationSupported = isAeSupported,
            exposureRangeMin = -4,
            exposureRangeMax = 4,
            exposureStep = 0.5f,
            minZoomRatio = 1.0f,
            maxZoomRatio = 4.0f,
            maxFps = fps,
            resolutionWidth = width,
            resolutionHeight = height,
            resolutionLabel = formatResolutionLabel(width, height),
            statusVerdict = verdict
        )
    }
}
