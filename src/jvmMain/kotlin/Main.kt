import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ru.itmo.graphics.image.type.FileTypeResolver
import ru.itmo.graphics.image.type.P5TypeResolver
import ru.itmo.graphics.image.type.P6TypeResolver
import ru.itmo.graphics.image.type.SkiaSupportedTypeResolver
import ru.itmo.graphics.viewmodel.presentation.view.MainWindowView
import ru.itmo.graphics.viewmodel.presentation.view.MenuBarView
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageViewModel
import java.awt.Dimension

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

        Window(
            onCloseRequest = ::exitApplication,
            title = "Nascar95 GUI",
            state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified),
        ) {
            setMinWindowSize()
            MenuBarView(viewModel::onEvent)
            MaterialTheme {
                val state by viewModel.state.collectAsState()
                MainWindowView(
                    window,
                    state,
                    state.bitmap?.asComposeImageBitmap(),
                    scope,
                    viewModel::onEvent,
                )
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
