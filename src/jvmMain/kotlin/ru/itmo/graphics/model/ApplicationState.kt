package ru.itmo.graphics.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.itmo.graphics.fetch.fetchImageModelUseCase
import ru.itmo.graphics.image.type.FileTypeResolver
import ru.itmo.graphics.image.type.P5TypeResolver
import ru.itmo.graphics.image.type.P6TypeResolver
import ru.itmo.graphics.image.type.SkiaSupportedTypeResolver
import java.io.File

class ApplicationState {

    var log: String? by mutableStateOf(null)
        private set

    var currentImageFileName: String? by mutableStateOf(null)
        private set

    private val typeResolver: FileTypeResolver = FileTypeResolver(
        listOf(
            P5TypeResolver(),
            P6TypeResolver(),
            SkiaSupportedTypeResolver(),
        ),
    )

    var image: ImageModel? by mutableStateOf(
        currentImageFileName?.let {
            fetchImageModelUseCase(it, typeResolver, this)
        },
    )

    private val lastSuccessfulStates: ArrayDeque<ApplicationStateSnapshot> by mutableStateOf(ArrayDeque())

    private val logger = KotlinLogging.logger { }

    fun onSaveButtonClick() {
        addStateToStack()
        logger.info { "onSaveButtonClick call" }
        image?.let {
            it.saveTo(it.file.absolutePath)
        }
        log = "Saved"
    }

    fun onAnySuccess() {
        addStateToStack()
    }

    fun onSavedAsButtonClick(fileName: String) {
        addStateToStack()
        logger.info { "onSavedAsButtonClick call with $fileName parameter" }
        image?.saveTo(fileName)
        log = "Saved as $fileName"
    }

    fun onOpenFileClick(fileName: String) {
        logger.info { "onOpenFileClick call with $fileName parameter" }
        addStateToStack()
        log = "Meta info\nFile name: $fileName"
        currentImageFileName = fileName
        image = currentImageFileName?.let {
            fetchImageModelUseCase(it, typeResolver, this)
        }
    }

    fun rollbackOnError(errorMsg: String) {
        logger.info { "rollbackOnError call with $errorMsg parameter" }
        unfoldLastSuccessfulState()
        log = "An error occurred\nMessage: $errorMsg"
    }

    private fun addStateToStack() {
        val element = ApplicationStateSnapshot(
            log = log,
            fileName = currentImageFileName,
        )
        lastSuccessfulStates.addFirst(
            element,
        )
        logger.info { "Added state to queue $element. Current size is ${lastSuccessfulStates.size}" }

        if (lastSuccessfulStates.size >= 20) {
            lastSuccessfulStates.removeLast()
            logger.info { "Removed oldest state from stack" }
        }
    }

    private fun unfoldLastSuccessfulState() {
        if (lastSuccessfulStates.isEmpty()) {
            log = null
            currentImageFileName = null
        } else {
            val (lastLog, lastFileName) = lastSuccessfulStates.removeFirst()
            logger.info { "Restored state $lastLog $lastFileName" }

            log = lastLog
            currentImageFileName = lastFileName
        }
    }
}

private data class ApplicationStateSnapshot(
    val log: String?,
    val fileName: String?,
)
