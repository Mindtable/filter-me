package ru.itmo.graphics.model.image

import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import java.io.InputStream

class PnmP6 : Pnm() {
    override val isSupported: Boolean = true
    override val colorInfo: ColorInfo = ColorInfo(ColorType.RGB_888X, ColorAlphaType.OPAQUE, ColorSpace.sRGB)

    override fun readPixelInfo(inputStream: InputStream, pixelIndex: Int, byteArray: ByteArray) {
        if (maxPixelValue < 256) {
            byteArray[pixelIndex * 4 + 0] = (normaliseDataBlock(inputStream) * 255).toInt().toByte()
            byteArray[pixelIndex * 4 + 1] = (normaliseDataBlock(inputStream) * 255).toInt().toByte()
            byteArray[pixelIndex * 4 + 2] = (normaliseDataBlock(inputStream) * 255).toInt().toByte()
            byteArray[pixelIndex * 4 + 3] = (255).toByte()
        } else {
            throw NotImplementedError("16 bit RGB is not supported")
        }
    }
}