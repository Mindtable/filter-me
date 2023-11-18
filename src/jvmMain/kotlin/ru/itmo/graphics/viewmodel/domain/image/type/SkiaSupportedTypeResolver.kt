package ru.itmo.graphics.viewmodel.domain.image.type

import ru.itmo.graphics.viewmodel.domain.model.ImageType
import ru.itmo.graphics.viewmodel.domain.model.image.SkiaSupported
import java.io.File

class SkiaSupportedTypeResolver : TypeResolver {

    private val skiaSupportedFormats = setOf(
        "BMP",
        "GIF",
        "HEIF",
        "ICO",
        "JPEG",
        "JPG",
        "WBMP",
        "WebP",
    )

    override fun resolve(file: File, data: ByteArray): ImageType? {
        val extension = file.extension
        val isFileSupported = skiaSupportedFormats.any {
            it.equals(extension, ignoreCase = true)
        }
        return if (isFileSupported) {
            SkiaSupported()
        } else {
            null
        }
    }
}
