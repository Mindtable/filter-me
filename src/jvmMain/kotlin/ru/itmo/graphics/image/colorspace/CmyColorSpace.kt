package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.presentation.viewmodel.Channel

object CmyColorSpace : ApplicationColorSpace {

    override val name: String = "CMY"

    override fun fromRgb(bb: MutableList<Float>) {
        bb[0] = 1.0f - bb[0]
        bb[1] = 1.0f - bb[1]
        bb[2] = 1.0f - bb[2]
    }

    override fun toRgb(bb: MutableList<Float>) {
        bb[0] = 1.0f - bb[0]
        bb[1] = 1.0f - bb[1]
        bb[2] = 1.0f - bb[2]
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
