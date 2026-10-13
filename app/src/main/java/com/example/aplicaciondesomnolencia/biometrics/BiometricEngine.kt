package com.example.aplicaciondesomnolencia.biometrics

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

enum class DriverState {
    ATTENTIVE,
    FATIGUE_DETECTED,
    NO_FACE_DETECTED
}

data class BiometricResult(
    val driverState: DriverState,
    val rawEar: Float,
    val smoothedEar: Float,
    val consecutiveFatigueFrames: Int,
    val riskProgress: Float,
)

/**
 * BiometricEngine tracks smoothed EAR, manages frame counters, and computes driver fatigue states.
 */
class BiometricEngine(
    var limiteEar: Float = DEFAULT_LIMITE_EAR,
    var limiteFrames: Int = DEFAULT_LIMITE_FRAMES,
    var alphaCurrent: Float = ALPHA_CURRENT,
    var alphaPrevious: Float = ALPHA_PREVIOUS
) {
    private var previousSmoothedEar: Float? = null
    var consecutiveFatigueFrames: Int = 0
        private set

    /**
     * Processes face landmarks and produces a updated BiometricResult state.
     */
    fun processLandmarks(
        faceLandmarks: List<NormalizedLandmark>?,
        use3D: Boolean = false
    ): BiometricResult {
        if (faceLandmarks.isNullOrEmpty()) {
            consecutiveFatigueFrames = 0
            val lastEar = previousSmoothedEar ?: 0.0f
            return BiometricResult(
                driverState = DriverState.NO_FACE_DETECTED,
                rawEar = 0.0f,
                smoothedEar = lastEar,
                consecutiveFatigueFrames = 0,
                riskProgress = 0.0f
            )
        }

        val rawEar = EarCalculator.calculateEar(faceLandmarks, use3D)

        val smoothedEar = if (previousSmoothedEar == null) {
            rawEar
        } else {
            (previousSmoothedEar!! * alphaPrevious) + (rawEar * alphaCurrent)
        }
        previousSmoothedEar = smoothedEar

        if (smoothedEar < limiteEar) {
            consecutiveFatigueFrames++
        } else {
            consecutiveFatigueFrames = 0
        }

        val riskProgress = if (limiteFrames > 0) {
            (consecutiveFatigueFrames.toFloat() / limiteFrames.toFloat()).coerceIn(0.0f, 1.0f)
        } else {
            1.0f
        }

        val driverState = if (consecutiveFatigueFrames >= limiteFrames) {
            DriverState.FATIGUE_DETECTED
        } else {
            DriverState.ATTENTIVE
        }

        return BiometricResult(
            driverState = driverState,
            rawEar = rawEar,
            smoothedEar = smoothedEar,
            consecutiveFatigueFrames = consecutiveFatigueFrames,
            riskProgress = riskProgress
        )
    }

    /**
     * Resets internal EMA smoothing memory and frame counter.
     */
    fun reset() {
        previousSmoothedEar = null
        consecutiveFatigueFrames = 0
    }

    companion object {
        const val DEFAULT_LIMITE_EAR = 0.15f
        const val DEFAULT_LIMITE_FRAMES = 15
        const val ALPHA_PREVIOUS = 0.7f
        const val ALPHA_CURRENT = 0.3f
    }
}
