package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel
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

    @Test
    fun transitional() {
        for (pair in rgbYcbcrPixels) {
            val result = YCbCr601ColorSpace.toRgb(YCbCr601ColorSpace.fromRgb(pair.first))
            compare(pair.first, result)
        }
    }

    @Test
    fun toRgb() {
        for (pair in rgbYcbcrPixels) {
            val result = YCbCr601ColorSpace.toRgb(pair.second)
            compare(pair.first, result)
        }
    }

    @Test
    fun fromRgb() {
        for (pair in rgbYcbcrPixels) {
            val result = YCbCr601ColorSpace.fromRgb(pair.first)
            compare(pair.second, result)
        }
    }

    private fun compare(expected: Pixel, actual: Pixel) {
        assertEquals(expected.channelOne, actual.channelOne, tolerance)
        assertEquals(expected.channelTwo, actual.channelTwo, tolerance)
        assertEquals(expected.channelThree, actual.channelThree, tolerance)
    }
}
