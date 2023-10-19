package ru.itmo.graphics.viewmodel.presentation.viewmodel

sealed interface ImageEvent

object OpenFileEvent : ImageEvent
object OpeningFileEvent : ImageEvent
data class FileOpenedEvent(val absolutePath: String) : ImageEvent
data class SaveAsEvent(val path: String) : ImageEvent
object SaveEvent : ImageEvent
object StartSaveAsEvent : ImageEvent

data class ImageError(val error: Throwable) : ImageEvent
object ImageErrorDismissed : ImageEvent

data class ChannelSettingsChanged(val channel: Channel) : ImageEvent

object ApplicationColorSpaceChanged : ImageEvent
