package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.presentation.viewmodel.Channel
import kotlin.math.abs

object HslColorSpace : ApplicationColorSpace {
    override val name = "HSL"

    override fun toRgb(bb: MutableList<Float>) {
        val hue = bb[0] * 360
        val saturation = bb[1]
        val lightness = bb[2]

        var red: Float
        var green: Float
        var blue: Float

        val chroma = (1 - abs(2 * lightness - 1)) * saturation

        val x = chroma * (1 - abs((hue / 60) % 2 - 1))

        val m = lightness - chroma / 2

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
        val lightness: Float

        val red = bb[0]
        val green = bb[1]
        val blue = bb[2]

        val max = maxOf(red, green, blue)
        val min = minOf(red, green, blue)

        val delta = max - min

        lightness = (max + min) / 2

        saturation = if (delta != 0f) {
            delta / (1 - abs(2 * lightness - 1))
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

        bb[0] = hue / 360f
        bb[1] = saturation
        bb[2] = lightness
    }

    override fun separateChannel(bb: MutableList<Float>, channel: Channel) {
        if (channel == Channel.CHANNEL_ONE) {
            bb[1] = 1f
            bb[2] = 0.5f
        } else if (channel == Channel.CHANNEL_TWO) {
            bb[0] = 1f
            bb[2] = 0.5f
        } else if (channel == Channel.CHANNEL_THREE) {
            bb[0] = 1f
            bb[1] = 0f
        }
    }
}
