package ru.itmo.graphics.viewmodel.presentation.view.settings.colorpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.HsvColor
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.viewmodel.domain.asBb
import ru.itmo.graphics.viewmodel.domain.asComposeColor
import ru.itmo.graphics.viewmodel.domain.asPixel
import ru.itmo.graphics.viewmodel.domain.image.colorspace.HsvColorSpace
import ru.itmo.graphics.viewmodel.domain.image.gamma.GammaConversion
import ru.itmo.graphics.viewmodel.presentation.view.main.ImageChannel
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.DescriptionText
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsType
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsViewProvider
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageError
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageState
import ru.itmo.graphics.viewmodel.presentation.viewmodel.UpdateLineSettings
import kotlin.math.max
import kotlin.math.min

private val log = KotlinLogging.logger { }

class LineSettingsProvider : SettingsViewProvider {

    override val type: SettingsType
        get() = SettingsType.WIDTHCOLORPICKER

    @Composable
    override fun draw(state: ImageState, onEvent: (ImageEvent) -> Unit) {
        LineSettings(state, onEvent)
    }

    @Composable
    override fun configureWindow(window: ComposeWindow) {
        window.isResizable = false
    }
}

@Composable
private fun LineSettings(state: ImageState, onEvent: (ImageEvent) -> Unit) {
    val colorPickerColor = remember {
        val lineColor = state.lineColor.asBb()
        state.colorSpace.toRgb(lineColor)
        lineColor.asComposeColor().copy(alpha = state.lineOpacity)
    }
    var currentColor by remember {
        val lineColor = state.lineColor.asBb()
        state.colorSpace.toRgb(lineColor)
        mutableStateOf(
            HsvColor.from(
                lineColor.asComposeColor().copy(alpha = state.lineOpacity),
            ),
        )
    }
    var lineOpacityText by remember {
        mutableStateOf("%.3f".format(currentColor.alpha))
    }
    val colorUpdate: (HsvColor) -> Unit = { color ->
        currentColor = color
        lineOpacityText = "%.3f".format(currentColor.alpha)
    }
    val colorByChannels = getHSVColorList(state, currentColor)
    var textToInput by remember {
        mutableStateOf("%.2f".format(state.lineWidth))
    }

    val textUpdate: (String) -> Unit = { input -> textToInput = input }
    Column(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .width(350.dp)
            .padding(10.dp),
    ) {
        DescriptionText(Modifier, "Line width")
        LineSettingInput(Modifier, textToInput, "Line width in px", "px", textUpdate)
        DescriptionText(Modifier, "Line opacity")
        LineSettingInput(
            Modifier.onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && event.key == Key.Enter) {
                    log.info { "Event with enter processing" }
                    val newOpacity = lineOpacityText.toFloatOrNull() ?: return@onKeyEvent false

                    val alpha = max(0f, min(1f, newOpacity))
                    log.info { "Change line opacity to $alpha" }
                    currentColor = currentColor.copy(
                        alpha = alpha,
                    )
                    lineOpacityText = if (newOpacity in 0f..1f) {
                        lineOpacityText
                    } else {
                        "%.3f".format(max(0f, min(1f, newOpacity)))
                    }

                    true
                }
                false
            },
            lineOpacityText,
            placeholder = "Line opacity value in [0;1]",
        ) { lineOpacityText = it }
        DescriptionText(Modifier, "Line color")
        ColorAlphaPicker(Modifier, colorUpdate, colorPickerColor)
        DescriptionText(Modifier, "Current opacity is ${currentColor.alpha}")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small)
                .border(width = 2.dp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                .padding(10.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            ColorPaletteBar(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = colorByChannels,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {
                    val lineWidth = textToInput.toFloatOrNull()
                    if (lineWidth == null) {
                        onEvent(ImageError(RuntimeException("$textToInput cannot be cast to float")))
                        return@Button
                    }
                    val bb = currentColor.toColor().let { (r, g, b) -> mutableListOf(r, g, b) }
                    state.colorSpace.fromRgb(bb)
                    GammaConversion.applyGamma(bb, state.gamma)
                    onEvent(
                        UpdateLineSettings(
                            lineWidth = lineWidth,
                            lineOpacity = currentColor.alpha,
                            lineColor = bb.asPixel(),
                        ),
                    )
                    log.info { "Line settings changed to {width: $textToInput, color: $currentColor}" }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                content = {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = "Apply",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                    )
                },
            )
        }
    }
}

private fun getHSVColorList(
    state: ImageState,
    currentColor: HsvColor,
): List<HsvColor> {
    val currentColorSpace = state.colorSpace
    val color = mutableListOf(currentColor.hue / 360f, currentColor.saturation, currentColor.value)
    HsvColorSpace.toRgb(color)
    currentColorSpace.fromRgb(color)
    val colorChannelAll = color.toMutableList()
    val colorChannelOne = color.toMutableList()
    val colorChannelTwo = color.toMutableList()
    val colorChannelThree = color.toMutableList()
    currentColorSpace.separateChannel(colorChannelOne, ImageChannel.CHANNEL_ONE)
    currentColorSpace.separateChannel(colorChannelTwo, ImageChannel.CHANNEL_TWO)
    currentColorSpace.separateChannel(colorChannelThree, ImageChannel.CHANNEL_THREE)
    currentColorSpace.toRgb(colorChannelAll)
    currentColorSpace.toRgb(colorChannelOne)
    currentColorSpace.toRgb(colorChannelTwo)
    currentColorSpace.toRgb(colorChannelThree)
    HsvColorSpace.fromRgb(colorChannelAll)
    HsvColorSpace.fromRgb(colorChannelOne)
    HsvColorSpace.fromRgb(colorChannelTwo)
    HsvColorSpace.fromRgb(colorChannelThree)
    return listOf(
        HsvColor(colorChannelAll[0] * 360f, colorChannelAll[1], colorChannelAll[2], currentColor.alpha),
        HsvColor(colorChannelOne[0] * 360f, colorChannelOne[1], colorChannelOne[2], currentColor.alpha),
        HsvColor(colorChannelTwo[0] * 360f, colorChannelTwo[1], colorChannelTwo[2], currentColor.alpha),
        HsvColor(colorChannelThree[0] * 360f, colorChannelThree[1], colorChannelThree[2], currentColor.alpha),
    )
}
