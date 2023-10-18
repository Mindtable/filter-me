package ru.itmo.graphics.model.image

import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import ru.itmo.graphics.tools.HalfFloatUtils
import ru.itmo.graphics.viewmodel.domain.Pixel
import java.io.InputStream
import java.io.OutputStream

class PnmP5 : Pnm() {
    override val pnmType: ByteArray = byteArrayOf(80, 53)
    override val isSupported: Boolean = true
    override val colorInfo: ColorInfo
        get() {
            return if (maxPixelValue < 256) {
                ColorInfo(ColorType.GRAY_8, ColorAlphaType.OPAQUE, ColorSpace.sRGB)
            } else {
                ColorInfo(ColorType.RGBA_F16NORM, ColorAlphaType.OPAQUE, ColorSpace.sRGB)
            }
        }

    override fun readPixelInfo(inputStream: InputStream, pixelIndex: Int, byteArray: ByteArray): Pixel {
        val normalized = normaliseDataBlock(inputStream)
        if (maxPixelValue < 256) {
            byteArray[pixelIndex] = (normalized * 255).toInt().toByte()
        } else {
            val color = HalfFloatUtils.toHalfFloat(normalized)

            for (i in pixelIndex * 8 until pixelIndex * 8 + 6 step 2) {
                byteArray[i] = color.toByte()
                byteArray[i + 1] = color.shr(8).toByte()
            }

            val alphaValue = 15360 // 1.0f in half float
            byteArray[pixelIndex * 8 + 6] = alphaValue.toByte()
            byteArray[pixelIndex * 8 + 7] = alphaValue.shr(8).toByte()
        }

        return Pixel(normalized, normalized, normalized)
    }

    override fun writePixelInfo(outputStream: OutputStream, pixelIndex: Int, byteArray: ByteArray) {
        if (maxPixelValue < 256) {
            outputStream.write(byteArray[pixelIndex].toInt())
        } else {
            val intBits = byteArray[pixelIndex * 8 + 1].toInt().shl(8) + byteArray[pixelIndex * 8]
            val color = (HalfFloatUtils.fromHalfFloat(intBits) * maxPixelValue).toInt().coerceIn(0..maxPixelValue)

            outputStream.write(color.shr(8))
            outputStream.write(color)
        }
    }
}
