package com.example.aplicaciondesomnolencia.mediapipe

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

/**
 * Utility for accessing left and right eye MediaPipe face landmarks.
 *
 * Left Eye Indices: 362, 385, 387, 263, 373, 380
 * Right Eye Indices: 33, 160, 158, 133, 153, 144
 */
object EyeLandmarks {
    val LEFT_EYE_INDICES = listOf(362, 385, 387, 263, 373, 380)
    val RIGHT_EYE_INDICES = listOf(33, 160, 158, 133, 153, 144)

    fun extractLandmarks(
        faceLandmarks: List<NormalizedLandmark>,
        indices: List<Int>
    ): List<NormalizedLandmark> {
        return indices.mapNotNull { index ->
            if (index in faceLandmarks.indices) faceLandmarks[index] else null
        }
    }

    fun getLeftEyeLandmarks(faceLandmarks: List<NormalizedLandmark>): List<NormalizedLandmark> {
        return extractLandmarks(faceLandmarks, LEFT_EYE_INDICES)
    }

    fun getRightEyeLandmarks(faceLandmarks: List<NormalizedLandmark>): List<NormalizedLandmark> {
        return extractLandmarks(faceLandmarks, RIGHT_EYE_INDICES)
    }
}
