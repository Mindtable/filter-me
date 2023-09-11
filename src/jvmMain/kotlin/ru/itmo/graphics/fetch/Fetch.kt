package ru.itmo.graphics.fetch

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.image.type.FileTypeResolver
import ru.itmo.graphics.model.ApplicationState
import ru.itmo.graphics.model.ImageModel
import java.io.File

fun fetchImageModelUseCase(
    fileName: String,
    typeResolver: FileTypeResolver,
    applicationState: ApplicationState,
): ImageModel? {
    val logger = KotlinLogging.logger { }
    logger.info { "Constructing file $fileName" }

    return runCatching { File(fileName) }
        .mapCatching {
            logger.info { "Reading bytes from file" }
            val data = it.readBytes()

            logger.info { "Resolving file type" }
            val type = typeResolver.resolveType(it, data)

            ImageModel(
                file = it,
                data = data,
                type = type,
                bitmap = null,
            )
        }
        .logOnSuccess(logger)
        .logOnFailure(logger)
        .onFailure { applicationState.rollbackOnError("${it.message}") }
        .getOrNull()
}

private fun <T> Result<T>.logOnSuccess(logger: KLogger) =
    onSuccess { logger.info { "Reading bytes from file success" } }

private fun <T> Result<T>.logOnFailure(logger: KLogger) = onFailure {
    logger.info {
        "File read error. Msg: ${it.message}"
    }
    it.printStackTrace()
}
