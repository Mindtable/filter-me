package ru.itmo.graphics.viewmodel.domain.model.image

import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import java.io.InputStream
import java.io.OutputStream

object PnmP6 : Pnm() {
    override val pnmType: ByteArray = byteArrayOf(80, 54)
    override val isSupported: Boolean = true
    override val colorInfo: ColorInfo
        get() {
            return ColorInfo(ColorType.RGB_888X, ColorAlphaType.OPAQUE, ColorSpace.sRGB)
        }

    override fun readPixelInfo(inputStream: InputStream, pixelIndex: Int, bb: MutableList<Float>) {
        for (i in 0..2) {
            val normalized = normaliseDataBlock(inputStream)
            bb[i] = normalized
        }
    }

    override fun writePixelInfo(outputStream: OutputStream, pixelIndex: Int, byteArray: ByteArray) {
        for (i in pixelIndex * 4 until pixelIndex * 4 + 3) {
            outputStream.write(byteArray[i].toInt())
        }
    }
}
