package ru.itmo.graphics.viewmodel.presentation.view.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.itmo.graphics.viewmodel.domain.Coordinates
import ru.itmo.graphics.viewmodel.presentation.view.main.FileDialogType.NONE
import ru.itmo.graphics.viewmodel.presentation.view.main.FileDialogType.OPEN
import ru.itmo.graphics.viewmodel.presentation.view.main.FileDialogType.SAVE
import ru.itmo.graphics.viewmodel.presentation.viewmodel.FileOpenedEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageError
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageErrorDismissed
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.ImageState
import ru.itmo.graphics.viewmodel.presentation.viewmodel.OpeningFileEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.SaveAsEvent
import ru.itmo.graphics.viewmodel.presentation.viewmodel.SendDrawingCoordinates
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.lang.Float.min
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger { }

@OptIn(ExperimentalComposeUiApi::class)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(color = MaterialTheme.colorScheme.background),
            ) {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState()),
                ) {
                    Text(state.log, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { onEvent(ImageErrorDismissed) },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    ) {
                        Text(
                            "Dismiss",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
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
                            .background(color = MaterialTheme.colorScheme.background)
                            .fillMaxHeight(0.85f)
                            .onPointerEvent(PointerEventType.Press) {
                                val position = it.changes.first().position
                                image ?: return@onPointerEvent
                                val scalingCoefficient = min(
                                    size.height.toFloat() / image.height,
                                    size.width.toFloat() / image.width,
                                )
                                logger.info {
                                    Coordinates(
                                        (position.x.toInt() / scalingCoefficient).roundToInt(),
                                        (position.y.toInt() / scalingCoefficient).roundToInt(),
                                    )
                                }

                                onEvent(
                                    SendDrawingCoordinates(
                                        Coordinates(
                                            (position.x.toInt() / scalingCoefficient).roundToInt(),
                                            (position.y.toInt() / scalingCoefficient).roundToInt(),
                                        ),
                                    ),
                                )
                            },
                    ) {
                        image?.let {
                            val scalingCoefficient = min(size.height / image.height, size.width / image.width)
                            scale(
                                scaleX = scalingCoefficient,
                                scaleY = scalingCoefficient,
                                pivot = Offset.Zero,
                            ) {
                                drawIntoCanvas {
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
                    var width by remember {
                        mutableStateOf(1)
                    }
                    var height by remember {
                        mutableStateOf(1)
                    }
                    BottomAppBar(
                        modifier = Modifier.fillMaxWidth()
                            .fillMaxHeight(0.15f).onSizeChanged {
                                width = it.width
                                height = it.height
                            },
                        contentColor = MaterialTheme.colorScheme.primaryContainer,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Row(
                            Modifier.fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.large,
                                ),
                        ) {
                            Column {
                                ResizedText(
                                    log = state.log,
                                    modifier = Modifier,
                                    width = width,
                                    height = height,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.displayLarge,
                                )
                            }
                        }
                    }
                },
            ) {}
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

fun chooseFileDialog(parent: Frame, mode: Int): File = FileDialog(parent, "Select File", mode)
    .apply {
        isMultipleMode = false
        isVisible = true
    }.files.first()

private fun coroutineExceptionHandler(onEvent: (ImageEvent) -> Unit) =
    CoroutineExceptionHandler { _, exception: Throwable ->
        exception.printStackTrace()
        onEvent(ImageError(exception))
    }
