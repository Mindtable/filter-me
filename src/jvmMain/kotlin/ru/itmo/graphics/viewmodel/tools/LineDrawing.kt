package ru.itmo.graphics.viewmodel.tools

import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.viewmodel.domain.Coordinates
import ru.itmo.graphics.viewmodel.domain.Pixel
import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.domain.asBb
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt

private val log = KotlinLogging.logger { }

fun plotLineFacade(
    x0: Float,
    y0: Float,
    x1: Float,
    y1: Float,
    color: Pixel,
    pixelData: PixelData,
    thick: Float,
    opacity: Float,
) {
    log.info { "Plot line facade arguments: [$x0, $y0], [$x1, $y1], $color, $thick, $opacity" }

    val dx = abs(x0 - x1)
    val dy = abs(y0 - y1)

    when {
        dx == 0f && dy == 0f -> log.info { "Draw line: Do nothing bc dx * dy == 0" }
        dy == 0f -> plotHorizontal(
            x0.roundToInt(),
            x1.roundToInt(),
            y0.roundToInt(),
            color.asBb(),
            pixelData,
            thick,
            opacity,
            false,
        ).also {
            log.info { "Chosen plotHorizontal bc dy == 0f" }
        }

        dx == 0f -> plotHorizontal(
            y0.roundToInt(),
            y1.roundToInt(),
            x0.roundToInt(),
            color.asBb(),
            pixelData,
            thick,
            opacity,
            true,
        ).also {
            log.info { "Chosen plotHorizontal bc dx == 0f" }
        }

        thick <= 1f -> wuAlgo(
            x0,
            y0,
            x1,
            y1,
            color,
            pixelData,
            opacity * thick,
        ).also {
            log.info { "Chosen wuAlgorithm bc thick <= 1f $thick" }
        }

        else -> plotLineWidth(
            Coordinates(x0.roundToInt(), y0.roundToInt()),
            Coordinates(x1.roundToInt(), y1.roundToInt()),
            color,
            pixelData,
            thick,
            opacity,
        ).also {
            log.info { "Chosen plotLineWidth" }
        }
    }
}

fun plotLineWidth(
    start: Coordinates,
    end: Coordinates,
    color: Pixel,
    pixelData: PixelData,
    thick: Float,
    opacity: Float,
) {
    val (x0, y0) = start
    val (x1, y1) = end
    val bbColor = color.asBb()
    var x0Var = x0
    var y0Var = y0
    val dx = abs(x1 - x0Var)
    val sx = if (x0Var < x1) 1 else -1
    val dy = abs(y1 - y0Var)
    val sy = if (y0Var < y1) 1 else -1
    var err = dx - dy
    var e2: Int
    var x2: Int
    var y2: Int
    val ed = if (dx + dy == 0) 1f else sqrt((dx * dx + dy * dy).toFloat())

    val lineWidth = (thick + 1) / 2
    while (true) {
        drawInPlace(pixelData, x0Var, y0Var, bbColor, opacity * (1f - max(0f, abs(err - dx + dy) / ed - lineWidth + 1)))
        e2 = err
        x2 = x0Var
        if (2 * e2 >= -dx) {
            e2 += dy
            y2 = y0Var
            while (e2 < ed * lineWidth && (y1 != y2 || dx > dy)) {
                val b = (abs(e2) / ed - lineWidth + 1)
                drawInPlace(pixelData, x0Var, y2 + sy, bbColor, opacity * (1f - max(0f, b)))
                y2 += sy
                e2 += dx
            }
            if (x0Var == x1) break
            e2 = err
            err -= dy
            x0Var += sx
        }
        if (2 * e2 <= dy) {
            e2 = dx - e2
            while (e2 < ed * lineWidth && (x1 != x2 || dx < dy)) {
                drawInPlace(
                    pixelData,
                    x2 + sx,
                    y0Var,
                    bbColor,
                    opacity * (1f - max(0f, (abs(e2) / ed - lineWidth + 1))),
                )
                x2 += sx
                e2 += dy
            }
            if (y0Var == y1) break
            err += dx
            y0Var += sy
        }
    }
}

fun plotHorizontal(
    x0: Int,
    x1: Int,
    y: Int,
    color: MutableList<Float>,
    pixelData: PixelData,
    thick: Float,
    opacity: Float,
    isVertical: Boolean,
) {
    for (x in min(x0, x1)..max(x0, x1)) {
        val halfThick = thick / 2f - 0.5f
        val fullOpacityRange = (y - halfThick.toInt())..(y + halfThick.toInt())

        log.info { "Half thick $halfThick $fullOpacityRange" }

        for (n in (y + floor(-halfThick).toInt())..(y + ceil(halfThick).toInt())) {
            val finalOpacity = opacity * when (n) {
                in fullOpacityRange -> 1f
                else -> abs(halfThick - abs(abs(n) - y))
            }
            when (isVertical) {
                false -> {
                    drawInPlace(pixelData, x, n, color, finalOpacity)
                    log.info { "($x, $n), op $finalOpacity" }
                }

                true -> {
                    drawInPlace(pixelData, n, x, color, finalOpacity)
                    log.info { "($n, $x), op $finalOpacity" }
                }
            }
        }
    }
}

fun wuAlgo(
    x0Or: Float,
    y0Or: Float,
    x1Or: Float,
    y1Or: Float,
    color: Pixel,
    pixelData: PixelData,
    opacity: Float,
) {
    fun ipart(i: Float) = floor(i)
    fun fpart(i: Float) = i - ipart(i)
    fun rfpart(i: Float) = 1f - fpart(i)
    var (x0, y0, x1, y1) = listOf(x0Or, y0Or, x1Or, y1Or)
    val bbColor = color.asBb()

    val plot = { x: Int, y: Int, b: Float ->
        drawInPlace(pixelData, x, y, bbColor, b * opacity)
    }

    val steep = abs(y1 - y0) > abs(x1 - x0)
    if (steep) {
        y0 = x0.also { x0 = y0 }
        y1 = x1.also { x1 = y1 }
    }
    if (x0 > x1) {
        x0 = x1.also { x1 = x0 }
        y0 = y1.also { y1 = y0 }
    }

    val dx = x1 - x0
    val dy = y1 - y0
    val gradient = if (dx == 0f) 1f else dy / dx

    var xend = round(x0)
    var yend = y0 + gradient * (xend - x0)
    var xgap = rfpart(x0 + 0.5f)
    var xpxl1 = xend
    var ypxl1 = ipart(yend)

    if (steep) {
        plot(ypxl1.toInt(), xpxl1.toInt(), rfpart(yend) * xgap)
        plot(ypxl1.toInt() + 1, xpxl1.toInt(), fpart(yend) * xgap)
    } else {
        plot(xpxl1.toInt(), ypxl1.toInt(), rfpart(yend) * xgap)
        plot(xpxl1.toInt(), ypxl1.toInt() + 1, fpart(yend) * xgap)
    }

    var intery = yend + gradient // first y-intersection for the main loop

    // handle second endpoint
    xend = round(x1)
    yend = y1 + gradient * (xend - x1)
    xgap = fpart(x1 + 0.5f)
    var xpxl2 = xend // this will be used in the main loop
    var ypxl2 = ipart(yend)

    if (steep) {
        plot(ypxl2.toInt(), xpxl2.toInt(), rfpart(yend) * xgap)
        plot(ypxl2.toInt() + 1, xpxl2.toInt(), fpart(yend) * xgap)
    } else {
        plot(xpxl2.toInt(), ypxl2.toInt(), rfpart(yend) * xgap)
        plot(xpxl2.toInt(), ypxl2.toInt() + 1, fpart(yend) * xgap)
    }

    if (steep) {
        for (x in (xpxl1.toInt() + 1)..(xpxl2.toInt() - 1)) {
            plot(ipart(intery).toInt(), x, rfpart(intery))
            plot(ipart(intery).toInt() + 1, x, fpart(intery))
            intery += gradient
        }
    } else {
        for (x in (xpxl1.toInt() + 1)..(xpxl2.toInt() - 1)) {
            plot(x, ipart(intery).toInt(), rfpart(intery))
            plot(x, ipart(intery).toInt() + 1, fpart(intery))
            intery += gradient
        }
    }
}
