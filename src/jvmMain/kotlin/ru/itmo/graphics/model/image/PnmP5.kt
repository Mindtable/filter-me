package ru.itmo.graphics.model.image

import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import java.io.InputStream

class PnmP5 : Pnm() {
    override val isSupported: Boolean = true
    override val colorInfo: ColorInfo = ColorInfo(ColorType.GRAY_8, ColorAlphaType.OPAQUE, ColorSpace.sRGB)

    override fun readPixelInfo(inputStream: InputStream, pixelIndex: Int, byteArray: ByteArray) {
        if (maxPixelValue < 256) {
            byteArray[pixelIndex] = (normaliseDataBlock(inputStream) * 255).toInt().toByte()
        } else {
            throw NotImplementedError("16 bit greyscale is not supported")
        }
    }
}