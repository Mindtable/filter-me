package ru.itmo.graphics.viewmodel.presentation.view.main

enum class DitheringAlgo(
    val text: String,
) {
    NONE("None"),
    ORDERED("Ordered"),
    RANDOM("Random"),
    FLOYDSTEINBERG("Floyd-Steinberg"),
    ATKINSON("Atkinson"),
    ;

    fun isNotNone() = this != NONE
}
