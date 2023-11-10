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
import kotlin.math.roundToInt
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
    val byteArray: ByteArray

    val timeInMillis = measureTimeMillis {
        byteArray = asByteArray(isMonochromeMode, channel, colorSpace, gamma)
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

fun PixelData.asByteArray(
    isMonochromeMode: Boolean,
    channel: ImageChannel,
    colorSpace: ApplicationColorSpace,
    gamma: Float,
): ByteArray {
    val byteArray = ByteArray(pixelCount * 4)

    val bb: MutableList<Float> = MutableList(3) { 0f }
    var pixel: MutableList<Float>

    for (i in 0..<height) {
        for (j in 0..<width) {
            pixel = getPixel(i, j)
            bb[0] = pixel[0]
            bb[1] = pixel[1]
            bb[2] = pixel[2]

            applyImageSettings(bb, isMonochromeMode, channel, colorSpace, gamma)

            val channelOne = bb[0]
            val channelTwo = bb[1]
            val channelThree = bb[2]

            byteArray[(i * width + j) * 4 + 0] = transformPixelToByte(channelOne)
            byteArray[(i * width + j) * 4 + 1] = transformPixelToByte(channelTwo)
            byteArray[(i * width + j) * 4 + 2] = transformPixelToByte(channelThree)
            byteArray[(i * width + j) * 4 + 3] = transformPixelToByte(1.0f)
        }
    }
    return byteArray
}

fun applyImageSettings(
    bb: MutableList<Float>,
    isMonochromeMode: Boolean,
    channel: ImageChannel,
    colorSpace: ApplicationColorSpace,
    gamma: Float,
) {
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
//            GammaConversion.applyGamma(pixel, oldGamma)
//            if (newGamma == 0f) {
//                GammaConversion.applyGamma(pixel, 1 / 2.4f)
//            } else {
//                GammaConversion.applyGamma(pixel, 1 / newGamma)
//            }

            if (newGamma == 0f) {
                if (oldGamma != 0f) {
                    GammaConversion.applyGamma(pixel, oldGamma / 2.4f)
                }
            } else {
                if (oldGamma != 0f) {
                    GammaConversion.applyGamma(pixel, oldGamma / newGamma)
                } else {
                    GammaConversion.applyGamma(pixel, 2.4f / newGamma)
                }
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

fun createGradient(height: Int = 400, width: Int = 600): PixelData {
    val step = 1.0f / width
    val pixelData = PixelData(MutableList(height * width * 3) { 0f }, height, width)

    for (i in 0..<height) {
        for (j in 0..<width) {
            val pixel = pixelData.getPixel(i, j)

            val color = step * j

            pixel[0] = color
            pixel[1] = color
            pixel[2] = color
        }
    }

    return pixelData
}

fun quantizeInPlace(bb: MutableList<Float>, levelsCount: Int, gamma: Float) {
    val fLevels = (1 shl levelsCount - 1).toFloat()

    GammaConversion.applyGamma(bb, gamma)
    for (i in bb.indices) {
        val t1 = bb[i] * fLevels
        val t2 = t1.roundToInt() / fLevels
        bb[i] = t2
    }
    GammaConversion.applyReverseGamma(bb, gamma)
    bb.map { x -> clamp(x) }
}

fun clamp(value: Float): Float = value.coerceIn(0.0f, 1.0f)

fun drawInPlace(pixelData: PixelData, x: Int, y: Int, color: List<Float>, brightness: Float) {
    if (x !in 0..<pixelData.width) return
    if (y !in 0..<pixelData.height) return

    if (brightness !in 0f..1f) {
        log.info { "KEK! $x $y $color $brightness" }
    }
    val pixel = pixelData.getPixel(y, x) // kostyl
    for (n in pixel.indices) {
        pixel[n] = clamp(pixel[n] * (1 - brightness) + color[n] * brightness)
    }
}
