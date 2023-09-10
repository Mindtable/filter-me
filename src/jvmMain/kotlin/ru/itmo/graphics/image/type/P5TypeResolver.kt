package ru.itmo.graphics.image.type

import ru.itmo.graphics.model.ImageType
import ru.itmo.graphics.model.image.PnmP5
import java.io.File

class P5TypeResolver : TypeResolver {

    override fun resolve(file: File, data: ByteArray): ImageType? {
        return if (data[0].toInt() == 'P'.code && data[1].toInt() == '5'.code) {
            PnmP5()
        } else {
            null
        }
    }
}