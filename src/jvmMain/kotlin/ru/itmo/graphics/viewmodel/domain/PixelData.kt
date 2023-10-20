package ru.itmo.graphics.viewmodel.domain

import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger { }

class PixelData(
    val data: MutableList<Float>,
    val height: Int,
    val width: Int,
) {
    val pixelCount: Int
        get() = height * width

    fun getPixel(row: Int, column: Int): MutableList<Float> {
        return runCatching {
            data.subList(row * width * 3 + column * 3, row * width * 3 + column * 3 + 3)
        }.onFailure {
            log.info { "$row $column" }
            log.info { "$width $height" }
        }.getOrThrow()
    }
}

data class Pixel(
    val channelOne: Float = 0f,
    val channelTwo: Float = 0f,
    val channelThree: Float = 0f,
)
