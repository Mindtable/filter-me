package ru.itmo.graphics.viewmodel.domain.image.type

import ru.itmo.graphics.viewmodel.domain.model.ImageType
import java.io.File

class FileTypeResolver(
    private val typeResolvers: Collection<TypeResolver>,
) {

    fun resolveType(file: File, data: ByteArray): ImageType {
        val result = typeResolvers.mapNotNull {
            it.resolve(file, data)
        }

        return when (result.size) {
            0 -> throw IllegalStateException("No suitable file type found for data")
            1 -> result.first()
            else -> throw IllegalStateException("Found more than one available file types: $result")
        }
    }
}
