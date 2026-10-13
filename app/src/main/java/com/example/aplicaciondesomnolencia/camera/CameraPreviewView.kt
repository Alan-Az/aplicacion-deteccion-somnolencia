package com.example.aplicaciondesomnolencia.camera

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.aplicaciondesomnolencia.mediapipe.FaceLandmarkerHelper
import java.util.concurrent.Executors

/**
 * CameraX Preview composable configured with camera feed, ImageAnalysis pipeline,
 * night mode/torch assistance, and auto exposure compensation optimization.
 */
@Composable
fun CameraPreviewView(
    faceLandmarkerHelper: FaceLandmarkerHelper,
    modifier: Modifier = Modifier,
    isFrontCamera: Boolean = true,
    isTorchEnabled: Boolean = false,
    onCameraSpecsInspected: ((CameraSpecs) -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var activeCamera by remember { mutableStateOf<Camera?>(null) }

    key(isFrontCamera) {
        val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

        val cameraSelector = if (isFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        LaunchedEffect(activeCamera, isTorchEnabled) {
            val camera = activeCamera ?: return@LaunchedEffect
            try {
                if (camera.cameraInfo.hasFlashUnit()) {
                    camera.cameraControl.enableTorch(isTorchEnabled)
                }
            } catch (e: Exception) {
                Log.e("CameraPreviewView", "Error controlling torch state", e)
            }

            try {
                val exposureState = camera.cameraInfo.exposureState
                if (exposureState.isExposureCompensationSupported) {
                    val range = exposureState.exposureCompensationRange
                    val targetIndex = if (isTorchEnabled) {
                        (range.upper / 2).coerceIn(range.lower, range.upper)
                    } else {
                        0
                    }
                    camera.cameraControl.setExposureCompensationIndex(targetIndex)
                }
            } catch (e: Exception) {
                Log.e("CameraPreviewView", "Error adjusting exposure compensation", e)
            }
        }

        Box(modifier = modifier) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                update = { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener(
                        {
                            try {
                                val cameraProvider = cameraProviderFuture.get()

                                val preview = Preview.Builder()
                                    .build()
                                    .also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                                    .build()
                                    .also {
                                        it.setAnalyzer(
                                            cameraExecutor,
                                            CameraAnalyzer(faceLandmarkerHelper, isFrontCamera),
                                        )
                                    }

                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis,
                                )
                                activeCamera = camera

                                val specs = CameraCapabilityInspector.inspect(
                                    cameraInfo = camera.cameraInfo,
                                    isFrontCamera = isFrontCamera
                                )
                                onCameraSpecsInspected?.invoke(specs)
                            } catch (e: Exception) {
                                Log.e("CameraPreviewView", "Camera binding failed", e)
                            }
                        },
                        ContextCompat.getMainExecutor(context),
                    )
                },
                modifier = Modifier.fillMaxSize(),
            )
        }

        DisposableEffect(lifecycleOwner, isFrontCamera) {
            onDispose {
                try {
                    val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                    cameraProvider.unbindAll()
                } catch (e: Exception) {
                    Log.e("CameraPreviewView", "Error unbinding camera provider", e)
                }
                cameraExecutor.shutdown()
            }
        }
    }
}
