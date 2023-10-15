package ru.itmo.graphics.viewmodel.presentation.viewmodel

import ru.itmo.graphics.viewmodel.presentation.viewmodel.FileDialogType.NONE

enum class FileDialogType {
    NONE,
    OPEN,
    SAVE,
}

fun FileDialogType.isOpen() = this != NONE
