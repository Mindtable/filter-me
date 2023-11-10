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
import ru.itmo.graphics.viewmodel.domain.model.image.PnmP6
import ru.itmo.graphics.viewmodel.presentation.view.main.FileDialogType.NONE
import ru.itmo.graphics.viewmodel.presentation.view.main.FileDialogType.OPEN
import ru.itmo.graphics.viewmodel.presentation.view.main.FileDialogType.SAVE
import ru.itmo.graphics.viewmodel.presentation.view.main.ImageChannel
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsType
import ru.itmo.graphics.viewmodel.presentation.view.settings.histogram.BrightnessDistribution
import ru.itmo.graphics.viewmodel.presentation.view.settings.histogram.getDistribution
import ru.itmo.graphics.viewmodel.tools.asByteArray
import ru.itmo.graphics.viewmodel.tools.autoCorrect
import ru.itmo.graphics.viewmodel.tools.convertColorSpace
import ru.itmo.graphics.viewmodel.tools.convertGamma
import ru.itmo.graphics.viewmodel.tools.createGradient
import ru.itmo.graphics.viewmodel.tools.drawInPlace
import ru.itmo.graphics.viewmodel.tools.plotLineFacade
import ru.itmo.graphics.viewmodel.tools.readImageV2
import ru.itmo.graphics.viewmodel.tools.toBitmap
import java.io.File
import java.time.Instant
import kotlin.math.max
import kotlin.system.measureTimeMillis

private val logger = KotlinLogging.logger { }

class ImageViewModel(
    private val scope: CoroutineScope,
    private val fileTypeResolver: FileTypeResolver,
) {
    val state = MutableStateFlow(ImageState())

    fun onEvent(event: ImageEvent) {
        when (event) {
            is AutoCorrect -> {
                scope.launch(SupervisorJob() + coroutineExceptionHandler()) {
                    val pixelData = state.value.pixelData ?: return@launch
                    val distribution: BrightnessDistribution

                    measureTimeMillis { distribution = pixelData.getDistribution(state.value.colorSpace) }
                        .also { logger.info { "Distribution calculation took $it ms" } }

                    autoCorrect(pixelData, distribution, event.coefficient, state.value.channel)

                    state.update {
                        it.copy(
                            log = "Autocorrection finished",
                            imageVersion = it.imageVersion + 1,
                        )
                    }
                }
            }

            ComputeGradient -> {
                scope.launch(SupervisorJob() + coroutineExceptionHandler()) {
                    state.update {
                        val pixelData = createGradient()
                        val imageModel = ImageModel(
                            file = File("/${Instant.now()}"),
                            type = PnmP6,
                            data = pixelData.asByteArray(
                                state.value.isMonochromeMode,
                                state.value.channel,
                                state.value.colorSpace,
                                state.value.gamma,
                            ),
                            bitmap = null,
                        )
                        it.copy(
                            log = "Gradient generated",
                            pixelData = pixelData,
                            imageModel = imageModel,
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

                val firstDrawPoint = state.value.firstDrawPoint
                if (firstDrawPoint == null) {
                    state.update {
                        it.copy(
                            log = "Set first point to ${event.coordinates}",
                            firstDrawPoint = event.coordinates,
                        )
                    }
                } else {
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
                        state.value.pixelData?.let { pixelData ->
                            // do not forget about mono-channel-mode
                            // drawLine(start, end, color, pixelData)
                            val lineColor = state.value.lineColor
                            val opacity = state.value.lineOpacity
                            val thick = state.value.lineWidth

                            plotLineFacade(
                                firstDrawPoint.x.toFloat(),
                                firstDrawPoint.y.toFloat(),
                                event.coordinates.x.toFloat(),
                                event.coordinates.y.toFloat(),
                                lineColor.copy(
                                    channelOne = lineColor.channelOne * opacity,
                                    channelTwo = lineColor.channelTwo * opacity,
                                    channelThree = lineColor.channelThree * opacity,
                                ),
                                pixelData,
                                thick,
                                opacity,
                            )
                            // debug purposes
                            drawInPlace(pixelData, firstDrawPoint.x, firstDrawPoint.y, listOf(0.0f, 0.0f, 0.0f), 1f)
                            drawInPlace(
                                pixelData,
                                event.coordinates.x,
                                event.coordinates.y,
                                listOf(0.0f, 0.0f, 0.0f),
                                1f,
                            )
                            state.update {
                                it.copy(
                                    log = "Drawing finished!",
                                    imageVersion = it.imageVersion + 1,
                                )
                            }
                        }
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

            is ApplyDithering -> {
                scope.launch(SupervisorJob() + coroutineExceptionHandler()) {
                    val pixelData = when (event.preview) {
                        null -> {
                            logger.info { "Change real image: preview disabled" }

                            state.value.pixelData
                        }

                        true -> {
                            logger.info { "Compute preview" }

                            state.value.pixelData?.copy()
                        }

                        false -> {
                            logger.info { "Disable preview" }

                            state.update {
                                it.copy(
                                    previewPixelData = null,
                                    imageVersion = it.imageVersion + if (it.previewPixelData != null) 1 else 0,
                                )
                            }
                            null
                        }
                    } ?: return@launch

                    logger.info { event }
                    event.ditheringAlgo.ditheringAlgorithm.applyInPlace(
                        pixelData,
                        state.value.colorSpace,
                        event.bitness,
                        state.value.isMonochromeMode,
                        state.value.gamma,
                    )

                    if (event.preview == true) {
                        state.update {
                            it.copy(
                                log = "Image dithered",
                                previewPixelData = pixelData,
                                imageVersion = it.imageVersion + 1,
                            )
                        }
                    } else {
                        state.update {
                            it.copy(
                                log = "Image dithered",
                                previewPixelData = null,
                                pixelData = pixelData,
                                imageVersion = it.imageVersion + 1,
                            )
                        }
                    }
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
                    logger.info { "Convert gamma from ${state.value.gamma} to $newGamma" }
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
                    if (it.settingsType == SettingsType.DITHERING) {
                        it.copy(
                            previewPixelData = null,
                            settingsType = null,
                            imageVersion = it.imageVersion + 1,
                        )
                    } else {
                        it.copy(
                            settingsType = null,
                        )
                    }
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
