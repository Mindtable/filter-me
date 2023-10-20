package ru.itmo.graphics.viewmodel.presentation.viewmodel

import ru.itmo.graphics.image.colorspace.ApplicationColorSpace

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
object MonochromeModeChanged : ImageEvent

data class ApplicationColorSpaceChanged(val colorSpace: ApplicationColorSpace) : ImageEvent
