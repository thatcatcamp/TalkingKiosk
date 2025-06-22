package com.capricallctx.campkiosk.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*



@Composable
fun rememberFaceDetector(): FaceDetector {
    val context = LocalContext.current
    return remember { FaceDetector(context) }
}

class FaceDetector(private val context: Context) {
    private var _humanPresent = mutableStateOf(false)
    val humanPresent: State<Boolean> = _humanPresent

    private var _lastDetectionTime = mutableStateOf(0L)
    val lastDetectionTime: State<Long> = _lastDetectionTime

    private var cameraManager: CameraManager? = null
    private var captureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private var imageReader: ImageReader? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var detectionJob: Job? = null
    
    companion object {
        private const val TAG = "FaceDetector"
    }

    fun startDetection() {
        Log.d(TAG, "startDetection() called")
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Camera permission not granted")
            return
        }
        
        Log.d(TAG, "Camera permission granted, starting detection")
        startBackgroundThread()
        openCamera()
        startPresenceTimeout()
    }

    fun stopDetection() {
        Log.d(TAG, "stopDetection() called")
        closeCamera()
        stopBackgroundThread()
        detectionJob?.cancel()
        _humanPresent.value = false
    }
    
    fun testDetection() {
        Log.d(TAG, "testDetection() - manually triggering detection")
        _humanPresent.value = true
        _lastDetectionTime.value = System.currentTimeMillis()
    }

    private fun startPresenceTimeout() {
        Log.d(TAG, "Starting presence timeout coroutine")
        detectionJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(2000) // Check every 2 seconds
                val timeSinceLastDetection = System.currentTimeMillis() - _lastDetectionTime.value
                Log.d(TAG, "Time since last detection: ${timeSinceLastDetection}ms")
                if (timeSinceLastDetection > 5000) { // 5 seconds timeout
                    if (_humanPresent.value) {
                        Log.d(TAG, "Setting human present to false (timeout)")
                        _humanPresent.value = false
                    }
                }
            }
        }
    }

    private fun startBackgroundThread() {
        Log.d(TAG, "Starting background thread")
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread?.looper!!)
        Log.d(TAG, "Background thread started")
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        backgroundThread?.join()
        backgroundThread = null
        backgroundHandler = null
    }

    private fun openCamera() {
        Log.d(TAG, "openCamera() called")
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            val frontCameraId = getFrontCameraId()
            if (frontCameraId == null) {
                Log.e(TAG, "No front camera found")
                return
            }
            Log.d(TAG, "Found front camera: $frontCameraId")

            // Very low resolution for performance on old tablets
            imageReader = ImageReader.newInstance(160, 120, ImageFormat.YUV_420_888, 1)
            imageReader?.setOnImageAvailableListener({ reader ->
                Log.d(TAG, "Image available for processing")
                val image = reader.acquireLatestImage()
                if (image != null) {
                    processImage(image)
                    image.close()
                }
            }, backgroundHandler)

            Log.d(TAG, "Opening camera...")
            cameraManager?.openCamera(frontCameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.d(TAG, "Camera opened successfully")
                    cameraDevice = camera
                    createCaptureSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.w(TAG, "Camera disconnected")
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e(TAG, "Camera error: $error")
                    camera.close()
                    cameraDevice = null
                }
            }, backgroundHandler)

        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception opening camera", e)
        } catch (e: Exception) {
            Log.e(TAG, "Exception opening camera", e)
        }
    }

    private fun getFrontCameraId(): String? {
        return try {
            cameraManager?.cameraIdList?.find { id ->
                val characteristics = cameraManager?.getCameraCharacteristics(id)
                characteristics?.get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_FRONT
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun createCaptureSession() {
        val surfaces = listOf(imageReader?.surface!!)

        cameraDevice?.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                startRepeatingRequest()
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                // Handle failure
            }
        }, backgroundHandler)
    }

    private fun startRepeatingRequest() {
        val requestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        requestBuilder?.addTarget(imageReader?.surface!!)

        captureSession?.setRepeatingRequest(
            requestBuilder?.build()!!,
            null,
            backgroundHandler
        )
    }

    private fun processImage(image: android.media.Image) {
        // Very simple presence detection for old tablets
        // Just check for significant changes in the image brightness
        try {
            val buffer = image.planes[0].buffer
            val data = ByteArray(buffer.remaining())
            buffer.get(data)

            // Simple motion/presence detection by checking brightness variation
            var brightnessSum = 0L
            val sampleSize = minOf(data.size, 1000) // Sample first 1000 bytes for performance

            for (i in 0 until sampleSize step 10) {
                brightnessSum += (data[i].toInt() and 0xFF)
            }

            val averageBrightness = brightnessSum / (sampleSize / 10)

            Log.d(TAG, "Average brightness: $averageBrightness")

            // Very basic presence detection - if there's significant brightness variation
            // This indicates something (likely a person) is in front of the camera
            if (averageBrightness in 30..200) { // Reasonable range for human presence
                Log.d(TAG, "Human presence detected!")
                _humanPresent.value = true
                _lastDetectionTime.value = System.currentTimeMillis()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing image", e)
        }
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null

        cameraDevice?.close()
        cameraDevice = null

        imageReader?.close()
        imageReader = null
    }
}
