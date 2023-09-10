package ru.itmo.graphics.image.type

import ru.itmo.graphics.model.ImageType
import ru.itmo.graphics.model.image.PnmP6
import java.io.File

class P6TypeResolver : TypeResolver {

    override fun resolve(file: File, data: ByteArray): ImageType? {
        return if (data[0].toInt() == 'P'.code && data[1].toInt() == '6'.code) {
            PnmP6()
        } else {
            null
        }
    }
}