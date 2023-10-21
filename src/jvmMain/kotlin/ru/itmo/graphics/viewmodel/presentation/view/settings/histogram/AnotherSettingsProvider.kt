package ru.itmo.graphics.viewmodel.presentation.view.settings.histogram

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import dpRoundToPx
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsType
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsViewProvider
import ru.itmo.graphics.viewmodel.presentation.viewmodel.AssignGamma
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ConvertGamma
import ru.itmo.graphics.viewmodel.presentation.viewmodel.DarkModeSettingSwitch
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageState
import java.awt.Dimension

class AnotherSettingsProvider : SettingsViewProvider {

    override val type: SettingsType
        get() = SettingsType.HISTOGRAM

    @Composable
    override fun draw(state: ImageState, onEvent: (ImageEvent) -> Unit) {
        Column {
            var textToInput by remember { mutableStateOf("") }
            Row {
                Text(
                    text = "Assigned gamma is ${state.gamma}",
                )
            }
            Row {
                TextField(
                    value = textToInput,
                    onValueChange = { textToInput = it },
                )
            }
            Row {
                Button(onClick = {
                    textToInput.toFloatOrNull()?.let {
                        onEvent(AssignGamma(it))
                        textToInput = ""
                    }
                }) {
                    Text("Assign")
                }
                Button(onClick = {
                    textToInput.toFloatOrNull()?.let {
                        onEvent(ConvertGamma(it))
                        textToInput = ""
                    }
                }) {
                    Text("Convert")
                }
            }
            Row {
                RadioButton(
                    selected = state.isDarkMode,
                    onClick = { onEvent(DarkModeSettingSwitch) },
                )
                Spacer(Modifier.width(30.dp))
                Text(
                    color = MaterialTheme.colorScheme.onSecondary,
                    text = "Dark mode setting",
                )
            }
        }
    }

    @Composable
    override fun configureWindow(window: ComposeWindow) {
        window.minimumSize = Dimension(100.dp.dpRoundToPx(), 100.dp.dpRoundToPx())
    }
}
