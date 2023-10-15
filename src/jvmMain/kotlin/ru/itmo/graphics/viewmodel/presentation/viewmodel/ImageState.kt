package ru.itmo.graphics.viewmodel.presentation.viewmodel

import org.jetbrains.skia.Bitmap
import ru.itmo.graphics.model.ImageModel
import ru.itmo.graphics.viewmodel.presentation.viewmodel.FileDialogType.NONE

data class ImageState(
    val log: String = "",
    val file: String? = null,
    val isError: Boolean = false,
    val openFileDialog: FileDialogType = NONE,
    val bitmap: Bitmap? = null,
    val imageModel: ImageModel? = null,
)
