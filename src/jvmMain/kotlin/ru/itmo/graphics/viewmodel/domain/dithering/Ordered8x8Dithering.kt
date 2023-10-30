package ru.itmo.graphics.viewmodel.domain.dithering

import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.domain.image.colorspace.ApplicationColorSpace
import ru.itmo.graphics.viewmodel.tools.clamp
import ru.itmo.graphics.viewmodel.tools.quantizeInPlace
import kotlin.math.pow

private val matrix = listOf(
    listOf(0, 48, 12, 60, 3, 51, 15, 63),
    listOf(32, 16, 44, 28, 35, 19, 47, 31),
    listOf(8, 56, 4, 52, 11, 59, 7, 55),
    listOf(40, 24, 36, 20, 43, 27, 39, 23),
    listOf(2, 50, 14, 62, 1, 49, 13, 61),
    listOf(34, 18, 46, 30, 33, 17, 45, 29),
    listOf(10, 58, 6, 54, 9, 57, 5, 53),
    listOf(42, 26, 38, 22, 41, 25, 37, 21),
).map { it.map { elem -> elem / 64f } }

object Ordered8x8Dithering : DitheringAlgorithm {
    override fun applyInPlace(
        pixelData: PixelData,
        colorSpace: ApplicationColorSpace,
        bitness: Int,
        isMonochrome: Boolean,
    ) {
        val base = 2f.pow(bitness)
        val step = 1.0f / base
        for (i in 0..<pixelData.height) {
            for (j in 0..<pixelData.width) {
                colorSpace.convertedToRgb(pixelData.getPixel(i, j)) { pixel ->
                    for (n in pixel.indices) {
                        val noise = matrix[i % 8][j % 8]
                        if (pixel[n] > noise) {
                            pixel[n] = clamp(pixel[n] + step)
                        } else {
                            pixel[n] = clamp(pixel[n] - step)
                        }
                    }

                    quantizeInPlace(pixel, bitness)
                }
            }
        }
    }
}
