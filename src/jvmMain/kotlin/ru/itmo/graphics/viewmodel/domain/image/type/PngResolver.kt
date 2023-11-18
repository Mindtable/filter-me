package ru.itmo.graphics.viewmodel.domain.image.type

import ru.itmo.graphics.viewmodel.domain.model.ImageType
import ru.itmo.graphics.viewmodel.domain.model.image.Png
import java.io.File

class PngResolver : TypeResolver {

    override fun resolve(file: File, data: ByteArray): ImageType? {
        if (data[0].toUByte() != 137.toUByte()) return null
        if (data[1].toUByte() != 80.toUByte()) return null
        if (data[2].toUByte() != 78.toUByte()) return null
        if (data[3].toUByte() != 71.toUByte()) return null
        if (data[4].toUByte() != 13.toUByte()) return null
        if (data[5].toUByte() != 10.toUByte()) return null
        if (data[6].toUByte() != 26.toUByte()) return null
        if (data[7].toUByte() != 10.toUByte()) return null

        return Png
    }
}
