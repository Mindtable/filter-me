package ru.itmo.graphics.viewmodel.domain.scale

import ru.itmo.graphics.viewmodel.domain.addPixels
import ru.itmo.graphics.viewmodel.domain.dividePixel
import ru.itmo.graphics.viewmodel.domain.multiplyPixel
import ru.itmo.graphics.viewmodel.domain.normalizePixel
import ru.itmo.graphics.viewmodel.domain.scale.utils.PixelNeighbor
import ru.itmo.graphics.viewmodel.domain.scale.utils.PixelPicker
import kotlin.math.sin

object Lanczos : ScalingAlgorithm {
    // This is lanczos3, so param is 3
    private const val PARAM = 3f
    private const val PI = 3.141592f

    override val radius: Float
        get() = PARAM + 0.01f

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

                val coefficient = calculateLanczos(neighbor.distance)
                k += coefficient
                multiplyPixel(tempPixel, coefficient)

                addPixels(newPixel, tempPixel)
            }

            dividePixel(newPixel, k)
            normalizePixel(newPixel)

            outputPixels.setPixel(i, newPixel)
        }
    }

    private fun calculateLanczos(distance: Float): Float {
        return if (distance == 0f) {
            1f
        } else if (distance < PARAM) {
            (PARAM * sin(PI * distance) * sin(PI * distance / PARAM)) / (PI * PI * distance * distance)
        } else {
            0f
        }
    }
}
