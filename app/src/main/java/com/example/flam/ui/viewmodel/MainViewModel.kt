package com.example.flam.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.flam.data.camera.CameraController
import com.example.flam.data.camera.FrameExtractor
import com.example.flam.data.nat.NativeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val cameraController: CameraController,
    private val nativeRepository: NativeRepository
) : ViewModel() {

    fun startCamera(onFrame: (ByteArray) -> Unit) {
        cameraController.start { image ->
            val bytes = FrameExtractor.extract(image)
            onFrame(bytes)
        }
    }

    fun stopCamera() {
        cameraController.stop()
    }

    fun testJNI() {
        val msg = nativeRepository.testNative()
        Log.d("JNI_TEST", msg)
    }

}
