package ru.itmo.graphics.viewmodel.domain.scale

import ru.itmo.graphics.viewmodel.domain.addPixels
import ru.itmo.graphics.viewmodel.domain.dividePixel
import ru.itmo.graphics.viewmodel.domain.multiplyPixel
import ru.itmo.graphics.viewmodel.domain.normalizePixel
import ru.itmo.graphics.viewmodel.domain.scale.utils.PixelNeighbor
import ru.itmo.graphics.viewmodel.domain.scale.utils.PixelPicker

object SplineBC : ScalingAlgorithm {
    var b: Float = 0f
    var c: Float = 0.5f

    override val radius: Float
        get() = 2.01f

    fun setBC(newB: Float, newC: Float) {
        b = newB
        c = newC
    }

    override fun scale(neighbors: List<List<PixelNeighbor>>, inputPixels: PixelPicker, outputPixels: PixelPicker) {
        val newPixel = MutableList(3) { 0f }
        var tempPixel: MutableList<Float>
        var k: Float

        for ((i, pixelNeighbors) in neighbors.withIndex()) {
            k = 0f
            multiplyPixel(newPixel, 0f)

            for (neighbor in pixelNeighbors) {
                tempPixel = inputPixels.getPixelCopy(neighbor.index)

                val coefficient = calculateSpline(neighbor.distance)
                k += coefficient
                multiplyPixel(tempPixel, coefficient)

                addPixels(newPixel, tempPixel)
            }

            dividePixel(newPixel, k)
            normalizePixel(newPixel)

            outputPixels.setPixel(i, newPixel)
        }
    }

    private fun calculateSpline(distance: Float): Float {
        return 1 / 6f * if (distance < 1f) {
            (12 - 9 * b - 6 * c) * distance * distance * distance + (-18 + 12 * b + 6 * c) * distance * distance + (6 - 2 * b)
        } else if (distance < 2f) {
            (-b - 6 * c) * distance * distance * distance + (6 * b + 30 * c) * distance * distance + (-12 * b - 48 * c) * distance + (8 * b + 24 * c)
        } else {
            0f
        }
    }
}
