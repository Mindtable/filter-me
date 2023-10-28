package ru.itmo.graphics.viewmodel.tools

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType.OPAQUE
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType.RGB_888X
import org.jetbrains.skia.ImageInfo
import ru.itmo.graphics.viewmodel.domain.ImageModel
import ru.itmo.graphics.viewmodel.domain.Pixel
import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.domain.asBb
import ru.itmo.graphics.viewmodel.domain.asPixel
import ru.itmo.graphics.viewmodel.domain.image.colorspace.ApplicationColorSpace
import ru.itmo.graphics.viewmodel.domain.image.gamma.GammaConversion
import ru.itmo.graphics.viewmodel.presentation.view.main.ImageChannel
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger { }

fun readImageV2(
    imageModel: ImageModel,
): PixelData {
    val (file, byteArray, imageType) = imageModel

    if (!imageType.isSupported) {
        throw IllegalStateException("Image type not supported for image read function")
    }

    val width: Int
    val height: Int

    val pixelData: PixelData

    val timeInMillis = measureTimeMillis {
        println(file.absolutePath)

        val fileStream = byteArray.inputStream()

        val imageDimension = imageType.readHeader(fileStream)

        width = imageDimension.width
        height = imageDimension.height

        pixelData = PixelData(MutableList(height * width * 3) { 0f }, height, width)
        val totalPixels = width * height

        println("Picture is $width pixels by $height pixels (Total $totalPixels pixels)")

        val totalLen = totalPixels * 3

        for (i in 0..<height) {
            for (j in 0..<width) {
                imageType.readPixelInfo(fileStream, i, pixelData.getPixel(i, j))
            }
        }

        val totalMemory = totalLen.toFloat() * Float.SIZE_BYTES
        println("Successfully read $totalLen color values. Total memory used: ${totalMemory / 1000 / 1000} Mb")

        fileStream.close()
    }

    log.info { "Time used to read $timeInMillis ms" }

    return pixelData
}

fun PixelData.toBitmap(
    colorSpace: ApplicationColorSpace,
    channel: ImageChannel,
    isMonochromeMode: Boolean,
    gamma: Float,
): Bitmap {
    val byteArray = ByteArray(pixelCount * 4)

    val bb: MutableList<Float> = MutableList(3) { 0f }
    var pixel: MutableList<Float>

    val timeInMillis = measureTimeMillis {
        for (i in 0..<height) {
            for (j in 0..<width) {
                pixel = getPixel(i, j)
                bb[0] = pixel[0]
                bb[1] = pixel[1]
                bb[2] = pixel[2]

                if (isMonochromeMode) {
                    if (channel == ImageChannel.CHANNEL_ONE) {
                        bb[1] = bb[0]
                        bb[2] = bb[0]
                    } else if (channel == ImageChannel.CHANNEL_TWO) {
                        bb[0] = bb[1]
                        bb[2] = bb[1]
                    } else if (channel == ImageChannel.CHANNEL_THREE) {
                        bb[0] = bb[2]
                        bb[1] = bb[2]
                    }
                } else {
                    if (channel != ImageChannel.ALL) {
                        colorSpace.separateChannel(bb, channel)
                    }

                    colorSpace.toRgb(bb)
                }

                GammaConversion.applyGamma(bb, gamma)

                val channelOne = bb[0]
                val channelTwo = bb[1]
                val channelThree = bb[2]

                byteArray[(i * width + j) * 4 + 0] = transformPixelToByte(channelOne)
                byteArray[(i * width + j) * 4 + 1] = transformPixelToByte(channelTwo)
                byteArray[(i * width + j) * 4 + 2] = transformPixelToByte(channelThree)
                byteArray[(i * width + j) * 4 + 3] = transformPixelToByte(1.0f)
            }
        }
    }

    log.info { "Time spent to draw $timeInMillis" }

    val bitmap = Bitmap()
    bitmap.setImageInfo(
        ImageInfo(
            ColorInfo(RGB_888X, OPAQUE, ColorSpace.sRGB),
            width,
            height,
        ),
    )
    bitmap.installPixels(byteArray)

    return bitmap
}

fun PixelData.convertColorSpace(
    oldColorSpace: ApplicationColorSpace,
    newColorSpace: ApplicationColorSpace,
): PixelData {
    var pixel: MutableList<Float>

    for (i in 0..<height) {
        for (j in 0..<width) {
            pixel = getPixel(i, j)
            oldColorSpace.toRgb(pixel)
            newColorSpace.fromRgb(pixel)
        }
    }

    return this
}

fun Pixel.convertColorSpace(
    oldColorSpace: ApplicationColorSpace,
    newColorSpace: ApplicationColorSpace,
): Pixel {
    val asBb = this.asBb()
    oldColorSpace.toRgb(asBb)
    newColorSpace.fromRgb(asBb)

    return asBb.asPixel()
}

fun PixelData.convertGamma(
    oldGamma: Float,
    newGamma: Float,
): PixelData {
    var pixel: MutableList<Float>

    for (i in 0..<height) {
        for (j in 0..<width) {
            pixel = getPixel(i, j)
            GammaConversion.applyGamma(pixel, oldGamma)
            if (newGamma == 0f) {
                GammaConversion.applyGamma(pixel, 1 / 2.4f)
            } else {
                GammaConversion.applyGamma(pixel, 1 / newGamma)
            }
        }
    }

    return this
}

fun transformPixelToByte(x: Float) = transformPixelToInt(x).toByte()
fun transformPixelToInt(x: Float) = ((x * 255).roundToEven() + 256) % 256

fun Float.roundToEven(): Int {
    val rounded = this.toInt()

    return if (rounded + 1 - this < this - rounded) {
        rounded + 1
    } else {
        rounded
    }
}
