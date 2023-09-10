package ru.itmo.graphics.image.type

import ru.itmo.graphics.model.ImageType
import java.io.File

class SkiaSupportedTypeResolver : TypeResolver {

    private val skiaSupportedFormats = setOf(
        "BMP",
        "GIF",
        "HEIF",
        "ICO",
        "JPEG",
        "PNG",
        "WBMP",
        "WebP",
    )

    override fun resolve(file: File, data: ByteArray): ImageType? {
        val extension = file.extension
        val isFileSupported = skiaSupportedFormats.any {
            it.equals(extension, ignoreCase = true)
        }
        return if (isFileSupported) {
            ImageType.SKIA_SUPPORTED
        } else {
            null
        }
    }
}
