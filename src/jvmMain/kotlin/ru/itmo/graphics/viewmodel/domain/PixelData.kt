package ru.itmo.graphics.viewmodel.domain

class PixelData(
    val data: MutableList<Float>,
    val height: Int,
    val width: Int,
) {
    val pixelCount: Int
        get() = height * width

    fun getPixel(row: Int, column: Int): MutableList<Float> {
        return data.subList(row * width * 3 + column * 3, row * width * 3 + column * 3 + 3)
    }
}

data class Pixel(
    val channelOne: Float = 0f,
    val channelTwo: Float = 0f,
    val channelThree: Float = 0f,
)
