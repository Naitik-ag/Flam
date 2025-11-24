package com.example.flam.data.camera

import android.media.Image

object FrameExtractor {

    fun extract(image: Image): ByteArray {
        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        // Allocate Y + V + U (NV21 format: Y + VU interleaved)
        val out = ByteArray(ySize + vSize + uSize)

        // Copy Y
        yBuffer.get(out, 0, ySize)

        // Copy V (plane 2)
        vBuffer.get(out, ySize, vSize)

        // Copy U (plane 1)
        uBuffer.get(out, ySize + vSize, uSize)

        image.close()
        return out
    }
}
