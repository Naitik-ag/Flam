package com.example.flam.data.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CameraController @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null

    private val cameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val TAG = "CameraController"

    private fun getBackCameraId(): String {
        for (id in cameraManager.cameraIdList) {
            val chars = cameraManager.getCameraCharacteristics(id)
            if (chars.get(CameraCharacteristics.LENS_FACING) ==
                CameraCharacteristics.LENS_FACING_BACK
            ) return id
        }
        throw IllegalStateException("No back camera found on device")
    }

    @SuppressLint("MissingPermission")
    fun start(callback: (Image) -> Unit) {
        val cameraId = getBackCameraId()

        imageReader = ImageReader.newInstance(
            1280,
            720,
            ImageFormat.YUV_420_888,
            3
        )

        imageReader!!.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            if (image != null) {
                callback(image)
            }
        }, null)

        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {

            override fun onOpened(device: CameraDevice) {
                cameraDevice = device
                createSession()
            }

            override fun onDisconnected(device: CameraDevice) {
                device.close()
            }

            override fun onError(device: CameraDevice, error: Int) {
                Log.e(TAG, "Camera error: $error")
                device.close()
            }

        }, null)
    }

    private fun createSession() {
        val cam = cameraDevice ?: return
        val readerSurface = imageReader?.surface ?: return

        cam.createCaptureSession(
            listOf(readerSurface),
            object : CameraCaptureSession.StateCallback() {

                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session

                    val request = cam.createCaptureRequest(
                        CameraDevice.TEMPLATE_PREVIEW
                    ).apply {
                        addTarget(readerSurface)
                        set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    }

                    session.setRepeatingRequest(request.build(), null, null)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(TAG, "Capture session config failed")
                }

            },
            null
        )
    }

    fun stop() {
        try {
            captureSession?.close()
            cameraDevice?.close()
            imageReader?.close()
        } catch (_: Exception) { }
    }
}
