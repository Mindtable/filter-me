package ru.itmo.graphics.viewmodel.presentation.viewmodel

import ru.itmo.graphics.viewmodel.domain.Coordinates
import ru.itmo.graphics.viewmodel.domain.Pixel
import ru.itmo.graphics.viewmodel.domain.image.colorspace.ApplicationColorSpace
import ru.itmo.graphics.viewmodel.presentation.view.main.ImageChannel
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsType

sealed interface ImageEvent

object OpenFileEvent : ImageEvent
object OpeningFileEvent : ImageEvent
data class FileOpenedEvent(val absolutePath: String) : ImageEvent
data class SaveAsEvent(val path: String) : ImageEvent
object SaveEvent : ImageEvent
object StartSaveAsEvent : ImageEvent

data class ImageError(val error: Throwable) : ImageEvent
data object ImageErrorDismissed : ImageEvent

data class ChannelSettingsChanged(val channel: ImageChannel) : ImageEvent
data object MonochromeModeChanged : ImageEvent

data class ApplicationColorSpaceChanged(val colorSpace: ApplicationColorSpace) : ImageEvent

data class ConvertGamma(val newGamma: Float) : ImageEvent
data class AssignGamma(val newGamma: Float) : ImageEvent

data class OpenSettings(val settingsType: SettingsType) : ImageEvent
data object CloseSettings : ImageEvent
data object DarkModeSettingSwitch : ImageEvent

data class UpdateLineSettings(val lineColor: Pixel, val lineOpacity: Float, val lineWidth: Float) : ImageEvent
data object DrawingModeSwitch : ImageEvent
data class SendDrawingCoordinates(val coordinates: Coordinates) : ImageEvent
data object ComputeGradient : ImageEvent
