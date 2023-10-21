package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel
import ru.itmo.graphics.viewmodel.domain.image.colorspace.YCbCr601ColorSpace
import kotlin.test.Test
import kotlin.test.assertEquals

class YCbCrColorSpaceTest {
    companion object {
        @JvmStatic
        private val tolerance = 0.001f

        @JvmStatic
        private val rgbYcbcrPixels = List(2) {
            Pair(
                Pixel(1f, 1f, 1f),
                Pixel(1f, 0f, 0f),
            )
            Pair(
                Pixel(1f, 0f, 1f),
                Pixel(0.5f, 0.5f, 0.5f),
            )
        }
    }

    private fun fromRgb(pixel: Pixel): Pixel {
        val bb = MutableList(3) { 0f }
        bb[0] = pixel.channelOne
        bb[1] = pixel.channelTwo
        bb[2] = pixel.channelThree
        YCbCr601ColorSpace.fromRgb(bb)
        return Pixel(bb[0], bb[1], bb[2])
    }

    private fun toRgb(pixel: Pixel): Pixel {
        val bb = MutableList(3) { 0f }
        bb[0] = pixel.channelOne
        bb[1] = pixel.channelTwo
        bb[2] = pixel.channelThree
        YCbCr601ColorSpace.toRgb(bb)
        return Pixel(bb[0], bb[1], bb[2])
    }

    @Test
    fun transitional() {
        for (pair in rgbYcbcrPixels) {
            val result = toRgb(fromRgb(pair.first))
            compare(pair.first, result)
        }
    }

    private fun compare(expected: Pixel, actual: Pixel) {
        assertEquals(expected.channelOne, actual.channelOne, tolerance)
        assertEquals(expected.channelTwo, actual.channelTwo, tolerance)
        assertEquals(expected.channelThree, actual.channelThree, tolerance)
    }
}
