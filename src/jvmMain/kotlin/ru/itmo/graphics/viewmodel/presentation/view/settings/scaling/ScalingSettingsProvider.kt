package ru.itmo.graphics.viewmodel.presentation.view.settings.scaling

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.CheckCircle
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.viewmodel.domain.scale.SplineBC
import ru.itmo.graphics.viewmodel.presentation.view.main.ScalingAlgo
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.DescriptionText
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsType
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsViewProvider
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageState
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ScaleImage

private val log = KotlinLogging.logger { }

class ScalingSettingsProvider : SettingsViewProvider {

    override val type: SettingsType
        get() = SettingsType.SCALING

    @Composable
    override fun draw(state: ImageState, onEvent: (ImageEvent) -> Unit) {
        ScalingSettings(state, onEvent)
    }

    @Composable
    override fun configureWindow(window: ComposeWindow) {
        window.isResizable = false
    }
}

@Composable
private fun ScalingSettings(state: ImageState, onEvent: (ImageEvent) -> Unit) {
    var selectedAlgo by rememberSaveable {
        mutableStateOf(ScalingAlgo.NEAREST)
    }
    var widthInput by rememberSaveable {
        mutableStateOf("200")
    }
    var heightInput by rememberSaveable {
        mutableStateOf("200")
    }
    var widthCenterInput by rememberSaveable {
        mutableStateOf("100.0")
    }
    var heightCenterInput by rememberSaveable {
        mutableStateOf("100.0")
    }
    var bcBInput by rememberSaveable {
        mutableStateOf(SplineBC.b.toString())
    }
    var bcCInput by rememberSaveable {
        mutableStateOf(SplineBC.c.toString())
    }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .width(350.dp)
            .padding(10.dp),
    ) {
        DescriptionText(text = "Scaling algorithm")
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
                ScalingAlgo.entries.forEach { algo ->
                    DropdownMenuItem(
                        text = { DescriptionText(text = algo.text) },
                        onClick = {
                            selectedAlgo = algo
                            expanded = false
                        },
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
                DescriptionText(text = "BC-Spline settings")
                TextInput(bcBInput) { input -> bcBInput = input }
                TextInput(bcCInput) { input -> bcCInput = input }
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        val newBcBValue = bcBInput.toFloat()
                        val newBcCValue = bcCInput.toFloat()
                        SplineBC.setBC(newBcBValue, newBcCValue)
                        log.info { "BC-Spline B and C set to ${SplineBC.b}, ${SplineBC.c}" }
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                DescriptionText(text = "New width x height")
                TextInput(widthInput) { input -> widthInput = input }
                TextInput(heightInput) { input -> heightInput = input }
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
                DescriptionText(text = "Scale center")
                TextInput(widthCenterInput) { input -> widthCenterInput = input }
                TextInput(heightCenterInput) { input -> heightCenterInput = input }
            }
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
                    val width = widthInput.toInt()
                    val height = heightInput.toInt()
                    val widthCenter = widthCenterInput.toFloat()
                    val heightCenter = heightCenterInput.toFloat()
                    onEvent(
                        ScaleImage(
                            newWidth = width,
                            newHeight = height,
                            widthCenter = widthCenter,
                            heightCenter = heightCenter,
                            algorithm = selectedAlgo.scalingAlgorithm,
                        ),
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                content = {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = "Scale",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center,
                    )
                },
            )
        }
    }
}

@Composable
private fun TextInput(
    textToInput: String,
    textUpdate: (String) -> Unit,
) {
    Row {
        TextField(
            value = textToInput,
            onValueChange = textUpdate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(10.dp),
            trailingIcon = {
                Icon(Icons.Sharp.CheckCircle, "", tint = MaterialTheme.colorScheme.onSecondaryContainer)
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                cursorColor = MaterialTheme.colorScheme.onSecondaryContainer,
                focusedIndicatorColor = Color.Transparent, // hide the indicator
                unfocusedIndicatorColor = Color.Transparent,
            ),
            singleLine = true,
        )
    }
}
