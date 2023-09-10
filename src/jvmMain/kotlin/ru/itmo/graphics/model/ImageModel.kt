package ru.itmo.graphics.model

import java.io.File

data class ImageModel(
    val file: File,
    val data: ByteArray,
    val type: ImageType,
) {
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


