package ru.itmo.graphics.viewmodel.domain.dithering

import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.domain.image.colorspace.ApplicationColorSpace
import ru.itmo.graphics.viewmodel.tools.quantizeInPlace

object QuantizationAlgorithm : DitheringAlgorithm {

    override fun applyInPlace(
        pixelData: PixelData,
        colorSpace: ApplicationColorSpace,
        bitness: Int,
        isMonochrome: Boolean,
    ) {
        for (i in 0..<pixelData.height) {
            for (j in 0..<pixelData.width) {
                val pixel = pixelData.getPixel(i, j)
                colorSpace.toRgb(pixel)
                quantizeInPlace(pixel, bitness)
                colorSpace.fromRgb(pixel)
            }
        }
    }
}
