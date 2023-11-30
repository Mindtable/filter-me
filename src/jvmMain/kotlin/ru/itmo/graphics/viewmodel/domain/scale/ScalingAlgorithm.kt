package ru.itmo.graphics.viewmodel.domain.scale

import ru.itmo.graphics.viewmodel.domain.scale.utils.PixelNeighbor
import ru.itmo.graphics.viewmodel.domain.scale.utils.PixelPicker

interface ScalingAlgorithm {
    val radius: Float

    fun scale(
        neighbors: List<List<PixelNeighbor>>,
        inputPixels: PixelPicker,
        outputPixels: PixelPicker,
    )
}
