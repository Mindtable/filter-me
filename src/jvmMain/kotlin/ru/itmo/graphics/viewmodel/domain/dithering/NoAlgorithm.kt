package ru.itmo.graphics.viewmodel.domain.dithering

import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.domain.image.colorspace.ApplicationColorSpace

object NoAlgorithm : DitheringAlgorithm {

    override fun applyInPlace(
        pixelData: PixelData,
        colorSpace: ApplicationColorSpace,
        bitness: Int,
        isMonochrome: Boolean,
    ) {
        // no algo
    }
}
