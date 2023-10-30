package ru.itmo.graphics.viewmodel.domain.dithering

import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.domain.image.colorspace.ApplicationColorSpace

interface DitheringAlgorithm {

    fun applyInPlace(
        pixelData: PixelData,
        colorSpace: ApplicationColorSpace,
        bitness: Int,
        isMonochrome: Boolean,
    )
}
