package ru.itmo.graphics.viewmodel.domain

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.skia.Bitmap
import ru.itmo.graphics.viewmodel.domain.model.ImageType
import ru.itmo.graphics.viewmodel.domain.model.image.Png
import ru.itmo.graphics.viewmodel.tools.png.writePngToFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.Instant

data class ImageModel(
    val file: File,
    val data: ByteArray,
    val type: ImageType,
    var bitmap: Bitmap?,
) {
    private val timeStamp = Instant.now().epochSecond
    private val log by lazy {
        KotlinLogging.logger { }
    }

    fun saveTo(
        fileName: String,
        bitmapToSave: Bitmap? = null,
        imageType: ImageType? = null,
        pixelData: PixelData? = null,
    ) {
        val finalBitmap: Bitmap? = bitmapToSave ?: bitmap
        val type: ImageType = imageType ?: type

        if (type.isSupported && finalBitmap != null) {
            log.info { "Save as PPM" }
            val byteStream = ByteArrayOutputStream()

            type.writeFile(byteStream, finalBitmap)

            File(fileName).writeBytes(byteStream.toByteArray())
        } else if (type is Png) {
            FileOutputStream(fileName).use {
                pixelData!!.writePngToFile(it)
            }
        } else {
            log.info { "Save by default way" }
            File(fileName).writeBytes(data)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageModel

        return timeStamp == other.timeStamp
    }

    override fun hashCode(): Int {
        return file.hashCode()
    }
}
