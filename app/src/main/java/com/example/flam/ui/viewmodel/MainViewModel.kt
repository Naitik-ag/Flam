package com.example.flam.ui.viewmodel

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flam.data.camera.CameraController
import com.example.flam.data.camera.FrameExtractor
import com.example.flam.data.nat.NativeRepository
import com.example.flam.data.web.WSClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import kotlin.math.max
import kotlin.system.measureTimeMillis

@HiltViewModel
class MainViewModel @Inject constructor(
    private val cameraController: CameraController,
    private val nativeRepository: NativeRepository
) : ViewModel() {

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap = _bitmap.asStateFlow()

    private var outputBitmap: Bitmap? = null
    private var width = 0
    private var height = 0
    val frameWidth: Int get() = width
    val frameHeight: Int get() = height
    @Volatile
    private var isProcessing = false

    private val _fps = MutableStateFlow(0f)
    val fps = _fps.asStateFlow()

    var mode = MutableStateFlow(0)
    var t1 = MutableStateFlow(50)
    var t2 = MutableStateFlow(150)

    private val _processingMs = MutableStateFlow(0L)
    val processingMs = _processingMs.asStateFlow()

    // Frame resolution exposed as flows (for UI stats panel)
    private val _frameWidth = MutableStateFlow(0)
    val frameWidthFlow = _frameWidth.asStateFlow()

    private val _frameHeight = MutableStateFlow(0)
    val frameHeightFlow = _frameHeight.asStateFlow()

    fun startCamera() {
        cameraController.start { image ->
            if (width == 0 || height == 0) {
                width = image.width
                height = image.height
                outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                _frameWidth.value = width
                _frameHeight.value = height
            }

            val nv21 = FrameExtractor.extract(image)
            processFrame(nv21)
        }
    }



    fun stopCamera() = cameraController.stop()

    private fun processFrame(nv21: ByteArray) {
        if (isProcessing) return
        isProcessing = true

        viewModelScope.launch(Dispatchers.Default) {
            var rgba: ByteArray

            val timeMs = measureTimeMillis {
                rgba = nativeRepository.processNV21(
                    nv21,
                    width,
                    height,
                    mode.value,
                    t1.value,
                    t2.value
                )
            }

            _processingMs.value = timeMs

            val fpsValue = if (timeMs > 0) 1000f / timeMs else 0f
            _fps.value = fpsValue

            outputBitmap?.let { bmp ->
                val buffer = java.nio.ByteBuffer.wrap(rgba)
                bmp.copyPixelsFromBuffer(buffer)
                _bitmap.value = bmp
            }

            isProcessing = false
            pushFrameToGL(rgba)
        }

    }

    val glFrame = AtomicReference<ByteBuffer?>(null)
    val glRequest = MutableSharedFlow<Unit>(replay = 0)
    private fun pushFrameToGL(rgba: ByteArray) {
        val buf = ByteBuffer.allocateDirect(rgba.size).order(ByteOrder.nativeOrder())
        buf.put(rgba)
        buf.position(0)
        glFrame.set(buf)
        // signal GLSurfaceView to render (non-blocking)
        viewModelScope.launch { glRequest.emit(Unit) }
    }

    fun captureSnapshot(): String? {
        val bmp = _bitmap.value ?: return null

        // Rotate bitmap 90 degrees clockwise for web
        val matrix = Matrix().apply { postRotate(90f) }
        val rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, false)

        val stream = ByteArrayOutputStream()
        rotatedBmp.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val jpegBytes = stream.toByteArray()

        if (rotatedBmp != bmp) rotatedBmp.recycle()

        return Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
    }

    private val ws = WSClient("ws://192.168.1.34:8080")

    fun connectWeb() {
        Log.d("WS_CLIENT", "Trying to connect to WS...")
        ws.connect()
    }

    fun sendSnapshotToWeb() {
        val b64 = captureSnapshot() ?: run {
            Log.e("WS_CLIENT", "Snapshot was null")
            return
        }
        Log.d("WS_CLIENT", "Captured snapshot base64 length = ${b64.length}")
        ws.sendBase64(b64)
    }
}
