package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel
import kotlin.test.Test
import kotlin.test.assertEquals

class CmyColorSpaceTest {
    private val cmy = CmyColorSpace()
    private val rgbCmyPixels = List(3) {
        Pair(
            Pixel(0.3f, 1f, 0.5f),
            Pixel(0.7f, 0f, 0.5f)
        )
        Pair(
            Pixel(0.3324f, 0.99432f, 0.0032f),
            Pixel(0.6676f, 0.00568f, 0.9968f)
        )
        Pair(
            Pixel(0f, 0f, 0f),
            Pixel(1f, 1f, 1f)
        )
    }

    @Test
    fun toRgb() {
        for (pair in rgbCmyPixels){
            assertEquals(pair.first, cmy.toRgb(pair.second))
        }
    }

    @Test
    fun fromRgb() {
        for (pair in rgbCmyPixels){
            assertEquals(pair.second, cmy.fromRgb(pair.first))
        }
    }
}