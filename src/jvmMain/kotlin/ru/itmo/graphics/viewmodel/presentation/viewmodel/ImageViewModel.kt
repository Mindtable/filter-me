package ru.itmo.graphics.viewmodel.presentation.viewmodel

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.ColorAlphaType.PREMUL
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import ru.itmo.graphics.viewmodel.domain.ImageModel
import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.domain.image.type.FileTypeResolver
import ru.itmo.graphics.viewmodel.domain.model.image.PnmP5
import ru.itmo.graphics.viewmodel.presentation.view.main.FileDialogType.NONE
import ru.itmo.graphics.viewmodel.presentation.view.main.FileDialogType.OPEN
import ru.itmo.graphics.viewmodel.presentation.view.main.FileDialogType.SAVE
import ru.itmo.graphics.viewmodel.presentation.view.main.ImageChannel
import ru.itmo.graphics.viewmodel.tools.convertColorSpace
import ru.itmo.graphics.viewmodel.tools.convertGamma
import ru.itmo.graphics.viewmodel.tools.createGradient
import ru.itmo.graphics.viewmodel.tools.readImageV2
import ru.itmo.graphics.viewmodel.tools.toBitmap
import java.io.File
import kotlin.math.max

private val logger = KotlinLogging.logger { }

class ImageViewModel(
    private val scope: CoroutineScope,
    private val fileTypeResolver: FileTypeResolver,
) {
    val state = MutableStateFlow(ImageState())

    fun onEvent(event: ImageEvent) {
        when (event) {
            ComputeGradient -> {
                scope.launch(SupervisorJob() + coroutineExceptionHandler()) {
                    state.update {
                        it.copy(
                            log = "Gradient generated",
                            pixelData = createGradient(),
                        )
                    }
                }
            }

            DrawingModeSwitch -> {
                state.update {
                    it.copy(
                        log = "${if (it.drawingModeEnable) "Finish" else "Start"} drawing",
                        firstDrawPoint = null,
                        drawingModeEnable = !it.drawingModeEnable,
                    )
                }
            }

            is SendDrawingCoordinates -> {
                if (!state.value.drawingModeEnable) return

                if (state.value.firstDrawPoint == null) {
                    state.update {
                        it.copy(
                            log = "Set first point to ${event.coordinates}",
                            firstDrawPoint = event.coordinates,
                        )
                    }
                } else {
                    val firstDrawPoint = state.value.firstDrawPoint
                    val log = "Draw line with coordinates $firstDrawPoint and ${event.coordinates}"
                    state.update {
                        it.copy(
                            firstDrawPoint = null,
                            log = log,
                            drawingModeEnable = false,
                        )
                    }
                    scope.launch(SupervisorJob() + coroutineExceptionHandler()) {
                        logger.info { log }
                        // do not forget about mono-channel-mode
                        // drawLine(start, end, color, pixelData)
                    }
                }
            }

            is SaveAsEvent -> {
                val imageState = state.value
                logger.info { "onSavedAsButtonClick call with ${imageState.file} parameter" }

                val imageModel = imageState.imageModel ?: throw IllegalStateException("Image model cannot be null!")
                val bitmapToSave = imageState.pixelData!!.toBitmap(
                    state.value.colorSpace,
                    state.value.channel,
                    true,
                    state.value.gamma,
                )

                scope.launch {
                    if (state.value.channel == ImageChannel.ALL) {
                        imageModel.saveTo(event.path, bitmapToSave)
                    } else {
                        imageModel.saveTo(event.path, bitmapToSave, PnmP5)
                    }

                    state.update {
                        it.copy(
                            log = "Image saved as ${event.path}!",
                        )
                    }
                }
            }

            is MonochromeModeChanged -> {
                state.update {
                    it.copy(
                        log = "Monochrome mode is ${if (it.isMonochromeMode) "disabled" else "enabled"}",
                        isMonochromeMode = !it.isMonochromeMode,
                    )
                }
            }

            is ChannelSettingsChanged -> {
                state.update {
                    it.copy(
                        channel = event.channel,
                    )
                }
            }

            is SaveEvent -> {
                val imageState = state.value
                logger.info { "onSavedAsButtonClick call with ${imageState.file} parameter" }

                val imageModel = imageState.imageModel ?: throw IllegalStateException("Image model cannot be null!")
                val bitmapToSave = imageState.pixelData!!.toBitmap(
                    state.value.colorSpace,
                    state.value.channel,
                    true,
                    state.value.gamma,
                )

                scope.launch {
                    if (state.value.channel == ImageChannel.ALL) {
                        imageModel.saveTo(imageModel.file.absolutePath, bitmapToSave)
                    } else {
                        imageModel.saveTo(imageModel.file.absolutePath, bitmapToSave, PnmP5)
                    }

                    state.update {
                        it.copy(
                            log = "Image saved!",
                        )
                    }
                }
            }

            is UpdateLineSettings -> {
                if (event.lineOpacity !in 0f..1f) {
                    onEvent(ImageError(RuntimeException("Line opacity should be in range [0;1]")))
                    return
                }

                if (event.lineWidth !in 0f..100f) {
                    onEvent(ImageError(RuntimeException("Line width should be in range [0;100]")))
                    return
                }

                state.update {
                    it.copy(
                        lineColor = event.lineColor,
                        lineOpacity = event.lineOpacity,
                        lineWidth = event.lineWidth,
                        log = "Line settings updated",
                    )
                }
            }

            is UpdateDitheringSettings -> {
                state.update{
                    it.copy(
                        ditheringAlgo = event.ditheringAlgo,
                        bitness = event.bitness,
                        isPreviewMode = event.preview,
                        log = "Dithering settings updated",
                    )
                }
            }

            is ApplicationColorSpaceChanged -> {
                scope.launch(SupervisorJob() + coroutineExceptionHandler()) {
                    val newColorSpace = event.colorSpace
                    if (state.value.colorSpace != newColorSpace) {
                        state.update {
                            it.copy(
                                log = "Colorspace changed to ${newColorSpace.name}",
                                pixelData = it.pixelData?.convertColorSpace(it.colorSpace, newColorSpace),
                                colorSpace = newColorSpace,
                                imageVersion = it.imageVersion + 1,
                                lineColor = it.lineColor.convertColorSpace(it.colorSpace, newColorSpace),
                            )
                        }
                        logger.info { "Color space change success with imageVersion ${state.value.imageVersion}" }
                    }
                }
            }

            is ConvertGamma -> {
                scope.launch(SupervisorJob() + coroutineExceptionHandler()) {
                    if (event.newGamma < 0f || event.newGamma > 10f) {
                        throw IllegalStateException("Gamma need to be in [0, 10]!")
                    }
                    val newGamma: Float = if (event.newGamma != 0f) {
                        max(event.newGamma, 0.1f)
                    } else {
                        0f
                    }
                    state.update {
                        it.copy(
                            log = "Image gamma converted to $newGamma",
                            pixelData = it.pixelData?.convertGamma(state.value.gamma, newGamma),
                            gamma = newGamma,
                            imageVersion = it.imageVersion + 1,
                        )
                    }
                }
            }

            is AssignGamma -> {
                scope.launch(SupervisorJob() + coroutineExceptionHandler()) {
                    if (event.newGamma < 0f || event.newGamma > 10f) {
                        throw IllegalStateException("Gamma need to be in [0, 10]!")
                    }
                    val newGamma: Float = if (event.newGamma != 0f) {
                        max(event.newGamma, 0.1f)
                    } else {
                        0f
                    }
                    state.update {
                        it.copy(
                            log = "Image gamma assigned to $newGamma",
                            gamma = newGamma,
                            imageVersion = it.imageVersion + 1,
                        )
                    }
                }
            }

            OpenFileEvent -> {
                state.update {
                    it.copy(
                        log = "Opening file...",
                        openFileDialog = OPEN,
                    )
                }
            }

            StartSaveAsEvent -> {
                state.update {
                    it.copy(
                        log = "Opening file...",
                        openFileDialog = SAVE,
                    )
                }
            }

            OpeningFileEvent -> {
                state.update {
                    it.copy(
                        openFileDialog = NONE,
                    )
                }
            }

            is ImageError -> {
                state.update {
                    it.copy(
                        log = event.error.stackTraceToString(),
                        isError = true,
                    )
                }
            }

            ImageErrorDismissed -> {
                state.update {
                    it.copy(
                        log = "Everything's OK",
                        isError = false,
                    )
                }
            }

            is FileOpenedEvent -> {
                scope.launch(SupervisorJob() + coroutineExceptionHandler()) {
                    val file = File(event.absolutePath)
                    val bytes = file.readBytes()

                    val type = fileTypeResolver.resolveType(file, bytes)

                    val imageModel = ImageModel(
                        file = file,
                        type = type,
                        data = bytes,
                        bitmap = null,
                    )
                    val bitmap = when {
                        type.isSupported -> {
                            readImageV2(imageModel)
                        }

                        else -> {
                            val image = Image.makeFromEncoded(bytes)
                            val bitmap = Bitmap()
                            bitmap.allocPixels(ImageInfo.makeN32(image.width, image.height, PREMUL))
                            val canvas = Canvas(bitmap)
                            canvas.drawImage(image, 0f, 0f)
                            bitmap.setImmutable()

                            bitmap
                        }
                    }

                    state.update {
                        when (bitmap) {
                            is PixelData -> it.copy(
                                log = "${event.absolutePath} opened",
                                file = event.absolutePath,
                                isError = false,
                                openFileDialog = NONE,
                                pixelData = bitmap,
                                imageModel = imageModel,
                            )

                            is Bitmap -> it.copy(
                                log = "${event.absolutePath} opened",
                                file = event.absolutePath,
                                isError = false,
                                openFileDialog = NONE,
                                bitmap = bitmap,
                                imageModel = imageModel,
                            )

                            else -> throw IllegalStateException("Unsupported type!")
                        }
                    }
                }
            }

            is OpenSettings -> {
                scope.launch(SupervisorJob() + coroutineExceptionHandler()) {
                    if (state.value.settingsType == event.settingsType) {
                        return@launch
                    }

                    state.update {
                        logger.info { "Settings update to ${event.settingsType}" }
                        it.copy(
                            settingsType = event.settingsType,
                        )
                    }
                }
            }

            CloseSettings -> {
                state.update {
                    it.copy(
                        settingsType = null,
                    )
                }
            }

            DarkModeSettingSwitch -> {
                state.update {
                    it.copy(
                        isDarkMode = !it.isDarkMode,
                        log = "Dark mode switch!",
                    )
                }
            }
        }
    }
}

fun ImageViewModel.coroutineExceptionHandler() = CoroutineExceptionHandler { _, exception: Throwable ->
    exception.printStackTrace()
    onEvent(ImageError(exception))
}
