package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel
import ru.itmo.graphics.viewmodel.domain.image.colorspace.HsvColorSpace
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

    private fun fromRgb(pixel: Pixel): Pixel {
        val bb = MutableList(3) { 0f }
        bb[0] = pixel.channelOne
        bb[1] = pixel.channelTwo
        bb[2] = pixel.channelThree
        HsvColorSpace.fromRgb(bb)
        return Pixel(bb[0], bb[1], bb[2])
    }

    private fun toRgb(pixel: Pixel): Pixel {
        val bb = MutableList(3) { 0f }
        bb[0] = pixel.channelOne
        bb[1] = pixel.channelTwo
        bb[2] = pixel.channelThree
        HsvColorSpace.toRgb(bb)
        return Pixel(bb[0], bb[1], bb[2])
    }

    @Test
    fun transitional() {
        for (pair in rgbHsvPixels) {
            val result = toRgb(fromRgb(pair.first))
            compare(pair.first, result)
        }
    }

    @Test
    fun toRgb() {
        for (pair in rgbHsvPixels) {
            val result = toRgb(pair.second)
            compare(pair.first, result)
        }
    }

    @Test
    fun fromRgb() {
        for (pair in rgbHsvPixels) {
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
