package ru.itmo.graphics.viewmodel.presentation.view.main

import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.isUnspecified

@Composable
fun ResizedText(
    log: String,
    style: TextStyle = MaterialTheme.typography.displayLarge,
    width: Int,
    height: Int,
    modifier: Modifier = Modifier,
    color: Color = style.color,
) {
    var resizedTextStyle by remember(style) {
        mutableStateOf(style)
    }
    var isRedrawNeeded by remember {
        mutableStateOf(false)
    }
    val defaultFontSize = MaterialTheme.typography.displayLarge.fontSize
    Text(
        text = log,
        color = color,
        modifier = modifier.drawWithContent {
            if (isRedrawNeeded) {
                drawContent()
            }
        },
        softWrap = false,
        style = resizedTextStyle,
        onTextLayout = { message ->
            if (message.didOverflowWidth || message.didOverflowHeight) {
                if (style.fontSize.isUnspecified) {
                    resizedTextStyle = resizedTextStyle.copy(
                        fontSize = defaultFontSize
                    )
                }
                if (resizedTextStyle.fontSize.value > 5) {
                    resizedTextStyle = resizedTextStyle.copy(
                        fontSize = resizedTextStyle.fontSize * 0.9
                    )
                }
            } else if (message.size.width < 0.5 * width && message.size.height < 0.9 * height) {
                resizedTextStyle = resizedTextStyle.copy(
                    fontSize = resizedTextStyle.fontSize * 1.05
                )
            } else {
                isRedrawNeeded = true
            }
        }
    )
}