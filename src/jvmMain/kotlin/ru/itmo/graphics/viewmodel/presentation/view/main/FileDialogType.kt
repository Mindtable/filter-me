package ru.itmo.graphics.viewmodel.presentation.view.main

import ru.itmo.graphics.viewmodel.presentation.view.main.FileDialogType.NONE

enum class FileDialogType {
    NONE,
    OPEN,
    SAVE,
}

fun FileDialogType.isOpen() = this != NONE
