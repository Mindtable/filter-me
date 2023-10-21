package ru.itmo.graphics.viewmodel.domain.image.type

import ru.itmo.graphics.viewmodel.domain.model.ImageType
import java.io.File

interface TypeResolver {

    fun resolve(file: File, data: ByteArray): ImageType?
}
