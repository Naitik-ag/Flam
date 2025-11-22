package com.example.flam.data.nat

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NativeRepository @Inject constructor() {

    fun testNative(): String {
        return NativeBridge.test()
    }
    fun processFrame(
        frameData: ByteArray,
        width: Int,
        height: Int
    ): ByteArray{
        return NativeBridge.processFrame(
            frameData,
            width,
            height
        );
    }
}