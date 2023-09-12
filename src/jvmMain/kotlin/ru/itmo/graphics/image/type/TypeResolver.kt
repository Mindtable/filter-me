package ru.itmo.graphics.image.type

import ru.itmo.graphics.model.ImageType
import java.io.File

interface TypeResolver {

    fun resolve(file: File, data: ByteArray): ImageType?
}
