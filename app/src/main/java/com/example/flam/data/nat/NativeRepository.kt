package com.example.flam.data.nat

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NativeRepository @Inject constructor() {

    fun testNative(): String {
        return NativeBridge.test()
    }
    fun processNV21(
        frame: ByteArray,
        width: Int,
        height: Int,
        mode: Int,
        t1: Int,
        t2: Int
    ): ByteArray {
        return NativeBridge.processFrameNV21(frame, width, height, mode, t1, t2)
    }

    fun testOpenCV(): String{
        return NativeBridge.testOpenCV();
    }
}