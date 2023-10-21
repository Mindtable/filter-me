package ru.itmo.graphics.viewmodel.domain.image.colorspace

import ru.itmo.graphics.viewmodel.presentation.view.main.ImageChannel

object YCoCgColorSpace : ApplicationColorSpace {
    override val name = "YCoCg"

    override fun toRgb(bb: MutableList<Float>) {
        val luma = bb[0]
        val chrominanceOrange = bb[1]
        val chrominanceGreen = bb[2]

        val tmp = luma - chrominanceGreen
        val red = chrominanceOrange + tmp
        val green = luma + chrominanceGreen
        val blue = tmp - chrominanceOrange

        bb[0] = red
        bb[1] = green
        bb[2] = blue
    }

    override fun fromRgb(bb: MutableList<Float>) {
        val red = bb[0]
        val green = bb[1]
        val blue = bb[2]

        val tmp = (red + blue) / 4
        val luma = green / 2 + tmp
        val chrominanceOrange = red / 2 - blue / 2
        val chrominanceGreen = green / 2 - tmp

        bb[0] = luma
        bb[1] = chrominanceOrange
        bb[2] = chrominanceGreen
    }

    override fun separateChannel(bb: MutableList<Float>, channel: ImageChannel) {
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
