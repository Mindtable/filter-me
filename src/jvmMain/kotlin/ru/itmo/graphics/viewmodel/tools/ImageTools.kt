package ru.itmo.graphics.viewmodel.tools

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType.OPAQUE
import org.jetbrains.skia.ColorInfo
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType.RGB_888X
import org.jetbrains.skia.ImageInfo
import ru.itmo.graphics.image.colorspace.ApplicationColorSpace
import ru.itmo.graphics.model.ImageModel
import ru.itmo.graphics.viewmodel.domain.Pixel
import ru.itmo.graphics.viewmodel.domain.PixelData
import kotlin.system.measureTimeMillis

fun readImage(imageModel: ImageModel): Bitmap {
    val (file, byteArray, imageType) = imageModel

    if (!imageType.isSupported) {
        throw IllegalStateException("Image type not supported for image read function")
    }

    lateinit var pixelMap: ByteArray
    val width: Int
    val height: Int

    val timeInMillis = measureTimeMillis {
        println(file.absolutePath)

        val fileStream = byteArray.inputStream()

        val imageDimension = imageType.readHeader(fileStream)

        width = imageDimension.width
        height = imageDimension.height
        val totalPixels = width * height

        println("Picture is $width pixels by $height pixels (Total $totalPixels pixels)")

        val totalLen = totalPixels * imageType.colorInfo.bytesPerPixel

        pixelMap = ByteArray(totalLen) { 0 }

        for (i in 0 until totalPixels) {
            imageType.readPixelInfo(fileStream, i, pixelMap)
        }

        val totalMemory = totalLen.toFloat() * Byte.SIZE_BYTES
        println("Successfully read $totalLen color values. Total memory used: ${totalMemory / 1000 / 1000} Mb")

        fileStream.close()
    }

    println("Total time used to load: ${timeInMillis.toFloat() / 1000} s")

    val bitmap = Bitmap()
    bitmap.setImageInfo(
        ImageInfo(
            imageType.colorInfo,
            width,
            height,
        ),
    )
    bitmap.installPixels(pixelMap)
    imageModel.bitmap = bitmap

    return bitmap
}

fun readImageV2(
    imageModel: ImageModel,
): PixelData {
    val (file, byteArray, imageType) = imageModel

    if (!imageType.isSupported) {
        throw IllegalStateException("Image type not supported for image read function")
    }

    lateinit var pixelMap: ByteArray
    val width: Int
    val height: Int

    val pixelData: PixelData

    val timeInMillis = measureTimeMillis {
        println(file.absolutePath)

        val fileStream = byteArray.inputStream()

        val imageDimension = imageType.readHeader(fileStream)

        width = imageDimension.width
        height = imageDimension.height

        pixelData = PixelData(Array(height) { Array(width) { Pixel() } })
        val totalPixels = width * height

        println("Picture is $width pixels by $height pixels (Total $totalPixels pixels)")

        val totalLen = totalPixels * imageType.colorInfo.bytesPerPixel

        pixelMap = ByteArray(totalLen) { 0 }

        for (i in 0 ..< height) {
            for (j in 0 ..< width) {
                pixelData.data[i][j] = imageType.readPixelInfo(fileStream, i, pixelMap)
            }
        }

        val totalMemory = totalLen.toFloat() * Byte.SIZE_BYTES
        println("Successfully read $totalLen color values. Total memory used: ${totalMemory / 1000 / 1000} Mb")

        fileStream.close()
    }

    return pixelData
}

fun PixelData.toBitmap(
    colorSpace: ApplicationColorSpace,
    showChannelOne: Boolean,
    showChannelTwo: Boolean,
    showChannelThree: Boolean,
): Bitmap {
    val pixelMap2 = this.data
        .flatten()
        .map {
            pixel -> colorSpace.toRgb(Pixel(
                if (showChannelOne) pixel.channelOne else 0f,
                if (showChannelTwo) pixel.channelTwo else 0f,
                if (showChannelThree) pixel.channelThree else 0f
            ))
        }
        .flatMap {
            val (channelOne, channelTwo, channelThree) = it
            val transform = { x: Float -> (x * 255).toInt().toByte() }

            listOf(
                channelOne,
                channelTwo,
                channelThree,
                1.0f,
            ).map(transform)
        }
        .toTypedArray()
        .toByteArray()

    val bitmap = Bitmap()
    bitmap.setImageInfo(
        ImageInfo(
            ColorInfo(RGB_888X, OPAQUE, ColorSpace.sRGB),
            width,
            height,
        ),
    )
    bitmap.installPixels(pixelMap2)

    return bitmap
}

fun PixelData.convertColorSpace(
    oldColorSpace: ApplicationColorSpace,
    newColorSpace: ApplicationColorSpace
) {
    for (row in this.data.indices) {
        for (column in this.data[row].indices) {
            data[row][column] = newColorSpace.fromRgb(oldColorSpace.toRgb(data[row][column]))
        }
    }
}