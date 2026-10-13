package com.example.aplicaciondesomnolencia.camera

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.aplicaciondesomnolencia.mediapipe.FaceLandmarkerHelper

/**
 * CameraX ImageAnalysis.Analyzer that forwards camera frames to MediaPipe FaceLandmarkerHelper.
 */
class CameraAnalyzer(
    private val faceLandmarkerHelper: FaceLandmarkerHelper,
    private val isFrontCamera: Boolean = true
) : ImageAnalysis.Analyzer {

    override fun analyze(imageProxy: ImageProxy) {
        try {
            if (!faceLandmarkerHelper.isPrepared()) {
                imageProxy.close()
                return
            }
            faceLandmarkerHelper.detectLiveStream(
                imageProxy = imageProxy,
                isFrontCamera = isFrontCamera
            )
        } catch (e: Exception) {
            Log.e("CameraAnalyzer", "Error in CameraAnalyzer analyze loop: ${e.message}", e)
            try {
                imageProxy.close()
            } catch (_: Exception) {}
        }
    }
}
