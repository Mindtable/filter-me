package ru.itmo.graphics.viewmodel.presentation.view.settings.colorpicker

import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.godaddy.android.colorpicker.HsvColor
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.viewmodel.domain.image.colorspace.HsvColorSpace
import ru.itmo.graphics.viewmodel.presentation.view.main.ImageChannel
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsType
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsViewProvider
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageState

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
        var currentColor by remember {
            mutableStateOf(HsvColor.from(Color.Red))
        }
        val colorUpdate: (HsvColor) -> Unit = {color -> currentColor = color}
        val colorByChannels = getHSVColorList(state, currentColor)
        var textToInput by remember {
            mutableStateOf("1")
        }
        val textUpdate: (String) -> Unit = { input -> textToInput = input }
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .width(350.dp)
                .padding(10.dp),
        ) {
            DescriptionText(Modifier, "Line width")
            LineWidthInput(Modifier, textToInput, textUpdate)
            DescriptionText(Modifier, "Line color")
            ColorAlphaPicker(Modifier, colorUpdate)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                    .padding(10.dp)
                    .align(Alignment.End),
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        // TODO: onEvent change ImageState
                        log.info{"Line settings changed to {width: $textToInput, color: $currentColor}"}
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
        HsvColor(colorChannelThree[0] * 360f, colorChannelThree[1], colorChannelThree[2], currentColor.alpha)
    )
}
