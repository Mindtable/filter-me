package ru.itmo.graphics.model.image

import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import ru.itmo.graphics.tools.HalfFloatUtils
import java.io.InputStream
import java.io.OutputStream

class PnmP6 : Pnm() {
    override val pnmType: ByteArray = byteArrayOf(80, 54)
    override val isSupported: Boolean = true
    override val colorInfo: ColorInfo
        get() {
            return if (maxPixelValue < 256) {
                ColorInfo(ColorType.RGB_888X, ColorAlphaType.OPAQUE, ColorSpace.sRGB)
            } else {
                ColorInfo(ColorType.RGBA_F16NORM, ColorAlphaType.OPAQUE, ColorSpace.sRGB)
            }
        }

    override fun readPixelInfo(inputStream: InputStream, pixelIndex: Int, byteArray: ByteArray) {
        if (maxPixelValue < 256) {

            for (i in pixelIndex * 4 until pixelIndex * 4 + 3) {
                byteArray[i] = (normaliseDataBlock(inputStream) * 255).toInt().toByte()
            }

            byteArray[pixelIndex * 4 + 3] = (255).toByte()
        } else {

            for (i in pixelIndex * 8 until pixelIndex * 8 + 6 step 2) {
                val color = HalfFloatUtils.toHalfFloat(normaliseDataBlock(inputStream))
                byteArray[i] = color.toByte()
                byteArray[i + 1] = color.shr(8).toByte()
            }

            val alphaValue = 15360 // 1.0f in half float
            byteArray[pixelIndex * 8 + 6] = alphaValue.toByte()
            byteArray[pixelIndex * 8 + 7] = alphaValue.shr(8).toByte()
        }
    }

    override fun writePixelInfo(outputStream: OutputStream, pixelIndex: Int, byteArray: ByteArray) {
        if (maxPixelValue < 256) {
            for (i in pixelIndex * 4 until pixelIndex * 4 + 3) {
                outputStream.write(byteArray[i].toInt())
            }
        } else {
            for (i in pixelIndex * 8  until  pixelIndex * 8 + 6 step 2) {
                val intBits = byteArray[i + 1].toInt().shl(8) + byteArray[i]
                val color = (HalfFloatUtils.fromHalfFloat(intBits) * maxPixelValue).toInt().coerceIn(0..maxPixelValue)

                outputStream.write(color.shr(8))
                outputStream.write(color)
            }
        }
    }
}