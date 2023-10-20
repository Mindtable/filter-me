package ru.itmo.graphics.viewmodel.presentation.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.itmo.graphics.utils.chooseFileDialog
import ru.itmo.graphics.viewmodel.presentation.viewmodel.AssignGamma
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ConvertGamma
import ru.itmo.graphics.viewmodel.presentation.viewmodel.FileDialogType
import ru.itmo.graphics.viewmodel.presentation.viewmodel.FileDialogType.NONE
import ru.itmo.graphics.viewmodel.presentation.viewmodel.FileDialogType.OPEN
import ru.itmo.graphics.viewmodel.presentation.viewmodel.FileDialogType.SAVE
import ru.itmo.graphics.viewmodel.presentation.viewmodel.FileOpenedEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageError
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageErrorDismissed
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageState
import ru.itmo.graphics.viewmodel.presentation.viewmodel.OpeningFileEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.SaveAsEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.isOpen
import java.awt.FileDialog
import java.lang.Float.min

private val logger = KotlinLogging.logger { }

@Composable
fun MainWindowView(
    window: ComposeWindow,
    state: ImageState,
    image: ImageBitmap?,
    scope: CoroutineScope,
    onEvent: (ImageEvent) -> Unit,
) {
    when {
        state.isError -> {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState()),
            ) {
                Text(state.log)
                Spacer(Modifier.height(20.dp))
                Button(onClick = { onEvent(ImageErrorDismissed) }) {
                    Text("Dismiss")
                }
            }
        }

        else -> {
            if (state.openFileDialog.isOpen()) {
                scope.openFileDialog(window, state.openFileDialog, onEvent)
                onEvent(OpeningFileEvent)
            }

            Scaffold(
                topBar = {
                    Canvas(
                        modifier = Modifier.fillMaxWidth()
                            .fillMaxHeight(0.75f),
                    ) {
                        image?.let {
                            val scalingCoefficient = min(size.height / image.height, size.width / image.width)
                            scale(
                                scaleX = scalingCoefficient,
                                scaleY = scalingCoefficient,
                                pivot = Offset.Zero,
                            ) {
                                drawIntoCanvas {
                                    logger.info { "Canvas redrawn" }
                                    it.withSave {
                                        val paint = Paint()
                                        paint.filterQuality = FilterQuality.None
                                        it.drawImage(image, Offset.Zero, paint)
                                    }
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    BottomAppBar(
                        modifier = Modifier.fillMaxWidth()
                            .fillMaxHeight(0.15f)
                            .padding(0.2.dp),
                    ) {
                        Row {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.2f),
                            ) {
                                Text(
                                    textAlign = TextAlign.Left,
                                    text = state.log,
                                    modifier = Modifier.padding(0.3.dp),
                                )
                            }
                            Column(modifier = Modifier.fillMaxWidth(0.5f)) {
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
                            }
                        }
                    }
                },
            ) {
                logger.info { "Scaffold redrawn" }
            }
        }
    }
}

private fun CoroutineScope.openFileDialog(
    window: ComposeWindow,
    type: FileDialogType,
    onEvent: (ImageEvent) -> Unit,
) {
    launch(SupervisorJob() + coroutineExceptionHandler(onEvent)) {
        when (type) {
            OPEN -> {
                val absolutePath = chooseFileDialog(window, FileDialog.LOAD).absolutePath

                onEvent(FileOpenedEvent(absolutePath))
            }

            SAVE -> {
                val absolutePath = chooseFileDialog(window, FileDialog.SAVE).absolutePath

                onEvent(SaveAsEvent(absolutePath))
            }

            NONE -> throw IllegalStateException("FileDialogEvent call with NONE type")
        }
    }
}

private fun coroutineExceptionHandler(onEvent: (ImageEvent) -> Unit) =
    CoroutineExceptionHandler { _, exception: Throwable ->
        exception.printStackTrace()
        onEvent(ImageError(exception))
    }
