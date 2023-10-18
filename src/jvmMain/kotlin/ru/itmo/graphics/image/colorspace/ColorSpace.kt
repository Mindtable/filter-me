package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel

interface ColorSpace {
    fun fromRgb(pixel: Pixel) : Pixel
    fun toRgb(pixel: Pixel) : Pixel
}