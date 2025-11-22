package com.example.flam.data.camera

import android.media.Image

object FrameExtractor {

    fun extract(image: Image): ByteArray {
        val plane0 = image.planes[0].buffer
        val plane1 = image.planes[2].buffer

        val ySize = plane0.remaining()
        val uvSize = plane1.remaining()

        val data = ByteArray(ySize + uvSize)

        plane0.get(data, 0, ySize)
        plane1.get(data, ySize, uvSize)

        image.close()

        return data
    }
}