package ru.itmo.graphics.model

import java.io.File

data class ImageModel(
    val file: File,
    val data: ByteArray,
    val type: ImageType,
) {
    fun saveTo(fileName: String) {
        // TODO: Add a way to access bitmap in ImageModel, to save properly

//        if (type.isSupported) {
//            val byteStream = ByteArrayOutputStream()
//            type.writeFile(byteStream, bitmap)
//            File(fileName).writeBytes(byteStream.toByteArray())
//        } else {
//            File(fileName).writeBytes(data)
//        }

        File(fileName).writeBytes(data)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageModel

        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        return file.hashCode()
    }
}


