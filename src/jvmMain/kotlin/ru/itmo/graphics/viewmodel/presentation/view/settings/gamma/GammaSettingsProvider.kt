package ru.itmo.graphics.viewmodel.presentation.view.settings.gamma

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsType
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsViewProvider
import ru.itmo.graphics.viewmodel.presentation.viewmodel.AssignGamma
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ConvertGamma
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageState

private val log = KotlinLogging.logger { }

class GammaSettingsProvider : SettingsViewProvider {

    override val type: SettingsType
        get() = SettingsType.GAMMA

    @Composable
    override fun draw(state: ImageState, onEvent: (ImageEvent) -> Unit) {
        GammaSettings(state, onEvent)
    }

    @Composable
    override fun configureWindow(window: ComposeWindow) {
        window.isResizable = false
    }
}

@Composable
private fun GammaSettings(state: ImageState, onEvent: (ImageEvent) -> Unit) {
    Box(Modifier.background(color = MaterialTheme.colorScheme.primary)) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .padding(10.dp, 20.dp),
        ) {
            var textToInput by remember { mutableStateOf("") }
            val textUpdate: (String) -> Unit = { input -> textToInput = input }

            AssignedGamma(state.gamma)
            TextInput(textToInput, textUpdate)
            ButtonsRow(textToInput, textUpdate, onEvent)
        }
    }
}

@Composable
private fun ColumnScope.AssignedGamma(gamma: Float) {
    Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
        Text(
            text = "Assigned gamma is $gamma",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
        )
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

@Composable
private fun ColumnScope.ButtonsRow(
    textToInput: String,
    textUpdate: (String) -> Unit,
    onEvent: (ImageEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .align(Alignment.CenterHorizontally),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        GammaSettingsButtonType.entries.forEach {
            it.asButton(textToInput, textUpdate, onEvent)
        }
    }
}

enum class GammaSettingsButtonType(val title: String) {
    CONVERT("Convert") {
        override fun createEvent(gamma: Float): ImageEvent = ConvertGamma(gamma)
    },
    ASSIGN("Assign") {
        override fun createEvent(gamma: Float): ImageEvent = AssignGamma(gamma)
    },
    ;

    abstract fun createEvent(gamma: Float): ImageEvent
}

@Composable
private fun GammaSettingsButtonType.asButton(
    textToInput: String,
    textUpdate: (String) -> Unit,
    onEvent: (ImageEvent) -> Unit,
) = GammaSettingsButton(this, textToInput, textUpdate, onEvent)

@Composable
private fun GammaSettingsButton(
    type: GammaSettingsButtonType,
    textToInput: String,
    textUpdate: (String) -> Unit,
    onEvent: (ImageEvent) -> Unit,
) {
    Button(
        onClick = { gammaSettingsButtonClick(type, textToInput, onEvent, textUpdate) },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Text(
            text = type.title,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

private fun gammaSettingsButtonClick(
    type: GammaSettingsButtonType,
    textToInput: String,
    onEvent: (ImageEvent) -> Unit,
    textUpdate: (String) -> Unit,
) {
    log.info { "$type onClick" }
    textToInput.toFloatOrNull()?.let { newGamme ->
        onEvent(type.createEvent(newGamme))
        textUpdate("")
    }
}
