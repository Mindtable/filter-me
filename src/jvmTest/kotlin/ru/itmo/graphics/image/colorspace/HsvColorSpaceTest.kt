package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel
import kotlin.test.Test
import kotlin.test.assertEquals

class HsvColorSpaceTest {
    companion object {
        @JvmStatic
        private val tolerance = 0.001f

        @JvmStatic
        private val rgbHsvPixels = List(3) {
            Pair(
                Pixel(1f, 1f, 1f),
                Pixel(0f, 0f, 1f),
            )
            Pair(
                Pixel(0.4f, 0.2f, 0.6f),
                Pixel(0.75f, 0.6667f, 0.6f),
            )
            Pair(
                Pixel(0.431373f, 0.878431f, 0.062745f),
                Pixel(0.258f, 0.9286f, 0.8784f),
            )
        }
    }

    @Test
    fun transitional() {
        for (pair in rgbHsvPixels) {
            val result = HsvColorSpace.toRgb(HsvColorSpace.fromRgb(pair.first))
            compare(pair.first, result)
        }
    }

    @Test
    fun toRgb() {
        for (pair in rgbHsvPixels) {
            val result = HsvColorSpace.toRgb(pair.second)
            compare(pair.first, result)
        }
    }

    @Test
    fun fromRgb() {
        for (pair in rgbHsvPixels) {
            val result = HsvColorSpace.fromRgb(pair.first)
            compare(pair.second, result)
        }
    }

    private fun compare(expected: Pixel, actual: Pixel) {
        assertEquals(expected.channelOne, actual.channelOne, tolerance)
        assertEquals(expected.channelTwo, actual.channelTwo, tolerance)
        assertEquals(expected.channelThree, actual.channelThree, tolerance)
    }
}
