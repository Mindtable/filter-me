package ru.itmo.graphics.viewmodel.domain.model.image

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import ru.itmo.graphics.viewmodel.domain.ImageDimension
import ru.itmo.graphics.viewmodel.domain.model.ImageType
import java.io.InputStream
import java.io.OutputStream

object Png : ImageType {

    override val isSupported: Boolean
        get() = false
    override val colorInfo: ColorInfo
        get() = ColorInfo(ColorType.RGB_888X, ColorAlphaType.OPAQUE, ColorSpace.sRGB)

    override fun readHeader(inputStream: InputStream): ImageDimension {
        TODO("Not yet implemented")
    }

    override fun readPixelInfo(inputStream: InputStream, pixelIndex: Int, bb: MutableList<Float>) {
        TODO("Not yet implemented")
    }

    override fun writeFile(outputStream: OutputStream, bitmap: Bitmap) {
        TODO("Not yet implemented")
    }
}
