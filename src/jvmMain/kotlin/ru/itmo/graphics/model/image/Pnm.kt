package ru.itmo.graphics.model.image

import ru.itmo.graphics.model.ImageDimension
import ru.itmo.graphics.model.ImageType
import ru.itmo.graphics.tools.InputStreamUtils
import java.io.InputStream

abstract class Pnm : ImageType {
    protected var maxPixelValue: Int = 0

    override val bytesPerPixel: Int
        get() {
            return if (maxPixelValue > 255) {
                colorInfo.bytesPerPixel * 2
            } else {
                colorInfo.bytesPerPixel
            }
        }

    protected fun normaliseDataBlock(inputStream: InputStream): Float {
        var value = inputStream.read()

        if (maxPixelValue > 255) {
            value.shl(8)
            value += inputStream.read()
        }

        return value.toFloat() / maxPixelValue
    }

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
}