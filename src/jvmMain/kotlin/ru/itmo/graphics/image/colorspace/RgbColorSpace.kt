package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel

class RgbColorSpace : ApplicationColorSpace {
    override fun fromRgb(pixel: Pixel): Pixel {
        return pixel
    }

    override fun toRgb(pixel: Pixel): Pixel {
        return pixel
    }
}