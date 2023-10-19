package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel
import kotlin.test.Test
import kotlin.test.assertEquals

class HslColorSpaceTest {
    companion object {
        @JvmStatic
        private val tolerance = 0.001f

        @JvmStatic
        private val rgbHslPixels = List(3) {
            Pair(
                Pixel(1f, 1f, 1f),
                Pixel(0f, 0f, 1f),
            )
            Pair(
                Pixel(0.4f, 0.2f, 0.6f),
                Pixel(0.75f, 0.5f, 0.4f),
            )
            Pair(
                Pixel(0.407843f, 0.898039f, 0.482353f),
                Pixel(0.358667f, 0.706f, 0.653f),
            )
        }
    }

    @Test
    fun transitional() {
        for (pair in rgbHslPixels) {
            val result = HslColorSpace.toRgb(HslColorSpace.fromRgb(pair.first))
            compare(pair.first, result)
        }
    }

    @Test
    fun toRgb() {
        for (pair in rgbHslPixels) {
            val result = HslColorSpace.toRgb(pair.second)
            compare(pair.first, result)
        }
    }

    @Test
    fun fromRgb() {
        for (pair in rgbHslPixels) {
            val result = HslColorSpace.fromRgb(pair.first)
            compare(pair.second, result)
        }
    }

    private fun compare(expected: Pixel, actual: Pixel) {
        assertEquals(expected.channelOne, actual.channelOne, tolerance)
        assertEquals(expected.channelTwo, actual.channelTwo, tolerance)
        assertEquals(expected.channelThree, actual.channelThree, tolerance)
    }
}
