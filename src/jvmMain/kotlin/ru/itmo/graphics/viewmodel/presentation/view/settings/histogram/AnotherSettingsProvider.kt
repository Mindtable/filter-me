package ru.itmo.graphics.viewmodel.presentation.view.settings.histogram

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.domain.image.colorspace.ApplicationColorSpace
import ru.itmo.graphics.viewmodel.domain.image.colorspace.YCbCr601ColorSpace
import ru.itmo.graphics.viewmodel.domain.image.colorspace.YCbCr709ColorSpace
import ru.itmo.graphics.viewmodel.domain.image.colorspace.YCoCgColorSpace
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.DescriptionText
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsType
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsViewProvider
import ru.itmo.graphics.viewmodel.presentation.view.theme.histogramBlue
import ru.itmo.graphics.viewmodel.presentation.view.theme.histogramGreen
import ru.itmo.graphics.viewmodel.presentation.view.theme.histogramRed
import ru.itmo.graphics.viewmodel.presentation.viewmodel.DarkModeSettingSwitch
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageState
import ru.itmo.graphics.viewmodel.tools.transformPixelToInt
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger { }

class AnotherSettingsProvider : SettingsViewProvider {

    override val type: SettingsType
        get() = SettingsType.HISTOGRAM

    @Composable
    override fun draw(state: ImageState, onEvent: (ImageEvent) -> Unit) {
        var distributionState by remember { mutableStateOf(BrightnessDistribution(256)) }
        LaunchedEffect(state.imageVersion, state.colorSpace) {
            log.info { "Launch histogram calculation with imageVersion ${state.imageVersion}" }
            val pixelData = state.pixelData ?: return@LaunchedEffect

            val distribution: BrightnessDistribution
            measureTimeMillis { distribution = pixelData.getDistribution(state.colorSpace) }
                .also { log.info { "Distribution calculation took $it ms" } }

            distributionState = distribution
        }

        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .width(600.dp)
                .padding(10.dp),
        ) {
            Row {
                RadioButton(
                    selected = state.isDarkMode,
                    onClick = { onEvent(DarkModeSettingSwitch) },
                )
                Spacer(Modifier.width(30.dp))
                Text(
                    color = MaterialTheme.colorScheme.onBackground,
                    text = "Dark mode setting",
                )
            }
            DescriptionText(
                text = "Histogram view of brightness distribution",
            )
            Histogram(
                distributionState.allChannels,
                modifier = Modifier.height(200.dp),
            )
            Spacer(modifier = Modifier.height(20.dp).fillMaxWidth())
            Histogram(
                distributionState.channelOne,
                modifier = Modifier.height(200.dp),
                strokeColor = MaterialTheme.colorScheme.histogramRed,
            )
            Spacer(modifier = Modifier.height(20.dp).fillMaxWidth())
            Histogram(
                distributionState.channelTwo,
                modifier = Modifier.height(200.dp),
                strokeColor = MaterialTheme.colorScheme.histogramGreen,
            )
            Spacer(modifier = Modifier.height(20.dp).fillMaxWidth())
            Histogram(
                distributionState.channelThree,
                modifier = Modifier.height(200.dp),
                strokeColor = MaterialTheme.colorScheme.histogramBlue,
            )
        }
    }

    @Composable
    override fun configureWindow(window: ComposeWindow) {
        window.isResizable = false
    }
}

class BrightnessDistribution(
    private val initialSize: Int = 255,
) {
    val allChannels: Array<Float> = Array(initialSize) { 0f }
    val channelOne: Array<Float> = Array(initialSize) { 0f }
    val channelTwo: Array<Float> = Array(initialSize) { 0f }
    val channelThree: Array<Float> = Array(initialSize) { 0f }

    fun normalize(maxValue: Float) {
        for (i in 0..<initialSize) {
            allChannels[i] = allChannels[i] / maxValue
            channelOne[i] = channelOne[i] / maxValue
            channelTwo[i] = channelTwo[i] / maxValue
            channelThree[i] = channelThree[i] / maxValue
        }
    }
}

fun PixelData.getDistribution(colorSpace: ApplicationColorSpace): BrightnessDistribution {
    val pixelData = this
    val distribution = BrightnessDistribution(256)
    val kostyl = colorSpace.isShiftNeeded()
    pixelData.consumeWithEachPixel { pixel ->
        val allBrightness = transformPixelToInt(pixel.sum() / 3 + if (kostyl) 1f / 3 else 0f)

        val channelOneBrightness = transformPixelToInt(pixel[0])
        val channelTwoBrightness = transformPixelToInt(pixel[1] + if (kostyl) 0.5f else 0f)
        val channelThreeBrightness = transformPixelToInt(pixel[2] + if (kostyl) 0.5f else 0f)

        distribution.allChannels[allBrightness]++
        distribution.channelOne[channelOneBrightness]++
        distribution.channelTwo[channelTwoBrightness]++
        distribution.channelThree[channelThreeBrightness]++
    }

    distribution.normalize(pixelData.width * pixelData.height.toFloat())

    return distribution
}

fun ApplicationColorSpace.isShiftNeeded(): Boolean = when (this) {
    is YCoCgColorSpace, YCbCr709ColorSpace, YCbCr601ColorSpace -> true
    else -> false
}
