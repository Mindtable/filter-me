package ru.itmo.graphics.viewmodel.domain.scale

import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.domain.scale.utils.PixelDirection
import ru.itmo.graphics.viewmodel.domain.scale.utils.PixelNeighbor
import ru.itmo.graphics.viewmodel.domain.scale.utils.PixelPicker
import kotlin.math.abs

object ImageScaling {
    private fun calculateNeighborsDown(
        initialLen: Int,
        resultingLen: Int,
        lag: Float,
        radius: Float,
    ): List<List<PixelNeighbor>> {
        val step = initialLen / resultingLen.toFloat()
        val neighbors = List(resultingLen) { emptyList<PixelNeighbor>().toMutableList() }

        for ((i, pixelNeighbors) in neighbors.withIndex()) {
            val left = lag + step / 2 + i * step - radius
            val right = left + 2 * radius
            for (ind in left.toInt()..(right + 0.5f).toInt()) {
                if (ind + 0.5f in left..right) {
                    pixelNeighbors.add(PixelNeighbor(ind, abs(ind + 0.5f - left - radius)))
                }
            }
        }

        return neighbors
    }

    private fun calculateNeighborsUp(
        initialLen: Int,
        resultingLen: Int,
        lag: Float,
        radius: Float,
    ): List<List<PixelNeighbor>> {
        val step = initialLen / resultingLen.toFloat()
        val neighbors = List(resultingLen) { emptyList<PixelNeighbor>().toMutableList() }

        for ((i, pixelNeighbors) in neighbors.withIndex()) {
            val left = lag + step / 2 + i * step - radius * step
            val right = left + 2 * radius * step
            for (ind in left.toInt()..(right + 0.5f).toInt()) {
                if (ind + 0.5f in left..right) {
                    pixelNeighbors.add(PixelNeighbor(ind, abs(ind + 0.5f - left - radius * step)))
                }
            }
        }

        return neighbors
    }

    fun scaleImage(
        initialLen: Int,
        resultingLen: Int,
        algorithm: ScalingAlgorithm,
        direction: PixelDirection,
        scaledCenter: Float,
        initialImage: PixelData,
        scaledImage: PixelData,
    ) {
        val lag = scaledCenter - resultingLen / 2f
        val neighbors: List<List<PixelNeighbor>> = if (initialLen >= resultingLen) {
            calculateNeighborsDown(resultingLen, initialLen, lag, algorithm.radius)
        } else {
            calculateNeighborsUp(resultingLen, initialLen, lag, algorithm.radius)
        }

        if (direction == PixelDirection.ROW) {
            for (i in 0..<initialImage.height) {
                val inputPicker = PixelPicker(initialImage, i, direction)
                val outputPicker = PixelPicker(scaledImage, i, direction)
                algorithm.scale(neighbors, inputPicker, outputPicker)
            }
        } else {
            for (i in 0..<initialImage.width) {
                val inputPicker = PixelPicker(initialImage, i, direction)
                val outputPicker = PixelPicker(scaledImage, i, direction)
                algorithm.scale(neighbors, inputPicker, outputPicker)
            }
        }
    }
}
