package ru.itmo.graphics.viewmodel.domain.image.colorspace

import ru.itmo.graphics.viewmodel.presentation.view.main.ImageChannel

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

    override fun separateChannel(bb: MutableList<Float>, channel: ImageChannel) {
        if (channel == ImageChannel.CHANNEL_ONE) {
            bb[1] = 0f
            bb[2] = 0f
        } else if (channel == ImageChannel.CHANNEL_TWO) {
            bb[0] = 0f
            bb[2] = 0f
        } else if (channel == ImageChannel.CHANNEL_THREE) {
            bb[0] = 0f
            bb[1] = 0f
        }
    }
}
