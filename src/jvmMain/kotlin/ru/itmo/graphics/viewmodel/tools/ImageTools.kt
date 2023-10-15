package ru.itmo.graphics.viewmodel.tools

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ImageInfo
import ru.itmo.graphics.model.ImageModel
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
