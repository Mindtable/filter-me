package ru.itmo.graphics.viewmodel.domain.image.colorspace

import ru.itmo.graphics.viewmodel.presentation.view.main.ImageChannel

interface ApplicationColorSpace {

    val name: String

    fun fromRgb(bb: MutableList<Float>)
    fun toRgb(bb: MutableList<Float>)

    fun separateChannel(bb: MutableList<Float>, channel: ImageChannel)
}
