package ru.itmo.graphics.viewmodel.domain.model

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorInfo
import ru.itmo.graphics.viewmodel.domain.ImageDimension
import java.io.InputStream
import java.io.OutputStream

interface ImageType {
    val isSupported: Boolean
    val colorInfo: ColorInfo

    fun readHeader(inputStream: InputStream): ImageDimension
    fun readPixelInfo(inputStream: InputStream, pixelIndex: Int, bb: MutableList<Float>)
    fun writeFile(outputStream: OutputStream, bitmap: Bitmap)
}
