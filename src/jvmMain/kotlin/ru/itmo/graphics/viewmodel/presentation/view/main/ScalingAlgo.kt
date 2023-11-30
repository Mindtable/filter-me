package ru.itmo.graphics.viewmodel.presentation.view.main

import ru.itmo.graphics.viewmodel.domain.scale.Bilinear
import ru.itmo.graphics.viewmodel.domain.scale.Lanczos
import ru.itmo.graphics.viewmodel.domain.scale.NearestNeighbor
import ru.itmo.graphics.viewmodel.domain.scale.ScalingAlgorithm
import ru.itmo.graphics.viewmodel.domain.scale.SplineBC

enum class ScalingAlgo(
    val text: String,
    val scalingAlgorithm: ScalingAlgorithm,
) {
    NEAREST("Nearest", NearestNeighbor),
    BILINEAR("Bilinear", Bilinear),
    LANCZOS("Lanczos 3", Lanczos),
    SPLINE("BC-Spline", SplineBC),
}
