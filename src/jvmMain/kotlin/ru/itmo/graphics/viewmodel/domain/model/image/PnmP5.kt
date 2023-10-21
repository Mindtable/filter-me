package ru.itmo.graphics.viewmodel.domain.model.image

import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import java.io.InputStream
import java.io.OutputStream

object PnmP5 : Pnm() {
    override val pnmType: ByteArray = byteArrayOf(80, 53)
    override val isSupported: Boolean = true
    override val colorInfo: ColorInfo
        get() {
            return ColorInfo(ColorType.RGB_888X, ColorAlphaType.OPAQUE, ColorSpace.sRGB)
        }

    override fun readPixelInfo(inputStream: InputStream, pixelIndex: Int, bb: MutableList<Float>) {
        val normalized = normaliseDataBlock(inputStream)

        for (i in 0..2) {
            bb[i] = normalized
        }
    }

    override fun writePixelInfo(outputStream: OutputStream, pixelIndex: Int, byteArray: ByteArray) {
        outputStream.write(byteArray[pixelIndex * 4].toInt())
    }
}
