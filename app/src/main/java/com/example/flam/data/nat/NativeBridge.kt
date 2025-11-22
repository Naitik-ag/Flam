package com.example.flam.data.nat

object NativeBridge {

    init {
        System.loadLibrary("native-lib")
    }

    external fun test(): String
}