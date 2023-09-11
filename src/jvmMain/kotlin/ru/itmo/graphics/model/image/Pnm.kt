package ru.itmo.graphics.model.image

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ImageInfo
import ru.itmo.graphics.model.ImageDimension
import ru.itmo.graphics.model.ImageType
import ru.itmo.graphics.tools.InputStreamUtils
import java.io.InputStream
import java.io.OutputStream

abstract class Pnm : ImageType {
    protected var maxPixelValue: Int = 0
    abstract val pnmType: ByteArray


    protected fun normaliseDataBlock(inputStream: InputStream): Float {
        var value = inputStream.read()

        if (maxPixelValue > 255) {
            value = value.shl(8)
            value += inputStream.read()
        }

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

        if (maxPixelValue > 65536) {
            throw Exception("MaxValue for pixel can't be more than 65536. Found $maxPixelValue")
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