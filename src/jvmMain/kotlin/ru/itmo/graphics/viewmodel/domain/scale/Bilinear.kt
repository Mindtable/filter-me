package ru.itmo.graphics.viewmodel.domain.scale

import ru.itmo.graphics.viewmodel.domain.addPixels
import ru.itmo.graphics.viewmodel.domain.dividePixel
import ru.itmo.graphics.viewmodel.domain.multiplyPixel
import ru.itmo.graphics.viewmodel.domain.normalizePixel
import ru.itmo.graphics.viewmodel.domain.scale.utils.PixelNeighbor
import ru.itmo.graphics.viewmodel.domain.scale.utils.PixelPicker

object Bilinear : ScalingAlgorithm {
    override val radius: Float
        get() = 1.01f

    override fun scale(
        neighbors: List<List<PixelNeighbor>>,
        inputPixels: PixelPicker,
        outputPixels: PixelPicker,
    ) {
        val newPixel = MutableList(3) { 0f }
        var tempPixel: MutableList<Float>
        var k: Float

        for ((i, pixelNeighbors) in neighbors.withIndex()) {
            k = 0f
            multiplyPixel(newPixel, 0f)

            for (neighbor in pixelNeighbors) {
                tempPixel = inputPixels.getPixelCopy(neighbor.index)

                val coefficient = calculateCoefficient(neighbor.distance)
                k += coefficient
                multiplyPixel(tempPixel, coefficient)

                addPixels(newPixel, tempPixel)
            }

            dividePixel(newPixel, k)
            normalizePixel(newPixel)

            outputPixels.setPixel(i, newPixel)
        }
    }

    private fun calculateCoefficient(distance: Float): Float {
        return if (distance < 1f) {
            1f - distance
        } else {
            0f
        }
    }
}
