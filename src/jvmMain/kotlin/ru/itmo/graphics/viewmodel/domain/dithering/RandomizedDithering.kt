package ru.itmo.graphics.viewmodel.domain.dithering

import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.domain.image.colorspace.ApplicationColorSpace
import ru.itmo.graphics.viewmodel.tools.clamp
import ru.itmo.graphics.viewmodel.tools.quantizeInPlace
import kotlin.math.pow
import kotlin.random.Random

private val log = KotlinLogging.logger { }

object RandomizedDithering : DitheringAlgorithm {
    override fun applyInPlace(
        pixelData: PixelData,
        colorSpace: ApplicationColorSpace,
        bitness: Int,
        isMonochrome: Boolean,
    ) {
        val base = 2f.pow(bitness)
        for (i in 0..<pixelData.height) {
            for (j in 0..<pixelData.width) {
                colorSpace.convertedToRgb(pixelData.getPixel(i, j)) { pixel ->

                    if (isMonochrome) {
                        val noise = (2 * Random.nextFloat() - 1f) / base
                        pixel[0] = clamp(pixel[0] + noise)
                        pixel[1] = pixel[0]
                        pixel[2] = pixel[0]
                    } else {
                        for (n in pixel.indices) {
                            val noise = (2 * Random.nextFloat() - 1f) / base
                            pixel[n] = clamp(pixel[n] + noise)
                        }
                    }

                    quantizeInPlace(pixel, bitness)
                }
            }
        }
    }
}
