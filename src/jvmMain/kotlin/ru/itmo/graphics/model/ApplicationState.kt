package ru.itmo.graphics.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.oshai.kotlinlogging.KotlinLogging

class ApplicationState {

    var log: String? by mutableStateOf(null)
        private set

    var currentImageFileName: String? by mutableStateOf(null)
        private set

    private val lastSuccessfulStates: ArrayDeque<ApplicationStateSnapshot> by mutableStateOf(ArrayDeque())

    private val logger = KotlinLogging.logger { }

    fun onSaveButtonClick() {
        addStateToStack()
        logger.info { "onSaveButtonClick call" }
        log = "Saved"
    }

    fun onSavedAsButtonClick(fileName: String) {
        addStateToStack()
        logger.info { "onSavedAsButtonClick call with $fileName parameter" }
        log = "Saved as $fileName"
    }

    fun onOpenFileClick(fileName: String) {
        logger.info { "onOpenFileClick call with $fileName parameter" }
        addStateToStack()
        log = "Meta info\nFile name: $fileName"
        currentImageFileName = fileName
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
        logger.info { "Added state tu queue $element" }
        lastSuccessfulStates.addFirst(
            element,
        )

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
            val (lastLog, lastFileName) = lastSuccessfulStates.removeLast()
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
