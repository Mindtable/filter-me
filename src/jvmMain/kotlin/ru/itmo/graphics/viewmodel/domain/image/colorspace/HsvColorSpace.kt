package ru.itmo.graphics.viewmodel.domain.image.colorspace

import ru.itmo.graphics.viewmodel.presentation.view.main.ImageChannel
import kotlin.math.abs

object HsvColorSpace : ApplicationColorSpace {
    override val name = "HSV"

    override fun toRgb(bb: MutableList<Float>) {
        val hue = bb[0] * 360
        val saturation = bb[1]
        val value = bb[2]

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

        bb[0] = red
        bb[1] = green
        bb[2] = blue
    }

    override fun fromRgb(bb: MutableList<Float>) {
        var hue: Float
        val saturation: Float
        val value: Float

        val red = bb[0]
        val green = bb[1]
        val blue = bb[2]

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

        bb[0] = hue / 360f
        bb[1] = saturation
        bb[2] = value
    }

    override fun separateChannel(bb: MutableList<Float>, channel: ImageChannel) {
        if (channel == ImageChannel.CHANNEL_ONE) {
            bb[1] = 1f
            bb[2] = 1f
        } else if (channel == ImageChannel.CHANNEL_TWO) {
            bb[0] = 1f
            bb[2] = 1f
        } else if (channel == ImageChannel.CHANNEL_THREE) {
            bb[0] = 1f
            bb[1] = 0f
        }
    }
}
