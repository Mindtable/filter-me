package ru.itmo.graphics.model.image

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorInfo
import ru.itmo.graphics.model.ImageDimension
import ru.itmo.graphics.model.ImageType
import java.io.InputStream
import java.io.OutputStream

class SkiaSupported : ImageType {
    override val isSupported: Boolean = false
    override val colorInfo: ColorInfo
        get() = TODO("Not yet implemented")

    override fun readHeader(inputStream: InputStream): ImageDimension {
        TODO("Not yet implemented")
    }

    override fun readPixelInfo(inputStream: InputStream, pixelIndex: Int, byteArray: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun writeFile(outputStream: OutputStream, bitmap: Bitmap) {
        TODO("Not yet implemented")
    }
}