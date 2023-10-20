package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.presentation.viewmodel.Channel

object RgbColorSpace : ApplicationColorSpace {

    override val name: String = "RGB"

    override fun fromRgb(bb: MutableList<Float>) {
        //
    }

    override fun toRgb(bb: MutableList<Float>) {
        //
    }

    override fun separateChannel(bb: MutableList<Float>, channel: Channel) {
        if (channel == Channel.CHANNEL_ONE) {
            bb[1] = 0f
            bb[2] = 0f
        } else if (channel == Channel.CHANNEL_TWO) {
            bb[0] = 0f
            bb[2] = 0f
        } else if (channel == Channel.CHANNEL_THREE) {
            bb[0] = 0f
            bb[1] = 0f
        }
    }
}
