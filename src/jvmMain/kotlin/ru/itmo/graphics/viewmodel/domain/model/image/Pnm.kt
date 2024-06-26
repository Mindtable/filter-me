package ru.itmo.graphics.viewmodel.domain.model.image

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ImageInfo
import ru.itmo.graphics.viewmodel.domain.ImageDimension
import ru.itmo.graphics.viewmodel.domain.model.ImageType
import ru.itmo.graphics.viewmodel.tools.InputStreamUtils
import java.io.InputStream
import java.io.OutputStream

abstract class Pnm : ImageType {
    protected var maxPixelValue: Int = 255
    abstract val pnmType: ByteArray

    protected fun normaliseDataBlock(inputStream: InputStream): Float {
        var value = inputStream.read()

        value = value.coerceIn(0..maxPixelValue)

        return value.toFloat() / maxPixelValue
    }

    abstract fun writePixelInfo(outputStream: OutputStream, pixelIndex: Int, byteArray: ByteArray)

    override fun readHeader(inputStream: InputStream): ImageDimension {
        // Skip PNM type
        inputStream.readNBytes(2)
        InputStreamUtils.readWhitespace(inputStream)

        // Image params
        val width = InputStreamUtils.readPositiveNumber(inputStream)
        val height = InputStreamUtils.readPositiveNumber(inputStream)
        maxPixelValue = InputStreamUtils.readPositiveNumber(inputStream)

        if (maxPixelValue > 255) {
            throw Exception("MaxValue for pixel can't be more than 255. Found $maxPixelValue")
        }

        return ImageDimension(width, height)
    }

    override fun writeFile(outputStream: OutputStream, bitmap: Bitmap) {
        outputStream.write(pnmType)
        outputStream.write('\n'.code)

        outputStream.write(bitmap.width.toString().toByteArray())
        outputStream.write(' '.code)
        outputStream.write(bitmap.height.toString().toByteArray())
        outputStream.write('\n'.code)
        outputStream.write(maxPixelValue.toString().toByteArray())
        outputStream.write('\n'.code)

        val byteArray = bitmap.readPixels(dstInfo = ImageInfo(colorInfo, bitmap.width, bitmap.height))
            ?: throw NullPointerException("Failed to convert pixels to $colorInfo")

        for (i in 0 until bitmap.width * bitmap.height) {
            writePixelInfo(outputStream, i, byteArray)
        }
    }
}
