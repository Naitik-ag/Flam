package com.example.flam.data.nat

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NativeRepository @Inject constructor() {

    fun testNative(): String {
        return NativeBridge.test()
    }
}