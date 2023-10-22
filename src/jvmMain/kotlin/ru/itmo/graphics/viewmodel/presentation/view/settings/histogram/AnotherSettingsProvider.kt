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
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsType
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsViewProvider
import ru.itmo.graphics.viewmodel.presentation.view.theme.histogramBlue
import ru.itmo.graphics.viewmodel.presentation.view.theme.histogramGreen
import ru.itmo.graphics.viewmodel.presentation.view.theme.histogramRed
import ru.itmo.graphics.viewmodel.presentation.viewmodel.DarkModeSettingSwitch
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageState
import kotlin.random.Random

private val log = KotlinLogging.logger { }

class AnotherSettingsProvider : SettingsViewProvider {

    override val type: SettingsType
        get() = SettingsType.HISTOGRAM

    @Composable
    override fun draw(state: ImageState, onEvent: (ImageEvent) -> Unit) {
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
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                text = "Histogram view of brightness distribution",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Histogram(
                arrayOf(0.2f, 0.0f, 0.1f, 0.3f, 0.2f),
                modifier = Modifier.height(200.dp),
            )
            Spacer(modifier = Modifier.height(20.dp).fillMaxWidth())
            Histogram(
                Array(255) { Random.nextInt(100) / 100f },
                modifier = Modifier.height(200.dp),
                strokeColor = MaterialTheme.colorScheme.histogramRed,
            )
            Spacer(modifier = Modifier.height(20.dp).fillMaxWidth())
            Histogram(
                Array(255) { Random.nextInt(100) / 100f },
                modifier = Modifier.height(200.dp),
                strokeColor = MaterialTheme.colorScheme.histogramGreen,
            )
            Spacer(modifier = Modifier.height(20.dp).fillMaxWidth())
            Histogram(
                Array(255) { Random.nextInt(100) / 100f },
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
