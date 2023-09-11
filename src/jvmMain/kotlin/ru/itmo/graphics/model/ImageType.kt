package ru.itmo.graphics.model

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorInfo
import java.io.InputStream
import java.io.OutputStream

interface ImageType {
    val isSupported: Boolean
    val colorInfo: ColorInfo

    fun readHeader(inputStream: InputStream): ImageDimension
    fun readPixelInfo(inputStream: InputStream, pixelIndex: Int, byteArray: ByteArray)
    fun writeFile(outputStream: OutputStream, bitmap: Bitmap)
}