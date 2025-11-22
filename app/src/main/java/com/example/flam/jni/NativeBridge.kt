package com.example.flam.jni

object NativeBridge {

    init {
        System.loadLibrary("native-lib")
    }

    external fun test(): String
}