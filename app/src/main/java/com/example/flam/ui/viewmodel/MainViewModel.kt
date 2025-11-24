package com.example.flam.ui.viewmodel

import android.graphics.Bitmap
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flam.data.camera.CameraController
import com.example.flam.data.camera.FrameExtractor
import com.example.flam.data.nat.NativeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
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

    @Volatile
    private var isProcessing = false

    private val _fps = MutableStateFlow(0f)
    val fps = _fps.asStateFlow()

    var mode = MutableStateFlow(0)
    var t1 = MutableStateFlow(50)
    var t2 = MutableStateFlow(150)


    fun startCamera() {
        cameraController.start { image ->

            // Must read FIRST — before extraction, because extraction closes image
            if (width == 0 || height == 0) {
                width = image.width
                height = image.height
                outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            }

            val nv21 = FrameExtractor.extract(image)  // image is closed inside extractor
            processFrame(nv21)
        }
    }



    fun stopCamera() = cameraController.stop()

    private fun processFrame(nv21: ByteArray) {
        if (isProcessing) return   // BACKPRESSURE: drop frame
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

            val fpsValue = if (timeMs > 0) 1000f / timeMs else 0f
            _fps.value = fpsValue

            outputBitmap?.let { bmp ->
                val buffer = java.nio.ByteBuffer.wrap(rgba)
                bmp.copyPixelsFromBuffer(buffer)
                _bitmap.value = bmp
            }

            isProcessing = false
        }
    }

    fun captureSnapshot(): String? {
        val bmp = _bitmap.value ?: return null

        val stream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val jpegBytes = stream.toByteArray()

        return Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
    }

    fun testJNI() = nativeRepository.testNative()

    fun testOpenCV() = nativeRepository.testOpenCV()
}
