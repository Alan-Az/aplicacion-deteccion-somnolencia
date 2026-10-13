package com.example.aplicaciondesomnolencia.biometrics

import com.example.aplicaciondesomnolencia.mediapipe.EyeLandmarks
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.sqrt

/**
 * Biometric EAR (Eye Aspect Ratio) Calculator.
 *
 * Computes the ratio of vertical distance to horizontal distance for eye landmarks.
 *
 * Left Eye Indices: [362, 385, 387, 263, 373, 380]
 *   p1=362, p2=385, p3=387, p4=263, p5=373, p6=380
 *
 * Right Eye Indices: [33, 160, 158, 133, 153, 144]
 *   p1=33, p2=160, p3=158, p4=133, p5=153, p6=144
 *
 * Formula: EAR = (||p2 - p6|| + ||p3 - p5||) / (2 * ||p1 - p4||)
 */
object EarCalculator {

    /**
     * Calculates Euclidean distance between two landmarks.
     *
     * @param p1 First normalized landmark
     * @param p2 Second normalized landmark
     * @param use3D True to include z-axis in Euclidean distance calculation
     */
    fun euclideanDistance(
        p1: NormalizedLandmark,
        p2: NormalizedLandmark,
        use3D: Boolean = false,
    ): Float {
        val dx = p1.x() - p2.x()
        val dy = p1.y() - p2.y()
        return if (use3D) {
            val dz = p1.z() - p2.z()
            sqrt((dx * dx) + (dy * dy) + (dz * dz))
        } else {
            sqrt((dx * dx) + (dy * dy))
        }
    }

    /**
     * Calculates Eye Aspect Ratio (EAR) for a single eye given 6 ordered landmarks:
     * [p1, p2, p3, p4, p5, p6].
     */
    fun calculateSingleEyeEar(
        eyeLandmarks: List<NormalizedLandmark>,
        use3D: Boolean = false,
    ): Float {
        if (eyeLandmarks.size < 6) return 0.0f

        val p1 = eyeLandmarks[0]
        val p2 = eyeLandmarks[1]
        val p3 = eyeLandmarks[2]
        val p4 = eyeLandmarks[3]
        val p5 = eyeLandmarks[4]
        val p6 = eyeLandmarks[5]

        val distP2P6 = euclideanDistance(p2, p6, use3D)
        val distP3P5 = euclideanDistance(p3, p5, use3D)
        val distP1P4 = euclideanDistance(p1, p4, use3D)

        if (distP1P4 == 0.0f) return 0.0f

        return (distP2P6 + distP3P5) / (2.0f * distP1P4)
    }

    /**
     * Calculates the average EAR between left and right eyes from full face landmarks.
     */
    fun calculateEar(
        faceLandmarks: List<NormalizedLandmark>,
        use3D: Boolean = false,
    ): Float {
        val leftEye = EyeLandmarks.getLeftEyeLandmarks(faceLandmarks)
        val rightEye = EyeLandmarks.getRightEyeLandmarks(faceLandmarks)

        val leftEar = if (leftEye.size == 6) calculateSingleEyeEar(leftEye, use3D) else 0.0f
        val rightEar = if (rightEye.size == 6) calculateSingleEyeEar(rightEye, use3D) else 0.0f

        return when {
            leftEye.size == 6 && rightEye.size == 6 -> (leftEar + rightEar) / 2.0f
            leftEye.size == 6 -> leftEar
            rightEye.size == 6 -> rightEar
            else -> 0.0f
        }
    }
}
