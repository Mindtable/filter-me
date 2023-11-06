package ru.itmo.graphics.viewmodel.presentation.view.settings.autocorrection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsType
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsViewProvider
import ru.itmo.graphics.viewmodel.presentation.viewmodel.AutoCorrect
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageState

private val log = KotlinLogging.logger { }

class AutoCorrectionSettingsProvider : SettingsViewProvider {

    override val type: SettingsType
        get() = SettingsType.AUTOCORRECTION

    @Composable
    override fun draw(state: ImageState, onEvent: (ImageEvent) -> Unit) {
        AutoCorrectionSettings(state, onEvent)
    }

    @Composable
    override fun configureWindow(window: ComposeWindow) {
        window.isResizable = false
    }
}

@Composable
private fun AutoCorrectionSettings(state: ImageState, onEvent: (ImageEvent) -> Unit) {
    Box(Modifier.background(color = MaterialTheme.colorScheme.primary)) {
        Column(
            modifier = Modifier
                .width(500.dp)
                .padding(10.dp, 20.dp),
        ) {
            var textToInput by remember { mutableStateOf("") }
            var chosenCoefficient by remember { mutableStateOf(0f) }
            val textUpdate: (String) -> Unit = { input -> textToInput = input }
            val updateCoefficient: () -> Boolean = updateCoefficient@{
                log.info { "Event with enter processing" }
                val newCoefficient = textToInput.toFloatOrNull() ?: return@updateCoefficient false

                chosenCoefficient = if (newCoefficient >= 0.5f) {
                    0.49f
                } else if (newCoefficient < 0f) {
                    0f
                } else {
                    newCoefficient
                }
                log.info { "Change autocorrection coefficient to $chosenCoefficient" }

                true
            }

            ChosenCoefficient(chosenCoefficient)
            TextInput(
                Modifier.onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyUp && event.key == Key.Enter) {
                        updateCoefficient()
                    }
                    false
                },
                textToInput,
                textUpdate,
            )
            ButtonsRow {
                updateCoefficient()
                onEvent(AutoCorrect(chosenCoefficient))
            }
        }
    }
}

@Composable
private fun ColumnScope.ChosenCoefficient(coefficient: Float) {
    log.info { "Redraw ChosenCoefficient with $coefficient" }
    Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
        Text(
            text = "Chosen coefficient is $coefficient",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun TextInput(
    modifier: Modifier,
    textToInput: String,
    textUpdate: (String) -> Unit,
) {
    Row {
        TextField(
            value = textToInput,
            onValueChange = textUpdate,
            modifier = modifier
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

@Composable
private fun ColumnScope.ButtonsRow(
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .align(Alignment.CenterHorizontally),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        AutoCorrectionButton(onClick)
    }
}

@Composable
private fun AutoCorrectionButton(
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Text(
            text = "Start autocorrection",
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}
