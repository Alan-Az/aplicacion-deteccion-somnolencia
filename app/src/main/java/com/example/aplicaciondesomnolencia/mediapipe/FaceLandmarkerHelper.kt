package com.example.aplicaciondesomnolencia.mediapipe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.aplicaciondesomnolencia.util.ModelDownloader
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker.FaceLandmarkerOptions
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult

class FaceLandmarkerHelper(
    var minFaceDetectionConfidence: Float = DEFAULT_FACE_DETECTION_CONFIDENCE,
    var minFaceTrackingConfidence: Float = DEFAULT_FACE_TRACKING_CONFIDENCE,
    var minFacePresenceConfidence: Float = DEFAULT_FACE_PRESENCE_CONFIDENCE,
    var maxNumFaces: Int = DEFAULT_NUM_FACES,
    var currentDelegate: Int = DELEGATE_CPU,
    var runningMode: RunningMode = RunningMode.LIVE_STREAM,
    val context: Context,
    var faceLandmarkerHelperListener: LandmarkerListener? = null
) {

    private var faceLandmarker: FaceLandmarker? = null
    private var lastTargetTimestampMs: Long = -1L

    init {
        setupFaceLandmarker()
    }

    fun clearFaceLandmarker() {
        faceLandmarker?.close()
        faceLandmarker = null
    }

    fun isPrepared(): Boolean = faceLandmarker != null

    fun setupFaceLandmarker() {
        val baseOptionsBuilder = BaseOptions.builder()

        when (currentDelegate) {
            DELEGATE_CPU -> baseOptionsBuilder.setDelegate(Delegate.CPU)
            DELEGATE_GPU -> baseOptionsBuilder.setDelegate(Delegate.GPU)
        }

        // Always use the asset path for MediaPipe BaseOptions
        baseOptionsBuilder.setModelAssetPath(ModelDownloader.MODEL_FILE_NAME)

        try {
            val baseOptions = baseOptionsBuilder.build()
            val optionsBuilder = FaceLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinFaceDetectionConfidence(minFaceDetectionConfidence)
                .setMinTrackingConfidence(minFaceTrackingConfidence)
                .setMinFacePresenceConfidence(minFacePresenceConfidence)
                .setNumFaces(maxNumFaces)
                .setRunningMode(runningMode)

            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                    .setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError)
            }

            val options = optionsBuilder.build()
            faceLandmarker = FaceLandmarker.createFromOptions(context, options)
            Log.d(TAG, "FaceLandmarker initialized successfully with delegate $currentDelegate.")
        } catch (e: Exception) {
            Log.e(TAG, "MediaPipe FaceLandmarker failed to load with delegate $currentDelegate: ${e.message}", e)
            if (currentDelegate == DELEGATE_GPU) {
                Log.w(TAG, "GPU delegate failed. Retrying setup with CPU delegate fallback...")
                currentDelegate = DELEGATE_CPU
                setupFaceLandmarker()
            } else {
                faceLandmarkerHelperListener?.onError(
                    "FaceLandmarker failed to initialize: " + e.message,
                    GPU_ERROR
                )
            }
        }
    }

    fun detectLiveStream(
        imageProxy: ImageProxy,
        isFrontCamera: Boolean
    ) {
        if (runningMode != RunningMode.LIVE_STREAM) {
            imageProxy.close()
            throw IllegalArgumentException(
                "Attempting to call detectLiveStream while not using RunningMode.LIVE_STREAM"
            )
        }

        try {
            var frameTime = SystemClock.uptimeMillis()
            if (frameTime <= lastTargetTimestampMs) {
                frameTime = lastTargetTimestampMs + 1
            }
            lastTargetTimestampMs = frameTime

            // Convert ImageProxy to Bitmap
            val bitmapBuffer = imageProxy.toBitmap()

            // Rotate bitmap according to camera orientation
            val matrix = Matrix().apply {
                postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                if (isFrontCamera) {
                    postScale(-1f, 1f, bitmapBuffer.width / 2f, bitmapBuffer.height / 2f)
                }
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer,
                0,
                0,
                bitmapBuffer.width,
                bitmapBuffer.height,
                matrix,
                true
            )

            val mpImage = BitmapImageBuilder(rotatedBitmap).build()
            detectAsync(mpImage, frameTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame in detectLiveStream: ${e.message}", e)
        } finally {
            // ALWAYS close ImageProxy to prevent CameraX analyzer pipeline freeze
            imageProxy.close()
        }
    }

    private fun detectAsync(mpImage: MPImage, frameTime: Long) {
        faceLandmarker?.detectAsync(mpImage, frameTime)
    }

    private fun returnLivestreamResult(
        result: FaceLandmarkerResult,
        inputImage: MPImage
    ) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()

        faceLandmarkerHelperListener?.onResults(
            ResultBundle(
                results = listOf(result),
                inferenceTime = inferenceTime,
                inputImageHeight = inputImage.height,
                inputImageWidth = inputImage.width
            )
        )
    }

    private fun returnLivestreamError(error: RuntimeException) {
        faceLandmarkerHelperListener?.onError(
            error.message ?: "An unknown error occurred"
        )
    }

    companion object {
        private const val TAG = "FaceLandmarkerHelper"

        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1

        const val DEFAULT_FACE_DETECTION_CONFIDENCE = 0.5f
        const val DEFAULT_FACE_TRACKING_CONFIDENCE = 0.5f
        const val DEFAULT_FACE_PRESENCE_CONFIDENCE = 0.5f
        const val DEFAULT_NUM_FACES = 1

        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1
    }

    data class ResultBundle(
        val results: List<FaceLandmarkerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int
    )

    interface LandmarkerListener {
        fun onError(error: String, errorCode: Int = OTHER_ERROR)
        fun onResults(resultBundle: ResultBundle)
    }
}
