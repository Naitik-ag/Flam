package com.example.flam.data.nat

object NativeBridge {

    init {
        System.loadLibrary("native-lib")
    }

    external fun test(): String

    external fun processFrameNV21(
        frame: ByteArray,
        width: Int,
        height: Int,
        mode: Int,
        t1: Int,
        t2: Int
    ): ByteArray


    external fun testOpenCV(): String
}