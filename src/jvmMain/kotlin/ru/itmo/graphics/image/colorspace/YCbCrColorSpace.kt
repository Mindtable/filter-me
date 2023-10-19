package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.domain.Pixel

private object YCbCr {
    fun toRgb(pixel: Pixel, a: Float, b: Float, c: Float, d: Float, e: Float): Pixel {
        val luma = pixel.channelOne
        val blueDifference = pixel.channelTwo
        val redDifference = pixel.channelThree

        val red = luma + e * redDifference
        val green = luma - (a * e / b) * redDifference - (c * d / b) * blueDifference
        val blue = luma + d * blueDifference

        return Pixel(red, green, blue)
    }

    fun fromRgb(pixel: Pixel, a: Float, b: Float, c: Float, d: Float, e: Float): Pixel {
        val red = pixel.channelOne
        val green = pixel.channelTwo
        val blue = pixel.channelThree

        val luma = a * red + b * green + c * blue
        val blueDifference = (blue - luma) / d
        val redDifference = (red - luma) / e

        return Pixel(luma, blueDifference, redDifference)
    }
}

object YCbCr601ColorSpace : ApplicationColorSpace {
    private const val A = 0.299f
    private const val B = 0.587f
    private const val C = 0.114f
    private const val D = 1.772f
    private const val E = 1.402f

    override val name = "YCbCr.601"

    override fun toRgb(pixel: Pixel): Pixel {
        return YCbCr.toRgb(pixel, A, B, C, D, E)
    }

    override fun fromRgb(pixel: Pixel): Pixel {
        return YCbCr.fromRgb(pixel, A, B, C, D, E)
    }
}

object YCbCr709ColorSpace : ApplicationColorSpace {
    private const val A = 0.2126f
    private const val B = 0.7152f
    private const val C = 0.0722f
    private const val D = 1.8556f
    private const val E = 1.5748f

    override val name = "YCbCr.709"

    override fun toRgb(pixel: Pixel): Pixel {
        return YCbCr.toRgb(pixel, A, B, C, D, E)
    }

    override fun fromRgb(pixel: Pixel): Pixel {
        return YCbCr.fromRgb(pixel, A, B, C, D, E)
    }
}
