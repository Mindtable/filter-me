package ru.itmo.graphics.model.image

import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import java.io.InputStream
import java.io.OutputStream

class PnmP6 : Pnm() {
    override val pnmType: ByteArray = byteArrayOf(80, 54)
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

    override fun writePixelInfo(outputStream: OutputStream, pixelIndex: Int, byteArray: ByteArray) {
        if (maxPixelValue < 256) {
            outputStream.write(byteArray[pixelIndex * 4 + 0].toInt())
            outputStream.write(byteArray[pixelIndex * 4 + 1].toInt())
            outputStream.write(byteArray[pixelIndex * 4 + 2].toInt())
            // Alpha is not making it in file
        } else {
            throw NotImplementedError("16 bit RGB is not supported")
        }
    }
}