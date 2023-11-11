package ru.itmo.graphics.viewmodel.tools

import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.presentation.view.main.ImageChannel
import ru.itmo.graphics.viewmodel.presentation.view.settings.histogram.BrightnessDistribution

private val log = KotlinLogging.logger { }

fun autoCorrect(pixelData: PixelData, distribution: BrightnessDistribution, coefficient: Float, channel: ImageChannel) {
    if (coefficient !in 0f..<0.5f) {
        log.error { "Invalid coefficient range. Exit. Coefficient $coefficient not in range ${0f..<0.5f}" }
        return
    }

    val channelIndex = when (channel) {
        ImageChannel.CHANNEL_ONE -> 0
        ImageChannel.CHANNEL_TWO -> 1
        ImageChannel.CHANNEL_THREE -> 2
        ImageChannel.ALL -> -1
    }

    val (minBrightness, maxBrightness) = findMinMax(distribution, coefficient, channel)

    for (i in 0..<pixelData.height) {
        for (j in 0..<pixelData.width) {
            val pixel = pixelData.getPixel(i, j)

            for (n in pixel.indices) {
                if (channelIndex != -1 && n != channelIndex) continue

                pixel[n] = clamp((pixel[n] - minBrightness) / (maxBrightness - minBrightness))
            }
        }
    }
}

fun findMinMax(
    distribution: BrightnessDistribution,
    coefficient: Float,
    channel: ImageChannel,
): AutoCorrectionParameters {
    var minBrightness: Int? = null
    var minBrightnessAccum = 0f
    var maxBrightness: Int? = null
    var maxBrightnessAccum = 0f

    if (channel == ImageChannel.ALL) {
        val (minBrightness1, maxBrightness1) = findMinMax(distribution, coefficient, ImageChannel.CHANNEL_ONE)
        val (minBrightness2, maxBrightness2) = findMinMax(distribution, coefficient, ImageChannel.CHANNEL_TWO)
        val (minBrightness3, maxBrightness3) = findMinMax(distribution, coefficient, ImageChannel.CHANNEL_THREE)

        return AutoCorrectionParameters(
            listOf(minBrightness1, minBrightness2, minBrightness3).min(),
            listOf(maxBrightness1, maxBrightness2, maxBrightness3).max(),
        )
    }

    val distributionArray = when (channel) {
        ImageChannel.CHANNEL_ONE -> distribution.channelOne
        ImageChannel.CHANNEL_TWO -> distribution.channelTwo
        ImageChannel.CHANNEL_THREE -> distribution.channelThree
        ImageChannel.ALL -> distribution.allChannels
    }

    for (i in 0..255 / 2) {
        minBrightnessAccum += distributionArray[i]
        maxBrightnessAccum += distributionArray[254 - i]

        if (minBrightness == null && minBrightnessAccum >= coefficient) {
            minBrightness = i
        }

        if (maxBrightness == null && maxBrightnessAccum >= coefficient) {
            maxBrightness = 254 - i
        }

        if (minBrightness != null && maxBrightness != null) {
            break
        }
    }

    return AutoCorrectionParameters(
        minBrightness = minBrightness!! / 255f,
        maxBrightness = maxBrightness!! / 255f,
    ).also { log.info { "Result of findMinMax with $coefficient: $it" } }
}

data class AutoCorrectionParameters(
    val minBrightness: Float,
    val maxBrightness: Float,
)
