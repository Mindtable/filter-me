package ru.itmo.graphics.viewmodel.presentation.view.main

import ru.itmo.graphics.viewmodel.domain.dithering.AtkinsonAlgorithm
import ru.itmo.graphics.viewmodel.domain.dithering.DitheringAlgorithm
import ru.itmo.graphics.viewmodel.domain.dithering.FloydSteinbergAlgorithm
import ru.itmo.graphics.viewmodel.domain.dithering.NoAlgorithm
import ru.itmo.graphics.viewmodel.domain.dithering.Ordered8x8Dithering
import ru.itmo.graphics.viewmodel.domain.dithering.QuantizationAlgorithm
import ru.itmo.graphics.viewmodel.domain.dithering.RandomizedDithering

enum class DitheringAlgo(
    val text: String,
    val ditheringAlgorithm: DitheringAlgorithm,
) {
    NONE("None", NoAlgorithm),
    QUANTIZATION("Quantization", QuantizationAlgorithm),
    ORDERED("Ordered", Ordered8x8Dithering),
    RANDOM("Random", RandomizedDithering),
    FLOYDSTEINBERG("Floyd-Steinberg", FloydSteinbergAlgorithm),
    ATKINSON("Atkinson", AtkinsonAlgorithm),
    ;

    fun isNotNone() = this != NONE
}
