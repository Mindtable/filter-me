package ru.itmo.graphics.viewmodel.presentation.view.settings.dithering

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.viewmodel.presentation.view.main.DitheringAlgo
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsType
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsViewProvider
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.DescriptionText
import ru.itmo.graphics.viewmodel.presentation.viewmodel.UpdateDitheringSettings
import kotlin.math.roundToInt

private val log = KotlinLogging.logger { }

class DitheringSettingsProvider : SettingsViewProvider {

    override val type: SettingsType
        get() = SettingsType.DITHERING

    @Composable
    override fun draw(state: ImageState, onEvent: (ImageEvent) -> Unit) {
        DitheringSettings(state, onEvent)
    }

    @Composable
    override fun configureWindow(window: ComposeWindow) {
        window.isResizable = false
    }
}

@Composable
private fun DitheringSettings(state: ImageState, onEvent: (ImageEvent) -> Unit) {
    var selectedAlgo by remember {
        mutableStateOf(state.ditheringAlgo)
    }
    var bitness by remember {
        mutableStateOf(state.bitness.toFloat())
    }
    var previewToggled by remember {
        mutableStateOf(state.isPreviewMode)
    }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .width(350.dp)
            .padding(10.dp),
    ) {
        DescriptionText(text = "Dithering algorithm")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            var expanded by remember {
                mutableStateOf(false)
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopEnd),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                onClick = { expanded = !expanded },
                shape = RoundedCornerShape(10.dp),
            ) {
                DescriptionText(
                    modifier = Modifier.weight(1f),
                    text = selectedAlgo.text,
                )
                Icon(
                    Icons.Sharp.MoreVert,
                    "Show other algo options",
                    modifier = Modifier.weight(0.1f),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DitheringAlgo.entries.forEach { algo ->
                    DropdownMenuItem(
                        text = { DescriptionText(text = algo.text) },
                        onClick = { selectedAlgo = algo },
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                DescriptionText(text = "Dithering bitness: ${bitness.roundToInt()}-bit")
                Slider(
                    value = bitness,
                    onValueChange = { bitness = it },
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.secondary,
                        activeTrackColor = MaterialTheme.colorScheme.secondary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    steps = 6,
                    valueRange = 1f..8f,
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            DescriptionText(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f),
                text = "Preview:",
            )
            Checkbox(
                checked = previewToggled,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f),
                onCheckedChange = { previewToggled = it },
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
                    val bitnessValue = bitness.roundToInt()
                    onEvent(
                        UpdateDitheringSettings(
                            ditheringAlgo = selectedAlgo,
                            bitness = bitnessValue,
                            preview = previewToggled,
                        )
                    )
                    log.info { "Dithering settings changed to {algo: $selectedAlgo, bitness: $bitnessValue, preview: $previewToggled}" }
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