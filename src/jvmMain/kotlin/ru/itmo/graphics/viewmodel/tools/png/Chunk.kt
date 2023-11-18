package ru.itmo.graphics.viewmodel.tools.png

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.DataInputStream
import java.util.zip.CRC32
import kotlin.math.min

private val log = KotlinLogging.logger { }

sealed class Chunk(
    val size: UInt,
    val rawData: ByteArray,
    val crc: UInt,
) {
    companion object
}

val Chunk.Companion.TYPE_LENGTH: Int
    get() = 4

sealed class KnownChunk(
    size: UInt,
    val type: ChunkType,
    rawData: ByteArray,
    crc: UInt,
) : Chunk(size, rawData, crc)

class IhdrChunk(
    size: UInt,
    rawData: ByteArray,
    crc: UInt,
) : KnownChunk(size, ChunkType.IHDR, rawData, crc)

class PlteChunk(
    size: UInt,
    rawData: ByteArray,
    crc: UInt,
) : KnownChunk(size, ChunkType.PLTE, rawData, crc)

class IdatChunk(
    size: UInt,
    rawData: ByteArray,
    crc: UInt,
) : KnownChunk(size, ChunkType.IDAT, rawData, crc)

class IendChunk(
    size: UInt,
    rawData: ByteArray,
    crc: UInt,
) : KnownChunk(size, ChunkType.IEND, rawData, crc)

class UnknownChunk(
    size: UInt,
    val type: String,
    rawData: ByteArray,
    crc: UInt,
) : Chunk(size, rawData, crc)

fun IhdrChunk.getPngImageInfo(): PngImageInfo {
    val stream = DataInputStream(rawData.inputStream())
    stream.readInt()

    val width = stream.readInt()
    val height = stream.readInt()
    val bitDepth = stream.read()
    val colorType = stream.read()
    val compressionMethod = stream.read()
    val filterType = stream.read()
    val interlace = stream.read()

    validateColorTypeAndBitDepth(colorType, bitDepth)
    if (compressionMethod != 0) throw InvalidPngStructureException()
    if (filterType != 0) throw InvalidPngStructureException()
    if (interlace != 0) throw InvalidPngStructureException()

    log.info {
        """

        [IHDR-DETAILS]
        width = $width
        height = $height
        bitDepth = $bitDepth
        colorType = $colorType
        compressionMethod = $compressionMethod
        filterType = $filterType
        interlace = $interlace
        """.trimIndent()
    }

    return PngImageInfo(
        width = width,
        height = height,
        bitDepth = bitDepth,
        colorType = colorType,
        isPaletteUsed = colorType % 2 == 1,
    )
}

fun PlteChunk.getPalette(): ByteArray {
    if ((size.toInt()) % 3 != 0) throw InvalidPngStructureException()

    val result = rawData.slice(4..<rawData.size).toByteArray()

    if (result.size != size.toInt()) throw InvalidPngStructureException()

    return result
}

fun Chunk.print(log: KLogger) {
    val dataString = rawData.slice(4..<min(rawData.size - 4, 21))
        .map { it.toString(16) }
        .joinToString(
            prefix = "[",
            postfix = "]",
            limit = 16,
        ) { it }

    val type = when (this) {
        is KnownChunk -> type.name
        is UnknownChunk -> type
    }

    log.info {
        """
            
        [CHUNK-INFO]
        size = $size
        type = $type
        data = $dataString
        crc = ${crc.toString(16)}
        """.trimIndent()
    }
}

fun Chunk.checkCrc() {
    val calculator = CRC32()
    calculator.update(rawData)

    log.info { "Crc computed by algorithm is ${calculator.value.toUInt().toString(16)}" }

    if (calculator.value.toUInt() != crc) throw InvalidPngStructureException()
}
