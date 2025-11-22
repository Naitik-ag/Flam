package com.example.flam.data.nat

object NativeBridge {

    init {
        System.loadLibrary("native-lib")
    }

    external fun test(): String

    external fun processFrame(
        frameData: ByteArray,
        width: Int,
        height: Int
    ): ByteArray
}