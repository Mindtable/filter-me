package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel

object YCoCgColorSpace : ApplicationColorSpace {
    override val name = "YCoCg"

    override fun toRgb(pixel: Pixel): Pixel {
        val tmp = pixel.channelOne - pixel.channelThree
        val red = pixel.channelTwo + tmp
        val green = pixel.channelOne + pixel.channelThree
        val blue = tmp - pixel.channelTwo

        return Pixel(red, green, blue)
    }

    override fun fromRgb(pixel: Pixel): Pixel {
        val tmp = (pixel.channelOne + pixel.channelThree) / 4
        val luma = pixel.channelTwo / 2 + tmp
        val chrominanceOrange = pixel.channelOne / 2 - pixel.channelThree / 2
        val chrominanceGreen = pixel.channelTwo / 2 - tmp

        return Pixel(luma, chrominanceOrange, chrominanceGreen)
    }
}
