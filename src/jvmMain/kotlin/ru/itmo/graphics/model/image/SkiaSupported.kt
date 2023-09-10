package ru.itmo.graphics.model.image

import org.jetbrains.skia.ColorInfo
import ru.itmo.graphics.model.ImageDimension
import ru.itmo.graphics.model.ImageType
import java.io.InputStream

class SkiaSupported : ImageType {
    override val isSupported: Boolean = false
    override val colorInfo: ColorInfo
        get() = TODO("Not yet implemented")
    override val bytesPerPixel: Int
        get() = TODO("Not yet implemented")

    override fun readHeader(inputStream: InputStream): ImageDimension {
        TODO("Not yet implemented")
    }

    override fun readPixelInfo(inputStream: InputStream, pixelIndex: Int, byteArray: ByteArray) {
        TODO("Not yet implemented")
    }
}