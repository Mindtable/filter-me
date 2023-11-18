package ru.itmo.graphics.viewmodel.tools.png

import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.viewmodel.domain.PixelData
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import java.util.zip.Inflater
import kotlin.math.abs

private val log = KotlinLogging.logger { }

fun DataInputStream.readPng(): PixelData {
    val stream = this
    val signature = stream.readNBytes(8)
    validatePngFileSignature(signature)

    val pngImageInfo = when (val ihdr = stream.readChunk().also { it.checkCrc() }) {
        is IhdrChunk -> ihdr.getPngImageInfo()
        else -> throw InvalidPngStructureException()
    }
    val chunks = mutableListOf<Chunk>()

    while (stream.available() > 0) {
        val chunk = stream.readChunk()
        chunk.checkCrc()

        chunks += chunk
        if (chunk is IendChunk) break
    }

    // imageData[7 * (1 + width * 4)]
    val imageData = getUncompressedImageData(chunks)

    return when {
        pngImageInfo.colorType == 0 -> imageData.readGrayscale(pngImageInfo)
        !pngImageInfo.isPaletteUsed -> imageData.readPngWithoutPallet(pngImageInfo)
        pngImageInfo.isPaletteUsed -> {
            val pltChunk = chunks.filterIsInstance<PlteChunk>().singleOrNull()
                ?: throw InvalidPngStructureException()

            imageData.readWithPalette(pngImageInfo, palette = pltChunk.getPalette())
        }

        else -> throw InvalidPngStructureException()
    }
}

fun ByteArray.readPngWithoutPallet(info: PngImageInfo): PixelData {
    val (width, height, _, colorType, _) = info
    // imageData[7 * (1 + width * 4)]
    val imageData = this

    val bytesInPixel = if (colorType == 6) 4 else 3
    val result = PixelData(MutableList(height * (1 + width * 3)) { 0f }, height, width)

    val distinctRowFilters = mutableSetOf<Byte>()

    var lastRowCache = mutableListOf<Int>()

    repeat(height) { y ->
        val rowOffset = y * (1 + width * bytesInPixel)
        val filterType = imageData[rowOffset]

        distinctRowFilters += filterType
        val currentRowCache = mutableListOf<Int>()

        fun sub(x: Int, channel: Int): Int =
            if (x >= 1) {
                currentRowCache[(x - 1) * 3 + channel]
            } else {
                0
            }

        fun up(x: Int, channel: Int): Int = if (y >= 1 && x >= 0) {
            lastRowCache[x * 3 + channel]
        } else {
            0
        }

        fun paethPredictor(a: Int, b: Int, c: Int): Int {
            val p = a + b - c
            val pa = abs(p - a)
            val pb = abs(p - b)
            val pc = abs(p - c)
            if (pa <= pb && pa <= pc) return a
            if (pb <= pc) return b
            return c
        }

        repeat(width) { x ->
            val pixel = result.getPixel(y, x)
            repeat(3) { channel ->
                var origin = imageData[rowOffset + 1 + x * bytesInPixel + channel].toUByte().toInt()

                val channelValue = when (filterType.toInt()) {
                    0 -> origin
                    1 -> {
                        origin + sub(x, channel)
                    }

                    2 -> {
                        origin + up(x, channel)
                    }

                    3 -> {
                        origin + (sub(x, channel) + up(x, channel)) / 2
                    }

                    4 -> {
                        origin + paethPredictor(sub(x, channel), up(x, channel), up(x - 1, channel))
                    }

                    else -> 0
                }
                val channelValueModulo = (channelValue + abs(channelValue) * 256) % 256
                pixel[channel] = channelValueModulo / 255f
                currentRowCache += channelValueModulo
            }
        }

        lastRowCache = currentRowCache
    }

    log.info { "Distinct row filters $distinctRowFilters" }
    return result
}

fun ByteArray.readGrayscale(info: PngImageInfo): PixelData {
    val (width, height, _, colorType, _) = info
    // imageData[7 * (1 + width * 4)]
    val imageData = this

    val result = PixelData(MutableList(height * (1 + width * 3)) { 0f }, height, width)

    val distinctRowFilters = mutableSetOf<Byte>()

    var lastRowCache = mutableListOf<Int>()

    repeat(height) { y ->
        val rowOffset = y * (1 + width)
        val filterType = imageData[rowOffset]

        distinctRowFilters += filterType
        val currentRowCache = mutableListOf<Int>()

        fun sub(x: Int): Int =
            if (x >= 1) {
                currentRowCache[x - 1]
            } else {
                0
            }

        fun up(x: Int): Int = if (y >= 1 && x >= 0) {
            lastRowCache[x]
        } else {
            0
        }

        fun paethPredictor(a: Int, b: Int, c: Int): Int {
            val p = a + b - c
            val pa = abs(p - a)
            val pb = abs(p - b)
            val pc = abs(p - c)
            if (pa <= pb && pa <= pc) return a
            if (pb <= pc) return b
            return c
        }

        repeat(width) { x ->
            val pixel = result.getPixel(y, x)
            var origin = imageData[rowOffset + 1 + x].toUByte().toInt()

            val channelValue = when (filterType.toInt()) {
                0 -> origin
                1 -> {
                    origin + sub(x)
                }

                2 -> {
                    origin + up(x)
                }

                3 -> {
                    origin + (sub(x) + up(x)) / 2
                }

                4 -> {
                    origin + paethPredictor(sub(x), up(x), up(x - 1))
                }

                else -> 0
            }
            val channelValueModulo = (channelValue + abs(channelValue) * 256) % 256
            pixel.forEachIndexed { index, _ -> pixel[index] = channelValueModulo / 255f }
            currentRowCache += channelValueModulo
        }

        lastRowCache = currentRowCache
    }

    log.info { "Distinct row filters $distinctRowFilters" }
    return result
}

fun ByteArray.readWithPalette(info: PngImageInfo, palette: ByteArray): PixelData {
    val (width, height, _, colorType, _) = info
    // imageData[7 * (1 + width * 4)]
    val imageData = this

    val result = PixelData(MutableList(height * (1 + width * 3)) { 0f }, height, width)

    val distinctRowFilters = mutableSetOf<Byte>()

    var lastRowCache = mutableListOf<Int>()

    repeat(height) { y ->
        val rowOffset = y * (1 + width)
        val filterType = imageData[rowOffset]

        distinctRowFilters += filterType
        val currentRowCache = mutableListOf<Int>()

        fun sub(x: Int): Int =
            if (x >= 1) {
                currentRowCache[x - 1]
            } else {
                0
            }

        fun up(x: Int): Int = if (y >= 1 && x >= 0) {
            lastRowCache[x]
        } else {
            0
        }

        fun paethPredictor(a: Int, b: Int, c: Int): Int {
            val p = a + b - c
            val pa = abs(p - a)
            val pb = abs(p - b)
            val pc = abs(p - c)
            if (pa <= pb && pa <= pc) return a
            if (pb <= pc) return b
            return c
        }

        repeat(width) { x ->
            val pixel = result.getPixel(y, x)
            var origin = imageData[rowOffset + 1 + x].toUByte().toInt()

            val channelValue = when (filterType.toInt()) {
                0 -> origin
                1 -> {
                    origin + sub(x)
                }

                2 -> {
                    origin + up(x)
                }

                3 -> {
                    origin + (sub(x) + up(x)) / 2
                }

                4 -> {
                    origin + paethPredictor(sub(x), up(x), up(x - 1))
                }

                else -> 0
            }
            val channelValueModulo = (channelValue + abs(channelValue) * 256) % 256
            pixel.forEachIndexed { i, _ ->
                pixel[i] = palette[channelValueModulo * 3 + i].toUByte().toInt() / 255f
            }
            currentRowCache += channelValueModulo
        }

        lastRowCache = currentRowCache
    }

    log.info { "Distinct row filters $distinctRowFilters" }
    return result
}

fun validatePngFileSignature(fileSignature: ByteArray) {
    if (fileSignature[0].toUByte() != 137.toUByte()) throw InvalidPngStructureException()
    if (fileSignature[1].toUByte() != 80.toUByte()) throw InvalidPngStructureException()
    if (fileSignature[2].toUByte() != 78.toUByte()) throw InvalidPngStructureException()
    if (fileSignature[3].toUByte() != 71.toUByte()) throw InvalidPngStructureException()
    if (fileSignature[4].toUByte() != 13.toUByte()) throw InvalidPngStructureException()
    if (fileSignature[5].toUByte() != 10.toUByte()) throw InvalidPngStructureException()
    if (fileSignature[6].toUByte() != 26.toUByte()) throw InvalidPngStructureException()
    if (fileSignature[7].toUByte() != 10.toUByte()) throw InvalidPngStructureException()
}

fun validateColorTypeAndBitDepth(colorType: Int, bitDepth: Int) {
    if (colorType !in setOf(0, 2, 3, 6)) throw InvalidPngStructureException()
    if (bitDepth != 8) throw InvalidPngStructureException()
    if (colorType == 6) log.warn { "Warning! Images with colorType 6 is only partially supported" }
}

fun DataInputStream.readChunk(): Chunk {
    val size = readInt().toUInt()
    val rawData = readNBytes(Chunk.TYPE_LENGTH + size.toInt())
    val type = UTF_8.decode(ByteBuffer.wrap(rawData, 0, 4)).toString()
    val crc = readInt().toUInt()

    return when (type.toChunkTypeOrUnknown()) {
        ChunkType.IHDR -> IhdrChunk(
            size = size,
            crc = crc,
            rawData = rawData,
        ).also { it.print(log) }

        ChunkType.IEND -> IendChunk(
            size = size,
            crc = crc,
            rawData = rawData,
        ).also { it.print(log) }

        ChunkType.IDAT -> IdatChunk(
            size = size,
            crc = crc,
            rawData = rawData,
        ).also { it.print(log) }

        ChunkType.PLTE -> PlteChunk(
            size = size,
            crc = crc,
            rawData = rawData,
        ).also { it.print(log) }

        ChunkType.UNKNOWN -> UnknownChunk(
            size = size,
            type = type,
            crc = crc,
            rawData = rawData,
        ).also { it.print(log) }
    }
}

fun getUncompressedImageData(chunks: List<Chunk>): ByteArray {
    val inflater = Inflater()
    val idatChunks = chunks.filterIsInstance<IdatChunk>()
    val buffer = ByteArray(idatChunks.maxOf { it.size }.toInt())

    val result = ByteArrayOutputStream()

    result.use {
        idatChunks.forEach {
            inflater.setInput(ByteBuffer.wrap(it.rawData, 4, it.rawData.size - 4))
            do {
                val count = inflater.inflate(buffer)
                result.write(buffer, 0, count)
            } while (count != 0)
        }
    }

    return result.toByteArray()
}

class InvalidPngStructureException() : RuntimeException()

enum class ChunkType {
    IDAT,
    IHDR,
    IEND,
    PLTE,
    UNKNOWN,
}

fun String.toChunkTypeOrUnknown(): ChunkType = runCatching { ChunkType.valueOf(this) }
    .fold(
        onSuccess = { it },
        onFailure = { ChunkType.UNKNOWN },
    )

data class PngImageInfo(
    val width: Int,
    val height: Int,
    val bitDepth: Int,
    val colorType: Int,
    val isPaletteUsed: Boolean,
)
