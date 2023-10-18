package ru.itmo.graphics.viewmodel.domain

class PixelData(
    val data: Array<Array<Pixel>>,
) {
    val height: Int
        get() = data.size

    val width: Int
        get() = data.firstOrNull()?.size ?: 0
}

data class Pixel(
    val channelOne: Float = 0f,
    val channelTwo: Float = 0f,
    val channelThree: Float = 0f,
)
