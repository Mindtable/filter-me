package ru.itmo.graphics.model

import org.jetbrains.skia.ColorInfo
import java.io.InputStream

interface ImageType {
    val isSupported: Boolean
    val colorInfo: ColorInfo
    val bytesPerPixel: Int

    fun readHeader(inputStream: InputStream): ImageDimension
    fun readPixelInfo(inputStream: InputStream, pixelIndex: Int, byteArray: ByteArray)
}