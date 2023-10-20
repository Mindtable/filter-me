package ru.itmo.graphics.viewmodel.tools

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType.OPAQUE
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType.RGB_888X
import org.jetbrains.skia.ImageInfo
import ru.itmo.graphics.image.colorspace.ApplicationColorSpace
import ru.itmo.graphics.image.gamma.GammaConversion
import ru.itmo.graphics.model.ImageModel
import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.presentation.viewmodel.Channel
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

        for (i in 0 ..< height) {
            for (j in 0 ..< width) {
                imageType.readPixelInfo(fileStream, i, pixelData.getPixel(i, j))
            }
        }

        val totalMemory = totalLen.toFloat() * Float.SIZE_BYTES
        println("Successfully read $totalLen color values. Total memory used: ${totalMemory / 1000 / 1000} Mb")

        fileStream.close()
    }

    return pixelData
}

fun PixelData.toBitmap(
    colorSpace: ApplicationColorSpace,
    channel: Channel,
    isMonochromeMode: Boolean,
    gamma: Float = 0f,
): Bitmap {
    val byteArray = ByteArray(pixelCount * 4)

    val transform = { x: Float -> (x * 255).toInt().toByte() }

    val bb: MutableList<Float> = MutableList(3) { 0f }
    var pixel: MutableList<Float>

    val timeInMillis = measureTimeMillis {
        for (i in 0 ..< height) {
            for (j in 0 ..< width) {
                pixel = getPixel(i, j)
                bb[0] = pixel[0]
                bb[1] = pixel[1]
                bb[2] = pixel[2]

                if (isMonochromeMode) {
                    if (channel == Channel.CHANNEL_ONE) {
                        bb[1] = bb[0]
                        bb[2] = bb[0]
                    } else if (channel == Channel.CHANNEL_TWO) {
                        bb[0] = bb[1]
                        bb[2] = bb[1]
                    } else if (channel == Channel.CHANNEL_THREE) {
                        bb[0] = bb[2]
                        bb[1] = bb[2]
                    }
                } else {
                    if (channel != Channel.ALL) {
                        colorSpace.separateChannel(bb, channel)
                    }

                    colorSpace.toRgb(bb)
                }

                GammaConversion.applyGamma(bb, gamma)

                val channelOne = bb[0]
                val channelTwo = bb[1]
                val channelThree = bb[2]

                byteArray[(i * width + j) * 4 + 0] = transform(channelOne)
                byteArray[(i * width + j) * 4 + 1] = transform(channelTwo)
                byteArray[(i * width + j) * 4 + 2] = transform(channelThree)
                byteArray[(i * width + j) * 4 + 3] = transform(1.0f)
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

    for (row in 0..< this.width) {
        for (column in 0..< this.height) {
            pixel = getPixel(row, column)
            oldColorSpace.toRgb(pixel)
            newColorSpace.fromRgb(pixel)
        }
    }

    return this
}