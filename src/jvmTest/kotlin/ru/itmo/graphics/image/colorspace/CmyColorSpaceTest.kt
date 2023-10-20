package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel
import kotlin.test.Test
import kotlin.test.assertEquals

class CmyColorSpaceTest {
    companion object {
        @JvmStatic
        private val rgbCmyPixels = List(3) {
            Pair(
                Pixel(0.3f, 1f, 0.5f),
                Pixel(0.7f, 0f, 0.5f),
            )
            Pair(
                Pixel(0.3324f, 0.99432f, 0.0032f),
                Pixel(0.6676f, 0.00568f, 0.9968f),
            )
            Pair(
                Pixel(0f, 0f, 0f),
                Pixel(1f, 1f, 1f),
            )
        }
    }

    private fun fromRgb(pixel: Pixel): Pixel {
        val bb: Array<Float> = Array(3) { 0f }
        bb[0] = pixel.channelOne
        bb[1] = pixel.channelTwo
        bb[2] = pixel.channelThree
        CmyColorSpace.fromRgb(bb)
        return Pixel(bb[0], bb[1], bb[2])
    }

    private fun toRgb(pixel: Pixel): Pixel {
        val bb: Array<Float> = Array(3) { 0f }
        bb[0] = pixel.channelOne
        bb[1] = pixel.channelTwo
        bb[2] = pixel.channelThree
        CmyColorSpace.toRgb(bb)
        return Pixel(bb[0], bb[1], bb[2])
    }

    @Test
    fun transitional() {
        for (pair in rgbCmyPixels) {
            val result = toRgb(fromRgb(pair.first))
            assertEquals(pair.first, result)
        }
    }

    @Test
    fun toRgb() {
        for (pair in rgbCmyPixels) {
            assertEquals(pair.first, toRgb(pair.second))
        }
    }

    @Test
    fun fromRgb() {
        for (pair in rgbCmyPixels) {
            assertEquals(pair.second, fromRgb(pair.first))
        }
    }
}
