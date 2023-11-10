package ru.itmo.graphics.viewmodel.domain.dithering

import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.domain.image.colorspace.ApplicationColorSpace
import ru.itmo.graphics.viewmodel.domain.image.gamma.GammaConversion
import ru.itmo.graphics.viewmodel.tools.clamp
import ru.itmo.graphics.viewmodel.tools.quantizeInPlace

object FloydSteinbergAlgorithm : DitheringAlgorithm {

    override fun applyInPlace(
        pixelData: PixelData,
        colorSpace: ApplicationColorSpace,
        bitness: Int,
        isMonochrome: Boolean,
        gamma: Float,
    ) {
        for (i in 0..<pixelData.height) {
            for (j in 0..<pixelData.width) {
                val pixel = pixelData.getPixel(i, j)
                colorSpace.toRgb(pixel)

                val originalPixel = pixel.toMutableList()

                quantizeInPlace(pixel, bitness, gamma)

                GammaConversion.applyGamma(originalPixel, gamma)
                GammaConversion.applyGamma(pixel, gamma)

                val errors = listOf(
                    originalPixel[0] - pixel[0],
                    originalPixel[1] - pixel[1],
                    originalPixel[2] - pixel[2],
                )

                GammaConversion.applyReverseGamma(pixel, gamma)

                val updatePixels = { iOffset: Int, jOffset: Int, coeff: Float ->
                    val newI = i + iOffset
                    val newJ = j + jOffset
                    if (newI in 0..<pixelData.height && newJ in 0..<pixelData.width) {
                        val pixelToUpdate = pixelData.getPixel(newI, newJ)
                        colorSpace.toRgb(pixelToUpdate)
                        for (n in pixelToUpdate.indices) {
                            pixelToUpdate[n] = clamp(pixelToUpdate[n] + errors[n] * coeff)
                        }
                        colorSpace.fromRgb(pixelToUpdate)
                    }
                }
                colorSpace.fromRgb(pixel)

                updatePixels(+1, 0, 7 / 16f)
                updatePixels(-1, 1, 3 / 16f)
                updatePixels(+0, 1, 5 / 16f)
                updatePixels(+1, 1, 1 / 16f)
            }
        }
    }
}
