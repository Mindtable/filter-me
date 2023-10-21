package ru.itmo.graphics.viewmodel.presentation.viewmodel

import org.jetbrains.skia.Bitmap
import ru.itmo.graphics.viewmodel.domain.ImageModel
import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.domain.image.colorspace.ApplicationColorSpace
import ru.itmo.graphics.viewmodel.domain.image.colorspace.RgbColorSpace
import ru.itmo.graphics.viewmodel.presentation.view.main.FileDialogType
import ru.itmo.graphics.viewmodel.presentation.view.main.FileDialogType.NONE
import ru.itmo.graphics.viewmodel.presentation.view.main.ImageChannel
import ru.itmo.graphics.viewmodel.presentation.view.settings.core.SettingsType

data class ImageState(
    val log: String = "",
    val file: String? = null,
    val isError: Boolean = false,
    val isMonochromeMode: Boolean = false,
    val openFileDialog: FileDialogType = NONE,
    val bitmap: Bitmap? = null,
    val imageModel: ImageModel? = null,
    val colorSpace: ApplicationColorSpace = RgbColorSpace,
    val pixelData: PixelData? = null,
    val channel: ImageChannel = ImageChannel.ALL,
    val gamma: Float = 0f,

    val settingsType: SettingsType? = null,
    val imageVersion: Long = 0,
    val isDarkMode: Boolean = true,
)
