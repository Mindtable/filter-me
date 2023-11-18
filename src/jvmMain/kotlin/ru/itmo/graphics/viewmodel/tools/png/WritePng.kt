package ru.itmo.graphics.viewmodel.tools.png

import ru.itmo.graphics.viewmodel.domain.PixelData
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.zip.CRC32
import java.util.zip.Deflater

fun PixelData.writePngToFile(outputStream: OutputStream) {
    writePngHeader(outputStream)
    writeIhdr(outputStream)
//    writeGama(outputStream, gamma)
    writeRows(outputStream)
    writeIend(outputStream)
}

private fun writePngHeader(outputStream: OutputStream) {
    outputStream.write(137)
    outputStream.write(80)
    outputStream.write(78)
    outputStream.write(71)
    outputStream.write(13)
    outputStream.write(10)
    outputStream.write(26)
    outputStream.write(10)
}

private fun PixelData.writeIhdr(outputStream: OutputStream) {
    val baos = ByteArrayOutputStream()

    val size = 13
    val type = ChunkType.IHDR.name
    baos.writer().use {
        it.write(type)
    }
    baos.write(width.toByteArray())
    baos.write(height.toByteArray())
    baos.write(8)
    baos.write(2)
    baos.write(0)
    baos.write(0)
    baos.write(0)

    val data = baos.toByteArray()
    val crc = data.calculateCrc()

    outputStream.write(size.toByteArray())
    outputStream.write(data)
    outputStream.write(crc.toByteArray())
}

private fun writeGama(outputStream: OutputStream, gamma: Float) {
    val baos = ByteArrayOutputStream()
    val size = 4
    val tyep = "gAMA"
    baos.writer().use { it.write(tyep) }
    baos.write((gamma * 100000).toInt().toByteArray())

    val data = baos.toByteArray()
    val crc = data.calculateCrc()

    outputStream.write(size.toByteArray())
    outputStream.write(data)
    outputStream.write(crc.toByteArray())
}

private fun PixelData.writeRows(outputStream: OutputStream) {
    val baos = ByteArrayOutputStream()
    repeat(height) { y ->
        baos.write(0) // filter type
        repeat(width) { x ->
            getPixel(y, x).forEach {
                baos.write((it * 255).toInt())
            }
        }
    }

    val compressedOutputStream = ByteArrayOutputStream()
    compressedOutputStream.writer().use { it.write(ChunkType.IDAT.name) }
    val deflater = Deflater()
    deflater.setInput(baos.toByteArray())
    deflater.finish() // jerk

    val buffer = ByteArray(32768)
    do {
        val count = deflater.deflate(buffer)
        compressedOutputStream.write(buffer, 0, count)
    } while (count != 0)

    val compressedData = compressedOutputStream.toByteArray()

    val crc = compressedData.calculateCrc()

    outputStream.write((compressedData.size - 4).toByteArray())
    outputStream.write(compressedData)
    outputStream.write(crc.toByteArray())
}

private fun writeIend(outputStream: OutputStream) {
    val baos = ByteArrayOutputStream()
    val size = 0
    baos.writer().use { it.write(ChunkType.IEND.name) }
    val data = baos.toByteArray()
    val crc = data.calculateCrc()
    outputStream.write(size.toByteArray())
    outputStream.write(data)
    outputStream.write(crc.toByteArray())
}

private fun ByteArray.calculateCrc(): Int {
    val calculator = CRC32()
    calculator.update(this)

    return calculator.value.toInt()
}

private fun Int.toByteArray(): ByteArray = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(this).array()
