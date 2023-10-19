package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel

object RgbColorSpace : ApplicationColorSpace {

    override val name: String = "RGB"

    override fun fromRgb(pixel: Pixel): Pixel {
        return pixel
    }

    override fun toRgb(pixel: Pixel): Pixel {
        return pixel
    }
}
