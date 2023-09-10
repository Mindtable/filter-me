import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.image.type.FileTypeResolver
import ru.itmo.graphics.image.type.P5TypeResolver
import ru.itmo.graphics.image.type.P6TypeResolver
import ru.itmo.graphics.image.type.SkiaSupportedTypeResolver
import ru.itmo.graphics.model.ImageModel
import ru.itmo.graphics.model.ImageType
import java.awt.FileDialog
import java.awt.Frame
import java.io.ByteArrayInputStream
import java.io.File
import java.lang.Float.min
import kotlin.system.measureTimeMillis

enum class Actions(private val actionString: String) {
    OPEN("Open"),
    SAVE("Save"),
    SAVEAS("Save as"),
    ;

    override fun toString(): String = actionString
}

fun isWhitespace(c: Int): Boolean {
    return c == 8 || c == 10 || c == 13 || c == 32
}

fun readWhitespace(fileStream: ByteArrayInputStream): Int {
    val c = fileStream.read()

    if (!isWhitespace(c)) {
        throw Exception("Expected whitespace character, encountered: $c")
    }

    return c
}

fun readNumber(fileStream: ByteArrayInputStream): Int {
    var number = 0
    var c = fileStream.read()
    while (c.toChar().minus('0') in 0..9) {
        number = number * 10 + c.toChar().minus('0')
        c = fileStream.read()
    }

    if (!isWhitespace(c)) {
        throw Exception("Expected digit character, encountered: ${c.toChar()} ($c)")
    }

    return number
}

fun readPositiveNumber(fileStream: ByteArrayInputStream): Int {
    val number = readNumber(fileStream)

    if (number <= 0) {
        throw Exception("Number must be positive. Got $number")
    }

    return number
}

fun readColorValue(fileStream: ByteArrayInputStream, maxValue: Int): Float {
    var value = fileStream.read()
    if (maxValue > 255) {
        value.shl(8)
        value += fileStream.read()
    }

    return value.toFloat() / maxValue
}

fun readPnm(imageModel: ImageModel): ImageBitmap {
    val (file, byteArray, imageType) = imageModel

    if (imageType !in setOf(ImageType.P5, ImageType.P6)) {
        throw IllegalStateException("Image type not supported for pnm read function")
    }

    lateinit var pixelMap: FloatArray

    val timeInMillis = measureTimeMillis {
        println(file.absolutePath)

        val fileStream = byteArray.inputStream()
        fileStream.readNBytes(2)

        println("Found ${imageType.name} type header")

        readWhitespace(fileStream)

        val width = readPositiveNumber(fileStream)
        val height = readPositiveNumber(fileStream)

        println("Picture is $width pixels by $height pixels (Total ${width * height} pixels)")

        val maxPixelValue = readPositiveNumber(fileStream)

        if (maxPixelValue > 65536) {
            throw Exception("MaxValue for pixel can't be more than 65536. Found $maxPixelValue")
        }

        println("MaxValue for pixel is $maxPixelValue")
        var totalLen = height * width
        if (imageType == ImageType.P6) {
            totalLen *= 3
        }

        pixelMap = FloatArray(totalLen) { 0f }

        for (i in pixelMap.indices) {
            pixelMap[i] = readColorValue(fileStream, maxPixelValue)
        }

        val totalMemory = totalLen.toFloat() * Int.SIZE_BYTES
        println("Successfully read $totalLen color values. Total memory used: ${totalMemory / 1000 / 1000} Mb")

        fileStream.close()
    }

    println("Total time used to load: ${timeInMillis.toFloat() / 1000} s")
    // TODO: issue #3
    return ImageBitmap(width = 0, height = 0)
}

fun chooseFile(parent: Frame): File = FileDialog(parent, "Select File", FileDialog.LOAD)
    .apply {
        isMultipleMode = false
        isVisible = true
    }.files.first()

fun main() = application {
    val typeResolver by lazy {
        FileTypeResolver(
            listOf(
                P5TypeResolver(),
                P6TypeResolver(),
                SkiaSupportedTypeResolver(),
            ),
        )
    }
    var logs by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf<String?>(null) }
    val image by remember(fileName) {
        mutableStateOf(
            fileName?.runCatching {
                File(this)
            }?.mapCatching {
                val data = it.readBytes()

                val type = typeResolver.resolveType(it, data)

                ImageModel(
                    file = it,
                    data = data,
                    type = type,
                )
            }?.getOrNull(),
        )
    }
    val bitmap by remember(image) {
        mutableStateOf(
            image?.let {
                when (it.type) {
                    ImageType.P5, ImageType.P6 -> readPnm(it)
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
        MenuBar {
            Menu(
                text = "File",
                mnemonic = 'F',
            ) {
                Item(
                    Actions.OPEN.toString(),
                    onClick = {
                        fileName = chooseFile(window).absolutePath
                        logs = "Meta-info.\nFileName: '$fileName'"
                    },
                    shortcut = KeyShortcut(Key.O, ctrl = true),
                )
                Item(
                    Actions.SAVE.toString(),
                    onClick = { logs = "saved" },
                    shortcut = KeyShortcut(Key.S, ctrl = true),
                )
                Item(
                    Actions.SAVEAS.toString(),
                    onClick = { logs = "saved as" },
                    shortcut = KeyShortcut(Key.S, ctrl = true, shift = true),
                )
            }
        }

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
                        drawIntoCanvas { canvas ->
                            canvas.withSave {
                                canvas.drawImage(bitmap, Offset.Zero, Paint())
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
                        text = logs,
                    )
                }
            },
        ) {}
    }
}

private fun loadDefaultImage() = with(KotlinLogging.logger { }) {
    useResource("sample.png", ::loadImageBitmap)
        .also { info { "used default file" } }
}
