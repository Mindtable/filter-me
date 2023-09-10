import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.LocalWindowExceptionHandlerFactory
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowExceptionHandlerFactory
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ImageInfo
import ru.itmo.graphics.fetch.fetchImageModelUseCase
import ru.itmo.graphics.image.type.FileTypeResolver
import ru.itmo.graphics.image.type.P5TypeResolver
import ru.itmo.graphics.image.type.P6TypeResolver
import ru.itmo.graphics.image.type.SkiaSupportedTypeResolver
import ru.itmo.graphics.model.ApplicationState
import ru.itmo.graphics.model.ImageModel
import ru.itmo.graphics.view.MenuBarView
import java.awt.Dimension
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.lang.Float.min
import kotlin.system.measureTimeMillis

fun readImage(imageModel: ImageModel): ImageBitmap {
    val (file, byteArray, imageType) = imageModel

    if (!imageType.isSupported) {
        throw IllegalStateException("Image type not supported for image read function")
    }

    lateinit var pixelMap: ByteArray
    val width: Int
    val height: Int

    val timeInMillis = measureTimeMillis {
        println(file.absolutePath)

        val fileStream = byteArray.inputStream()

        val imageDimension = imageType.readHeader(fileStream)

        width = imageDimension.width
        height = imageDimension.height
        val totalPixels = width * height

        println("Picture is $width pixels by $height pixels (Total $totalPixels pixels)")

        val totalLen = totalPixels * imageType.bytesPerPixel

        pixelMap = ByteArray(totalLen) { 0 }

        for (i in 0 until totalPixels) {
            imageType.readPixelInfo(fileStream, i, pixelMap)
        }

        val totalMemory = totalLen.toFloat() * Byte.SIZE_BYTES
        println("Successfully read $totalLen color values. Total memory used: ${totalMemory / 1000 / 1000} Mb")

        fileStream.close()
    }

    println("Total time used to load: ${timeInMillis.toFloat() / 1000} s")

    val bitmap = Bitmap()
    bitmap.setImageInfo(
        ImageInfo(
            imageType.colorInfo,
            width,
            height,
        ),
    )
    bitmap.installPixels(pixelMap)

    return bitmap.asComposeImageBitmap()
}

fun chooseFile(parent: Frame): File = FileDialog(parent, "Select File", FileDialog.LOAD)
    .apply {
        isMultipleMode = false
        isVisible = true
    }.files.first()

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    var lastError: Throwable? by mutableStateOf(null)

    application {
        val applicationState = remember { ApplicationState() }
        CompositionLocalProvider(
            LocalWindowExceptionHandlerFactory provides WindowExceptionHandlerFactory { window ->
                WindowExceptionHandler {
                    lastError = it
                    applicationState.rollbackOnError("${it.message}}")
                }
            },
        ) {
            val logger by lazy {
                KotlinLogging.logger { }
            }
            val typeResolver by lazy {
                FileTypeResolver(
                    listOf(
                        P5TypeResolver(),
                        P6TypeResolver(),
                        SkiaSupportedTypeResolver(),
                    ),
                )
            }
            val image by remember(applicationState.currentImageFileName) {
                mutableStateOf(
                    applicationState.currentImageFileName?.let {
                        fetchImageModelUseCase(it, typeResolver)
                    },
                )
            }
            val bitmap by remember(image) {
                mutableStateOf(
                    image?.let {
                        when (it.type.isSupported) {
                            true -> readImage(it)
                            else -> loadImageBitmap(it.data.inputStream())
                        }
                    } ?: loadDefaultImage(),
                )
            }
            Window(
                onCloseRequest = ::exitApplication,
                title = "Nascar95 GUI",
                state = rememberWindowState(width = Dp.Unspecified, height = Dp.Unspecified),
            ) {
                setMinWindowSize()
                MenuBarView(applicationState)

                Scaffold(
                    topBar = {
                        Canvas(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            val scalingCoefficient = min(size.height / bitmap.height, size.width / bitmap.width)
                            scale(
                                scaleX = scalingCoefficient,
                                scaleY = scalingCoefficient,
                                pivot = Offset.Zero,
                            ) {
                                drawIntoCanvas {
                                    logger.info { "Canvas redrawn" }
                                    it.withSave {
                                        it.drawImage(bitmap, Offset.Zero, Paint())
                                    }
                                }
                            }
                        }
                    },
                    bottomBar = {
                        BottomAppBar(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                textAlign = TextAlign.Left,
                                text = applicationState.log ?: "",
                            )
                        }
                    },
                ) {
                    logger.info { "Scaffold redrawn" }
                }
            }
        }
    }
}

private fun loadDefaultImage() = with(KotlinLogging.logger { }) {
    useResource("sample.png", ::loadImageBitmap)
        .also { info { "used default file" } }
}

@Composable
fun Dp.dpRoundToPx() = with(LocalDensity.current) { this@dpRoundToPx.roundToPx() }

@Composable
private fun FrameWindowScope.setMinWindowSize() {
    window.minimumSize = Dimension(100.dp.dpRoundToPx(), 100.dp.dpRoundToPx())
}
