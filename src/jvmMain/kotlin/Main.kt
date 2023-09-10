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
import java.awt.FileDialog
import java.awt.Frame
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.lang.Float.min
import kotlin.system.measureTimeMillis

enum class Actions(private val actionString: String) {
    OPEN("Open"),
    SAVE("Save"),
    SAVEAS("Save as"),
    ;

    override fun toString(): String = actionString
}

enum class ImageType {
    P5, P6
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

fun openFileDialog(parent: Frame): File {
    val file = chooseFile(parent)

    var imageType: ImageType
    lateinit var pixelMap: FloatArray

    val timeInMillis = measureTimeMillis {
        println(file.absolutePath)

        val byteArray = file.readBytes()
        val fileStream = byteArray.inputStream()
        val type = fileStream.readNBytes(2)

        imageType = if (type[0].toInt() == 80 && type[1].toInt() == 53) {
            ImageType.P5
        } else if (type[0].toInt() == 80 && type[1].toInt() == 54) {
            ImageType.P6
        } else {
            throw Exception("Incorrect type header")
        }

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
    return file
}

fun chooseFile(parent: Frame): File = FileDialog(parent, "Select File", FileDialog.LOAD)
    .apply {
        isMultipleMode = false
        isVisible = true
    }.files.first()

fun main() = application {
    var logs by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf<String?>(null) }
    val bitmap by remember(fileName) {
        mutableStateOf(
            loadBitmapFromDisk(fileName),
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

private fun loadBitmapFromDisk(
    fileName: String?,
) = with(KotlinLogging.logger { }) {
    fileName.also { info { "Start loading file $fileName" } }
        ?.let {
            FileInputStream(it)
        }
        ?.use {
            loadImageBitmap(it)
        }
        ?: useResource("sample.png", ::loadImageBitmap)
            .also { info { "used default file" } }
}
