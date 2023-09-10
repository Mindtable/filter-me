package ru.itmo.graphics.image.type

import ru.itmo.graphics.model.ImageType
import java.io.File

class P6TypeResolver : TypeResolver {

    override fun resolve(file: File, data: ByteArray): ImageType? {
        return if (data[0].toInt() == 'P'.code && data[1].toInt() == '6'.code) {
            ImageType.P6
        } else {
            null
        }
    }
}