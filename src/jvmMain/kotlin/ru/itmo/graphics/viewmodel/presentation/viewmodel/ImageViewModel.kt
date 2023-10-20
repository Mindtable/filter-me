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
import ru.itmo.graphics.image.type.FileTypeResolver
import ru.itmo.graphics.model.ImageModel
import ru.itmo.graphics.model.image.PnmP5
import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.presentation.viewmodel.FileDialogType.NONE
import ru.itmo.graphics.viewmodel.presentation.viewmodel.FileDialogType.OPEN
import ru.itmo.graphics.viewmodel.presentation.viewmodel.FileDialogType.SAVE
import ru.itmo.graphics.viewmodel.tools.convertColorSpace
import ru.itmo.graphics.viewmodel.tools.readImageV2
import ru.itmo.graphics.viewmodel.tools.toBitmap
import java.io.File

private val logger = KotlinLogging.logger { }

class ImageViewModel(
    private val scope: CoroutineScope,
    private val fileTypeResolver: FileTypeResolver,
) {
    val state = MutableStateFlow(ImageState())

    fun onEvent(event: ImageEvent) {
        when (event) {
            is SaveAsEvent -> {
                val imageState = state.value
                logger.info { "onSavedAsButtonClick call with ${imageState.file} parameter" }

                val imageModel = imageState.imageModel ?: throw IllegalStateException("Image model cannot be null!")
                val bitmapToSave = imageState.pixelData!!.toBitmap(
                    state.value.colorSpace,
                    state.value.channel,
                    true,
                )

                scope.launch {
                    if (state.value.channel == Channel.ALL) {
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
                )

                scope.launch {
                    if (state.value.channel == Channel.ALL) {
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

            is ApplicationColorSpaceChanged -> {
                scope.launch(SupervisorJob() + coroutineExceptionHandler()) {
                    val newColorSpace = event.colorSpace
                    if (state.value.colorSpace != newColorSpace) {
                        state.update {
                            it.copy(
                                log = "Colorspace changed to ${newColorSpace.name}",
                                pixelData = it.pixelData?.convertColorSpace(state.value.colorSpace, newColorSpace),
                                colorSpace = newColorSpace,
                            )
                        }
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
                            readImageV2(
                                imageModel,
                            )
                            // .convertColorSpace(RgbColorSpace, state.value.colorSpace)
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
        }
    }
}

fun ImageViewModel.coroutineExceptionHandler() = CoroutineExceptionHandler { _, exception: Throwable ->
    exception.printStackTrace()
    onEvent(ImageError(exception))
}
