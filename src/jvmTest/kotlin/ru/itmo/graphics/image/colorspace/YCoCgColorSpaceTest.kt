package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel
import kotlin.test.Test
import kotlin.test.assertEquals

class YCoCgColorSpaceTest {
    companion object {
        @JvmStatic
        private val tolerance = 0.001f

        @JvmStatic
        private val rgbYcocgPixels = List(3) {
            Pair(
                Pixel(1f, 1f, 1f),
                Pixel(1f, 0f, 0f),
            )
            Pair(
                Pixel(0.2f, 0.4f, 0.6f),
                Pixel(0.4f, -0.2f, 0f),
            )
            Pair(
                Pixel(0.749f, 0.332f, 0.11f),
                Pixel(0.38075f, 0.3195f, -0.04875f),
            )
        }
    }

    private fun fromRgb(pixel: Pixel): Pixel {
        val bb: Array<Float> = Array(3) { 0f }
        bb[0] = pixel.channelOne
        bb[1] = pixel.channelTwo
        bb[2] = pixel.channelThree
        YCoCgColorSpace.fromRgb(bb)
        return Pixel(bb[0], bb[1], bb[2])
    }

    private fun toRgb(pixel: Pixel): Pixel {
        val bb: Array<Float> = Array(3) { 0f }
        bb[0] = pixel.channelOne
        bb[1] = pixel.channelTwo
        bb[2] = pixel.channelThree
        YCoCgColorSpace.toRgb(bb)
        return Pixel(bb[0], bb[1], bb[2])
    }

    @Test
    fun transitional() {
        for (pair in rgbYcocgPixels) {
            val result = toRgb(fromRgb(pair.first))
            compare(pair.first, result)
        }
    }

    @Test
    fun toRgb() {
        for (pair in rgbYcocgPixels) {
            val result = toRgb(pair.second)
            compare(pair.first, result)
        }
    }

    @Test
    fun fromRgb() {
        for (pair in rgbYcocgPixels) {
            val result = fromRgb(pair.first)
            compare(pair.second, result)
        }
    }

    private fun compare(expected: Pixel, actual: Pixel) {
        assertEquals(expected.channelOne, actual.channelOne, tolerance)
        assertEquals(expected.channelTwo, actual.channelTwo, tolerance)
        assertEquals(expected.channelThree, actual.channelThree, tolerance)
    }
}
