package ru.itmo.graphics.image.colorspace

import ru.itmo.graphics.viewmodel.presentation.viewmodel.Channel

interface ApplicationColorSpace {

    val name: String

    fun fromRgb(bb: MutableList<Float>)
    fun toRgb(bb: MutableList<Float>)

    fun separateChannel(bb: MutableList<Float>, channel: Channel)
}
