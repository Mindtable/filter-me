package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel
import kotlin.math.abs

object HsvColorSpace : ApplicationColorSpace {
    override val name = "HSV"

    override fun toRgb(pixel: Pixel): Pixel {
        val hue = pixel.channelOne * 360
        val saturation = pixel.channelTwo
        val value = pixel.channelThree

        var red: Float
        var green: Float
        var blue: Float

        val chroma = value * saturation

        val x = chroma * (1 - abs((hue / 60) % 2 - 1))

        val m = value - chroma

        when {
            hue < 60 -> {
                red = chroma
                green = x
                blue = 0f
            }
            hue < 120 -> {
                red = x
                green = chroma
                blue = 0f
            }
            hue < 180 -> {
                red = 0f
                green = chroma
                blue = x
            }
            hue < 240 -> {
                red = 0f
                green = x
                blue = chroma
            }
            hue < 300 -> {
                red = x
                green = 0f
                blue = chroma
            }
            else -> {
                red = chroma
                green = 0f
                blue = x
            }
        }

        red += m
        green += m
        blue += m

        return Pixel(red, green, blue)
    }

    override fun fromRgb(pixel: Pixel): Pixel {
        var hue: Float
        val saturation: Float
        val value: Float

        val red = pixel.channelOne
        val green = pixel.channelTwo
        val blue = pixel.channelThree

        val max = maxOf(red, green, blue)
        val min = minOf(red, green, blue)

        val delta = max - min

        saturation = if (max != 0f) {
            delta / max
        } else {
            0f
        }

        hue = when (max) {
            min -> 0f
            red -> 60 * (((green - blue) / delta) % 6)
            green -> 60 * (((blue - red) / delta) + 2)
            else -> 60 * (((red - green) / delta) + 4)
        }

        if (hue < 0) {
            hue += 360
        }

        value = max

        return Pixel(hue / 360f, saturation, value)
    }
}
