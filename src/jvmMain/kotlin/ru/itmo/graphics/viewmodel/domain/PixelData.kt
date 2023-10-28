package ru.itmo.graphics.viewmodel.domain

import androidx.compose.ui.graphics.Color
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

fun Pixel.asBb(): MutableList<Float> = mutableListOf(channelOne, channelTwo, channelThree)

fun List<Float>.asComposeColor(): Color = Color(this[0], this[1], this[2])
fun List<Float>.asPixel(): Pixel = Pixel(this[0], this[1], this[2])
