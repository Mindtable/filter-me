package ru.itmo.graphics.viewmodel.domain.scale

import ru.itmo.graphics.viewmodel.domain.scale.utils.PixelNeighbor
import ru.itmo.graphics.viewmodel.domain.scale.utils.PixelPicker

object NearestNeighbor : ScalingAlgorithm {
    override val radius: Float
        get() = 0.501f

    override fun scale(
        neighbors: List<List<PixelNeighbor>>,
        inputPixels: PixelPicker,
        outputPixels: PixelPicker,
    ) {
        for ((i, pixelNeighbors) in neighbors.withIndex()) {
            val nearest = getNearest(pixelNeighbors)
            val nearestPixel = inputPixels.getPixelCopy(nearest.index)
            outputPixels.setPixel(i, nearestPixel)
        }
    }

    private fun getNearest(neighbors: List<PixelNeighbor>): PixelNeighbor {
        var nearest = neighbors[0]
        for (neighbor in neighbors) {
            if (neighbor.distance < nearest.distance) {
                nearest = neighbor
            }
        }

        return nearest
    }
}
