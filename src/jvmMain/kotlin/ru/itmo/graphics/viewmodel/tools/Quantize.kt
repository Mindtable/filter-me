package ru.itmo.graphics.viewmodel.tools

fun getNearestPaletteColor(value: Float, bitDepth: Int, gamma: Float, threshold: Float): Float {
    var value = value
    value *= 255f
    val lower = getLower(value, bitDepth, gamma).toFloat()
    val upper = getUpper(value, bitDepth, gamma).toFloat()
    return if (value - lower > threshold * (upper - lower)) {
        Math.min(upper / 255f, 1.0f)
    } else {
        Math.max(
            lower / 255f,
            0.0f,
        )
    }
}

private fun getLower(value: Float, bitDepth: Int, gamma: Float): Int {
    return (
        Math.floor(value / (255f / (Math.pow(2.0, bitDepth.toDouble()) - 1))) * (
            255f / (
                Math.pow(
                    2.0,
                    bitDepth.toDouble(),
                ) - 1
                )
            )
        ).toInt()
}

private fun getUpper(value: Float, bitDepth: Int, gamma: Float): Int {
    return (
        Math.ceil(value / (255f / (Math.pow(2.0, bitDepth.toDouble()) - 1))) * (
            255f / (
                Math.pow(
                    2.0,
                    bitDepth.toDouble(),
                ) - 1
                )
            )
        ).toInt()
}

