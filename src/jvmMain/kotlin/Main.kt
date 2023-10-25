import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ru.itmo.graphics.viewmodel.presentation.view.theme.AppTheme
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.viewmodel.domain.image.type.FileTypeResolver
import ru.itmo.graphics.viewmodel.domain.image.type.P5TypeResolver
import ru.itmo.graphics.viewmodel.domain.image.type.P6TypeResolver
import ru.itmo.graphics.viewmodel.domain.image.type.SkiaSupportedTypeResolver
import ru.itmo.graphics.viewmodel.presentation.view.main.MainWindowView
import ru.itmo.graphics.viewmodel.presentation.view.main.MenuBarView
import ru.itmo.graphics.viewmodel.presentation.view.settings.colorpicker.LineSettingsProvider
import ru.itmo.graphics.viewmodel.presentation.view.settings.gamma.GammaSettingsProvider
import ru.itmo.graphics.viewmodel.presentation.view.settings.histogram.AnotherSettingsProvider
import ru.itmo.graphics.viewmodel.presentation.viewmodel.CloseSettings
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageViewModel
import ru.itmo.graphics.viewmodel.tools.toBitmap
import java.awt.Dimension

private val log = KotlinLogging.logger { }

fun main() {
    application {
        val scope = rememberCoroutineScope()
        val viewModel = remember {
            ImageViewModel(
                scope,
                FileTypeResolver(
                    listOf(
                        P5TypeResolver(),
                        P6TypeResolver(),
                        SkiaSupportedTypeResolver(),
                    ),
                ),
            )
        }

        val state by viewModel.state.collectAsState()
        val settingsWindowProviders = remember {
            listOf(
                GammaSettingsProvider(),
                AnotherSettingsProvider(),
                LineSettingsProvider(),
            ).associateBy { it.type }
        }

        Window(
            onCloseRequest = ::exitApplication,
            title = "Nascar95 GUI",
            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified),
            alwaysOnTop = state.settingsType == null,

        ) {
            val imageBitmap by remember(
                state.pixelData,
                state.colorSpace,
                state.channel,
                state.isMonochromeMode,
                state.gamma,
                state.imageVersion,
            ) {
                mutableStateOf(
                    state.pixelData?.toBitmap(
                        state.colorSpace,
                        state.channel,
                        state.isMonochromeMode,
                        state.gamma,
                    )?.asComposeImageBitmap(),
                )
            }

            val imageBitMap2 by remember(state.bitmap) {
                mutableStateOf(state.bitmap?.asComposeImageBitmap())
            }

            setMinWindowSize()
            MenuBarView(
                state,
                viewModel::onEvent,
            )
            AppTheme(useDarkTheme = state.isDarkMode) {
                MainWindowView(
                    window,
                    state,
                    imageBitmap ?: imageBitMap2,
                    scope,
                    viewModel::onEvent,
                )
            }
        }

        settingsWindowProviders[state.settingsType]?.let { viewProvider ->
            log.info { "Coroutine launch" }
            Window(
                onCloseRequest = { viewModel.onEvent(CloseSettings) },
                title = viewProvider.type.description,
                state = rememberWindowState(size = DpSize.Unspecified),
                focusable = true,
                alwaysOnTop = state.settingsType != null,
            ) {
                viewProvider.configureWindow(window)
                AppTheme(useDarkTheme = state.isDarkMode) {
                    viewProvider.draw(state, viewModel::onEvent)
                }
            }
        }
    }
}

@Composable
fun Dp.dpRoundToPx() = with(LocalDensity.current) { this@dpRoundToPx.roundToPx() }

@Composable
private fun FrameWindowScope.setMinWindowSize() {
    window.minimumSize = Dimension(100.dp.dpRoundToPx(), 100.dp.dpRoundToPx())
}
