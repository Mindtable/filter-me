package ru.itmo.graphics.viewmodel.presentation.view.settings.histogram

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger { }

@Composable
fun Histogram(
    normalizedValues: Array<Float>,
    paddingPercent: Float = 0.005f,
    modifier: Modifier,
    strokeColor: Color = MaterialTheme.colorScheme.onTertiary,
) {
    Box(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.tertiary, shape = MaterialTheme.shapes.medium)
            .padding(10.dp)
            .border(
                width = 3.dp,
                color = MaterialTheme.colorScheme.onTertiary,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(10.dp),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val dataSize = normalizedValues.size

            val xLeft = paddingPercent * canvasWidth
            val xRight = (1 - paddingPercent) * canvasWidth
            val yTop = paddingPercent * canvasHeight
            val yDown = (1 - paddingPercent) * canvasHeight
            val strokeWidth = (xRight - xLeft) / dataSize

            val maxValue = normalizedValues.max()

            normalizedValues.forEachIndexed { index, value ->
                val normalizedValue = if (value == 0f) value else value / maxValue
                drawLine(
                    start = Offset(
                        x = xLeft + index * strokeWidth + strokeWidth * 0.5f,
                        y = yTop + (1 - normalizedValue) * (yDown - yTop),
                    ),
                    end = Offset(
                        x = xLeft + index * strokeWidth + strokeWidth * 0.5f,
                        y = yDown,
                    ),
                    color = strokeColor,
                    strokeWidth = strokeWidth,
                )
            }
        }
    }
}
