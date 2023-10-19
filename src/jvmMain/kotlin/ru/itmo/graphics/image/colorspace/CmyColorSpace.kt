package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel

class CmyColorSpace : ApplicationColorSpace {
    override fun toRgb(pixel: Pixel): Pixel {
        val red = 1.0f - pixel.channelOne
        val green = 1.0f - pixel.channelTwo
        val blue = 1.0f - pixel.channelThree
        return Pixel(red, green, blue)
    }

    override fun fromRgb(pixel: Pixel): Pixel {
        val cyan = 1.0f - pixel.channelOne
        val magenta = 1.0f - pixel.channelTwo
        val yellow = 1.0f - pixel.channelThree
        return Pixel(cyan, magenta, yellow)
    }
}