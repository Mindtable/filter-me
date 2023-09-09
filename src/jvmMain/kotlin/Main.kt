import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.FileDialog
import java.awt.Frame
import java.io.ByteArrayInputStream
import java.io.File
import kotlin.system.measureTimeMillis

enum class ImageType {
    P5, P6
}

fun isWhitespace(c: Int) : Boolean {
    return c == 8 || c == 10 || c == 13 || c == 32
}

fun readWhitespace(fileStream: ByteArrayInputStream) : Int {
    val c = fileStream.read()

    if (!isWhitespace(c))
    {
        throw Exception("Expected whitespace character, encountered: $c")
    }

    return c
}

fun readNumber(fileStream: ByteArrayInputStream) : Int {
    var number = 0
    var c = fileStream.read()
    while (c.toChar().minus('0') in 0..9) {
        number = number * 10 + c.toChar().minus('0')
        c = fileStream.read()
    }

    if (!isWhitespace(c))
    {
        throw Exception("Expected digit character, encountered: ${c.toChar()} ($c)")
    }

    return number
}

fun readPositiveNumber(fileStream: ByteArrayInputStream) : Int {
    val number = readNumber(fileStream)

    if (number <= 0)
    {
        throw Exception("Number must be positive. Got $number")
    }

    return number
}

fun readColorValue(fileStream: ByteArrayInputStream, maxValue: Int) : Float {
    var value = fileStream.read()
    if (maxValue > 255)
    {
        value.shl(8)
        value += fileStream.read()
    }

    return value.toFloat() / maxValue
}

fun openFileDialog(parent : Frame): File {
    val file = FileDialog(parent, "Select File", FileDialog.LOAD).apply {
        isMultipleMode = false
        isVisible = true
    }.files.first()

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

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose for Desktop",
        state = rememberWindowState(width = 480.dp, height = 480.dp)
    ) {
        MaterialTheme {
            Button(onClick = {
                openFileDialog(this.window)
            }) {
                Text("File Picker")
            }
        }
    }
}
