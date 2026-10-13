package com.example.aplicaciondesomnolencia.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ModelDownloader {
    private const val TAG = "ModelDownloader"
    const val MODEL_FILE_NAME = "face_landmarker.task"
    private const val MODEL_URL =
        "https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/1/face_landmarker.task"

    /**
     * Ensures that the face_landmarker.task file exists either in assets or in app's internal filesDir.
     * Returns the File reference or throws an exception if missing/download fails.
     */
    suspend fun ensureModelExists(context: Context): File = withContext(Dispatchers.IO) {
        val targetFile = File(context.filesDir, MODEL_FILE_NAME)
        if (targetFile.exists() && targetFile.length() > 0) {
            Log.d(TAG, "Model file already exists in filesDir (${targetFile.length()} bytes)")
            return@withContext targetFile
        }

        // Try copying from assets
        try {
            context.assets.open(MODEL_FILE_NAME).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (targetFile.exists() && targetFile.length() > 0) {
                Log.d(TAG, "Copied model file from assets to filesDir (${targetFile.length()} bytes)")
                return@withContext targetFile
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not copy model from assets: ${e.message}")
        }

        // Download from URL if not found in assets
        Log.d(TAG, "Downloading model from $MODEL_URL...")
        val url = URL(MODEL_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.connect()

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            connection.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.d(TAG, "Model downloaded successfully (${targetFile.length()} bytes)")
        } else {
            throw IllegalStateException("Failed to download model: HTTP ${connection.responseCode}")
        }

        return@withContext targetFile
    }
}
