package ru.itmo.graphics.model

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.skia.Bitmap
import java.io.ByteArrayOutputStream
import java.io.File
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

    fun saveTo(fileName: String) {
        if (type.isSupported && bitmap != null) {
            log.info { "Save as PPM" }
            val byteStream = ByteArrayOutputStream()
            type.writeFile(byteStream, bitmap!!)
            File(fileName).writeBytes(byteStream.toByteArray())
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
