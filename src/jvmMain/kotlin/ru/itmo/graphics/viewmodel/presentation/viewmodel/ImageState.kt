package ru.itmo.graphics.viewmodel.presentation.viewmodel

import org.jetbrains.skia.Bitmap
import ru.itmo.graphics.image.colorspace.ApplicationColorSpace
import ru.itmo.graphics.image.colorspace.RgbColorSpace
import ru.itmo.graphics.model.ImageModel
import ru.itmo.graphics.viewmodel.domain.PixelData
import ru.itmo.graphics.viewmodel.presentation.viewmodel.FileDialogType.NONE

data class ImageState(
    val log: String = "",
    val file: String? = null,
    val isError: Boolean = false,
    val openFileDialog: FileDialogType = NONE,
    val bitmap: Bitmap? = null,
    val imageModel: ImageModel? = null,
    val colorSpace: ApplicationColorSpace = RgbColorSpace,
    val pixelData: PixelData? = null,
    val showChannels: Map<Channel, Boolean> = mapOf(
        Channel.CHANNEL_ONE to true,
        Channel.CHANNEL_TWO to true,
        Channel.CHANNEL_THREE to true,
    ),
)
