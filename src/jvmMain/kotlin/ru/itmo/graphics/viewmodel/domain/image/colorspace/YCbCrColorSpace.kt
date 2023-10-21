package ru.itmo.graphics.viewmodel.domain.image.colorspace

import ru.itmo.graphics.viewmodel.presentation.view.main.ImageChannel

private object YCbCr {
    fun toRgb(a: Float, b: Float, c: Float, d: Float, e: Float, bb: MutableList<Float>) {
        val luma = bb[0]
        val blueDifference = bb[1]
        val redDifference = bb[2]

        val red = luma + e * redDifference
        val green = luma - (a * e / b) * redDifference - (c * d / b) * blueDifference
        val blue = luma + d * blueDifference

        bb[0] = red
        bb[1] = green
        bb[2] = blue
    }

    fun fromRgb(a: Float, b: Float, c: Float, d: Float, e: Float, bb: MutableList<Float>) {
        val red = bb[0]
        val green = bb[1]
        val blue = bb[2]

        val luma = a * red + b * green + c * blue
        val blueDifference = (blue - luma) / d
        val redDifference = (red - luma) / e

        bb[0] = luma
        bb[1] = blueDifference
        bb[2] = redDifference
    }

    fun separateChannel(bb: MutableList<Float>, channel: ImageChannel) {
        if (channel == ImageChannel.CHANNEL_ONE) {
            bb[1] = 0f
            bb[2] = 0f
        } else if (channel == ImageChannel.CHANNEL_TWO) {
            bb[0] = 0.5f
            bb[2] = 0f
        } else if (channel == ImageChannel.CHANNEL_THREE) {
            bb[0] = 0.5f
            bb[1] = 0f
        }
    }
}

object YCbCr601ColorSpace : ApplicationColorSpace {
    private const val A = 0.299f
    private const val B = 0.587f
    private const val C = 0.114f
    private const val D = 1.772f
    private const val E = 1.402f

    override val name = "YCbCr.601"

    override fun fromRgb(bb: MutableList<Float>) {
        YCbCr.fromRgb(A, B, C, D, E, bb)
    }

    override fun toRgb(bb: MutableList<Float>) {
        YCbCr.toRgb(A, B, C, D, E, bb)
    }

    override fun separateChannel(bb: MutableList<Float>, channel: ImageChannel) {
        YCbCr.separateChannel(bb, channel)
    }
}

object YCbCr709ColorSpace : ApplicationColorSpace {
    private const val A = 0.2126f
    private const val B = 0.7152f
    private const val C = 0.0722f
    private const val D = 1.8556f
    private const val E = 1.5748f

    override val name = "YCbCr.709"

    override fun fromRgb(bb: MutableList<Float>) {
        YCbCr.fromRgb(A, B, C, D, E, bb)
    }

    override fun toRgb(bb: MutableList<Float>) {
        YCbCr.toRgb(A, B, C, D, E, bb)
    }

    override fun separateChannel(bb: MutableList<Float>, channel: ImageChannel) {
        YCbCr.separateChannel(bb, channel)
    }
}
